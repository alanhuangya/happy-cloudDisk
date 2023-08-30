package com.alan.controller;

import com.alan.entity.Account;
import com.alan.entity.constants.Constants;
import com.alan.entity.dto.CreateImageCode;
import com.alan.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 用户信息(UserInfo)表控制层
 *
 * @author makejava
 * @since 2023-08-28 22:05:29
 */
@RestController
@RequestMapping
public class AccountController {
    /**
     * 根据请求类型返回验证码并存入session
     *
     * @param type 0:登录注册  1:邮箱验证码发送  默认0
     */
    @GetMapping("checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session
            , @RequestParam(value = "type", required = false) Integer type) throws IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache"); //响应消息不能缓存
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");

        String code = vCode.getCode();
        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }

}

