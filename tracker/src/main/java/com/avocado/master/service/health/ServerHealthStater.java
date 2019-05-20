package com.avocado.master.service.health;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * ServerPool class
 * 处理客户端心跳信息
 *
 * @author xuning
 * @date 2019-05-07 09:25
 */
@Data
@Component
public class ServerHealthStater {

    @Resource
    private ServerHealthWorker serverHealthWorker;



    @PostConstruct
    public void init() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> serverHealthWorker.setFlag(false)));
        //服务端在20006端口监听客户端请求的TCP连接
        final ServerSocket server = new ServerSocket(20006);
        serverHealthWorker.execute(server);
    }

}
