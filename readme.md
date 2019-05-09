# 电商秒杀系统

![](https://img.shields.io/badge/Java-1.8-blue.svg) ![](https://img.shields.io/badge/Spring&nbsp;Boot-2.1.3&nbsp;Release-blue.svg) ![](https://img.shields.io/badge/Redis-3.2-blue.svg) ![](https://img.shields.io/badge/MySQL-5.7.23-blue.svg) ![](https://img.shields.io/badge/Druid-1.1.10-blue.svg) ![](https://img.shields.io/badge/Thymeleaf-3.0.11-blue.svg)

本项目实现了电商项目的秒杀功能，主要内容包含了用户登录、浏览商品、秒杀抢购、创建订单等功能，着重解决秒杀系统的并发问题。项目利用JMeter工具进行压力测试，着重对比采用缓存、消息队列等手段对于提高系统响应速度并发能力的效果。

项目视频参考地址：[https://www.bilibili.com/video/av50818180/](https://www.bilibili.com/video/av50818180/)

## 一、快速开始
#### 「环境准备」
1. 安装Docker;
2. 安装MySQL的Docker环境 (注意设置容器时区和MySQL时区);
    ```bash
    docker pull mysql:5.7.23
    docker run --name e3-mall-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 -e TZ=Asia/Shanghai -d mysql:5.7.23 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci --default-time_zone='+8:00'
    ```
3. 安装Redis的Docker环境;
    ```bash
    docker pull redis:3.2
    docker run -d -p 6379:6379 --name e3-mall-redis redis:3.2
    ```
4. 安装RabbitMQ的Docker环境;
    ```bash
    docker pull rabbitmq:3.7-management
    docker run -d --name e3-mall-rabbitmq -p 5672:5672 -p 15672:15672 --hostname e3-mall-rabbitmq -e RABBITMQ_DEFAULT_VHOST=mq_vhost  -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin rabbitmq:3.7-management
    ```
#### 「导入项目」
1. 项目采用Maven构建;
2. 通过IDEA直接以Maven方式导入项目即可。

## 二、功能实现
### I. 用户模块

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


### II. 商品模块

#### 「建立商品与订单表」
1. 数据库表包含普通商品表、普通订单表，以及专门用于秒杀商品的秒杀商品表和秒杀订单表;
2. 普通商品表：
    ```sql
    CREATE TABLE goods (
        id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT "商品id",
        goods_name VARCHAR(16) DEFAULT NULL COMMENT "商品名称",
        goods_title VARCHAR(64) DEFAULT NULL COMMENT "商品标题",
        goods_img VARCHAR(64) DEFAULT NULL COMMENT "商品图片",
        goods_detail LONGTEXT COMMENT "商品详情",
        goods_price DECIMAL(10, 2) DEFAULT '0.00' COMMENT "商品单价",
        goods_stock INT(11) DEFAULT 0 COMMENT "-1表示没有限制"	
    ) ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
   ```
3. 秒杀商品表：
    ```sql
    CREATE TABLE miaosha_goods (
        id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT "秒杀商品id",
        goods_id BIGINT(20) DEFAULT NULL COMMENT "商品id",
        miaosha_price DECIMAL(10, 2) DEFAULT '0.00' COMMENT "商品秒杀单价",
        stock_count INT(11) DEFAULT NULL COMMENT "库存数量",
        start_date DATETIME DEFAULT NULL COMMENT "秒杀开始时间",
        end_date DATETIME DEFAULT NULL COMMENT "秒杀结束时间"
    ) ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
    ```
4. 普通订单表：
    ```sql
    CREATE TABLE order_info (
        id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT "订单id",
        user_id BIGINT(20) DEFAULT NULL COMMENT "用户id",
        goods_id BIGINT(20) DEFAULT NULL COMMENT "商品id",
        goods_name VARCHAR(16) DEFAULT NULL COMMENT "反范式冗余的商品名称",
        goods_count INT(11) DEFAULT '0' COMMENT "商品数量",
        goods_price DECIMAL(10, 2) DEFAULT '0.00' COMMENT "商品单价",
        delivery_addr_id BIGINT(20) DEFAULT NULL COMMENT "收货地址id",
        order_channel TINYINT(4) DEFAULT '0' COMMENT "1-pc，2-android，3-ios",
        status TINYINT(4) DEFAULT '0' COMMENT "订单状态 0-新建未支付，1-已支付，2-已发货，3-已收货，4-已退款，5-已完成",
        create_date DATETIME DEFAULT NULL COMMENT "创建订单时间",
        pay_date DATETIME DEFAULT NULL COMMENT "支付时间"
    ) ENGINE=INNODB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;
    ```
5. 秒杀订单表：
    ```sql
    CREATE TABLE miaosha_order (
        id BIGINT(20) NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT "秒杀订单id",
        user_id BIGINT(20) DEFAULT NULL COMMENT "用户id",
        order_id BIGINT(20) DEFAULT NULL COMMENT "订单id",
        goods_id BIGINT(20) DEFAULT NULL COMMENT "商品id"
    ) ENGINE=INNODB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4;
    ```

#### 「展示商品列表及详情」
1. 插入商品数据：
    ```sql
    INSERT INTO goods VALUES (1, "iPhoneX", "Apple iPhone X (A1865) 64GB 深空灰色 移动联通电信4G手机", "/img/iphonex.png", "Apple iPhone X (A1865) 64GB 深空灰色 移动联通电信4G手机【五月特惠】大屏性价比iPhone7Plus4199元，iPhone8低至3499元，iPhoneXR低至4799元！更多特价、优惠券，点此查看！选移动，享大流量，不换号购机！", 5999, 100);
    INSERT INTO goods VALUES (2, "华为 P30", "华为 HUAWEI P30 Pro 超感光徕卡四摄10倍混合变焦麒麟980芯片屏内指纹", "/img/p30.png", "华为 HUAWEI P30 Pro 超感光徕卡四摄10倍混合变焦麒麟980芯片屏内指纹 8GB+256GB极光色全网通版双4G手机！", 5988, 55);   
 
    INSERT INTO miaosha_goods VALUES (1, 1, 0.01, 4, NOW(), NOW()), (2, 2, 0.01, 9, NOW(), NOW());
    ```
2. 通过连接普通商品表和秒杀商品表，查询出秒杀商品的全部信息;
3. 将所有商品展示在 `goods_list` 中，单个商品的全部信息展示在商品详情页 `goods_detail` 中。

#### 「下单秒杀商品」
1. 下单秒杀商品包含减少库存、创建订单两步，需要保证两步操作的原子性;
2. 通过Spring声明式事务，保证减少库存、创建普通订单以及创建秒杀订单三步的原子性;
3. 秒杀倒计时刷新由前端完成，后端仅仅在查看商品详情时返回一次计算的剩余时间即可，保证所有客户端的秒杀时间能够同步，以后端为准;
4. 具体代码参考 [<u>Commits on May 6, 2019</u>](https://github.com/MrSorrow/seckill/commits/master)

## 三、 压测优化

### I. 压力测试
利用JMeter对接口进行压测。压测文件保存在 [`preesure_test`](https://github.com/MrSorrow/seckill/blob/master/pressure_test) 文件夹下。

测试时MySQL、Redis连接池配置参数如下：
```yaml
# druid pool config
druid:
  initial-size: 100
  max-active: 1000
  min-idle: 500
  max-wait: 5000
# redis pool config
redis:
  timeout: 5000
  lettuce:
    pool:
      max-active: 2000
      max-idle: 200
      max-wait: 3
```

由于设置的数据库连接池连接数量较大，需要对Docker-MySQL的最大连接数进行设置：
```sql
set GLOBAL max_connections=2000;
set GLOBAL max_user_connections=1500;
```

未采取任何优化措施前，部分核心接口测试性能如下：

| 压测接口    | 接口地址 |  线程数*循环次数  | 异常 % |   吞吐量   |
| :--------: | :-----: | :-------------: | :----: | :-------: |
| 商品列表接口压测 | [http://localhost:8080/goods/list](http://localhost:8080/goods/list) | 5000*1 | 0.000% | 467.1 / sec |
| 单用户信息查询压测 | [http://localhost:8080/user/info](http://localhost:8080/user/info) | 5000*1 | 0.000% | 996.9 / sec |
| 多用户秒杀接口压测 | [http://localhost:8080/seckill/kill](http://localhost:8080/seckill/kill) | 5000*1 | 0.000% | 289.8 / sec |

### II. 性能优化
系统性能瓶颈在于MySQL的读写性能，所以主要利用各种技术减少对数据库层面的访问。

从缓存实现性能优化角度，用户的请求首先经过CDN优化，然后经过Nginx服务器缓存，再进入应用程序实现的页面级缓存，然后经过对象级缓存，最后可能经过数据库缓存。前面的层层缓存使得用户请求读写数据库的压力减小很多。
页面缓存优化技术包括：
1. 页面缓存+URL缓存+对象缓存
2. 页面静态化，前后端分离
3. 静态资源优化
4. CDN优化

以上是否通用接口的优化方式，针对秒杀场景，较短时间内大量的秒杀请求访问系统，根据其读多写少的业务特性，一旦商品秒杀结束，后续的秒杀请求一律返回秒杀失败即可，无需再访问缓存或数据库。
秒杀接口优化策略包括：
1. Redis预减库存减少数据库的访问
2. 通过内存标记减少Redis访问
3. 请求先进入消息队列进行缓冲削峰，异步下单，增强用户体验

其他提高系统并发能力的策略主要就是服务的水平复制与垂直拆分，构建分布式集群应用，通过多个节点分担压力，负载均衡等。

#### 「模板引擎页面缓存」
1. 页面缓存主要是指 `Controller` 方法返回的不再是包含数据的 `Json`，而是直接返回最终的HTML页面;
2. 通过页面模板引擎将 `Service` 层得到的数据直接手动渲染页面，将整个页面缓存至Redis中; 
3. 缓存的过期时间可以设定较短，保证用户看到的数据不至于过旧。

#### 「模板引擎URL缓存」
1. URL缓存主要是指针对不同的请求URL，缓存对应的HTML页面，基本和页面缓存相似;
2. 通过以不同的URL为缓存的键来存储不同的页面;
3. 同样缓存的淘汰策略也是自动过期。

#### 「对象缓存」
1. 对象缓存主要是指将 `Service` 层得到的数据存入Redis中，这样每次请求先进行缓存查询，如果命中则直接返回;
2. 对象缓存一般没有变化则常驻于缓存，或设置较久的过期时间。数据在更新时，需要对缓存进行处理，往往先更新数据库，再删除缓存。

#### 「页面静态化」
1. 主要针对商品详情页，可以采取页面静态化的方式，直接生成静态HTML页面，保存在本地，额外用高性能HTTP服务器进行访问;
2. 页面静态化方式可以使得前后端分离，一种方式是后端只提供RESTful API接口，前端编写半静态页面，通过Ajax获取数据再进行页面填充;
3. 另一种页面静态化方式可以直接在添加商品时，利用页面模板引擎生成完全静态的商品详情网页，如果数据更改再重新生成新的页面。

#### 「浏览器缓存」
通过浏览器端的缓存，可以降低对服务器的访问次数。Spring Boot对于静态资源的浏览器缓存相关配置如下：
```properties
# SPRING RESOURCES HANDLING (ResourceProperties)
spring.resources.add-mappings=true # Whether to enable default resource handling.
spring.resources.cache.cachecontrol.cache-private= # Indicate that the response message is intended for a single user and must not be stored by a shared cache.
spring.resources.cache.cachecontrol.cache-public= # Indicate that any cache may store the response.
spring.resources.cache.cachecontrol.max-age= # Maximum time the response should be cached, in seconds if no duration suffix is not specified.
spring.resources.cache.cachecontrol.must-revalidate= # Indicate that once it has become stale, a cache must not use the response without re-validating it with the server.
spring.resources.cache.cachecontrol.no-cache= # Indicate that the cached response can be reused only if re-validated with the server.
spring.resources.cache.cachecontrol.no-store= # Indicate to not cache the response in any case.
spring.resources.cache.cachecontrol.no-transform= # Indicate intermediaries (caches and others) that they should not transform the response content.
spring.resources.cache.cachecontrol.proxy-revalidate= # Same meaning as the "must-revalidate" directive, except that it does not apply to private caches.
spring.resources.cache.cachecontrol.s-max-age= # Maximum time the response should be cached by shared caches, in seconds if no duration suffix is not specified.
spring.resources.cache.cachecontrol.stale-if-error= # Maximum time the response may be used when errors are encountered, in seconds if no duration suffix is not specified.
spring.resources.cache.cachecontrol.stale-while-revalidate= # Maximum time the response can be served after it becomes stale, in seconds if no duration suffix is not specified.
spring.resources.cache.period= # Cache period for the resources served by the resource handler. If a duration suffix is not specified, seconds will be used.
spring.resources.chain.cache=true # Whether to enable caching in the Resource chain.
spring.resources.chain.compressed=false # Whether to enable resolution of already compressed resources (gzip, brotli).
spring.resources.chain.enabled= # Whether to enable the Spring Resource Handling chain. By default, disabled unless at least one strategy has been enabled.
spring.resources.chain.html-application-cache=false # Whether to enable HTML5 application cache manifest rewriting.
spring.resources.chain.strategy.content.enabled=false # Whether to enable the content Version Strategy.
spring.resources.chain.strategy.content.paths=/** # Comma-separated list of patterns to apply to the content Version Strategy.
spring.resources.chain.strategy.fixed.enabled=false # Whether to enable the fixed Version Strategy.
spring.resources.chain.strategy.fixed.paths=/** # Comma-separated list of patterns to apply to the fixed Version Strategy.
spring.resources.chain.strategy.fixed.version= # Version string to use for the fixed Version Strategy.
spring.resources.static-locations=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/ # 静态资源路径
```

#### 「静态资源优化」
1. 对JS/CSS文件压缩，减少流量;
2. 多个JS/CSS进行组合为一个文件，减少请求资源时建立的TCP连接数;
3. 利用WebPack可以进行前端资源打包。

#### 「CDN就近访问优化」
1. CDN：内容分发网络(Content Delivery Network)，CDN系统能够实时的根据网络流量和各节点的连接、负载情况选择从最近的节点将数据返回给用户;
2. 购买现有的如阿里云CDN，腾讯CDN，百度CDN等等服务。

#### 「消息队列削峰」
1. 系统初始化，把商品库存数量加载到Redis中;
2. 当用户进行秒杀请求时，Redis预减库存，如果库存不足，可以直接返回商品秒杀完成;
3. 如果还有库存，则用户所有的秒杀请求全部进入消息队列，页面先显示排队中;
4. 秒杀请求出队，减少库存并生成秒杀订单;
5. 客户端则需要不断轮询是否秒杀成功。

### III. 安全优化

#### 「隐藏秒杀接口」

#### 「接口限流防刷」


## 四、打包部署

### I. 项目打包
1. 将Spring Boot项目打包成Jar包，指定打包路径和打包名称;
    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
        <finalName>seckill-springboot</finalName>
        <directory>/Users/guoping/Projects/Intelljidea/seckill/seckill/</directory>
    </build>
    ```
2. 编写Dockerfile构建镜像，参考[官方文档](https://spring.io/guides/topicals/spring-boot-docker);
    ```docker
    # 基础镜像
    FROM java:8
    # 挂载点为/tmp，jar包就会存在这里
    VOLUME /tmp
    # 拷贝打包好的jar包
    COPY seckill-springboot.jar seckill-springboot.jar
    # 暴露端口
    EXPOSE 8080
    # 容器创建后执行jar
    ENTRYPOINT ["java","-jar","/seckill-springboot.jar"]
    ```
3. 构建镜像创建实例并运行服务;
    ```bash
    docker build -t guoping/seckill:1.0 .
    docker run -d -p 80:8080 --name e3-mall-seckill guoping/seckill:1.0
    ```
4. 测试服务。