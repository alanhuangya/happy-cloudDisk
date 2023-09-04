import {ElMessage} from "element-plus";

/**
 * 消息提示
 * @param msg 消息内容
 * @param callback 回调函数
 * @param type 消息类型
 */
const showMessage = (msg, callback, type) => {
    ElMessage({
        type: type,
        message: msg,
        duration: 2000,
        onClose: () => {
            if (callback) {
                callback();
            }
        }
    })
}

const message = {
    // 错误消息提示
    error: (msg, callback) => {
        showMessage(msg, callback, "error");
    },
    // 成功消息提示
    success: (msg, callback) => {
        showMessage(msg, callback, "success");
    },
    // 警告消息提示
    warning: (msg, callback) => {
        showMessage(msg, callback, "warning");
    },
}

// 导出
export default message;