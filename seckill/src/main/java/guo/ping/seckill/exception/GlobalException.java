package guo.ping.seckill.exception;

import guo.ping.seckill.result.CodeMsg;

/**
 * @description: 定义封装CodeMsg的全局异常，用于Service中错误向外直接抛出，并由全局异常处理器捕获处理
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/5 10:58 AM
 * @project: seckill
 */
public class GlobalException extends RuntimeException {

    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg) {
        this.codeMsg = codeMsg;
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
