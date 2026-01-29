<template>
  <div class="drone-coze-iframe-container">
    <!-- 标题与控制栏 -->
    <div class="iframe-header">
      <div class="header-left">
        <span class="icon">🚁</span>
        <h3>无人机调度AI助手（小鹰）</h3>
        <el-tag size="mini" type="success">在线</el-tag>
      </div>
      <div class="header-actions">
        <el-button
          size="small"
          icon="el-icon-refresh"
          @click="reloadIframe"
          :loading="reloading"
        >
          刷新助手
        </el-button>
        <el-button
          size="small"
          icon="el-icon-full-screen"
          @click="toggleFullscreen"
          v-if="!isFullscreen"
        >
          全屏
        </el-button>
        <el-button
          size="small"
          icon="el-icon-close"
          @click="exitFullscreen"
          v-else
        >
          退出全屏
        </el-button>
      </div>
    </div>

    <!-- 快捷提问按钮 -->
    <div class="quick-questions">
      <div class="quick-questions-label">快捷提问：</div>
      <el-button-group>
        <el-button
          v-for="(q, index) in quickQuestions"
          :key="index"
          size="small"
          @click="sendQuickQuestion(q.text)"
        >
          {{ q.label }}
        </el-button>
      </el-button-group>
    </div>

    <!-- Iframe 主体 -->
    <div
      class="iframe-wrapper"
      :class="{ fullscreen: isFullscreen }"
      ref="iframeWrapper"
    >
      <!-- 加载状态 -->
      <div v-if="loading" class="iframe-loading">
        <el-icon class="loading-icon"><el-icon-loading /></el-icon>
        <p>正在加载无人机调度助手...</p>
      </div>

      <!-- Coze 智能体 Iframe -->
      <iframe
        ref="cozeIframe"
        :src="iframeSrc"
        :title="iframeTitle"
        frameborder="0"
        allow="clipboard-write;"
        @load="onIframeLoad"
        @error="onIframeError"
      ></iframe>
    </div>

    <!-- 底部信息 -->
    <div class="iframe-footer">
      <div class="footer-info">
        <el-tooltip content="基于扣子平台构建" placement="top">
          <span class="powered-by"
            >Powered by
            <el-link
              href="https://www.coze.cn"
              target="_blank"
              type="primary"
              :underline="false"
            >
              Coze
            </el-link>
          </span>
        </el-tooltip>
        <span class="divider">|</span>
        <span>智能体ID: {{ botId }}</span>
      </div>
      <el-button type="text" size="mini" @click="openInNewTab">
        在新窗口打开
      </el-button>
    </div>
  </div>
</template>
    
    <script>
