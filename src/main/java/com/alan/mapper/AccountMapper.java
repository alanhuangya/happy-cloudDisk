package com.alan.mapper;

import com.alan.entity.po.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户信息(Account)表数据库访问层
 *
 * @author makejava
 * @since 2023-08-28 22:05:30
 */
@Mapper
public interface AccountMapper<T,P> extends BaseMapper<T,P>{
    //根据用户邮箱查询用户信息
    Account selectAccountByEmail(@Param("email") String email);

    //根据用户昵称查询用户信息
    Account selectAccountByNickName(String nickName);


    void insert(Account account);

    /**
     * 根据用户id修改用户信息
     * @param t
     * @param userId
     * @return
     */
    Integer updateByUserId(@Param("bean") T t, @Param("userId") String userId);

    void updateByEmail(@Param("bean") T t,@Param("email") String email);

    /**
     * 修改用户的空间
     * @param userId
     * @param useSpace
     * @param totalSpace
     * @return
     */
    Integer updateUserSpace(@Param("userId") String userId,@Param("useSpace") Long useSpace, @Param("totalSpace") Long totalSpace);
}

