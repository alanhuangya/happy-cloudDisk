package com.alan.controller.basecontroller;

import com.alan.entity.constants.Constants;
import com.alan.entity.dto.SessionWebUserDto;
import com.alan.entity.enums.ResponseCodeEnum;
import com.alan.entity.vo.PaginationResultVO;
import com.alan.entity.vo.ResponseVO;
import com.alan.utils.CopyTools;
import com.alan.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BaseController {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    // 成功
    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    // 失败
    protected <T> ResponseVO getErrorResponseVO(String msg) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_ERROR);
        responseVO.setCode(ResponseCodeEnum.CODE_500.getCode());
        responseVO.setInfo(msg);
        return responseVO;
    }

    // 自定义返回
    protected <T> ResponseVO getResponseVO(String msg,T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(msg);
        responseVO.setData(t);
        return responseVO;
    }

    // 读取文件
    protected void readFile(HttpServletResponse response, String filePath) {
        // 判断文件路径是否合法
        if(!StringTools.pathIsOk(filePath)){
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            // 判断文件是否存在
            if (!file.exists()) {
                return;
            }

            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        }catch (Exception e) {
            logger.error("读取文件失败，文件路径：{}", filePath, e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
        }
    }

    /**
     * 从session中获取用户信息
     * @param session
     * @return
     */
    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        return sessionWebUserDto;
    }

    /**
     * 将源分页对象转换为目标分页对象
     * @param result 源分页对象
     * @param classz 目标分页对象的泛型
     * @return 目标分页对象
     * @param <S> 源分页对象的泛型
     * @param <T> 目标分页对象的泛型
     */
    protected <S,T>PaginationResultVO<T> convert2PaginationVO(PaginationResultVO<S> result, Class<T> classz) {
        // 创建一个分页结果对象
        PaginationResultVO<T> resultVO = new PaginationResultVO<>();

        // 将源list转换为目标list，因为源list和目标list的泛型不一样，所以需要手动转换
        resultVO.setList(CopyTools.copyList(result.getList(), classz));

        // 将源list的其他属性赋值给目标list
        resultVO.setPageNo(result.getPageNo());
        resultVO.setPageSize(result.getPageSize());
        resultVO.setPageTotal(result.getPageTotal());
        resultVO.setTotalCount(result.getTotalCount());

        return resultVO;
    }






}
