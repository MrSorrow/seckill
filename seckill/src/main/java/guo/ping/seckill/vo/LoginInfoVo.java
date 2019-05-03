package guo.ping.seckill.vo;

import guo.ping.seckill.validator.IsMobile;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @description: 用于封装用户登录信息的VO
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/3 7:03 PM
 * @project: seckill
 */
public class LoginInfoVo {

    @NotNull
    @IsMobile
    private String mobile;
    @NotNull
    @Length(min = 32, max = 32)
    private String password;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginInfoVo{" +
                "mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
