package guo.ping.seckill.service;

import guo.ping.seckill.dao.GoodsDao;
import guo.ping.seckill.domain.Goods;
import guo.ping.seckill.domain.SecKillGoods;
import guo.ping.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: 商品Service
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 10:57 AM
 * @project: seckill
 */
@Service
public class GoodsService {

    @Autowired
    private GoodsDao goodsDao;

    /**
     * 获取所有秒杀商品详情
     * @return
     */
    public List<GoodsVO> getGoodsVOs() {
        return goodsDao.getGoodsVOs();
    }

    /**
     * 获取某款商品详细信息
     * @param goodsId
     * @return
     */
    public GoodsVO getGoodsDetailById(Long goodsId) {
        return goodsDao.getGoodsDetailById(goodsId);
    }

    /**
     * 减小库存
     * @param goods 对goods进行的库存进行更新
     */
    public void reduceStock(GoodsVO goods) {
        SecKillGoods g = new SecKillGoods();
        g.setId(goods.getId());
        g.setStockCount(goods.getStockCount() - 1);
        goodsDao.updateStock(g);
    }
}
