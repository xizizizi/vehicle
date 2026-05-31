// src/api/inspectionData.js
import request from "@/utils/request";

// 获取巡查数据列表
export function getInspectionDataList(params) {
  return request({
    url: "/api/reports/data/list",
    method: "get",
    params,
  });
}

// 获取统计信息（如果需要的话）
export function getSystemStats(systemType) {
  return request({
    url: "/api/reports/data/stats",
    method: "get",
    params: { systemType },
  });
}
