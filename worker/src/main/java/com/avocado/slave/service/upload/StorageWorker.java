package com.avocado.slave.service.upload;

import com.alibaba.fastjson.JSONObject;
import com.avocado.client.SocketClient;
import com.avocado.client.StorageServerBackupClient;
import com.avocado.common.constants.Constants;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.utils.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.avocado.common.constants.Constants.BUFFER_LENGTH;

/**
 * UploadExecuter class
 *
 * @author xuning
 * @date 2019-05-06 15:33
 */
@Slf4j
@Component
public class StorageWorker {

    private static final boolean DEBUG_ENABLED = log.isDebugEnabled();
    @Setter
    private volatile boolean flag = true;
    private String fileSavePath;
    private String fileBakPath;
    private ThreadPoolExecutor pool;

    public int getConnectionCount() {
        if (pool==null) {
            return 0;
        }
        return pool.getActiveCount();
    }

    @Value("${file-save.path}")
    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath.endsWith("/") ? fileSavePath : fileSavePath + "/";
    }

    @Value("${file-bak.path}")
    public void setFileBakPath(String fileBakPath) {
        this.fileBakPath = fileBakPath.endsWith("/") ? fileBakPath : fileBakPath + "/";
    }

    @PostConstruct
    public void initDir() throws Exception {
        // check the directory exists
        checkFilePath(fileSavePath);
        checkFilePath(fileBakPath);
    }

    @Async
    public void executeServer(ServerSocket server) throws IOException {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("upload-pool-%d").build();

        //Common Thread Pool
        pool = new ThreadPoolExecutor(5, 200,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

        log.info("StorageWorker listening at host:{}, port:{}", server.getInetAddress().getHostAddress(), server.getLocalPort());
        String target = "java.util.concurrent.ThreadPoolExecutor@" + Integer.toHexString(pool.hashCode());

        while (flag) {
            Socket client = server.accept();
            String hostAddress = client.getInetAddress().getHostAddress();
            if (DEBUG_ENABLED) {
                log.debug("Accepting connection from {} .", hostAddress);
                log.debug(pool.toString().replace(target, "Thread pool status: "));
            }
            pool.execute(() -> {
                try (OutputStream outputStream = client.getOutputStream();
                     InputStream inputStream = client.getInputStream();
                     BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                     PrintStream printStream = new PrintStream(new BufferedOutputStream(outputStream))) {

                    // 1.读取握手标记 'upload|backup'
                    String operation = bufferedReader.readLine();
                    if (DEBUG_ENABLED) {
                        log.debug("Accepting operation: {} from {} .", operation, hostAddress);
                    }
                    if (checkOperation(operation)) {
                        println(printStream, "error operation: " + operation);
                        return;
                    }
                    // 2.输出'ok'，表示可以接受此操作
                    println(printStream, Constants.OK_STR);

                    // 3.读取上传文件元信息
                    FileMeta fileMeta = getFileMeta(bufferedReader);

                    final Long size = fileMeta.getSize();
                    if (checkFileInfo(size)) {
                        println(printStream, "error file size:" + size);
                        return;
                    }
                    // 4.输出'ok'，表示可以文件正常
                    println(printStream, Constants.OK_STR);

                    try {
                        // 下载文件
                        if (operation.equals(Constants.DOWNLOAD_STR)) {
                            download(outputStream, fileMeta);
                            return;
                        }
                        // 上传文件
                        upload(inputStream, printStream, fileMeta, operation);
                    } catch (IOException | NoSuchAlgorithmException e) {
                        println(printStream, "error " + operation + " file");
                        log.error("Handle file error:", e);
                    }
                } catch (IOException e) {
                    log.error("Handle file error:", e);
                } finally {
                    IOUtils.close(client);
                }

            });
        }
    }

    protected void download(OutputStream outputStream, FileMeta fileMeta) throws IOException {
        // 5.输出文件
        String path = fileMeta.getPath();
        File file = new File(fileSavePath + path);
        FileInputStream fileInputStream = new FileInputStream(file);
        copy(fileInputStream, outputStream, file.length());
    }

    /**
     * 文件上传
     *
     * @param inputStream 输入流
     * @param printStream 字符输出流
     * @param fileMeta    文件信息
     * @param operation   操作'upload|backup'
     * @throws IOException              e
     * @throws NoSuchAlgorithmException e
     */
    protected void upload(InputStream inputStream, PrintStream printStream, FileMeta fileMeta, String operation) throws IOException, NoSuchAlgorithmException {
        // 5.保存文件
        OutputStream fileOutputStream = getOutputStream(operation, fileMeta);
        // 使用md5校验文件
        String checksum = "";
        if (operation.equals(Constants.UPLOAD_STR)) {
            MessageDigest complete = MessageDigest.getInstance("MD5");
            copyAndBack(inputStream, fileMeta, fileOutputStream, complete);
            checksum = getChecksum(complete);
        } else {
            copy(inputStream, fileOutputStream, fileMeta.getSize());
        }
        IOUtils.close(fileOutputStream);

        // 6.输出信息
        println(printStream, checksum);
    }

    /**
     * 流拷贝（有文件长度）
     *
     * @param inputStream  输入
     * @param outputStream 输出
     * @param size         文件长度
     * @throws IOException e
     */
    protected void copy(InputStream inputStream,
                        OutputStream outputStream, Long size) throws IOException {
        // only save file
        IOUtils.copy(inputStream, outputStream, size);
    }

    protected OutputStream getOutputStream(String operation, FileMeta fileMeta) throws IOException {
        File file = getFile(operation, fileMeta);
        FileUtils.createParentDirs(file);
        return new FileOutputStream(file);
    }

    protected File getFile(String operation, FileMeta fileMeta) {
        String fullPath = (operation.equals(Constants.UPLOAD_STR) ? fileSavePath : fileBakPath) + fileMeta.getPath();
        return new File(fullPath);
    }

    protected String getChecksum(MessageDigest complete) {
        return MD5Checksum.toMD5Checksum(complete);
    }

    protected void copyAndBack(InputStream inputStream, FileMeta fileMeta, OutputStream fileOutputStream, MessageDigest complete) throws IOException {
        StorageServerBackupClient storageServerClient = new StorageServerBackupClient(fileMeta) {
            @Override
            protected void copyIn(InputStream inputStream) throws IOException {
                byte[] buffer = new byte[BUFFER_LENGTH];
                // only save file
                OutputStream socketOutputStream = socketClient.getSocketOutputStream();
                final Long size = fileMeta.getSize();
                for (int i = 0, read; i < size; i += read) {
                    read = inputStream.read(buffer, 0, size - i < BUFFER_LENGTH ? (int) (size - i) : BUFFER_LENGTH);
                    // 多个备份同时写出
                    for (SocketClient client : socketClients) {
                        client.getSocketOutputStream().write(buffer, 0, read);
                    }
                    fileOutputStream.write(buffer, 0, read);
                    // 计算md5
                    complete.update(buffer, 0, read);
                }
                socketOutputStream.flush();
            }
        };
        storageServerClient.backup(inputStream);
        storageServerClient.close();
    }

    protected FileMeta getFileMeta(BufferedReader bufferedReader) throws IOException {
        String fileMetaStr = bufferedReader.readLine();
        ClientUtils.checkResponse(fileMetaStr);
        if (DEBUG_ENABLED) {
            log.debug("Received file meta: {}", fileMetaStr);
        }
        return JSONObject.parseObject(fileMetaStr, FileMeta.class);
    }

    protected void println(PrintStream out, String line) {
        out.println(line);
        out.flush();
    }

    protected boolean checkFileInfo(Long size) {
        if (size == null || size <= 0) {
            log.error("File size can not less than 0, actual {}", size);
            return true;
        }
        return false;
    }

    protected boolean checkOperation(String operation) {
        if (!StringUtils.equalsAny(operation, Constants.UPLOAD_STR, Constants.BACKUP_STR, Constants.DOWNLOAD_STR)) {
            log.error("Unknown operation: '{}'", operation);
            return true;
        }
        return false;
    }

    protected void checkFilePath(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            log.info("File path: {} not exists, creating...", filePath);
            if (!file.mkdir()) {
                throw new IOException("mkdir " + filePath + " failed");
            }
        }

    }
}
