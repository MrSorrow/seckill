package guo.ping.seckill.mq;

import guo.ping.seckill.config.RabbitMQConfig;
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
     * 发送消息方法
     * @param message 消息对象
     */
    public void sendMessage(Object message) {
        logger.info(message.toString());
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, message);
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE, "hhh");
    }
}
