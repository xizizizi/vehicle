<template>
  <div class="login-container">
    <!-- 背景图片（无模糊） -->
    <div class="background-image"></div>

    <!-- 登录表单 -->
    <el-form
      ref="loginForm"
      :model="user"
      :rules="loginRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">系统登录</h3>
        <p class="subtitle">欢迎使用，请登录您的账户</p>
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="username"
          v-model="user.username"
          placeholder="请输入用户名"
          name="username"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="password"
          v-model="user.password"
          :type="passwordType"
          placeholder="请输入密码"
          name="password"
          tabindex="2"
          auto-complete="on"
          @keyup.enter.native="handleLogin"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon
            :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'"
          />
        </span>
      </el-form-item>

      <!-- 验证码部分 -->
      <el-form-item prop="captchaCode">
        <div class="captcha-wrapper">
          <span class="svg-container">
            <svg-icon icon-class="validCode" />
          </span>
          <el-input
            ref="captchaCode"
            v-model="user.captchaCode"
            placeholder="请输入验证码"
            name="captchaCode"
            type="text"
            tabindex="3"
            class="captcha-input"
            @keyup.enter.native="handleLogin"
          />
          <canvas
            ref="captchaCanvas"
            width="120"
            height="40"
            class="captcha-canvas"
            @click="refreshCaptcha"
            title="点击刷新验证码"
          ></canvas>
        </div>
      </el-form-item>

      <el-button
        :loading="loading"
        type="primary"
        style="width: 100%; margin-bottom: 30px"
        @click.native.prevent="handleLogin"
        class="login-btn"
      >
        登录
      </el-button>

      <div class="captcha-tips">看不清？点击验证码图片可刷新</div>

      <!-- 底部版权信息 -->
      <div class="footer">
        <p>© 2026 无人机调度管理系统. All rights reserved.</p>
        <p class="version">Version 1.0.0</p>
      </div>
    </el-form>
  </div>
</template>
  
  <script>
import { validUsername } from "@/utils/validate";

