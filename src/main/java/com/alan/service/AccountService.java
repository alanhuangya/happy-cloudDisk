package com.alan.service;

import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.po.Account;

/**
 * 用户信息(UserInfo)表服务接口
 *
 * @author makejava
 * @since 2023-08-28 22:05:32
 */
public interface AccountService {


    void register(String email, String nickName, String password, String emailCode);

    SessionWebUserDto login(String email, String password);

    void resetPwd(String email, String password, String emailCode);

    /**
     * 根据用户id修改用户信息
     * @param account
     * @param userId
     * @return
     */
    Integer updateUserInfoByUserId(Account account, String userId);
}
