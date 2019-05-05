package guo.ping.seckill.redis;

/**
 * @description: 用户登录信息存储在Redis中
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/5 1:42 PM
 * @project: seckill
 */
public class UserKey extends BasePrefix{

    /**
     * Session过期时间 1天
     */
    private static final int TOKEN_EXPIRE = 3600 * 24 * 1;

    /**
     * 用户登录key
     */
    public static final UserKey userKey = new UserKey(TOKEN_EXPIRE, "token");

    public UserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}