export default {
  name: "DroneCozeIframe",
  data() {
    return {
      // Coze 智能体信息
      botId: "7598031741319938099",
      iframeTitle: "无人机调度专家助手 - Coze",

      // 状态控制
      loading: true,
      reloading: false,
      isFullscreen: false,

      // 快捷提问列表（针对无人机调度优化）
      quickQuestions: [
        {
          label: "物流配送规划",
          text: "帮我规划一条从南昌站到昌北机场的无人机医疗物资配送最优路径，考虑当前天气。",
        },
        {
          label: "多机协同巡逻",
          text: "多架无人机在八一广场区域协同巡逻，怎么避免冲突和规划最优路线？",
        },
        {
          label: "电池消耗预测",
          text: "在目前的气象条件下，DJI Matrice 350 型号无人机执行1小时巡逻任务，电池消耗如何预测？",
        },
        {
          label: "应急响应方案",
          text: "突发暴雨天气，正在执行配送任务的无人机应该如何应急处理？",
        },
        {
          label: "空域申请咨询",
          text: "在昌北机场周边进行物流配送，需要哪些空域申请手续和注意事项？",
        },
      ],
    };
  },
  computed: {
    // 构建 iframe src，可添加额外参数
    iframeSrc() {
      // 基础URL，直接链接到您的智能体
      let src = `https://www.coze.cn/store/agent/${this.botId}`;

      // 可选：添加URL参数以定制体验
      const params = new URLSearchParams({
        bot_id: "true",
        // 'theme': 'light', // 可选：强制浅色主题
        // 'fixed': 'true',  // 可选：固定某些UI元素
      });

      return `${src}?${params.toString()}`;
    },
  },
  mounted() {
    // 监听全屏变化
    document.addEventListener("fullscreenchange", this.handleFullscreenChange);
    document.addEventListener(
      "webkitfullscreenchange",
      this.handleFullscreenChange
    );
    document.addEventListener(
      "mozfullscreenchange",
      this.handleFullscreenChange
    );
    document.addEventListener(
      "MSFullscreenChange",
      this.handleFullscreenChange
    );

    // 初始加载完成后，可以尝试与iframe通信（如果需要）
    this.$nextTick(() => {
      // 可以在这里初始化一些iframe通信逻辑
    });
  },
  beforeDestroy() {
    // 清理事件监听
    document.removeEventListener(
      "fullscreenchange",
      this.handleFullscreenChange
    );
    document.removeEventListener(
      "webkitfullscreenchange",
      this.handleFullscreenChange
    );
    document.removeEventListener(
      "mozfullscreenchange",
      this.handleFullscreenChange
    );
    document.removeEventListener(
      "MSFullscreenChange",
      this.handleFullscreenChange
    );

    // 退出全屏（如果正在全屏）
    if (this.isFullscreen && document.fullscreenElement) {
      this.exitFullscreen();
    }
  },
  methods: {
    // Iframe 事件处理
    onIframeLoad() {
      console.log("Coze智能体加载完成");
      this.loading = false;
      this.reloading = false;

      // 可以在这里添加与iframe的通信逻辑
      // 例如：发送初始化消息、设置主题等
    },

    onIframeError() {
      console.error("Coze智能体加载失败");
      this.loading = false;
      this.reloading = false;
      this.$message.error("智能体加载失败，请检查网络或刷新重试");
    },

    // 操作按钮方法
    reloadIframe() {
      this.reloading = true;
      this.loading = true;
      this.$refs.cozeIframe.contentWindow.location.reload();

      // 设置超时，防止无限加载
      setTimeout(() => {
        if (this.loading) {
          this.loading = false;
          this.reloading = false;
        }
      }, 10000);
    },

    sendQuickQuestion(question) {
      // 注意：由于跨域限制，直接通过JS向iframe注入内容可能受限
      // 这里提供一种可能的实现思路，实际效果取决于Coze页面的支持程度
      try {
        const iframe = this.$refs.cozeIframe;
        if (iframe && iframe.contentWindow) {
          // 尝试查找Coze页面中的输入框并填充文本
          // 这需要Coze页面提供相应的API或已知的DOM结构
          console.log("尝试发送快捷问题:", question);

          // 由于同源策略限制，直接操作跨域iframe的DOM通常不可行
          // 作为备选方案，我们可以在当前页面显示提示，并让用户手动发送
          this.$message.info(`问题已复制：${question}`);

          // 尝试使用剪贴板API复制问题，方便用户手动粘贴
          if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard
              .writeText(question)
              .then(() => {
                this.$message.success(
                  "问题已复制到剪贴板，请在聊天框中粘贴发送"
                );
              })
              .catch((err) => {
                console.error("复制失败: ", err);
              });
          }
        }
      } catch (error) {
        console.warn("与iframe通信受限，这是浏览器的安全限制");
        this.$message.warning(
          "由于浏览器安全限制，请手动输入或复制问题到聊天框"
        );
      }
    },

    // 全屏控制
    toggleFullscreen() {
      const wrapper = this.$refs.iframeWrapper;
      if (!this.isFullscreen) {
        if (wrapper.requestFullscreen) {
          wrapper.requestFullscreen();
        } else if (wrapper.webkitRequestFullscreen) {
          wrapper.webkitRequestFullscreen();
        } else if (wrapper.mozRequestFullScreen) {
          wrapper.mozRequestFullScreen();
        } else if (wrapper.msRequestFullscreen) {
          wrapper.msRequestFullscreen();
        }
      }
    },

    exitFullscreen() {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.mozCancelFullScreen) {
        document.mozCancelFullScreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    },

    handleFullscreenChange() {
      this.isFullscreen = !!(
        document.fullscreenElement ||
        document.webkitFullscreenElement ||
        document.mozFullScreenElement ||
        document.msFullscreenElement
      );
    },

    // 其他工具方法
    openInNewTab() {
      window.open(this.iframeSrc, "_blank");
    },
  },
};
</script>
    
    <style scoped>
.drone-coze-iframe-container {
  display: flex;
  flex-direction: column;
  height: 800px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
}

/* 头部样式 */
.iframe-header {
  padding: 15px 20px;
  background: linear-gradient(135deg, #1a56db, #7e3af2);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-left .icon {
  font-size: 24px;
}

.header-left h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

/* 快捷问题区域 */
.quick-questions {
  padding: 15px 20px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.quick-questions-label {
  margin-bottom: 10px;
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.quick-questions .el-button-group {
  flex-wrap: wrap;
  gap: 8px;
}

.quick-questions .el-button {
  margin-bottom: 5px;
}

/* Iframe包装器 */
.iframe-wrapper {
  flex: 1;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
}

.iframe-wrapper.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 9999;
  background: white;
}

.iframe-wrapper iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

/* 加载状态 */
.iframe-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.loading-icon {
  font-size: 40px;
  color: #7e3af2;
  margin-bottom: 15px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.iframe-loading p {
  color: #6b7280;
  font-size: 16px;
}

/* 底部样式 */
.iframe-footer {
  padding: 12px 20px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.footer-info {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 13px;
  color: #6b7280;
}

.powered-by {
  display: flex;
  align-items: center;
  gap: 5px;
}

.divider {
  color: #d1d5db;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .drone-coze-iframe-container {
    height: 600px;
  }
  .iframe-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .header-actions {
    align-self: flex-end;
  }

  .quick-questions .el-button-group {
    justify-content: center;
  }
  .quick-questions .el-button {
    font-size: 12px;
    padding: 6px 10px;
  }
}
</style>