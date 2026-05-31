<template>
    <div>
      <HeaderComponent />
      <div class="main-container">
        <SidebarComponent 
          :selectedLocation="selectedLocation"
          @select-location="selectLocation"
          @clear-selection="clearSelection"
          @route-plan="handleRoutePlan"
        />
        <MapComponent 
          :selectedLocation="selectedLocation"
          @location-selected="selectLocation"
        />
      </div>
    </div>
  </template>
  
  <script>
  import HeaderComponent from './HeaderComponent.vue'
  import SidebarComponent from './SidebarComponent.vue'
  import MapComponent from './MapComponent.vue'
  
  export default {
    name: 'NanchangPOISystem',
    components: {
      HeaderComponent,
      SidebarComponent,
      MapComponent
    },
    data() {
      return {
        selectedLocation: null
      }
    },
    methods: {
      selectLocation(location) {
        this.selectedLocation = location
      },
      clearSelection() {
        this.selectedLocation = null
      },
      handleRoutePlan() {
        if (this.selectedLocation) {
          alert(`开始从当前位置到 ${this.selectedLocation.name} 的路径规划\n坐标: ${this.selectedLocation.lat.toFixed(6)}, ${this.selectedLocation.lng.toFixed(6)}`)
          // 这里可以调用后端的路径规划API
        } else {
          alert('请先选择一个位置')
        }
      }
    }
  }
  </script>
  
  <style scoped>
  .main-container {
    display: flex;
    min-height: calc(100vh - 120px);
  }
  
  @media (max-width: 768px) {
    .main-container {
      flex-direction: column;
    }
  }
  </style>