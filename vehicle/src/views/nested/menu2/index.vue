<template>
    <div class="app-container dark-theme">
      <!-- 顶部统计卡片 -->
      <stats-panel :stats="stats" />
      
      <!-- 主要内容区域 -->
      <el-row :gutter="20" style="margin-top: 20px; height: calc(100vh - 200px);">
        <!-- 左侧面板 -->
        <el-col :span="6" class="left-panel">
          <!-- 无人机列表 -->
          <uav-list 
            :uavs="uavs" 
            @select-uav="selectUav"
            @refresh="loadData"
            class="uav-list-container"
          />
        </el-col>
    
        <!-- 中间地图区域 -->
        <el-col :span="12" class="center-panel">
           
            <map-panel 
            :uavs="uavs"
            :missions="missions"
            :orders="orders"
            :locations="locations"
            :show-tracks="showTracks"
            @refresh-map="refreshMap"
            @toggle-tracks="toggleTracks"
            class="map-container"
            />
         </el-col>
    
        <!-- 右侧面板 -->
        <el-col :span="6" class="right-panel">
          <div class="right-content">
            <!-- 任务管理 -->
            <mission-management 
              :missions="missions"
              :orders="orders"
              :uavs="uavs"
              :locations="locations"
              @assign-mission="assignMission"
              @pause-mission="pauseMission"
              @create-mission="showCreateMission = true"
              @create-order="showCreateOrder = true"
              class="mission-management-container"
            />
            
            <!-- 快捷操作 - 放在右侧下方并确保对齐 -->
            <quick-actions 
              @create-mission="showCreateMission = true"
              @create-order="showCreateOrder = true"
              class="quick-actions-container"
            />
          </div>
        </el-col>
      </el-row>
    
      <!-- 创建巡航任务对话框 -->
      <create-mission-dialog
        :visible="showCreateMission"
        :locations="locations"
        :uavs="uavs"
        @close="showCreateMission = false"
        @create="createMission"
      />
    
      <!-- 创建快递订单对话框 -->
      <create-order-dialog
        :visible="showCreateOrder"
        :locations="locations"
        :uavs="uavs"
        @close="showCreateOrder = false"
        @create="createOrder"
      />
    </div>
  </template>
  <script>
  import StatsPanel from './StatsPanel.vue'
  import UavList from './UavList.vue'
  import QuickActions from './QuickActions.vue'
  import MapPanel from './MapPanel.vue'
  import MissionManagement from './MissionManagement.vue'
  import CreateMissionDialog from './CreateMissionDialog.vue'
  import CreateOrderDialog from './CreateOrderDialog.vue'
  
  import { getUavs, getMissions, getOrders, getLocations, getStats, createMission, assignMission, createOrder } from '@/api/uav'
  
  export default {
    name: 'UavDispatch',
    components: {
      StatsPanel,
      UavList,
      QuickActions,
      MapPanel,
      MissionManagement,
      CreateMissionDialog,
      CreateOrderDialog
    },
    data() {
      return {
        // 数据
        uavs: [],
        missions: [],
        orders: [],
        locations: {},
        stats: {},
        
        // 状态
        showCreateMission: false,
        showCreateOrder: false,
        showTracks: true,
        
        refreshTimer: null,
        useMockData: false // 是否使用模拟数据
      }
    },
    
    mounted() {
      this.loadData()
      this.startAutoRefresh()
       // 180秒后检查数据
    setTimeout(() => {
      this.checkDataSources()
    }, 1800000)
  
    },
    
    beforeDestroy() {
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
      }
    },
    
    methods: {
 // 加载数据
async loadData() {
  try {
    console.log('开始加载数据...')
    
    // 并行发起所有请求
    const requests = [
      getUavs(),
      getMissions(),
      getOrders(), 
      getLocations(),
      getStats()
    ]
    
    // 等待所有请求完成（无论成功失败）
    const results = await Promise.allSettled(requests)
    
    console.log('所有请求完成:', results)
    
    // 处理结果
    this.uavs = results[0].status === 'fulfilled' ? results[0].value : []
    this.missions = results[1].status === 'fulfilled' ? results[1].value : []
    this.orders = results[2].status === 'fulfilled' ? results[2].value : []
    this.locations = results[3].status === 'fulfilled' ? results[3].value : {}
    this.stats = results[4].status === 'fulfilled' ? results[4].value : {}
    
    // 检查失败情况
    const failedCount = results.filter(r => r.status === 'rejected').length
    if (failedCount > 0) {
      console.warn(`${failedCount} 个接口调用失败，使用降级数据`)
    }
    
    console.log('最终数据:', {
      uavs: this.uavs,
      missions: this.missions,
      orders: this.orders, 
      locations: this.locations,
      stats: this.stats
    })
    
  } catch (error) {
    console.error('加载数据过程中出现未知异常:', error)
    // 确保即使有未知错误也有数据显示
    this.loadMockData()
  }
},
checkDataSources() {
    console.group('数据源检查')
    console.log('无人机数据:', this.uavs)
    console.log('任务数据:', this.missions) 
    console.log('订单数据:', this.orders)
    console.log('位置数据:', this.locations)
    console.log('统计数据:', this.stats)
    console.groupEnd()
  },
// 加载模拟数据
loadMockData() {
  this.uavs = [
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
    {
      id: 4,
      sn: "NCUAV_004",
      model: "DJI_Matrice_350",
      status: "IDLE",
      batteryLevel: 100,
      currentLng: 115.983,
      currentLat: 28.625,
      currentMission: null,
    }
  ];
  this.missions = [
    {
      id: 1,
      missionName: "交通巡查任务",
      missionType: "CRUISE",
      status: "ACTIVE",
      assignedUavId: 3,
      createdTime: "2024-01-15 10:00:00"
    },
    {
      id: 2,
      missionName: "快递配送任务",
      missionType: "EXPRESS",
      status: "PENDING",
      assignedUavId: null,
      createdTime: "2024-01-15 11:30:00"
    }
  ];
  this.orders = [
    {
      id: 1,
      orderSn: "EXP_20240115001",
      status: "PENDING",
      pickupLng: 115.907,
      pickupLat: 28.662,
      deliveryLng: 115.9,
      deliveryLat: 28.865,
      weight: 2.5,
      createdTime: "2024-01-15 09:30:00"
    },
    {
      id: 2,
      orderSn: "EXP_20240115002",
      status: "ASSIGNED",
      pickupLng: 115.899,
      pickupLat: 28.679,
      deliveryLng: 115.768,
      deliveryLat: 28.62,
      weight: 1.2,
      createdTime: "2024-01-15 10:15:00"
    }
  ];
  this.locations = {
    南昌站: { lng: 115.907, lat: 28.662 },
    昌北机场: { lng: 115.9, lat: 28.865 },
    八一广场: { lng: 115.899, lat: 28.679 },
    南昌东站: { lng: 115.983, lat: 28.625 },
    徐坊客运站: { lng: 115.889, lat: 28.663 },
    南昌西站: { lng: 115.768, lat: 28.62 },
  };
  this.stats = {
    totalUavs: 4,
    idleUavs: 3,
    activeMissions: 1,
    pendingOrders: 1,
  };
  
  console.log('模拟数据加载完成')
},
      
      // 加载模拟数据
      loadMockData() {
        this.uavs = [
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
          }
        ];
        this.missions = [
          {
            id: 1,
            missionName: "交通巡查任务",
            missionType: "CRUISE",
            status: "ACTIVE",
            assignedUavId: 3,
            createdTime: "2024-01-15 10:00:00"
          }
        ];
        this.orders = [
          {
            id: 1,
            orderSn: "EXP_20240115001",
            status: "PENDING",
            pickupLng: 115.907,
            pickupLat: 28.662,
            deliveryLng: 115.9,
            deliveryLat: 28.865,
            weight: 2.5,
            createdTime: "2024-01-15 09:30:00"
          }
        ];
        this.locations = {
          南昌站: { lng: 115.907, lat: 28.662 },
          昌北机场: { lng: 115.9, lat: 28.865 },
          八一广场: { lng: 115.899, lat: 28.679 },
          南昌东站: { lng: 115.983, lat: 28.625 },
          徐坊客运站: { lng: 115.889, lat: 28.663 },
          南昌西站: { lng: 115.768, lat: 28.62 },
        };
        this.stats = {
          totalUavs: 3,
          idleUavs: 2,
          activeMissions: 1,
          pendingOrders: 1,
        };
        
        console.log('模拟数据加载完成')
      },
      
      // 选择无人机
      selectUav(uav) {
        console.log('选中无人机:', uav)
        // 这里可以触发地图聚焦到选中的无人机
      },
      
