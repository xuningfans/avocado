package com.avocado.common.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;

import static com.avocado.common.constants.Constants.ERROR;

/**
 * ClientUtils class
 *
 * @author xuning
 * @date 2019-05-17 16:49
 */
@UtilityClass
public class ClientUtils {


    public void checkResponse(String message) throws IOException {
        if (message.startsWith(ERROR)) {
            throw new IOException("server response error:" + message);
        }
    }
}
