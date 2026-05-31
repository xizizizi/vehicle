<template>
  <div id="ranking-board">
    <div class="ranking-board-title">巡查上报记录数量</div>
    <dv-scroll-ranking-board v-if="config.data.length" :config="config" />
    <div v-else class="no-data">暂无数据</div>
  </div>
</template>
  
  <script>
import axios from "axios";

export default {
  name: "RankingBoard",
  data() {
    return {
      config: {
        data: [],
        rowNum: 9,
      },
    };
  },
  mounted() {
    this.fetchReportStats();
    // 每300秒刷新数据
    setInterval(this.fetchReportStats, 300000);
  },
  methods: {
    async fetchReportStats() {
      try {
        const response = await axios.get("/api/reports/stats");
        this.processReportData(response.data);
      } catch (error) {
        console.error("获取数据失败:", error);
      }
    },
    processReportData(reportData) {
      // 从 reportData 中提取 reportStats 数据
      const reportStats = reportData.reportStats || {};

      // 定义分类名称映射关系
      const categoryMap = {
        日常养护: "日常养护",
        路面问题: "路面问题",
        路基问题: "路基危害",
        农林病害: "农林病害",
        交通事故: "交通事故",
        除雪作业: "除雪作业",
        桥通问题: "桥通问题",
        交安设施: "交安设施",
        绿化养护: "绿化养护",
      };

      // 转换数据格式并过滤掉值为0的条目
      const data = Object.entries(reportStats)
        .filter(([_, value]) => value > 0)
        .map(([category, value]) => ({
          name: categoryMap[category] || category,
          value: value,
        }));

      // 按值降序排序
      data.sort((a, b) => b.value - a.value);

      this.config = {
        data: data,
        rowNum: Math.min(data.length, 9),
      };
    },
  },
};
</script>
  
  <style lang="scss" scoped>
#ranking-board {
  width: 100%;
  height: 100%;
  background-color: rgba(6, 30, 93, 0.5);
  border-top: 2px solid rgba(1, 153, 209, 0.5);
  display: flex;
  flex-direction: column;
  padding: 0 15px;
  box-sizing: border-box;
  overflow: hidden;

  .ranking-board-title {
    height: 50px;
    font-weight: bold;
    font-size: 18px;
    text-align: center;
    line-height: 50px;
    flex-shrink: 0;
  }

  .dv-scroll-ranking-board {
    flex: 1;
    min-height: 0;
  }
}

@media (max-width: 1366px) {
  #ranking-board {
    padding: 0 10px;
    .ranking-board-title {
      font-size: 16px;
    }
  }
}
</style>