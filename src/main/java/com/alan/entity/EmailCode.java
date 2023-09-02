package com.alan.entity;

import java.util.Date;
import java.io.Serializable;

/**
 * 邮箱验证码(EmailCode)实体类
 *
 * @author makejava
 * @since 2023-09-01 15:36:43
 */
public class EmailCode implements Serializable {
    private static final long serialVersionUID = -59770119314595443L;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 验证码
     */
    private String code;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 状态
     */
    private Integer status;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}

