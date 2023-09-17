package com.alan.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.io.Serializable;

/**
 * 用户信息(Account)实体类
 *
 * @author makejava
 * @since 2023-08-28 22:05:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {
    private static final long serialVersionUID = -43484800503424133L;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 邮箱
     */
    private String email;
    /**
     * qqOpenId
     */
    private String qqOpenId;
    /**
     * qq头像
     */
    private String qqAvatar;
    /**
     * 加入时间
     */
    private Date joinTime;
    /**
     * 最后登录时间
     */
    private Date lastLoginTime;
    /**
     * 0:禁用 1:启用
     */
    private Integer status;
    /**
     * 密码
     */
    private String password;
    /**
     * 使用空间 单位:byte
     */
    private Long useSpace;
    /**
     * 总空间
     */
    private Long totalSpace;




}

