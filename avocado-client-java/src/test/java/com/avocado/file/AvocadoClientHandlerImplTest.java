package com.avocado.file;

import com.avocado.common.dto.file.FileMeta;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class AvocadoClientHandlerImplTest {

    @Test
    public void upload() throws Exception {
        String path = "/Users/xuning/Downloads/SKMBT_C36017032800060_0001 (1).jpg";
        File file = new File(path);
        AvocadoClientHandlerImpl dfsClientHandler = new AvocadoClientHandlerImpl("localhost", 6666);
        FileInputStream inputStream = new FileInputStream(file);
        FileMeta upload = dfsClientHandler.upload(inputStream, file.getName(), file.length());
        inputStream.close();
        System.err.println(upload);
        Assert.assertNotNull(upload.getPath());
    }

    @Test
    public void downloadById() {
    }

    @Test
    public void downloadByPath() throws Exception {
        String path = "/Users/xuning/Documents/Java/Idea/avocado/avocado-client-java/target/SKMBT_C36017032800060_0001 (1).jpg";
        File file = new File(path);
        AvocadoClientHandlerImpl dfsClientHandler = new AvocadoClientHandlerImpl("localhost", 6666);
        FileOutputStream outputStream = new FileOutputStream(file);
        FileMeta fileMeta = dfsClientHandler.downloadByPath(outputStream, "/2019/05/17/20190517151410_239.jpg");
        outputStream.close();
        System.err.println(fileMeta);
        Assert.assertEquals(file.length(), fileMeta.getSize().longValue());
    }
}