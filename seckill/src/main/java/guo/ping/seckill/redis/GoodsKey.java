package guo.ping.seckill.redis;

/**
 * @description: 商品相关的Redis存储key
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/9 3:11 PM
 * @project: seckill
 */
public class GoodsKey extends BasePrefix {

    /**
     * 商品库存key
     */
    public static final GoodsKey goodsStockKey = new GoodsKey(0, "goodsStock");
    /**
     * 商品秒杀活动是否结束key
     */
    public static final GoodsKey goodsSecKillOverKey = new GoodsKey(0, "goodsSecKillIsOver");

    public GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
