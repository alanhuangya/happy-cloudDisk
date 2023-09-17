package com.alan.entity.dto;

import lombok.Data;

@Data
public class DownloadFileDto {
    /**
     * 下载码
     */
    private String downloadCode;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;
}
