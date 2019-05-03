package guo.ping.seckill.result;

/**
 * @description: 封装服务端响应的状态码和消息
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/2 11:59 PM
 * @project: seckill
 */
public class CodeMsg {

    /**
     * 通用状态码
     */
    public static final CodeMsg SUCCESS = new CodeMsg(0,"success");
    public static final CodeMsg SERVER_ERROR = new CodeMsg(500100,"服务端异常");

    /**
     * 登录模块5002XX
     */
    public static final CodeMsg SESSION_ERROR = new CodeMsg(500210,"Session不存在或已经失效");
    public static final CodeMsg PASSWORD_EMPTY = new CodeMsg(500211,"密码不能为空");
    public static final CodeMsg MOBILE_EMPTY = new CodeMsg(500212,"手机号不能为空");
    public static final CodeMsg MOBILE_ERROR = new CodeMsg(500213, "手机号格式错误");
    public static final CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500214, "手机号不存在");
    public static final CodeMsg PASSWORD_ERROR = new CodeMsg(500215, "密码错误");

    /**
     * 商品模块5003XX
     */
    public static final CodeMsg PRODUCT_ERROR = new CodeMsg(500301,"服务端异常");

    /**
     * 订单模块5004XX
     */
    public static final CodeMsg ORDER_ERROR = new CodeMsg(500401,"服务端异常");

    /**
     * 秒杀模块5005XX
     */
    public static final CodeMsg SECKILL_ERROR = new CodeMsg(500501,"服务端异常");

    private int code;
    private String msg;

    public CodeMsg(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }
}
