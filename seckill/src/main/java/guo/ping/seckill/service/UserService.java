package guo.ping.seckill.service;

import guo.ping.seckill.dao.UserDao;
import guo.ping.seckill.domain.User;
import guo.ping.seckill.result.CodeMsg;
import guo.ping.seckill.utils.MD5Util;
import guo.ping.seckill.vo.LoginInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public CodeMsg login(LoginInfoVo loginInfoVo) {
        String mobile = loginInfoVo.getMobile();
        String formPassword = loginInfoVo.getPassword();

        User user = userDao.getUserById(Long.parseLong(mobile));
        if (user == null) {
            return CodeMsg.MOBILE_NOT_EXIST;
        }

        // 验证密码，二次MD5加密验证
        String salt = user.getSalt();
        String dbPassword = MD5Util.formPasswordToDBPassword(formPassword, salt);
        if (!user.getPassword().equals(dbPassword)) {
            return CodeMsg.PASSWORD_ERROR;
        }
        return CodeMsg.SUCCESS;
    }
}
