package com.alan.entity;

import java.util.Date;
import java.io.Serializable;

/**
 * 用户信息(UserInfo)实体类
 *
 * @author makejava
 * @since 2023-08-28 22:05:31
 */
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


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQqOpenId() {
        return qqOpenId;
    }

    public void setQqOpenId(String qqOpenId) {
        this.qqOpenId = qqOpenId;
    }

    public String getQqAvatar() {
        return qqAvatar;
    }

    public void setQqAvatar(String qqAvatar) {
        this.qqAvatar = qqAvatar;
    }

    public Date getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Date joinTime) {
        this.joinTime = joinTime;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getUseSpace() {
        return useSpace;
    }

    public void setUseSpace(Long useSpace) {
        this.useSpace = useSpace;
    }

    public Long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }

}

