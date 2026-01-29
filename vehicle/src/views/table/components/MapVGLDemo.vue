<template>
    <div class="map-container">
      <div id="mapContainer" class="map"></div>
  
      <!-- 路线规划面板 -->
      <div class="route-planning-panel" v-if="isPlanningRoute">
        <el-card class="planning-card">
          <div slot="header">
            <span>路线规划</span>
            <el-button
              type="text"
              @click="cancelRoutePlanning"
              style="float: right; padding: 0"
            >
              <i class="el-icon-close"></i>
            </el-button>
          </div>
  
          <el-form :model="routeForm" label-width="80px">
            <el-form-item label="起点位置">
              <el-select
                v-model="routeForm.startLocation"
                placeholder="请选择起点"
                style="width: 100%"
              >
                <el-option
                  v-for="(location, name) in locations"
                  :key="'start-' + name"
                  :label="name"
                  :value="name"
                >
                  <span style="float: left">{{ name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ location.lng.toFixed(4) }}, {{ location.lat.toFixed(4) }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
  
            <el-form-item label="终点位置">
              <el-select
                v-model="routeForm.endLocation"
                placeholder="请选择终点"
                style="width: 100%"
              >
                <el-option
                  v-for="(location, name) in locations"
                  :key="'end-' + name"
                  :label="name"
                  :value="name"
                >
                  <span style="float: left">{{ name }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">
                    {{ location.lng.toFixed(4) }}, {{ location.lat.toFixed(4) }}
                  </span>
                </el-option>
              </el-select>
            </el-form-item>
  
            <el-form-item>
              <el-button
                type="primary"
                @click="executeRoutePlanning"
                style="width: 100%"
                :loading="planningLoading"
                :disabled="!routeForm.startLocation || !routeForm.endLocation"
              >
                {{ planningLoading ? "规划中..." : "开始规划" }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
  
      <!-- 地图控制按钮 - 现在在右边中间 -->
      <div class="map-controls">
        <el-button @click="startRoutePlanning" type="primary" class="control-btn">
          <i class="el-icon-map-location"></i> 规划路径
        </el-button>
        <el-button @click="resetMap">重置视图</el-button>
      </div>
  
      <!-- 路径规划结果对话框 -->
      <route-planning-dialog
        :visible="showRouteDialog"
        :route-data="currentRouteData"
        :route-info="currentRouteInfo"
        :locations="locations"
        @update:visible="showRouteDialog = $event"
        @use-route="handleUseRoute"
        @show-on-map="handleShowOnMap"
        @close="handleRouteDialogClose"
      />
    </div>
  </template>
  
  <script>
  import axios from "axios";
  import RoutePlanningDialog from "./RoutePlanningDialog.vue";
  
  export default {
    name: "MapVGLDemo",
    components: {
      RoutePlanningDialog,
    },
    data() {
      return {
        map: null,
        isMapLoaded: false,
  
        // 关键位置数据 - 南昌市主要地标
        locations: {
          南昌西站: { lng: 115.782, lat: 28.621, name: "南昌西站" },
          南昌站: { lng: 115.907, lat: 28.662, name: "南昌站" },
          八一广场: { lng: 115.904, lat: 28.678, name: "八一广场" },
          滕王阁: { lng: 115.878, lat: 28.682, name: "滕王阁" },
          南昌大学: { lng: 115.806, lat: 28.663, name: "南昌大学" },
          昌北机场: { lng: 115.905, lat: 28.865, name: "昌北机场" },
          秋水广场: { lng: 115.857, lat: 28.683, name: "秋水广场" },
          江西省博物馆: { lng: 115.872, lat: 28.675, name: "江西省博物馆" },
          南昌八一起义纪念馆: {
            lng: 115.892,
            lat: 28.679,
            name: "南昌八一起义纪念馆",
          },
          梅岭国家森林公园: {
            lng: 115.72,
            lat: 28.783,
            name: "梅岭国家森林公园",
          },
        },
  
        // 路线规划相关
        isPlanningRoute: false,
        planningLoading: false,
        routeForm: {
          startLocation: "",
          endLocation: "",
        },
  
        // 路径规划对话框相关
        showRouteDialog: false,
        currentRouteData: {
          startLocation: "",
          endLocation: "",
          startPoint: null,
          endPoint: null,
        },
        currentRouteInfo: {
          path: [],
          totalDistance: 0,
          estimatedTime: 0,
          waypointCount: 0,
        },
  
        // 路径绘制相关
        routePolyline: null,
        routeMarkers: [],
        markers: [],
      };
    },
    mounted() {
      this.initBaiduMap();
    },
    beforeDestroy() {
      if (this.map) {
        this.clearMarkers();
        this.clearRouteFromMap();
      }
    },
    methods: {
      // 初始化百度地图 - 和11.txt完全相同的逻辑
      initBaiduMap() {
        // 如果百度地图API已经加载，直接创建地图
        if (typeof BMap !== "undefined") {
          this.isMapLoaded = true;
          this.createMap();
          return;
        }
  
        // 设置全局回调函数
        window.initBaiduMap = () => {
          this.isMapLoaded = true;
          this.createMap();
        };
  
        // 创建并加载百度地图API脚本
        const script = document.createElement("script");
        script.type = "text/javascript";
        // 使用与11.txt完全相同的AK和API版本
        script.src = `https://api.map.baidu.com/api?v=3.0&ak=B1d61NuTDv2wqQAI9FiDKQJjzAdfE2aE&callback=initBaiduMap`;
        script.onerror = () => {
          console.error("百度地图API加载失败");
          this.$message.error("百度地图加载失败，请检查网络连接");
        };
  
        document.head.appendChild(script);
      },
  
      // 创建地图 - 与11.txt完全相同的逻辑
      createMap() {
        try {
          // 创建地图实例
          this.map = new BMap.Map("mapContainer");
          
          // 设置初始中心点和缩放级别 - 南昌市中心
          const point = new BMap.Point(115.8572, 28.6892);
          this.map.centerAndZoom(point, 12);
          
          // 启用滚轮缩放
          this.map.enableScrollWheelZoom(true);
  
          // 添加三个标准控件（与11.txt完全相同）
          this.map.addControl(new BMap.NavigationControl());  // 左上角：缩放控件
          this.map.addControl(new BMap.ScaleControl());       // 左下角：比例尺
          this.map.addControl(new BMap.MapTypeControl());     // 右上角：地图类型控件（普通地图、卫星影像、三维地图）
  
          console.log("百度地图初始化成功，已添加地图类型控件");
  
          // 添加标记点
          this.addLocationMarkers();
        } catch (error) {
          console.error("创建地图失败:", error);
          this.$message.error("地图加载失败: " + error.message);
        }
      },
  
      // 添加关键位置标记
      addLocationMarkers() {
        if (!this.map) return;
  
        // 清除现有标记
        this.clearMarkers();
  
        // 遍历所有位置添加标记
        Object.entries(this.locations).forEach(([name, location]) => {
          try {
            const point = new BMap.Point(location.lng, location.lat);
            const marker = new BMap.Marker(point);
  
            // 创建信息窗口内容
            const infoWindow = new BMap.InfoWindow(`
              <div style="padding: 12px; min-width: 200px;">
                <h4 style="margin: 0 0 8px 0; color: #409EFF;">${name}</h4>
                <p style="margin: 0; color: #666;">经度: ${location.lng.toFixed(4)}</p>
                <p style="margin: 0; color: #666;">纬度: ${location.lat.toFixed(4)}</p>
              </div>
            `);
  
            // 添加点击事件
            marker.addEventListener("click", () => {
              this.map.openInfoWindow(infoWindow, point);
            });
  
            // 添加标记到地图
            this.map.addOverlay(marker);
            this.markers.push(marker);
          } catch (error) {
            console.error("添加标记失败:", error);
          }
        });
      },
  
      // 开始路线规划
      startRoutePlanning() {
        this.isPlanningRoute = true;
        this.routeForm.startLocation = "";
        this.routeForm.endLocation = "";
      },
  
      // 取消路线规划
      cancelRoutePlanning() {
        this.isPlanningRoute = false;
        this.routeForm.startLocation = "";
        this.routeForm.endLocation = "";
        this.planningLoading = false;
      },
  
      // 执行路线规划
      async executeRoutePlanning() {
        if (!this.routeForm.startLocation || !this.routeForm.endLocation) {
          this.$message.warning("请选择起点和终点");
          return;
        }
  
        const startLocation = this.locations[this.routeForm.startLocation];
        const endLocation = this.locations[this.routeForm.endLocation];
  
        if (!startLocation || !endLocation) {
          this.$message.error("选择的位置无效");
          return;
        }
  
        if (this.routeForm.startLocation === this.routeForm.endLocation) {
          this.$message.warning("起点和终点不能相同");
          return;
        }
  
        try {
          this.planningLoading = true;
          this.$message.info("正在规划路线...");
  
          const requestData = {
            startLng: startLocation.lng,
            startLat: startLocation.lat,
            endLng: endLocation.lng,
            endLat: endLocation.lat,
          };
  
          console.log("路线规划请求数据:", requestData);
  
          // 使用 axios 进行请求
          const response = await axios.post("/api/route/plan", requestData);
          console.log("路线规划完整响应:", response);
  
          if (response.data && response.data.success) {
            this.currentRouteData = {
              startLocation: this.routeForm.startLocation,
              endLocation: this.routeForm.endLocation,
              startPoint: {
                name: this.routeForm.startLocation,
                lng: startLocation.lng,
                lat: startLocation.lat,
              },
              endPoint: {
                name: this.routeForm.endLocation,
                lng: endLocation.lng,
                lat: endLocation.lat,
              },
            };
  
            if (response.data.data && response.data.data.path) {
              this.currentRouteInfo = response.data.data;
              // 为路径点添加真实名称
              this.addRealLocationNamesToPath(this.currentRouteInfo.path);
            } else {
              this.currentRouteInfo = this.createDefaultRoute(
                startLocation.lng,
                startLocation.lat,
                endLocation.lng,
                endLocation.lat
              );
            }
  
            this.isPlanningRoute = false;
            this.showRouteDialog = true;
  
            console.log("路径规划成功，准备显示对话框");
            this.$message.success("路线规划完成！");
          } else {
            const errorMsg = response.data.message || "未知错误";
            this.$message.error("路线规划失败：" + errorMsg);
          }
        } catch (error) {
          console.error("路线规划错误:", error);
  
          const startLocation = this.locations[this.routeForm.startLocation];
          const endLocation = this.locations[this.routeForm.endLocation];
  
          if (startLocation && endLocation) {
            this.currentRouteData = {
              startLocation: this.routeForm.startLocation,
              endLocation: this.routeForm.endLocation,
              startPoint: {
                name: this.routeForm.startLocation,
                lng: startLocation.lng,
                lat: startLocation.lat,
              },
              endPoint: {
                name: this.routeForm.endLocation,
                lng: endLocation.lng,
                lat: endLocation.lat,
              },
            };
  
            this.currentRouteInfo = this.createDefaultRoute(
              startLocation.lng,
              startLocation.lat,
              endLocation.lng,
              endLocation.lat
            );
  
            this.isPlanningRoute = false;
            this.showRouteDialog = true;
            this.$message.warning("使用默认路径（服务器响应异常）");
          } else {
            this.$message.error(
              "路线规划请求失败: " + (error.message || "未知错误")
            );
          }
        } finally {
          this.planningLoading = false;
        }
      },
  
      // 为路径点添加真实位置名称
      addRealLocationNamesToPath(path) {
        path.forEach((point, index) => {
          // 查找最近的关键位置
          const nearestLocation = this.findNearestLocation(point.lng, point.lat);
          if (
            nearestLocation &&
            this.calculateDistance(
              point.lng,
              point.lat,
              nearestLocation.lng,
              nearestLocation.lat
            ) < 5000
          ) {
            point.locationName = nearestLocation.name;
          } else {
            // 如果没有找到附近的关键位置，使用通用名称
            if (index === 0) {
              point.locationName = this.currentRouteData.startLocation;
            } else if (index === path.length - 1) {
              point.locationName = this.currentRouteData.endLocation;
            } else {
              point.locationName = `航点 ${index}`;
            }
          }
        });
      },
  
      // 查找最近的关键位置
      findNearestLocation(lng, lat) {
        let nearestLocation = null;
        let minDistance = Infinity;
  
        Object.values(this.locations).forEach((location) => {
          const distance = this.calculateDistance(
            lng,
            lat,
            location.lng,
            location.lat
          );
          if (distance < minDistance) {
            minDistance = distance;
            nearestLocation = location;
          }
        });
  
        return nearestLocation;
      },
  
      // 创建默认路径（回退方案）
      createDefaultRoute(startLng, startLat, endLng, endLat) {
        const path = [];
        const segments = 8;
  
        // 起点
        path.push({
          lng: startLng,
          lat: startLat,
          altitude: 100,
          stopTime: 0,
          locationName: this.routeForm.startLocation,
        });
  
        // 中间点
        for (let i = 1; i < segments; i++) {
          const ratio = i / segments;
          const lng = startLng + (endLng - startLng) * ratio;
          const lat = startLat + (endLat - startLat) * ratio;
  
          // 查找最近的位置名称
          const nearestLocation = this.findNearestLocation(lng, lat);
          const locationName =
            nearestLocation &&
            this.calculateDistance(
              lng,
              lat,
              nearestLocation.lng,
              nearestLocation.lat
            ) < 3000
              ? nearestLocation.name
              : `航点 ${i}`;
  
          path.push({
            lng: lng,
            lat: lat,
            altitude: 100 + i * 10,
            stopTime: 0,
            locationName: locationName,
          });
        }
  
        // 终点
        path.push({
          lng: endLng,
          lat: endLat,
          altitude: 100,
          stopTime: 0,
          locationName: this.routeForm.endLocation,
        });
  
        // 计算距离
        const totalDistance = this.calculateRouteDistance(path);
        const estimatedTime = totalDistance / 8.0;
  
        return {
          path: path,
          totalDistance: totalDistance,
          estimatedTime: estimatedTime,
          waypointCount: path.length,
        };
      },
  
      // 计算路径总距离
      calculateRouteDistance(path) {
        let totalDistance = 0;
        for (let i = 0; i < path.length - 1; i++) {
          const point1 = path[i];
          const point2 = path[i + 1];
          totalDistance += this.calculateDistance(
            point1.lng,
            point1.lat,
            point2.lng,
            point2.lat
          );
        }
        return totalDistance;
      },
  
      // 计算两点间距离
      calculateDistance(lng1, lat1, lng2, lat2) {
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
      },
  
      // 处理使用路径
      handleUseRoute(routeInfo) {
        this.saveRouteToPreset(routeInfo);
        this.drawRouteOnMap(routeInfo.path);
        this.$emit("use-route", routeInfo);
        this.$message.success("路径已保存到预设路线并应用到任务创建");
      },
  
      // 处理在地图上显示路径
      handleShowOnMap(routeInfo) {
        this.drawRouteOnMap(routeInfo.path);
        this.showRouteDialog = false;
        this.$message.success("路径已显示在地图上");
      },
  
      // 保存路径到预设路线
      saveRouteToPreset(routeInfo) {
        const presetRoute = {
          id: Date.now(),
          name: `${this.currentRouteData.startLocation} 到 ${this.currentRouteData.endLocation}`,
          ...routeInfo,
          startLocation: this.currentRouteData.startLocation,
          endLocation: this.currentRouteData.endLocation,
          createdAt: new Date().toISOString(),
        };
  
        this.$emit("route-saved", presetRoute);
        console.log("路径已保存到预设路线:", presetRoute);
      },
  
      // 在地图上绘制路径
      drawRouteOnMap(path) {
        if (!this.map || !path || path.length === 0) return;
  
        // 清除之前的路径和标记
        this.clearRouteFromMap();
  
        // 创建路径点
        const points = path.map((point) => new BMap.Point(point.lng, point.lat));
  
        // 创建折线
        this.routePolyline = new BMap.Polyline(points, {
          strokeColor: "#409EFF",
          strokeWeight: 4,
          strokeOpacity: 0.8,
          strokeStyle: "solid",
        });
  
        // 添加路径到地图
        this.map.addOverlay(this.routePolyline);
  
        // 添加路径标记点
        path.forEach((point, index) => {
          const markerPoint = new BMap.Point(point.lng, point.lat);
          const marker = new BMap.Marker(markerPoint);
  
          // 信息窗口
          const infoWindow = new BMap.InfoWindow(`
              <div style="padding: 12px; min-width: 200px;">
                <h4 style="margin: 0 0 8px 0; color: #409EFF;">${
                  point.locationName || `航点 ${index}`
                }</h4>
                <p style="margin: 0; color: #666;">经度: ${point.lng.toFixed(
                  4
                )}</p>
                <p style="margin: 0; color: #666;">纬度: ${point.lat.toFixed(
                  4
                )}</p>
                <p style="margin: 0; color: #666;">高度: ${
                  point.altitude || 100
                }米</p>
              </div>
            `);
  
          marker.addEventListener("click", () => {
            this.map.openInfoWindow(infoWindow, markerPoint);
          });
  
          this.map.addOverlay(marker);
          this.routeMarkers.push(marker);
        });
  
        // 调整地图视野以包含整个路径
        this.map.setViewport(points);
      },
  
      // 清除地图上的路径
      clearRouteFromMap() {
        if (this.routePolyline) {
          this.map.removeOverlay(this.routePolyline);
          this.routePolyline = null;
        }
  
        this.routeMarkers.forEach((marker) => {
          this.map.removeOverlay(marker);
        });
        this.routeMarkers = [];
      },
  
      // 清除所有标记
      clearMarkers() {
        this.markers.forEach((marker) => {
          try {
            this.map.removeOverlay(marker);
          } catch (error) {
            console.error("清除标记错误:", error);
          }
        });
        this.markers = [];
      },
  
      // 处理对话框关闭
      handleRouteDialogClose() {
        this.showRouteDialog = false;
      },
  
      // 重置地图视图
      resetMap() {
        if (this.map) {
          const point = new BMap.Point(115.8572, 28.6892);
          this.map.centerAndZoom(point, 12);
          this.clearRouteFromMap();
          this.addLocationMarkers();
        }
      },
    },
  };
  </script>
  
  <style scoped>
  .map-container {
    position: relative;
    width: 100%;
    height: 600px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    background-color: #fff;
  }
  
  .map {
    width: 100%;
    height: 100%;
    background-color: #f5f7fa;
  }
  
  /* 路线规划面板样式 */
  .route-planning-panel {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 1000;
    width: 300px;
  }
  
  .planning-card {
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    background: #fff;
    border: 1px solid #ebeef5;
  }
  
  /* 地图控制按钮 - 现在在右边中间 */
  .map-controls {
    position: absolute;
    top: 50%;               /* 改为垂直居中 */
    right: 20px;            /* 保持右边距不变 */
    transform: translateY(-50%); /* 使用 transform 实现精确垂直居中 */
    z-index: 1000;
    display: flex;
    flex-direction: column;
    gap: 10px;
    background: #fff;
    padding: 12px;
    border-radius: 6px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid #ebeef5;
  }
  
  .map-controls .el-button {
    background: #fff;
    color: #606266;
    border: 1px solid #dcdfe6;
    padding: 8px 15px;
    border-radius: 4px;
    transition: all 0.3s;
    white-space: nowrap;
  }
  
  .map-controls .el-button:hover {
    background: #f5f7fa;
    border-color: #c0c4cc;
    color: #409EFF;
  }
  
  .control-btn {
    background: #409EFF !important;
    border-color: #409EFF !important;
    color: #fff !important;
  }
  
  .control-btn:hover {
    background: #66b1ff !important;
    border-color: #66b1ff !important;
    color: #fff !important;
  }
  </style>