package com.alan.controller;

import com.alan.annotation.GlobalInterceptor;
import com.alan.annotation.VerifyParam;
import com.alan.controller.basecontroller.BaseController;
import com.alan.entity.enums.VerifyRegexEnum;
import com.alan.entity.vo.ResponseVO;
import com.alan.entity.constants.Constants;
import com.alan.entity.dto.CreateImageCode;
import com.alan.exception.BusinessException;
import com.alan.service.EmailCodeService;
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
public class AccountController extends BaseController {


    @Resource
    private EmailCodeService emailCodeService;

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

    /**
     * 获取邮箱验证码
     *
     * @param session   用于获取session中的验证码
     * @param email     邮箱
     * @param checkCode 验证码
     * @param type      0:注册  1:找回密码
     * @return
     */
    @PostMapping("sendEmailCode")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO sendEmailCode(HttpSession session,
                                    @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL) String email,
                                    @VerifyParam(required = true) String checkCode,
                                    @VerifyParam(required = true) Integer type) {

        try {
            // 如果验证码错误
            if (!checkCode.equals(session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                return getErrorResponseVO("验证码错误");
            }
            // 如果验证码正确，发送邮箱验证码
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO("验证码发送成功");
        } finally {
            // 无论是否发送成功，都要清除session中的验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

}

