package guo.ping.seckill.config;

import guo.ping.seckill.resolver.UserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @description: Spring MVC的相关配置
 * @see: https://songjin.io/2018/07/01/springboot-webmvcconfigurer/
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/5 4:54 PM
 * @project: seckill
 */
@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Autowired
    private UserArgumentResolver userArgumentResolver;

    /**
     * Spring MVC的参数解析器，用于添加自定义参数解析器
     * @param argumentResolvers
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }

}
