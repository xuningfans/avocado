package com.avocado.common.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;

/**
 * ClientUtils class
 *
 * @author xuning
 * @date 2019-05-17 16:49
 */
@UtilityClass
public class ClientUtils {
    public void checkResponse(String message) throws IOException {
        if (message.startsWith("error")) {
            throw new IOException("server response error:" + message);
        }
    }
}
