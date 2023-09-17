package com.alan.mapper;

import com.alan.entity.po.FileInfo;
import com.alan.entity.query.FileInfoQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 文件信息(FileInfo)表数据库访问层
 *
 * @author makejava
 * @since 2023-09-06 11:38:43
 */
public interface FileInfoMapper {

    /**
     * 根据条件查询列表
     */
    Integer selectCount(@Param("query") FileInfoQuery query);

    /**
     * 根据条件查询列表
     */
    List<FileInfo> selectList(@Param("query") FileInfoQuery query);

    /**
     * 查询使用空间
     *
     * @param userId
     * @return
     */
    Long selectUseSpace(@Param("userId") String userId);

    Integer insert(@Param("bean") FileInfo bean);

    FileInfo selectByFileIdAndUserId(@Param("fileId") String fileId, @Param("userId") String userId);

    void updateFileStatusWithOldStatus(@Param("fileId") String fileId, @Param("userId") String userId, @Param("bean") FileInfo bean, @Param("oldStatus") Integer oldStatus);

    Integer updateByFileIdAndUserId(@Param("bean") FileInfo bean, @Param("fileId") String fileId, @Param("userId") String userId);

    void updateDelFlagByBatch(@Param("bean") FileInfo fileInfo,
                              @Param("userId") String userId,
                              @Param("filePidList") List<String> filePidList,
                              @Param("fileIdList") List<String> fileIdList,
                              @Param("oldDelFlag") Integer oldDelFlag);
}


