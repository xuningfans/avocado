package com.avocado.slave.service.health;

import com.alibaba.fastjson.JSON;
import com.avocado.common.dto.HealthMessage;
import com.avocado.common.utils.IOUtils;
import com.avocado.common.utils.ThreadUtils;
import com.avocado.slave.service.upload.StorageWorker;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.net.Socket;

/**
 * SlaveHealthExecuter class
 *
 * @author xuning
 * @date 2019-05-06 15:33
 */
@Slf4j
@Component
public class SlaveHealthWorker implements Closeable {

    private static final boolean TRACE_ENABLED = log.isTraceEnabled();

    private Socket clientKeepAlive;

    private PrintStream printStream;

    private BufferedReader bufferedReader;

    @Setter
    private volatile boolean flag = true;

    @Value("${tracker.health.host}")
    private String trackerHealthHost;

    @Value("${tracker.health.port}")
    private Integer trackerHealthPort;

    @Value("${worker.port}")
    private Integer workerPort;

    @Resource
    private StorageWorker storageWorker;

    @PostConstruct
    public void connect() {
        try {
            clientKeepAlive = new Socket(trackerHealthHost, trackerHealthPort);
            printStream = new PrintStream(clientKeepAlive.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(clientKeepAlive.getInputStream()));
        } catch (IOException e) {
            log.error("connection exception, cause {}", e.getMessage());
        }
    }

    @Async
    public void execute() {
        File file = new File(".");
        while (flag) {
            if (clientKeepAlive == null || !clientKeepAlive.isConnected()) {
                log.warn("Waiting for connect to server");
                ThreadUtils.sleep(1000 * 10);
                continue;
            }
            try {
                long usableSpaceByte = file.getUsableSpace();
//                double usableSpace = usableSpaceByte / 1024f / 1024 / 1024;
                // send heath data to tracker
//                String format = String.format("%.2f", usableSpace);

                HealthMessage healthMessage = HealthMessage.builder()
                        .usableSpace(usableSpaceByte)
                        .workerPort(workerPort)
                        .connectionCount(storageWorker.getConnectionCount())
                        .build();
                printStream.println(JSON.toJSONString(healthMessage));
                String resp = bufferedReader.readLine();
                if (TRACE_ENABLED) {
                    log.trace("Received server response: {}", resp);
                }
                Thread.sleep(1000 * 30);
            } catch (IOException e) {
                log.error("Connection exception, reconnecting... , cause by: {}", e.getMessage());
                // wait 10 sec
                ThreadUtils.sleep(1000 * 10);
                this.close();
                this.connect();
            } catch (InterruptedException e) {
                log.warn("Sleep interrupted, cause {}", e.getMessage());
            } catch (Exception e) {
                log.error("Unknown error,", e);
            }
        }
    }

    @Override
    public void close() {
        IOUtils.close(bufferedReader);
        IOUtils.close(printStream);
        IOUtils.close(clientKeepAlive);
    }
}
