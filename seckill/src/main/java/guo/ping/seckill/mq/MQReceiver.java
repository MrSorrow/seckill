package guo.ping.seckill.mq;

import guo.ping.seckill.config.RabbitMQConfig;
import guo.ping.seckill.domain.OrderInfo;
import guo.ping.seckill.domain.SecKillOrder;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.message.SecKillMessage;
import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.service.GoodsService;
import guo.ping.seckill.service.OrderService;
import guo.ping.seckill.service.SecKillService;
import guo.ping.seckill.vo.GoodsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * @description: 消息队列接收者
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/9 11:23 AM
 * @project: seckill
 */
@Component
@RabbitListener(queues = RabbitMQConfig.QUEUE)
public class MQReceiver {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private SecKillService secKillService;

    /**
     * 接收User类型的消息
     * @param user
     */
    @RabbitHandler
    public void receiveUseMessage(@Payload User user) {
        logger.info(user.getNickname());
    }

    /**
     * 接收普通String类型的消息
     * @param msg
     */
    @RabbitHandler
    public void receiveStringMessage(@Payload String msg) {
        logger.info(msg);
    }

    @RabbitHandler
    public void receiveSecKillMessage(@Payload SecKillMessage secKillMessage) {
        User user = secKillMessage.getUser();
        Long goodsId = secKillMessage.getGoodsId();

        logger.info("收到用户" + user.getId() + "秒杀" + goodsId + "商品请求");

        // 判断库存
        GoodsVO goods = goodsService.getGoodsDetailById(goodsId);
        if (goods.getStockCount() <= 0) {
            return;
        }
        // 判断是否已经秒杀到商品，防止一人多次秒杀成功
        SecKillOrder order = orderService.getSecKillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {
            return;
        }

        // 正常进入秒杀流程：1.减少库存，2.创建订单，3-写入秒杀订单 三步需要原子操作
        OrderInfo orderInfo = secKillService.secKill(user, goods);

        if (orderInfo != null) {
            // 秒杀成功

        } else {
            // 秒杀失败
        }
    }
}
