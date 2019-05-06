package guo.ping.seckill.controller.goods;

import guo.ping.seckill.domain.User;
import guo.ping.seckill.service.GoodsService;
import guo.ping.seckill.service.UserService;
import guo.ping.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
    @Autowired
    private GoodsService goodsService;

    /**
     * 展示商品列表
     * @param model 将信息加入model用于页面展示
     * @param user 通过UserArgumentResolver解析参数token获取user对象
     * @return
     */
    @RequestMapping("/list")
    public String goodsList(Model model, User user) {
        model.addAttribute("user", user);
        List<GoodsVO> goods = goodsService.getGoodsVOs();
        model.addAttribute("goodsList", goods);
        return "goods_list";
    }
}
