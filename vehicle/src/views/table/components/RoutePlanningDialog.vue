<template>
    <el-dialog
      :visible.sync="dialogVisible"
      title="路径规划结果"
      width="98%"
      top="1vh"
      :before-close="handleClose"
      class="route-planning-dialog"
      custom-class="route-dialog-custom"
      :modal="false"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      :show-close="true"
      :lock-scroll="false"
      :fullscreen="isFullscreen"
      @open="onDialogOpen"
    >
      <div slot="title" class="dialog-header">
        <span>路径规划结果</span>
        <div class="header-actions">
          <el-button 
            type="text" 
            @click="toggleFullscreen"
            :title="isFullscreen ? '退出全屏' : '全屏显示'"
          >
            <i :class="isFullscreen ? 'el-icon-close' : 'el-icon-full-screen'"></i>
          </el-button>
          <el-button 
            type="text" 
            @click="handleClose"
            title="关闭"
          >
            <i class="el-icon-close"></i>
          </el-button>
        </div>
      </div>
  
      <div class="route-planning-container">
        <div class="route-info-panel">
          <el-card class="info-card">
            <div slot="header" class="clearfix">
              <span>路径信息</span>
            </div>
            
            <div class="route-summary">
              <div class="summary-item">
                <div class="label">起点</div>
                <div class="value">{{ routeData.startLocation }}</div>
              </div>
              <div class="summary-item">
                <div class="label">终点</div>
                <div class="value">{{ routeData.endLocation }}</div>
              </div>
              <div class="summary-item">
                <div class="label">总距离</div>
                <div class="value">{{ (routeInfo.totalDistance / 1000).toFixed(2) }} km</div>
              </div>
              <div class="summary-item">
                <div class="label">预计时间</div>
                <div class="value">{{ Math.round(routeInfo.estimatedTime / 60) }} 分钟</div>
              </div>
              <div class="summary-item">
                <div class="label">航点数量</div>
                <div class="value">{{ routeInfo.waypointCount }} 个</div>
              </div>
            </div>
  
            <div class="action-buttons">
              <el-button 
                type="primary" 
                @click="useRouteForMission"
                style="width: 100%; margin-bottom: 10px;"
              >
                使用此路径创建任务
              </el-button>
              <!-- 新增：在地图上显示按钮 -->
  <el-button 
    type="info" 
    @click="showOnMap"
    style="width: 100%; margin-bottom: 10px;"
  >
    在地图上显示路径
  </el-button>
  <el-button 
    @click="exportRoute"
    style="width: 100%; margin-bottom: 10px;"
  >
    导出路径数据
  </el-button>
  <el-button 
    type="success" 
    @click="toggleAnimation"
    style="width: 100%;"
  >
    {{ isAnimating ? '暂停动画' : '播放动画' }}
  </el-button>
            </div>
          </el-card>
  
          <el-card class="waypoints-card">
            <div slot="header">
              <span>途径站点 ({{ routeInfo.path.length }})</span>
            </div>
            <div class="waypoints-list">
              <div 
                v-for="(point, index) in routeInfo.path" 
                :key="index"
                class="waypoint-item"
                :class="{ 
                  active: currentWaypointIndex === index,
                  start: index === 0,
                  end: index === routeInfo.path.length - 1
                }"
              >
                <div class="waypoint-marker">
                  <div class="marker-icon">
                    <i v-if="index === 0" class="el-icon-position start-icon"></i>
                    <i v-else-if="index === routeInfo.path.length - 1" class="el-icon-location end-icon"></i>
                    <div v-else class="waypoint-dot"></div>
                  </div>
                  <div class="waypoint-info">
  <div class="waypoint-name">
    {{ point.locationName || getWaypointName(index) }}
  </div>
  <div class="waypoint-coords">
    {{ point.lng.toFixed(6) }}, {{ point.lat.toFixed(6) }}
  </div>
  <div class="waypoint-altitude">
    高度: {{ point.altitude || 100 }}米
  </div>
