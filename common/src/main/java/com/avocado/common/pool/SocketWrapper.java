package com.avocado.common.pool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * SocketWrapper class
 *
 * @author xuning
 * @date 2019-05-09 13:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketWrapper {

    private Socket socket;

    private InputStream inputStream;

    private OutputStream outputStream;
}
