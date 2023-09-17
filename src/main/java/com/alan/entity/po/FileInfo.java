package com.alan.entity.po;

import java.util.Date;
import java.io.Serializable;

/**
 * 文件信息(FileInfo)实体类
 *
 * @author makejava
 * @since 2023-09-06 11:38:44
 */
public class FileInfo implements Serializable {
    private static final long serialVersionUID = 672779833159769191L;
    /**
     * 文件ID
     */
    private String fileId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * md5值，第一次上传记录
     */
    private String fileMd5;
    /**
     * 父级ID
     */
    private String filePid;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 封面
     */
    private String fileCover;
    /**
     * 文件路径
     */
    private String filePath;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;
    /**
     * 0:文件 1:目录
     */
    private Integer folderType;
    /**
     * 1:视频 2:音频  3:图片 4:文档 5:其他
     */
    private Integer fileCategory;
    /**
     *  1:视频 2:音频  3:图片 4:pdf 5:doc 6:excel 7:txt 8:code 9:zip 10:其他
     */
    private Integer fileType;
    /**
     * 0:转码中 1转码失败 2:转码成功
     */
    private Integer status;
    /**
     * 回收站时间
     */
    private Date recoveryTime;
    /**
     * 删除标记 0:删除  1:回收站  2:正常
     */
    private Integer delFlag;


    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFilePid() {
        return filePid;
    }

    public void setFilePid(String filePid) {
        this.filePid = filePid;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileCover() {
        return fileCover;
    }

    public void setFileCover(String fileCover) {
        this.fileCover = fileCover;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getFolderType() {
        return folderType;
    }

    public void setFolderType(Integer folderType) {
        this.folderType = folderType;
    }

    public Integer getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(Integer fileCategory) {
        this.fileCategory = fileCategory;
    }

    public Integer getFileType() {
        return fileType;
    }

    public void setFileType(Integer fileType) {
        this.fileType = fileType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getRecoveryTime() {
        return recoveryTime;
    }

    public void setRecoveryTime(Date recoveryTime) {
        this.recoveryTime = recoveryTime;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

}

