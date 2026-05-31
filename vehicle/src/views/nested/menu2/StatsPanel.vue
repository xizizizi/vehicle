<template>
    <div class="filter-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: rgba(24, 144, 255, 0.1); 
              color: #1890ff;">
                <i class="el-icon-monitor"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalUavs || 0 }}</div>
                <div class="stat-label">总无人机数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: rgba(82, 196, 26, 0.1); color: #52c41a;">
                <i class="el-icon-sunny"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.idleUavs || 0 }}</div>
                <div class="stat-label">空闲无人机</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: rgba(250, 173, 20, 0.1); color: #faad14;">
                <i class="el-icon-s-promotion"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.activeMissions || 0 }}</div>
                <div class="stat-label">活跃任务</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: rgba(245, 108, 108, 0.1); color: #f56c6c;">
                <i class="el-icon-box"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.pendingOrders || 0 }}</div>
                <div class="stat-label">待处理订单</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </template>
  
  <script>
  export default {
    name: 'StatsPanel',
    props: {
      stats: {
        type: Object,
        default: () => ({})
      }
    }
  }
  </script>
  
  <style scoped>
  .filter-container {
    margin-bottom: 20px;
  }
  
  .stat-card {
    border-radius: 12px;
    background: rgba(13, 25, 42, 0.9);
    border: 1px solid rgba(64, 158, 255, 0.1);
    box-shadow: 0 4px 20px rgba(0, 150, 255, 0.15);
    transition: all 0.3s ease;
  }
  
  .stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 25px rgba(0, 150, 255, 0.25);
  }
  
  .stat-content {
    display: flex;
    align-items: center;
    padding: 8px;
  }
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    font-size: 28px;
    transition: all 0.3s ease;
  }
  
  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #e0f7ff;
    line-height: 1;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  }
  
  .stat-label {
    font-size: 13px;
    color: #8fc1ff;
    margin-top: 6px;
    font-weight: 500;
  }
  </style>