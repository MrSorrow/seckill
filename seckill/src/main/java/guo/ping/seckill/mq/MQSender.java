package guo.ping.seckill.mq;

import guo.ping.seckill.config.RabbitMQConfig;
import guo.ping.seckill.message.SecKillMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @description: 消息的发送者
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/9 12:09 AM
 * @project: seckill
 */
@Component
public class MQSender {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送秒杀请求消息，包含用户和商品id
     * @param secKillMessage
     */
    public void sendSecKillMessage(SecKillMessage secKillMessage) {
        logger.info("用户" + secKillMessage.getUser().getId() + "发起秒杀" + secKillMessage.getGoodsId() + "商品请求");
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, secKillMessage);
    }
}
