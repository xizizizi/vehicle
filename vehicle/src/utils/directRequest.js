import axios from "axios";
import { Message } from "element-ui";
import store from "@/store";
import { getToken } from "@/utils/auth";

// 创建一个完全独立的axios实例，不经过任何拦截器
const directRequest = axios.create({
  baseURL: "http://127.0.0.1:8080/",
  timeout: 10000,
});

// 只添加token，不添加响应拦截器
directRequest.interceptors.request.use(
  (config) => {
    if (store.getters.token) {
      config.headers["X-Token"] = getToken();
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default directRequest;
