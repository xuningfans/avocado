package com.avocado.common.utils;

import com.avocado.common.constants.Constants;
import com.avocado.common.dto.file.FileMeta;
import com.avocado.common.exception.AvocadoException;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.List;

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
        if (Constants.OK_STR.equals(message)) {
            return;
        }
        if (message.startsWith(ERROR)) {
            throw new IOException("server response error:" + message);
        }
    }

    public String[] checkAndGetServer(FileMeta uploadInfo, int index) {
        final List<String> storageServerHosts = uploadInfo.getStorageServerHosts();
        if (storageServerHosts.size() + 1 < index) {
            throw new AvocadoException("Storage server size must greater than " + (index - 1));
        }
        final String host = storageServerHosts.get(index);
        return getHosts(host);
    }

    public String[] getHosts(String host) {
        final String split = ":";
        if (!host.contains(split)) {
            throw new AvocadoException("Storage server host must have a work port.");
        }
        return host.split(split);
    }
}
