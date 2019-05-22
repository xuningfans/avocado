package com.avocado.master.service.tracker;

import com.alibaba.fastjson.JSONObject;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.utils.FileUtils;
import com.avocado.common.utils.ThreadUtils;
import com.avocado.master.cache.SlavesCache;
import com.avocado.master.domain.file.FileMetaEntity;
import com.avocado.master.service.file.FileMetaService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
import java.util.Random;
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

    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();

    @Setter
    private volatile boolean flag = true;

    @Resource
    private SlavesCache slavesCache;

    @Resource
    private FileMetaService fileMetaService;

    /**
     * 分发服务器
     */
    @Async
    public void execute(ServerSocket server) {
        log.info("TrackerServer listening at host:{}, port:{}", server.getInetAddress().getHostAddress(), server.getLocalPort());

        while (flag) {

            Map<String, String> all = slavesCache.getAll();
            if (all.size() == 0) {
                log.warn("No StorageWorker found");
                ThreadUtils.sleep(1000 * 10);
                continue;
            }


            try (Socket client = server.accept();
                 PrintStream printStream = new PrintStream(client.getOutputStream());
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

                // 1.获取握手信息，'get_server|get_file_meta'
                String operation = bufferedReader.readLine();
                String hostAddress = client.getInetAddress().getHostAddress();
                if (DEBUG_ENABLED) {
                    log.debug("Accepting operation: {} from {} .", operation, hostAddress);
                }

                if (UPDATE_CHECKSUM_STR.equals(operation)) {
                    updateChecksum(printStream, bufferedReader);
                    continue;
                }

                FileMeta result = getFileMeta(printStream, bufferedReader, operation);

                if (result == null) {
                    log.warn("Error handle operation:{},client:{}", operation, hostAddress);
                    printStream.println("error: can not get/generate file meta");
                    continue;
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

    protected void updateChecksum(PrintStream printStream, BufferedReader bufferedReader) throws IOException {
        printStream.println(OK_STR);
        String fileMetaStr = bufferedReader.readLine();
        if (DEBUG_ENABLED) {
            log.debug("Received file meta:{}", fileMetaStr);
        }
        FileMeta fileMetaReceived = JSONObject.parseObject(fileMetaStr, FileMeta.class);
        fileMetaService.updateChecksumById(fileMetaReceived.getChecksum(), fileMetaReceived.getId());
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
            log.error("Generate file meta error", e);
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
        FileMeta fileMetaReceived = JSONObject.parseObject(fileMetaStr, FileMeta.class);
        String id = fileMetaReceived.getId();

        FileMeta fileMeta = null;
        // 4.返回服务器，存储路径
        if (id != null) {
            // 通过id查找
            fileMeta = fileMetaService.findById(id);
        }

        String path = fileMetaReceived.getPath();
        if (path != null) {
            // 通过路径查找
            fileMeta = fileMetaService.findByPath(path);
        }


        if (DEBUG_ENABLED) {
            if (fileMeta != null) {
                log.debug("Find file ({}) from db.", fileMeta.toString());
            }
        }

//        return JSONObject.parseObject("{\"backupServerHost\":\"127.0.0.1\",\"backupServerPort\":9991,\"id\":\"ab6dda6f4987414389182424717351e0\",\"name\":\"SKMBT_C36017032800060_0001 (1).jpg\",\"path\":\"/2019/05/17/20190517151410_239.jpg\",\"size\":698473,\"storageServerHost\":\"127.0.0.1\",\"storageServerPort\":9991}", FileMeta.class);
        return fileMeta;
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
        FileMeta receivedFileMeta = JSONObject.parseObject(fileMetaStr, FileMeta.class);

        // 4.返回服务器，存储路径，文件id等信息
        String[] servers = all.keySet().toArray(new String[0]);
        int length = servers.length;
        Random random = new Random();
        int storageServerIndex = random.nextInt(length);
        int backupServerIndex;
        if (length == 1) {
            backupServerIndex = storageServerIndex;
        } else {
            while (true) {
                int anInt = random.nextInt(length);
                if (anInt != storageServerIndex) {
                    backupServerIndex = anInt;
                    break;
                }
            }
        }
        String name = receivedFileMeta.getName();
        FileMeta result = FileMeta.builder()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                // TODO 调度算法
                .backupServerHost(servers[backupServerIndex])
                .backupServerPort(9991)
                .storageServerHost(servers[storageServerIndex])
                .storageServerPort(9991)
                .name(name)
                .size(receivedFileMeta.getSize())
                .path(FileUtils.getSaveFullPath("/", name))
                .build();
        FileMetaEntity fileMetaEntity = new FileMetaEntity();
        BeanUtils.copyProperties(result, fileMetaEntity);
        fileMetaService.save(fileMetaEntity);
        return result;

    }
}