</div>
                  <div class="waypoint-distance" v-if="index > 0">
                    +{{ calculateSegmentDistance(index) }}m
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
  
        <div class="route-animation-panel">
          <el-card class="animation-card">
            <div slot="header">
              <span>路径预览</span>
              <div class="animation-controls">
                <el-button 
                  size="mini" 
                  @click="resetAnimation"
                  :disabled="isAnimating"
                >
                  <i class="el-icon-refresh"></i> 重置
                </el-button>
                <el-button 
                  size="mini" 
                  :type="isAnimating ? 'warning' : 'success'"
                  @click="toggleAnimation"
                >
                  <i :class="isAnimating ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
                  {{ isAnimating ? '暂停' : '播放' }}
                </el-button>
                <el-slider
                  v-model="animationProgress"
                  :min="0"
                  :max="100"
                  :step="1"
                  style="width: 200px; margin: 0 15px;"
                  @input="onProgressChange"
                ></el-slider>
                <span class="progress-text">{{ animationProgress }}%</span>
              </div>
            </div>
  
            <div class="animation-container">
              <div class="route-map" ref="routeMap">
                <div class="map-background">
                  <!-- 网格背景 -->
                  <div class="grid-lines">
                    <div 
                      v-for="i in 10" 
                      :key="'h-' + i" 
                      class="grid-line horizontal"
                      :style="{ top: (i * 10) + '%' }"
                    ></div>
                    <div 
                      v-for="i in 10" 
                      :key="'v-' + i" 
                      class="grid-line vertical"
                      :style="{ left: (i * 10) + '%' }"
                    ></div>
                  </div>
                  
                  <!-- SVG 路径 -->
                  <svg 
                    class="route-svg" 
                    :width="animationWidth" 
                    :height="animationHeight"
                    ref="routeSvg"
                  >
                    <!-- 完整路径线 -->
                    <path 
                      :d="routePath" 
                      fill="none" 
                      stroke="#e0e0e0" 
                      stroke-width="4"
                      stroke-dasharray="5,5"
                    />
                    
                    <!-- 已完成的路径线 -->
                    <path 
                      :d="completedPath" 
                      fill="none" 
                      stroke="#1890ff" 
                      stroke-width="4"
                      class="completed-path"
                    />
                    
                    <!-- 途径站点标记 -->
                    <circle 
                      v-for="(point, index) in scaledPath" 
                      :key="'point-' + index"
                      :cx="point.x"
                      :cy="point.y"
                      :r="getPointRadius(index)"
                      :fill="getPointColor(index)"
                      :stroke="getPointStrokeColor(index)"
                      stroke-width="2"
                      class="waypoint-marker"
                    />
                    
                    <!-- 当前无人机位置 -->
                    <circle 
                      v-if="currentDronePosition"
                      :cx="currentDronePosition.x"
                      :cy="currentDronePosition.y"
                      r="10"
                      fill="#ff4d4f"
                      stroke="#fff"
                      stroke-width="3"
                      class="drone-marker"
                    >
                      <animate 
                        attributeName="r" 
                        values="10;14;10" 
                        dur="1s" 
                        repeatCount="indefinite"
                      />
                    </circle>
                    
                    <!-- 无人机图标 -->
                    <text 
                      v-if="currentDronePosition"
                      :x="currentDronePosition.x"
                      :y="currentDronePosition.y - 20"
                      text-anchor="middle"
                      fill="#ff4d4f"
                      font-size="16"
                      font-weight="bold"
                      class="drone-label"
                    >
                      ✈
                    </text>
                  </svg>
                  
                <!-- 起点终点标签 -->
<div 
  v-if="scaledPath.length > 0"
  class="start-label map-label"
  :style="getLabelStyle(0)"
>
  <i class="el-icon-position"></i>
  <span class="label-text">{{ routeInfo.path[0].locationName || routeData.startLocation }}</span>
</div>

<div 
  v-if="scaledPath.length > 0"
  class="end-label map-label"
  :style="getLabelStyle(scaledPath.length - 1)"
>
  <i class="el-icon-location"></i>
  <span class="label-text">{{ routeInfo.path[routeInfo.path.length - 1].locationName || routeData.endLocation }}</span>
