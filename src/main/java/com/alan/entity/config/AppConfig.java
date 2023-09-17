package com.alan.entity.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appconfig")
@Data
public class AppConfig {
    /**
     * 发送人
     */
    @Value("${spring.mail.username:}")
    private String sendUserName;

    /**
     * 是否为管理员
     */
    @Value("${admin.emails:}")
    private String adminEmails;

    /**
     * 是否为管理员
     */
    @Value("${dev:false}")
    private Boolean dev;


    /**
     * 项目文件夹
     */
    @Value("${project.folder:}")
    private String projectFolder;
}