<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <i class="el-icon-chat-line-round" style="margin-right: 10px"></i>
        AI 智能助手
      </h1>
      <p class="page-desc">
        基于多状态电池预测的无人机换电调度的智能对话助手，为您提供专业的无人机调度建议
      </p>
    </div>

    <!-- 主内容区域 -->
    <div class="ai-container">
      <!-- 左侧聊天区域 -->
      <div class="chat-container">
        <!-- 聊天消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-state">
            <div class="empty-icon">
              <i class="el-icon-chat-dot-round"></i>
            </div>
            <h3>开始对话</h3>
            <p>我是您的无人机调度专家，可以为您提供专业的调度建议和问题解答</p>
            <div class="quick-questions">
              <el-button
                v-for="question in quickQuestions"
                :key="question"
                @click="sendQuickQuestion(question)"
                class="quick-question-btn"
              >
                {{ question }}
              </el-button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message-item', message.type]"
          >
            <!-- 用户消息 -->
            <div v-if="message.type === 'user'" class="message-bubble user">
              <div class="avatar user-avatar">
                <i class="el-icon-user"></i>
              </div>
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">
                  {{ formatTime(message.timestamp) }}
                </div>
              </div>
            </div>

            <!-- AI 消息 -->
            <!-- AI 消息 -->
            <div v-else class="message-bubble ai">
              <div class="avatar ai-avatar">
                <i class="el-icon-aim"></i>
              </div>
              <div class="message-content">
                <div class="message-header">
                  <span class="ai-name">AI 助手</span>
                  <el-tooltip content="复制到剪贴板" placement="top">
                    <el-button
                      type="text"
                      icon="el-icon-document-copy"
                      @click="copyToClipboard(message.content)"
                      class="copy-btn"
                    ></el-button>
                  </el-tooltip>
                </div>
                <div class="message-text markdown-body">
                  <!-- 使用 v-html 渲染解析后的 Markdown -->
                  <div
                    class="ai-response"
                    v-html="formatAIResponse(message.content)"
                  ></div>
                </div>
                <div class="message-time">
                  {{ formatTime(message.timestamp) }}
                </div>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="loading" class="loading-message">
            <div class="message-bubble ai">
              <div class="avatar ai-avatar">
                <i class="el-icon-aim"></i>
              </div>
              <div class="message-content">
                <div class="thinking">
                  <span>AI 正在思考</span>
                  <span class="dots">
                    <span class="dot">.</span>
                    <span class="dot">.</span>
                    <span class="dot">.</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-container">
          <div class="input-wrapper">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              :maxlength="1000"
              placeholder="请输入您的问题..."
              :disabled="loading"
              @keyup.enter.native="handleSendMessage"
              resize="none"
              class="message-input"
            >
            </el-input>
            <div class="input-actions">
              <div class="input-tips">
                <span class="char-count">{{ inputMessage.length }}/1000</span>
                <el-tooltip content="发送消息 (Ctrl+Enter)" placement="top">
                  <el-button
                    type="primary"
                    :loading="loading"
                    :disabled="!inputMessage.trim() || loading"
                    @click="handleSendMessage"
                    class="send-btn"
                  >
                    {{ loading ? "发送中..." : "发送" }}
                    <i class="el-icon-position"></i>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧信息面板 -->
      <div class="info-panel">
        <div class="panel-card">
          <h3 class="panel-title">
            <i class="el-icon-info"></i>
            助手信息
          </h3>
          <div class="panel-content">
            <div class="info-item">
              <label>专业领域：</label>
              <span>无人机调度与物流管理</span>
            </div>
            <div class="info-item">
              <label>擅长方向：</label>
              <ul class="expertise-list">
                <li>路径规划优化</li>
                <li>场景决策建议</li>
                <li>电池续航管理</li>
                <li>多机协同调度</li>
                <li>气象影响分析</li>
                <li>安全规范指导</li>
                <li>成本效益评估</li>
              </ul>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <h3 class="panel-title">
            <i class="el-icon-setting"></i>
            对话设置
          </h3>
          <div class="panel-content">
            <div class="settings-item">
              <label>用户标识：</label>
              <span class="user-id">{{ userId }}</span>
              <el-tooltip content="重新生成用户ID" placement="top">
                <el-button
                  type="text"
                  icon="el-icon-refresh"
                  @click="generateUserId"
                  size="mini"
                ></el-button>
              </el-tooltip>
            </div>
            <div class="settings-item">
              <label>对话统计：</label>
              <div class="stats">
                <span>消息：{{ messages.length }}</span>
                <el-divider direction="vertical"></el-divider>
                <span>今日：{{ todayMessages }}</span>
              </div>
            </div>
            <div class="action-buttons">
              <el-button
                @click="clearConversation"
                icon="el-icon-delete"
                size="small"
              >
                清空对话
              </el-button>
              <el-button
                @click="exportConversation"
                icon="el-icon-download"
                size="small"
              >
                导出对话
              </el-button>
            </div>
          </div>
        </div>

        <!-- 系统状态 -->
        <div class="panel-card status-card">
          <h3 class="panel-title">
            <i class="el-icon-monitor"></i>
            系统状态
          </h3>
          <div class="panel-content">
            <div
              class="status-item"
              :class="{ online: systemStatus === 'online' }"
            >
              <span class="status-dot"></span>
              <span class="status-text"> 服务在线 </span>
            </div>
            <div v-if="systemStatus === 'online'" class="status-info">
              <p>API 连接正常</p>
              <p class="timestamp">上次检查：{{ formatTime(lastCheck) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
  
  <script>
import { chatWithAI, testAIHealth } from "@/api/coze";
import {
  getUserId,
  saveUserId,
  getTodayMessages,
  saveTodayMessages,
} from "@/utils/storage";

export default {
  name: "AIAssistant",
  data() {
    return {
      // 用户输入
      inputMessage: "",

      // 消息列表
      messages: [],

      // 加载状态
      loading: false,

      // 用户ID
      userId: "",

      // 系统状态
      systemStatus: "checking",
      lastCheck: null,

      // 快速问题
      quickQuestions: [
        "如何规划无人机配送路线？",
        "多架无人机如何协同工作？",
        "恶劣天气下如何调度无人机？",
        "如何评估无人机调度成本？",
        "电池续航应该如何管理？",
      ],

      // 今日消息计数
      todayMessages: 0,
    };
  },

  mounted() {
    this.init();
  },

  methods: {
    // 初始化
    init() {
      // 获取或生成用户ID
      this.userId = getUserId() || this.generateUserId();
      saveUserId(this.userId);

      // 获取今日消息数
      this.todayMessages = getTodayMessages();

      // 检查系统状态
      this.checkSystemStatus();

      // 加载历史对话
      this.loadConversation();
    },

    // 生成用户ID
    generateUserId() {
      const newUserId =
        "user_" + Date.now() + "_" + Math.random().toString(36).substr(2, 9);
      this.userId = newUserId;
      saveUserId(newUserId);
      this.$message.success("已生成新的用户ID");
      return newUserId;
    },
    // 在 methods 中添加这个方法
    formatAIResponse(content) {
      if (!content) return "";

      // 简单的 Markdown 格式转换
      let formatted = content
        // 标题
        .replace(/^### (.*$)/gm, "<h3>$1</h3>")
        .replace(/^## (.*$)/gm, "<h2>$1</h2>")
        .replace(/^# (.*$)/gm, "<h1>$1</h1>")
        // 加粗
        .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
        // 斜体
        .replace(/\*(.*?)\*/g, "<em>$1</em>")
        // 代码块
        .replace(/```([\s\S]*?)```/g, "<pre><code>$1</code></pre>")
        // 行内代码
        .replace(/`([^`]+)`/g, "<code>$1</code>")
        // 无序列表
        .replace(/^\* (.*$)/gm, "<li>$1</li>")
        // 有序列表
        .replace(/^\d+\. (.*$)/gm, "<li>$1</li>")
        // 链接
        .replace(
          /\[([^\]]+)\]\(([^)]+)\)/g,
          '<a href="$2" target="_blank">$1</a>'
        )
        // 换行
        .replace(/\n/g, "<br>");

      // 包装列表项
      if (formatted.includes("<li>")) {
        formatted =
          "<ul>" + formatted.replace(/<\/li><li>/g, "</li><li>") + "</ul>";
      }

      return formatted;
    },

    // 检查系统状态
    async checkSystemStatus() {
      try {
        const response = await testAIHealth();
        if (response.success) {
          this.systemStatus = "online";
        } else {
          this.systemStatus = "offline";
        }
      } catch (error) {
        this.systemStatus = "offline";
      }
      this.lastCheck = Date.now();
    },

    // 发送消息
    async handleSendMessage() {
      const message = this.inputMessage.trim();
      if (!message || this.loading) return;

      // 添加用户消息
      this.addMessage("user", message);

      // 清空输入框
      this.inputMessage = "";

      // 更新今日消息计数
      this.todayMessages++;
      saveTodayMessages(this.todayMessages);

      // 发送到 AI
      await this.sendToAI(message);

      // 滚动到底部
      this.scrollToBottom();
    },

    // 发送快速问题
    sendQuickQuestion(question) {
      this.inputMessage = question;
      this.handleSendMessage();
    },

    // 发送到 AI 接口
    async sendToAI(message) {
      this.loading = true;

      try {
        const response = await chatWithAI({
          message: message,
          userId: this.userId,
        });

        if (response.success) {
          // 添加 AI 回复
          this.addMessage("ai", response.data);

          // 保存对话历史
          this.saveConversation();
        } else {
          this.$message.error(response.message || "AI 回复失败");
          this.addMessage("ai", "抱歉，我暂时无法回答这个问题。请稍后重试。");
        }
      } catch (error) {
        console.error("AI 请求失败:", error);
        this.$message.error("请求失败，请检查网络连接");
        this.addMessage("ai", "网络连接失败，请检查您的网络设置。");
      } finally {
        this.loading = false;
      }
    },

    // 添加消息到列表
    addMessage(type, content) {
      const message = {
        type: type,
        content: content,
        timestamp: Date.now(),
      };
      this.messages.push(message);
    },

    // 格式化时间
    formatTime(timestamp) {
      if (!timestamp) return "";

      const date = new Date(timestamp);
      const now = new Date();
      const isToday = date.toDateString() === now.toDateString();

      if (isToday) {
        return date.toLocaleTimeString("zh-CN", {
          hour: "2-digit",
          minute: "2-digit",
        });
      } else {
        return date.toLocaleDateString("zh-CN");
      }
    },

    // 清空对话
    clearConversation() {
      this.$confirm("确定要清空当前对话吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.messages = [];
        localStorage.removeItem("ai_conversation");
        this.$message.success("对话已清空");
      });
    },

    // 导出对话
    exportConversation() {
      if (this.messages.length === 0) {
        this.$message.warning("没有可导出的对话内容");
        return;
      }

      let content = `AI 助手对话记录\n生成时间：${new Date().toLocaleString()}\n用户ID：${
        this.userId
      }\n\n`;

      this.messages.forEach((msg) => {
        const role = msg.type === "user" ? "用户" : "AI助手";
        const time = this.formatTime(msg.timestamp);
        content += `[${time}] ${role}：\n${msg.content}\n\n`;
      });

      const blob = new Blob([content], { type: "text/plain;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `AI对话_${new Date().getTime()}.txt`;
      link.click();
      URL.revokeObjectURL(url);

      this.$message.success("对话已导出");
    },

    // 复制到剪贴板
    copyToClipboard(text) {
      navigator.clipboard
        .writeText(text)
        .then(() => {
          this.$message.success("已复制到剪贴板");
        })
        .catch((err) => {
          console.error("复制失败:", err);
          this.$message.error("复制失败");
        });
    },

    // 保存对话
    saveConversation() {
      try {
        const conversation = {
          messages: this.messages,
          timestamp: Date.now(),
        };
        localStorage.setItem("ai_conversation", JSON.stringify(conversation));
      } catch (error) {
        console.error("保存对话失败:", error);
      }
    },

    // 加载对话
    loadConversation() {
      try {
        const saved = localStorage.getItem("ai_conversation");
        if (saved) {
          const conversation = JSON.parse(saved);
          // 只加载今天的数据
          const today = new Date().toDateString();
          const savedDate = new Date(conversation.timestamp).toDateString();

          if (today === savedDate) {
            this.messages = conversation.messages || [];
          }
        }
      } catch (error) {
        console.error("加载对话失败:", error);
      }
    },

    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    },
  },

  watch: {
    // 监听消息变化，自动滚动
    messages: {
      handler() {
        this.scrollToBottom();
      },
      deep: true,
    },
  },
};
</script>

  
  <style lang="scss" scoped>
.app-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 40px 20px 20px 20px;
  background: linear-gradient(135deg, #1a1e3e 0%, #151b35 50%, #111538 80%);
  position: relative;
  overflow: hidden;

  &::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: radial-gradient(
        circle at 20% 30%,
        rgba(42, 98, 255, 0.15) 0%,
        transparent 50%
      ),
      radial-gradient(
        circle at 80% 70%,
        rgba(86, 188, 255, 0.1) 0%,
        transparent 50%
      ),
      radial-gradient(
        circle at 40% 80%,
        rgba(135, 97, 255, 0.1) 0%,
        transparent 50%
      );
    z-index: 0;
    animation: pulseGlow 8s ease-in-out infinite alternate;
  }
}

@keyframes pulseGlow {
  0% {
    opacity: 0.6;
    filter: blur(40px);
  }
  100% {
    opacity: 1;
    filter: blur(60px);
  }
}

.page-header {
  margin-bottom: 10px;
  position: relative;
  z-index: 1;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #ffffff;
    margin: 0 0 12px 0;
    display: flex;
    align-items: center;
    text-shadow: 0 0 20px rgba(64, 158, 255, 0.5);

    i {
      color: #2a62ff;
      filter: drop-shadow(0 0 8px rgba(42, 98, 255, 0.8));
    }
  }

  .page-desc {
    font-size: 16px;
    color: #a0c8ff;
    margin: 0;
    opacity: 0.9;
  }
}

.ai-container {
  display: flex;
  flex: 1;
  gap: 24px;
  min-height: 0;
  position: relative;
  z-index: 1;

  .chat-container {
    flex: 3;
    display: flex;
    flex-direction: column;
    background: rgba(18, 25, 52, 0.82);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    border: 1px solid rgba(64, 158, 255, 0.2);
    box-shadow: 0 8px 32px rgba(0, 10, 255, 0.1),
      inset 0 1px 0 rgba(255, 255, 255, 0.1);
    overflow: hidden;

    .messages-container {
      flex: 1;
      padding: 24px;
      overflow-y: auto;
      max-height: 60vh;
      background: rgba(18, 26, 61, 0.25);

      &::-webkit-scrollbar {
        width: 8px;
      }

      &::-webkit-scrollbar-track {
        background: rgba(18, 25, 52, 0.3);
        border-radius: 4px;
      }

      &::-webkit-scrollbar-thumb {
        background: linear-gradient(180deg, #2a62ff, #86bcff);
        border-radius: 4px;

        &:hover {
          background: linear-gradient(180deg, #4d84ff, #a8d2ff);
        }
      }

      .empty-state {
        text-align: center;
        padding: 80px 20px;
        color: #a0c8ff;

        .empty-icon {
          font-size: 60px;
          color: #2a62ff;
          margin-bottom: 24px;
          filter: drop-shadow(0 0 12px rgba(42, 98, 255, 0.6));
        }

        h3 {
          margin: 0 0 12px 0;
          color: #ffffff;
          font-size: 22px;
          font-weight: 600;
        }

        p {
          margin: 0 0 32px 0;
          font-size: 16px;
          max-width: 500px;
          margin-left: auto;
          margin-right: auto;
          opacity: 0.9;
        }

        .quick-questions {
          display: flex;
          flex-wrap: wrap;
          gap: 12px;
          justify-content: center;
          max-width: 600px;
          margin: 0 auto;

          .quick-question-btn {
            background: rgba(42, 98, 255, 0.15);
            border: 1px solid rgba(64, 158, 255, 0.3);
            color: #86bcff;
            padding: 10px 20px;
            border-radius: 12px;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(42, 98, 255, 0.3);
              border-color: #2a62ff;
              color: #ffffff;
              transform: translateY(-2px);
              box-shadow: 0 4px 20px rgba(42, 98, 255, 0.3);
            }
          }
        }
      }

      .message-item {
        margin-bottom: 24px;
        animation: fadeIn 0.4s ease;

        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        .message-bubble {
          display: flex;
          max-width: 80%;

          &.user {
            margin-left: auto;
            flex-direction: row-reverse;

            .message-content {
              margin-left: 0;
              margin-right: 12px;
              align-items: flex-end;
            }

            .message-text {
              background: linear-gradient(135deg, #2a62ff 0%, #4d84ff 100%);
              color: white;
              border-radius: 18px 18px 0 18px;
              box-shadow: 0 4px 20px rgba(42, 98, 255, 0.3);
              border: 1px solid rgba(86, 188, 255, 0.3);
            }

            .avatar {
              background: linear-gradient(135deg, #2a62ff 0%, #4d84ff 100%);
              box-shadow: 0 4px 15px rgba(42, 98, 255, 0.4);
            }
          }

          &.ai {
            .message-text {
              background: rgba(30, 40, 80, 0.7);
              color: #e0f0ff;
              border-radius: 18px 18px 18px 0;
              border: 1px solid rgba(86, 188, 255, 0.2);
              box-shadow: 0 4px 20px rgba(0, 20, 60, 0.2);
            }

            .avatar {
              background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
              box-shadow: 0 4px 15px rgba(103, 194, 58, 0.4);
            }
          }

          .avatar {
            width: 44px;
            height: 44px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            color: white;
            font-size: 20px;
          }

          .message-content {
            flex: 1;
            margin-left: 12px;
            display: flex;
            flex-direction: column;

            .message-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 8px;

              .ai-name {
                font-weight: 600;
                color: #67c23a;
                font-size: 14px;
                text-shadow: 0 0 8px rgba(103, 194, 58, 0.3);
              }

              .copy-btn {
                padding: 0;
                font-size: 14px;
                color: #86bcff;

                &:hover {
                  color: #2a62ff;
                  transform: scale(1.1);
                }
              }
            }

            .message-text {
              padding: 16px;
              line-height: 1.6;
              font-size: 15px;
              word-break: break-word;

              .ai-response {
                margin: 0;
                white-space: pre-wrap;
                font-family: inherit;
                font-size: 15px;
                line-height: 1.7;
              }
            }

            .message-time {
              font-size: 12px;
              color: #86bcff;
              margin-top: 6px;
              opacity: 0.8;
            }
          }
        }
      }

      .loading-message {
        .thinking {
          display: flex;
          align-items: center;
          color: #86bcff;

          .dots {
            margin-left: 8px;

            .dot {
              animation: dotFlashing 1.4s infinite linear;
              animation-delay: 0s;
              color: #2a62ff;

              &:nth-child(2) {
                animation-delay: 0.2s;
              }

              &:nth-child(3) {
                animation-delay: 0.4s;
              }
            }
          }

          @keyframes dotFlashing {
            0% {
              opacity: 0;
            }
            50% {
              opacity: 1;
            }
            100% {
              opacity: 0;
            }
          }
        }
      }
    }

    .input-container {
      border-top: 1px solid rgba(64, 158, 255, 0.15);
      padding: 24px;
      background: rgba(18, 25, 52, 0.5);

      .input-wrapper {
        .message-input {
          ::v-deep .el-textarea__inner {
            background: rgba(10, 15, 40, 0.7);
            border: 1px solid rgba(86, 188, 255, 0.3);
            border-radius: 12px;
            resize: none;
            color: #e0f0ff;
            font-size: 15px;
            box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.2);

            &::placeholder {
              color: #86bcff;
              opacity: 0.6;
            }

            &:focus {
              border-color: #2a62ff;
              box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.2),
                0 0 20px rgba(42, 98, 255, 0.3);
              outline: none;
            }
          }
        }

        .input-actions {
          margin-top: 12px;

          .input-tips {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .char-count {
              font-size: 13px;
              color: #86bcff;
              opacity: 0.8;
            }

            .send-btn {
              background: linear-gradient(135deg, #2a62ff 0%, #4d84ff 100%);
              border: none;
              border-radius: 24px;
              padding: 10px 28px;
              font-weight: 600;
              color: white;
              box-shadow: 0 4px 20px rgba(42, 98, 255, 0.4);
              transition: all 0.3s ease;

              &:hover:not(:disabled) {
                transform: translateY(-2px);
                box-shadow: 0 6px 25px rgba(42, 98, 255, 0.6);
              }

              &:disabled {
                background: rgba(64, 158, 255, 0.3);
                opacity: 0.7;
              }
            }
          }
        }
      }
    }
  }

  .info-panel {
    flex: 1;
    min-width: 300px;
    display: flex;
    flex-direction: column;
    gap: 24px;

    .panel-card {
      background: rgba(18, 25, 52, 0.7);
      backdrop-filter: blur(20px);
      border-radius: 20px;
      padding: 24px;
      border: 1px solid rgba(64, 158, 255, 0.2);
      box-shadow: 0 8px 32px rgba(0, 10, 255, 0.1),
        inset 0 1px 0 rgba(255, 255, 255, 0.1);

      .panel-title {
        font-size: 18px;
        font-weight: 600;
        color: #ffffff;
        margin: 0 0 20px 0;
        display: flex;
        align-items: center;
        text-shadow: 0 0 10px rgba(64, 158, 255, 0.3);

        i {
          margin-right: 10px;
          color: #2a62ff;
          filter: drop-shadow(0 0 6px rgba(42, 98, 255, 0.8));
        }
      }

      .panel-content {
        .info-item,
        .settings-item {
          margin-bottom: 10px;

          label {
            font-size: 14px;
            color: #86bcff;
            font-weight: 500;
            display: block;
            margin-bottom: 2px;
            opacity: 0.9;
          }

          span,
          .user-id {
            font-size: 14px;
            color: #ffffff;
            display: inline-block;
            vertical-align: middle;
          }

          .user-id {
            background: rgba(42, 98, 255, 0.2);
            padding: 6px 12px;
            border-radius: 8px;
            font-family: monospace;
            margin-right: 8px;
            border: 1px solid rgba(86, 188, 255, 0.2);
          }

          .expertise-list {
            list-style: none;
            padding: 0;
            margin: 0;

            li {
              padding: 10px 0;
              border-bottom: 1px solid rgba(86, 188, 255, 0.1);
              color: #e0f0ff;
              position: relative;
              font-size: 14px; // 这里控制 li 的字体大小
              padding-left: 20px;

              &:before {
                content: "▸";
                position: absolute;
                left: 0;
                color: #2a62ff;
                filter: drop-shadow(0 0 4px rgba(42, 98, 255, 0.8));
              }

              &:last-child {
                border-bottom: none;
              }
            }
          }

          .stats {
            display: flex;
            align-items: center;
            font-size: 10px;
            color: #86bcff;
            gap: 12px;

            span {
              color: #ffffff;
            }

            .el-divider {
              background-color: rgba(86, 188, 255, 0.3);
            }
          }
        }

        .action-buttons {
          margin-top: 24px;
          display: flex;
          gap: 12px;

          .el-button {
            flex: 1;
            background: rgba(42, 98, 255, 0.15);
            border: 1px solid rgba(64, 158, 255, 0.3);
            color: #86bcff;
            border-radius: 12px;
            padding: 10px;

            &:hover {
              background: rgba(42, 98, 255, 0.3);
              border-color: #2a62ff;
              color: #ffffff;
              transform: translateY(-2px);
              box-shadow: 0 4px 20px rgba(42, 98, 255, 0.3);
            }
          }
        }
      }

      &.status-card {
        .status-item {
          display: flex;
          align-items: center;
          margin-bottom: 20px;

          .status-dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #f56c6c;
            margin-right: 12px;
            box-shadow: 0 0 10px #f56c6c;
            animation: pulse 2s infinite;

            @keyframes pulse {
              0%,
              100% {
                opacity: 1;
              }
              50% {
                opacity: 0.5;
              }
            }
          }

          &.online .status-dot {
            background: #67c23a;
            box-shadow: 0 0 10px #67c23a;
          }

          .status-text {
            color: #ffffff;
            font-weight: 500;
          }
        }

        .status-info {
          font-size: 14px;
          color: #86bcff;

          p {
            margin: 8px 0;
          }

          .timestamp {
            font-size: 12px;
            color: #a0c8ff;
            opacity: 0.8;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 992px) {
  .ai-container {
    flex-direction: column;

    .info-panel {
      min-width: auto;
    }
  }

  .app-container {
    padding: 24px 16px;
  }
}
.message-text {
  padding: 16px;
  line-height: 1.6;
  font-size: 15px;
  word-break: break-word;

  .ai-response {
    margin: 0;
    white-space: pre-wrap;
    font-family: inherit;
    font-size: 15px;
    line-height: 1.7;

    // 支持 HTML 标签样式
    ::v-deep {
      h1,
      h2,
      h3,
      h4,
      h5,
      h6 {
        margin: 1em 0 0.5em 0;
        font-weight: bold;
        color: #ffffff;
      }

      h1 {
        font-size: 1.4em;
      }
      h2 {
        font-size: 1.3em;
      }
      h3 {
        font-size: 1.2em;
      }

      p {
        margin: 0.8em 0;
      }

      ul,
      ol {
        margin: 0.8em 0;
        padding-left: 2em;
      }

      li {
        margin: 0.4em 0;
      }

      strong,
      b {
        font-weight: bold;
        color: #ffffff;
      }

      em,
      i {
        font-style: italic;
        color: #a0c8ff;
      }

      code {
        background: rgba(42, 98, 255, 0.2);
        padding: 0.2em 0.4em;
        border-radius: 4px;
        font-family: "Menlo", "Monaco", "Consolas", monospace;
        font-size: 0.9em;
        color: #86bcff;
      }

      pre {
        background: rgba(10, 15, 40, 0.8);
        border: 1px solid rgba(86, 188, 255, 0.2);
        border-radius: 8px;
        padding: 1em;
        margin: 1em 0;
        overflow-x: auto;
        font-family: "Menlo", "Monaco", "Consolas", monospace;
        font-size: 0.9em;
        line-height: 1.5;

        code {
          background: transparent;
          padding: 0;
          border-radius: 0;
          color: #e0f0ff;
        }
      }

      a {
        color: #2a62ff;
        text-decoration: none;
        border-bottom: 1px solid rgba(42, 98, 255, 0.3);
        transition: all 0.2s;

        &:hover {
          color: #86bcff;
          border-bottom-color: #86bcff;
        }
      }

      blockquote {
        margin: 1em 0;
        padding: 0.5em 1em;
        border-left: 4px solid #2a62ff;
        background: rgba(42, 98, 255, 0.1);
        border-radius: 0 8px 8px 0;
        color: #a0c8ff;

        p {
          margin: 0;
        }
      }
    }
  }
}
</style>