<template>
  <div id="data-view">
    <div class="dashboard-container">
      <top-header />
      <div class="main-content">
        <digital-flop />

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
    return {}
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

/* 响应式调整 */
@media (max-width: 1920px) {
  #data-view .dashboard-container {
    padding: 15px;
  }
  
  #data-view .main-layout-container,
  #data-view .right-top-row {
    gap: 15px;
  }
  
  #data-view .left-column {
    width: 260px;
    min-width: 240px;
  }
}

@media (max-width: 1600px) {
  #data-view .dashboard-container {
    padding: 12px;
    min-width: 1100px;
  }
  
  #data-view .main-layout-container,
  #data-view .right-top-row {
    gap: 12px;
  }
  
  #data-view .left-column {
    width: 240px;
    min-width: 220px;
  }
  
  /* 在中等屏幕下，右侧上方三个组件改为垂直排列 */
  #data-view .right-top-row {
    flex-direction: column;
    
    & > * {
      height: 33.33%; /* 每个组件占据1/3高度 */
      flex: none;
    }
  }
}

@media (max-width: 1400px) {
  #data-view .dashboard-container {
    padding: 10px;
    min-width: 1000px;
  }
  
  #data-view .main-layout-container,
  #data-view .right-top-row {
    gap: 10px;
  }
  
  #data-view .left-column {
    width: 220px;
    min-width: 200px;
  }
}

@media (max-width: 1200px) {
  #data-view .dashboard-container {
    min-width: 900px;
    padding: 8px;
  }
  
  #data-view .left-column {
    width: 200px;
    min-width: 180px;
  }
}

/* 超小屏幕 - 改为垂直布局 */
@media (max-width: 1000px) {
  #data-view .dashboard-container {
    min-width: 800px;
  }
  
  #data-view .main-layout-container {
    flex-direction: column;
  }
  
  #data-view .left-column {
    width: 100%;
    height: 300px;
    margin-bottom: 15px;
  }
  
  #data-view .right-column {
    flex: 1;
  }
  
  #data-view .right-top-row > * {
    height: 200px;
  }
}
</style>