package com.alan.entity.dto;

import lombok.Data;

/**
 * session中的用户信息
 */
@Data
public class SessionWebDto {
    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 是否是管理员
     */
    private Boolean isAdmin;

    /**
     * 用户使用空间
     */
    private Long useSpace;

    /**
     * 用户总空间
     */
    private Long totalSpace;

    /**
     * 用户头像
     */
    private String avatar;
}
