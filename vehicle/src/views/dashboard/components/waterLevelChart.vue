<template>
  <div id="water-level-chart">
    <div class="water-level-chart-title">
      调度任务累计完成

      <div class="water-level-chart-details">
        <span>{{ totalCompletedTasks }}</span
        >个
      </div>
    </div>

    <div class="chart-container">
      <dv-water-level-pond :config="config" />
    </div>

    <!-- 添加任务状态概览 -->
    <div class="task-stats-overview">
      <div class="stat-item">
        <div class="stat-label">待处理</div>
        <div class="stat-value">{{ pendingTasks }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">执行中</div>
        <div class="stat-value">{{ activeTasks }}</div>
      </div>
      <div class="stat-item">
        <div class="stat-label">总任务</div>
        <div class="stat-value">{{ totalTasks }}</div>
      </div>
    </div>
  </div>
</template>
  
  <script>
import axios from "axios";

export default {
  name: "WaterLevelChart",
  data() {
    return {
      config: {
        data: [0],
        shape: "round",
        waveHeight: 25,
        waveNum: 2,
        formatter: "{value}%",
      },
      totalCompletedTasks: 0,
      completionRate: 0,
      pendingTasks: 0,
      activeTasks: 0,
      totalTasks: 0,
    };
  },
  methods: {
    async fetchData() {
      try {
        const response = await axios.get(
          "http://localhost:8080/api/reports/task-execution-stats"
        );
        const stats = response.data;
        console.log("获取任务执行统计:", stats);

        // 更新数据
        this.completionRate = stats.completionRate || 0;
        this.totalCompletedTasks = stats.totalCompletedTasks || 0;
        this.pendingTasks = stats.pendingTasks || 0;
        this.activeTasks = stats.activeTasks || 0;
        this.totalTasks = stats.totalTasks || 0;
        this.totalCompletedTasks = this.totalTasks - this.activeTasks - this.pendingTasks;
        // 更新水位图配置
        this.config = {
          ...this.config,
          data: [this.completionRate],
        };
      } catch (error) {
        console.error("获取任务执行统计失败:", error);
        // 设置默认值
        this.config.data = [0];
        this.totalCompletedTasks = 0;
        this.pendingTasks = 0;
        this.activeTasks = 0;
        this.totalTasks = 0;
      }
    },
  },
  mounted() {
    this.fetchData();
    // 每300秒刷新数据
    setInterval(this.fetchData, 300000);
  },
};
</script>
  
  <style lang="scss">
#water-level-chart {
  width: 100%;
  height: 100%; /* 确保填满父容器 */
  box-sizing: border-box;
  margin-left: 20px;
  background-color: rgba(6, 30, 93, 0.5);
  border-top: 2px solid rgba(1, 153, 209, 0.5);
  display: flex;
  flex-direction: column;
  padding: 10px;

  .water-level-chart-title {
    font-weight: bold;
    height: 14px;
    display: flex;
    align-items: center;
    font-size: 20px;
    justify-content: center;
    margin-bottom: 10px;
  }

  .water-level-chart-details {
    height: 30px;
    display: flex;
    justify-content: center;
    font-size: 20px;
    align-items: center;
    margin-bottom: 0px;

    span {
      font-size: 40px;
      font-weight: bold;
      color: #58a1ff;
      margin: 0 8px;
    }
  }

  .chart-container {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 120px;
  }

  .task-stats-overview {
    display: flex;
    justify-content: space-around;
    height: 30px;
    align-items: center;
    background-color: rgba(6, 30, 93, 0.3);
    border-radius: 4px;
    padding: 5px;
    margin-top: 10px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      flex: 1;

      .stat-label {
        font-size: 16px;
        color: #8a9baf;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 25px;
        font-weight: bold;
        color: #26fcd8;
      }
    }
  }

  .dv-water-pond-level {
    width: 160px;
    height: 160px;
    border: 8px solid #19c3eb;
    border-radius: 50%;

    ellipse {
      stroke: transparent !important;
    }

    text {
      font-size: 40px;
      fill: #fff;
      font-weight: bold;
    }
  }
}

/* 响应式调整 */
@media (max-width: 1400px) {
  #water-level-chart {
    .dv-water-pond-level {
      width: 140px;
      height: 140px;

      text {
        font-size: 28px;
      }
    }

    .water-level-chart-details span {
      font-size: 24px;
    }
  }
}

/* 在 WaterLevelChart 组件的样式中添加 */
@media (max-width: 1600px) {
  #water-level-chart {
    margin-left: 15px;

    .water-level-chart-title {
      font-size: 18px;
      height: 45px;
    }

    .dv-water-pond-level {
      width: 140px;
      height: 140px;

      text {
        font-size: 32px;
      }
    }
  }
}

@media (max-width: 1400px) {
  #water-level-chart {
    margin-left: 10px;

    .dv-water-pond-level {
      width: 120px;
      height: 120px;

      text {
        font-size: 28px;
      }
    }
  }
}
</style>