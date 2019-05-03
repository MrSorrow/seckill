package guo.ping.seckill.dao;

import guo.ping.seckill.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @description: 用户数据库访问DAO
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/3 7:52 PM
 * @project: seckill
 */
@Mapper
public interface UserDao {

    /**
     * 根据用户id/手机号查询用户信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM miaosha_user WHERE id = #{id}")
    public User getUserById(@Param("id") Long id);


}
