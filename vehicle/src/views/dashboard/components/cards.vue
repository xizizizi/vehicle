<template>
    <div id="cards">
      <div
        class="card-item"
        v-for="(card, i) in cards"
        :key="card.title"
      >
      <div class="card-header">
  <div class="card-header-left">
    <div class="card-title">{{ card.title }}</div>
    <div class="card-subtitle">{{ getAreaTypeDescription(card.title) }}</div>
  </div>
  <div class="card-header-right">
    <div class="status-indicator" :class="getStatusClass(card)">
      {{ getStatusText(card) }}
    </div>
    <div class="card-sequence">{{ '0' + (i + 1) }}</div>
  </div>
</div>
        <!-- 环形图表和状态指示器容器 -->
        <div class="chart-status-container">
          <!-- 环形图表 -->
          <div class="chart-container">
            <dv-charts 
              v-if="card.ring && card.ring.series && card.ring.series[0].data"
              class="ring-charts" 
              :option="card.ring" 
            />
            <div v-else class="ring-placeholder">
              <div class="placeholder-icon">📊</div>
              <div class="placeholder-text">无数据</div>
            </div>
          </div>         
        </div>
        
        <div class="card-footer">
          <div class="card-footer-item area">
            <div class="footer-title">
              <span class="icon">📍</span>区域面积
            </div>
            <div class="footer-detail">
              <dv-digital-flop :config="normalizeDigitalConfig(card.total)" style="width:70%;height:35px;" />
              <span class="unit">{{ getAreaUnit(card.title) }}</span>
            </div>
          </div>
          <div class="card-footer-item report">
            <div class="footer-title">
              <span class="icon">📋</span>巡查上报
            </div>
            <div class="footer-detail">
              <dv-digital-flop :config="normalizeDigitalConfig(card.num)" style="width:70%;height:35px;" />
              <span class="unit">处</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'Cards',
    data () {
      return {
        cards: [],
        totalReports: 0,
        areaTypeMap: {
          '商业区': { description: '商业建筑区域', unit: '座' },
          '住宅区': { description: '居民住宅区域', unit: '公顷' },
          '工业区': { description: '工业生产区域', unit: '公顷' },
          '自然区域': { description: '自然生态区域', unit: '公顷' },
          '特殊区域': { description: '特殊管制区域', unit: '公顷' }
        }
      }
    },
    methods: {
      // 从API获取数据
      fetchCardData() {
        axios.get('http://localhost:8080/api/reports/area-cards')
          .then(response => {
            console.log('获取卡片数据成功:', response.data);
            const areaCards = response.data.areaCards || [];
            
            // 计算总上报数量
            this.totalReports = areaCards.reduce((sum, card) => {
              const reportCount = card.num?.number?.[0] || 0;
              return sum + reportCount;
            }, 0);
            
            this.cards = this.processCardData(areaCards);
          })
          .catch(error => {
            console.error('获取卡片数据失败:', error);
            this.$message.error('获取数据失败，请检查网络连接');
          });
      },
      
      // 处理卡片数据
      processCardData(cards) {
        return cards.map(card => {
          // 获取当前区域的巡查上报数量
          const reportCount = card.num?.number?.[0] || 0;
          
          // 计算异常占比
          let abnormalPercentage = 0;
          if (this.totalReports > 0) {
            abnormalPercentage = Math.round((reportCount / this.totalReports) * 100);
          }
          
          // 构建环形图配置
          card.ring = {
            color: ['#03d3ec', '#00ff88', '#ff6b6b'],
            series: [
              {
                type: 'gauge',
                data: [{ name: '异常占比', value: abnormalPercentage }],
                startAngle: -Math.PI / 2,
                endAngle: Math.PI * 1.5,
                arcLineWidth: 15,
                radius: '80%',
                axisLabel: { show: false },
                axisTick: { show: false },
                pointer: { 
                  show: true,
                  length: '60%',
                  width: 4
                },
                backgroundArc: {
                  style: {
                    stroke: '#1a3a6a',
                    lineWidth: 15
                  }
                },
                details: {
                  show: true,
                  formatter: '{value}%',
                  style: {
                    fill: '#ffffff',
                    fontSize: 18,
                    fontWeight: 'bold'
                  }
                },
                gauge: {
                  fillOpacity: 1,
                  stroke: 'none'
                }
              }
            ]
          };
          
          return card;
        });
      },
      
      // 标准化数字翻转配置
      normalizeDigitalConfig(config) {
        if (!config) return {};
        
        const numbers = Array.isArray(config.number) ? config.number : [config.number || 0];
        
        return {
          number: numbers,
          style: {
            fill: '#26fcd8',
            fontWeight: 'bold',
            fontSize: 16
          },
          formatter: (number) => {
            return number.toLocaleString();
          }
        };
      },
  
      // 获取区域类型描述
      getAreaTypeDescription(title) {
        return this.areaTypeMap[title]?.description || '区域信息';
      },
  
      // 获取区域单位
      getAreaUnit(title) {
        return this.areaTypeMap[title]?.unit || '公顷';
      },
  
      // 获取状态类名
      getStatusClass(card) {
        const reportCount = card.num?.number?.[0] || 0;
        if (reportCount === 0) return 'status-normal';
        if (reportCount < 3) return 'status-warning';
        return 'status-alert';
      },
  
      // 获取状态文本
      getStatusText(card) {
        const reportCount = card.num?.number?.[0] || 0;
        if (reportCount === 0) return '正常';
        if (reportCount < 3) return '注意';
        return '需关注';
      }
    },
    mounted() {
      this.fetchCardData();
      // 每300秒刷新数据
      setInterval(this.fetchCardData, 300000);
    }
  }
  </script>
  
  <style lang="scss">
  #cards {
    width: 100%;
  height: 100%; /* 确保填满父容器 */
    display: flex;
    justify-content: space-between;
    height: 100%;
    gap: 12px;
    padding: 0 8px;
  
    .card-item {
      background: linear-gradient(145deg, rgba(6, 30, 93, 0.6), rgba(4, 20, 60, 0.8));
      border-top: 3px solid rgba(1, 153, 209, 0.7);
      border-radius: 8px;
      width: 19%;
      display: flex;
      flex-direction: column;
      position: relative;
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(1, 153, 209, 0.4);
      }
    }
  
    .card-header {
      display: flex;
      height: 22%;
      align-items: center;
      justify-content: space-between;
      padding: 12px 16px 8px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }
      .card-header-left {
        .card-title {
          font-size: 16px;
          font-weight: bold;
          color: #ffffff;
          margin-bottom: 2px;
        }
        
        .card-subtitle {
          font-size: 11px;
          color: #8a9baf;
          opacity: 0.8;
        }
      }
  
      .card-header-right {
    display: flex;
    align-items: center;
    gap: 8px; /* 状态指示器和序号之间的间距 */
  }

  .card-sequence {
    font-size: 32px;
    color: #03d3ec;
    font-weight: bold;
    opacity: 0.9;
  }
    /* 新增：图表和状态指示器容器 */
    .chart-status-container {
      height: 50%;
      display: flex;
      flex-direction: column;
      position: relative;
      margin-bottom: 2px;
    }
  
    .chart-container {
      height: 75%; /* 图表占据容器的大部分高度 */
      display: flex;
      align-items: center;
      justify-content: center;
      position: relative;
      margin-bottom: 2px;
      .ring-charts {
        width: 100%;
        height: 100%;
      }
      
      .ring-placeholder {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #5d7aa7;
        
        .placeholder-icon {
          font-size: 32px;
          margin-bottom: 4px;
          opacity: 0.5;
        }
        
        .placeholder-text {
          font-size: 16px;
          opacity: 0.7;
        }
      }
    }
  
    .status-indicator {
    padding: 4px 10px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: bold;
    white-space: nowrap;
    
    &.status-normal {
      background: rgba(0, 255, 136, 0.2);
      color: #00ff88;
      border: 1px solid rgba(0, 255, 136, 0.4);
    }
    
    &.status-warning {
      background: rgba(255, 193, 7, 0.2);
      color: #ffc107;
      border: 1px solid rgba(255, 193, 7, 0.4);
    }
    
    &.status-alert {
      background: rgba(255, 107, 107, 0.2);
      color: #ff6b6b;
      border: 1px solid rgba(255, 107, 107, 0.4);
    }
    }
  
    .card-footer {
      height: 10%;
      display: flex;
      align-items: center;
      justify-content: space-around;
      padding: 0 12px;
      gap: 8px;
    }
  
    .card-footer-item {
      padding: 8px;
      box-sizing: border-box;
      width: 48%;
      background: rgba(6, 30, 93, 0.4);
      border-radius: 6px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      transition: all 0.3s ease;
  
      &:hover {
        background: rgba(6, 30, 93, 0.6);
        border-color: rgba(1, 153, 209, 0.3);
      }
  
      .footer-title {
        font-size: 15px;
        color: #8a9baf;
        margin-bottom: 6px;
        display: flex;
        align-items: center;
        gap: 4px;
        
        .icon {
          font-size: 14px;
        }
      }
  
      .footer-detail {
        color: #1294fb;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-size: 25px;
  
        .dv-digital-flop {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 40px;
        }
        
        .unit {
          font-size: 14px;
          color: #5d7aa7;
          margin-left: 4px;
        }
      }
      
      // 为不同类型设置不同颜色
      &.area .footer-detail {
        color: #26fcd8;
      }
      
      &.report .footer-detail {
        color: #ff6b6b;
      }
    }
  }
  
  // 响应式调整
  @media (max-width: 1600px) {
    #cards {
      .card-header {
        .card-title {
          font-size: 14px;
        }
        
        .card-subtitle {
          font-size: 10px;
        }
        
        .card-header-right {
          font-size: 28px;
        }
      }
      
      .card-footer-item {
        .footer-detail {
          font-size: 12px;
        }
      }
      
    
    }
  }
  </style>