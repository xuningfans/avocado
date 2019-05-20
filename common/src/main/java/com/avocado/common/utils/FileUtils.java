package com.avocado.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileUtils class
 *
 * @author xuning
 * @date 2017/5/9 11:24
 */
@Slf4j
@UtilityClass
public class FileUtils {

    public static void createParentDirs(File file) throws IOException {
        File parent = file.getCanonicalFile().getParentFile();
        if (parent != null) {
            if (parent.exists()) {
                return;
            }
            if (parent.mkdirs() && !parent.isDirectory()) {
                throw new IOException("Unable to create parent directories of " + file);
            }
        }
    }

    /**
     * 保存byte[]文件
     *
     * @return path 保存文件路径 格式： .../upload/yyyyMM/yyyyMMddHHmmss_SSS.xxx
     * @throws IOException e
     */
    public static String saveFile(String basePath, String fileName, byte[] bytes) throws IOException {
        FileOutputStream fos = null;
        try {
            String path = getSaveFullPath(basePath, fileName);
            fos = new FileOutputStream(path);
            fos.write(bytes);
            return path;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 保存inputStream文件
     *
     * @return path 保存文件路径 格式： .../upload/yyyyMM/yyyyMMddHHmmss_SSS.xxx
     * @throws IOException e
     */
    public static String saveFile(String basePath, String fileName, InputStream inputStream) throws IOException {
        String path = getSaveFullPath(basePath, fileName);
        try (FileOutputStream fileOutputStream = new FileOutputStream(path)) {
            IOUtils.copy(inputStream, fileOutputStream);
            return path;
        }
    }

    public static String getSaveFullPath(String basePath, String fileName) {
        String savePath = getSaveFilePath(basePath);
        String newFileName = getSaveFileName(fileName);
        return savePath + "/" + newFileName;
    }

    /**
     * generate saved file name<p/>
     * aaa.jpg ==> yyyyMMddHHmmss_SSS.jpg
     *
     * @param fileName 文件名称
     * @return 文件保存名称
     */
    public static String getSaveFileName(String fileName) {
        // 文件名称
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss_SSS");
        String fileExt = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        // .yyyyMMddHHmmss_SSS.xxx
        // 生成文件名
        return df.format(new Date()) + fileExt;
    }

    public static String getSaveFilePath(String path) {
        // 文件保存目录路径
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String savePath = path + (path.endsWith("/") ? "" : "/") + sdf.format(new Date());
        File saveDirFile = new File(savePath);
        if (!saveDirFile.exists()) {
            saveDirFile.mkdirs();
        }
        return savePath;
    }

    public static String getRelativePath(String basePath, String path) {
        return path.replace(basePath, "");
    }

    public static File byteToTmpFile(byte[] bytes, String fileName) {
        String tmpPath = getTmpPath();
        File file = new File(tmpPath + "/" + fileName);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(bytes);
        } catch (IOException e) {
            log.error("写入临时文件失败：", e);
        }
        return file;
    }

    public static String getTmpPath() {
        return System.getProperty("java.io.tmpdir");
    }

}
