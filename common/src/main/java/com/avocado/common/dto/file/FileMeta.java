package com.avocado.common.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FileMeta class
 *
 * @author xuning
 * @date 2019-05-08 16:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMeta {

    private String id;

    private String name;

    private String path;

    private Long size;

    private String checksum;

    private String storageServerHost;
    private Integer storageServerPort;

    private String backupServerHost;
    private Integer backupServerPort;
}
