<template>
    <div id="rose-chart">
      <div class="rose-chart-title">本周任务完成情况</div>
      <dv-charts v-if="option.series && option.series[0].data.length" :option="option" />
      <div v-else class="loading">加载数据中...</div>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  // 创建指向后端的axios实例
  const apiClient = axios.create({
    baseURL: 'http://localhost:8080', // 后端API地址
    timeout: 5000
  });
  
  // 颜色映射
  const colorMap = {
    '交通事故': '#da2f00',
    '消防救援': '#fa3600',
    '测绘': '#ff4411',
    '物流': '#82724c',
    '安防巡逻': '#541200',
    '活动拍摄': '#801b00',
    '环境调查': '#a02200',
    '设备维护': '#5d1400',
    '农林监测': '#b72700'
  };
  
  export default {
    name: 'RoseChart',
    data() {
      return {
        option: {
            
          series: [{
            type: 'pie',
            radius: ['30%', '70%'], // 增大饼图半径，减少空白
            center: ['50%', '55%'], // 调整饼图中心位置向下一点
            roseSort: false,
            data: [],
            insideLabel: { show: false },
            outsideLabel: {
              formatter: '{name} {percent}%',
              labelLineEndLength: 20,
              style: { fill: '#fff' },
              labelLineStyle: { stroke: '#fff' }
            },
            roseType: false // 设置为false保持圆形饼图
          }],
          color: Object.values(colorMap) // 使用预定义的颜色
        }
      }
    },
    methods: {
      async fetchData() {
        try {
          // 1. 从后端API获取数据
          const response = await apiClient.get('/tasks/weekly-completion');
          const completionData = response.data;
          
          // 2. 转换数据格式
          const chartData = completionData.map(item => ({
            name: item.type,
            value: item.count,
            itemStyle: { color: colorMap[item.type] || '#ffffff' }
          }));
          
          // 3. 更新图表数据
          this.option.series[0].data = chartData;
        } catch (error) {
          console.error('获取数据失败:', error);
        }
      }
    },
    mounted() {
      this.fetchData();
      // 每300秒刷新一次数据
      setInterval(this.fetchData, 300000);
    }
  }
  </script>
  
  <style lang="scss">
  #rose-chart {
    width: 100%;
    height: 100%;
    background-color: rgba(6, 30, 93, 0.5);
    border-top: 2px solid rgba(1, 153, 209, .5);
    box-sizing: border-box;
  
    .rose-chart-title {
      height: 50px;
      font-weight: bold;
      text-indent: 20px;
      font-size: 20px;
      display: flex;
      align-items: center;
      color: #fff;
    }
  
    .dv-charts-container {
      height: calc(100% - 50px);
    }
    
    .loading {
      height: calc(100% - 10px);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 16px;
    }
  }
  </style>