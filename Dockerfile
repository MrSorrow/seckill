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