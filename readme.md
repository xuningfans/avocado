#一个简单的分布式文件管理服务器
######用于解决自建文件服务器文件管理问题（更简单、更稳定、更易用）


仅管理多台服务器，将多台服务器映射为一台，没有进行小文件合并或者大文件分片。采用一主多从模式，主服务器负责分发上传、下载请求，管理文件元数据（仅存储在数据库中）。从服务器负责存储文件。


                                      架构图如下：
                                      +----------+
                                      | tracker  |  
                                      +----------+
                                       /        \
                                      /          \
                            +----------+         +----------+
                            |  worker  | <=====> |  worker  |  
                            +----------+         +----------+
                            
                            Tracker负责调度，资源分配； Worker互为主备。

使用
>上传：  
AvocadoUtils.builder().host("localhost")
                .port(6666)
                .build().uploadFile(new File("upload.jpg"));

>下载：  
AvocadoUtils.builder()
                .host("localhost")
                .port(6666)
                .build().downloadFile("path/to/download.jpg", new File("path/to/upload.jpg"));

---

>开发计划：
>- 修改socket通讯为NIO(netty)
>- 更多更灵活的调度算法
>- 支持文件路径定制（当前生成策略根据时间生成/yyyyMM/yyyyMMddHHmmss_SSS.xxx）
>- 支持文件文件副本数定制
>- 增加客户端指定上传、下载服务器及路径功能
>- 增加web上传下载接口
>- 上传下载支持鉴权
>- 增加小文件合并、大文件分片
>- 增加主节点HA支持
