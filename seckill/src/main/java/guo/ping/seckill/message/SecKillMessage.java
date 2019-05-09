package guo.ping.seckill.message;

import guo.ping.seckill.domain.User;

/**
 * @description: 用户秒杀请求消息，用于MQ发送接收
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/9 3:26 PM
 * @project: seckill
 */
public class SecKillMessage {

    private User user;
    private Long goodsId;

    public SecKillMessage() {
    }

    public SecKillMessage(User user, Long goodsId) {
        this.user = user;
        this.goodsId = goodsId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }
}
