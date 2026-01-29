<template>
    <el-card class="panel-card" style="margin-bottom: 20px;">
      <div slot="header" class="panel-header">
        <span><i class="el-icon-monitor"></i> 无人机状态</span>
        <el-button type="text" @click="$emit('refresh')" style="color: #8fc1ff;">刷新</el-button>
      </div>
      <div class="uav-list">
        <div 
          v-for="uav in uavs" 
          :key="uav.id"
          class="uav-item"
          :class="`status-${uav.status ? uav.status.toLowerCase() : 'idle'}`"
          @click="$emit('select-uav', uav)"
        >
          <div class="uav-header">
            <span class="uav-sn">{{ uav.sn }}</span>
            <el-tag 
              :type="getStatusType(uav.status)"
              size="mini"
              effect="dark"
            >
              {{ getStatusText(uav.status) }}
            </el-tag>
          </div>
          <div class="uav-details">
            <div class="detail-row">
              <span>型号: {{ uav.model || '未知型号' }}</span>
              <span>载重: {{ uav.loadCapacity || '-' }}kg</span>
            </div>
            <div class="detail-row">
              <span>电量: {{ uav.batteryLevel || 0 }}%</span>
              <el-progress 
                :percentage="uav.batteryLevel || 0" 
                :color="getBatteryColor(uav.batteryLevel)"
                :show-text="true"
                :stroke-width="8"
                style="width: 70px;"
              />
            </div>
            <div class="detail-row">
              <span>位置: {{ formatCoord(uav.currentLng) }}, {{ formatCoord(uav.currentLat) }}</span>
            </div>
            <div class="detail-row mission">
              <span>任务: {{ uav.currentMission ? uav.currentMission : '无任务' }}</span>
            </div>
          </div>
        </div>
        
        <!-- 无数据时的提示 -->
        <div v-if="uavs.length === 0" class="no-data">
          <i class="el-icon-warning"></i>
          <p>暂无无人机数据</p>
          <el-button type="text" @click="$emit('refresh')" style="color: #8fc1ff;">点击刷新</el-button>
        </div>
      </div>
    </el-card>
  </template>
  
  <script>
  export default {
    name: 'UavList',
    data() {
      return {
        plannedRoute: null
      }
    },
    props: {
      uavs: {
        type: Array,
        default: () => []
      }
    },
    methods: {
        // 处理规划完成的路径
handleRoutePlanned(route) {
  this.plannedRoute = route
  this.$message.success(`路线规划完成！距离: ${(route.totalDistance/1000).toFixed(2)}km`)
},

// 使用路径创建任务
useRouteForMission(route) {
  this.plannedRoute = route
  this.showCreateMission = true
  this.$message.info('路径已准备好，请在任务创建中使用')
},
      getStatusType(status) {
        const types = {
          'IDLE': 'success',
          'ON_MISSION': 'danger',
          'CRUISING': 'primary',
          'DELIVERING': 'warning'
        }
        return types[status] || 'info'
      },
      
      getStatusText(status) {
        const texts = {
          'IDLE': '空闲',
          'ON_MISSION': '任务中',
          'CRUISING': '巡航中',
          'DELIVERING': '配送中'
        }
        return texts[status] || status || '未知'
      },
      
      getBatteryColor(percentage) {
        if (percentage > 70) return '#52c41a'
        if (percentage > 30) return '#faad14'
        return '#f56c6c'
      },
      
      formatCoord(val) {
        if (val === null || val === undefined) return '-';
        return Number(val).toFixed(4);
      },
    }
  }
  </script>
  
  <style scoped>
  .panel-card {
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 150, 255, 0.15);
    background: rgba(13, 25, 42, 0.9);
    border: 1px solid rgba(64, 158, 255, 0.1);
  }
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
    color: #e0f7ff;
  }
  
  .uav-list {
    max-height: 320px;
    overflow-y: auto;
  }
  
  .uav-item {
    padding: 16px;
    margin-bottom: 10px;
    border-radius: 8px;
    border: 1px solid rgba(64, 158, 255, 0.2);
    background: rgba(22, 42, 66, 0.6);
    cursor: pointer;
    transition: all 0.3s ease;
    backdrop-filter: blur(5px);
  }
  
  .uav-item:hover {
    border-color: #1890ff;
    box-shadow: 0 4px 15px rgba(24, 144, 255, 0.3);
    transform: translateY(-1px);
  }
  
  .uav-item.status-idle {
    border-left: 4px solid #52c41a;
  }
  
  .uav-item.status-on_mission {
    border-left: 4px solid #f56c6c;
  }
  
  .uav-item.status-cruising {
    border-left: 4px solid #1890ff;
  }
  
  .uav-item.status-delivering {
    border-left: 4px solid #faad14;
  }
  
  .uav-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }
  
  .uav-sn {
    font-weight: bold;
    color: #e0f7ff;
    font-size: 14px;
  }
  
  .uav-details {
    font-size: 12px;
    color: #8fc1ff;
  }
  
  .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }
  
  .detail-row.mission {
    color: #faad14;
    font-weight: 500;
  }
  
  .no-data {
    text-align: center;
    padding: 30px;
    color: #8fc1ff;
  }
  
  .no-data i {
    font-size: 48px;
    margin-bottom: 12px;
    display: block;
    color: #1890ff;
  }
  
  .no-data p {
    margin-bottom: 12px;
  }
  
  /* 滚动条样式 */
  ::-webkit-scrollbar {
    width: 6px;
  }
  
  ::-webkit-scrollbar-track {
    background: rgba(22, 42, 66, 0.6);
    border-radius: 3px;
  }
  
  ::-webkit-scrollbar-thumb {
    background: #1890ff;
    border-radius: 3px;
  }
  
  ::-webkit-scrollbar-thumb:hover {
    background: #40a9ff;
  }
  </style>