package com.avocado.file;

import com.avocado.common.dto.file.FileMeta;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * AvocadoClientHandler class
 *
 * @author xuning
 * @date 2019-05-07 14:48
 */
public interface AvocadoClientHandler {

    /**
     * 上传文件
     *
     * @param inputStream 输入流
     * @param fileName    文件名称
     * @param fileSize    文件大小
     * @return 文件路径
     * @throws Exception e
     */
    FileMeta upload(InputStream inputStream, String fileName, Long fileSize) throws Exception;

    /**
     * 文件下载
     * @param outputStream 写入到流
     * @param fileId 文件id
     * @return 文件信息
     * @throws Exception e
     */
    FileMeta downloadById(OutputStream outputStream, String fileId) throws Exception;

    /**
     * 文件下载
     * @param outputStream 写入到流
     * @param fileId 文件id
     * @return 文件信息
     * @throws Exception e
     */
    FileMeta downloadByPath(OutputStream outputStream, String fileId) throws Exception;
}
