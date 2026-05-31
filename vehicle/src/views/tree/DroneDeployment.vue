<template>
  <div class="drone-deployment">
    <!-- 部署无人机按钮 -->
    <el-button
      type="success"
      size="small"
      @click="openDeploymentDialog"
      style="margin-left: 15px"
      :disabled="disabled"
    >
      <i class="el-icon-position"></i>
      部署无人机
    </el-button>

    <!-- 部署无人机对话框 -->
    <el-dialog
      title="部署无人机"
      :visible.sync="dialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        :model="deployForm"
        :rules="rules"
        ref="formRef"
        label-width="120px"
        class="deployment-form"
      >
        <!-- 无人机序列号 -->
        <el-form-item label="无人机序列号" prop="sn">
          <el-input
            v-model="deployForm.sn"
            placeholder="请输入无人机序列号，如：DJI-001"
            style="width: 100%"
          ></el-input>
          <el-button
            type="text"
            @click="generateSerialNumber"
            style="position: absolute; right: -70px; top: 0"
          >
            自动生成
          </el-button>
        </el-form-item>

        <!-- 无人机型号 -->
        <el-form-item label="无人机型号" prop="model">
          <el-select
            v-model="deployForm.model"
            placeholder="请选择无人机型号"
            style="width: 100%"
          >
            <el-option label="DJI Mavic 3" value="DJI_Mavic_3"></el-option>
            <el-option label="DJI M300" value="DJI_M300"></el-option>
            <el-option label="DJI Phantom 4" value="DJI_Phantom_4"></el-option>
            <el-option label="DJI Mini 3" value="DJI_Mini_3"></el-option>
            <el-option label="Autel EVO II" value="Autel_EVO_II"></el-option>
          </el-select>
        </el-form-item>

        <!-- 地点选择 -->
        <el-form-item label="部署地点" prop="locationName">
          <el-select
            v-model="deployForm.locationName"
            placeholder="请选择部署地点"
            style="width: 100%"
            @change="handleLocationChange"
            :loading="loadingLocations"
          >
            <el-option
              v-for="location in locations"
              :key="location.id"
              :label="`${location.name} (${location.type})`"
              :value="location.name"
            >
              <div
                style="
                  display: flex;
                  justify-content: space-between;
                  align-items: center;
                "
              >
                <span>{{ location.name }}</span>
                <span style="color: #8492a6; font-size: 12px">
                  {{ location.lng.toFixed(6) }}, {{ location.lat.toFixed(6) }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 显示选中地点信息 -->
        <div v-if="selectedLocation" class="location-info">
          <h4>地点信息</h4>
          <div class="info-item">
            <span class="label">名称：</span>
            <span class="value">{{ selectedLocation.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">类型：</span>
            <el-tag
              size="mini"
              :type="getLocationTypeTag(selectedLocation.type)"
            >
              {{ selectedLocation.type }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">坐标：</span>
            <span class="value"
              >{{ selectedLocation.lng.toFixed(6) }},
              {{ selectedLocation.lat.toFixed(6) }}</span
            >
          </div>
          <div class="info-item" v-if="selectedLocation.address">
            <span class="label">地址：</span>
            <span class="value">{{ selectedLocation.address }}</span>
          </div>
        </div>

        <el-form-item label="初始电量" prop="batteryLevel">
          <el-slider
            v-model="deployForm.batteryLevel"
            :min="0"
            :max="100"
            :step="1"
            show-input
            style="width: 90%; margin-left: 10px"
          >
          </el-slider>
          <span style="margin-left: 10px; color: #666">%</span>
        </el-form-item>

        <el-form-item label="初始状态" prop="status">
          <el-select
            v-model="deployForm.status"
            placeholder="请选择初始状态"
            style="width: 100%"
          >
            <el-option label="空闲" value="IDLE"></el-option>
            <el-option label="待命" value="CHARGING"></el-option>
            <el-option label="离线" value="MAINTENANCE"></el-option>
            <el-option label="飞行中" value="ON_MISSION"></el-option>
            <el-option label="故障" value="ERROR"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="负载能力(kg)" prop="loadCapacity">
          <el-input-number
            v-model="deployForm.loadCapacity"
            :min="0"
            :max="50"
            :step="0.5"
            controls-position="right"
            style="width: 150px"
          ></el-input-number>
          <span style="margin-left: 10px; color: #666">千克</span>
        </el-form-item>

        <!-- 经纬度信息（只读显示） -->
        <el-form-item v-if="selectedLocation" label="经纬度">
          <div style="color: #666; font-size: 14px">
            {{ selectedLocation.lng.toFixed(6) }},
            {{ selectedLocation.lat.toFixed(6) }}
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="handleDeploy"
            :loading="deployLoading"
          >
            确认部署
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
  
  <script>
import axios from "axios";

export default {
  name: "DroneDeployment",

  props: {
    // 可以从父组件传递地点数据
    locationsData: {
      type: Array,
      default: () => [],
    },

    // 地图实例，用于在地图上添加标记
    mapInstance: {
      type: Object,
      default: null,
    },

    // 是否禁用按钮
    disabled: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      dialogVisible: false,
      deployLoading: false,
      loadingLocations: false,

      // 部署表单数据 - 与后端接口字段对应
      deployForm: {
        sn: "", // 序列号
        model: "", // 型号
        status: "IDLE", // 状态
        batteryLevel: 100, // 电量
        locationName: "", // 位置名称
        loadCapacity: 5.0, // 负载能力
      },

      // 表单验证规则
      rules: {
        sn: [
          { required: true, message: "请输入无人机序列号", trigger: "blur" },
        ],
        model: [
          { required: true, message: "请选择无人机型号", trigger: "change" },
        ],
        locationName: [
          { required: true, message: "请选择部署地点", trigger: "change" },
        ],
        batteryLevel: [
          { required: true, message: "请设置初始电量", trigger: "change" },
        ],
        status: [
          { required: true, message: "请选择初始状态", trigger: "change" },
        ],
        loadCapacity: [
          { required: true, message: "请输入负载能力", trigger: "blur" },
        ],
      },

      // 地点列表
      locations: [],

      // 选中的地点信息
      selectedLocation: null,
    };
  },

  watch: {
    // 监听父组件传递的地点数据变化
    locationsData: {
      handler(newVal) {
        if (newVal && newVal.length > 0) {
          this.locations = newVal.map((loc, index) => ({
            id: index + 1,
            ...loc,
          }));
        }
      },
      immediate: true,
      deep: true,
    },
  },

  methods: {
    // 打开部署对话框
    openDeploymentDialog() {
      console.log("打开部署对话框");
      this.dialogVisible = true;

      // 如果父组件没有传递地点数据，则自己加载
      if (this.locations.length === 0) {
        this.loadLocations();
      }

      // 自动生成序列号
      this.generateSerialNumber();
    },

    // 生成序列号
    generateSerialNumber() {
      const randomNum = Math.floor(Math.random() * 900) + 100; // 生成100-999的随机数
      this.deployForm.sn = `DJI-${randomNum}`;
    },

    // 加载地点数据
    async loadLocations() {
      if (this.loadingLocations) return;

      this.loadingLocations = true;
      try {
        const response = await axios.get(
          "http://localhost:5000/api/nanchang-data"
        );
        if (response.data.success) {
          this.locations = response.data.data.locations.map((loc, index) => ({
            id: index + 1,
            ...loc,
          }));
        } else {
          this.$message.error("获取地点数据失败");
          // 使用模拟数据
          this.loadMockLocations();
        }
      } catch (error) {
        console.error("获取地点数据失败:", error);
        this.$message.error("获取地点数据失败");
        // 使用模拟数据
        this.loadMockLocations();
      } finally {
        this.loadingLocations = false;
      }
    },

    // 加载模拟地点数据
    loadMockLocations() {
      this.locations = [
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
    },

    // 处理地点选择变化
    handleLocationChange(locationName) {
      this.selectedLocation = this.locations.find(
        (loc) => loc.name === locationName
      );
    },

    // 获取地点类型标签样式
    getLocationTypeTag(type) {
      const typeMap = {
        医疗: "danger",
        交通: "warning",
        教育: "success",
        商业: "",
        景点: "info",
        政府: "warning",
      };
      return typeMap[type] || "info";
    },

    // 处理部署操作
    async handleDeploy() {
      // 表单验证
      try {
        await this.$refs.formRef.validate();
      } catch (error) {
        this.$message.warning("请填写完整信息");
        return;
      }

      // 检查地点选择
      if (!this.selectedLocation) {
        this.$message.warning("请选择部署地点");
        return;
      }

      this.deployLoading = true;

      try {
        // 准备请求数据 - 与后端接口字段完全对应
        const deployData = {
          sn: this.deployForm.sn, // 序列号
          model: this.deployForm.model, // 型号
          status: this.deployForm.status, // 状态
          batteryLevel: this.deployForm.batteryLevel, // 电量
          locationName: this.deployForm.locationName, // 位置名称
          loadCapacity: parseFloat(this.deployForm.loadCapacity), // 负载能力
        };

        console.log("发送部署请求:", deployData);

        // 调用添加无人机接口
        const response = await axios.post(
          "http://localhost:8080/api/uav-manage/add",
          deployData
        );

        console.log("部署响应:", response.data);

        if (response.data.success) {
          this.$message.success("无人机部署成功！");

          // 通知父组件部署成功，传递完整数据
          this.$emit("deploy-success", {
            droneData: deployData,
            location: this.selectedLocation,
          });

          // 在地图上添加无人机标记
          if (this.mapInstance) {
            this.addDroneMarker(deployData);
          }

          // 关闭对话框并重置表单
          this.dialogVisible = false;
          this.resetForm();
        } else {
          this.$message.error(response.data.message || "部署失败");
        }
      } catch (error) {
        console.error("部署失败:", error);
        const errorMsg =
          error.response?.data?.message || error.message || "请检查网络连接";
        this.$message.error(`部署失败: ${errorMsg}`);
      } finally {
        this.deployLoading = false;
      }
    },

    // 在地图上添加无人机标记
    addDroneMarker(droneData) {
      if (!this.mapInstance) return;

      try {
        // 这里需要确保 BMap 已经加载
        if (typeof BMap === "undefined") {
          console.warn("BMap 未加载，无法添加标记");
          return;
        }

        const point = new BMap.Point(
          this.selectedLocation.lng,
          this.selectedLocation.lat
        );

        const droneIcon = new BMap.Icon(
          `<div style="
                width: 30px;
                height: 30px;
                background: radial-gradient(circle, #67c23a 40%, #85ce61 60%);
                border-radius: 50%;
                border: 3px solid white;
                box-shadow: 0 2px 8px rgba(0,0,0,0.3);
                display: flex;
                align-items: center;
                justify-content: center;
                color: white;
                font-size: 16px;
                font-weight: bold;
              ">
                <i class="el-icon-position" style="font-size: 14px;"></i>
              </div>`,
          new BMap.Size(30, 30)
        );

        const marker = new BMap.Marker(point, { icon: droneIcon });

        // 添加信息窗口
        const content = `
              <div style="padding: 10px; min-width: 250px;">
                <h4 style="margin: 0 0 10px 0; color: #67c23a;">
                  <i class="el-icon-position"></i> ${droneData.sn}
                </h4>
                <p style="margin: 5px 0;"><strong>型号:</strong> ${
                  droneData.model
                }</p>
                <p style="margin: 5px 0;"><strong>位置:</strong> ${
                  droneData.locationName
                }</p>
                <p style="margin: 5px 0;"><strong>坐标:</strong> ${this.selectedLocation.lng.toFixed(
                  6
                )}, ${this.selectedLocation.lat.toFixed(6)}</p>
                <p style="margin: 5px 0;"><strong>电量:</strong> ${
                  droneData.batteryLevel
                }%</p>
                <p style="margin: 5px 0;"><strong>状态:</strong> ${this.getStatusText(
                  droneData.status
                )}</p>
                <p style="margin: 5px 0;"><strong>负载能力:</strong> ${
                  droneData.loadCapacity
                }kg</p>
              </div>
            `;

        marker.addEventListener("click", () => {
          const infoWindow = new BMap.InfoWindow(content);
          marker.openInfoWindow(infoWindow);
        });

        this.mapInstance.addOverlay(marker);

        // 定位到无人机位置
        this.mapInstance.centerAndZoom(point, 16);
      } catch (error) {
        console.error("添加无人机标记失败:", error);
      }
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        IDLE: "空闲",
        CHARGING: "待命",
        ON_MISSION: "飞行中",
        MAINTENANCE: "离线",
        ERROR: "故障",
      };
      return statusMap[status] || status;
    },

    // 重置表单
    resetForm() {
      if (this.$refs.formRef) {
        this.$refs.formRef.resetFields();
      }
      this.selectedLocation = null;
      this.deployForm = {
        sn: "",
        model: "",
        status: "IDLE",
        batteryLevel: 100,
        locationName: "",
        loadCapacity: 5.0,
      };
    },
  },
};
</script>
  
  <style scoped>
.drone-deployment {
  display: inline-block;
}

.deployment-form {
  padding: 10px 20px;
}

.location-info {
  background-color: #f8f9fa;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 20px;
  border-left: 4px solid #409eff;
}

.location-info h4 {
  margin-top: 0;
  margin-bottom: 12px;
  color: #409eff;
  font-size: 14px;
}

.info-item {
  margin-bottom: 6px;
  font-size: 13px;
}

.info-item .label {
  color: #666;
  font-weight: 500;
  margin-right: 8px;
}

.info-item .value {
  color: #333;
}

.dialog-footer {
  text-align: right;
}
</style>