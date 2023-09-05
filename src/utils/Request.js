import axios from 'axios';
import {ElLoading} from "element-plus";
import Message from "@/utils/Message";
import router from "@/router";


const responseTypeJson = 'json';
const contentTypeForm = 'application/x-www-form-urlencoded;charset=UTF-8';
const contentTypeJson = 'application/json';
let loading = null;

// 创建axios实例
const instance = axios.create({
    baseURL: '/api',
    timeout: 30 * 1000,
})

// 请求前拦截器，用于处理需要在请求前的操作
instance.interceptors.request.use(
    // 请求前拦截,config为请求配置对象
    (config) => {
        // 如果有showLoading属性则执行
        if (config.showLoading) {
            loading = ElLoading.service({
                //锁定，不可点击
                lock: true,
                text: '加载中......',
                background: 'rgba(0, 0, 0, 0.7)',
            })
        }
        return config;
    },
    // 请求错误处理
    (error) => {
        // 请求失败关闭loading
        if (config.showLoading && loading) {
            loading.close();
        }
        Message.error("请求发送失败");

        return Promise.reject("请求发送失败");
    }
);

// 请求后拦截器，用于处理需要在请求前的操作
instance.interceptors.response.use(
    (response) => {
        // 请求后关闭loading
        const {showLoading, errorCallback, showError = true, responseType} = response.config;
        if (showLoading && loading) {
            loading.close();
        }
        // 获取接口返回结果
        const responseData = response.data;
        // 如果是文件流，则直接返回
        if (responseType == "arraybuffer" || responseType == "blob") {
            return responseData;
        }
        // 正常请求
        // 如果接口请求成功，则直接返回数据
        if (responseData.code == 200) {
            return responseData;
        } else if (responseData.code == 901) {
            // 登录超时
            router.push("login?redirectUrl=" + encodeURI(router.currentRoute.value.path));
            return Promise.reject({showError: false, msg: "登录超时"});
        } else {
            // 其他错误
            if (errorCallback) {
                // 如果有错误回调，则执行
                errorCallback(responseData.info);
            }
            // 如果需要显示错误信息，则显示
            return Promise.reject({showError: showError, msg: responseData.info});
        }
    },
    // 请求错误处理
    (error) => {
        // 请求失败关闭loading
        if (error.config.showLoading && loading) {
            loading.close();
        }
        return Promise.reject({showError: true, msg: "网络异常"});
    }
);

const request = (config) => {
    // config为请求配置对象
    const {url, params, dataType, showLoading = true, responseType = responseTypeJson} = config;
    let contentType = contentTypeForm;
    //创建form对象
    let formData = new FormData();
    //遍历parms
    for (let key in params) {
        //如果key存在,则添加到formData中,否则添加空字符串
        formData.append(key, params[key] == undefined ? "" : params[key]);
    }
    if (dataType != null && dataType == 'json') {
        //如果dataType为json,则设置contentType为json
        contentType = contentTypeJson;
    }
    let headers = {
        // 设置请求头
        'Content-Type': contentType,
        'X-Requested-With': 'XMLHttpRequest',
    };
    // 返回axios实例
    return instance.post(url, formData, {
        onUploadProgress: (event) => {
            // 上传进度
            if (config.onUploadProgress) {
                config.onUploadProgress(event);
            }
        },
        responseType: responseType,
        headers: headers,
        showLoading: showLoading,
        errorCallback: config.errorCallback,
        showError: config.showError,
    }).catch((error) => {
        if (error.showError) {
            Message.error(error.msg);
        }
        return null;
    });
};

export default request;