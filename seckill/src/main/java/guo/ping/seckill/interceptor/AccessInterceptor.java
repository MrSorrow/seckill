package guo.ping.seckill.interceptor;

import com.alibaba.fastjson.JSON;
import guo.ping.seckill.annotation.AccessLimit;
import guo.ping.seckill.context.UserContext;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.redis.AccessLimitKey;
import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.result.ServerResponse;
import guo.ping.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * @description: 自定义限流拦截器
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/10 2:16 PM
 * @project: seckill
 */
@Component
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 方法执行前，对方法进行拦截查看是否包含@AccessLimit注解，并进行相应的设置
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            // 获取用户并保存到ThreadLocal中
            User user = getUser(request);
            UserContext.setUser(user);

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取方法上@AccessLimit的注解
            AccessLimit accessLimit = handlerMethod.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            // 存在@AccessLimit注解，则需要对注解配置进行获取并进行处理
            int maxValue = accessLimit.maxValue();
            int seconds = accessLimit.seconds();
            boolean needLogin = accessLimit.needLogin();

            String key = request.getRequestURI();

            if (needLogin && UserContext.getUser() == null) {
                render(response, CodeMsg.SESSION_ERROR);
                return false;
            }

            // 不需要登录则根据是否包含user来拼接key
            key += "_" + (UserContext.getUser() != null ? user.getId() : "");

            // 利用Redis实现限流
            AccessLimitKey accessLimitKey = AccessLimitKey.getAccessKeyWithExpire(seconds);
            String accessKey = accessLimitKey.getPrefix() + ":" + key;
            Integer count = (Integer) redisTemplate.opsForValue().get(accessKey);
            if (count == null) {
                redisTemplate.opsForValue().set(accessKey, 1);
                redisTemplate.expire(accessKey, accessLimitKey.expireSeconds(), TimeUnit.SECONDS);
            }
            // 仍然在限制内
            else if (count <= maxValue) {
                redisTemplate.opsForValue().increment(accessKey);
            }
            // 超出最大访问次数限制
            else {
                render(response, CodeMsg.ACCESS_BEYOND_LIMIT);
                return false;
            }
        }
        return true;
    }

    /**
     * 返回页面，被拦截器拦截需要登录却没有登录
     * @param response
     * @param codeMsg
     */
    private void render(HttpServletResponse response, CodeMsg codeMsg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = response.getOutputStream();
        String out = JSON.toJSONString(ServerResponse.error(codeMsg));
        outputStream.write(out.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();
    }

    public User getUser(HttpServletRequest request) {
        String paramToken = request.getParameter(UserService.COOKIE_TOKEN_NAME);
        String cookieToken = getCookieValue(request, UserService.COOKIE_TOKEN_NAME);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        // 优先从参数中获取用户token
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        // 根据Token查询User
        return userService.getUserByToken(token);
    }


    /**
     * 根据Cookie的名称获取对应的值
     * @param request
     * @param name
     * @return
     */
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        // 压测下发现问题，cookies可能为空
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
