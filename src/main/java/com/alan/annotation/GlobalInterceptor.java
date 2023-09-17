package com.alan.annotation;

import org.springframework.web.bind.annotation.Mapping;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // 作用在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
@Documented // 生成文档
@Mapping // 作用在方法上
public @interface GlobalInterceptor {
    /**
     * 校验参数
     *
     * @return 是否校验参数
     */
    boolean checkParams() default false;

    /**
     * 校验登录
     *
     * @return 是否校验登录
     */
    boolean checkLogin() default true;

    /**
     * 校验管理员
     * @return 是否校验管理员
     */
    boolean checkAdmin() default false;
}