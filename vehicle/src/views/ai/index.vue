<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <i class="el-icon-chat-line-round" style="margin-right: 10px"></i>
        AI 智能助手
      </h1>
      <p class="page-desc">
        基于 Coze 平台的智能对话助手，为您提供专业的无人机调度建议
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
                <div class="message-text">
                  <!-- 解析换行和列表 -->
                  <pre class="ai-response">{{
                    formatAIResponse(message.content)
                  }}</pre>
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
              <span class="status-text">
                服务在线
              </span>
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

    // 格式化 AI 回复
    formatAIResponse(content) {
      if (!content) return "";

      // 处理 Markdown 样式的列表
      return content
        .replace(/\n/g, "\n")
        .replace(/\*\*(.*?)\*\*/g, "$1") // 移除粗体标记
        .replace(/\d+\.\s/g, (match) => `\n${match}`); // 为数字列表项添加换行
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
  background: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 10px 0;
    display: flex;
    align-items: center;
  }

  .page-desc {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.ai-container {
  display: flex;
  flex: 1;
  gap: 20px;
  min-height: 0;

  .chat-container {
    flex: 3;
    display: flex;
    flex-direction: column;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;

    .messages-container {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      max-height: 60vh;

      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #909399;

        .empty-icon {
          font-size: 48px;
          color: #c0c4cc;
          margin-bottom: 20px;
        }

        h3 {
          margin: 0 0 10px 0;
          color: #303133;
        }

        p {
          margin: 0 0 20px 0;
          font-size: 14px;
        }

        .quick-questions {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          justify-content: center;
          max-width: 600px;
          margin: 0 auto;

          .quick-question-btn {
            border: 1px solid #dcdfe6;
            background: #f5f7fa;
            color: #606266;

            &:hover {
              border-color: #409eff;
              color: #409eff;
            }
          }
        }
      }

      .message-item {
        margin-bottom: 20px;
        animation: fadeIn 0.3s ease;

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
              margin-right: 10px;
              align-items: flex-end;
            }

            .message-text {
              background: #409eff;
              color: white;
              border-radius: 12px 12px 0 12px;
            }
          }

          &.ai {
            .message-text {
              background: #f6f8fa;
              color: #303133;
              border-radius: 12px 12px 12px 0;
            }
          }

          .avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;

            &.user-avatar {
              background: #409eff;
              color: white;
            }

            &.ai-avatar {
              background: #67c23a;
              color: white;
            }
          }

          .message-content {
            flex: 1;
            margin-left: 10px;
            display: flex;
            flex-direction: column;

            .message-header {
              display: flex;
              justify-content: space-between;
              align-items: center;
              margin-bottom: 5px;

              .ai-name {
                font-weight: 500;
                color: #67c23a;
              }

              .copy-btn {
                padding: 0;
                font-size: 14px;

                &:hover {
                  color: #409eff;
                }
              }
            }

            .message-text {
              padding: 12px 15px;
              line-height: 1.5;
              font-size: 14px;
              word-break: break-word;

              .ai-response {
                margin: 0;
                white-space: pre-wrap;
                font-family: inherit;
                font-size: 14px;
                line-height: 1.6;
              }
            }

            .message-time {
              font-size: 12px;
              color: #909399;
              margin-top: 5px;
            }
          }
        }
      }

      .loading-message {
        .thinking {
          display: flex;
          align-items: center;

          .dots {
            margin-left: 5px;

            .dot {
              animation: dotFlashing 1.4s infinite linear;
              animation-delay: 0s;

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
      border-top: 1px solid #ebeef5;
      padding: 20px;
      background: #fafafa;

      .input-wrapper {
        .message-input {
          ::v-deep .el-textarea__inner {
            border: 1px solid #dcdfe6;
            border-radius: 8px;
            resize: none;

            &:focus {
              border-color: #409eff;
            }
          }
        }

        .input-actions {
          margin-top: 10px;

          .input-tips {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .char-count {
              font-size: 12px;
              color: #909399;
            }

            .send-btn {
              border-radius: 20px;
              padding: 8px 20px;
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
    gap: 20px;

    .panel-card {
      background: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

      .panel-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 15px 0;
        display: flex;
        align-items: center;

        i {
          margin-right: 8px;
        }
      }

      .panel-content {
        .info-item,
        .settings-item {
          margin-bottom: 15px;

          label {
            font-size: 14px;
            color: #606266;
            font-weight: 500;
            display: block;
            margin-bottom: 5px;
          }

          span,
          .user-id {
            font-size: 14px;
            color: #303133;
            display: inline-block;
            vertical-align: middle;
          }

          .user-id {
            background: #f5f7fa;
            padding: 2px 8px;
            border-radius: 4px;
            font-family: monospace;
            margin-right: 5px;
          }

          .expertise-list {
            list-style: none;
            padding: 0;
            margin: 0;

            li {
              padding: 5px 0;
              border-bottom: 1px dashed #ebeef5;

              &:last-child {
                border-bottom: none;
              }
            }
          }

          .stats {
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #909399;
          }
        }

        .action-buttons {
          margin-top: 20px;
          display: flex;
          gap: 10px;
        }
      }

      &.status-card {
        .status-item {
          display: flex;
          align-items: center;
          margin-bottom: 15px;

          .status-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: #f56c6c;
            margin-right: 10px;

            &.online {
              background: #67c23a;
            }
          }

          &.online .status-dot {
            background: #67c23a;
          }
        }

        .status-info {
          font-size: 13px;
          color: #909399;

          p {
            margin: 5px 0;
          }

          .timestamp {
            font-size: 12px;
            color: #c0c4cc;
          }

          &.error {
            color: #f56c6c;
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
}
</style>