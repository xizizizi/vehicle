import request from "@/utils/request";

// AI 聊天接口
export function chatWithAI(data) {
  return request({
    url: "/coze/chat",
    method: "post",
    data,
  });
}

// 快速测试接口
export function quickTest(data) {
  return request({
    url: "/coze/quick-test",
    method: "post",
    data,
  });
}

// 健康检查
export function testAIHealth() {
  return request({
    url: "/coze/health",
    method: "get",
  });
}

// 测试接口
export function testAI() {
  return request({
    url: "/coze/test",
    method: "get",
  });
}
