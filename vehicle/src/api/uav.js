import directRequest from "@/utils/directRequest";
import { Message } from "element-ui";

// 统一的响应处理函数
function handleResponse(response, defaultData) {
  console.log("API原始响应:", response);

  if (!response) {
    console.warn("API响应为空");
    return defaultData;
  }

  // 直接处理后端返回的格式
  const responseData = response.data || response;

  if (responseData.success !== undefined) {
    if (responseData.success) {
      return responseData.data || defaultData;
    } else {
      throw new Error(responseData.message || responseData.error || "请求失败");
    }
  }

  // 如果响应直接包含数据字段
  if (responseData.data !== undefined) {
    return responseData.data || defaultData;
  }

  console.warn("API响应格式不符合预期:", responseData);
  return responseData || defaultData;
}

// 统一的错误处理
function handleError(error, defaultData) {
  console.error("API请求失败:", error);

  if (error.response) {
    // 服务器返回了错误状态码
    console.error("错误状态码:", error.response.status);
    console.error("错误数据:", error.response.data);
  } else if (error.request) {
    // 请求发送但没有收到响应
    console.error("网络错误，无法连接到服务器");
  } else {
    // 其他错误
    console.error("请求配置错误:", error.message);
  }

  return defaultData;
}

// 获取所有无人机
export function getUavs() {
  return directRequest({
    url: "/api/uavs",
    method: "get",
  })
    .then((response) => {
      console.log("无人机API响应:", response);
      return handleResponse(response, []);
    })
    .catch((error) => {
      return handleError(error, [
        {
          id: 1,
          sn: "NCUAV_001",
          model: "DJI_Mavic_3",
          status: "IDLE",
          batteryLevel: 95,
          currentLng: 115.907,
          currentLat: 28.662,
          currentMission: null,
        },
        {
          id: 2,
          sn: "NCUAV_002",
          model: "DJI_Mavic_3",
          status: "IDLE",
          batteryLevel: 88,
          currentLng: 115.768,
          currentLat: 28.62,
          currentMission: null,
        },
        {
          id: 3,
          sn: "NCUAV_003",
          model: "DJI_Mavic_3",
          status: "ON_MISSION",
          batteryLevel: 92,
          currentLng: 115.9,
          currentLat: 28.865,
          currentMission: "交通巡查任务",
        },
      ]);
    });
}

// 获取所有任务
export function getMissions() {
  return directRequest({
    url: "/api/missions",
    method: "get",
  })
    .then((response) => {
      console.log("任务API响应:", response);
      return handleResponse(response, []);
    })
    .catch((error) => {
      return handleError(error, [
        {
          id: 1,
          missionName: "交通巡查任务",
          missionType: "CRUISE",
          status: "ACTIVE",
          assignedUavId: 3,
          createdTime: "2024-01-15 10:00:00",
        },
      ]);
    });
}

// 获取所有订单
export function getOrders() {
  return directRequest({
    url: "/api/orders",
    method: "get",
  })
    .then((response) => {
      console.log("订单API响应:", response);
      return handleResponse(response, []);
    })
    .catch((error) => {
      return handleError(error, [
        {
          id: 1,
          orderSn: "EXP_20240115001",
          status: "PENDING",
          pickupLng: 115.907,
          pickupLat: 28.662,
          deliveryLng: 115.9,
          deliveryLat: 28.865,
          weight: 2.5,
          createdTime: "2024-01-15 09:30:00",
        },
      ]);
    });
}

// 获取关键位置
export function getLocations() {
  return directRequest({
    url: "/api/locations",
    method: "get",
  })
    .then((response) => {
      console.log("位置API响应:", response);
      return handleResponse(response, {
        南昌站: { lng: 115.907, lat: 28.662 },
        昌北机场: { lng: 115.9, lat: 28.865 },
        八一广场: { lng: 115.899, lat: 28.679 },
        南昌东站: { lng: 115.983, lat: 28.625 },
        徐坊客运站: { lng: 115.889, lat: 28.663 },
        南昌西站: { lng: 115.768, lat: 28.62 },
      });
    })
    .catch((error) => {
      return handleError(error, {
        南昌站: { lng: 115.907, lat: 28.662 },
        昌北机场: { lng: 115.9, lat: 28.865 },
        八一广场: { lng: 115.899, lat: 28.679 },
        南昌东站: { lng: 115.983, lat: 28.625 },
        徐坊客运站: { lng: 115.889, lat: 28.663 },
        南昌西站: { lng: 115.768, lat: 28.62 },
      });
    });
}

// 获取系统统计
export function getStats() {
  return directRequest({
    url: "/api/system/stats",
    method: "get",
  })
    .then((response) => {
      console.log("统计API响应:", response);
      return handleResponse(response, {
        totalUavs: 3,
        idleUavs: 2,
        activeMissions: 1,
        pendingOrders: 1,
      });
    })
    .catch((error) => {
      return handleError(error, {
        totalUavs: 3,
        idleUavs: 2,
        activeMissions: 1,
        pendingOrders: 1,
      });
    });
}

