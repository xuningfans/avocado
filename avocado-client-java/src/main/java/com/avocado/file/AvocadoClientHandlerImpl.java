package com.avocado.file;

import com.avocado.client.StorageServerClient;
import com.avocado.client.TrackerServerClient;
import com.avocado.common.dto.file.FileMeta;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * AvocadoClientHandlerImpl class
 *
 * @author xuning
 * @date 2019-05-07 14:51
 */
@Slf4j
@AllArgsConstructor
public class AvocadoClientHandlerImpl implements AvocadoClientHandler {

    private final String host;

    private final Integer port;

    @Override
    public FileMeta upload(InputStream inputStream, String fileName, Long fileSize) throws Exception {
        StorageServerClient storageServerClient = null;
        try (TrackerServerClient trackerServerClient = new TrackerServerClient(host, port)) {
            trackerServerClient.open();
            FileMeta uploadServer = trackerServerClient.getUploadInfo(fileName, fileSize);
            uploadServer.setSize(fileSize);
            storageServerClient = new StorageServerClient(uploadServer.getStorageServerHost(), uploadServer.getStorageServerPort());
            String checksum = storageServerClient.upload(uploadServer, inputStream);
            uploadServer.setChecksum(checksum);
            // 更新数据库中checksum（非长连接，重新连接）
            trackerServerClient.open();
            trackerServerClient.updateChecksum(uploadServer.getId(), checksum);
            return uploadServer;
        } finally {
            if (storageServerClient != null) {
                storageServerClient.close();
            }
        }

    }

    @Override
    public FileMeta downloadById(OutputStream outputStream, String fileId) throws Exception {
        StorageServerClient storageServerClient = null;
        try (TrackerServerClient trackerServerClient = new TrackerServerClient(host, port)) {
            trackerServerClient.open();
            FileMeta fileMeta = trackerServerClient.getFileMetaInfo(fileId, null);
            storageServerClient = new StorageServerClient(fileMeta.getStorageServerHost(), fileMeta.getStorageServerPort());
            storageServerClient.download(fileMeta, outputStream);
            return fileMeta;
        } finally {
            if (storageServerClient != null) {
                storageServerClient.close();
            }
        }

    }

    @Override
    public FileMeta downloadByPath(OutputStream outputStream, String filePath) throws Exception {
        StorageServerClient storageServerClient = null;
        try (TrackerServerClient trackerServerClient = new TrackerServerClient(host, port)) {
            trackerServerClient.open();
            FileMeta fileMeta = trackerServerClient.getFileMetaInfo(null, filePath);
            storageServerClient = new StorageServerClient(fileMeta.getStorageServerHost(), fileMeta.getStorageServerPort());
            storageServerClient.download(fileMeta, outputStream);
            return fileMeta;
        } finally {
            if (storageServerClient != null) {
                storageServerClient.close();
            }
        }

    }
}
