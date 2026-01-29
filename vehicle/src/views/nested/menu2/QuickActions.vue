<template>
    <el-card class="panel-card">
      <div slot="header" class="panel-header">
        <span><i class="el-icon-s-operation"></i> 快捷操作</span>
      </div>
      <div class="quick-actions">
        <el-button 
          type="primary" 
          icon="el-icon-s-promotion" 
          @click="$emit('create-mission')"
          class="action-btn mission-btn"
        >
          创建巡航任务
        </el-button>
        <el-button 
          type="success" 
          icon="el-icon-box" 
          @click="$emit('create-order')"
          class="action-btn order-btn"
        >
          创建快递订单
        </el-button>
      </div>
    </el-card>
  </template>
  
  <script>
  export default {
    name: 'QuickActions'
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
  
  .quick-actions {
    text-align: center;
  }
  
  .action-btn {
    width: 100%;
    margin-bottom: 12px;
    height: 44px;
    border-radius: 8px;
    border: none;
    font-weight: 500;
    font-size: 14px;
    transition: all 0.3s ease;
  }
  
  .mission-btn {
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
  }
  
  .mission-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(24, 144, 255, 0.4);
  }
  
  .order-btn {
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
  }
  
  .order-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(82, 196, 26, 0.4);
  }
  </style>