package guo.ping.seckill.mq;

import guo.ping.seckill.config.RabbitMQConfig;
import guo.ping.seckill.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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
}
