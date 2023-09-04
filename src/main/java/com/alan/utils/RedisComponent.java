package com.alan.utils;

import com.alan.entity.constants.Constants;
import com.alan.entity.dto.SySettingsDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    public SySettingsDto getSysSettingsDto() {
        SySettingsDto sySettingsDto = (SySettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(sySettingsDto==null) {
            sySettingsDto = new SySettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sySettingsDto);
        }
        return sySettingsDto;
    }

}
