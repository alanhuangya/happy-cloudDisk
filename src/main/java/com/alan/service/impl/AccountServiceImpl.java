package com.alan.service.impl;

import com.alan.config.AppConfig;
import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.dto.UserSpaceDto;
import com.alan.entity.po.Account;
import com.alan.entity.constants.Constants;
import com.alan.entity.dto.SysSettingsDto;
import com.alan.entity.enums.UserStatusEnum;
import com.alan.exception.BusinessException;
import com.alan.mapper.AccountMapper;
import com.alan.service.AccountService;
import com.alan.service.EmailCodeService;
import com.alan.utils.RedisComponent;
import com.alan.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 用户信息(UserInfo)表服务实现类
 *
 * @author makejava
 * @since 2023-08-28 22:05:32
 */
@Service("userInfoService")
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;


    /**
     * 注册
     *
     * @param email     邮箱
     * @param nickName  昵称
     * @param password  密码
     * @param emailCode 邮箱验证码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String password, String nickName, String emailCode) {
        // 1.校验邮箱验证码
        emailCodeService.checkEmailCode(email, emailCode);
        // 2.判断邮箱是否已经注册
        Account accountByEmail = accountMapper.selectAccountByEmail(email);
        if (accountByEmail != null) {
            throw new RuntimeException("该邮箱已经注册");
        }
        // 3.判断昵称是否已经注册
        Account accountByNickName = accountMapper.selectAccountByNickName(nickName);
        if (accountByNickName != null) {
            throw new RuntimeException("该昵称已经存在");
        }
        // 4.注册
        // 4.1 获取随机id，设置用户id
        String userId = StringTools.getRandomString(Constants.LENGTH_10);
        Account account = new Account();
        account.setUserId(userId);
        account.setEmail(email);
        account.setNickName(nickName);
        account.setPassword(StringTools.encodeByMD5(password));
        account.setJoinTime(new Date());
        account.setUseSpace(0L);
        account.setStatus(UserStatusEnum.ENABLE.getStatus());
        // 4.2 从redis中取出系统设置
        SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
        // 4.3 用户空间
        account.setTotalSpace(sysSettingsDto.getUserInitUseSpace() * Constants.MB);

        // 4.4 插入数据库
        accountMapper.insert(account);
    }

    @Override
    public SessionWebUserDto login(String email, String password) {
        // 1.根据邮箱查询用户
        Account account = accountMapper.selectAccountByEmail(email);
        if (account == null || !account.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码错误");
        }
        // 2.如果账户被禁用
        if (UserStatusEnum.DISABLE.getStatus().equals(account.getStatus())) {
            throw new BusinessException("账号已禁用");
        }

        // 3.更新最后登录时间
        Account updateAccount = new Account();
        updateAccount.setLastLoginTime(new Date());

        // 4.根据id更新用户最后一次操作时间
        String userId = account.getUserId();
        accountMapper.updateByUserId(updateAccount, userId);

        // 5.封装sessionWebDto
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setUserId(userId);
        sessionWebUserDto.setNickName(account.getNickName());

        // 6.判断是不是管理员，可能有多个管理员
        if (ArrayUtils.contains(appConfig.getAdminEmails().split(","), email)) {
            sessionWebUserDto.setIsAdmin(true);
        } else {
            sessionWebUserDto.setIsAdmin(false);
        }
        // 7.用户空间
        UserSpaceDto userSpaceDto = new UserSpaceDto();

        //TODO:查询用户用户已经上传文件大小的总和

        userSpaceDto.setTotalSpace(userSpaceDto.getTotalSpace());

        // 8.保存用户空间
        redisComponent.saveUserSpaceUse(userId, userSpaceDto);

        return sessionWebUserDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(String email, String password, String emailCode) {
        // 1.根据邮箱查询用户
        Account account = accountMapper.selectAccountByEmail(email);
        if (account == null) {
            throw new BusinessException("邮箱账号不存在");
        }
        // 2.校验邮箱验证码
        emailCodeService.checkEmailCode(email, emailCode);
        // 3.更新密码
        Account updateAccount = new Account();
        updateAccount.setPassword(StringTools.encodeByMD5(password));
        accountMapper.updateByEmail(updateAccount, email);

    }

    @Override
    public Integer updateUserInfoByUserId(Account account, String userId) {
        return accountMapper.updateByUserId(account, userId);
    }
}
