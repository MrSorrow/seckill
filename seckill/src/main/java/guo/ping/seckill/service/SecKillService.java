package guo.ping.seckill.service;

import guo.ping.seckill.domain.OrderInfo;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private OrderService orderService;

    /**
     * 减少库存，下订单，写入秒杀订单
     * 需要用事务保证原子性，默认传播方式是REQUIRED，没事务创建事务，有事务延用事务
     * @param user
     * @param goods
     * @return
     */
    @Transactional
    public OrderInfo secKill(User user, GoodsVO goods) {
        // 减少库存
        goodsService.reduceStock(goods);
        // 秒杀下单
        return orderService.createSecKillOrder(user, goods);
    }
}
