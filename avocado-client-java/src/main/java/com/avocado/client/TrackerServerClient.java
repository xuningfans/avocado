package com.avocado.client;

import com.alibaba.fastjson.JSONObject;
import com.avocado.common.constants.Constants;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.utils.ClientUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;

import static com.avocado.common.constants.Constants.UPDATE_CHECKSUM_STR;

/**
 * DfsGetServerClient class
 * <p>
 * trackerServer client
 *
 * @author xuning
 * @date 2019-05-16 09:42
 */
@Slf4j
@Setter
public class TrackerServerClient implements Closeable {

    /**
     * TrackServer info
     */
    protected SocketClient socketClient;

    /**
     * TrackServer host
     */
    protected String host;

    /**
     * TrackServer port
     */
    protected Integer port;

    public TrackerServerClient(String host, Integer port) {
        this.host = host;
        this.port = port;
        this.socketClient = new SocketClient(host, port);
    }

    public void open() throws IOException {
        socketClient.open();
    }

    public FileMeta getUploadInfo(String fileName, Long fileSize) throws IOException {
        return getFileMeta(null, fileName, null, fileSize, Constants.GET_SERVER_STR);

    }

    public FileMeta getFileMetaInfo(String fileId, String filePath) throws IOException {
        return getFileMeta(fileId, null, filePath, null, Constants.GET_FILE_META_STR);
    }

    protected FileMeta getFileMeta(String id, String fileName, String filePath, Long fileSize, String operation) throws IOException {
        PrintStream printStream = socketClient.getPrintStream();
        // 1.发送操作信息'get_server'|'get_file_meta'
        printStream.println(operation);
        printStream.flush();
        // 2.获取响应‘ok’
        BufferedReader bufferedReader = socketClient.getBufferedReader();
        String resp = bufferedReader.readLine();
        if (Constants.OK_STR.equals(resp)) {
            // 3.发送文件信息json串
            FileMeta fileMeta = FileMeta.builder()
                    .id(id)
                    .name(fileName)
                    .path(filePath)
                    .size(fileSize)
                    .build();
            printStream.println(JSONObject.toJSONString(fileMeta));
            printStream.flush();
            // 4.获取文件存储路径、backup以及upload服务器信息
            String fileMetaStr = bufferedReader.readLine();
            ClientUtils.checkResponse(fileMetaStr);
            return JSONObject.parseObject(fileMetaStr, FileMeta.class);
        }
        throw new IOException("server response error:" + resp);
    }

    public void updateChecksum(String fileId, String checksum) throws IOException {
        PrintStream printStream = socketClient.getPrintStream();
        // 1.发送操作信息'get_server'|'get_file_meta'
        printStream.println(UPDATE_CHECKSUM_STR);
        printStream.flush();
        BufferedReader bufferedReader = socketClient.getBufferedReader();
        String resp = bufferedReader.readLine();
        if (Constants.OK_STR.equals(resp)) {
            FileMeta fileMeta = FileMeta.builder()
                    .id(fileId)
                    .checksum(checksum)
                    .build();
            printStream.println(JSONObject.toJSONString(fileMeta));
            printStream.flush();
            return;
        }
        throw new IOException("server response error:" + resp);
    }

    @Override
    public void close() throws IOException {
        if (socketClient != null) {
            socketClient.close();
        }
    }
}
