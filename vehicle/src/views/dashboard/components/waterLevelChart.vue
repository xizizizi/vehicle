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
        this.totalCompletedTasks =
          this.totalTasks - this.activeTasks - this.pendingTasks;
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
  
<style lang="scss" scoped>
#water-level-chart {
  width: 100%;
  min-height: 280px; /* 🔑 保证初始高度，避免压缩 */
  background-color: rgba(6, 30, 93, 0.5);
  border-top: 2px solid rgba(1, 153, 209, 0.5);
  display: flex;
  flex-direction: column;
  padding: 10px;
  box-sizing: border-box;

  .water-level-chart-title {
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    margin-bottom: 10px;
    flex-shrink: 0;
  }

  .water-level-chart-details {
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 20px;
    margin-top: 5px;
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
    min-height: 120px; /* 保证水位图容器最小可见 */
  }

  .task-stats-overview {
    display: flex;
    justify-content: space-around;
    align-items: center;
    background-color: rgba(6, 30, 93, 0.3);
    border-radius: 4px;
    padding: 8px 5px;
    margin-top: 10px;
    flex-shrink: 0;

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

  /* 水位图圆圈使用相对尺寸，随容器缩放 */
  .dv-water-pond-level {
    width: 80%; /* 相对于父容器宽度 */
    max-width: 140px; /* 限制最大尺寸，避免过大 */
    height: auto;
    aspect-ratio: 1 / 1; /* 保持正方形 */
    border: 8px solid #19c3eb;
    border-radius: 50%;

    ellipse {
      stroke: transparent !important;
    }
    text {
      font-size: clamp(20px, 5vw, 40px); /* 响应式字体 */
      fill: #fff;
      font-weight: bold;
    }
  }
}

/* 响应式微调 */
@media (max-width: 1400px) {
  #water-level-chart .dv-water-pond-level {
    max-width: 120px;
    text {
      font-size: 28px;
    }
  }
  .water-level-chart-details span {
    font-size: 32px;
  }
}

@media (max-width: 768px) {
  #water-level-chart {
    min-height: 240px;
  }
  .water-level-chart-title {
    font-size: 16px;
  }
  .task-stats-overview .stat-value {
    font-size: 20px;
  }
}
</style>