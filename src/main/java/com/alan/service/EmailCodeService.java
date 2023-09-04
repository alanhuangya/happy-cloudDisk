package com.alan.service;

/**
 * 邮箱验证码(EmailCode)表服务接口
 *
 * @author makejava
 * @since 2023-09-01 15:36:43
 */
public interface EmailCodeService {


    /**
     * 发送邮箱验证码
     * @param email
     * @param type
     */
    void sendEmailCode(String email, Integer type);

    /**
     * 校验邮箱验证码
     * @param email
     * @param emailCode
     */
    void checkEmailCode(String email, String emailCode);
}
