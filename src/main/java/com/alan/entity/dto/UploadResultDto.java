package com.alan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传结果
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UploadResultDto implements Serializable {
    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件上传状态
     */
    private String status;
}
