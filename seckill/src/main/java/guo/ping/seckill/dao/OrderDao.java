package guo.ping.seckill.dao;

import guo.ping.seckill.domain.OrderInfo;
import guo.ping.seckill.domain.SecKillOrder;
import org.apache.ibatis.annotations.*;

/**
 * @description: 订单Dao
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 8:57 PM
 * @project: seckill
 */
@Mapper
public interface OrderDao {

    /**
     * 获取指定用户指定商品的订单
     * @param userId
     * @param goodsId
     * @return
     */
    @Select("SELECT * FROM miaosha_order WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    SecKillOrder getSecKillOrderByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Insert("INSERT INTO order_info VALUES (NULL, #{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{deliveryAddrId}, #{orderChannel}, #{status}, #{createDate}, NULL)")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = Long.class, before = false, statement = "SELECT last_insert_id()")
    Long insertOrderInfo(OrderInfo orderInfo);

    @Insert("INSERT INTO miaosha_order VALUES (NULL, #{userId}, #{orderId}, #{goodsId})")
    Long insertSecKillOrder(SecKillOrder secKillOrder);
}
