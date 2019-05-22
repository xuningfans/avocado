package com.avocado.slave.service.health;

import com.avocado.common.utils.IOUtils;
import com.avocado.common.utils.ThreadUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

    @PostConstruct
    public void connect() {
        try {
            clientKeepAlive = new Socket("127.0.0.1", 20006);
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
                double usableSpace = usableSpaceByte / 1024f / 1024 / 1024;
                // send heath data to tracker
                String format = String.format("%.2f", usableSpace);
                // language=JSON
                printStream.println("{\"usableSpace\": " + format + "}");
                Thread.sleep(1000 * 30);
                String echo = bufferedReader.readLine();
                if (TRACE_ENABLED) {
                    log.trace("Received server response: {}", echo);
                }
            } catch (IOException e) {
                log.error("connection exception, reconnecting... , cause {}", e.getMessage());
                // wait 10 sec
                ThreadUtils.sleep(10000);
                this.close();
                this.connect();
            } catch (InterruptedException e) {
                log.warn("sleep interrupted, cause {}", e.getMessage());
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
