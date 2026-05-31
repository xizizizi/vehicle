<template>
    <div id="data-view">
      <div class="dashboard-container">
        <top-header />
        <div class="main-content">
          <digital-flop />
          <!-- 多领域调度卡片 -->
          <div class="domain-quick-nav">
            <div class="nav-header">
              <div class="nav-title">
                <i class="el-icon-menu"></i>
                多领域调度
              </div>
              <div class="nav-subtitle">快速访问各领域调度系统</div>
              <div class="nav-update-time" v-if="lastUpdateTime">
                最后更新: {{ lastUpdateTime }}
              </div>
            </div>
            
            <div class="nav-cards">
              <router-link to="/medical" class="nav-card medical">
                <div class="card-icon">
                  <i class="el-icon-first-aid-kit"></i>
                </div>
                <div class="card-content">
                  <div class="card-title">医疗救助</div>
                  <div class="card-stats">
                    <span class="stat-value">{{ systemStats.medical || 0 }}</span>
                    <span class="stat-label">紧急任务</span>
                  </div>
                </div>
                <div class="card-arrow">
                  <i class="el-icon-arrow-right"></i>
                </div>
              </router-link>
  
              <router-link to="/forest" class="nav-card forest">
                <div class="card-icon">
                  <i class="el-icon-place"></i>
                </div>
                <div class="card-content">
                  <div class="card-title">森林防护</div>
                  <div class="card-stats">
                    <span class="stat-value">{{ systemStats.forest || 0 }}</span>
                    <span class="stat-label">火情预警</span>
                  </div>
                </div>
                <div class="card-arrow">
                  <i class="el-icon-arrow-right"></i>
                </div>
              </router-link>
  
              <router-link to="/power" class="nav-card power">
                <div class="card-icon">
                  <i class="el-icon-lightning"></i>
                </div>
                <div class="card-content">
                  <div class="card-title">电力巡检</div>
                  <div class="card-stats">
                    <span class="stat-value">{{ systemStats.power || 0 }}</span>
                    <span class="stat-label">异常报告</span>
                  </div>
                </div>
                <div class="card-arrow">
                  <i class="el-icon-arrow-right"></i>
                </div>
              </router-link>
  
              <router-link to="/ai" class="nav-card ai">
                <div class="card-icon">
                  <i class="el-icon-aim"></i>
                </div>
                <div class="card-content">
                  <div class="card-title">AI助手</div>
                  <div class="card-stats">
                    <span class="stat-value">24</span>
                    <span class="stat-label">小时在线</span>
                  </div>
                  <div class="card-note"></div>
                </div>
                <div class="card-arrow">
                  <i class="el-icon-arrow-right"></i>
                </div>
              </router-link>
            </div>
          </div>
          <!-- 主要内容区域 -->
          <div class="main-layout-container">
            <!-- 左侧：排名榜 -->
            <div class="left-column">
              <ranking-board />
            </div>
            
            <!-- 右侧：四个组件 -->
            <div class="right-column">
              <!-- 右侧上方：三个组件并列 -->
              <div class="right-top-row">
                <rose-chart />
                <water-level-chart />
                <scroll-board />
              </div>
              
              <!-- 右侧下方：卡片组件 -->
              <div class="right-bottom-row">
                <cards />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import topHeader from './topHeader'
  import digitalFlop from './digitalFlop'
  import rankingBoard from './rankingBoard'
  import roseChart from './roseChart'
  import waterLevelChart from './waterLevelChart'
  import scrollBoard from './scrollBoard'
  import cards from './cards'
  
  export default {
    name: 'DataView',
    components: {
      topHeader,
      digitalFlop,
      rankingBoard,
      roseChart,
      waterLevelChart,
      scrollBoard,
      cards
    },
    data() {
      return {
        systemStats: {
          medical: 0,
          forest: 0,
          power: 0,
          other: 0,
          total: 0
        },
        lastUpdateTime: null
      }
    },
    methods: {
      // 获取系统统计信息
      async fetchSystemStats() {
        try {
          const response = await fetch('http://localhost:8080/api/multi-system/stats/simple');
          const result = await response.json();
          
          if (result.success && result.data) {
            this.systemStats = result.data;
            this.lastUpdateTime = new Date().toLocaleTimeString('zh-CN', {
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            });
          }
        } catch (error) {
          console.error('获取系统统计信息失败:', error);
          // 可以添加错误提示
          this.$message({
            message: '获取系统统计信息失败',
            type: 'error',
            duration: 3000
          });
        }
      }
    },
    mounted() {
      // 页面加载时获取数据
      this.fetchSystemStats();
      
      // 每30秒刷新一次数据
      setInterval(() => {
        this.fetchSystemStats();
      }, 30000);
    }
  }
  </script>
  
  <style lang="scss" scoped>
  #data-view {
    width: 100%;
    height: 100%;
    background-color: #030409;
    color: #fff;
    
    .dashboard-container {
      position: relative;
      width: 100%;
      height: 100%;
      background-image: url('./img/bg.png');
      background-size: cover;
      box-shadow: 0 0 3px blue;
      display: flex;
      flex-direction: column;
      padding: 20px;
      box-sizing: border-box;
      overflow: auto;
      min-width: 1200px;
    }
  
    .main-content {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-height: 0;
    }
  
    /* 主要布局容器 */
    .main-layout-container {
      flex: 1;
      display: flex;
      margin-top: 20px;
      gap: 20px;
      min-height: 0;
      min-width: 0;
    }
  
    /* 左侧列 - 固定宽度 */
    .left-column {
      width: 280px;
      min-width: 250px;
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
    }
  
    /* 右侧列 - 自适应宽度 */
    .right-column {
      flex: 1;
      display: flex;
      flex-direction: column;
      min-width: 0;
      gap: 20px;
    }
  
    /* 右侧上方行 - 三个组件水平排列 */
    .right-top-row {
      flex: 1; /* 占据右侧列的1/2高度 */
      display: flex;
      gap: 20px;
      min-height: 0;
      min-width: 0;
      
      & > * {
        flex: 1;
        min-width: 0;
      }
    }
  
    /* 右侧下方行 - cards组件 */
    .right-bottom-row {
      flex: 1; /* 占据右侧列的1/2高度 */
      min-height: 0;
      min-width: 0;
      display: flex;
    }
  }
  .domain-quick-nav {
    margin: 20px 0;
    background: rgba(6, 30, 93, 0.5);
    border-radius: 12px;
    padding: 20px;
    border: 1px solid rgba(0, 240, 255, 0.3);
    box-shadow: 0 0 20px rgba(0, 240, 255, 0.15);
    position: relative;
  }
  
  .nav-header {
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(0, 240, 255, 0.2);
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    flex-wrap: wrap;
  }
  
  .nav-title {
    font-size: 20px;
    font-weight: bold;
    color: #00f0ff;
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 5px;
  }
  
  .nav-subtitle {
    color: #b3d9ff;
    font-size: 14px;
    flex: 1;
  }
  
  .nav-update-time {
    color: #66c2ff;
    font-size: 12px;
    opacity: 0.8;
    text-align: right;
    white-space: nowrap;
  }
  
  .nav-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
  }
  
  .nav-card {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 10px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 15px;
    text-decoration: none;
    color: inherit;
    border: 1px solid transparent;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
  }
  
  .nav-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  .nav-card.medical::before {
    background: linear-gradient(90deg, #ff6b6b, #ff4757);
  }
  
  .nav-card.forest::before {
    background: linear-gradient(90deg, #26fcd8, #00a884);
  }
  
  .nav-card.power::before {
    background: linear-gradient(90deg, #ffb830, #ff9f00);
  }
  
  .nav-card.ai::before {
    background: linear-gradient(90deg, #00f0ff, #009dff);
  }
  
  .nav-card:hover {
    transform: translateY(-5px);
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(0, 240, 255, 0.3);
  }
  
  .nav-card:hover::before {
    opacity: 1;
  }
  
  .card-icon {
    font-size: 28px;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  
  .nav-card.medical .card-icon {
    background: rgba(255, 107, 107, 0.2);
    color: #ff6b6b;
  }
  
  .nav-card.forest .card-icon {
    background: rgba(38, 252, 216, 0.2);
    color: #26fcd8;
  }
  
  .nav-card.power .card-icon {
    background: rgba(255, 184, 48, 0.2);
    color: #ffb830;
  }
  
  .nav-card.ai .card-icon {
    background: rgba(0, 240, 255, 0.2);
    color: #00f0ff;
  }
  
  .card-content {
    flex: 1;
    min-width: 0;
  }
  
  .card-title {
    font-size: 18px;
    font-weight: bold;
    color: #fff;
    margin-bottom: 8px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .card-stats {
    display: flex;
    align-items: baseline;
    gap: 8px;
    margin-bottom: 4px;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    line-height: 1;
  }
  
  .nav-card.medical .stat-value { color: #ff6b6b; }
  .nav-card.forest .stat-value { color: #26fcd8; }
  .nav-card.power .stat-value { color: #ffb830; }
  .nav-card.ai .stat-value { color: #00f0ff; }
  
  .stat-label {
    color: #b3d9ff;
    font-size: 14px;
    white-space: nowrap;
  }
  
  .card-note {
    font-size: 12px;
    color: #8cc8ff;
    opacity: 0.8;
    margin-top: 2px;
  }
  
  .card-arrow {
    opacity: 0.5;
    transition: all 0.3s ease;
    flex-shrink: 0;
  }
  
  .nav-card:hover .card-arrow {
    opacity: 1;
    transform: translateX(5px);
  }
  
  /* 响应式调整 */
  @media (max-width: 768px) {
    .nav-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 10px;
    }
    
    .nav-update-time {
      text-align: left;
    }
    
    .nav-cards {
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    }
    
    .card-stats {
      flex-direction: column;
      align-items: flex-start;
      gap: 2px;
    }
    
    .stat-value {
      font-size: 20px;
    }
  }
  
  @media (max-width: 480px) {
    .nav-cards {
      grid-template-columns: 1fr;
    }
    
    .card-content {
      min-width: 0;
    }
    
    .card-title {
      font-size: 16px;
    }
    
    .stat-value {
      font-size: 18px;
    }
  }
  
  /* 添加数据加载动画 */
  @keyframes pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.5; }
  }
  
  .loading {
    animation: pulse 1.5s ease-in-out infinite;
  }
  
  /* 数据更新动画 */
  .stat-value {
    transition: transform 0.3s ease;
  }
  
  .stat-value.updated {
    transform: scale(1.1);
  }
  </style>