export default {
  name: "Login",
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error("请输入用户名"));
      } else {
        callback();
      }
    };
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error("密码不能少于 6 位"));
      } else {
        callback();
      }
    };
    const validateCaptcha = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入验证码"));
      } else if (value.length < 4) {
        callback(new Error("验证码长度不能少于4位"));
      } else if (
        this.captchaText &&
        value.toUpperCase() !== this.captchaText.toUpperCase()
      ) {
        callback(new Error("验证码错误"));
      } else {
        callback();
      }
    };

    return {
      user: {
        username: "",
        password: "",
        captchaCode: "",
      },
      captchaText: "",
      loginRules: {
        username: [
          { required: true, trigger: "blur", validator: validateUsername },
        ],
        password: [
          { required: true, trigger: "blur", validator: validatePassword },
        ],
        captchaCode: [
          { required: true, trigger: "blur", validator: validateCaptcha },
        ],
      },
      loading: false,
      passwordType: "password",
      redirect: undefined,
    };
  },
  mounted() {
    this.refreshCaptcha();
    window.addEventListener("resize", this.debouncedResize);
    this.$nextTick(() => {
      this.$refs.username.focus();
    });
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.debouncedResize);
  },
  created() {
    this.debouncedResize = this.debounce(this.handleResize, 200);
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true,
    },
  },
  methods: {
    // 防抖函数
    debounce(fn, delay) {
      let timer;
      return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
      };
    },

    // 窗口大小变化处理
    handleResize() {
      this.resizeCanvasAndDraw();
    },

    // 调整画布物理尺寸并重绘
    resizeCanvasAndDraw() {
      const canvas = this.$refs.captchaCanvas;
      if (!canvas) return;
      const rect = canvas.getBoundingClientRect();
      const dpr = window.devicePixelRatio || 1;
      // 设置画布物理像素尺寸 = CSS尺寸 * 设备像素比
      canvas.width = rect.width * dpr;
      canvas.height = rect.height * dpr;
      this.drawCaptcha();
    },

    showPwd() {
      if (this.passwordType === "password") {
        this.passwordType = "";
      } else {
        this.passwordType = "password";
      }
      this.$nextTick(() => {
        this.$refs.password.focus();
      });
    },

    generateCaptchaText(length = 4) {
      const chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
      let result = "";
      for (let i = 0; i < length; i++) {
        result += chars.charAt(Math.floor(Math.random() * chars.length));
      }
      this.captchaText = result;
      return result;
    },

    // 绘制验证码（基于当前画布宽高）
    drawCaptcha() {
      const canvas = this.$refs.captchaCanvas;
      if (!canvas) return;

      const ctx = canvas.getContext("2d");
      const width = canvas.width;
      const height = canvas.height;

      ctx.clearRect(0, 0, width, height);

      // 渐变背景
      const gradient = ctx.createLinearGradient(0, 0, width, height);
      gradient.addColorStop(0, this.getRandomColor(200, 240));
      gradient.addColorStop(1, this.getRandomColor(180, 220));
      ctx.fillStyle = gradient;
      ctx.fillRect(0, 0, width, height);

      // 干扰线
      for (let i = 0; i < 5; i++) {
        ctx.beginPath();
        ctx.moveTo(Math.random() * width, Math.random() * height);
        ctx.lineTo(Math.random() * width, Math.random() * height);
        ctx.strokeStyle = this.getRandomColor(80, 150);
        ctx.lineWidth = 0.8;
        ctx.stroke();
      }

      // 干扰点
      for (let i = 0; i < 30; i++) {
        ctx.beginPath();
        ctx.arc(
          Math.random() * width,
          Math.random() * height,
          Math.random() * 2,
          0,
          2 * Math.PI
        );
        ctx.fillStyle = this.getRandomColor(0, 200);
        ctx.fill();
      }

      // 绘制文字（字体大小基于画布高度）
      const fontSize = height * 0.6;
      ctx.font = `bold ${fontSize}px Arial`;
      ctx.textBaseline = "middle";

      for (let i = 0; i < this.captchaText.length; i++) {
        const char = this.captchaText.charAt(i);
        const x = (width / (this.captchaText.length + 1)) * (i + 1);
        const y = height / 2 + (Math.random() - 0.5) * 8;

        const rotation = (Math.random() - 0.5) * 0.5;
        ctx.save();
        ctx.translate(x, y);
        ctx.rotate(rotation);

        ctx.fillStyle = "#333";
        ctx.fillText(char, 0, 0);

        ctx.restore();
      }

      // 边框
      ctx.strokeStyle = this.getRandomColor(100, 180);
      ctx.lineWidth = 1;
      ctx.strokeRect(0, 0, width, height);
    },

    getRandomColor(min, max) {
      const r = Math.floor(Math.random() * (max - min) + min);
      const g = Math.floor(Math.random() * (max - min) + min);
      const b = Math.floor(Math.random() * (max - min) + min);
      return `rgb(${r}, ${g}, ${b})`;
    },

    refreshCaptcha() {
      this.generateCaptchaText();
      this.resizeCanvasAndDraw(); // 刷新时重新适配尺寸
      this.user.captchaCode = "";
      if (this.$refs.loginForm) {
        this.$refs.loginForm.clearValidate(["captchaCode"]);
      }
    },

    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          if (
            !this.captchaText ||
            this.user.captchaCode.toUpperCase() !==
              this.captchaText.toUpperCase()
          ) {
            this.$message.error("验证码错误");
            this.refreshCaptcha();
            return false;
          }

          this.loading = true;
          this.$store
            .dispatch("user/login", this.user)
            .then(() => {
              this.$message.success("登录成功");
              this.$router.push({ path: this.redirect || "/" });
              this.loading = false;
            })
            .catch((error) => {
              console.error("登录失败:", error);
              this.refreshCaptcha();
              this.loading = false;
              this.$message.error(
                error.message || "登录失败，请检查用户名和密码"
              );
            });
        } else {
          console.log("表单验证失败");
          return false;
        }
      });
    },
  },
};
</script>
  
  <style lang="scss">
