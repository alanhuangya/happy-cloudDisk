package com.alan.service.impl;

import com.alan.controller.basecontroller.BaseController;
import com.alan.entity.config.AppConfig;
import com.alan.entity.constants.Constants;
import com.alan.entity.dto.DownloadFileDto;
import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.dto.UploadResultDto;
import com.alan.entity.dto.UserSpaceDto;
import com.alan.entity.enums.*;
import com.alan.entity.po.FileInfo;
import com.alan.entity.query.FileInfoQuery;
import com.alan.entity.query.SimplePage;
import com.alan.entity.vo.PaginationResultVO;
import com.alan.entity.vo.ResponseVO;
import com.alan.exception.BusinessException;
import com.alan.mapper.AccountMapper;
import com.alan.mapper.FileInfoMapper;
import com.alan.service.FileInfoService;
import com.alan.utils.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * 文件信息(FileInfo)表服务实现类
 *
 * @author makejava
 * @since 2023-09-06 11:38:44
 */
@Slf4j
@Service("fileInfoService")
public class FileInfoServiceImpl extends BaseController implements FileInfoService {

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    @Lazy
    private FileInfoServiceImpl fileInfoService;

    @Resource
    private AppConfig appConfig;


    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(FileInfoQuery param) {
        return fileInfoMapper.selectCount(param);
    }


    @Override
    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
        // 根据条件查询总数
        int count = fileInfoMapper.selectCount(param);

