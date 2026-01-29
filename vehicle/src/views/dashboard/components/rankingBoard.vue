<template>
    <div id="ranking-board">
      <div class="ranking-board-title">巡查上报记录数量</div>
      <dv-scroll-ranking-board v-if="config.data.length" :config="config" />
      <div v-else class="no-data">暂无数据</div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'RankingBoard',
    data() {
      return {
        config: {
          data: [],
          rowNum: 9
        }
      }
    },
    mounted() {
      this.fetchReportStats();
      // 每300秒刷新数据
      setInterval(this.fetchReportStats, 300000);
    },
    methods: {
      async fetchReportStats() {
        try {
          const response = await axios.get('/api/reports/stats');
          this.processReportData(response.data);
        } catch (error) {
          console.error('获取数据失败:', error);
        }
      },
      processReportData(reportData) {
        // 从 reportData 中提取 reportStats 数据
        const reportStats = reportData.reportStats || {};
        
        // 定义分类名称映射关系
        const categoryMap = {
          '日常养护': '日常养护',
          '路面问题': '路面问题',
          '路基问题': '路基危害',
          '农林病害': '农林病害',
          '交通事故': '交通事故',
          '除雪作业': '除雪作业',
          '桥通问题': '桥通问题',
          '交安设施': '交安设施',
          '绿化养护': '绿化养护'
        };
  
        // 转换数据格式并过滤掉值为0的条目
        const data = Object.entries(reportStats)
          .filter(([_, value]) => value > 0)
          .map(([category, value]) => ({
            name: categoryMap[category] || category,
            value: value
          }));
  
        // 按值降序排序
        data.sort((a, b) => b.value - a.value);
  
        this.config = {
          data: data,
          rowNum: Math.min(data.length, 9)
        };
      }
    }
  }
  </script>
  
  <style lang="scss">
  #ranking-board {
    width: 100%;
  height: 100%; /* 确保填满父容器 */
   // width: 280px; /* 改为固定宽度 */
    min-width: 250px; /* 最小宽度 */
    box-shadow: 0 0 3px blue;
    display: flex;
    flex-direction: column;
    background-color: rgba(6, 30, 93, 0.5);
    border-top: 2px solid rgba(1, 153, 209, .5);
    box-sizing: border-box;
    padding: 0 20px; /* 减少左右内边距 */
    margin-right: 15px; /* 添加右边距与其他组件分隔 */
  
    .ranking-board-title {
      font-weight: bold;
      height: 50px;
      display: flex;
      align-items: center;
      font-size: 18px; /* 稍微减小字体 */
      text-align: center;
      justify-content: center;
    }
  
    .dv-scroll-ranking-board {
      flex: 1;
      
      /* 调整内部样式 */
      ::v-deep .row-item {
        padding: 4px 2px;
      }
      
      ::v-deep .ranking-info {
        font-size: 12px;
      }
    }
  
    .no-data {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #999;
      font-size: 14px;
    }
  }
  
  /* 响应式调整 */
  @media (max-width: 1600px) {
    #ranking-board {
      width: 240px;
      min-width: 220px;
      padding: 0 15px;
      
      .ranking-board-title {
        font-size: 16px;
      }
    }
  }
  
  @media (max-width: 500px) {
    #ranking-board {
      width: 220px;
      min-width: 200px;
      padding: 0 10px;
    }
  }

  
  </style>