package guo.ping.seckill.dao;

import guo.ping.seckill.vo.GoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @description: 商品dao
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 10:58 AM
 * @project: seckill
 */
@Mapper
public interface GoodsDao {

    @Select("SELECT g.*, mg.miaosha_price, mg.stock_count, mg.start_date, mg.end_date FROM goods g RIGHT JOIN miaosha_goods mg ON g.id = mg.goods_id")
    List<GoodsVO> getGoodsVOs();

    @Select("SELECT g.*, mg.miaosha_price, mg.stock_count, mg.start_date, mg.end_date FROM goods g RIGHT JOIN miaosha_goods mg ON g.id = mg.goods_id WHERE g.id = #{goodsId}")
    GoodsVO getGoodsDetailById(@Param("goodsId") Long goodsId);
}
