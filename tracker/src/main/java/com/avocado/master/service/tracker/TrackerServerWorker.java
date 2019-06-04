package com.avocado.master.service.tracker;

import com.alibaba.fastjson.JSONObject;
import com.avocado.common.dto.HealthMessage;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.utils.FileUtils;
import com.avocado.common.utils.ThreadUtils;
import com.avocado.master.cache.SlavesCache;
import com.avocado.master.domain.file.FileMetaEntity;
import com.avocado.master.service.file.FileMetaService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

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

    @Value("${file.replication.count}")
    private Integer fileReplicationCount;

    /**
     * 分发服务器
     */
    @Async
    public void execute(ServerSocket server) {
        log.info("TrackerServer listening at host:{}, port:{}", server.getInetAddress().getHostAddress(), server.getLocalPort());

        while (flag) {
            if (slavesCache.getSize() == 0) {
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

        return fileMeta;
    }

    protected FileMeta replyUploadFileMeta(PrintStream printStream, BufferedReader bufferedReader) throws IOException, NoSuchFieldException, IllegalAccessException {
        // 2.校验握手正常，返回，'ok'
        printStream.println(OK_STR);

        // 3.读取文件名
        String fileMetaStr = bufferedReader.readLine();
        if (DEBUG_ENABLED) {
            log.debug("Received file meta:{}", fileMetaStr);
        }
        FileMeta receivedFileMeta = JSONObject.parseObject(fileMetaStr, FileMeta.class);

        // 4.返回服务器，存储路径，文件id等信息
        String name = receivedFileMeta.getName();
        FileMeta result = FileMeta.builder()
                .id(UUID.randomUUID().toString().replaceAll("-", ""))
                .name(name)
                .size(receivedFileMeta.getSize())
                .path(FileUtils.getSaveFullPath("/", name))
                .build();

        getServers(receivedFileMeta, result);


        FileMetaEntity fileMetaEntity = new FileMetaEntity();
        BeanUtils.copyProperties(result, fileMetaEntity);
        fileMetaService.save(fileMetaEntity);
        return result;

    }

    protected void getServers(FileMeta receivedFileMeta, FileMeta result) throws NoSuchFieldException, IllegalAccessException {
        Set<Integer> storageSet = new HashSet<>();
        Map<String, HealthMessage> slaves = slavesCache.getAll();
        List<Map.Entry<String, HealthMessage>> entries = new ArrayList<>(slaves.entrySet());
        int length = entries.size();
        // 副本数量不能大于存储节点数量
        Integer fileReplicationCount = this.fileReplicationCount;
        if (receivedFileMeta.getFileReplicationCount() != null && receivedFileMeta.getFileReplicationCount() > 0) {
            fileReplicationCount = receivedFileMeta.getFileReplicationCount();
        }
        fileReplicationCount = fileReplicationCount > length ? length : fileReplicationCount;
        Random random = new Random();
        while (storageSet.size() == fileReplicationCount) {
            int anInt = random.nextInt(length);
            storageSet.add(anInt);
        }

        int methodIndex = 1;
        for (Integer integer : storageSet) {
            Field storageServerHostField = FileMeta.class.getDeclaredField("storageServerHost" + methodIndex++);
            Field storageServerPortField = FileMeta.class.getDeclaredField("storageServerPort" + methodIndex++);
            storageServerHostField.setAccessible(true);
            storageServerPortField.setAccessible(true);
            storageServerHostField.set(result, entries.get(integer).getKey());
            storageServerHostField.setInt(result, entries.get(integer).getValue().getWorkerPort());
        }
    }
}
