package com.alan.entity.dto;

import lombok.Data;

@Data
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
