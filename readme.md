# 电商秒杀系统

![](https://img.shields.io/badge/Java-1.8-blue.svg) ![](https://img.shields.io/badge/Spring&nbsp;Boot-2.1.3&nbsp;Release-blue.svg) ![](https://img.shields.io/badge/Redis-3.2-blue.svg) ![](https://img.shields.io/badge/MySQL-5.7.23-blue.svg) ![](https://img.shields.io/badge/Druid-1.1.10-blue.svg) ![](https://img.shields.io/badge/Thymeleaf-3.0.11-blue.svg)

本项目实现了电商项目的秒杀功能，主要内容包含了用户登录、浏览商品、秒杀抢购、创建订单等功能，着重解决秒杀系统的并发问题。项目利用JMeter工具进行压力测试，着重对比采用缓存、消息队列等手段对于提高系统响应速度并发能力的效果。

项目视频参考地址：[https://www.bilibili.com/video/av50818180/](https://www.bilibili.com/video/av50818180/)

## 一、登录模块

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

#### 「两次MD5密码加密」
1. 客户端登录时避免明文密码在网络中传输，所以进行客户端第一次MD5;
2. MD5的密码传输至服务端时，需要随机生成salt进行二次MD5，保存salt和两次MD5结果至数据库中。

#### 「分布式Session」
1. UUID方式生成Token，Redis保存Token-User的键值信息模拟Session;
2. 将Token写到Cookie中，设置Path为顶级域名之下。

#### 「注册登录功能实现」
1. 封装服务端响应对象 `guo.ping.seckill.result.ServerResponse` 以及状态码消息对象 `guo.ping.seckill.result.CodeMsg`;
2. 利用JSR-303注解校验参数，并自定义手机号校验注解 `guo.ping.seckill.validator.IsMobile` 以及验证器 `guo.ping.seckill.validator.IsMobileValidator`;
3. 实现用户登录，批量注册用户逻辑;
4. 自定义方法参数解析器用于获取请求中包含的Token值，并查询Redis封装成User;
5. 具体代码参考 [<u>Commits on May 3, 2019</u> 和 <u>Commits on May 5, 2019</u>](https://github.com/MrSorrow/seckill/commits/master)



