package guo.ping.seckill.redis;

/**
 * @description: 基础KeyPrefix的实现
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/5 11:22 AM
 * @project: seckill
 */
public class BasePrefix implements KeyPrefix {

    private int expireSeconds;
    private String prefix;

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    public BasePrefix(String prefix) {
        // 过期时间默认0表示不过期
        this(0, prefix);
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
