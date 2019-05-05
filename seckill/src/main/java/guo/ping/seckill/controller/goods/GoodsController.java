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
     * @param user 通过UserArgumentResolver解析参数token获取user对象
     * @return
     */
    @RequestMapping("/list")
    public String goodsList(Model model, User user) {
        model.addAttribute("user", user);
        return "goods_list";
    }
}
