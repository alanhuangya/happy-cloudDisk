package com.alan.utils;

import com.alan.entity.constants.Constants;
import com.alan.entity.dto.SysSettingsDto;
import com.alan.entity.dto.UserSpaceDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取系统设置
     * 如果不存在就新建
     */
    public SysSettingsDto getSysSettingsDto() {
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (sysSettingsDto == null) {
            sysSettingsDto = new SysSettingsDto();
            saveSysSettingsDto(sysSettingsDto);
        }
        return sysSettingsDto;
    }

    /**
     * 保存设置
     *
     * @param sysSettingsDto
     */
    public void saveSysSettingsDto(SysSettingsDto sysSettingsDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
    }

    /**
     * 保存已使用的空间
     *
     * @param userId
     */
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId,
                userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
    }

    /**
     * 获取已使用的空间
     *
     * @param userId
     */
    public UserSpaceDto getUserSpaceUse(String userId) {
        UserSpaceDto userSpaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (userSpaceDto == null) {
            userSpaceDto = new UserSpaceDto();

            //TODO:查询用户用户已经上传文件大小的总和

            // 设置默认值
            userSpaceDto.setUseSpace(0L);
            userSpaceDto.setTotalSpace(getSysSettingsDto().getUserInitUseSpace() * Constants.MB);

            // 保存到redis
            saveUserSpaceUse(userId, userSpaceDto);
        }
        return userSpaceDto;
    }

}
