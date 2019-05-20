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

    public TrackerServerClient(String host, Integer port) throws IOException {
        this.host = host;
        this.port = port;
        this.socketClient = new SocketClient(host, port);
    }

    public FileMeta getUploadInfo(String fileName) throws IOException {
        return getFileMeta(null, fileName, null, Constants.GET_SERVER_STR);

    }

    public FileMeta getFileMetaInfo(String fileId, String filePath) throws IOException {
        return getFileMeta(fileId, null, filePath, Constants.GET_FILE_META_STR);
    }

    protected FileMeta getFileMeta(String id, String fileName, String filePath, String operation) throws IOException {
        PrintStream printStream = socketClient.getPrintStream();
        // 1.发送操作信息'get_server'|'get_file_meta'
        printStream.println(operation);
        printStream.flush();
        // 2.获取响应‘ok’
        BufferedReader bufferedReader = socketClient.getBufferedReader();
        String resp = bufferedReader.readLine();
        if (Constants.OK_STR.equals(resp)) {
            // 3.发送文件信息json串
            FileMeta build = FileMeta.builder()
                    .id(id)
                    .name(fileName)
                    .path(filePath)
                    .build();
            printStream.println(JSONObject.toJSONString(build));
            printStream.flush();
            // 4.获取文件存储路径、backup以及upload服务器信息
            String fileMetaStr = bufferedReader.readLine();
            ClientUtils.checkResponse(fileMetaStr);
            return JSONObject.parseObject(fileMetaStr, FileMeta.class);
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
