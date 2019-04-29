# 电商秒杀系统


## 登录功能

#### 「建立用户表」

```sql
CREATE TABLE miaosha_user (
    id BIGINT(20) NOT NULL PRIMARY KEY COMMENT "用户ID，手机号码",
    nickname VARCHAR(255) NOT NULL COMMENT "昵称",
    password VARCHAR(32) DEFAULT NULL COMMENT "MD5(MD5(pass明文, 固定SALT), 随机SALT)",
    salt VARCHAR(10) DEFAULT NULL,
    head VARCHAR(128) DEFAULT NULL COMMENT "头像，云存储ID",
    register_date datetime DEFAULT NULL COMMENT "注册时间",
    last_login_date datetime DEFAULT NULL COMMENT "上一次登录时间",
    login_count int(11) DEFAULT '0' COMMENT "登录次数"
) ENGINE INNODB DEFAULT CHARSET=utf8mb4;
```

#### 「两次MD5」
1. 客户端登录时避免明文密码在网络中传输，所以进行客户端第一次MD5;
2. MD5的密码传输至服务端时，需要随机生成salt进行二次MD5，保存salt和两次MD5结果至数据库中。

