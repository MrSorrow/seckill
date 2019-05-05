package guo.ping.seckill.service;

import guo.ping.seckill.dao.UserDao;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.exception.GlobalException;
import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.utils.MD5Util;
import guo.ping.seckill.vo.LoginInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Random;

/**
 * @description: 用户Service
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/3 7:59 PM
 * @project: seckill
 */
@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public User getUserById(Long id) {
        return userDao.getUserById(id);
    }

    /**
     * 登录服务，包含第二次MD5加密的逻辑
     * @param loginInfoVo
     * @return
     */
    public CodeMsg login(LoginInfoVo loginInfoVo) {
        if (loginInfoVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        String mobile = loginInfoVo.getMobile();
        String formPassword = loginInfoVo.getPassword();

        User user = userDao.getUserById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 验证密码，二次MD5加密验证
        String salt = user.getSalt();
        String dbPassword = MD5Util.formPasswordToDBPassword(formPassword, salt);
        if (!user.getPassword().equals(dbPassword)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        return CodeMsg.SUCCESS;
    }

    /**
     * 批量注册用户功能，用于JMeter的压测
     * @param num 批量数量
     * @return
     */
    public CodeMsg batchRegister(Integer num) {
        if (num < 1) {
            throw new GlobalException(CodeMsg.REGISTER_BATCH);
        }
        for (int i = 0; i < num; i++) {
            User user = new User();
            user.setId(12320890318L + i);
            user.setNickname("user" + i);
            user.setPassword("password" + i);
            user.setSalt("salt" + i);
            user.setRegisterDate(new Date());
            user.setLastLoginDate(new Date());
            user.setLoginCount(0);
            userDao.insert(user);
        }
        return CodeMsg.SUCCESS;
    }
}
