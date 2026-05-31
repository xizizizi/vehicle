<template>
    <div class="ranking-board">
      <!-- 顶部区域：巡查类型排行 -->
      <div class="board-section">
        <div class="board-title">巡查任务类型排行</div>
        <dv-scroll-ranking-board
          :config="rankingConfig"
          style="height:300px;"
          @row-click="onRankClick"
        />
      </div>
  
      <!-- 中部区域：最新任务上报 -->
      <div class="board-section">
        <div class="board-title">最新巡查上报</div>
        <dv-scroll-board
          :config="scrollConfig"
          style="height:680px;"
          @row-click="onTaskClick"
        />
      </div>
  
      <!-- 底部区域：区域上报统计 -->
      <div class="board-section">
        <div class="board-title">区域上报统计</div>
        <div class="area-cards">
          <div
            v-for="card in areaCards"
            :key="card.title"
            class="area-card"
            @click="onAreaClick(card)"
          >
            <div class="card-title">{{ card.title }}</div>
            <div class="card-value">{{ card.value }}</div>
          </div>
        </div>
      </div>
  
      <!-- 弹窗：任务详情 -->
      <el-dialog
        v-model="dialogVisible"
        width="600px"
        title="任务详情"
        class="dark-dialog"
      >
        <div v-if="selectedTask">
          <p><strong>任务名称：</strong>{{ selectedTask.task_name }}</p>
          <p><strong>上报时间：</strong>{{ selectedTask.task_time }}</p>
          <p><strong>上报数量：</strong>{{ selectedTask.quantity }}</p>
          <p><strong>状态：</strong>{{ selectedTask.status }}</p>
          <p><strong>说明：</strong>{{ selectedTask.details || '暂无详细信息' }}</p>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import axios from 'axios';
  
  export default {
    name: 'RankingBoard',
    data() {
      return {
        rankingConfig: {
          data: [],
          unit: '次',
          waitTime: 2000,
          rowNum: 7,
          valueFormatter: (v) => `${v}`,
        },
        scrollConfig: {
          header: ['上报时间', '任务名称', '数量', '状态'],
          data: [],
          rowNum: 7,
          waitTime: 1500,
        },
        areaCards: [],
        dialogVisible: false,
        selectedTask: null,
      };
    },
    mounted() {
      this.loadRankingData();
      this.loadLatestReports();
      this.loadAreaStats();
  
      // 定时刷新
      setInterval(this.loadRankingData, 20000);
      setInterval(this.loadLatestReports, 15000);
      setInterval(this.loadAreaStats, 30000);
    },
    methods: {
      // 获取任务类型排行
      loadRankingData() {
        axios
          .get('/api/reports/ranking')
          .then((res) => {
            this.rankingConfig.data = res.data.map((item) => ({
              name: item.type_name,
              value: item.report_count,
            }));
          })
          .catch(() => console.warn('未获取到排行数据'));
      },
  
      // 获取最新任务上报
      loadLatestReports() {
        axios
          .get('/api/reports/latest')
          .then((res) => {
            this.scrollConfig.data = res.data.map((item) => [
              item.report_time,
              item.task_name,
              item.quantity,
              item.status,
            ]);
          })
          .catch(() => console.warn('未获取到最新任务数据'));
      },
  
      // 获取区域统计
      loadAreaStats() {
        axios
          .get('/api/reports/area')
          .then((res) => {
            this.areaCards = res.data.map((area) => ({
              title: area.region_name,
              value: area.report_count,
            }));
          })
          .catch(() => console.warn('未获取到区域数据'));
      },
  
      // 点击排行项目
      onRankClick(row) {
        const typeName = row.name;
        axios
          .get(`/api/reports/details?type=${encodeURIComponent(typeName)}`)
          .then((res) => {
            this.$message.info(`【${typeName}】共有 ${res.data.count} 条上报记录`);
          })
          .catch(() => {
            this.$message.error('无法获取任务类型详情');
          });
      },
  
      // 点击任务行
      onTaskClick(row) {
        this.selectedTask = {
          task_time: row[0],
          task_name: row[1],
          quantity: row[2],
          status: row[3],
        };
        this.dialogVisible = true;
      },
  
      // 点击区域卡片
      onAreaClick(card) {
        this.$message.info(`正在查看 ${card.title} 区域详情`);
      },
    },
  };
  </script>
  
  <style scoped>
  .ranking-board {
    width: 100%;
    height: 100%;
    color: #ccefff;
    display: flex;
    flex-direction: column;
    gap: 20px;
    font-family: 'Orbitron', 'Microsoft YaHei', sans-serif;
  }
  
  .board-section {
    background: rgba(10, 20, 40, 0.65);
    border: 1px solid rgba(0, 242, 255, 0.25);
    border-radius: 12px;
    box-shadow: 0 0 12px rgba(0, 255, 255, 0.1);
    padding: 15px;
  }
  
  .board-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 10px;
    color: #00eaff;
    text-shadow: 0 0 8px rgba(0, 234, 255, 0.5);
    border-left: 4px solid #00eaff;
    padding-left: 10px;
  }
  
  .area-cards {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .area-card {
    flex: 1 1 30%;
    min-width: 160px;
    background: rgba(0, 40, 80, 0.7);
    border: 1px solid rgba(0, 200, 255, 0.3);
    border-radius: 10px;
    text-align: center;
    padding: 15px;
    cursor: pointer;
    transition: all 0.3s ease;
  }
  .area-card:hover {
    box-shadow: 0 0 12px rgba(0, 255, 255, 0.6);
    transform: scale(1.03);
  }
  .card-title {
    color: #00d4ff;
    font-size: 16px;
    margin-bottom: 8px;
  }
  .card-value {
    font-size: 22px;
    font-weight: bold;
    color: #ffffff;
  }
  
  /* 弹窗玻璃拟态风格 */
  .dark-dialog {
    background: rgba(10, 20, 40, 0.85);
    color: #c8e7ff;
    border: 1px solid rgba(3, 211, 236, 0.3);
    backdrop-filter: blur(8px);
  }
  .dark-dialog .el-dialog__header {
    border-bottom: 1px solid rgba(3, 211, 236, 0.2);
    color: #00eaff;
  }
  .dark-dialog .el-dialog__body {
    color: #c8e7ff;
    line-height: 1.8;
  }
  </style>
  