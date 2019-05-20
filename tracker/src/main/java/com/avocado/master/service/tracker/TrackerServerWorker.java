package com.avocado.master.service.tracker;

import com.alibaba.fastjson.JSONObject;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.utils.FileUtils;
import com.avocado.common.utils.ThreadUtils;
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
import java.util.Map;
import java.util.UUID;

import static com.avocado.common.constants.Constants.*;

/**
 * TrackerServerWorker class
 * 负责指定存储路径，分发服务器
 *
 * @author xuning
 * @date 2019-05-07 14:10
 */
@Slf4j
@Component
public class TrackerServerWorker {

    public static final boolean DEBUG_ENABLED = log.isDebugEnabled();
    @Setter
    private volatile boolean flag = true;
    @Resource
    private SlavesCache slavesCache;

    /**
     * 分发服务器
     */
    @Async
    public void execute(ServerSocket server) {

        while (flag) {

            Map<String, String> all = slavesCache.getAll();
            if (all.size() == 0) {
                log.warn("No StorageWorker found");
                ThreadUtils.sleep(1000 * 5);
                continue;
            }

            log.info("TrackerServer listening at host:{}, port:{}", server.getInetAddress().getHostAddress(), server.getLocalPort());

            try (Socket client = server.accept();
                 PrintStream printStream = new PrintStream(client.getOutputStream());
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                // 1.获取握手信息，'get_server|get_file_meta'
                String operation = bufferedReader.readLine();
                String hostAddress = client.getInetAddress().getHostAddress();
                if (DEBUG_ENABLED) {
                    log.debug("Accepting operation: {} from {} .", operation, hostAddress);
                }

                FileMeta result = getFileMeta(printStream, bufferedReader, operation);

                if (result == null) {
                    log.warn("Error handle operation:{},client:{}", operation, hostAddress);
                    printStream.println("error: can not get/generate file meta");
                    return;
                }

                String json = JSONObject.toJSONString(result);
                if (DEBUG_ENABLED) {
                    log.debug("Replying file meta:{}", json);
                }
                printStream.println(json);
            } catch (Exception e) {
                log.error("Handle client error", e);
            }
        }
    }

    protected FileMeta getFileMeta(PrintStream printStream, BufferedReader bufferedReader, String operation) {
        try {
            if (GET_SERVER_STR.equals(operation)) {
                return replyUploadFileMeta(printStream, bufferedReader);
            }

            if (GET_FILE_META_STR.equals(operation)) {
                return replyDownloadFileMeta(printStream, bufferedReader);
            }
        } catch (Exception e) {
            log.error("generate file meta error", e);
        }
        return null;
    }

    protected FileMeta replyDownloadFileMeta(PrintStream printStream, BufferedReader bufferedReader) throws IOException {
        // 2.校验握手正常，返回，'ok'
        printStream.println(OK_STR);

        // 3.读取文件id或者文件路径
        String fileMetaStr = bufferedReader.readLine();
        if (DEBUG_ENABLED) {
            log.debug("Received file meta:{}", fileMetaStr);
        }

        // 4.返回服务器，存储路径
        return JSONObject.parseObject("{\"backupServerHost\":\"127.0.0.1\",\"backupServerPort\":9991,\"id\":\"ab6dda6f4987414389182424717351e0\",\"name\":\"SKMBT_C36017032800060_0001 (1).jpg\",\"path\":\"/2019/05/17/20190517151410_239.jpg\",\"size\":698473,\"storageServerHost\":\"127.0.0.1\",\"storageServerPort\":9991}", FileMeta.class);
    }

    protected FileMeta replyUploadFileMeta(PrintStream printStream, BufferedReader bufferedReader) throws IOException {
        Map<String, String> all = slavesCache.getAll();
        // 2.校验握手正常，返回，'ok'
        printStream.println(OK_STR);

        // 3.读取文件名
        String fileMetaStr = bufferedReader.readLine();
        if (DEBUG_ENABLED) {
            log.debug("Received file meta:{}", fileMetaStr);
        }
        FileMeta fileMeta = JSONObject.parseObject(fileMetaStr, FileMeta.class);

        // 4.返回服务器，存储路径
        String[] servers = all.keySet().toArray(new String[0]);
        String name = fileMeta.getName();
        return FileMeta.builder()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .backupServerHost(servers[0])
                .backupServerPort(9991)
                .storageServerHost(servers[0])
                .storageServerPort(9991)
                .name(name)
                .path(FileUtils.getSaveFullPath("/", name))
                .build();

    }
}
