<template>
  <div id="scroll-board">
    <dv-scroll-board
      v-if="config.data && config.data.length"
      :config="config"
    />
    <div v-else class="loading">加载中...</div>
  </div>
</template>
  
  <script>
import axios from "axios";

// 创建指向后端的axios实例
const apiClient = axios.create({
  baseURL: "http://localhost:8080", // 后端API地址
  timeout: 5000,
});

// 状态映射关系
const statusMap = {
  COMPLETED: "已完成",
  EXECUTING: "执行中",
  PENDING: "待处理",
};

// 任务类型映射（根据您的后端数据）
const taskTypeMap = {
  1: "交安设施",
  2: "消防救援",
  3: "测绘",
  4: "物流",
  5: "安防巡逻",
  6: "活动拍摄",
  7: "环境调查",
  8: "设备维护",
  9: "农林监测",
};

export default {
  name: "ScrollBoard",
  data() {
    return {
      config: {
        header: ["时间", "任务详情列表", "数量", "执行状态"],
        data: [],
        index: true,
        columnWidth: [50, 150, 300],
        align: ["center"],
        rowNum: 7,
        headerBGC: "#1981f6",
        headerHeight: 45,
        oddRowBGC: "rgba(0, 44, 81, 0.8)",
        evenRowBGC: "rgba(10, 29, 50, 0.8)",
      },
    };
  },
  methods: {
    async fetchData() {
      try {
        // 1. 获取最新任务数据
        const tasksResponse = await apiClient.get("/tasks/latest");
        const tasks = tasksResponse.data;

        // 2. 转换数据格式
        const tableData = tasks.map((task) => {
          // 使用映射获取类型名称
          const typeName =
            taskTypeMap[task.taskTypeId] || `类型${task.taskTypeId}`;
          return [
            task.taskTime,
            `${typeName}-${task.taskName}`,
            task.quantity,
            statusMap[task.status] || task.status,
          ];
        });

        // 3. 更新表格配置
        this.config = {
          ...this.config,
          data: tableData,
        };
      } catch (error) {
        console.error("获取数据失败:", error);
      }
    },
  },
  mounted() {
    this.fetchData();
    setInterval(this.fetchData, 100000); // 每100秒刷新一次
  },
};
</script>
  
  <style lang="scss" scoped>
#scroll-board {
  width: 100%;
  min-height: 280px; /* 保证初始高度 */
  background-color: rgba(6, 30, 93, 0.5);
  border-top: 2px solid rgba(1, 153, 209, 0.5);
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  .dv-scroll-board {
    flex: 1;
    width: 100%;
    min-height: 0;
  }

  .loading {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
}
</style>