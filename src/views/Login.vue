<template>
  <div class="main">
    <!-- 侧边栏照片 -->
    <el-aside class="login-img">
      <el-image src="src/assets/login_img.png"/>
    </el-aside>

    <!-- 登录面板 -->
    <div class="login-panel">
      <el-form :model="formData" :rules="rules" ref="formDataRef">
        <!-- 标题 -->
        <el-header style="text-align: center">Happy云盘</el-header>

        <!-- 邮箱输入 -->
        <el-form-item prop="email" :label-width="formLabelWidth">
          <el-input
              clearable
              prefix-icon="User"
              v-model="formData.email"
              placeholder="请输入邮箱"
          />
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item prop="password" :label-width="formLabelWidth" v-if="opType == 1">
          <el-input
              type="password"
              prefix-icon="Lock"
              v-model="formData.password"
              placeholder="请输入密码"
              show-password
          />
        </el-form-item>

        <el-dialog v-model="dialogFormVisible" title="发送邮箱验证码" width="500px">
          <el-form :rules="rules" :model="emailCodeModel" ref="emailCodeModelRef">
            <el-form-item label="邮箱">
              {{ formData.email }}
            </el-form-item>
            <el-form-item label="验证码" prop="checkCode">
              <div class="check-code-panel">
                <!-- 验证码输入框 -->
                <el-input
                    prefix-icon="CircleCheck"
                    v-model="emailCodeModel.checkCode"
                    placeholder="请输入验证码"
                >
                </el-input>

                <!-- 验证码图片 -->
                <img
                    class="check-code"
                    :src="checkCodeUrl4SendMail"
                    @click="changeCheckCode(1)"
                />
              </div>
            </el-form-item>
          </el-form>
          <template #footer>
              <span class="dialog-footer">
                <el-button @click="dialogFormVisible = false">取消</el-button>
                <el-button type="primary" @click="getEmailCode">
                  发送
                </el-button>
              </span>
          </template>
        </el-dialog>
        <!-- 注册 -->
        <div v-if="opType == 0 || opType == 2">
          <el-form-item prop="emailCode">
            <div class="send-email-panel">
              <el-input
                  v-model="formData.emailCode"
                  prefix-icon="CircleCheck"
                  placeholder="请输入邮箱验证码"
              >
              </el-input>
              <el-button class="send-mail-btn" type="primary" @click="sendEmailCode"
              >获取验证码
              </el-button
              >
            </div>


            <!-- 未收到验证码 -->
            <el-popover placement="left" width="500px" trigger="click">
              <div>
                <p>1. 在垃圾箱中查找邮箱验证码</p>
                <p>2. 在邮箱中头像->设置->反垃圾->白名单->设置邮件地址白名单</p>
                <p>
                  3. 将邮箱【laoluo@wuhancoder.com】添加到白名单不知道怎么设置？
                </p>
              </div>
              <template #reference>
                <el-link :underline="false">未收到邮箱验证码？</el-link>
              </template>
            </el-popover>
          </el-form-item>

          <!-- 昵称 -->
          <el-form-item prop="nickName" :label-width="formLabelWidth" v-if="opType == 0">
            <el-input
                clearable
                prefix-icon="User"
                v-model="formData.nickName"
                placeholder="请输入昵称"
            />
          </el-form-item>

          <!-- 第一次密码 -->
          <el-form-item prop="registerPassword" :label-width="formLabelWidth">
            <el-input
                type="password"
                prefix-icon="Lock"
                v-model="formData.registerPassword"
                placeholder="请输入密码"
                show-password
            />
          </el-form-item>

          <!-- 第二次密码 -->
          <el-form-item
              prop="registerPasswordAgain"
              :label-width="formLabelWidth"
              v-if="opType == 2 || opType == 0"
          >
            <el-input
                type="password"
                prefix-icon="Lock"
                v-model="formData.registerPasswordAgain"
                placeholder="请再次输入密码"
                show-password
            />
          </el-form-item>
        </div>

        <!-- 验证码 -->
        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <!-- 验证码输入框 -->
            <el-input
                prefix-icon="CircleCheck"
                v-model="formData.checkCode"
                placeholder="请输入验证码"
                @keyup.enter="doSubmit"
            >
            </el-input>

            <!-- 验证码图片 -->
            <img
                class="check-code"
                :src="checkCodeUrl"
                @click="changeCheckCode(0)"
            />
          </div>
        </el-form-item>

        <el-form-item v-if="opType == 1">
          <!-- 记住我 -->
          <el-checkbox v-model="formData.rememberMe">记住我</el-checkbox>

          <!-- 忘记密码&没有账号 -->
          <div class="no-account">
            <el-link :underline="false" @click="showPanel(2)"
            >忘记密码?
            </el-link
            >
            <el-link :underline="false" @click="showPanel(0)"
            >没有账号?
            </el-link
            >
          </div>
        </el-form-item>

        <!-- 注册(已有账号?) -->
        <el-form-item v-if="opType == 0">
          <el-link :underline="false" @click="showPanel(1)">已有账号?</el-link>
        </el-form-item>

        <!-- 重置密码(去登录?) -->
        <el-form-item v-if="opType == 2">
          <el-link :underline="false" @click="showPanel(1)">去登录？</el-link>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button type="primary" style="width: 100%; height: 40px" @click="doSubmit">
          <span v-if="opType == 0">注册</span>
          <span v-if="opType == 1">登录</span>
          <span v-if="opType == 2">重置密码</span>
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, getCurrentInstance, nextTick, onMounted} from "vue";
import {useRouter, useRoute} from "vue-router";
import md5 from "js-md5";

