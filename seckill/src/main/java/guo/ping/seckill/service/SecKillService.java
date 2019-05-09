package guo.ping.seckill.service;

import guo.ping.seckill.domain.OrderInfo;
import guo.ping.seckill.domain.SecKillOrder;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.redis.GoodsKey;
import guo.ping.seckill.vo.GoodsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description: 秒杀Service，需要保证原子性
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 8:39 PM
 * @project: seckill
 */
@Service
public class SecKillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 减少库存，下订单，写入秒杀订单
     * 需要用事务保证原子性，默认传播方式是REQUIRED，没事务创建事务，有事务事务
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo secKill(User user, GoodsVO goods) {
        // 减少库存
        int updateRows = goodsService.reduceStock(goods);
        logger.info("当前库存为：" + goods.getStockCount() + "，减少库存更新记录数为：" + updateRows);
        // 减少库存成功才进行秒杀下单
        if (updateRows == 1) {
            return orderService.createSecKillOrder(user, goods);
        }
        // 如果库存没有更新成功，则不能进行下单
        else {
            // 没有秒杀成功，说明该款商品秒杀结束了
            setSecKillOver(goods.getId());
            return null;
        }
    }

    /**
     * 获取用户秒杀商品结果
     * -1：秒杀失败 0-秒杀排队中 orderId-秒杀成功
     * @param userId
     * @param goodsId
     * @return
     */
    public Long getSecKillResult(Long userId, Long goodsId) {
        SecKillOrder order = orderService.getSecKillOrderByUserIdAndGoodsId(userId, goodsId);
        if (order != null) {
            // 秒杀成功
            return order.getOrderId();
        }
        // 没有秒杀成功，可能秒杀失败，也可能还在排队，所以需要Redis设置活动结束标记
        boolean isOver = getSecKillOver(goodsId);
        return isOver ? -1L : 0L;
    }

    /**
     * 在Redis中设置某商品的秒杀活动是否结束
     * @param goodsId
     */
    private void setSecKillOver(Long goodsId) {
        redisTemplate.opsForValue().set(GoodsKey.goodsSecKillOverKey.getPrefix() + ":" + goodsId, true);
    }

    /**
     * 查询某商品的秒杀活动是否结束
     * @param goodsId
     * @return
     */
    private boolean getSecKillOver(Long goodsId) {
        return redisTemplate.hasKey(GoodsKey.goodsSecKillOverKey.getPrefix() + ":" + goodsId);
    }
}
