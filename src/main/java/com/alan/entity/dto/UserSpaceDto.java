package com.alan.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSpaceDto {
    /**
     * 已使用空间
     */
    private Long useSpace;

    /**
     * 总空间
     */
    private Long totalSpace;
}
