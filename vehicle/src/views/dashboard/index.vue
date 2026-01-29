<template>
    <div class="dashboard-container">
      <!-- 移除了重复的面包屑导航 -->
      <datav />
    </div>
  </template>
  
  <script>
  import datav from './components/index.vue'
  
  export default {
    name: 'Dashboard',
    components: {
      datav
    },
    computed: {
      name() {
        return this.$store.state.user.name
      }
    }
  }
  </script>
  
  <style lang="scss" scoped>
  .dashboard-container {
    height: calc(100vh - 50px); 
    overflow: hidden;
    position: relative;
    
  }

  /* 响应式调整 */
@media (max-width: 1600px) {
  .dashboard-container {
    min-width: 1100px;
  }
}

@media (max-width: 1400px) {
  .dashboard-container {
    min-width: 1000px;
  }
}

@media (max-width: 1200px) {
  .dashboard-container {
    min-width: 900px;
  }
}
  </style>