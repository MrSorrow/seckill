package guo.ping.seckill.controller.goods;

import guo.ping.seckill.domain.OrderInfo;
import guo.ping.seckill.domain.SecKillOrder;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.message.SecKillMessage;
import guo.ping.seckill.mq.MQSender;
import guo.ping.seckill.redis.GoodsKey;
import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.result.ServerResponse;
import guo.ping.seckill.service.GoodsService;
import guo.ping.seckill.service.OrderService;
import guo.ping.seckill.service.SecKillService;
import guo.ping.seckill.vo.GoodsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @description: 秒杀商品Controller
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 6:18 PM
 * @project: seckill
 */
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SecKillService secKillService;
    @Autowired
    private MQSender mqSender;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 内存标记Map，key为商品id，value为是否秒杀结束，用于减少对Redis的访问
     * 该内存标记非线程安全，但不会影响功能，只是有多个线程多次复写某商品卖完
     */
    private HashMap<Long, Boolean> goodsSecKillOverMap = new HashMap<>();

    @RequestMapping("/kill")
    public String secKill(Model model, User user, @RequestParam("goodsId") Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);

        GoodsVO goods = goodsService.getGoodsDetailById(goodsId);
        // 判断库存
        if (goods.getStockCount() <= 0) {
            model.addAttribute("errorMsg", CodeMsg.SECKILL_OVER.getMsg());
            return "kill_fail";
        }
        // 判断是否已经秒杀到商品，防止一人多次秒杀成功
        SecKillOrder order = orderService.getSecKillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errorMsg", CodeMsg.SECKILL_REPEAT.getMsg());
            return "kill_fail";
        }

        // 正常进入秒杀流程：1.减少库存，2.创建订单，3-写入秒杀订单 三步需要原子操作
        OrderInfo orderInfo = secKillService.secKill(user, goods);

        if (orderInfo != null) {
            // 进入订单详情页
            model.addAttribute("orderInfo", orderInfo);
            model.addAttribute("goods", goods);
            return "order_detail";
        } else {
            model.addAttribute("errorMsg", CodeMsg.SECKILL_OVER.getMsg());
            return "kill_fail";
        }
    }

    @RequestMapping("/asynckill")
    @ResponseBody
    public String asyncSecKill(Model model, User user, @RequestParam("goodsId") Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);

        // 先访问内存标记，查看是否卖完
        if (goodsSecKillOverMap.containsKey(goodsId) && goodsSecKillOverMap.get(goodsId)) {
            model.addAttribute("errorMsg", CodeMsg.SECKILL_OVER.getMsg());
            return "kill_fail";
        }

        // Redis预减库存
        Long nowStock = redisTemplate.opsForValue().decrement(GoodsKey.goodsStockKey.getPrefix() + ":" + goodsId);
        logger.info("商品" + goodsId + "预减库存完Redis当前库存数量为：" + nowStock);

        // 如果库存预减完毕，则直接返回秒杀失败
        if (nowStock < 0) {
            // 记录当前商品秒杀完毕
            goodsSecKillOverMap.put(goodsId, true);
            model.addAttribute("errorMsg", CodeMsg.SECKILL_OVER.getMsg());
            return "kill_fail";
        }

        // 判断是否已经秒杀到商品，防止一人多次秒杀成功
        // TODO: 需要将订单缓存到Redis中，这样查询效率更高，减少数据库访问开销
        SecKillOrder order = orderService.getSecKillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {
            model.addAttribute("errorMsg", CodeMsg.SECKILL_REPEAT.getMsg());
            return "kill_fail";
        }

        // 正常进入秒杀流程：入队进行异步下单
        mqSender.sendSecKillMessage(new SecKillMessage(user, goodsId));
        model.addAttribute("killMsg", CodeMsg.SECKILL_WAITTING.getMsg());
        return "kill_wait";
    }

    /**
     * 用于客户端轮询秒杀结果的接口
     * @param model
     * @param user
     * @param goodsId
     * @return -1：秒杀失败 0-秒杀排队中 orderId-秒杀成功
     */
    @RequestMapping("/result")
    @ResponseBody
    public ServerResponse<Long> asyncSecKillResult(Model model, User user, @RequestParam("goodsId") Long goodsId) {
        if (user == null) {
            return ServerResponse.error(CodeMsg.SESSION_ERROR);
        }
        model.addAttribute("user", user);

        // 获取用户秒杀商品结果
        Long status = secKillService.getSecKillResult(user.getId(), goodsId);
        return ServerResponse.success(status);
    }

    /**
     * Spring提供的初始化后方法，在当前bean初始化后会被执行
     * 利用该方法在系统初始化时加载库存到Redis
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVO> goodsVOs = goodsService.getGoodsVOs();
        if (goodsVOs != null && goodsVOs.size() > 0) {
            for (GoodsVO goods : goodsVOs) {
                redisTemplate.opsForValue().set(GoodsKey.goodsStockKey.getPrefix() + ":" + goods.getId(),
                        goods.getStockCount());
            }
        }
    }
}
