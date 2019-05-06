package guo.ping.seckill.dao;

import guo.ping.seckill.domain.Goods;
import guo.ping.seckill.domain.SecKillGoods;
import guo.ping.seckill.vo.GoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 查询所有的秒杀商品列表
     * @return
     */
    @Select("SELECT g.*, mg.miaosha_price, mg.stock_count, mg.start_date, mg.end_date FROM goods g RIGHT JOIN miaosha_goods mg ON g.id = mg.goods_id")
    List<GoodsVO> getGoodsVOs();

    /**
     * 查询单个秒杀商品详情
     * @param goodsId
     * @return
     */
    @Select("SELECT g.*, mg.miaosha_price, mg.stock_count, mg.start_date, mg.end_date FROM goods g INNER JOIN miaosha_goods mg ON g.id = mg.goods_id AND g.id = #{goodsId}")
    GoodsVO getGoodsDetailById(@Param("goodsId") Long goodsId);

    /**
     * 更新库存
     * @param g
     * @return
     */
    @Update("UPDATE miaosha_goods SET stock_count = #{stockCount} WHERE goods_id = #{goodsId}")
    int updateStock(SecKillGoods g);
}
