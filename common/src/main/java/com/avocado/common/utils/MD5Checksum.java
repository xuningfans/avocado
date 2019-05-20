package com.avocado.common.utils;

import lombok.experimental.UtilityClass;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * MD5Checksum class
 * 计算文件md5
 *
 * @author xuning
 * @date 2019-05-17 09:38
 */
@UtilityClass
public class MD5Checksum {

    public static final int BUFFER_SIZE = 1024;

    public byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[BUFFER_SIZE];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    /**
     * see this How-to for a faster way to convert
     * a byte array to a HEX string
     */
    public String toMD5Checksum(MessageDigest messageDigest) {
        byte[] digest = messageDigest.digest();
        StringBuilder result = new StringBuilder();
        for (byte value : digest) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
