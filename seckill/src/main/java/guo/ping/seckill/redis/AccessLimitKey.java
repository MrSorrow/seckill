package guo.ping.seckill.redis;

/**
 * @description: 接口限流的Key
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/10 2:57 PM
 * @project: seckill
 */
public class AccessLimitKey extends BasePrefix {

    /**
     * 接口访问限流次数key
     */
    private static final String ACCESS_PREFIX = "access_limit_key";

    public static AccessLimitKey getAccessKeyWithExpire(int expireSeconds) {
        return new AccessLimitKey(expireSeconds, ACCESS_PREFIX);
    }


    private AccessLimitKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
