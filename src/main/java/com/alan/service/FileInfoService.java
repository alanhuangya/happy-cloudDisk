package com.alan.service;

import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.dto.UploadResultDto;
import com.alan.entity.po.FileInfo;
import com.alan.entity.query.FileInfoQuery;
import com.alan.entity.vo.PaginationResultVO;
import com.alan.entity.vo.ResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 文件信息(FileInfo)表服务接口
 *
 * @author makejava
 * @since 2023-09-06 11:38:44
 */
public interface FileInfoService {

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(FileInfoQuery param);

    /**
     * 分页查询
     *
     * @param param 筛选条件
     * @return
     */
    PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param);

    /**
     * 根据条件查询列表
     *
     * @param param
     * @return 文件信息列表
     */
    List<FileInfo> findListByParam(FileInfoQuery param);


    UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId,
                               MultipartFile file, String fileName, String filePid,
                               String fileMd5, Integer chunkIndex, Integer chunks);

    void getImage(HttpServletResponse response, String imageFolder, String imageName);

    void getFile(HttpServletResponse response, String fileId, String userId);

    FileInfo newFolder(String filePid, String userId, String fileName);


    List<FileInfo> getFolderInfo(String path, String userId);

    FileInfo rename(String fileId, String userId, String fileName);

    List<FileInfo> loadAllFolder(String userId, String filePid, String currentFileIds);

    void changeFileFolder(String fileIds, String filePid, String userId);

    String createDownloadUrl(String fileId, String userId);

    void download(HttpServletResponse response, HttpServletRequest request, String code) throws UnsupportedEncodingException;

    void removeFile2RecycleBatch(String userId, String fileIds);
}
