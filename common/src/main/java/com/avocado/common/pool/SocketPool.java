package com.avocado.common.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * SocketPool class
 *
 * @author xuning
 * @date 2019-05-09 13:36
 */
public class SocketPool extends GenericObjectPool<SocketWrapper> {

    public SocketPool(String host, Integer port) {
        super(new SocketPooledObjectFactory(host, port));
        this.setMinIdle(5);
        this.setMaxIdle(5);
    }

}
