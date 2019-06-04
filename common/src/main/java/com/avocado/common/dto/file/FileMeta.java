package com.avocado.common.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * FileMeta class
 * 最多支持5副本
 *
 * @author xuning
 * @date 2019-05-08 16:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMeta {

    /**
     * 文件id
     */
    private String id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件存储路径
     */
    private String path;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 文件校验值
     */
    private String checksum;

    /**
     * 文件副本数量（最多支持5副本，存储在storageServer字段中）
     */
    private Integer fileReplicationCount;

    private List<String> storageServerHosts;

}
