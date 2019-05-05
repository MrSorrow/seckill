package guo.ping.seckill.controller.goods;

import guo.ping.seckill.domain.User;
import guo.ping.seckill.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @description: 商品Controller
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/5 3:45 PM
 * @project: seckill
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private UserService userService;

    /**
     * 展示商品列表
     * @param model 将信息加入model用于页面展示
     * @param cookieToken 从cookie中取出数据
     * @param paramToken 从请求参数中取出数据
     * @return
     */
    @RequestMapping("/list")
    public String goodsList(Model model,
                            @CookieValue(value = UserService.COOKIE_TOKEN_NAME, required = false) String cookieToken,
                            @RequestParam(value = UserService.COOKIE_TOKEN_NAME, required = false) String paramToken) {
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return "login";
        }
        // 优先从参数中获取用户token
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        // 根据Token查询User
        User user = userService.getUserByToken(token);
        model.addAttribute("user", user);
        return "goods_list";
    }
}
