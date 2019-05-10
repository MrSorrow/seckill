package guo.ping.seckill.service;

import guo.ping.seckill.dao.UserDao;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.exception.GlobalException;
import guo.ping.seckill.redis.UserKey;
import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.utils.MD5Util;
import guo.ping.seckill.utils.UUIDUtil;
import guo.ping.seckill.vo.LoginInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @description: 用户Service
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/3 7:59 PM
 * @project: seckill
 */
@Service
public class UserService {

    public static final String COOKIE_TOKEN_NAME = "TOKEN";

    @Autowired
    private UserDao userDao;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public User getUserById(Long id) {
        return userDao.getUserById(id);
    }

    /**
     * 登录服务，包含第二次MD5加密的逻辑
     * @param loginInfoVO
     * @return
     */
    public String login(HttpServletResponse response, LoginInfoVO loginInfoVO) {
        if (loginInfoVO == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        String mobile = loginInfoVO.getMobile();
        String formPassword = loginInfoVO.getPassword();

        User user = userDao.getUserById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 验证密码，二次MD5加密验证
        String salt = user.getSalt();
        String dbPassword = MD5Util.formPasswordToDBPassword(formPassword, salt);
        if (!user.getPassword().equals(dbPassword)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        // 用户输入信息正确后，在Redis中保存Session信息，浏览器保存Cookie
        String token = addCookie(response, user);

        return token;
    }

    /**
     * 在Redis中保存Session信息，浏览器保存Cookie，返回Token
     * @param response
     * @param user
     */
    private String addCookie(HttpServletResponse response, User user) {
        String token = UUIDUtil.uuid();
        String key = UserKey.userKey.getPrefix() + ":" + token;
        int expire = UserKey.userKey.expireSeconds();
        redisTemplate.opsForValue().set(key, user);
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);

        Cookie cookie = new Cookie(COOKIE_TOKEN_NAME, token);
        cookie.setMaxAge(expire);
        cookie.setPath("/");
        response.addCookie(cookie);
        return token;
    }

    /**
     * 批量注册用户功能，用于JMeter的压测
     * @param num 批量数量
     * @return
     */
    public CodeMsg batchRegister(Integer num) {
        if (num < 1) {
            throw new GlobalException(CodeMsg.REGISTER_BATCH);
        }
        for (int i = 0; i < num; i++) {
            User user = new User();
            user.setId(12320890318L + i);
            user.setNickname("user" + i);
            user.setPassword("password" + i);
            user.setSalt("salt" + i);
            user.setRegisterDate(new Date());
            user.setLastLoginDate(new Date());
            user.setLoginCount(0);
            userDao.insert(user);
        }
        return CodeMsg.SUCCESS;
    }

    /**
     * 根据Token查询用户信息
     * @param token
     * @return
     */
    public User getUserByToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        String key = UserKey.userKey.getPrefix() + ":" + token;
        int expire = UserKey.userKey.expireSeconds();
        // 更新用户Session有效期，如果key不存在，并不会在redis新生成key
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        return (User) redisTemplate.opsForValue().get(key);
    }
}
