一个简单的分布式文件管理服务器
==============
>用于解决自建文件服务器文件管理问题（更简单、更稳定、更易用）  
>仅管理多台服务器，将多台服务器映射为一台，没有进行小文件合并或者大文件分片。采用一主多从模式，主服务器负责分发上传、下载请求，管理文件元数据（仅存储在数据库中）。从服务器负责存储文件。  
>  
>优点：  
>>高稳定性，文件元信息存储数据库，文件存储直接由操作系统管理，主从服务器崩溃不会丢失文件。  
>>架构简单可靠，仅使用socket通信。  
>>API更易用，一行代码即可完成上传下载。

|Author|徐宁|
|---|---
|E-mail|fastpc@qq.com  


                                      架构图如下：
                                      +----------+
                                      | tracker  |  
                                      +----------+
                                       /        \
                                      /          \
                            +----------+         +----------+
                            |  worker  | <=====> |  worker  |  
                            +----------+         +----------+
                            
                            Tracker负责调度，资源分配； 多个Worker互为主备。
---------

#### 使用

- [usage][]: 使用手册

[usage]: https://github.com/xuningfans/avocado/blob/master/usage.md

---------

>开发计划：
>- [ ] 其他语言SDK
>   - [ ] Python
>   - [ ] Go
>   - [ ] C/C++
>- [ ] 修改socket通讯为NIO(netty)
>- [ ] 更多更灵活的调度算法
>- [ ] 支持文件路径定制（当前生成策略根据时间生成/yyyy/MM/dd/yyyyMMddHHmmss_SSS.xxx）
>- [x] ~~支持文件文件副本数定制~~
>- [ ] 增加客户端指定上传、下载服务器及路径功能
>- [ ] 增加web上传下载接口
>- [ ] 上传下载支持鉴权
>- [ ] 增加小文件合并
>- [ ] 增加大文件分片
>- [ ] 增加主节点HA支持
