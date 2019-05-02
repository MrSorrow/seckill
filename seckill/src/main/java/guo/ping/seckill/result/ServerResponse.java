package guo.ping.seckill.result;

/**
 * @description: 服务端数据响应封装接口
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/2 11:49 PM
 * @project: seckill
 */
public class ServerResponse<T> {

    private int code;
    private String msg;
    private T data;

    private ServerResponse(CodeMsg codeMsg, T data) {
        if (codeMsg == null) {
            return;
        }
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();
        this.data = data;
    }

    private ServerResponse(CodeMsg codeMsg) {
        this(codeMsg, null);
    }

    /**
     * 成功的状态码和消息是一致的
     * @param data
     * @param <T>
     * @return
     */
    public static <T> ServerResponse<T> success(T data) {
        return new ServerResponse<>(CodeMsg.SUCCESS, data);
    }

    /**
     * 错误的状态码和消息不是一致的，需要用户动态传入
     * @param codeMsg
     * @param <T>
     * @return
     */
    public static <T> ServerResponse<T> error(CodeMsg codeMsg) {
        return new ServerResponse<>(codeMsg);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
