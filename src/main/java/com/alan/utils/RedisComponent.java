package com.alan.utils;

import com.alan.entity.constants.Constants;
import com.alan.entity.dto.DownloadFileDto;
import com.alan.entity.dto.SysSettingsDto;
import com.alan.entity.dto.UserSpaceDto;
import com.alan.mapper.FileInfoMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    @Resource
    private FileInfoMapper fileInfoMapper;

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
        if (userSpaceDto.getTotalSpace() == null) {
            userSpaceDto = new UserSpaceDto();

            Long useSpace = fileInfoMapper.selectUseSpace(userId);
            userSpaceDto.setUseSpace(useSpace);
            userSpaceDto.setTotalSpace(getSysSettingsDto().getUserInitUseSpace() * Constants.MB);

            // 保存到redis
            saveUserSpaceUse(userId, userSpaceDto);
        }
        return userSpaceDto;
    }

    /**
     * 保存临时文件大小
     * @param userId 用户id
     * @param fileId 文件id
     * @param fileSize 文件大小
     */
    public void saveFileTempSize(String userId, String fileId, Long fileSize) {
        // 保存到redis
        Long currentSize = getFileTempSize(userId, fileId);
        redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId,
                currentSize + fileSize, Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }

    /**
     * 获取临时文件大小
     * @param userId 用户id
     * @param fileId 文件id
     * @return 文件大小
     */
    public Long getFileTempSize(String userId, String fileId) {
        Long currentSize = getFileSizeFromRedis(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
        return currentSize;
    }

    /**
     * 将临时文件大小保存到redis
     * @param key redis key
     * @return
     */
    private Long getFileSizeFromRedis(String key) {
        // 从redis中获取
        Object sizeObj = redisUtils.get(key);
        // 如果为空，返回0
        if(sizeObj == null){
            return 0L;
        }
        // 如果是Integer或者Long类型，直接返回
        if(sizeObj instanceof Integer){
            return ((Integer) sizeObj).longValue();
        } else if(sizeObj instanceof Long){
            return (Long) sizeObj;
        }
        return 0L;
    }

    /**
     * 保存下载码
     * @param downloadFileDto
     */
    public void saveDownloadCode(DownloadFileDto downloadFileDto) {
        // 设置过期时间为5分钟
        redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD + downloadFileDto.getDownloadCode(),
                downloadFileDto, Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
    }

    public DownloadFileDto getDownloadCode(String code) {
        // 从redis中获取
        return (DownloadFileDto)redisUtils.get(Constants.REDIS_KEY_DOWNLOAD + code);
    }

}
