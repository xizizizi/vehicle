<template>
  <div class="dashboard-container">
    <!-- 移除了重复的面包屑导航 -->
    <datav />
  </div>
</template>
  
  <script>
import datav from "./components/index.vue";

export default {
  name: "Dashboard",
  components: {
    datav,
  },
  computed: {
    name() {
      return this.$store.state.user.name;
    },
  },
};
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
    background-image: url("./components/img/bg.png");
    background-size: cover;
    display: flex;
    flex-direction: column;
    padding: 20px;
    box-sizing: border-box;
    overflow-y: auto; /* 确保垂直滚动 */
    overflow-x: auto;
    /* 不要设置 min-height: 100% 之类的冲突属性 */
  }

  .main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;     /* 关键：允许子内容滚动 */
  overflow: visible; /* 或 auto */
}
  /* 主要布局容器：窄屏换行，弹性宽度 */
  .main-layout-container {
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-top: 20px;
    min-height: 0;
  }

  /* 左侧列：弹性基础 280px，允许缩小到 250px */
  .left-column {
    flex: 1 1 280px;
    min-width: 250px;
    display: flex;
    flex-direction: column;
  }

  /* 右侧列：占据剩余空间，也允许缩小 */
  .right-column {
    flex: 3 1 500px;
    min-width: 280px;
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .right-top-row {
    flex: 0 0 auto; /* 不伸缩，根据内容高度 */
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    min-height: 320px; /* 保证足够显示，但不至于无限长 */
    margin-bottom: 20px;

    & > * {
      flex: 1 1 280px;
      min-width: 0;
      height: 320px; /* 固定每个组件的高度，内部滚动或自适应 */
    }
  }

  .right-bottom-row {
    flex: 1; /* 占据剩余空间，让卡片区域可滚动 */
    min-height: 200px;
    overflow-y: auto; /* 如果卡片太多，允许内部滚动 */
    display: flex;
  }

  /* 多领域调度卡片区域响应式 */
  .domain-quick-nav {
    margin: 20px 0;
    background: rgba(6, 30, 93, 0.5);
    border-radius: 12px;
    padding: 20px;
    border: 1px solid rgba(0, 240, 255, 0.3);

    .nav-header {
      flex-wrap: wrap;
      gap: 10px;
    }

    .nav-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 20px;
    }

    .nav-card .card-title {
      white-space: normal;
      word-break: break-word;
    }
  }
}

/* 窄屏（< 1024px）时左右列上下排列 */
@media (max-width: 1024px) {
  #data-view .main-layout-container {
    flex-direction: column;
  }
  #data-view .left-column {
    flex-basis: auto;
  }
}
</style>