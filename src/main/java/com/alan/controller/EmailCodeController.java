package com.alan.controller;

import com.alan.entity.EmailCode;
import com.alan.service.EmailCodeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 邮箱验证码(EmailCode)表控制层
 *
 * @author makejava
 * @since 2023-09-01 15:36:43
 */
@RestController
@RequestMapping("emailCode")
public class EmailCodeController {

}

