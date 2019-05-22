package com.avocado.master.service.file;

import com.avocado.common.dto.file.FileMeta;
import com.avocado.master.domain.file.FileMetaEntity;
import com.avocado.master.repository.file.FileMetaRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * FileMetaService class
 * 文件元信息Service
 *
 * @author xuning
 * @date 2019-05-21 19:06
 */
@Service
@Transactional(readOnly = true, rollbackFor = Exception.class)
public class FileMetaService {

    @Resource
    private FileMetaRepository fileMetaRepository;

    /**
     * 保存文件元信息
     *
     * @param fileMetaEntity 文件实体
     * @return 保存后的实体
     */
    @Transactional(rollbackFor = Exception.class)
    public FileMeta save(FileMetaEntity fileMetaEntity) {
        FileMetaEntity fileMetaEntitySaved = fileMetaRepository.save(fileMetaEntity);
        FileMeta fileMeta = new FileMeta();
        BeanUtils.copyProperties(fileMetaEntitySaved, fileMeta);
        return fileMeta;
    }

    /**
     * 通过 id 更新 checksum
     *
     * @param checksum 校验码
     * @param id       id
     * @return 受影响行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateChecksumById(String checksum, String id) {
        return fileMetaRepository.updateChecksumById(checksum, id);
    }

    /**
     * 通过id查找文件元信息
     *
     * @param id id
     * @return 文件元信息
     */
    public FileMeta findById(String id) {
        Optional<FileMetaEntity> optionalFileMetaEntity = fileMetaRepository.findById(id);
        if (optionalFileMetaEntity.isPresent()) {
            FileMetaEntity fileMetaEntity = optionalFileMetaEntity.get();
            FileMeta fileMeta = new FileMeta();
            BeanUtils.copyProperties(fileMetaEntity, fileMeta);
            return fileMeta;
        }

        return null;
    }

    /**
     * 通过路径查找文件元信息
     *
     * @param path 路径
     * @return 文件元信息
     */
    public FileMeta findByPath(String path) {
        FileMetaEntity fileMetaEntity = fileMetaRepository.findByPath(path);
        if (fileMetaEntity != null) {
            FileMeta fileMeta = new FileMeta();
            BeanUtils.copyProperties(fileMetaEntity, fileMeta);
            return fileMeta;
        }
        return null;
    }
}
