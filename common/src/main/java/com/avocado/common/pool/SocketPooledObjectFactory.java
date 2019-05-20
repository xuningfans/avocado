package com.avocado.common.pool;

import lombok.RequiredArgsConstructor;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.net.Socket;

/**
 * SocketPooledObjectFactory class
 *
 * @author xuning
 * @date 2019-05-09 13:24
 */
@RequiredArgsConstructor
public class SocketPooledObjectFactory implements PooledObjectFactory<SocketWrapper> {

    private final String host;

    private final Integer port;

    @Override
    public PooledObject<SocketWrapper> makeObject() throws Exception {
        Socket socket = new Socket(host, port);
        SocketWrapper socketWrapper = SocketWrapper.builder()
                .socket(socket)
                .inputStream(socket.getInputStream())
                .outputStream(socket.getOutputStream())
                .build();
        return new DefaultPooledObject<>(socketWrapper);
    }

    @Override
    public void destroyObject(PooledObject<SocketWrapper> pooledObject) throws Exception {
        Socket socket = pooledObject.getObject().getSocket();
        if (socket.isConnected()) {
            socket.close();
        }
    }

    @Override
    public boolean validateObject(PooledObject<SocketWrapper> pooledObject) {
        return pooledObject.getObject().getSocket().isConnected();
    }

    @Override
    public void activateObject(PooledObject<SocketWrapper> pooledObject) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<SocketWrapper> pooledObject) throws Exception {

    }

}
