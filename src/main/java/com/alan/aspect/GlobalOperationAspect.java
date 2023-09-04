package com.alan.aspect;

import com.alan.annotation.GlobalInterceptor;
import com.alan.annotation.VerifyParam;
import com.alan.entity.enums.ResponseCodeEnum;
import com.alan.entity.enums.VerifyRegexEnum;
import com.alan.exception.BusinessException;
import com.alan.utils.StringTools;
import com.alan.utils.VerifyUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.swing.text.StyledEditorKit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Aspect // 标记为切面类
@Component // 加入到IOC容器
@Slf4j // 日志
public class GlobalOperationAspect {
    private static final String[] TYPE_BASE = {"java.lang.String", "java.lang.Integer", "java.lang.Long"};

    /**
     * 定义切入点, 拦截所有被@GlobalInterceptor注解的方法
     * 该注解的作用是: 校验参数
     */
    @Pointcut("@annotation(com.alan.annotation.GlobalInterceptor)")
    private void requestInterceptor() {
    }

    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint point) throws NoSuchMethodException {
        log.debug("拦截成功1");
        try {
            // 获取目标对象, 也就是被拦截的方法所在的类
            Object target = point.getTarget();
            // 获取被拦截的方法的参数
            Object[] arguments = point.getArgs();
            // 获取被拦截的方法的名称
            String methodName = point.getSignature().getName();
            // 获取被拦截的方法的参数类型
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            // 获取被拦截的方法
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            // 获取被拦截的方法上的注解
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            // 如果注解为空, 则直接返回
            if (interceptor == null) {
                return;
            }
            // 如果注解不为空, 则执行拦截逻辑
            if (interceptor.checkParams()) {
                validateParams(method, arguments);
            }
        }catch (BusinessException e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }catch (Exception e) {
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Throwable e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void validateParams(Method m, Object[] arguments) throws BusinessException {
        Parameter[] parameters = m.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = arguments[i];

            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            // 如果参数上没有VerifyParam注解, 则直接跳过
            if (verifyParam == null) {
                continue;
            }

            if (ArrayUtils.contains(TYPE_BASE, parameter.getType().getName())) {
                // 基本类型
                checkValue(value, verifyParam);
            } else {
                // 对象
                checkObjValue(parameter, value);
            }

        }

    }

    /**
     * 校验对象，如：Account
     * @param parameter 参数
     * @param value 参数值
     */
    private void checkObjValue(Parameter parameter, Object value) {
        try{
            // 获取参数类型，如：com.alan.entity.Account
            String typeName = parameter.getParameterizedType().getTypeName();
            // 根据参数类型获取Class对象,就是获取Account类的Class对象
            Class clazz = Class.forName(typeName);
            // 获取Account类中的所有属性
            Field[] fields = clazz.getDeclaredFields();
            // 遍历所有属性
            for (Field field : fields) {
                // 获取属性上的VerifyParam注解
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                // 如果属性上没有VerifyParam注解, 则直接跳过
                if (fieldVerifyParam == null) {
                    continue;
                }
                // 设置属性可访问
                field.setAccessible(true);
                // 获取属性值
                Object resultValue = field.get(value);
                // 校验属性值
                checkValue(resultValue, fieldVerifyParam);
            }

        } catch (BusinessException e) {
            log.error("校验参数失败", e);
            throw e;
        } catch (Exception e) {
            log.error("校验参数失败", e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }

    /**
     * 校验基本类型, 如String, Integer, Long
     *
     * @param value       参数值
     * @param verifyParam 参数注解
     */
    private void checkValue(Object value, VerifyParam verifyParam) {
        Boolean isEmpty = value == null || "".equals(value);
        Integer length = value == null ? 0 : value.toString().length();

        // 如果参数值为空, 且注解中的required为true, 则抛出异常
        if (isEmpty && verifyParam.required()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 校验长度
        if (!isEmpty && (verifyParam.max() != -1 && length > verifyParam.max()
                || verifyParam.min() != -1 && verifyParam.min() > length)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // 校验正则
        if (!isEmpty && StringTools.isEmpty(verifyParam.regex().getRegex())
                && !VerifyUtils.verify(verifyParam.regex(), String.valueOf(value))) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }


}
