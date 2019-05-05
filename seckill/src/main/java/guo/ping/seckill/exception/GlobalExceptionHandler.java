package guo.ping.seckill.exception;

import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.result.ServerResponse;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @description: 全局异常处理器
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/3 11:03 PM
 * @project: seckill
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ServerResponse<CodeMsg> exceptionHandler(HttpServletRequest request, Exception e) {
        // 参数校验异常
        if (e instanceof BindException) {
            BindException bindException = (BindException) e;
            List<ObjectError> errors = bindException.getAllErrors();
            String msg = errors.get(0).getDefaultMessage();
            return ServerResponse.error(CodeMsg.BIND_ERROR.fillMsg(msg));
        }
        // 全局异常
        else if (e instanceof GlobalException) {
            GlobalException globalException = (GlobalException) e;
            return ServerResponse.error(globalException.getCodeMsg());
        } else {
            return ServerResponse.error(CodeMsg.SERVER_ERROR);
        }
    }

}
