package com.avocado.master.domain.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * FileMetaEntity class
 *
 * @author xuning
 * @date 2019-05-21 18:50
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_meta")
@EntityListeners(AuditingEntityListener.class)
public class FileMetaEntity {

    @Id
    @Column(length = 40)
    private String id;

    @Column(length = 500)
    private String name;

    @Column(length = 500)
    private String path;

    private Long size;

    private String checksum;

    @Column(name = "storage_server_host")
    private String storageServerHost;

    @Column(name = "storage_server_port")
    private Integer storageServerPort;

    @Column(name = "backup_server_host")
    private String backupServerHost;

    @Column(name = "backup_server_port")
    private Integer backupServerPort;

    @CreatedDate
    @Column(name = "created_date")
    protected Date createdDate;
}
