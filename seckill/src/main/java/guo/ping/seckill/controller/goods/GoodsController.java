package guo.ping.seckill.controller.goods;

import guo.ping.seckill.annotation.AccessLimit;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.service.GoodsService;
import guo.ping.seckill.service.UserService;
import guo.ping.seckill.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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
    @AccessLimit(seconds = 10, maxValue = 5)
    public String goodsList(Model model, User user) {
        model.addAttribute("user", user);
        List<GoodsVO> goods = goodsService.getGoodsVOs();
        model.addAttribute("goodsList", goods);
        return "goods_list";
    }

    /**
     * 展示商品详情页面
     * @param model
     * @param user
     * @return
     */
    @RequestMapping("/detail/{goodsId}")
    public String goodsDetail(Model model, User user, @PathVariable("goodsId") Long goodsId) {
        model.addAttribute("user", user);

        GoodsVO goodsVO = goodsService.getGoodsDetailById(goodsId);
        model.addAttribute("goods", goodsVO);

        Long startTime = goodsVO.getStartDate().getTime();
        Long endTime = goodsVO.getEndDate().getTime();
        Long now = System.currentTimeMillis();

        // 记录秒杀状态 0-未开始 1-正在进行 2-已结束
        int secKillStatus = 0;
        long remainSeconds = 0;

        // 秒杀未开始
        if (now < startTime) {
            secKillStatus = 0;
            remainSeconds = (startTime - now) / 1000;
        }
        // 秒杀已结束
        else if (now > endTime) {
            secKillStatus = 2;
            remainSeconds = -1;
        }
        // 秒杀正在进行
        else {
            secKillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        return "goods_detail";
    }
}