const router = useRouter();
const route = useRoute();
const formLabelWidth = "0px";
const formData = ref({});
const formDataRef = ref();
const {proxy} = getCurrentInstance();

onMounted(() => {
  showPanel(1);
});

// 0:注册 1:登录 2:重置密码
const opType = ref(1);

const api = {
  checkCode: "/api/checkCode",
  sendEmailCode: "/sendEmailCode",
  login: "/login",
  register: "/register",
  resetPwd: "/resetPwd",
};

const checkCodeUrl = ref(api.checkCode);
const checkCodeUrl4SendMail = ref(api.checkCode);

const checkRegisterPassword = (rule, value, callback) => {
  if (value !== formData.value.registerPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};

const dialogFormVisible = ref(false);
const emailCodeModel = ref({});
const emailCodeModelRef = ref();

const getEmailCode = () => {
  emailCodeModelRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    console.log(opType.value);
    const params = {
      email: emailCodeModel.value.email,
      checkCode: emailCodeModel.value.checkCode,
      type: opType.value === 0 ? 0 : 1,
    };
    console.log(params)
    let request = await proxy.Request({
      url: api.sendEmailCode,
      params: params,
      errorCallback: () => {
        changeCheckCode(1);
      }
    });
    if (!request) {
      return;
    }
    proxy.Message.success("验证码已发送，请登录邮箱查看");
    dialogFormVisible.value = false;
  });
}


const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    //assign() 方法用于将所有可枚举属性的值从一个或多个源对象复制到目标对象。它将返回目标对象。
    Object.assign(params, formData.value);

    // 注册&找回密码
    if (opType.value === 0 || opType.value === 2) {
      // 注册，将注册密码赋值给密码，删除注册密码
      params.password = params.registerPassword;
      delete params.registerPassword;
      delete params.registerPasswordAgain;
    }
    // 登录
    if (opType.value === 1) {
      // 如果密码不是cookie中的密码，就md5加密
      let cookieLoginInfo = proxy.VueCookies.get("loginInfo");
      let cookiePassword =
          cookieLoginInfo == null ? null : cookieLoginInfo.password;
      if (params.password !== cookiePassword) {
        params.password = md5(params.password);
      }
    }
    let url = api.null;
    if (opType.value === 0) {
      url = api.register;
    } else if (opType.value === 1) {
      url = api.login;
    } else if (opType.value === 2) {
      url = api.resetPwd;
    }
    let result = await proxy.Request({
      url: url,
      params: params,
      errorCallback: () => {
        changeCheckCode(0);
      }
    });
    if (!result) {
      return;
    }
    if (opType.value == 0) {
      proxy.Message.success("注册成功,请登录");
      showPanel(1);
    } else if (opType.value == 1) {
      if (params.rememberMe) {
        // 记住密码
        const loginInfo = {
          email: params.email,
          password: params.password,
          rememberMe: params.rememberMe,
        };
        // 保存到cookie,7天
        proxy.VueCookies.set("loginInfo", loginInfo, "7d");
      } else {
        proxy.VueCookies.remove("loginInfo");
      }
      proxy.Message.success("登录成功");
      // 存储cookies
      proxy.VueCookies.set("userInfo", result.data, 0);
      // 跳转到首页
      router.push("/");
    } else if (opType.value == 2) {
      proxy.Message.success("重置密码成功,请登录");
      showPanel(1);
    }

  })
}

