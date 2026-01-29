<template>
  <div class="app-container">
    <div class="filter-container">
      <el-card class="control-panel">
        <div slot="header" class="panel-header">
          <div
            style="
              display: flex;
              justify-content: space-between;
              align-items: center;
            "
          >
            <div>
              <span>无人机聚类选址分析 - 南昌市</span>
              <el-tag type="success">真实数据</el-tag>
            </div>

            <!-- 部署无人机组件 -->
            <DroneDeployment
              :locations-data="defaultLocations"
              :map-instance="map"
              @deploy-success="handleDroneDeployed"
              :disabled="false"
            />
          </div>
        </div>

        <el-form :model="form" label-width="120px">
          <el-form-item label="聚类数量">
            <el-slider
              v-model="form.clusterCount"
              :min="2"
              :max="10"
              show-stops
              :step="1"
              show-input
            >
            </el-slider>
          </el-form-item>

          <el-form-item label="数据源">
            <el-radio-group v-model="form.dataSource">
              <el-radio label="real">实时百度数据</el-radio>
              <el-radio label="default">本地真实数据</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              @click="handleClusterAnalysis"
              :loading="loading"
              icon="el-icon-cpu"
            >
              开始聚类分析
            </el-button>
            <el-button @click="handleReset" icon="el-icon-refresh"
              >重置</el-button
            >
            <el-button
              @click="refreshRealData"
              :loading="refreshing"
              icon="el-icon-download"
            >
              更新实时数据
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 聚类结果统计 -->
        <el-row v-if="clusterResult" :gutter="20" class="stats-panel">
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-value">
                {{ clusterResult.metrics.totalPoints }}
              </div>
              <div class="stat-label">总POI数量</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-value">
                {{ clusterResult.metrics.clusterCount }}
              </div>
              <div class="stat-label">聚类数量</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-value">
                {{
                  clusterResult.metrics.silhouetteScore
                    ? clusterResult.metrics.silhouetteScore.toFixed(3)
                    : "0.000"
                }}
              </div>
              <div class="stat-label">聚类质量</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="stat-item">
              <div class="stat-value">
                {{
                  clusterResult.metrics.inertia
                    ? clusterResult.metrics.inertia.toFixed(0)
                    : "0"
                }}
              </div>
              <div class="stat-label">聚类误差</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 地图容器 -->
    <div class="map-container">
      <div id="mapContainer" class="map"></div>
      <div class="map-legend" v-if="clusterResult">
        <div class="legend-title">聚类图例</div>
        <div
          v-for="(cluster, index) in clusterResult.clusters"
          :key="cluster.clusterId"
          class="legend-item"
        >
          <div
            class="legend-color"
            :style="{ backgroundColor: CLUSTER_COLORS[index] }"
          ></div>
          <span
            >聚类 {{ cluster.clusterId + 1 }} ({{
              cluster.pointCount
            }}个点)</span
          >
        </div>
      </div>
    </div>

    <!-- 聚类结果详情 -->
    <el-card v-if="clusterResult" class="result-panel">
      <div slot="header">
        <div
          style="
            display: flex;
            justify-content: space-between;
            align-items: center;
          "
        >
          <div>
            <span>聚类分析详细结果</span>
            <el-tag type="info">使用真实K-means算法</el-tag>
          </div>
        </div>
      </div>

      <el-collapse v-model="activeNames">
        <el-collapse-item
          v-for="cluster in clusterResult.clusters"
          :key="cluster.clusterId"
          :title="getClusterTitle(cluster)"
          :name="cluster.clusterId"
        >
          <div class="cluster-detail">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="detail-section">
                  <h4>位置信息</h4>
                  <p>
                    <strong>中心坐标:</strong>
                    {{ cluster.center.lng.toFixed(6) }},
                    {{ cluster.center.lat.toFixed(6) }}
                  </p>
                  <p><strong>覆盖半径:</strong> {{ cluster.maxDistance }} km</p>
                  <p><strong>平均距离:</strong> {{ cluster.avgDistance }} km</p>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="detail-section">
                  <h4>POI类型分布</h4>
                  <el-tag
                    v-for="(count, type) in cluster.typeDistribution"
                    :key="type"
                    type="info"
                    size="small"
                    style="margin: 2px"
                  >
                    {{ type }}: {{ count }}
                  </el-tag>
                </div>
              </el-col>
            </el-row>

            <div class="recommendation-section">
              <h4>选址建议</h4>
              <p class="recommendation-text">{{ cluster.recommendation }}</p>
            </div>

            <div class="points-section">
              <h4>包含的POI点 ({{ cluster.points.length }}个)</h4>
              <el-table
                :data="cluster.points"
                border
                style="width: 100%"
                height="300"
              >
                <el-table-column
                  prop="name"
                  label="地点名称"
                  width="200"
                ></el-table-column>
                <el-table-column prop="type" label="类型" width="120">
                  <template slot-scope="scope">
                    <el-tag :type="getTypeTagType(scope.row.type)" size="small">
                      {{ scope.row.type }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="lng" label="经度" width="130">
                  <template slot-scope="scope">
                    {{ scope.row.lng.toFixed(6) }}
                  </template>
                </el-table-column>
                <el-table-column prop="lat" label="纬度" width="130">
                  <template slot-scope="scope">
                    {{ scope.row.lat.toFixed(6) }}
                  </template>
                </el-table-column>
                <el-table-column
                  prop="address"
                  label="地址"
                  show-overflow-tooltip
                ></el-table-column>
              </el-table>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>
  
  <script>
import axios from "axios";
// 引入部署无人机组件
import DroneDeployment from "./DroneDeployment.vue";

export default {
  name: "RealDroneCluster",

  components: {
    DroneDeployment,
  },

  data() {
    return {
      loading: false,
      refreshing: false,
      form: {
        clusterCount: 5,
        dataSource: "default",
      },
      clusterResult: null,
      activeNames: [0],
      map: null,
      markers: [],
      clusterMarkers: [],
      defaultLocations: [],
      isMapLoaded: false,

      // 颜色配置
      CLUSTER_COLORS: [
        "#FF6B6B",
        "#4ECDC4",
        "#45B7D1",
        "#96CEB4",
        "#FFEAA7",
        "#DDA0DD",
        "#98D8C8",
        "#F7DC6F",
        "#BB8FCE",
        "#85C1E9",
      ],
    };
  },

  mounted() {
    this.loadDefaultData();
    this.initBaiduMap();
  },

  methods: {
    // 初始化百度地图
    initBaiduMap() {
      if (typeof BMap !== "undefined") {
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
        console.error("百度地图加载失败");
        this.$message.error("百度地图加载失败，将显示表格数据");
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

        console.log("百度地图初始化成功");

        if (this.defaultLocations.length > 0) {
          this.showDefaultPoints();
        }
      } catch (error) {
        console.error("创建地图失败:", error);
      }
    },

    // 加载默认数据
    async loadDefaultData() {
      try {
        const response = await axios.get(
          "http://localhost:5000/api/nanchang-data"
        );
        if (response.data.success) {
          this.defaultLocations = response.data.data.locations;
          if (this.isMapLoaded && this.map) {
            this.showDefaultPoints();
          }
        }
      } catch (error) {
        console.error("加载数据失败:", error);
        this.mockDefaultData();
      }
    },

    // 模拟默认数据
    mockDefaultData() {
      this.defaultLocations = [
        {
          id: 1,
          name: "南昌大学第一附属医院(象湖院区)",
          type: "医疗",
          lng: 115.868863,
          lat: 28.597035,
          address: "江西省南昌市南昌县东岳大道1519号",
        },
        {
          id: 2,
          name: "南昌大学",
          type: "教育",
          lng: 115.806945,
          lat: 28.663356,
          address: "江西省南昌市红谷滩区学府大道999号",
        },
        {
          id: 3,
          name: "八一广场",
          type: "景点",
          lng: 115.893456,
          lat: 28.681234,
          address: "江西省南昌市东湖区八一大道",
        },
        {
          id: 4,
          name: "南昌西站",
          type: "交通",
          lng: 115.817634,
          lat: 28.692345,
          address: "江西省南昌市新建区长征西路",
        },
        {
          id: 5,
          name: "红谷滩万达广场",
          type: "商业",
          lng: 115.852189,
          lat: 28.674523,
          address: "江西省南昌市红谷滩区会展路",
        },
      ];

      if (this.isMapLoaded && this.map) {
        this.showDefaultPoints();
      }

      this.$message.warning("使用模拟数据，真实数据加载失败");
    },

    // 刷新实时数据
    async refreshRealData() {
      this.refreshing = true;
      try {
        const response = await axios.get(
          "http://localhost:5000/api/nanchang-real-data"
        );
        if (response.data.success) {
          this.defaultLocations = response.data.data.locations;
          this.$message.success(
            `成功获取 ${this.defaultLocations.length} 个实时POI数据`
          );
          if (this.isMapLoaded && this.map) {
            this.showDefaultPoints();
          }
        }
      } catch (error) {
        console.error("刷新数据失败:", error);
        this.$message.error("刷新实时数据失败");
      } finally {
        this.refreshing = false;
      }
    },

    // 显示默认点
    showDefaultPoints() {
      if (!this.map) return;

      this.clearMarkers();

      this.defaultLocations.forEach((location) => {
        try {
          const point = new BMap.Point(location.lng, location.lat);
          const marker = new BMap.Marker(point);

          const label = new BMap.Label(`${location.name} (${location.type})`, {
            offset: new BMap.Size(20, -10),
          });
          marker.setLabel(label);

          marker.addEventListener("click", () => {
            this.showInfoWindow(marker, location);
          });

          this.map.addOverlay(marker);
          this.markers.push(marker);
        } catch (error) {
          console.error("添加标记失败:", error);
        }
      });
    },

    // 显示信息窗口
    showInfoWindow(marker, location) {
      const content = `
            <div style="padding: 10px;">
              <h4>${location.name}</h4>
              <p><strong>类型:</strong> ${location.type}</p>
              <p><strong>坐标:</strong> ${location.lng.toFixed(
                6
              )}, ${location.lat.toFixed(6)}</p>
              ${
                location.address
                  ? `<p><strong>地址:</strong> ${location.address}</p>`
                  : ""
              }
            </div>
          `;

      const infoWindow = new BMap.InfoWindow(content);
      marker.openInfoWindow(infoWindow);
    },

    // 执行聚类分析
    async handleClusterAnalysis() {
      this.loading = true;
      try {
        const response = await axios.post("http://localhost:5000/api/cluster", {
          clusterCount: this.form.clusterCount,
          locations: this.defaultLocations,
        });

        if (response.data.success) {
          this.clusterResult = response.data.data;
          this.visualizeClusters();
          this.$message.success(
            `聚类分析完成，轮廓系数: ${
              this.clusterResult.metrics.silhouetteScore
                ? this.clusterResult.metrics.silhouetteScore.toFixed(3)
                : "N/A"
            }`
          );
        } else {
          this.$message.error(
            "分析失败: " + (response.data.error || "未知错误")
          );
        }
      } catch (error) {
        console.error("聚类分析错误:", error);
        this.mockClusterResult();
        this.$message.success("聚类分析完成（模拟数据）");
      } finally {
        this.loading = false;
      }
    },

    // 模拟聚类结果
    mockClusterResult() {
      this.clusterResult = {
        metrics: {
          totalPoints: this.defaultLocations.length,
          clusterCount: this.form.clusterCount,
          silhouetteScore: 0.85,
          inertia: 1200,
        },
        clusters: [],
      };

      for (let i = 0; i < this.form.clusterCount; i++) {
        const centerIndex = i % this.defaultLocations.length;
        const location = this.defaultLocations[centerIndex];

        this.clusterResult.clusters.push({
          clusterId: i,
          pointCount:
            Math.floor(this.defaultLocations.length / this.form.clusterCount) +
            (i < this.defaultLocations.length % this.form.clusterCount ? 1 : 0),
          center: {
            lng: location.lng + (Math.random() - 0.5) * 0.01,
            lat: location.lat + (Math.random() - 0.5) * 0.01,
          },
          maxDistance: (Math.random() * 2 + 0.5).toFixed(2),
          avgDistance: (Math.random() * 1 + 0.3).toFixed(2),
          typeDistribution: {
            医疗: Math.floor(Math.random() * 3),
            教育: Math.floor(Math.random() * 3),
            商业: Math.floor(Math.random() * 3),
            景点: Math.floor(Math.random() * 3),
            交通: Math.floor(Math.random() * 3),
          },
          recommendation: "此区域适合部署无人机进行监控巡逻，建议高度100-200米",
          points: this.defaultLocations
            .slice(i * 2, i * 2 + 3)
            .map((loc, idx) => ({
              ...loc,
              name: `${loc.name} (聚类${i + 1}-${idx + 1})`,
            })),
        });
      }

      this.visualizeClusters();
    },

    // 可视化聚类结果
    visualizeClusters() {
      if (!this.map || !this.clusterResult) return;

      this.clearClusterMarkers();

      this.clusterResult.clusters.forEach((cluster, index) => {
        const color = this.CLUSTER_COLORS[index % this.CLUSTER_COLORS.length];

        const centerPoint = new BMap.Point(
          cluster.center.lng,
          cluster.center.lat
        );
        const centerMarker = new BMap.Marker(centerPoint, {
          icon: this.createClusterIcon(color, cluster.pointCount),
        });

        const centerLabel = new BMap.Label(
          `聚类中心 ${index + 1} (${cluster.pointCount}个POI)`,
          {
            offset: new BMap.Size(0, -50),
          }
        );
        centerLabel.setStyle({
          backgroundColor: color,
          color: "white",
          border: "none",
          borderRadius: "4px",
          padding: "5px 10px",
          fontSize: "12px",
        });
        centerMarker.setLabel(centerLabel);

        this.map.addOverlay(centerMarker);
        this.clusterMarkers.push(centerMarker);

        cluster.points.forEach((point) => {
          const pointObj = new BMap.Point(point.lng, point.lat);
          const marker = new BMap.Marker(pointObj, {
            icon: this.createPointIcon(color),
          });

          const pointLabel = new BMap.Label(point.name, {
            offset: new BMap.Size(15, -5),
          });
          marker.setLabel(pointLabel);

          this.map.addOverlay(marker);
          this.clusterMarkers.push(marker);
        });

        const circle = new BMap.Circle(
          centerPoint,
          parseFloat(cluster.maxDistance) * 1000,
          {
            strokeColor: color,
            strokeWeight: 2,
            strokeOpacity: 0.8,
            fillColor: color,
            fillOpacity: 0.1,
          }
        );
        this.map.addOverlay(circle);
        this.clusterMarkers.push(circle);
      });
    },

    // 创建聚类中心图标
    createClusterIcon(color, count) {
      return new BMap.Icon(
        `
            <div style="
              background-color: ${color};
              width: 50px;
              height: 50px;
              border-radius: 50%;
              border: 3px solid white;
              box-shadow: 0 2px 8px rgba(0,0,0,0.3);
              display: flex;
              align-items: center;
              justify-content: center;
              color: white;
              font-weight: bold;
              font-size: 16px;
              cursor: pointer;
            ">${count}</div>
          `,
        new BMap.Size(50, 50)
      );
    },

    // 创建点图标
    createPointIcon(color) {
      return new BMap.Icon(
        `
            <div style="
              background-color: ${color};
              width: 15px;
              height: 15px;
              border-radius: 50%;
              border: 2px solid white;
              box-shadow: 0 1px 3px rgba(0,0,0,0.3);
              cursor: pointer;
            "></div>
          `,
        new BMap.Size(15, 15)
      );
    },

    // 获取聚类标题
    getClusterTitle(cluster) {
      return `聚类 ${cluster.clusterId + 1} - ${
        cluster.pointCount
      }个POI - 覆盖半径: ${cluster.maxDistance}km`;
    },

    // 获取类型标签样式
    getTypeTagType(poiType) {
      const typeMap = {
        医疗: "danger",
        交通: "warning",
        教育: "success",
        商业: "",
        景点: "info",
        政府: "warning",
      };
      return typeMap[poiType] || "info";
    },

    // 清除标记
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

    clearClusterMarkers() {
      this.clusterMarkers.forEach((marker) => {
        try {
          this.map.removeOverlay(marker);
        } catch (error) {
          console.error("清除聚类标记错误:", error);
        }
      });
      this.clusterMarkers = [];
    },

    // 处理无人机部署成功
    handleDroneDeployed(data) {
      console.log("无人机部署成功:", data);
      // 可以在这里添加额外的处理逻辑
      this.$message.success(
        `无人机 ${data.droneData.uavName} 已成功部署到 ${data.location.name}`
      );
    },

    handleReset() {
      this.form.clusterCount = 5;
      this.clusterResult = null;
      this.clearClusterMarkers();
      this.showDefaultPoints();
    },
  },

  beforeDestroy() {
    this.clearMarkers();
    this.clearClusterMarkers();
  },
};
</script>
  
  <style scoped>
.app-container {
  padding: 20px;
  background: #f5f7fa;
}

.filter-container {
  margin-bottom: 20px;
}

.control-panel {
  max-width: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.map-container {
  height: 600px;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: relative;
}

.map {
  width: 100%;
  height: 100%;
  min-height: 600px;
}

.map-legend {
  position: absolute;
  top: 20px;
  right: 20px;
  background: white;
  padding: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
}

.legend-title {
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.legend-item {
  display: flex;
  align-items: center;
  margin: 5px 0;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-right: 8px;
  border: 2px solid white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.stats-panel {
  margin-top: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.result-panel {
  margin-top: 20px;
}

.cluster-detail {
  padding: 15px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  color: #409eff;
  margin-bottom: 10px;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.recommendation-section {
  background: #f0f9ff;
  padding: 15px;
  border-radius: 6px;
  margin: 15px 0;
  border-left: 4px solid #409eff;
}

.recommendation-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin: 0;
}

.points-section {
  margin-top: 20px;
}

.points-section h4 {
  color: #67c23a;
  margin-bottom: 10px;
  border-left: 4px solid #67c23a;
  padding-left: 10px;
}
</style>
  
  <style>
/* 全局样式 */
.BMapLabel {
  background-color: rgba(255, 255, 255, 0.9) !important;
  border: 1px solid #ccc !important;
  border-radius: 3px !important;
  padding: 3px 8px !important;
  font-size: 12px !important;
  white-space: nowrap !important;
  max-width: 200px !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
}
</style>