<template>
  <div>
    <!-- 侧边栏照片 -->
    <el-aside class="login-img">
      <el-image src="src/assets/login_img.png" />
    </el-aside>

    <!-- 登录面板 -->
    <el-main class="login-panel">
      <el-form>
        <!-- 标题 -->
        <el-header style="text-align: center">Happy云盘</el-header>

        <!-- 邮箱输入 -->
        <el-form-item :label-width="formLabelWidth">
          <el-input
            clearable
            prefix-icon="User"
            v-model="formData.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>

        <!-- 密码输入 -->
        <el-form-item :label-width="formLabelWidth" v-if="opType == 1">
          <el-input
            type="password"
            prefix-icon="Lock"
            v-model="formData.password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <!-- 注册 -->
        <div>
          <el-form-item>
            <div class="send-email-panel">
              <el-input
                v-model="formData.emailCode"
                prefix-icon="CircleCheck"
                placeholder="请输入邮箱验证码"
              >
              </el-input>
              <el-button class="send-mail-btn" type="primary"
                >获取验证码</el-button
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
          <el-form-item :label-width="formLabelWidth" v-if="opType == 0">
            <el-input
              clearable
              prefix-icon="User"
              v-model="formData.nickName"
              placeholder="请输入昵称"
            />
          </el-form-item>

          <!-- 第一次密码 -->
          <el-form-item :label-width="formLabelWidth">
            <el-input
              type="password"
              prefix-icon="Lock"
              v-model="formData.password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>

          <!-- 第二次密码 -->
          <el-form-item :label-width="formLabelWidth" v-if="opType == 2">
            <el-input
              type="password"
              prefix-icon="Lock"
              v-model="formData.password"
              placeholder="请再次输入密码"
              show-password
            />
          </el-form-item>
        </div>

        <!-- 验证码 -->
        <el-form-item>
          <div class="check-code-panle">
            <!-- 验证码输入框 -->
            <el-input
              prefix-icon="CircleCheck"
              v-model="formData.checkCode"
              placeholder="请输入验证码"
              @keyup.enter="doSubmit"
              style="width: 200px"
            >
            </el-input>

            <!-- 验证码图片 -->
            <img class="check-code" />
          </div>
        </el-form-item>

        <el-form-item v-if="opType == 1">
          <!-- 记住我 -->
          <el-checkbox v-model="formData.rememberMe">记住我</el-checkbox>

          <!-- 忘记密码&没有账号 -->
          <div class="no-account">
            <el-link :underline="false">忘记密码</el-link>
            <el-link :underline="false">没有账号</el-link>
          </div>
        </el-form-item>

        <!-- 注册(已有账号?) -->
        <el-form-item v-if="opType == 0">
          <el-link :underline="false">已有账号?</el-link>
        </el-form-item>

        <!-- 重置密码(去登录?) -->
        <el-form-item v-if="opType == 2">
          <el-link :underline="false">去登录？</el-link>
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button type="primary" style="width: 100%; height: 40px">
          <span v-if="opType == 0">注册</span>
          <span v-if="opType == 1">登录</span>
          <span v-if="opType == 2">重置密码</span>
        </el-button>
      </el-form>
    </el-main>
  </div>
</template>

<script>
export default {
  data() {
    return {
      formLabelWidth: "0px",
      formData: {},
    };
  },
};
</script>
<style lang="scss">
$defult-height: 40px;

/* 侧边栏照片 */
.login-img {
  width: auto;
  height: auto;
  margin-top: 10px;
  float: left;
}

/* 登录面板 */
.login-panel {
  /* 边框 */
  border: 1px solid #ccc;
  border-radius: 10px;
  width: 450px;
  margin: 0 auto;
  float: right;
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
</style>