/* 全局样式 - 修改为响应式单位，输入框黑色字体和透明背景 */
.login-container {
  .el-input {
    display: inline-block;
    height: 100%; /* 高度继承自父元素 */
    width: 85%;

    input {
      background: transparent;
      border: 0px;
      border-radius: 0px;
      padding: 12px 5px 12px 15px;
      color: #333;
      height: 100%;
      caret-color: #333;

      &:-webkit-autofill {
        box-shadow: 0 0 0px 1000px rgba(255, 255, 255, 0.3) inset !important;
        -webkit-text-fill-color: #333 !important;
        transition: background-color 5000s ease-in-out 0s;
      }
    }
  }

  .el-form-item {
    border: 1px solid rgba(0, 0, 0, 0.3);
    background: rgba(255, 255, 255, 0.3);
    border-radius: 8px;
    color: #333;
    height: clamp(40px, 5vw, 60px); /* 输入框整体高度随窗口缩放 */

    &.is-error {
      border-color: #f56c6c;
      background: rgba(245, 108, 108, 0.2);
    }
  }

  .captcha-input {
    width: 65% !important;
  }

  .el-button {
    border-radius: 8px;
    height: clamp(40px, 5vw, 50px);
    font-size: clamp(14px, 1.8vw, 18px);
  }

  .el-input__inner::placeholder {
    color: rgba(51, 51, 51, 0.7);
  }

  .el-form-item__error {
    color: #f56c6c;
    font-size: 12px;
  }
}
</style>
  
  <style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  position: relative;
  padding-right: 10%;

  .background-image {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("/backgrounds/bjt1.png");
    background-size: 100% 100%;
    background-position: center;
    background-repeat: no-repeat;
    background-attachment: fixed;
    z-index: 1;
  }

  /* 登录表单 - 使用 vw + clamp 实现等比缩放 */
  .login-form {
    position: relative;
    width: clamp(320px, 35vw, 500px);
    max-width: 90%;
    padding: clamp(20px, 3vw, 40px) clamp(15px, 2.5vw, 35px);
    background: rgba(255, 255, 255, 0.6);
    border-radius: 15px;
    box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
    z-index: 2;
    border: 1px solid rgba(255, 255, 255, 0.5);

    @media (max-height: 700px) {
      padding: 30px 25px; /* 针对高度较小的屏幕微调 */
    }
  }

  .title-container {
    text-align: center;
    margin-bottom: clamp(20px, 4vw, 40px);

    .title {
      font-size: clamp(22px, 2.8vw, 32px);
      color: #333;
      margin: 0 0 10px 0;
      font-weight: 600;
      letter-spacing: 1px;
    }

    .subtitle {
      font-size: clamp(12px, 1.5vw, 16px);
      color: #555;
      margin: 0;
    }
  }

  .captcha-wrapper {
    display: flex;
    align-items: center;

    .svg-container {
      color: #555;
      padding: 0 clamp(8px, 1vw, 15px);
    }

    .captcha-canvas {
      margin-left: 10px;
      cursor: pointer;
      border-radius: 6px;
      border: 1px solid rgba(0, 0, 0, 0.3);
      background-color: rgba(255, 255, 255, 0.4);
      transition: all 0.3s;
      width: clamp(80px, 10vw, 150px);
      height: clamp(30px, 3.5vw, 50px);
      object-fit: contain; /* 防止画布内容变形 */

      &:hover {
        border-color: #409eff;
        box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
      }

      &:active {
        transform: scale(0.98);
      }
    }
  }

  .captcha-tips {
    text-align: center;
    color: #666;
    font-size: clamp(10px, 1.2vw, 13px);
    margin-top: -15px;
    margin-bottom: 20px;
  }

  .login-btn {
    background: rgba(64, 158, 255, 0.9);
    border: none;
    color: white;
    transition: all 0.3s;
    height: clamp(40px, 5vw, 50px);
    font-size: clamp(14px, 1.8vw, 18px);

    &:hover {
      background: rgba(64, 158, 255, 1);
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(64, 158, 255, 0.4);
    }

    &:active {
      transform: translateY(0);
    }

    &.is-loading {
      opacity: 0.8;
    }
  }

  .svg-container {
    padding: 0 12px;
    color: #555;
    width: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .show-pwd {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    font-size: clamp(16px, 1.8vw, 22px);
    color: #555;
    cursor: pointer;
    user-select: none;
    transition: color 0.3s;

    &:hover {
      color: #409eff;
    }
  }

  .footer {
    text-align: center;
    margin-top: clamp(15px, 3vw, 30px);
    padding-top: clamp(10px, 2vw, 20px);
    border-top: 1px solid rgba(0, 0, 0, 0.1);

    p {
      margin: 5px 0;
      color: #666;
      font-size: clamp(10px, 1.2vw, 14px);
    }

    .version {
      font-size: clamp(9px, 1vw, 12px);
      opacity: 1;
    }
  }
}

/* 移动端微调 - 验证码换行 */
@media (max-width: 768px) {
  .login-container {
    justify-content: center;
    padding-right: 0;

    .login-form {
      width: 90%;
    }

    .captcha-wrapper {
      flex-wrap: wrap;

      .captcha-input {
        width: 100% !important;
        margin-bottom: 10px;
      }

      .captcha-canvas {
        margin-left: 0;
        width: 100%;
        max-width: 150px;
        margin: 0 auto;
      }
    }
  }
}
</style>