package com.alan.service.impl;

import com.alan.config.AppConfig;
import com.alan.entity.constants.Constants;
import com.alan.entity.dto.SySettingsDto;
import com.alan.exception.BusinessException;
import com.alan.mapper.AccountMapper;
import com.alan.entity.Account;
import com.alan.mapper.EmailCodeMapper;
import com.alan.entity.EmailCode;
import com.alan.service.EmailCodeService;
import com.alan.utils.RedisComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
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

    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);
    @Resource
    private EmailCodeMapper emailCodeMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Override
    public void sendEmailCode(String email, Integer type) {
        // 1.如果是注册,查询邮箱是否已经注册
        if(type == Constants.REGISTER_ZERO) {
            Account account = accountMapper.selectAccountByEmail(email);
            if(account != null) {
                throw new BusinessException("该邮箱已经注册");
            }
        }

        // 2.生成5位纯数字验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 10000));

        // 3.发送邮件
        sendEmailCode(email, code);

        // 4.将验证码存入数据库
        EmailCode emailCode = new EmailCode();
        emailCode.setEmail(email);
        emailCode.setCode(code);
        emailCode.setCreateTime(new Date());
        emailCode.setStatus(0);
        emailCodeMapper.insert(emailCode);
    }

    private void sendEmailCode(String toEmail, String code) {
        try {
            // 1.创建邮箱对象
            MimeMessage message = javaMailSender.createMimeMessage();
            // 2.创建邮箱帮助对象
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            // 3.设置发送信息
            // 发送人
            helper.setFrom(appConfig.getSendUserName());
            helper.setTo(toEmail);

            // 创建系统设置对象
            SySettingsDto sySettingsDto = redisComponent.getSysSettingsDto();
            // 邮件主题
            helper.setSubject(sySettingsDto.getRegisterEmailTitle());
            // 邮件内容
            helper.setText(String.format(sySettingsDto.getRegisterEmailContent(), code));
            // 发送时间
            helper.setSentDate(new Date());

            // 4.发送邮件
            javaMailSender.send(message);

        } catch (Exception e) {
            logger.error("发送邮件失败", e);
            throw new BusinessException("发送邮件失败");
        }
    }
}