const sendEmailCode = () => {
  formDataRef.value.validateField("email", (valid) => {
    if (!valid) {
      return;
    }
    dialogFormVisible.value = true;
    nextTick(() => {
      changeCheckCode(1);
      emailCodeModelRef.value.resetFields();
      emailCodeModel.value = {
        email: formData.value.email,
      };
    })
  });
};


//输入框的规则
const rules = reactive({
  email: [
    {required: true, message: "请输入邮箱", trigger: "blur"},
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],

  password: [
    {required: true, message: "请输入密码", trigger: "blur"},
    {min: 6, max: 20, message: "密码长度在 6 到 20 个字符", trigger: "blur"},
  ],

  registerPassword: [
    {required: true, message: "请输入密码", trigger: "blur"},
    {
      validator: proxy.Verify.password,
      message: "密码只能是数字，字母，特殊字符 8-18位",
    },
  ],

  //判断两次密码是否一致
  registerPasswordAgain: [
    {required: true, message: "请再次输入密码", trigger: "blur"},
    {
      validator: checkRegisterPassword,
      message: "两次输入的密码不一致",
      trigger: ["blur", "change"],
    }
  ],

  checkCode: [
    {required: true, message: "请输入验证码", trigger: "blur"},
    {min: 5, max: 5, message: "验证码长度为5个字符", trigger: "blur"},
  ],
  emailCode: [
    {required: true, message: "请输入邮箱验证码", trigger: "blur"},
    {min: 5, max: 5, message: "邮箱验证码长度为5个字符", trigger: "blur"},
  ],
  nickName: [
    {required: true, message: "请输入昵称", trigger: "blur"},
    {min: 2, max: 20, message: "昵称长度在 2 到 20 个字符", trigger: "blur"},
  ],
});


const changeCheckCode = (type) => {
  //注册
  if (type == 0) {
    checkCodeUrl.value =
        api.checkCode + "?type=" + type + "&time=" + new Date().getTime();
  } else {
    checkCodeUrl4SendMail.value =
        api.checkCode + "?type=" + type + "&time=" + new Date().getTime();
  }
};

const showPanel = (type) => {
  opType.value = type;
  resetForm();
};

/**
 * 重置表单
 */
const resetForm = () => {
  // 更新验证码
  changeCheckCode(0);
  formDataRef.value.resetFields();
  formData.value = {};

  // 登录
  if (opType.value == 1) {
    const cookieLoginInfo = proxy.VueCookies.get("loginInfo");
    if (cookieLoginInfo) {
      // md5解密，因为cookie中的密码是加密的
      // cookieLoginInfo.password = md5(cookieLoginInfo.password);
      //json格式打印
      console.log(JSON.stringify(cookieLoginInfo));
      formData.value = cookieLoginInfo;
    }
  }
}

</script>
<style lang="scss">
$defult-height: 40px;
$bg-img-url: "../assets/login_img.png";

.main {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}


/* 侧边栏照片 */
.login-img {
  //调整图片为80%
  width: 60%;

}

/* 登录面板 */
.login-panel {
  /* 边框 */
  border: 1px solid #ccc;
  border-radius: 10px;
  width: 450px;
  float: right;
  padding: 20px;
  margin-right: 100px;
}

.el-input {
  height: $defult-height;
}

.el-link {
  color: #4e9efd;
}

.no-account {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

// 注册邮箱验证码
.send-email-panel {
  display: flex;
  width: 100%;
  justify-content: space-between;

  .send-mail-btn {
    margin-left: 10px;
    height: $defult-height;
  }
}

// 验证码面板
.check-code-panel {
  display: flex;
  width: 100%;
  justify-content: space-between;

  .check-code {
    margin-left: 10px;
  }
}
</style>