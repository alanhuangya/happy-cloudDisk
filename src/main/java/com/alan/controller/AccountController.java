package com.alan.controller;

import com.alan.annotation.GlobalInterceptor;
import com.alan.annotation.VerifyParam;
import com.alan.entity.config.AppConfig;
import com.alan.controller.basecontroller.BaseController;
import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.dto.UserSpaceDto;
import com.alan.entity.enums.VerifyRegexEnum;
import com.alan.entity.po.Account;
import com.alan.entity.vo.ResponseVO;
import com.alan.entity.constants.Constants;
import com.alan.entity.dto.CreateImageCode;
import com.alan.exception.BusinessException;
import com.alan.service.AccountService;
import com.alan.service.EmailCodeService;
import com.alan.utils.RedisComponent;
import com.alan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 用户信息(Account)表控制层
 *
 * @author makejava
 * @since 2023-08-28 22:05:29
 */
@RestController
@RequestMapping
@Slf4j
public class AccountController extends BaseController {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private AccountService accountService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

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
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO sendEmailCode(HttpSession session,
                                    @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL) String email,
                                    @VerifyParam(required = true) String checkCode,
                                    @VerifyParam(required = true) Integer type) {

        try {
            // 如果验证码错误
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                return getErrorResponseVO("图片验证码错误");
            }
            // 如果验证码正确，发送邮箱验证码
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO("验证码发送成功");
        } finally {
            // 无论是否发送成功，都要清除session中的验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    @PostMapping("register")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL) String email,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String nickName,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode) {
        try {
            // 判断图片验证码是否正确
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码错误");
            }
            // 注册
            accountService.register(email, password, nickName, emailCode);
            return getSuccessResponseVO("注册成功");
        } finally {
            // 无论是否发送成功，都要清除session中的验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * 登录
     *
     * @param session   用于存入session
     * @param email     邮箱
     * @param password  密码
     * @param checkCode 验证码
     * @return 登录成功后的用户信息
     */
    @PostMapping("/login")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO login(HttpSession session,
                            @VerifyParam(required = true) String email,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String checkCode) {
        try {
            // 判断图片验证码是否正确
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码错误");
            }
            // 注册
            // 创建sessionWebDto对象，用于存入session
            SessionWebUserDto sessionWebUserDto = accountService.login(email, password);
            // 将sessionWebDto存入session
            session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
            return getResponseVO("登录成功", sessionWebUserDto);
        } finally {
            // 无论是否发送成功，都要清除session中的验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    @PostMapping("resetPwd")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public ResponseVO resetPwd(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL) String email,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode) {
        try {
            // 判断图片验证码是否正确
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码错误");
            }
            // 注册
            // 创建sessionWebDto对象，用于存入session
            accountService.resetPwd(email, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            // 无论是否发送成功，都要清除session中的验证码
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    /**
     * 获取头像
     * @param response 响应
     * @param userId 用户id
     */
    @RequestMapping("getAvatar/{userId}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void getAvatar(HttpServletResponse response,
                                @VerifyParam(required = true) @PathVariable("userId") String userId) {
        String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        File folder = new File(appConfig.getProjectFolder() +avatarFolderName);
        // 如果文件夹不存在，则创建
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 设置头像路径
        String avatarPath = appConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        File file = new File(avatarPath);
        // 如果文件不存在，则使用默认头像
        if (!file.exists()) {
            // 如果用户没有设置头像，则使用默认头像
            String path = appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFUALT;
            if (!new File(path).exists()) {
                printNoDefaultImage(response);
                return;
            }
            // 设置默认头像路径
            avatarPath = appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFUALT;
        }

        // 设置响应的媒体类型，这样浏览器会识别出响应的是图片
        response.setContentType("image/jpg");
        log.info("头像路径：{}", avatarPath);
        readFile(response, avatarPath);
    }


    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE); // 设置响应的媒体类型，这样浏览器会识别出响应的是图片
        response.setStatus(HttpStatus.OK.value()); // 设置响应状态码
        PrintWriter writer = null; // 获取响应的输出流
        try {
            writer = response.getWriter(); // 输出无默认图
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
            writer.close();
        } catch (Exception e) {
            log.error("输出无默认图失败", e);
        } finally {
            writer.close();
        }
    }

    /**
     * 获取用户信息
     * @param session
     * @return 用户信息
     */
    @RequestMapping("getUserInfo")
    @GlobalInterceptor
    public ResponseVO getUserInfo(HttpSession session) {
        // 从session中获取用户信息
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);

        // 返回用户信息
        return getSuccessResponseVO(sessionWebUserDto);
    }

    @PostMapping("getUseSpace")
    public ResponseVO getUseSpace(HttpSession session) {
        // 从session中获取用户信息
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);

        // 从redis中获取用户使用空间
        UserSpaceDto spaceDto = redisComponent.getUserSpaceUse(sessionWebUserDto.getUserId());


        // 返回用户信息
        return getSuccessResponseVO(spaceDto);
    }




    /**
     * 退出登录
     * @param session
     * @return
     */
    @RequestMapping("logout")
    public ResponseVO logout(HttpSession session) {
        // 将session中的用户信息清除，invalidate()方法会将session中的所有信息清除
        session.invalidate();

        return getSuccessResponseVO(null);
    }

    /**
     * 更新用户头像
     * @param session
     * @param avatar 头像文件,MultipartFile类型是spring提供的文件上传类型
     * @return
     */
    @PostMapping("updateUserAvatar")
    @GlobalInterceptor
    public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar) {
        // 从session中获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);

        // 获取用户头像文件夹路径
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;

        // 获取用户头像文件夹
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);

        // 如果文件夹不存在，则创建
        if(!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }

        // 获取用户头像文件
        File targetFile = new File(targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX);

        // 上传头像到指定位置
        try {
            avatar.transferTo(targetFile);
        } catch (Exception e) {
            log.error("上传头像失败", e);
        }
        // 更新用户头像
        Account account = new Account();
        account.setQqAvatar("");

        // 更新数据库
        accountService.updateUserInfoByUserId(account,webUserDto.getUserId());

        // 更新session中的用户信息
        webUserDto.setAvatar(null);
        session.setAttribute(Constants.SESSION_KEY, webUserDto);
        
        return getSuccessResponseVO(null);
    }

    @PostMapping("updatePassword")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updatePassword(HttpSession session,
                                     @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min = 8,max = 18) String password) {
        // 从session中获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);

        // 更新用户密码
        Account account = new Account();
        account.setPassword(StringTools.encodeByMD5(password));

        // 更新数据库
        accountService.updateUserInfoByUserId(account,webUserDto.getUserId());

        return getSuccessResponseVO(null);
    }



}

