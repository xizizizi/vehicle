// api/ai.js
import request from "@/utils/request";

// AI对话
export function chatWithAI(data) {
  return request({
    url: "/api/ai/chat",
    method: "post",
    data,
  });
}

// 快速对话
export function quickChat(data) {
  return request({
    url: "/api/ai/quick-chat",
    method: "post",
    data,
  });
}

// 清理会话
export function clearSession(sessionId) {
  return request({
    url: `/api/ai/session/${sessionId}`,
    method: "delete",
  });
}

// 获取状态
export function getAIStatus() {
  return request({
    url: "/api/ai/status",
    method: "get",
  });
}
