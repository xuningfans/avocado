package com.avocado.master.service.tracker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * TrackerServerStarter class
 *
 * @author xuning
 * @date 2019-05-07 14:08
 */
@Slf4j
@Component
public class TrackerServerStarter {

    @Resource
    private TrackerServerWorker trackerServerWorker;

    @Value("${tracker.file.port}")
    private Integer trackerFilePort;

    @PostConstruct
    public void init() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> trackerServerWorker.setFlag(false)));
        //服务端在20006端口监听客户端请求的TCP连接
        final ServerSocket server = new ServerSocket(trackerFilePort);
        trackerServerWorker.execute(server);
    }
}
