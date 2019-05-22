package com.avocado.client;

import com.alibaba.fastjson.JSON;
import com.avocado.common.constants.Constants;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.utils.ClientUtils;
import com.avocado.common.utils.IOUtils;
import lombok.Data;

import java.io.*;

/**
 * StorageServerClient class
 *
 * @author xuning
 * @date 2019-05-15 13:24
 */
@Data
public class StorageServerClient implements Closeable {

    protected SocketClient socketClient;

    protected String host;

    protected Integer port;

    public StorageServerClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socketClient = new SocketClient(host, port);
        socketClient.open();
    }

    public String upload(FileMeta fileMeta, InputStream inputStream) throws IOException {
        return upload(fileMeta, inputStream, Constants.UPLOAD_STR);
    }

    public String backup(FileMeta fileMeta, InputStream inputStream) throws IOException {
        return upload(fileMeta, inputStream, Constants.BACKUP_STR);
    }

    public String upload(FileMeta fileMeta, InputStream inputStream, String operation) throws IOException {

        // 1.发送操作握手信息，backup|upload
        handshake(operation);
        // 2.读取返回标记 'ok'
        BufferedReader bufferedReader = socketClient.getBufferedReader();
        String ok = bufferedReader.readLine();
        ClientUtils.checkResponse(ok);
        if (Constants.OK_STR.equals(ok)) {
            // 3.发送文件元信息
            handshake(JSON.toJSONString(fileMeta));
            // 4.读取返回标记 'ok'
            ok = bufferedReader.readLine();
            ClientUtils.checkResponse(ok);
            if (Constants.OK_STR.equals(ok)) {
                // 5.开始上传
                copyIn(inputStream);
                // 6.读取返回文件信息
                ok = bufferedReader.readLine();
                ClientUtils.checkResponse(ok);
            }
        }
        return ok;
    }

    protected void copyIn(InputStream inputStream) throws IOException {
        IOUtils.copy(inputStream, socketClient.getSocketOutputStream());
    }

    protected void copyOut(OutputStream outputStream) throws IOException {
        IOUtils.copy(socketClient.getSocketInputStream(), outputStream);
    }

    protected void handshake(String message) {
        // 发送消息
        PrintStream printStream = socketClient.getPrintStream();
        printStream.println(message);
        printStream.flush();
    }


    @Override
    public void close() throws IOException {
        if (socketClient != null) {
            socketClient.close();
        }
    }

    public void download(FileMeta fileMeta, OutputStream outputStream) throws IOException {
        // 1.发送操作握手信息，backup|upload
        handshake(Constants.DOWNLOAD_STR);
        // 2.读取返回标记 'ok'
        BufferedReader bufferedReader = socketClient.getBufferedReader();
        String ok = bufferedReader.readLine();
        ClientUtils.checkResponse(ok);
        if (Constants.OK_STR.equals(ok)) {
            // 3.发送文件元信息
            handshake(JSON.toJSONString(fileMeta));
            // 4.读取返回标记 'ok'
            ok = bufferedReader.readLine();
            ClientUtils.checkResponse(ok);
            if (Constants.OK_STR.equals(ok)) {
                // 5.开始上传
                copyOut(outputStream);
            }
        }
    }
}
