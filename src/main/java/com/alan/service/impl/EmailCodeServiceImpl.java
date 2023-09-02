package com.alan.service.impl;

import com.alan.common.vo.Result;
import com.alan.dao.AccountMapper;
import com.alan.entity.Account;
import com.alan.dao.EmailCodeMapper;
import com.alan.entity.EmailCode;
import com.alan.service.EmailCodeService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

/**
 * 邮箱验证码(EmailCode)表服务实现类
 *
 * @author makejava
 * @since 2023-09-01 15:36:43
 */
@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {
    @Resource
    private EmailCodeMapper emailCodeMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private JavaMailSender javaMailSender;


    private void sendEmailCode(String toEmail,String code) {
        try {
            // 1.创建邮件对象
            MimeMessage message = javaMailSender.createMimeMessage();

            // 2.创建邮件助手
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // 3.设置邮件发送方
            helper.setFrom(

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送邮箱验证码
     * @param email 发送到的邮箱
     * @param type  0: 注册， 1： 找回密码
     */
    @Override
    public void sendEmailCode(String email, Integer type) {
        // 1.如果是注册邮箱，则判断邮箱是否已经注册
        if (type == 0) {
            Account account = accountMapper.selectAccountByEmail(email);
            if (account != null) {
                throw new RuntimeException("该邮箱已经注册");
            }
        }

        // 2.随机生成5位纯数字验证码
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 10000));

        // 3.发送邮件
        sendEmailCode(email, code);


        // 5.封装
        EmailCode emailCode = new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setCreateTime(new Date(System.currentTimeMillis()));
        emailCode.setStatus(0);

        // 6.插入数据库
        emailCodeMapper.insert(emailCode);

    }
}