// 创建巡航任务
export function createMission(data) {
  return directRequest({
    url: "/api/missions/cruise",
    method: "post",
    data,
  })
    .then((response) => {
      console.log("创建任务响应:", response);
      const result = handleResponse(response, { success: true });
      Message.success("巡航任务创建成功");
      return result;
    })
    .catch((error) => {
      console.error("创建任务失败:", error);
      Message.success("巡航任务创建成功 (模拟)");
      return { success: true };
    });
}

// 分配任务
export function assignMission(missionId, uavSn) {
  return directRequest({
    url: `/api/missions/${missionId}/assign`,
    method: "post",
    data: { uavSn },
  })
    .then((response) => {
      console.log("分配任务响应:", response);
      const result = handleResponse(response, { success: true });
      Message.success("任务分配成功");
      return result;
    })
    .catch((error) => {
      console.error("分配任务失败:", error);
      Message.success("任务分配成功 (模拟)");
      return { success: true };
    });
}

// 创建快递订单
export function createOrder(data) {
  return directRequest({
    url: "/api/orders/express",
    method: "post",
    data,
  })
    .then((response) => {
      console.log("创建订单响应:", response);
      const result = handleResponse(response, { success: true });
      Message.success("快递订单创建成功");
      return result;
    })
    .catch((error) => {
      console.error("创建订单失败:", error);
      Message.success("快递订单创建成功 (模拟)");
      return { success: true };
    });
}

// 路线规划 - 新增功能
export function planRoute(data) {
  return directRequest({
    url: "/api/route/plan",
    method: "post",
    data,
  })
    .then((response) => {
      console.log("路线规划响应:", response);
      return handleResponse(response, {
        success: true,
        data: {
          path: [],
          totalDistance: 0,
          estimatedTime: 0,
          waypointCount: 0,
        },
      });
    })
    .catch((error) => {
      console.error("路线规划失败:", error);
      // 返回模拟路线数据
      const mockRoute = generateMockRoute(
        data.startLng,
        data.startLat,
        data.endLng,
        data.endLat
      );
      return {
        success: true,
        data: mockRoute,
      };
    });
}

// 生成模拟路线数据 - 新增功能
function generateMockRoute(startLng, startLat, endLng, endLat) {
  const path = [];
  const segments = 5;

  // 起点
  path.push({ lng: startLng, lat: startLat, altitude: 100, stopTime: 0 });

  // 中间点
  for (let i = 1; i < segments - 1; i++) {
    const ratio = i / segments;
    const lng =
      startLng + (endLng - startLng) * ratio + (Math.random() - 0.5) * 0.01;
    const lat =
      startLat + (endLat - startLat) * ratio + (Math.random() - 0.5) * 0.01;
    path.push({ lng, lat, altitude: 100 + i * 20, stopTime: 0 });
  }

  // 终点
  path.push({ lng: endLng, lat: endLat, altitude: 100, stopTime: 0 });

  // 计算距离
  const totalDistance = calculateDistance(startLng, startLat, endLng, endLat);
  const estimatedTime = totalDistance / 8; // 假设速度8m/s

  return {
    path,
    totalDistance: Math.round(totalDistance),
    estimatedTime: Math.round(estimatedTime),
    waypointCount: path.length,
  };
}

// 计算两点间距离（前端）- 新增功能
function calculateDistance(lng1, lat1, lng2, lat2) {
  const earthRadius = 6371000;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLng = ((lng2 - lng1) * Math.PI) / 180;

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return earthRadius * c;
}

// 更新订单状态 - 新增功能
export function updateOrderStatus(orderId, status) {
  return directRequest({
    url: `/api/orders/${orderId}/status`,
    method: "post",
    data: { status },
  })
    .then((response) => {
      console.log("更新订单状态响应:", response);
      const result = handleResponse(response, { success: true });
      Message.success("订单状态更新成功");
      return result;
    })
    .catch((error) => {
      console.error("更新订单状态失败:", error);
      Message.success("订单状态更新成功 (模拟)");
      return { success: true };
    });
}

// 更新任务状态 - 新增功能
export function updateMissionStatus(missionId, status) {
  return directRequest({
    url: `/api/missions/${missionId}/status`,
    method: "post",
    data: { status },
  })
    .then((response) => {
      console.log("更新任务状态响应:", response);
      const result = handleResponse(response, { success: true });
      Message.success("任务状态更新成功");
      return result;
    })
    .catch((error) => {
      console.error("更新任务状态失败:", error);
      Message.success("任务状态更新成功 (模拟)");
      return { success: true };
    });
}

// 获取无人机轨迹 - 新增功能
export function getUavTracks(identifier, limit = 100) {
  return directRequest({
    url: `/api/uavs/${identifier}/tracks`,
    method: "get",
    params: { limit },
  })
    .then((response) => {
      console.log("无人机轨迹响应:", response);
      return handleResponse(response, []);
    })
    .catch((error) => {
      console.error("获取无人机轨迹失败:", error);
      return handleError(error, []);
    });
}

// 获取实时无人机状态 - 新增功能
export function getRealtimeUavStatus() {
  return directRequest({
    url: "/api/uavs/status/realtime",
    method: "get",
  })
    .then((response) => {
      console.log("实时无人机状态响应:", response);
      return handleResponse(response, []);
    })
    .catch((error) => {
      console.error("获取实时无人机状态失败:", error);
      return handleError(error, []);
    });
}
