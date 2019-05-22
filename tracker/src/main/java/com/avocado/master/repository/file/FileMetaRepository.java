package com.avocado.master.repository.file;

import com.avocado.master.domain.file.FileMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * FileMetaRepository class
 *
 * @author xuning
 * @date 2019-05-21 18:56
 */
public interface FileMetaRepository extends JpaRepository<FileMetaEntity, String> {

    /**
     * 通过 id 更新 checksum
     *
     * @param checksum 校验码
     * @param id       id
     * @return 受影响行数
     */
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE file_meta SET checksum = ? WHERE id = ?")
    int updateChecksumById(String checksum, String id);

    /**
     * 通过文件路径查找
     * @param path 路径
     * @return 文件元信息
     */
    FileMetaEntity findByPath(String path);

}