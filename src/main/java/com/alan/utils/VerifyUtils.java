package com.alan.utils;

import com.alan.entity.enums.VerifyRegexEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyUtils {
    /**
     * 校验，如果value为空，直接返回false，否则返回正则校验结果，true为通过，false为不通过
     * @param regex 正则表达式
     * @param value 要校验的值
     * @return 校验结果
     */
    public static boolean verify(String regex, String value) {
        // 如果value为空，直接返回false
        if (StringTools.isEmpty(value)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    /**
     * 枚举类校验
     * @param regex 正则表达式（枚举类中的正则表达式）
     * @param value 要校验的值
     * @return 校验结果
     */
    public static boolean verify(VerifyRegexEnum regex, String value) {
        return verify(regex.getRegex(), value);
    }
}
