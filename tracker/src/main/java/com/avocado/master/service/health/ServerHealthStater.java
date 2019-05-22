package com.avocado.master.service.health;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${tracker.health.port}")
    private Integer trackerHealthPort;

    @PostConstruct
    public void init() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> serverHealthWorker.setFlag(false)));
        final ServerSocket server = new ServerSocket(trackerHealthPort);
        serverHealthWorker.execute(server);
    }

}
