package com.alan.config;

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
}
