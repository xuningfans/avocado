package com.avocado.client;

import com.avocado.common.constants.Constants;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.utils.ClientUtils;
import com.avocado.common.utils.IOUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * StorageServerClient class
 *
 * @author xuning
 * @date 2019-05-15 13:24
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StorageServerBackupClient extends StorageServerClient {

    protected SocketClient[] socketClients;
    private FileMeta fileMeta;

    public StorageServerBackupClient(FileMeta fileMeta) throws IOException {
        // 备份操作，同时初始化所有节点连接
        this.fileMeta = fileMeta;
        final List<String> storageServerHosts = fileMeta.getStorageServerHosts();
        socketClients = new SocketClient[storageServerHosts.size()];

        String[] hosts;
        for (int i = 0; i < storageServerHosts.size(); i++) {
            String host = storageServerHosts.get(i);
            hosts = ClientUtils.getHosts(host);
            socketClients[i] = new SocketClient(hosts[0], Integer.parseInt(hosts[1]));
        }

        for (SocketClient socketClient : socketClients) {
            socketClient.open();
        }
    }


    public void backup(InputStream inputStream) throws IOException {
        upload(null, inputStream, Constants.BACKUP_STR);
    }

    @Override
    protected void handshake(String message, SocketClient socketClient) throws IOException {
        for (SocketClient client : socketClients) {
            super.handshake(message, client);
        }
    }

    @Override
    protected String getResponseFileMeta() {
        return "";
    }

    @Override
    public void close() {
        for (SocketClient socketClient : socketClients) {
            IOUtils.close(socketClient);
        }
    }

}
