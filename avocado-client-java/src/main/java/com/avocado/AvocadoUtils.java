package com.avocado;

import com.avocado.common.dto.file.FileMeta;
import com.avocado.file.AvocadoClientHandlerImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * DfsClientUtils class
 *
 * @author xuning
 * @date 2019-05-20 15:05
 */
@Builder
@AllArgsConstructor
public class AvocadoUtils {

    private String host;

    private Integer port;

    public FileMeta uploadFile(File file) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            AvocadoClientHandlerImpl dfsClientHandler = new AvocadoClientHandlerImpl("localhost", 6666);
            return dfsClientHandler.upload(fileInputStream, file.getName(), file.length());
        }
    }

    public FileMeta downloadFile(String pathOrId, File file) throws Exception {
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            AvocadoClientHandlerImpl dfsClientHandler = new AvocadoClientHandlerImpl("localhost", 6666);
            FileMeta fileMeta;
            if (pathOrId.contains(File.separator)) {
                fileMeta = dfsClientHandler.downloadByPath(outputStream, pathOrId);
            } else {
                fileMeta = dfsClientHandler.downloadById(outputStream, pathOrId);
            }
            return fileMeta;
        }
    }
}
