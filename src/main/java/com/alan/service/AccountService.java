package com.alan.service;

import com.alan.entity.dto.SessionWebDto;

/**
 * 用户信息(UserInfo)表服务接口
 *
 * @author makejava
 * @since 2023-08-28 22:05:32
 */
public interface AccountService {


    void register(String email, String nickName, String password, String emailCode);

    SessionWebDto login(String email, String password);
}
