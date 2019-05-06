package guo.ping.seckill.vo;

import guo.ping.seckill.domain.Goods;

import java.util.Date;

/**
 * @description: 联合普通商品表信息和秒杀商品表的信息
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/6 11:06 AM
 * @project: seckill
 */
public class GoodsVO extends Goods {

    private Double miaoshaPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;

    public Double getMiaoshaPrice() {
        return miaoshaPrice;
    }

    public void setMiaoshaPrice(Double miaoshaPrice) {
        this.miaoshaPrice = miaoshaPrice;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
