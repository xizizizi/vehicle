import axios from "axios";
import { Message } from "element-ui";
import store from "@/store";
import { getToken } from "@/utils/auth";

// 创建 axios 实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || "/",
  timeout: 120000, // 将超时时间增加到120秒（2分钟）
  headers: {
    "Content-Type": "application/json;charset=utf-8",
  },
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 显示加载动画
    if (config.showLoading !== false) {
      // 可以在这里添加全局 loading
    }

    // 为 Coze 接口添加更长的超时时间
    if (config.url.includes("/coze/")) {
      config.timeout = 180000; // Coze 接口单独设置180秒超时
    }

    // 如果有 token，添加到 header
    if (store.getters.token) {
      config.headers["Authorization"] = "Bearer " + getToken();
    }

    return config;
  },
  (error) => {
    console.error("请求配置错误:", error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 隐藏加载动画
    if (response.config.showLoading !== false) {
      // 在这里隐藏全局 loading
    }

    // 检查返回的 HTTP 状态码
    if (response.status !== 200 && response.status !== 201) {
      Message({
        message: `请求失败，状态码: ${response.status}`,
        type: "error",
        duration: 5 * 1000,
      });
      return Promise.reject(new Error(`HTTP Error ${response.status}`));
    }

    // 如果后端返回了自定义的 success 字段
    if (res.success !== undefined) {
      if (!res.success) {
        Message({
          message: res.message || "请求失败",
          type: "error",
          duration: 5 * 1000,
        });
        return Promise.reject(new Error(res.message || "请求失败"));
      }
      return res;
    }

    // 如果没有 success 字段，直接返回数据
    return res;
  },
  (error) => {
    console.error("响应错误:", error);

    // 隐藏加载动画
    // 在这里隐藏全局 loading

    let message = "请求失败";

    if (error.code === "ECONNABORTED" && error.message.includes("timeout")) {
      message = "请求超时，请稍后重试";
    } else if (error.response) {
      // 服务器返回错误状态码
      switch (error.response.status) {
        case 400:
          message = "请求参数错误";
          break;
        case 401:
          message = "未授权，请重新登录";
          // 跳转到登录页面
          store.dispatch("user/resetToken").then(() => {
            location.reload();
          });
          break;
        case 403:
          message = "拒绝访问";
          break;
        case 404:
          message = "请求地址不存在";
          break;
        case 408:
          message = "请求超时";
          break;
        case 500:
          message = "服务器内部错误";
          break;
        case 502:
          message = "网关错误";
          break;
        case 503:
          message = "服务不可用";
          break;
        case 504:
          message = "网关超时";
          break;
        default:
          message = `请求错误 (${error.response.status})`;
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      message = "网络连接失败，请检查网络设置";
    } else {
      // 请求配置错误
      message = error.message;
    }

    Message({
      message: message,
      type: "error",
      duration: 5 * 1000,
    });

    return Promise.reject(error);
  }
);

export default service;
