package com.alan.service;

import com.alan.entity.EmailCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * 邮箱验证码(EmailCode)表服务接口
 *
 * @author makejava
 * @since 2023-09-01 15:36:43
 */
public interface EmailCodeService {



    void sendEmailCode(String email, Integer type);
}
