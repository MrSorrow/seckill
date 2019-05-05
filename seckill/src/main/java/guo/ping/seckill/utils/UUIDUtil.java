package guo.ping.seckill.utils;

import java.util.UUID;

/**
 * @description: 生成UUID的工具类
 * @author: guoping wang
 * @email: Kingdompin@163.com
 * @date: 2019/5/5 11:17 AM
 * @project: seckill
 */
public class UUIDUtil {

    /**
     * 产生UUID字符串，将“-”去除
     * @return
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
