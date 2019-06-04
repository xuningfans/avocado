package com.avocado.master.service.health;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avocado.common.utils.IOUtils;
import com.avocado.master.cache.SlavesCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

import static com.avocado.common.constants.Constants.BYE_STR;
import static com.avocado.common.constants.Constants.OK_STR;

/**
 * ServerHealthWorker
 * class
 *
 * @author xuning
 * @date 2019-05-07 09:48
 */
@Slf4j
@Component
public class ServerHealthWorker {

    @Setter
    private volatile boolean flag = true;

    @Resource
    private SlavesCache slavesCache;

    /**
     * 处理心跳细节的方法，这里主要是方便线程池服务器的调用
     */
    @Async
    public void execute(ServerSocket server) throws IOException {
        log.info("HealthServer listening at host:{}, port:{} ", server.getInetAddress().getHostAddress(), server.getLocalPort());


        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().build();
        ExecutorService pool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        while (flag) {
            Socket client = server.accept();
            pool.execute(() -> handleClient(client));
        }
    }

    private void handleClient(Socket client) {
        try (PrintStream out = new PrintStream(client.getOutputStream());
             BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            while (flag) {
                //接收从客户端发送过来的数据
                String str = buf.readLine();

                if (BYE_STR.equals(str)) {
                    flag = false;
                } else {
                    out.println(OK_STR);
                    final JSONObject jsonObject = JSON.parseObject(str);
                    slavesCache.put(client.getInetAddress().getHostAddress() + ":" + jsonObject.get("workerPort"), str);
                }
            }
        } catch (IOException e) {
            log.error("Handle health error", e);
        } finally {
            IOUtils.close(client);
        }
    }

}