// 创建任务
async createMission(missionData) {
  try {
    const result = await createMission(missionData);
    this.$message.success('巡航任务创建成功');
    this.showCreateMission = false;
    this.loadData();
  } catch (error) {
    console.error('创建任务失败:', error);
    // 错误信息已在API层处理，这里只做日志记录
  }
},

// 创建订单  
async createOrder(orderData) {
  try {
    const result = await createOrder(orderData);
    this.$message.success('快递订单创建成功');
    this.showCreateOrder = false;
    this.loadData();
  } catch (error) {
    console.error('创建订单失败:', error);
    // 错误信息已在API层处理，这里只做日志记录
  }
},

// 分配任务
async assignMission(mission) {
  try {
    const availableUav = this.uavs.find(uav => uav.status === 'IDLE');
    if (availableUav) {
      await assignMission(mission.id, availableUav.sn);
      this.$message.success(`任务已分配给无人机 ${availableUav.sn}`);
      this.loadData();
    } else {
      this.$message.warning('没有可用的无人机');
    }
  } catch (error) {
    console.error('任务分配失败:', error);
    // 错误信息已在API层处理，这里只做日志记录
  }
},
      
      // 分配任务
      async assignMission(mission) {
        try {
          if (this.useMockData) {
            // 模拟分配任务
            const availableUav = this.uavs.find(uav => uav.status === 'IDLE');
            if (availableUav) {
              mission.status = 'ACTIVE';
              mission.assignedUavId = availableUav.id;
              availableUav.status = 'ON_MISSION';
              availableUav.currentMission = mission.missionName;
              this.stats.idleUavs--;
              this.stats.activeMissions++;
              this.$message.success(`任务已分配给无人机 ${availableUav.sn} (模拟)`);
            } else {
              this.$message.warning('没有可用的无人机');
            }
          } else {
            const availableUav = this.uavs.find(uav => uav.status === 'IDLE');
            if (availableUav) {
              await assignMission(mission.id, availableUav.sn);
              this.$message.success(`任务已分配给无人机 ${availableUav.sn}`);
              this.loadData();
            } else {
              this.$message.warning('没有可用的无人机');
            }
          }
        } catch (error) {
          console.error('任务分配失败:', error);
          // 错误信息已在API层处理
        }
      },
      
      // 暂停任务
      async pauseMission(mission) {
        try {
          if (this.useMockData) {
            // 模拟暂停任务
            mission.status = 'PAUSED';
            const uav = this.uavs.find(u => u.id === mission.assignedUavId);
            if (uav) {
              uav.status = 'IDLE';
              uav.currentMission = null;
              this.stats.idleUavs++;
              this.stats.activeMissions--;
            }
            this.$message.info(`已暂停任务: ${mission.missionName} (模拟)`);
          } else {
            // 这里应该调用暂停任务的API
            this.$message.info(`已暂停任务: ${mission.missionName}`);
            this.loadData();
          }
        } catch (error) {
          console.error('暂停任务失败:', error);
          this.$message.error('暂停任务失败');
        }
      },
      
      // 刷新地图
      refreshMap() {
        this.loadData();
        this.$message.success('地图数据已刷新');
      },
      
      // 切换轨迹显示
      toggleTracks() {
        this.showTracks = !this.showTracks;
        this.$message.info(this.showTracks ? '已显示轨迹' : '已隐藏轨迹');
      },
      
      // 启动自动刷新
      startAutoRefresh() {
        this.refreshTimer = setInterval(() => {
          if (!this.useMockData) {
            this.loadData();
          }
        }, 180000); // 每180秒刷新一次
      }
    }
  }
  </script>
  
  <style scoped>
  .app-container {
    padding: 20px;
    background: linear-gradient(135deg, #0a1a2a 0%, #1a3a5f 100%);
    min-height: calc(100vh - 84px);
    color: #e0f7ff;
    height: 100vh;
    overflow: hidden;
  }
  
  /* 主内容区域布局优化 */
  .left-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  
  .center-panel {
    display: flex;
    flex-direction: column;
    height: 100%;
  }
  
  .right-panel {
    height: 100%;
  }
  
  /* 右侧内容容器 - 新增 */
  .right-content {
    display: flex;
    flex-direction: column;
    height: 100%;
    gap: 20px;
  }
  
  /* 无人机列表容器 - 增加高度 */
  .uav-list-container {
    flex: 1;
    min-height: 600px;
    max-height: 100%;
    overflow: hidden;
  }
  
  /* 地图容器优化 */
  .map-container {
    flex: 1;
    min-height: 600px;
    height: 100%;
  }
  
  /* 任务管理容器 */
  .mission-management-container {
    flex: 1;
    min-height: 400px;
    max-height: calc(100% - 140px);
    overflow: hidden;
  }
  
  /* 快捷操作容器 - 确保对齐 */
  .quick-actions-container {
    flex-shrink: 0;
    height: auto;
    margin-top: 0; /* 移除之前的 auto margin */
  }
  
  /* 为各个面板添加统一样式 */
  .app-container ::v-deep .panel-card {
    margin-bottom: 0;
    box-shadow: 0 4px 20px rgba(0, 150, 255, 0.15);
    border: 1px solid rgba(64, 158, 255, 0.1);
    background: rgba(13, 25, 42, 0.9);
    backdrop-filter: blur(10px);
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  /* 快捷操作卡片特殊样式 */
  .app-container ::v-deep .quick-actions-container .panel-card {
    height: auto; /* 不强制100%高度 */
    min-height: auto;
  }
  
  /* 地图容器样式 */
  .app-container ::v-deep .map-container .map-panel {
    height: 100%;
  }
  
  .app-container ::v-deep .map-container .map-container {
    height: 100%;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid rgba(64, 158, 255, 0.2);
  }
  
  /* 无人机列表内部滚动优化 */
  .app-container ::v-deep .uav-list-container .uav-list {
    max-height: none;
    height: calc(100% - 60px);
    overflow-y: auto;
  }
  
  /* 任务管理内部滚动优化 */
  .app-container ::v-deep .mission-management-container .mission-list,
  .app-container ::v-deep .mission-management-container .order-list {
    max-height: none;
    height: calc(100% - 100px);
    overflow-y: auto;
  }
  
  /* 暗色主题下的全局样式 */
  .dark-theme ::v-deep .el-card {
    background: rgba(13, 25, 42, 0.9);
    border: 1px solid rgba(64, 158, 255, 0.1);
    color: #e0f7ff;
  }
  
  .dark-theme ::v-deep .el-card__header {
    background: rgba(22, 42, 66, 0.8);
    border-bottom: 1px solid rgba(64, 158, 255, 0.1);
    color: #e0f7ff;
    flex-shrink: 0;
  }
  
  .dark-theme ::v-deep .el-card__body {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  
  .dark-theme ::v-deep .el-button {
    border-radius: 6px;
  }
  
  .dark-theme ::v-deep .el-button--primary {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    border: none;
  }
  
  .dark-theme ::v-deep .el-button--success {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    border: none;
  }
  
  .dark-theme ::v-deep .el-tabs__item {
    color: #8fc1ff;
  }
  
  .dark-theme ::v-deep .el-tabs__item.is-active {
    color: #1890ff;
  }
  
  .dark-theme ::v-deep .el-tabs__active-bar {
    background-color: #1890ff;
  }
  
  /* 响应式调整 */
  @media (max-height: 800px) {
    .app-container {
      padding: 15px;
    }
    
    .uav-list-container {
      min-height: 500px;
    }
    
    .map-container {
      min-height: 500px;
    }
    
    .mission-management-container {
      min-height: 300px;
      max-height: calc(100% - 120px);
    }
  }
  
  @media (min-height: 900px) {
    .uav-list-container {
      min-height: 700px;
    }
    
    .map-container {
      min-height: 700px;
    }
    
    .mission-management-container {
      min-height: 500px;
      max-height: calc(100% - 160px);
    }
  }
  
  /* 确保快捷操作按钮正确对齐 */
  .app-container ::v-deep .quick-actions-container .quick-actions {
    display: flex;
    flex-direction: column;
    align-items: stretch; /* 确保按钮宽度一致 */
    gap: 12px;
  }
  
  .app-container ::v-deep .quick-actions-container .action-btn {
    width: 100%;
    text-align: center;
  }
  </style>