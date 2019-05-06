package guo.ping.seckill.controller.goods;

import guo.ping.seckill.domain.OrderInfo;
import guo.ping.seckill.domain.SecKillOrder;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.service.GoodsService;
import guo.ping.seckill.service.OrderService;
import guo.ping.seckill.service.SecKillService;
import guo.ping.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description: 秒杀商品Controller
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 6:18 PM
 * @project: seckill
 */
@Controller
@RequestMapping("/seckill")
public class SecKillController {

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SecKillService secKillService;

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

        // 进入订单详情页
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);
        return "order_detail";
    }
}
