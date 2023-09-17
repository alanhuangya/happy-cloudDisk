package com.alan.controller;

import com.alan.annotation.GlobalInterceptor;
import com.alan.annotation.VerifyParam;
import com.alan.controller.basecontroller.BaseController;
import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.dto.UploadResultDto;
import com.alan.entity.enums.FileCategoryEnums;
import com.alan.entity.enums.FileDelFlagEnums;
import com.alan.entity.po.FileInfo;
import com.alan.entity.query.FileInfoQuery;
import com.alan.entity.vo.PaginationResultVO;
import com.alan.entity.vo.ResponseVO;
import com.alan.service.FileInfoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 文件信息(FileInfo)表控制层
 *
 * @author makejava
 * @since 2023-09-06 11:38:43
 */
@RestController
@RequestMapping("file")
public class FileInfoController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private FileInfoService fileInfoService;


    /**
     * 分页查询
     *
     * @param session  会话
     * @param query    查询条件
     * @param category 文件类型
     * @return
     */
    @PostMapping("/loadDataList")
    @GlobalInterceptor
    public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category) {
        // 因为数据库中的文件类型是数字，所以通过枚举类来转换
        FileCategoryEnums categoryEnums = FileCategoryEnums.getByCode(category);

        // 如果枚举类不为空，就把枚举类的值赋给query
        if (categoryEnums != null) {
            query.setFileCategory(categoryEnums.getCategory());
        }

        // 封装查询条件
        query.setUserId(getUserInfoFromSession(session).getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfo.class));
    }

    @PostMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO uploadFile(HttpSession session,
                                 String fileId,
                                 MultipartFile file,
                                 @VerifyParam(required = true) String fileName,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileMd5,
                                 @VerifyParam(required = true) Integer chunkIndex,
                                 @VerifyParam(required = true) Integer chunks) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        UploadResultDto resultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return getSuccessResponseVO(resultDto);
    }

    /**
     * 获取缩略图
     *
     * @param response    响应，用于输出
     * @param imageFolder 文件夹
     * @param imageName   文件名
     */
    @GetMapping("/getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response,
                         @PathVariable("imageFolder") String imageFolder,
                         @PathVariable("imageName") String imageName) {
        fileInfoService.getImage(response, imageFolder, imageName);
    }

    /**
     * 获取视频信息
     *
     * @param response
     * @param session
     * @param fileId
     */
    @GetMapping("/ts/getVideoInfo/{fileId}")
    @GlobalInterceptor
    public void getVideoInfo(HttpServletResponse response,
                             HttpSession session,
                             @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.getFile(response, fileId, webUserDto.getUserId());
    }

    /**
     * 获取文件的信息
     *
     * @param response
     * @param session
     * @param fileId
     */
    @RequestMapping("/getFile/{fileId}")
    @GlobalInterceptor
    public void getFile(HttpServletResponse response,
                        HttpSession session,
                        @PathVariable("fileId") @VerifyParam(required = true) String fileId) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.getFile(response, fileId, webUserDto.getUserId());
    }

    /**
     * 新建文件夹
     *
     * @param session
     * @param filePid
     * @param fileName
     * @return
     */
    @RequestMapping("/newFoloder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO newFoloder(HttpSession session,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileName) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileInfo fileInfo = fileInfoService.newFolder(filePid, webUserDto.getUserId(), fileName);
        return getSuccessResponseVO(fileInfo);
    }

    /**
     * 获取文件夹信息
     *
     * @param session
     * @param path
     * @return
     */
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getFolderInfo(HttpSession session,
                                    @VerifyParam(required = true) String path) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        List<FileInfo> fileInfoList = fileInfoService.getFolderInfo(path, webUserDto.getUserId());
        return getSuccessResponseVO(fileInfoList);
    }

    @RequestMapping("/rename")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO rename(HttpSession session,
                             @VerifyParam(required = true) String fileId,
                             @VerifyParam(required = true) String fileName) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileInfo fileInfo = fileInfoService.rename(fileId, webUserDto.getUserId(), fileName);
        return getResponseVO("重命名文件成功", fileInfo);
    }

    @RequestMapping("/loadAllFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadAllFolder(HttpSession session,
                                    @VerifyParam(required = true) String filePid,
                                    String currentFileIds) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        List<FileInfo> fileInfoList = fileInfoService.loadAllFolder(webUserDto.getUserId(), filePid, currentFileIds);
        return getResponseVO("获取所有文件列表成功", fileInfoList);
    }

    @RequestMapping("/changeFileFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO changeFileFolder(HttpSession session,
                                       @VerifyParam(required = true) String fileIds,
                                       @VerifyParam(required = true) String filePid) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.changeFileFolder(fileIds, filePid, webUserDto.getUserId());
        return getResponseVO("移动文件成功", null);
    }

    /**
     * 创建下载链接
     *
     * @param session
     * @param fileId
     * @return
     */
    @RequestMapping("/createDownloadUrl/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO createDownloadUrl(HttpSession session,
                                        @VerifyParam(required = true)
                                        @PathVariable("fileId") String fileId) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String downloadUrl = fileInfoService.createDownloadUrl(fileId, webUserDto.getUserId());
        return getResponseVO("获取下载链接成功", downloadUrl);
    }

    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO download(HttpServletResponse response,
                               HttpServletRequest request,
                               @VerifyParam(required = true)
                               @PathVariable("code") String code) throws UnsupportedEncodingException {
        fileInfoService.download(response, request, code);
        return getResponseVO("下载成功", null);
    }

    @RequestMapping("/delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delFile(HttpSession session,
                              @VerifyParam(required = true) String fileIds) {
        // 获取用户信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(), fileIds);
        return getResponseVO("删除文件成功", null);
    }
}

