package guo.ping.seckill.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @description: 自定义验证手机号格式注解
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/3 10:34 PM
 * @project: seckill
 */
@Documented
@Constraint(
        validatedBy = {IsMobileValidator.class}
)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
public @interface IsMobile {

    boolean required() default true;

    String message() default "手机号码格式错误";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
