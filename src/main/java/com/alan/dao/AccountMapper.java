package com.alan.dao;

import com.alan.entity.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户信息(UserInfo)表数据库访问层
 *
 * @author makejava
 * @since 2023-08-28 22:05:30
 */
@Mapper
public interface AccountMapper {
    //根据用户邮箱查询用户信息
    Account selectAccountByEmail(String email);

}

