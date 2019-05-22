使用、构建手册
==============

下载项目zip或者clone项目，进入根目录执行
```bash
已安装maven执行
mvn clean install

未安装maven执行
./mvnw clean install
```


执行完成后可进入avocado/assembly/target目录下查看，tracker.zip主节点压缩包worker.zip存储节点压缩包。

    tracker\worker-|
                   |---bin  （启动脚本路径）
                   |---conf （配置文件路径）
                   |---lib  （jar文件路径）

----------
## 启动
主节点或从节点均可使用以下脚本启动
```bash
bin/startup.sh
```

## 配置文件详解
### Tracker
配置数据库：
application.properties  
```properties
spring.datasource.driver-class-name=
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
```
---------
监听端口配置：avocado.properties
```properties
#
# 主节点调度端口
#
tracker.file.port=6666
#
# 主节点接收心跳端口
#
tracker.health.port=20006
```
### Storage
监听端口配置：
```properties
#
# 服务器上传下载文件端口
#
worker.port=9991
#
# 主节点心跳配置
#
tracker.health.host=127.0.0.1
tracker.health.port=20006
```

## API使用
### Java
引入依赖（需先构建）
```xml
<dependency>
    <groupId>com.avocado</groupId>
    <artifactId>avocado-client-java</artifactId>
    <version>current-version</version>
</dependency>
```
>上传：  
```Java
AvocadoUtils.builder()
    .host("localhost")
    .port(6666)
    .build().uploadFile(new File("upload.jpg"));
```
>下载： 
```Java
AvocadoUtils.builder()
    .host("localhost")
    .port(6666)
    .build().downloadFile("server returned path or id", new File("path/to/save.jpg"));
``` 
