package com.avocado.common.utils;

import lombok.experimental.UtilityClass;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.avocado.common.constants.Constants.BUFFER_LENGTH;

/**
 * IOUtils class
 *
 * @author xuning
 * @date 2019-05-17 09:52
 */
@UtilityClass
public class IOUtils {

    private static final int EOF = -1;

    public long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        long count = 0;
        int readLength;
        byte[] buffer = new byte[BUFFER_LENGTH];
        while (EOF != (readLength = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, readLength);
            count += readLength;
        }
        outputStream.flush();
        return count;
    }


    /**
     * 流拷贝（有文件长度）
     *
     * @param inputStream  输入
     * @param outputStream 输出
     * @param size         文件长度
     * @throws IOException e
     */
    public void copy(InputStream inputStream,
                     OutputStream outputStream, Long size) throws IOException {
        byte[] buffer = new byte[BUFFER_LENGTH];
        for (int i = 0, read; i < size; i += read) {
            read = inputStream.read(buffer, 0, size - i < BUFFER_LENGTH ? (int) (size - i) : BUFFER_LENGTH);
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
    }

    public void close(Closeable closeable) {

        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {

            }
        }
    }
}
