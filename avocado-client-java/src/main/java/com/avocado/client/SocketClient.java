package com.avocado.client;

import lombok.Getter;
import lombok.ToString;

import java.io.*;
import java.net.Socket;

/**
 * SocketClient class
 *
 * @author xuning
 * @date 2019-05-16 09:44
 */
@Getter
@ToString
public class SocketClient implements Closeable{

    protected final String host;
    protected final Integer port;
    protected final Socket socket;
    protected final InputStream socketInputStream;
    protected final BufferedReader bufferedReader;
    protected final OutputStream socketOutputStream;
    protected final PrintStream printStream;

    public SocketClient(String host, Integer port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        socketInputStream = socket.getInputStream();
        socketOutputStream = socket.getOutputStream();
        printStream = new PrintStream(new BufferedOutputStream(socketOutputStream));
        bufferedReader = new BufferedReader(new InputStreamReader(socketInputStream));
    }


    @Override
    public void close() throws IOException {

        if (bufferedReader != null) {
            bufferedReader.close();
        }
        if (socketInputStream != null) {
            socketInputStream.close();
        }
        if (printStream != null) {
            printStream.close();
        }
        if (socketOutputStream != null) {
            socketOutputStream.close();
        }

        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
