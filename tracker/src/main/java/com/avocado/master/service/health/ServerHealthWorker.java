package com.avocado.master.service.health;

import com.avocado.master.cache.SlavesCache;
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

import static com.avocado.common.constants.Constants.BYE_STR;
import static com.avocado.common.constants.Constants.OK_STR;

/**
 * ServerHealthExecuter class
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

        while (flag) {

            try (Socket client = server.accept();
                 PrintStream out = new PrintStream(client.getOutputStream());
                 BufferedReader buf = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                //获取Socket的输出流，用来向客户端发送数据
                //获取Socket的输入流，用来接收从客户端发送过来的数据
                while (flag) {
                    //接收从客户端发送过来的数据
                    String str = buf.readLine();
                    if (BYE_STR.equals(str)) {
                        flag = false;
                    } else {
                        out.println(OK_STR);
                        slavesCache.put(client.getInetAddress().getHostAddress(), str);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