</div>
                </div>
                
                <!-- 实时信息面板 -->
                <div class="real-time-info">
                  <div class="info-header">实时飞行信息</div>
                  <div class="info-item">
                    <span class="label">当前位置:</span>
                    <span class="value">{{ currentWaypointName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">完成进度:</span>
                    <span class="value">{{ animationProgress }}%</span>
                  </div>
                  <div class="info-item">
                    <span class="label">已飞行:</span>
                    <span class="value">{{ completedDistance.toFixed(0) }} 米</span>
                  </div>
                  <div class="info-item">
                    <span class="label">剩余距离:</span>
                    <span class="value">{{ (routeInfo.totalDistance - completedDistance).toFixed(0) }} 米</span>
                  </div>
                  <div class="info-item">
                    <span class="label">当前高度:</span>
                    <span class="value">{{ getCurrentAltitude() }} 米</span>
                  </div>
                  <div class="info-item">
                    <span class="label">飞行状态:</span>
                    <span class="value" :style="{ color: isAnimating ? '#52c41a' : '#faad14' }">
                      {{ isAnimating ? '飞行中' : '已暂停' }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </el-dialog>
  </template>
  
  <script>
  export default {
    name: 'RoutePlanningDialog',
    props: {
      visible: {
        type: Boolean,
        default: false
      },
      routeData: {
        type: Object,
        default: () => ({
          startLocation: '',
          endLocation: '',
          startPoint: null,
          endPoint: null
        })
      },
      routeInfo: {
        type: Object,
        default: () => ({
          path: [],
          totalDistance: 0,
          estimatedTime: 0,
          waypointCount: 0
        })
      },
      locations: {
    type: Object,
    default: () => ({})
  }
    },
    data() {
      return {
        isAnimating: false,
        animationProgress: 0,
        animationInterval: null,
        animationSpeed: 30, // 加快动画速度
        currentWaypointIndex: 0,
        completedDistance: 0,
        animationWidth: 1200, // 增加默认宽度
        animationHeight: 600, // 增加默认高度
        isFullscreen: false,
        resizeObserver: null
      }
    },
    computed: {
      dialogVisible: {
        get() {
          return this.visible
        },
        set(value) {
          this.$emit('update:visible', value)
        }
      },
      
      computedPathBounds() {
        if (!this.routeInfo.path || this.routeInfo.path.length === 0) {
          return { minLng: 115.7, maxLng: 116.1, minLat: 28.4, maxLat: 29.0 }
        }
        
        const lngs = this.routeInfo.path.map(p => p.lng)
        const lats = this.routeInfo.path.map(p => p.lat)
        
        const lngRange = Math.max(0.1, Math.max(...lngs) - Math.min(...lngs))
        const latRange = Math.max(0.1, Math.max(...lats) - Math.min(...lats))
        
        return {
          minLng: Math.min(...lngs) - lngRange * 0.1,
          maxLng: Math.max(...lngs) + lngRange * 0.1,
          minLat: Math.min(...lats) - latRange * 0.1,
          maxLat: Math.max(...lats) + latRange * 0.1
        }
      },
      
      scaledPath() {
        const bounds = this.computedPathBounds
        const padding = 60 // 增加边距
        
        return this.routeInfo.path.map(point => {
          const x = ((point.lng - bounds.minLng) / (bounds.maxLng - bounds.minLng)) * 
                   (this.animationWidth - padding * 2) + padding
          const y = this.animationHeight - 
                   ((point.lat - bounds.minLat) / (bounds.maxLat - bounds.minLat)) * 
                   (this.animationHeight - padding * 2) - padding
          return { x, y }
        })
      },
      
      routePath() {
        if (this.scaledPath.length === 0) return ''
        return 'M ' + this.scaledPath.map(p => `${p.x},${p.y}`).join(' L ')
      },
      
      completedPath() {
        if (this.scaledPath.length === 0) return ''
        
        const progressIndex = Math.floor((this.animationProgress / 100) * (this.scaledPath.length - 1))
        const points = this.scaledPath.slice(0, progressIndex + 1)
        
        return points.length > 1 ? 'M ' + points.map(p => `${p.x},${p.y}`).join(' L ') : ''
      },
      
      currentDronePosition() {
        if (this.scaledPath.length === 0) return null
        
        const progress = this.animationProgress / 100
        const totalPoints = this.scaledPath.length
        const exactIndex = progress * (totalPoints - 1)
        const index = Math.floor(exactIndex)
        const fraction = exactIndex - index
        
        if (index >= totalPoints - 1) {
          return this.scaledPath[totalPoints - 1]
        }
        
        const p1 = this.scaledPath[index]
        const p2 = this.scaledPath[index + 1]
        
        return {
          x: p1.x + (p2.x - p1.x) * fraction,
          y: p1.y + (p2.y - p1.y) * fraction
        }
      },
      
      currentWaypointName() {
        if (this.currentWaypointIndex === 0) return '起点'
        if (this.currentWaypointIndex === this.routeInfo.path.length - 1) return '终点'
        return `航点 ${this.currentWaypointIndex}`
      }
    },
    watch: {
      visible(newVal) {
        if (newVal) {
          this.initAnimation()
          this.$nextTick(() => {
            this.updateAnimationSize()
            // 自动开始动画
            setTimeout(() => {
              this.startAnimation()
            }, 500)
          })
        } else {
          this.stopAnimation()
        }
      },
      
      animationProgress(newVal) {
        const totalPoints = this.routeInfo.path.length
        this.currentWaypointIndex = Math.floor((newVal / 100) * (totalPoints - 1))
        this.completedDistance = (newVal / 100) * this.routeInfo.totalDistance
      }
    },
    methods: {
      // 对话框打开时的回调
      onDialogOpen() {
        this.$nextTick(() => {
          this.updateAnimationSize()
          // 设置观察器监听尺寸变化
          if (this.$refs.routeMap) {
            this.resizeObserver = new ResizeObserver(() => {
              this.updateAnimationSize()
            })
            this.resizeObserver.observe(this.$refs.routeMap)
          }
        })
      },
      showOnMap() {
    this.$emit('show-on-map', this.routeInfo)
  },
      
      // 切换全屏
      toggleFullscreen() {
        this.isFullscreen = !this.isFullscreen
        this.$nextTick(() => {
          this.updateAnimationSize()
        })
      },
      
      // 更新动画区域尺寸
      updateAnimationSize() {
        const animationPanel = this.$el.querySelector('.route-animation-panel')
        if (animationPanel) {
          const rect = animationPanel.getBoundingClientRect()
          this.animationWidth = Math.max(800, rect.width - 60)
          this.animationHeight = Math.max(500, rect.height - 160)
        }
      },
      
      getWaypointName(index) {
        if (index === 0) return '起点'
        if (index === this.routeInfo.path.length - 1) return '终点'
        return `航点 ${index}`
      },
      
      getPointRadius(index) {
        return index === 0 || index === this.scaledPath.length - 1 ? 8 : 6
      },
      
      getPointColor(index) {
        if (index === 0) return '#52c41a'
        if (index === this.scaledPath.length - 1) return '#fa8c16'
        return '#1890ff'
      },
      
      getPointStrokeColor(index) {
        if (index === 0) return '#389e0d'
        if (index === this.scaledPath.length - 1) return '#d46b08'
        return '#096dd9'
      },
      
      getLabelStyle(index) {
        if (!this.scaledPath[index]) return {}
        return {
          left: this.scaledPath[index].x + 'px',
          top: this.scaledPath[index].y - 35 + 'px'
        }
      },
      
      getCurrentAltitude() {
        if (this.currentWaypointIndex < this.routeInfo.path.length) {
          const point = this.routeInfo.path[this.currentWaypointIndex]
          return point.altitude || 100
        }
        return 100
      },
      
      initAnimation() {
        this.animationProgress = 0
        this.currentWaypointIndex = 0
        this.completedDistance = 0
        this.isAnimating = false
      },
      
      toggleAnimation() {
        if (this.isAnimating) {
          this.stopAnimation()
        } else {
          this.startAnimation()
        }
      },
      
      startAnimation() {
        if (this.animationProgress >= 100) {
          this.animationProgress = 0
        }
        
        this.isAnimating = true
        this.animationInterval = setInterval(() => {
          this.animationProgress += 0.8 // 加快动画速度
          if (this.animationProgress >= 100) {
            this.animationProgress = 100
            this.stopAnimation()
          }
        }, this.animationSpeed)
      },
      
      stopAnimation() {
        this.isAnimating = false
        if (this.animationInterval) {
          clearInterval(this.animationInterval)
          this.animationInterval = null
        }
      },
      
      resetAnimation() {
        this.stopAnimation()
        this.initAnimation()
      },
      
      onProgressChange(value) {
        this.animationProgress = value
      },
      
      calculateSegmentDistance(index) {
        if (index === 0) return 0
        
        const point1 = this.routeInfo.path[index - 1]
        const point2 = this.routeInfo.path[index]
        
        return this.calculateDistance(point1.lng, point1.lat, point2.lng, point2.lat)
      },
      
      calculateDistance(lng1, lat1, lng2, lat2) {
        const earthRadius = 6371000
        const dLat = (lat2 - lat1) * Math.PI / 180
        const dLng = (lng2 - lng1) * Math.PI / 180
        
        const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                 Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                 Math.sin(dLng/2) * Math.sin(dLng/2)
        
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
        return earthRadius * c
      },
      
      useRouteForMission() {
        this.$emit('use-route', this.routeInfo)
        this.handleClose()
      },
      
      exportRoute() {
        const routeData = {
          ...this.routeInfo,
          startLocation: this.routeData.startLocation,
          endLocation: this.routeData.endLocation,
          timestamp: new Date().toISOString()
        }
        
        const dataStr = JSON.stringify(routeData, null, 2)
        const dataBlob = new Blob([dataStr], { type: 'application/json' })
        
        const link = document.createElement('a')
        link.href = URL.createObjectURL(dataBlob)
        link.download = `route_${this.routeData.startLocation}_to_${this.routeData.endLocation}.json`
        link.click()
        
        this.$message.success('路径数据导出成功')
      },
      
      handleClose() {
        this.stopAnimation()
        if (this.resizeObserver) {
          this.resizeObserver.disconnect()
        }
        this.dialogVisible = false
        this.$emit('close')
      }
    },
    
    mounted() {
      console.log('路径规划对话框已挂载')
    },
    
    beforeDestroy() {
      this.stopAnimation()
      if (this.resizeObserver) {
        this.resizeObserver.disconnect()
      }
    }
  }
  </script>
  
  <style scoped>
  .route-planning-dialog {
    font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
  }
  
  .route-planning-container {
    display: flex;
    height: 80vh;
    gap: 20px;
    overflow: hidden;
  }
  
  .route-info-panel {
    width: 350px;
    display: flex;
    flex-direction: column;
    gap: 20px;
    min-width: 350px;
    flex-shrink: 0;
  }
  
  .route-animation-panel {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
  }
  
  .info-card, .waypoints-card, .animation-card {
    height: auto;
    display: flex;
    flex-direction: column;
  }
  
  .animation-card {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  
  .animation-card >>> .el-card__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 0;
  }
  
  .animation-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-height: 0;
  }
  
  .route-map {
    flex: 1;
    position: relative;
    min-height: 0;
    background: #f8f9fa;
    border-radius: 8px;
    overflow: hidden;
  }
  
  .dialog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }
  
  .header-actions {
    display: flex;
    gap: 10px;
  }
  
  .route-summary {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .summary-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .summary-item .label {
    color: #666;
    font-size: 14px;
    font-weight: 500;
  }
  
  .summary-item .value {
    color: #333;
    font-weight: 600;
    font-size: 14px;
  }
  
  .action-buttons {
    margin-top: 20px;
  }
  
  .waypoints-list {
    max-height: 400px;
    overflow-y: auto;
    flex: 1;
  }
  
  .waypoint-item {
    padding: 14px 0;
    border-bottom: 1px solid #f5f5f5;
    transition: all 0.3s ease;
    cursor: pointer;
  }
  
  .waypoint-item:hover {
    background: #fafafa;
  }
  
  .waypoint-item.active {
    background: #f0f7ff;
    border-left: 4px solid #1890ff;
    padding-left: 16px;
  }
  
  .waypoint-item.start {
    border-left-color: #52c41a;
  }
  
  .waypoint-item.end {
    border-left-color: #fa8c16;
  }
  
  .waypoint-marker {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .marker-icon {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  
  .start-icon {
    color: #52c41a;
    font-size: 20px;
  }
  
  .end-icon {
    color: #fa8c16;
    font-size: 20px;
  }
  
  .waypoint-dot {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: #1890ff;
    border: 3px solid #096dd9;
  }
  
  .waypoint-info {
    flex: 1;
    min-width: 0;
  }
  
  .waypoint-name {
    font-weight: 600;
    color: #333;
    margin-bottom: 4px;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .waypoint-coords {
    font-size: 12px;
    color: #666;
    margin-bottom: 2px;
  }
  
  .waypoint-altitude {
    font-size: 12px;
    color: #999;
  }
  
  .waypoint-distance {
    font-size: 12px;
    color: #1890ff;
    font-weight: 600;
    flex-shrink: 0;
    background: #f0f7ff;
    padding: 2px 6px;
    border-radius: 10px;
  }
  
  .animation-controls {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: nowrap;
  }
  
  .progress-text {
    font-size: 14px;
    color: #666;
    min-width: 45px;
    font-weight: 600;
  }
  
  .map-background {
    position: relative;
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #f5f7fa 0%, #e3e8f0 100%);
    min-height: 500px;
  }
  
  .grid-lines {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
  
  .grid-line {
    position: absolute;
    background: rgba(255, 255, 255, 0.4);
  }
  
  .grid-line.horizontal {
    width: 100%;
    height: 1px;
  }
  
  .grid-line.vertical {
    width: 1px;
    height: 100%;
  }
  
  .route-svg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
  
  .completed-path {
    filter: drop-shadow(0 0 3px rgba(24, 144, 255, 0.6));
    animation: pathGlow 2s ease-in-out infinite alternate;
  }
  
  @keyframes pathGlow {
    from {
      filter: drop-shadow(0 0 3px rgba(24, 144, 255, 0.6));
    }
    to {
      filter: drop-shadow(0 0 6px rgba(24, 144, 255, 0.8));
    }
  }
  
  .waypoint-marker {
    transition: all 0.3s ease;
  }
  
  .drone-marker {
    filter: drop-shadow(0 0 6px rgba(255, 77, 79, 0.8));
    animation: dronePulse 1.5s ease-in-out infinite;
  }
  
  @keyframes dronePulse {
    0%, 100% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.1);
    }
  }
  
  .drone-label {
    animation: float 2s ease-in-out infinite;
    filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  }
  
  @keyframes float {
    0%, 100% { transform: translateY(0px); }
    50% { transform: translateY(-8px); }
  }
  
  .map-label {
    position: absolute;
    background: rgba(255, 255, 255, 0.98);
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    border: 2px solid;
    transform: translateX(-50%);
    white-space: nowrap;
    max-width: 220px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: flex;
    align-items: center;
    gap: 6px;
    z-index: 10;
  }
  
  .label-text {
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .start-label {
    color: #52c41a;
    border-color: #b7eb8f;
    background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
  }
  
  .end-label {
    color: #fa8c16;
    border-color: #ffd591;
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
  }
  
  .real-time-info {
    position: absolute;
    bottom: 25px;
    left: 25px;
    background: rgba(255, 255, 255, 0.95);
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
    border: 1px solid #e8e8e8;
    z-index: 10;
    min-width: 240px;
    backdrop-filter: blur(10px);
  }
  
  .info-header {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 2px solid #1890ff;
  }
  
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-size: 14px;
    min-width: 200px;
  }
  
  .info-item:last-child {
    margin-bottom: 0;
  }
  
  .info-item .label {
    color: #666;
    flex-shrink: 0;
    font-weight: 500;
  }
  
  .info-item .value {
    color: #333;
    font-weight: 600;
    flex-shrink: 0;
    margin-left: 10px;
  }
  
  /* 滚动条样式 */
  .waypoints-list::-webkit-scrollbar {
    width: 8px;
  }
  
  .waypoints-list::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }
  
  .waypoints-list::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
  }
  
  .waypoints-list::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
  
  /* 全屏样式 */
  :deep(.route-dialog-custom.is-fullscreen) {
    width: 100% !important;
    margin-top: 0 !important;
    height: 100vh;
  }
  
  :deep(.route-dialog-custom.is-fullscreen .el-dialog__body) {
    flex: 1;
    padding: 20px;
  }
  </style>
  
  <style>
  .route-dialog-custom {
    z-index: 2001 !important;
    display: flex;
    flex-direction: column;
    max-height: 96vh;
  }
  
  .route-dialog-custom .el-dialog {
    display: flex;
    flex-direction: column;
    margin-top: 1vh !important;
    margin-bottom: 1vh !important;
    height: auto;
    max-height: 98vh;
  }
  
  .route-dialog-custom .el-dialog__body {
    padding: 20px;
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
  }
  
  .route-dialog-custom .el-dialog__header {
    padding: 20px 20px 15px;
    border-bottom: 1px solid #e8e8e8;
    flex-shrink: 0;
    background: #fafafa;
  }
  
  .route-dialog-custom .el-dialog__headerbtn {
    top: 20px;
    right: 20px;
    font-size: 18px;
  }
  
  /* 确保对话框可以拖动 */
  .route-dialog-custom .el-dialog {
    position: absolute;
  }
  
  .route-dialog-custom .el-dialog__header {
    cursor: move;
  }
  
  /* 全屏模式 */
  .route-dialog-custom.is-fullscreen {
    width: 100% !important;
    height: 100vh !important;
    margin: 0 !important;
    top: 0 !important;
  }
  
  .route-dialog-custom.is-fullscreen .el-dialog {
    width: 100% !important;
    height: 100vh !important;
    max-height: 100vh !important;
    margin: 0 !important;
  }
  
  .route-dialog-custom.is-fullscreen .route-planning-container {
    height: calc(100vh - 120px) !important;
  }
  </style>
