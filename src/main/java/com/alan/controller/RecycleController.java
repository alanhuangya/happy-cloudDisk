package com.alan.controller;

import com.alan.annotation.GlobalInterceptor;
import com.alan.controller.basecontroller.BaseController;
import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.enums.FileDelFlagEnums;
import com.alan.entity.query.FileInfoQuery;
import com.alan.entity.vo.FileInfoVO;
import com.alan.entity.vo.PaginationResultVO;
import com.alan.entity.vo.ResponseVO;
import com.alan.service.FileInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RequestMapping("/recycle")
@RestController
public class RecycleController extends BaseController {
    @Resource
    private FileInfoService fileInfoService;


    @PostMapping("/loadRecycleList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadRecycleList(HttpSession session,Integer pageNo, Integer pageSize) {
        // 获取当前用户
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);

        // 封装查询条件
        FileInfoQuery query = new FileInfoQuery();
        query.setPageSize(pageSize);
        query.setPageNo(pageNo);
        query.setUserId(webUserDto.getUserId());
        query.setOrderBy("recovery_time desc");
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(query);

        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }
}
