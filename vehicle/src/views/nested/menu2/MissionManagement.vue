<template>
    <el-card class="panel-card">
      <div slot="header" class="panel-header">
        <span><i class="el-icon-document"></i> 任务管理</span>
      </div>
      <el-tabs v-model="activeTab" class="dark-tabs">
        <el-tab-pane label="巡航任务" name="missions">
          <div class="mission-list">
            <div 
              v-for="mission in missions" 
              :key="mission.id"
              class="mission-item"
              :class="`status-${mission.status ? mission.status.toLowerCase() : 'pending'}`"
            >
              <div class="mission-header">
                <span class="mission-name">{{ mission.missionName }}</span>
                <el-tag 
                  :type="getMissionStatusType(mission.status)"
                  size="mini"
                  effect="dark"
                >
                  {{ getMissionStatusText(mission.status) }}
                </el-tag>
              </div>
              <div class="mission-details">
                <div>类型: {{ getMissionTypeText(mission.missionType) }}</div>
                <div v-if="mission.assignedUavId">
                  无人机: {{ getUavSn(mission.assignedUavId) }}
                </div>
                <div>创建: {{ formatTime(mission.createdTime) }}</div>
              </div>
              <div class="mission-actions">
                <el-button 
                  v-if="mission.status === 'PENDING'"
                  type="primary" 
                  size="mini"
                  @click="$emit('assign-mission', mission)"
                  class="action-btn"
                >
                  分配
                </el-button>
                <el-button 
                  v-if="mission.status === 'ACTIVE'"
                  type="warning" 
                  size="mini"
                  @click="$emit('pause-mission', mission)"
                  class="action-btn"
                >
                  暂停
                </el-button>
              </div>
            </div>
            
            <!-- 无任务时的提示 -->
            <div v-if="missions.length === 0" class="no-data">
              <i class="el-icon-document-remove"></i>
              <p>暂无巡航任务</p>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="快递订单" name="orders">
          <div class="order-list">
            <div 
              v-for="order in orders" 
              :key="order.id"
              class="order-item"
              :class="`status-${order.status ? order.status.toLowerCase() : 'pending'}`"
            >
              <div class="order-header">
                <span class="order-sn">{{ order.orderSn }}</span>
                <el-tag 
                  :type="getOrderStatusType(order.status)"
                  size="mini"
                  effect="dark"
                >
                  {{ getOrderStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="order-route">
                <div class="route-from">
                  <i class="el-icon-location"></i>
                  {{ getLocationName(order.pickupLng, order.pickupLat) }}
                </div>
                <div class="route-arrow">
                  <i class="el-icon-right"></i>
                </div>
                <div class="route-to">
                  <i class="el-icon-location"></i>
                  {{ getLocationName(order.deliveryLng, order.deliveryLat) }}
                </div>
              </div>
              <div class="order-details">
                <span>重量: {{ order.weight }}kg</span>
                <span>{{ formatTime(order.createdTime) }}</span>
              </div>
            </div>
            
            <!-- 无订单时的提示 -->
            <div v-if="orders.length === 0" class="no-data">
              <i class="el-icon-tickets"></i>
              <p>暂无快递订单</p>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </template>
  
  <script>
  export default {
    name: 'MissionManagement',
    props: {
      missions: {
        type: Array,
        default: () => []
      },
      orders: {
        type: Array,
        default: () => []
      },
      uavs: {
        type: Array,
        default: () => []
      },
      locations: {
        type: Object,
        default: () => ({})
      }
    },
    data() {
      return {
        activeTab: 'missions'
      }
    },
    methods: {
      getMissionStatusType(status) {
        const types = {
          'PENDING': 'info',
          'ACTIVE': 'success',
          'PAUSED': 'warning',
          'COMPLETED': '',
          'CANCELLED': 'danger'
        }
        return types[status] || 'info'
      },
      
      getMissionStatusText(status) {
        const texts = {
          'PENDING': '待分配',
          'ACTIVE': '进行中',
          'PAUSED': '已暂停',
          'COMPLETED': '已完成',
          'CANCELLED': '已取消'
        }
        return texts[status] || status || '未知'
      },
      
      getMissionTypeText(type) {
        const texts = {
          'CRUISE': '巡航',
          'EXPRESS': '快递',
          'SURVEILLANCE': '监控'
        }
        return texts[type] || type || '未知'
      },
      
      getOrderStatusType(status) {
        const types = {
          'PENDING': 'warning',
          'ASSIGNED': 'primary',
          'DELIVERING': 'success',
          'COMPLETED': '',
          'FAILED': 'danger'
        }
        return types[status] || 'info'
      },
      
      getOrderStatusText(status) {
        const texts = {
          'PENDING': '待处理',
          'ASSIGNED': '已分配',
          'DELIVERING': '配送中',
          'COMPLETED': '已完成',
          'FAILED': '失败'
        }
        return texts[status] || status || '未知'
      },
      
      getUavSn(uavId) {
        const uav = this.uavs.find(u => u.id === uavId)
        return uav ? uav.sn : '未知'
      },
      
      getLocationName(lng, lat) {
        if (!this.locations) return '未知位置'
        
        for (const [name, loc] of Object.entries(this.locations)) {
          if (Math.abs(loc.lng - lng) < 0.001 && Math.abs(loc.lat - lat) < 0.001) {
            return name
          }
        }
        return '自定义位置'
      },
      
      formatTime(timeStr) {
        if (!timeStr) return '-'
        try {
          const date = new Date(timeStr)
          return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
        } catch (e) {
          return '-'
        }
      }
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
  
  .dark-tabs ::v-deep .el-tabs__item {
    color: #8fc1ff;
  }
  
  .dark-tabs ::v-deep .el-tabs__item.is-active {
    color: #1890ff;
  }
  
  .dark-tabs ::v-deep .el-tabs__active-bar {
    background-color: #1890ff;
  }
  
  .dark-tabs ::v-deep .el-tabs__nav-wrap::after {
    background-color: rgba(64, 158, 255, 0.2);
  }
  
  .mission-list,
  .order-list {
    max-height: 400px;
    overflow-y: auto;
  }
  
  .mission-item,
  .order-item {
    padding: 16px;
    margin-bottom: 10px;
    border-radius: 8px;
    border: 1px solid rgba(64, 158, 255, 0.2);
    background: rgba(22, 42, 66, 0.6);
    transition: all 0.3s ease;
    backdrop-filter: blur(5px);
  }
  
  .mission-item:hover,
  .order-item:hover {
    border-color: #1890ff;
    box-shadow: 0 4px 15px rgba(24, 144, 255, 0.2);
    transform: translateY(-1px);
  }
  
  .mission-item.status-active,
  .order-item.status-delivering {
    border-left: 4px solid #52c41a;
  }
  
  .mission-item.status-pending,
  .order-item.status-pending {
    border-left: 4px solid #faad14;
  }
  
  .mission-item.status-completed,
  .order-item.status-completed {
    border-left: 4px solid #8fc1ff;
  }
  
  .mission-header,
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }
  
  .mission-name,
  .order-sn {
    font-weight: bold;
    color: #e0f7ff;
    font-size: 14px;
  }
  
  .mission-details,
  .order-details {
    font-size: 12px;
    color: #8fc1ff;
    margin-bottom: 10px;
  }
  
  .mission-details div,
  .order-details span {
    margin-bottom: 4px;
  }
  
  .mission-actions {
    text-align: right;
  }
  
  .action-btn {
    border-radius: 6px;
    border: none;
    font-weight: 500;
  }
  
  .order-route {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    font-size: 12px;
    color: #8fc1ff;
  }
  
  .route-from,
  .route-to {
    flex: 1;
  }
  
  .route-arrow {
    color: #1890ff;
    margin: 0 12px;
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