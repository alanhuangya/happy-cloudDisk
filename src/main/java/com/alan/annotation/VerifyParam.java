package com.alan.annotation;

import com.alan.entity.enums.VerifyRegexEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.FIELD})
public @interface VerifyParam {
    /**
     * 最小值
     * @return
     */
    int min() default -1;

    /**
     * 最大值
     * @return
     */
    int max() default -1;

    /**
     * 是否必填
     * @return
     */
    boolean required() default false;

    /**
     * 正则校验
     * @return
     */
    VerifyRegexEnum regex() default VerifyRegexEnum.NO;

}
