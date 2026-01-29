<template>
    <div class="dashboard-container">
      <!-- 集成路径规划的3D地图卡片 -->
      <el-card class="dashboard-chart">
        <div class="chart-header">
          <h3>南昌市无人机路径规划系统</h3>
          <p>基于MapVGL的3D可视化与智能路径规划</p>
        </div>
        <MapVGLDemo @route-saved="handleRouteSaved" @use-route="handleUseRoute" />
      </el-card>
  
      <!-- 预设路线列表 -->
      <el-card class="preset-routes" v-if="presetRoutes.length > 0">
        <div slot="header">
          <span>预设路线</span>
        </div>
        <div class="routes-list">
          <el-tag
            v-for="route in presetRoutes"
            :key="route.id"
            type="info"
            closable
            @close="removePresetRoute(route.id)"
            @click="loadPresetRoute(route)"
            class="route-tag"
          >
            {{ route.name }}
          </el-tag>
        </div>
      </el-card>
    </div>
  </template>
  
  <script>
  import { mapGetters } from 'vuex'
  import MapVGLDemo from './components/MapVGLDemo.vue'
  
  export default {
    name: 'index',
    components: {
      MapVGLDemo
    },
    data() {
      return {
        presetRoutes: []
      }
    },
    computed: {
      ...mapGetters([
        'name'
      ])
    },
    methods: {
      handleRouteSaved(route) {
        this.presetRoutes.push(route);
        this.$message.success(`路线 "${route.name}" 已保存到预设`);
      },
      
      handleUseRoute(routeInfo) {
        console.log('使用路线创建任务:', routeInfo);
        this.$message.info('已使用路线创建新任务');
      },
      
      removePresetRoute(routeId) {
        this.presetRoutes = this.presetRoutes.filter(route => route.id !== routeId);
        this.$message.success('预设路线已删除');
      },
      
      loadPresetRoute(route) {
        console.log('加载预设路线:', route);
        this.$message.info(`正在加载路线: ${route.name}`);
      }
    }
  }
  </script>
  
  <style lang="scss" scoped>
  .dashboard {
    &-container {
      margin: 30px;
      display: flex;
      flex-direction: column;
      gap: 24px;
      background: #1d2b3a;
      min-height: 100vh;
    }
    &-text {
      font-size: 24px;
      line-height: 36px;
      margin-bottom: 20px;
      color: #e1e5e9;
      font-weight: bold;
    }
    
    &-chart {
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
      background-color: #2a3b4c;
      border: 2px solid #4dabf7;
      
      .chart-header {
        margin-bottom: 2px;
        padding-bottom: 2px;
        border-bottom: 1px solid #3a506b;
        
        h3 {
          font-size: 18px;
          margin: 0 0 8px 0;
          color: #e1e5e9;
        }
        
        p {
          margin: 0;
          font-size: 14px;
          color: #8a9ba8;
        }
      }
    }
  }
  
  .preset-routes {
    background: #2a3b4c;
    border: 1px solid #3a506b;
    
    :deep(.el-card__header) {
      background: #1d2b3a;
      border-bottom: 1px solid #3a506b;
      color: #e1e5e9;
    }
  }
  
  .routes-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .route-tag {
    cursor: pointer;
    transition: all 0.3s;
    background: rgba(77, 171, 247, 0.2);
    border-color: rgba(77, 171, 247, 0.4);
    color: #e1e5e9;
  }
  
  .route-tag:hover {
    background: rgba(77, 171, 247, 0.4);
    transform: translateY(-2px);
  }
  
  @media (max-width: 768px) {
    .dashboard-container {
      margin: 15px;
    }
    
    .dashboard-text {
      font-size: 20px;
      line-height: 30px;
    }
  }
  </style>