package com.avocado.slave.service.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * UploadStarter class
 *
 * @author xuning
 * @date 2019-05-06 15:33
 */
@Slf4j
@Component
public class StorageStater {

    @Resource
    private StorageWorker storageWorker;

    @PostConstruct
    public void init() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> storageWorker.setFlag(false)));
        final ServerSocket server = new ServerSocket(9991);
        storageWorker.executeServer(server);
    }

}
