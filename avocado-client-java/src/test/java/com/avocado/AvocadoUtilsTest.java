package com.avocado;

import com.avocado.common.dto.file.FileMeta;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class AvocadoUtilsTest {

    @Test
    public void uploadFile() throws Exception {
        FileMeta fileMeta = AvocadoUtils.builder()
                .host("localhost")
                .port(6666)
                .build().uploadFile(new File("/Users/xuning/Downloads/SKMBT_C36017032800060_0001 (1).jpg"));
        assertNotNull(fileMeta);
    }

    @Test
    public void downloadFile() throws Exception {

        FileMeta fileMeta = AvocadoUtils.builder()
                .host("localhost")
                .port(6666)
                .build().downloadFile("aa.jpg", new File("bbb.jpg"));
        assertNotNull(fileMeta);
    }
}