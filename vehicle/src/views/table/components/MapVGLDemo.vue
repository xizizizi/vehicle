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

          <!-- 无人机选择 -->
          <el-form-item label="选择无人机">
            <el-select
              v-model="selectedUavId"
              placeholder="请选择无人机"
              style="width: 100%"
              @change="onUavSelected"
              popper-append-to-body
            >
              <el-option
                v-for="uav in uavList"
                :key="uav.id"
                :label="`${uav.sn} (${uav.model})`"
                :value="uav.id"
              >
                <span style="float: left">{{ uav.sn }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  电量 {{ uav.batteryLevel }}% | {{ uav.status }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 无人机详情 -->
          <el-card
            v-if="selectedUav"
            class="uav-detail-card"
            style="margin-top: 10px"
          >
            <div slot="header">
              <span>无人机详情</span>
            </div>
            <div class="uav-detail">
              <p><span>序列号:</span> {{ selectedUav.sn }}</p>
              <p><span>型号:</span> {{ selectedUav.model }}</p>
              <p><span>状态:</span> {{ selectedUav.status }}</p>
              <p><span>电量:</span> {{ selectedUav.batteryLevel }}%</p>
              <p><span>载重:</span> {{ selectedUav.loadCapacity }} kg</p>
              <p>
                <span>当前位置:</span>
                {{
                  selectedUav.currentLng
                    ? selectedUav.currentLng.toFixed(4)
                    : ""
                }},
                {{
                  selectedUav.currentLat
                    ? selectedUav.currentLat.toFixed(4)
                    : ""
                }}
              </p>
            </div>
          </el-card>

          <el-form-item>
            <el-button
              type="primary"
              @click="executeRoutePlanning"
              style="width: 100%"
              :loading="planningLoading"
              :disabled="
                !routeForm.startLocation ||
                !routeForm.endLocation ||
                !selectedUavId
              "
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
        // 医疗设施
        江西省人民医院: {
          lng: 115.879606,
          lat: 28.711791,
          name: "江西省人民医院",
        },
        南昌大学第一附属医院: {
          lng: 115.868863,
          lat: 28.597035,
          name: "南昌大学第一附属医院",
        },
        南昌大学第二附属医院: {
          lng: 115.842114,
          lat: 28.667088,
          name: "南昌大学第二附属医院",
        },
        南昌市第一医院: {
          lng: 115.896668,
          lat: 28.684614,
          name: "南昌市第一医院",
        },
        南昌市洪都中医院: {
          lng: 115.899238,
          lat: 28.683892,
          name: "南昌市洪都中医院",
        },
        江西省儿童医院: {
          lng: 115.9023,
          lat: 28.693298,
          name: "江西省儿童医院",
        },
        "江西省妇幼保健院(九龙湖院区)": {
          lng: 115.810491,
          lat: 28.627024,
          name: "江西省妇幼保健院(九龙湖院区)",
        },

        // 教育机构
        南昌大学: { lng: 115.807177, lat: 28.661912, name: "南昌大学" },
        江西师范大学: { lng: 116.0381, lat: 28.685261, name: "江西师范大学" },
        华东交通大学: { lng: 115.876787, lat: 28.747183, name: "华东交通大学" },
        南昌航空大学: { lng: 115.834196, lat: 28.656103, name: "南昌航空大学" },
        南昌市第一中学: {
          lng: 115.876291,
          lat: 28.629699,
          name: "南昌市第一中学",
        },
        南昌市第二中学: {
          lng: 115.859801,
          lat: 28.705898,
          name: "南昌市第二中学",
        },

        // 交通设施
        南昌西站: { lng: 115.799611, lat: 28.628861, name: "南昌西站" },
        南昌站: { lng: 115.926112, lat: 28.66832, name: "南昌站" },
        昌北机场: { lng: 115.918639, lat: 28.864072, name: "昌北机场" },
        南昌东站: { lng: 116.027898, lat: 28.619678, name: "南昌东站" },
        徐坊客运站: { lng: 115.889, lat: 28.663, name: "徐坊客运站" },

        // 景点
        八一广场: { lng: 115.910978, lat: 28.680175, name: "八一广场" },
        滕王阁: { lng: 115.887051, lat: 28.686511, name: "滕王阁" },
        秋水广场: { lng: 115.869658, lat: 28.688643, name: "秋水广场" },
        八一公园: { lng: 115.903784, lat: 28.684104, name: "八一公园" },

        // 商业
        红谷滩万达广场: {
          lng: 115.855417,
          lat: 28.698629,
          name: "红谷滩万达广场",
        },
        天虹商场: { lng: 116.027845, lat: 28.680401, name: "天虹商场" },
        中山路商业街: { lng: 115.898978, lat: 28.681788, name: "中山路商业街" },

        // 政府机构
        南昌市公安局: { lng: 115.867836, lat: 28.706614, name: "南昌市公安局" },

        // 原有地点
        江西省博物馆: { lng: 115.888391, lat: 28.711774, name: "江西省博物馆" },
        南昌八一起义纪念馆: {
          lng: 115.895908,
          lat: 28.680744,
          name: "南昌八一起义纪念馆",
        },
        梅岭国家森林公园: {
          lng: 115.732622,
          lat: 28.775951,
          name: "梅岭国家森林公园",
        },
      },

      // 无人机列表
      uavList: [],
      selectedUavId: null,
      selectedUav: null,

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
    this.fetchUavList();
  },
  beforeDestroy() {
    if (this.map) {
      this.clearMarkers();
      this.clearRouteFromMap();
    }
  },
  methods: {
    // 初始化百度地图
    initBaiduMap() {
      if (typeof BMap !== "undefined") {
        this.isMapLoaded = true;
        this.createMap();
        return;
      }

      window.initBaiduMap = () => {
        this.isMapLoaded = true;
        this.createMap();
      };

      const script = document.createElement("script");
      script.type = "text/javascript";
      script.src = `https://api.map.baidu.com/api?v=3.0&ak=B1d61NuTDv2wqQAI9FiDKQJjzAdfE2aE&callback=initBaiduMap`;
      script.onerror = () => {
        console.error("百度地图API加载失败");
        this.$message.error("百度地图加载失败，请检查网络连接");
      };
      document.head.appendChild(script);
    },

    // 创建地图
    createMap() {
      try {
        this.map = new BMap.Map("mapContainer");
        const point = new BMap.Point(115.8572, 28.6892);
        this.map.centerAndZoom(point, 12);
        this.map.enableScrollWheelZoom(true);
        this.map.addControl(new BMap.NavigationControl());
        this.map.addControl(new BMap.ScaleControl());
        this.map.addControl(new BMap.MapTypeControl());
        console.log("百度地图初始化成功，已添加地图类型控件");
        this.addLocationMarkers();
      } catch (error) {
        console.error("创建地图失败:", error);
        this.$message.error("地图加载失败: " + error.message);
      }
    },

    // 添加关键位置标记
    addLocationMarkers() {
      if (!this.map) return;
      this.clearMarkers();

      Object.entries(this.locations).forEach(([name, location]) => {
        try {
          const point = new BMap.Point(location.lng, location.lat);
          const marker = new BMap.Marker(point);
          const infoWindow = new BMap.InfoWindow(`
                <div style="padding: 12px; min-width: 200px;">
                  <h4 style="margin: 0 0 8px 0; color: #409EFF;">${name}</h4>
                  <p style="margin: 0; color: #666;">经度: ${location.lng.toFixed(
                    4
                  )}</p>
                  <p style="margin: 0; color: #666;">纬度: ${location.lat.toFixed(
                    4
                  )}</p>
                </div>
              `);
          marker.addEventListener("click", () => {
            this.map.openInfoWindow(infoWindow, point);
          });
          this.map.addOverlay(marker);
          this.markers.push(marker);
        } catch (error) {
          console.error("添加标记失败:", error);
        }
      });
    },

    // 获取无人机列表
    async fetchUavList() {
      try {
        const response = await axios.get("/api/uavs");
        if (response.data.success) {
          this.uavList = response.data.data;
        }
      } catch (error) {
        console.error("获取无人机列表失败", error);
        this.$message.error("获取无人机列表失败");
      }
    },

    // 无人机选中事件
    onUavSelected(uavId) {
      this.selectedUav = this.uavList.find((u) => u.id === uavId);
    },

    // 开始路线规划
    startRoutePlanning() {
      this.isPlanningRoute = true;
      this.routeForm.startLocation = "";
      this.routeForm.endLocation = "";
      this.selectedUavId = null;
      this.selectedUav = null;
    },

    // 取消路线规划
    cancelRoutePlanning() {
      this.isPlanningRoute = false;
      this.routeForm.startLocation = "";
      this.routeForm.endLocation = "";
      this.selectedUavId = null;
      this.selectedUav = null;
    },

    // 执行路线规划
    async executeRoutePlanning() {
      if (!this.routeForm.startLocation || !this.routeForm.endLocation) {
        this.$message.warning("请选择起点和终点");
        return;
      }
      if (!this.selectedUavId) {
        this.$message.warning("请选择无人机");
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
          uavId: this.selectedUavId,
        };

        console.log("路线规划请求数据:", requestData);
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

      path.push({
        lng: startLng,
        lat: startLat,
        altitude: 100,
        stopTime: 0,
        locationName: this.routeForm.startLocation,
      });

      for (let i = 1; i < segments; i++) {
        const ratio = i / segments;
        const lng = startLng + (endLng - startLng) * ratio;
        const lat = startLat + (endLat - startLat) * ratio;

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

      path.push({
        lng: endLng,
        lat: endLat,
        altitude: 100,
        stopTime: 0,
        locationName: this.routeForm.endLocation,
      });

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

    // 计算两点间距离（米）
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
      this.clearRouteFromMap();

      const points = path.map((point) => new BMap.Point(point.lng, point.lat));

      this.routePolyline = new BMap.Polyline(points, {
        strokeColor: "#409EFF",
        strokeWeight: 4,
        strokeOpacity: 0.8,
        strokeStyle: "solid",
      });
      this.map.addOverlay(this.routePolyline);

      path.forEach((point, index) => {
        const markerPoint = new BMap.Point(point.lng, point.lat);
        const marker = new BMap.Marker(markerPoint);
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
  width: 450px;
}

.planning-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background: #fff;
  border: 1px solid #ebeef5;
}

/* 无人机详情卡片 */
.uav-detail-card {
  background: #f9fafc;
  border: 1px solid #e4e7ed;
  margin-top: 10px;
}
.uav-detail p {
  margin: 5px 0;
  color: #606266;
  display: flex;
  justify-content: space-between;
  font-size: 10px; /* 新增 */
}
.uav-detail p span:first-child {
  color: #909399;
  font-size: 10px; /* 新增 */
}

/* 地图控制按钮 - 右边中间 */
.map-controls {
  position: absolute;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
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
  color: #409eff;
}

.control-btn {
  background: #409eff !important;
  border-color: #409eff !important;
  color: #fff !important;
}

.control-btn:hover {
  background: #66b1ff !important;
  border-color: #66b1ff !important;
  color: #fff !important;
}
/* 确保下拉菜单不被地图覆盖 */
.el-select-dropdown {
  z-index: 3000 !important;
}

/* 确保下拉菜单有足够高度且可滚动 */
.el-select-dropdown__list {
  max-height: 300px !important;
  overflow-y: auto !important;
}

/* 如果父容器有overflow限制，确保下拉菜单能突破 */
.route-planning-panel {
  overflow: visible !important;
}
</style>