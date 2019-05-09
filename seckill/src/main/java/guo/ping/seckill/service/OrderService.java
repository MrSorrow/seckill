package guo.ping.seckill.service;

import guo.ping.seckill.dao.OrderDao;
import guo.ping.seckill.domain.OrderInfo;
import guo.ping.seckill.domain.SecKillOrder;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.redis.OrderKey;
import guo.ping.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @description: 订单Service
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 8:32 PM
 * @project: seckill
 */
@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取指定用户指定商品的订单
     * @param userId
     * @param goodsId
     * @return
     */
    public SecKillOrder getSecKillOrderByUserIdAndGoodsId(Long userId, Long goodsId) {
        // 先从缓存中查看，如果没有从数据库中查询
        SecKillOrder secKillOrder = getSecKillOrderByUserIdAndGoodsIdFromCache(userId, goodsId);
        return secKillOrder != null ? secKillOrder : orderDao.getSecKillOrderByUserIdAndGoodsId(userId, goodsId);
    }

    /**
     * 秒杀下单
     * @param user 用户
     * @param goods 用户要秒杀的商品
     * @return
     */
    @Transactional
    public OrderInfo createSecKillOrder(User user, GoodsVO goods) {
        // 插入普通订单记录
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsCount(1);
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setCreateDate(new Date());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insertOrderInfo(orderInfo);

        // 插入秒杀订单记录
        SecKillOrder secKillOrder = new SecKillOrder();
        secKillOrder.setGoodsId(goods.getId());
        secKillOrder.setOrderId(orderInfo.getId());
        secKillOrder.setUserId(user.getId());
        orderDao.insertSecKillOrder(secKillOrder);

        // 在缓存中插入秒杀订单记录
        insertSecKillOrderInCache(secKillOrder);

        return orderInfo;
    }

    /**
     * 从缓存中查询是否包含用户秒杀商品订单
     * @param userId
     * @param goodsId
     * @return
     */
    private SecKillOrder getSecKillOrderByUserIdAndGoodsIdFromCache(Long userId, Long goodsId) {
        return (SecKillOrder) redisTemplate.opsForValue().get(OrderKey.secKillOrderKey.getPrefix()
                + ":" + userId + ":" + goodsId);
    }

    /**
     * 在缓存中插入秒杀订单
     * @param secKillOrder
     */
    private void insertSecKillOrderInCache(SecKillOrder secKillOrder) {
        redisTemplate.opsForValue().set(OrderKey.secKillOrderKey.getPrefix()
                + ":" + secKillOrder.getUserId() + ":" + secKillOrder.getGoodsId(), secKillOrder);
    }
}
