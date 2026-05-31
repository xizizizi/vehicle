import request from "@/utils/request";

export function login(data) {
  return request({
    url: "login",
    method: "post",
    data,
  });
}


export function getInfo(token) {
  return request({
    url: "getUserInfoByToken",
    method: "get", // 保持GET
    params: { token }, // 确保参数在URL中
  });
}

// export function logout() {
//   return request({
//      url: "/vehicle/user/logout",
//      method: "post",
//   });
// }