        // 每页显示的条数，如果没有传入，默认为15条
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        // 创建分页对象，传入当前页码，总条数，每页显示的条数
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);

        // 将分页对象传入查询条件中
        param.setSimplePage(page);

        // 查询列表
        List<FileInfo> list = findListByParam(param);

        // 将查询结果封装到分页结果对象中
        PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;


    }

    /**
     * 根据条件查询列表
     *
     * @param param
     * @return
     */
    @Override
    public List<FileInfo> findListByParam(FileInfoQuery param) {
        return fileInfoMapper.selectList(param);
    }

    /**
     * 上传文件
     *
     * @param webUserDto 用户信息
     * @param fileId     文件id
     * @param file       文件
     * @param fileName   文件名
     * @param filePid    文件父id
     * @param fileMd5    文件md5
     * @param chunkIndex 当前分片索引
     * @param chunks     分片总数
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId,
                                      MultipartFile file, String fileName,
                                      String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {

        // 创建上传结果对象
        UploadResultDto resultDto = new UploadResultDto();
        boolean uploadSuccess = true;
        File tempFileFolder = null;

        try {
            // 判断文件id是否为空，如果为空，就生成一个随机的文件id
            if (StringTools.isEmpty(fileId)) {
                fileId = StringTools.getRandomString(Constants.LENGTH_10);
            }
            // 将文件id设置到上传结果对象中
            resultDto.setFileId(fileId);
            Date curDate = new Date();

            // 获取用户已经使用的空间
            UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());

            if (chunkIndex == 0) {
                // 第一个分片
                // 将文件md5设置到查询条件中，查询数据库中是否存在该文件，如果存在，就不用再上传了，md5值是指文件的唯一标识
                FileInfoQuery fileInfoQuery = new FileInfoQuery();
                fileInfoQuery.setFileMd5(fileMd5);

                // 设置分页对象，查询第一页的数据,0表示第一页，1表示每页显示的条数
                fileInfoQuery.setSimplePage(new SimplePage(0, 1));

                // 状态为正常的文件
                fileInfoQuery.setStatus(FileStatusEnums.USING.getStatus());

                List<FileInfo> dbFileList = fileInfoMapper.selectList(fileInfoQuery);

                //秒传
                if (!dbFileList.isEmpty()) {
                    // 获取第一个文件
                    FileInfo dbFile = dbFileList.get(0);

                    // 判断文件大小，如果文件大小超过了用户的总空间，就抛出异常
                    if (dbFile.getFileSize() + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
                        throw new BusinessException(ResponseCodeEnum.CODE_904);
                    }

                    // 设置文件属性
                    dbFile.setFileId(fileId);
                    dbFile.setFilePid(filePid);
                    dbFile.setUserId(webUserDto.getUserId());
                    dbFile.setCreateTime(curDate);
                    dbFile.setLastUpdateTime(curDate);
                    dbFile.setStatus(FileStatusEnums.USING.getStatus());
                    dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
                    dbFile.setFileMd5(fileMd5);

                    // 文件重命名
                    fileName = autoRename(fileId, webUserDto.getUserId(), fileName);
                    dbFile.setFileName(fileName);
                    fileInfoMapper.insert(dbFile);

                    // 设置状态为秒传
                    resultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());

                    // 更新用户使用空间
                    updateUserSpace(webUserDto, dbFile.getFileSize());

                    return resultDto;
                }
            }

            // 从redis中获取临时文件大小
            Long currentTempSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);

            // 判断文件大小，如果文件大小超过了用户的总空间，就抛出异常
            if (file.getSize() + currentTempSize + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
                throw new BusinessException(ResponseCodeEnum.CODE_904);
            }

            // 暂存临时目录
            String tempFolderName = appConfig.getProjectFolder() + "/file" + Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName = webUserDto.getUserId() + fileId;

            tempFileFolder = new File(tempFolderName + currentUserFolderName);

            if (!tempFileFolder.exists()) {
                tempFileFolder.mkdirs();
            }

            // 上传到服务器上的文件名
            File newFile = new File(tempFileFolder.getPath() + "/" + chunkIndex);
            // transferTo()方法将文件写入到指定的文件中
            file.transferTo(newFile);

            // 如果不是最后一个分片，就将状态设置为正在上传
            if (chunkIndex < chunks - 1) {
                // 将状态设置为正在上传
                resultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());

                // 保存临时文件
                redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId, file.getSize());

                return resultDto;
            }

            // 保存临时文件
            redisComponent.saveFileTempSize(webUserDto.getUserId(), fileId, file.getSize());

            // 最后一个分片上传完成，记录数据库，异步合并分片
            // 记录文件的月份
            String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
            String fileSuffix = StringTools.getFileSuffix(fileName);
            //真实文件名
            //userId+fileId+文件后缀
            String realFileName = currentUserFolderName + fileSuffix;
            // 根据后缀从枚举类中获取文件类型
            FileTypeEnums fileTypeEnums = FileTypeEnums.getFileTypeBySuffix(fileSuffix);

            // 自动重命名，因为文件名可能重复
            fileName = autoRename(fileId, webUserDto.getUserId(), fileName);

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(fileId);
            fileInfo.setUserId(webUserDto.getUserId());
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setFileName(fileName);
            fileInfo.setFilePath(month + "/" + realFileName);
            fileInfo.setFilePid(filePid);
            fileInfo.setCreateTime(curDate);
            fileInfo.setLastUpdateTime(curDate);
            fileInfo.setFileCategory(fileTypeEnums.getCategory().getCategory());
            fileInfo.setFileType(fileTypeEnums.getType());
            fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
            fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());
            fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());

            fileInfoMapper.insert(fileInfo);

            Long totalSize = redisComponent.getFileTempSize(webUserDto.getUserId(), fileId);
            updateUserSpace(webUserDto, totalSize);
            resultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileInfoService.transferFile(fileInfo.getFileId(), webUserDto);
                }
            });

            return resultDto;

        } catch (BusinessException e) {
            log.error("上传文件失败", e);
            uploadSuccess = false;
            throw e;
        } catch (Exception e) {
            log.error("上传文件失败", e);
            uploadSuccess = false;
        } finally {
            if (!uploadSuccess && tempFileFolder != null) {
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    log.error("删除临时目录失败", e);
                }
            }
        }
        return resultDto;


    }

    /**
     * 获取缩略图
     *
     * @param response
     * @param imageFolder
     * @param imageName
     */
    @Override
    public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        // 如果文件夹或者文件名为空，就不处理
        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) || !StringTools.pathIsOk(imageFolder) || !StringTools.pathIsOk(imageName)) {
            return;
        }
        String imageSuffix = StringTools.getFileSuffix(imageName);

        // 文件路径(file/image/文件夹/文件名)
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" + imageFolder + "/" + imageName;
        imageSuffix = imageSuffix.replace(".", ""); // 去掉后缀中的点
        String contentType = "image/" + imageSuffix; // 设置响应头
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "max-age=2592000");
        // 输出文件
        readFile(response, filePath);
    }


    @Override
    public void getFile(HttpServletResponse response, String fileId, String userId) {
        // 查询文件信息
        String filePath = null;

        if (fileId.endsWith(".ts")) {
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];

            FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(realFileId, userId);
            String fileName = fileInfo.getFilePath();
            fileName = StringTools.getFileNameNoSuffix(fileName) + "/" + fileId;
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" + fileName;
        } else {
            FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                return;
            }
            // 如果是视频文件，就读取.m3u8文件
            if (FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                // 获取文件路径（无后缀）
                String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
                filePath = appConfig.getProjectFolder() + "/" + Constants.FILE_FOLDER_FILE + "/" + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
            } else {
                filePath = appConfig.getProjectFolder() + "/" + Constants.FILE_FOLDER_FILE + "/" + fileInfo.getFilePath();
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
        }
        readFile(response, filePath);


    }

    @Override
    public FileInfo newFolder(String filePid, String userId, String folderName) {
        // 校验文件名
        checkFileName(filePid, userId, folderName, FileFolderTypeEnums.FOLDER.getType());

        // 封装文件信息
        Date curDate = new Date();
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
        fileInfo.setUserId(userId);
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(folderName);
        fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfo.setCreateTime(curDate);
        fileInfo.setLastUpdateTime(curDate);
        fileInfo.setStatus(FileStatusEnums.USING.getStatus());
        fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());

        // 操作数据库
        fileInfoMapper.insert(fileInfo);

        return fileInfo;
    }

    @Override
    public List<FileInfo> getFolderInfo(String path, String userId) {
        // 获取分割后的路径
        String[] pathArray = path.split("/");

        // 设置查询条件
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfoQuery.setFileIdArray(pathArray);

        //设置排序规则，order by field("fileId1", "fileId2" ....)
        String orderBy = "field(file_id,\"" + StringUtils.join(pathArray, "\",\"") + "\")";
        fileInfoQuery.setOrderBy(orderBy);

        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);


        return fileInfoList;
    }

    private void checkFileName(String filePid, String userId, String fileName, Integer folderType) {
        // 设置查询条件
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFolderType(folderType);
        fileInfoQuery.setFileName(fileName);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);

        // 返回查询结果（个数）
        Integer count = fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 0) {
            throw new BusinessException("此目录下已经存在同名文件，请修改名称");
        }

    }

    private String autoRename(String fileId, String UserId, String fileName) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(UserId);
        fileInfoQuery.setFilePid(fileId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfoQuery.setFileName(fileName);
        Integer count = fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 0) {
            fileName = StringTools.rename(fileName);
        }
        return fileName;
    }

    private void updateUserSpace(SessionWebUserDto webUserDto, Long useSpace) {
        Integer count = accountMapper.updateUserSpace(webUserDto.getUserId(), useSpace, null);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        UserSpaceDto userSpaceDto = redisComponent.getUserSpaceUse(webUserDto.getUserId());
        userSpaceDto.setUseSpace(userSpaceDto.getUseSpace() + useSpace);
        redisComponent.saveUserSpaceUse(webUserDto.getUserId(), userSpaceDto);
    }

    @Async
    public void transferFile(String fileId, SessionWebUserDto webUserDto) {
        Boolean transferSuccess = true; // 是否转换成功
        String targetFilePath = null; // 目标文件路径
        String cover = null; // 是否覆盖
        FileTypeEnums fileTypeEnum = null; // 文件类型
        // 根据文件id和用户id查询文件信息
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, webUserDto.getUserId());
        try {
            // 如果文件信息为空或者文件状态为转换中，就不转换
            if (fileInfo == null || !FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())) {
                return;
            }
            // 找到临时目录(cloud-backend/file/temp/用户id+文件id)
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_TEMP;
            String currentFolderName = webUserDto.getUserId() + fileId;
            File fileFolder = new File(tempFolderName + currentFolderName);
            String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
            String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());

            //目标目录
            String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFolder = new File(targetFolderName + "/" + month);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }

            // 真实的文件名
            String realFileName = currentFolderName + fileSuffix;
            targetFilePath = targetFolder.getPath() + "/" + realFileName;

            // 合并文件
            union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);

            // 视频文件切割
            // 获取文件后缀名
            fileTypeEnum = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
            // 如果是视频文件，就切割视频文件
            if (fileTypeEnum == FileTypeEnums.VIDEO) {
                cutFile4Video(fileId, targetFilePath);
                //生成视频缩略图
                cover = month + "/" + currentFolderName + Constants.IMAGE_PNG_SUFFIX; // 封面图路径
                String coverPath = targetFolderName + "/" + cover; // 封面图真实路径

                /**
                 * new File(targetFilePath), Constants.LENGTH_150, new File(coverPath)
                 * 视频文件路径，缩略图大小，缩略图路径
                 */
                ScaleFilter.createCover4Video(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath));
            } else if (fileTypeEnum == FileTypeEnums.IMAGE) {
                // 生成缩略图
                cover = month + "/" + realFileName.replace(".", "_."); // 封面图路径
                String coverPath = targetFolderName + "/" + cover; // 封面图真实路径
                Boolean created = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath), false);
                if (!created) {
                    FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }

        } catch (Exception e) {
            log.error("文件转码失败，文件ID：{}，userId：{}", fileId, webUserDto.getUserId(), e);
            transferSuccess = false;
        } finally {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setFileSize(new File(targetFilePath).length());
            updateInfo.setFileCover(cover);
            updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
            fileInfoMapper.updateFileStatusWithOldStatus(fileId, webUserDto.getUserId(), updateInfo, FileStatusEnums.TRANSFER.getStatus());
        }
    }

    /**
     * 合并文件
     *
     * @param dirPath    临时文件目录
     * @param toFilePath 目标文件路径
     * @param fileName   文件名
     * @param delSource  是否删除源文件
     */
    private void union(String dirPath, String toFilePath, String fileName, Boolean delSource) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }
        File[] files = dir.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile = null;
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < files.length; i++) {
                int len = -1;
                File chunkFile = new File(dirPath + "/" + i);
                RandomAccessFile readFile = null;
                try {
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败", e);
                    throw new BusinessException("合并分片失败");
                } finally {
                    readFile.close();
                }
            }
        } catch (Exception e) {
            log.error("合并文件:{}失败", fileName, e);
            throw new BusinessException("合并文件" + fileName + "出错了");
        } finally {
            if (writeFile != null) {
                try {
                    writeFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (delSource && dir.exists()) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void cutFile4Video(String fileId, String videoFilePath) {
        // 创建同名的目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        // 执行切割命令
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";

        // ts文件路径
        String tsPath = tsFolder.getPath() + "/" + Constants.TS_NAME;

        // 生成ts文件,CMD_TRANSFER_2TS是ffmpeg的命令,将视频文件转换成ts文件,videoFilePath是视频文件路径,tsPath是ts文件路径
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);

        // 用java代码执行cmd命令，cmd命令是将视频文件转换成ts文件
        ProcessUtils.executeCommand(cmd, false);
        //生成索引文件.m3u8和ts文件
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        // 删除ts文件
        new File(tsPath).delete();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileInfo rename(String fileId, String userId, String fileName) {
        // 查找文件信息
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }
        String filePid = fileInfo.getFilePid();
        // 校验文件名,避免重复
        checkFileName(filePid, userId, fileName, FileFolderTypeEnums.FILE.getType());
        // 如果是文件，获取后缀
        if (FileFolderTypeEnums.FILE.getType().equals(fileInfo.getFolderType())) {
            // 文件名 = 文件名
            fileName = fileName + StringTools.getFileSuffix(fileInfo.getFileName());
        }

        // 更新数据库
        Date curDate = new Date();
        FileInfo updateInfo = new FileInfo();
        updateInfo.setFileName(fileName);
        updateInfo.setLastUpdateTime(curDate);
        fileInfoMapper.updateByFileIdAndUserId(updateInfo, fileId, userId);

        // 更新缓存
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(curDate);
        return fileInfo;
    }


    @Override
    public List<FileInfo> loadAllFolder(String userId, String filePid, String currentFileIds) {
        // 封装查询条件
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFilePid(filePid);

        // 如果currentFileIds不为空，就将currentFileIds转换成数组
        if (!StringTools.isEmpty(currentFileIds)) {
            fileInfoQuery.setExcludeFileIdArray(currentFileIds.split(","));
        }
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfoQuery.setOrderBy("create_time desc");
        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
        return fileInfoList;
    }


    @Override
    public void changeFileFolder(String fileIds, String filePid, String userId) {
        // 如果移动到自己的目录
        if (fileIds.equals(filePid)) {
            throw new BusinessException("不能移动到自己的目录");
        }

        // 如果不是移动到根目录
        if (!Constants.ZERO_STR.equals(filePid)) {
            // 查询目标目录是否存在
            FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(filePid, userId);
            if (fileInfo == null) {
                throw new BusinessException("目标目录不存在");
            }
        }
        // 分割fileIds
        String[] array = fileIds.split(",");

        // 先查询要移动的目录下的所有文件，避免出现名字重复的情况
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFilePid(filePid);
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);

        // 将查询出的list集合收集为以fileName为key, 以集合元素fileInfo为值
        // (file1, file2) -> file2) 如果两个文件名字相同，取第二个
        Map<String, FileInfo> fileInfoMap = fileInfoList
                .stream()
                .collect(Collectors.toMap(FileInfo::getFileName,
                        Function.identity(), (file1, file2) -> file2));

        // 查询选中的文件
        fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFileIdArray(array);
        List<FileInfo> list = fileInfoService.findListByParam(fileInfoQuery);

        // 将所选文件重命名
        for (FileInfo item : list) {
            FileInfo rootFileInfo = fileInfoMap.get(item.getFileName());
            // 文件名已经存在，重命名被还原的文件名
            FileInfo updateInfo = new FileInfo();
            // 如果文件名已经存在，就重命名
            if (rootFileInfo != null) {
                String fileName = StringTools.rename(item.getFileName());
                updateInfo.setFileName(fileName);
            }
            // 更新文件的父id
            updateInfo.setFilePid(filePid);
            fileInfoMapper.updateByFileIdAndUserId(updateInfo, item.getFileId(), userId);
        }


    }

    @Override
    public String createDownloadUrl(String fileId, String userId) {
        FileInfo fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        // 如果文件不存在，就抛出异常
        if (fileInfo == null) {
            throw new BusinessException("文件不存在");
        }
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            throw new BusinessException("文件夹不能下载");
        }

        // 生成随机的下载码
        String code = StringTools.getRandomString(Constants.LENGTH_50);
        // 将下载码存入redis
        DownloadFileDto downloadFileDto = new DownloadFileDto();
        downloadFileDto.setDownloadCode(code);
        downloadFileDto.setFileName(fileInfo.getFileName());
        downloadFileDto.setFilePath(fileInfo.getFilePath());

        redisComponent.saveDownloadCode(downloadFileDto);
        return code;
    }

    @Override
    public void download(HttpServletResponse response, HttpServletRequest request, String code) throws UnsupportedEncodingException {
        DownloadFileDto downloadFileDto = redisComponent.getDownloadCode(code);
        if (downloadFileDto == null) {
            throw new BusinessException("下载码不存在");
        }
        // 获取文件路径
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + "/" + downloadFileDto.getFilePath();
        String fileName = downloadFileDto.getFileName();
        response.setContentType("application/x-msdownload;charset=UTF-8");
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } else {
            fileName = new String(fileName.getBytes("UTF-8"), "ISO8859-1");
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
        readFile(response, filePath);
    }

    @Override
    public void removeFile2RecycleBatch(String userId, String fileIds) {
        // 分割fileIds
        String[] fileIdArray = fileIds.split(",");
        // 封装查询条件
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFileIdArray(fileIdArray);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        // 查询文件信息
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
        if (fileInfoList.isEmpty()) {
            throw new BusinessException("所选文件不存在");
        }
        // 文件夹下的所有文件
        List<String> delPidList = new ArrayList<>();
        for (FileInfo fileInfo : fileInfoList) {
            findAllSubFolderFileList(delPidList, userId, fileInfo.getFileId(), FileDelFlagEnums.USING.getFlag());
        }
        if (!delPidList.isEmpty()) {
            FileInfo updateInfo = new FileInfo();
            updateInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());

            fileInfoMapper.updateDelFlagByBatch(updateInfo,userId,delPidList,null,FileDelFlagEnums.USING.getFlag());
        }
        // 将选中的文件更新为删除状态
        List<String> delFileIdList = Arrays.asList(fileIdArray);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setRecoveryTime(new Date());
        fileInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        fileInfoMapper.updateDelFlagByBatch(fileInfo,userId,null,delFileIdList,FileDelFlagEnums.USING.getFlag());

    }

    private void findAllSubFolderFileList(List<String> fileIdList, String userId, String fileId, Integer delFlag) {
        // 将文件id添加到集合中
        fileIdList.add(fileId);

        // 封装查询条件
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFilePid(fileId);
        query.setDelFlag(delFlag);
        query.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        List<FileInfo> fileInfoList = fileInfoService.findListByParam(query);

        for(FileInfo fileInfo:fileInfoList) {
            findAllSubFolderFileList(fileIdList, userId, fileInfo.getFileId(), delFlag);
        }
    }
}
