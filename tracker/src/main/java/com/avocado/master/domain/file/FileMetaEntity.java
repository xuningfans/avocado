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

    @Column(name = "storage_server_host1")
    private String storageServerHost1;

    @Column(name = "storage_server_port1")
    private Integer storageServerPort1;

    @Column(name = "storage_server_host2")
    private String storageServerHost2;

    @Column(name = "storage_server_port2")
    private Integer storageServerPort2;

    @Column(name = "storage_server_host3")
    private String storageServerHost3;

    @Column(name = "storage_server_port3")
    private Integer storageServerPort3;

    @Column(name = "storage_server_host4")
    private String storageServerHost4;

    @Column(name = "storage_server_port4")
    private Integer storageServerPort4;

    @Column(name = "storage_server_host5")
    private String storageServerHost5;

    @Column(name = "storage_server_port5")
    private Integer storageServerPort5;

    @CreatedDate
    @Column(name = "created_date")
    protected Date createdDate;
}
