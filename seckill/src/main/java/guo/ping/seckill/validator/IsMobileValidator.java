package guo.ping.seckill.validator;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @description: 自定义验证手机格式注解器
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/3 10:40 PM
 * @project: seckill
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {
    // <IsMobile, String> 注解类型和验证目标内容类型

    private boolean isRequired;

    private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        isRequired = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (isRequired) {
            return isMobile(s);
        } else {
            return StringUtils.isEmpty(s) ? true : isMobile(s);
        }
    }

    public boolean isMobile(String src) {
        if(StringUtils.isEmpty(src)) {
            return false;
        }
        Matcher m = mobile_pattern.matcher(src);
        return m.matches();
    }
}
