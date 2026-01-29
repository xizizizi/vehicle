<template>
  <el-card class="panel-card map-panel">
    <div slot="header" class="panel-header">
      <span><i class="el-icon-map-location"></i> 南昌市无人机监控地图</span>
      <div class="map-controls">
        <el-button
          :type="showUavs ? 'success' : 'default'"
          size="mini"
          @click="toggleUavs"
          class="control-btn"
        >
          {{ showUavs ? "隐藏无人机" : "显示无人机" }}
        </el-button>
        <el-button type="text" icon="el-icon-refresh" @click="resetMap">
          重置地图
        </el-button>
      </div>
    </div>

    <!-- 地图显示区域 -->
    <div class="map-display-area">
      <!-- 百度地图容器 -->
      <div id="baidu-map" class="map-container">
        <div v-if="!mapLoaded" class="map-loading">
          <i class="el-icon-loading"></i>
          <p>地图加载中...</p>
          <el-button type="text" @click="initMap">重试加载地图</el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>
  
  <script>
export default {
  name: "MapPanel",
  props: {
    uavs: {
      type: Array,
      default: () => [],
    },
    locations: {
      type: Object,
      default: () => ({}),
    },
    missions: {
      type: Array,
      default: () => [],
    },
    orders: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      // 地图相关
      mapLoaded: false,
      map: null,
      uavMarkers: {},
      locationMarkers: {},
      routePolylines: [],
      uavTracks: {},

      // 显示控制
      showUavs: true, // 控制无人机显示状态

      // 百度地图API配置
      baiduMapAK: "t12Jbls6aC4P07QJ98Z7Ke0e4lbUpnxX", // 需要替换为实际的AK
    };
  },
  watch: {
    uavs: {
      handler(newUavs) {
        if (this.mapLoaded) {
          this.renderUavs();
        }
      },
      deep: true,
    },
  },
  mounted() {
    this.loadBaiduMapAPI();
  },
  methods: {
    // 加载百度地图API
    loadBaiduMapAPI() {
      if (window.BMap) {
        this.initMap();
        return;
      }

      const script = document.createElement("script");
      script.type = "text/javascript";
      script.src = `https://api.map.baidu.com/api?v=3.0&ak=${this.baiduMapAK}&callback=baiduMapInit`;
      script.onerror = () => {
        console.error("百度地图API加载失败");
        this.$message.error("百度地图加载失败，请检查网络连接");
      };

      window.baiduMapInit = () => {
        this.initMap();
      };

      document.head.appendChild(script);
    },

    // 在 initMap 方法中，在地图初始化后添加以下代码
    initMap() {
      if (!window.BMap) {
        console.warn("百度地图API未加载，等待加载...");
        setTimeout(() => {
          this.initMap();
        }, 500);
        return;
      }

      try {
        this.map = new BMap.Map("baidu-map");
        const point = new BMap.Point(115.857, 28.683); // 南昌市中心坐标
        this.map.centerAndZoom(point, 12);
        this.map.enableScrollWheelZoom(true);

        // 添加地图控件
        this.map.addControl(new BMap.NavigationControl());
        this.map.addControl(new BMap.ScaleControl());
        this.map.addControl(new BMap.MapTypeControl());

        // 关键修改：在地图控件添加后设置默认地图类型
        // 使用 setTimeout 确保控件初始化完成
        setTimeout(() => {
          // 方法1：尝试使用卫星图常量
          if (window.BMAP_SATELLITE_MAP) {
            this.map.setMapType(BMAP_SATELLITE_MAP);
          }
          // 方法2：如果常量不可用，使用数字2
          else {
            try {
              this.map.setMapType(2);
            } catch (e) {
              console.warn("设置卫星图失败:", e);
            }
          }
        }, 100);

        this.mapLoaded = true;
        this.addLocationMarkers();
        this.renderUavs();
        this.renderMissionRoutes();
      } catch (error) {
        console.error("地图初始化失败:", error);
        this.$message.error("地图加载失败: " + error.message);
      }
    },

    // 同样修改 resetMap 方法
    resetMap() {
      if (this.map) {
        const point = new BMap.Point(115.857, 28.683);
        this.map.centerAndZoom(point, 12);

        // 重置时也设置卫星图
        setTimeout(() => {
          if (window.BMAP_SATELLITE_MAP) {
            this.map.setMapType(BMAP_SATELLITE_MAP);
          } else {
            try {
              this.map.setMapType(2);
            } catch (e) {
              console.warn("重置卫星图失败:", e);
            }
          }
        }, 100);

        this.$message.success("地图已重置到初始视图");
      }
      this.$emit("refresh-map");
    },

    // 添加物流站点标记
    addLocationMarkers() {
      console.log("开始添加站点标记，当前locations:", this.locations);

      if (!this.map) {
        console.error("地图未初始化");
        return;
      }

      if (!this.locations || Object.keys(this.locations).length === 0) {
        console.error("locations数据为空");
        return;
      }

      // 清除现有标记
      Object.values(this.locationMarkers).forEach(({ marker, label }) => {
        if (marker) this.map.removeOverlay(marker);
        if (label) this.map.removeOverlay(label);
      });
      this.locationMarkers = {};

      // 遍历所有站点
      Object.entries(this.locations).forEach(([name, location]) => {
        try {
          console.log(`添加站点: ${name}`, location);

          const point = new BMap.Point(location.lng, location.lat);

          // 创建标记
          const marker = new BMap.Marker(point);

          // 创建信息窗口
          const infoWindow = new BMap.InfoWindow(`
              <div style="padding: 12px; min-width: 200px;">
                <h4 style="margin: 0 0 8px 0; color: #1890ff;">${name}</h4>
                <p style="margin: 0; color: #666;">经度: ${location.lng.toFixed(
                  4
                )}</p>
                <p style="margin: 0; color: #666;">纬度: ${location.lat.toFixed(
                  4
                )}</p>
                <p style="margin: 8px 0 0 0; color: #52c41a;">
                  <i class="el-icon-location"></i> 物流站点
                </p>
              </div>
            `);

          marker.addEventListener("click", () => {
            this.map.openInfoWindow(infoWindow, point);
          });

          // 添加标签
          const label = new BMap.Label(name, {
            position: point,
            offset: new BMap.Size(0, -35),
          });
          label.setStyle({
            color: "#fff",
            backgroundColor: "#1890ff",
            border: "none",
            borderRadius: "8px",
            padding: "3px 10px",
            fontSize: "12px",
            fontWeight: "bold",
          });

          this.map.addOverlay(marker);
          this.map.addOverlay(label);
          this.locationMarkers[name] = { marker, label };

          console.log(`站点 ${name} 添加成功`);
        } catch (error) {
          console.error(`添加站点 ${name} 时出错:`, error);
        }
      });

      console.log(
        `站点标记添加完成，总数: ${Object.keys(this.locationMarkers).length}`
      );
    },

    // 渲染无人机
    renderUavs() {
      if (!this.map || this.uavs.length === 0) return;

      // 清除现有无人机标记
      Object.values(this.uavMarkers).forEach(({ marker, label }) => {
        this.map.removeOverlay(marker);
        this.map.removeOverlay(label);
      });
      this.uavMarkers = {};

      this.uavs.forEach((uav) => {
        if (uav.currentLng && uav.currentLat) {
          const point = new BMap.Point(uav.currentLng, uav.currentLat);
          this.createUavMarker(uav, point);
        }
      });
    },

    // 创建无人机标记
    createUavMarker(uav, point) {
      try {
        console.log(`创建无人机标记: ${uav.sn}`);

        // 直接使用默认标记
        const marker = new BMap.Marker(point);

        // 信息窗口内容
        const infoWindow = new BMap.InfoWindow(`
            <div style="padding: 16px; min-width: 240px;">
              <h4 style="margin: 0 0 12px 0; color: #1890ff; border-bottom: 1px solid #eee; padding-bottom: 8px;">
                ${uav.sn}
              </h4>
              <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px; font-size: 13px;">
                <span style="color: #666;">状态:</span>
                <span style="color: ${this.getUavStatusColor(
                  uav.status
                )}; font-weight: bold;">${this.getStatusText(uav.status)}</span>
                <span style="color: #666;">型号:</span>
                <span>${uav.model || "未知"}</span>
                <span style="color: #666;">电量:</span>
                <span>
                  ${uav.batteryLevel}%
                  <div style="display: inline-block; width: 40px; height: 6px; background: #f0f0f0; border-radius: 3px; margin-left: 8px; vertical-align: middle;">
                    <div style="height: 100%; background: ${this.getBatteryColor(
                      uav.batteryLevel
                    )}; border-radius: 3px; width: ${uav.batteryLevel}%;"></div>
                  </div>
                </span>
                <span style="color: #666;">任务:</span>
                <span>${uav.currentMission || "无任务"}</span>
                <span style="color: #666;">位置:</span>
                <span>${uav.currentLng.toFixed(4)}, ${uav.currentLat.toFixed(
          4
        )}</span>
              </div>
            </div>
          `);

        marker.addEventListener("click", () => {
          this.map.openInfoWindow(infoWindow, point);
        });

        // 添加无人机编号标签
        const statusColor = this.getUavStatusColor(uav.status);
        const label = new BMap.Label(uav.sn, {
          position: point,
          offset: new BMap.Size(0, 20),
        });
        label.setStyle({
          color: "#fff",
          backgroundColor: statusColor,
          border: "none",
          borderRadius: "8px",
          padding: "2px 8px",
          fontSize: "11px",
          fontWeight: "bold",
        });

        this.map.addOverlay(marker);
        this.map.addOverlay(label);
        this.uavMarkers[uav.sn] = { marker, label };
      } catch (error) {
        console.error(`创建无人机标记 ${uav.sn} 时出错:`, error);
      }
    },

    // 渲染任务路线
    renderMissionRoutes() {
      // 清除现有路线
      this.routePolylines.forEach((polyline) => {
        this.map.removeOverlay(polyline);
      });
      this.routePolylines = [];

      // 渲染任务路线
      this.missions.forEach((mission) => {
        if (mission.routePoints && mission.routePoints.length > 1) {
          this.drawMissionRoute(mission);
        }
      });

      // 渲染订单路线
      this.orders.forEach((order) => {
        if (order.status === "DELIVERING" || order.status === "ASSIGNED") {
          this.drawOrderRoute(order);
        }
      });
    },

    // 绘制任务路线
    drawMissionRoute(mission) {
      const points = mission.routePoints.map(
        (point) => new BMap.Point(point.lng, point.lat)
      );

      const polyline = new BMap.Polyline(points, {
        strokeColor: "#1890ff",
        strokeWeight: 4,
        strokeOpacity: 0.7,
        strokeStyle: "solid",
      });

      this.map.addOverlay(polyline);
      this.routePolylines.push(polyline);

      // 添加起点终点标记
      if (points.length > 0) {
        this.addRouteEndpoint(points[0], "起点", "#52c41a");
        this.addRouteEndpoint(points[points.length - 1], "终点", "#fa541c");
      }
    },

    // 绘制订单路线
    drawOrderRoute(order) {
      const startPoint = new BMap.Point(order.pickupLng, order.pickupLat);
      const endPoint = new BMap.Point(order.deliveryLng, order.deliveryLat);

      const polyline = new BMap.Polyline([startPoint, endPoint], {
        strokeColor: "#faad14",
        strokeWeight: 3,
        strokeOpacity: 0.7,
        strokeStyle: "dashed",
      });

      this.map.addOverlay(polyline);
      this.routePolylines.push(polyline);

      this.addRouteEndpoint(startPoint, "取件点", "#faad14");
      this.addRouteEndpoint(endPoint, "送件点", "#722ed1");
    },

    // 添加路线端点标记
    addRouteEndpoint(point, text, color) {
      const marker = new BMap.Marker(point);
      const label = new BMap.Label(text, {
        position: point,
        offset: new BMap.Size(0, -25),
      });
      label.setStyle({
        color: "#fff",
        backgroundColor: color,
        border: "none",
        borderRadius: "10px",
        padding: "2px 8px",
        fontSize: "11px",
      });

      this.map.addOverlay(marker);
      this.map.addOverlay(label);
    },

    // 切换无人机显示/隐藏
    toggleUavs() {
      this.showUavs = !this.showUavs;
      this.toggleUavsDisplay(this.showUavs);
    },

    // 控制无人机显示
    toggleUavsDisplay(show) {
      Object.values(this.uavMarkers).forEach(({ marker, label }) => {
        if (show) {
          this.map.addOverlay(marker);
          this.map.addOverlay(label);
        } else {
          this.map.removeOverlay(marker);
          this.map.removeOverlay(label);
        }
      });
    },

    // 获取无人机状态颜色
    getUavStatusColor(status) {
      const colors = {
        IDLE: "#52c41a",
        ON_MISSION: "#1890ff",
        CRUISING: "#722ed1",
        DELIVERING: "#faad14",
        CHARGING: "#fa8c16",
        ERROR: "#f5222d",
      };
      return colors[status] || "#666";
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        IDLE: "空闲",
        ON_MISSION: "任务中",
        CRUISING: "巡航中",
        DELIVERING: "配送中",
        CHARGING: "充电中",
        ERROR: "故障",
      };
      return statusMap[status] || status;
    },

    // 获取电池颜色
    getBatteryColor(percentage) {
      if (percentage > 70) return "#52c41a";
      if (percentage > 30) return "#faad14";
      return "#f5222d";
    },
  },

  beforeDestroy() {
    delete window.baiduMapInit;
  },
};
</script>
  
  <style scoped>
.panel-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.map-display-area {
  flex: 1;
  position: relative;
}

.map-container {
  width: 100%;
  height: 100%;
  min-height: 600px;
  border-radius: 4px;
  overflow: hidden;
}

.map-loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #666;
  background: #f5f5f5;
}

/* 控制按钮样式 */
.control-btn {
  min-width: 90px;
}
</style>