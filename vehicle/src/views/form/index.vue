<template>
    <div class="report-stats-page">
      <div class="header">
        <div class="title-section">
          <div class="title">巡查上报数据统计</div>
          <div class="update-time">上次刷新：{{ lastUpdateTime }}</div>
          <div class="loading-indicator" v-if="loading">
            <i class="el-icon-loading"></i> 数据加载中...
          </div>
        </div>
        <div class="action-buttons">
          <el-button 
            type="primary" 
            size="mini" 
            class="refresh-btn" 
            @click="handleRefresh"
            :loading="loading"
            :disabled="loading">
            <i class="el-icon-refresh"></i>
            {{ loading ? '刷新中...' : '手动刷新' }}
          </el-button>
          <el-button @click="$router.push('/reports/data-manager')" size="mini">
            <i class="el-icon-s-management"></i>
            巡查数据管理
          </el-button>
  
          <el-button 
            @click="openTrendDialog" 
            size="mini" 
            type="info"
            class="trend-dialog-btn"
          >
            <i class="el-icon-data-analysis"></i>
            趋势分析
          </el-button>
  
          <el-button @click="handleExport" size="mini" type="success" :loading="exporting">
            <i class="el-icon-download"></i>
            {{ exporting ? '导出中...' : '导出数据' }}
          </el-button>
        </div>
      </div>
  
      <!-- 统计卡片区域 -->
      <div class="stats-cards">
        <div class="stat-card glow" v-for="card in statCards" :key="card.title">
          <div class="card-icon" :style="{ color: card.color }">
            <i :class="card.icon"></i>
          </div>
          <div class="card-content">
            <div class="card-value">{{ card.value }}</div>
            <div class="card-title">{{ card.title }}</div>
          </div>
          <div class="card-trend" :class="card.trend">
            <i :class="card.trendIcon"></i>
            {{ card.trendValue }}
          </div>
        </div>
      </div>
  
      <div class="content">
        <!-- 左侧：滚动排名 -->
        <div class="panel glow">
          <div class="panel-header">
            <div class="panel-title">巡查上报记录数量排名</div>
            <div class="panel-subtitle">按类别统计上报数量</div>
          </div>
          <div v-if="rankingData.length && !loading" class="ranking-container">
            <div 
              v-for="(item, index) in rankingData" 
              :key="'ranking-' + index + '-' + item.name"
              class="ranking-item"
              :class="{ 'top-three': index < 3 }"
            >
              <div class="rank-index">
                <span class="rank-number">{{ index + 1 }}</span>
                <div class="rank-medal" v-if="index < 3">
                  <i :class="medalIcons[index]"></i>
                </div>
              </div>
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-value">{{ item.value }}次</div>
              <div class="rank-bar">
                <div 
                  class="bar-fill" 
                  :style="{ width: calculateBarWidth(item.value) }"
                ></div>
              </div>
            </div>
          </div>
          <div v-else-if="loading" class="loading-container">
            <i class="el-icon-loading"></i>
            <div>数据加载中...</div>
          </div>
          <div v-else class="no-data">
            <i class="el-icon-warning-outline"></i>
            <div>暂无数据</div>
          </div>
        </div>
  
        <!-- 右侧：饼图展示 -->
        <div class="panel glow">
          <div class="panel-header">
            <div class="panel-title">上报类别比例分布</div>
            <div class="panel-subtitle">各类别占比情况</div>
          </div>
          <div v-if="pieData.length && !loading" class="chart-container">
            <div class="pie-chart">
              <div 
                v-for="(item, index) in pieData" 
                :key="'pie-' + index + '-' + item.name"
                class="pie-item"
              >
                <div class="pie-color" :style="{ backgroundColor: pieColors[index % pieColors.length] }"></div>
                <div class="pie-info">
                  <div class="pie-name">{{ item.name }}</div>
                  <div class="pie-value">{{ item.value }}次 ({{ calculatePercentage(item.value) }}%)</div>
                </div>
              </div>
            </div>
            <div class="pie-visual">
              <div class="pie-chart-svg">
                <svg viewBox="0 0 200 200">
                  <path 
                    v-for="(slice, index) in pieSlices" 
                    :key="index"
                    :d="slice.d"
                    :fill="pieColors[index % pieColors.length]"
                    class="pie-slice"
                    @mouseenter="highlightSlice(index)"
                    @mouseleave="resetHighlight"
                  />
                </svg>
              </div>
              <div class="pie-total">
                <div class="total-label">总计</div>
                <div class="total-value">{{ totalCount }}</div>
              </div>
            </div>
          </div>
          <div v-else-if="loading" class="loading-container">
            <i class="el-icon-loading"></i>
            <div>数据加载中...</div>
          </div>
          <div v-else class="no-data">
            <i class="el-icon-warning-outline"></i>
            <div>暂无数据</div>
          </div>
        </div>
      </div>
  
      <!-- 趋势图弹窗 -->
      <el-dialog
        title="上报趋势分析"
        :visible.sync="trendDialogVisible"
        width="90%"
        top="5vh"
        custom-class="trend-dialog"
        :close-on-click-modal="false"
        @closed="handleDialogClosed"
      >
        <div class="trend-dialog-content">
          <!-- 趋势图控制 -->
          <div class="trend-dialog-controls">
            <el-radio-group v-model="timeRange" size="mini" @change="changeTimeRange">
              <el-radio-button label="week">近一周</el-radio-button>
              <el-radio-button label="month">近一月</el-radio-button>
              <el-radio-button label="quarter">近三月</el-radio-button>
            </el-radio-group>
            <el-button 
              size="mini" 
              @click="toggleChartType"
              class="chart-type-btn"
            >
              <i :class="chartTypeIcon"></i>
              {{ chartType === 'line' ? '切换柱状图' : '切换折线图' }}
            </el-button>
          </div>
          
          <div class="trend-dialog-chart">
            <div v-if="trendData.length && !loading" class="trend-chart-container">
              <div class="trend-y-axis">
                <div 
                  v-for="tick in yAxisTicks" 
                  :key="tick"
                  class="y-tick"
                >
                  {{ tick }}
                </div>
              </div>
              
              <div class="trend-main-content">
                <div class="trend-grid">
                  <div 
                    v-for="tick in yAxisTicks" 
                    :key="tick"
                    class="grid-line"
                  ></div>
                </div>
                
                <div class="trend-chart" :class="chartType">
                  <!-- 折线图 -->
                  <div v-if="chartType === 'line'" class="line-chart-wrapper">
                    <svg class="line-chart-svg" :viewBox="`0 0 ${svgWidth} ${svgHeight}`" preserveAspectRatio="none">
                      <!-- 渐变背景 -->
                      <defs>
                        <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                          <stop offset="0%" stop-color="rgba(0, 240, 255, 0.3)" />
                          <stop offset="100%" stop-color="rgba(0, 240, 255, 0.05)" />
                        </linearGradient>
                      </defs>
                      
                      <!-- 区域填充 -->
                      <path 
                        :d="areaPath" 
                        fill="url(#areaGradient)" 
                        class="line-area"
                      />
                      
                      <!-- 折线 -->
                      <path 
                        :d="linePath" 
                        fill="none" 
                        stroke="#00f0ff" 
                        stroke-width="2" 
                        class="line-path"
                      />
                      
                      <!-- 数据点 -->
                      <circle 
                        v-for="(point, index) in svgPoints" 
                        :key="'point-' + index"
                        :cx="point.x" 
                        :cy="point.y" 
                        r="4" 
                        fill="#00f0ff"
                        stroke="#fff"
                        stroke-width="1.5"
                        class="data-point"
                        @mouseenter="showSvgTooltip(point, index)"
                        @mouseleave="hideTooltip"
                      />
                    </svg>
                  </div>
                  
                  <!-- 柱状图 -->
                  <div v-else class="bar-chart-wrapper">
                    <div class="bar-chart">
                      <div 
                        v-for="(item, index) in trendData" 
                        :key="'trend-' + index + '-' + item.date"
                        class="trend-bar-container"
                      >
                        <div 
                          class="trend-bar"
                          :style="{ height: calculateBarHeight(item.value) }"
                          @mouseenter="showBarTooltip(item, index)"
                          @mouseleave="hideTooltip"
                        >
                          <div class="bar-value">{{ item.value }}</div>
                        </div>
                        <div class="bar-date">{{ item.date }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 图例和统计 -->
            <div v-if="trendData.length && !loading" class="trend-legend">
              <div class="legend-item">
                <div class="legend-color" :style="{ backgroundColor: '#00f0ff' }"></div>
                <span>上报数量趋势</span>
              </div>
              <div class="trend-stats">
                <div class="stat-item">
                  <span class="stat-label">平均值:</span>
                  <span class="stat-value">{{ averageValue }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">最高值:</span>
                  <span class="stat-value">{{ maxValue }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">增长率:</span>
                  <span class="stat-value" :class="growthRateClass">{{ growthRate }}%</span>
                </div>
              </div>
            </div>
            
            <div v-else-if="loading" class="loading-container">
              <i class="el-icon-loading"></i>
              <div>趋势数据加载中...</div>
            </div>
            <div v-else class="no-data">
              <i class="el-icon-warning-outline"></i>
              <div>暂无趋势数据</div>
            </div>
          </div>
          
          <!-- 趋势图提示框 -->
          <div 
            v-if="tooltip.visible" 
            class="trend-tooltip"
            :style="{ left: tooltip.x + 'px', top: tooltip.y + 'px' }"
          >
            <div class="tooltip-date">{{ tooltip.date }}</div>
            <div class="tooltip-value">上报数量: {{ tooltip.value }}次</div>
          </div>
        </div>
        
        <span slot="footer" class="dialog-footer">
          <el-button @click="trendDialogVisible = false" size="mini">关闭</el-button>
          <el-button type="primary" @click="exportTrendData" size="mini" :loading="exporting">
            <i class="el-icon-download"></i>
            导出趋势数据
          </el-button>
        </span>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    name: "FormReportStats",
    data() {
      return {
        loading: false,
        exporting: false,
        timeRange: 'month',
        chartType: 'line', // 'line' 或 'bar'
        lastUpdateTime: "暂无数据",
        rankingData: [],
        pieData: [],
        trendData: [],
        totalCount: 0,
        trendDialogVisible: false,
        tooltip: {
          visible: false,
          x: 0,
          y: 0,
          date: '',
          value: 0
        },
        pieColors: [
          "#00f0ff",
          "#03d3ec",
          "#26fcd8",
          "#29b7ff",
          "#1476ff",
          "#1b4fff",
          "#ffb830",
          "#ff6b6b",
          "#a29bfe",
          "#fd79a8"
        ],
        medalIcons: [
          "el-icon-medal-1",
          "el-icon-medal",
          "el-icon-trophy"
        ],
        statCards: [
          {
            title: "总上报数量",
            value: "0",
            icon: "el-icon-document",
            color: "#00f0ff",
            trend: "up",
            trendIcon: "el-icon-top",
            trendValue: "+0%"
          },
          {
            title: "活跃类别",
            value: "0",
            icon: "el-icon-star-on",
            color: "#26fcd8",
            trend: "up",
            trendIcon: "el-icon-top",
            trendValue: "+0"
          },
          {
            title: "处理中",
            value: "0",
            icon: "el-icon-loading",
            color: "#ffb830",
            trend: "stable",
            trendIcon: "el-icon-minus",
            trendValue: "0"
          },
          {
            title: "已完成",
            value: "0",
            icon: "el-icon-success",
            color: "#26fcd8",
            trend: "up",
            trendIcon: "el-icon-top",
            trendValue: "+0%"
          }
        ],
        svgWidth: 600,
        svgHeight: 200
      };
    },
    computed: {
      pieSlices() {
        if (!this.pieData.length) return [];
        
        const total = this.pieData.reduce((sum, item) => sum + item.value, 0);
        let currentAngle = 0;
        
        return this.pieData.map(item => {
          const percentage = item.value / total;
          const angle = percentage * 360;
          
          const startAngle = currentAngle;
          const endAngle = currentAngle + angle;
          currentAngle = endAngle;
          
          // 计算饼图切片路径
          const start = this.polarToCartesian(100, 100, 80, startAngle);
          const end = this.polarToCartesian(100, 100, 80, endAngle);
          
          const largeArcFlag = angle > 180 ? 1 : 0;
          
          const d = [
            `M 100 100`,
            `L ${start.x} ${start.y}`,
            `A 80 80 0 ${largeArcFlag} 1 ${end.x} ${end.y}`,
            'Z'
          ].join(' ');
          
          return { d };
        });
      },
      
      yAxisTicks() {
        if (!this.trendData.length) return [0, 25, 50, 75, 100];
        
        const maxValue = Math.max(...this.trendData.map(item => item.value));
        const step = Math.ceil(maxValue / 4);
        const ticks = [0];
        
        for (let i = 1; i <= 4; i++) {
          ticks.push(step * i);
        }
        
        return ticks;
      },
      
      svgPoints() {
        if (!this.trendData.length) return [];
        
        const maxValue = Math.max(...this.trendData.map(item => item.value));
        const padding = 20;
        const chartHeight = this.svgHeight - padding * 2;
        const chartWidth = this.svgWidth - padding * 2;
        const pointSpacing = chartWidth / (this.trendData.length - 1);
        
        return this.trendData.map((item, index) => ({
          x: padding + index * pointSpacing,
          y: padding + chartHeight - (item.value / maxValue) * chartHeight,
          value: item.value,
          date: item.date
        }));
      },
      
      linePath() {
        if (this.svgPoints.length < 2) return '';
        
        let path = `M ${this.svgPoints[0].x} ${this.svgPoints[0].y}`;
        
        for (let i = 1; i < this.svgPoints.length; i++) {
          path += ` L ${this.svgPoints[i].x} ${this.svgPoints[i].y}`;
        }
        
        return path;
      },
      
      areaPath() {
        if (this.svgPoints.length < 2) return '';
        
        let path = `M ${this.svgPoints[0].x} ${this.svgHeight}`;
        path += ` L ${this.svgPoints[0].x} ${this.svgPoints[0].y}`;
        
        for (let i = 1; i < this.svgPoints.length; i++) {
          path += ` L ${this.svgPoints[i].x} ${this.svgPoints[i].y}`;
        }
        
        path += ` L ${this.svgPoints[this.svgPoints.length - 1].x} ${this.svgHeight}`;
        path += ' Z';
        
        return path;
      },
      
      averageValue() {
        if (!this.trendData.length) return 0;
        const sum = this.trendData.reduce((acc, item) => acc + item.value, 0);
        return Math.round(sum / this.trendData.length);
      },
      
      maxValue() {
        if (!this.trendData.length) return 0;
        return Math.max(...this.trendData.map(item => item.value));
      },
      
      growthRate() {
        if (this.trendData.length < 2) return 0;
        const first = this.trendData[0].value;
        const last = this.trendData[this.trendData.length - 1].value;
        if (first === 0) return last > 0 ? 100 : 0;
        return ((last - first) / first * 100).toFixed(1);
      },
      
      growthRateClass() {
        return parseFloat(this.growthRate) >= 0 ? 'positive' : 'negative';
      },
      
      chartTypeIcon() {
        return this.chartType === 'line' ? 'el-icon-data-line' : 'el-icon-data-analysis';
      }
    },
    mounted() {
      this.fetchReportStats();
      // 每5分钟刷新一次
      this.intervalId = setInterval(() => this.fetchReportStats(), 300000);
    },
    beforeDestroy() {
      if (this.intervalId) {
        clearInterval(this.intervalId);
      }
    },
    methods: {
      // 打开趋势图弹窗
      openTrendDialog() {
        this.trendDialogVisible = true;
        // 确保有趋势数据
        if (this.trendData.length === 0) {
          this.generateTrendData();
        }
      },
      
      // 处理弹窗关闭
      handleDialogClosed() {
        this.hideTooltip();
      },
      
      // 导出趋势数据
      exportTrendData() {
        this.exporting = true;
        try {
          // 模拟导出过程
          setTimeout(() => {
            // 创建导出数据
            const exportData = {
              title: '巡查上报趋势数据',
              exportTime: new Date().toLocaleString(),
              timeRange: this.timeRange,
              chartType: this.chartType,
              trend: this.trendData,
              stats: {
                average: this.averageValue,
                max: this.maxValue,
                growthRate: this.growthRate
              }
            };
            
            // 创建下载链接
            const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(exportData, null, 2));
            const downloadAnchorNode = document.createElement('a');
            downloadAnchorNode.setAttribute("href", dataStr);
            downloadAnchorNode.setAttribute("download", `巡查趋势数据_${new Date().toISOString().split('T')[0]}.json`);
            document.body.appendChild(downloadAnchorNode);
            downloadAnchorNode.click();
            downloadAnchorNode.remove();
            
            this.$message.success('趋势数据导出成功');
            this.exporting = false;
          }, 1000);
        } catch (error) {
          console.error("导出失败:", error);
          this.$message.error('趋势数据导出失败');
          this.exporting = false;
        }
      },
  
      async handleRefresh() {
        await this.fetchReportStats();
        this.$message.success('数据刷新成功');
      },
      
      async handleExport() {
        this.exporting = true;
        try {
          // 模拟导出过程
          await new Promise(resolve => setTimeout(resolve, 1500));
          
          // 创建导出数据
          const exportData = {
            title: '巡查上报数据统计',
            exportTime: new Date().toLocaleString(),
            summary: this.statCards,
            ranking: this.rankingData,
            distribution: this.pieData,
            trend: this.trendData
          };
          
          // 创建下载链接
          const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(exportData, null, 2));
          const downloadAnchorNode = document.createElement('a');
          downloadAnchorNode.setAttribute("href", dataStr);
          downloadAnchorNode.setAttribute("download", `巡查数据统计_${new Date().toISOString().split('T')[0]}.json`);
          document.body.appendChild(downloadAnchorNode);
          downloadAnchorNode.click();
          downloadAnchorNode.remove();
          
          this.$message.success('数据导出成功');
        } catch (error) {
          console.error("导出失败:", error);
          this.$message.error('数据导出失败');
        } finally {
          this.exporting = false;
        }
      },
      
      async fetchReportStats() {
        this.loading = true;
        try {
          const res = await axios.get("http://localhost:8080/api/reports/stats");
          
          if (res.data && res.data.reportStats) {
            this.processReportData(res.data.reportStats);
            this.updateStatCards(res.data);
            this.lastUpdateTime = new Date().toLocaleString();
            
            // 生成趋势数据
            this.generateTrendData();
          } else {
            console.warn("返回数据格式不正确:", res.data);
            // 使用模拟数据作为备选
            this.useMockData();
          }
        } catch (error) {
          console.error("获取数据失败:", error);
          this.$message.error('数据加载失败，请检查网络连接');
          // 使用模拟数据作为备选
          this.useMockData();
        } finally {
          this.loading = false;
        }
      },
      
      useMockData() {
        // 模拟数据作为备选
        const mockData = {
          日常养护: 45,
          路面问题: 32,
          路基问题: 28,
          农林病害: 15,
          交通事故: 12,
          除雪作业: 8,
          桥通问题: 18,
          交安设施: 22,
          绿化养护: 25
        };
        this.processReportData(mockData);
        this.generateTrendData();
        this.lastUpdateTime = new Date().toLocaleString();
      },
      
      processReportData(reportData) {
        if (!reportData || typeof reportData !== 'object') {
          console.warn('reportData 格式不正确:', reportData);
          reportData = {};
        }
        
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
  
        const data = Object.entries(reportData)
          .filter(([key, val]) => val > 0) // 只保留有数据的项目
          .map(([key, val]) => ({
            name: categoryMap[key] || key,
            value: val,
          }));
  
        // 排序
        data.sort((a, b) => b.value - a.value);
        
        this.rankingData = data;
        this.pieData = data;
        this.totalCount = data.reduce((sum, item) => sum + item.value, 0);
      },
      
      updateStatCards(data) {
        const reportStats = data.reportStats || {};
        
        // 计算总数
        const total = Object.values(reportStats).reduce((sum, val) => sum + val, 0);
        
        // 计算活跃类别数量（值大于0的类别）
        const activeCategories = Object.values(reportStats).filter(val => val > 0).length;
        
        this.statCards[0].value = total.toLocaleString();
        this.statCards[1].value = activeCategories.toString();
        this.statCards[2].value = Math.floor(total * 0.3).toLocaleString();
        this.statCards[3].value = Math.floor(total * 0.6).toLocaleString();
        
        // 模拟趋势变化
        this.statCards[0].trendValue = `+${Math.floor(Math.random() * 20) + 5}%`;
        this.statCards[1].trendValue = `+${Math.floor(Math.random() * 5) + 1}`;
      },
      
      generateTrendData() {
        // 根据时间范围生成不同数量的数据点
        let dataPoints = 7;
        if (this.timeRange === 'month') dataPoints = 12;
        if (this.timeRange === 'quarter') dataPoints = 15;
        
        const dates = [];
        const baseValue = this.totalCount > 0 ? Math.max(this.totalCount / 10, 5) : 10;
        
        // 生成日期和值
        for (let i = 0; i < dataPoints; i++) {
          const date = new Date();
          
          if (this.timeRange === 'week') {
            date.setDate(date.getDate() - (dataPoints - 1 - i));
          } else if (this.timeRange === 'month') {
            date.setDate(date.getDate() - (dataPoints - 1 - i) * 2.5);
          } else {
            date.setDate(date.getDate() - (dataPoints - 1 - i) * 6);
          }
          
          // 生成更合理的随机值，避免过大波动
          const randomFactor = 0.7 + Math.random() * 0.6;
          const value = Math.max(1, Math.floor(baseValue * randomFactor));
          
          dates.push({
            date: `${date.getMonth() + 1}/${date.getDate()}`,
            value: value
          });
        }
        
        this.trendData = dates;
      },
      
      changeTimeRange() {
        this.generateTrendData();
      },
      
      toggleChartType() {
        this.chartType = this.chartType === 'line' ? 'bar' : 'line';
      },
      
      calculateBarWidth(value) {
        if (!this.rankingData.length) return '0%';
        const maxValue = Math.max(...this.rankingData.map(item => item.value));
        return `${(value / maxValue) * 100}%`;
      },
      
      calculatePercentage(value) {
        if (this.totalCount === 0) return 0;
        return ((value / this.totalCount) * 100).toFixed(1);
      },
      
      calculateBarHeight(value) {
        if (!this.trendData.length) return '0%';
        const maxValue = Math.max(...this.trendData.map(item => item.value));
        return `${(value / maxValue) * 100}%`;
      },
      
      polarToCartesian(centerX, centerY, radius, angleInDegrees) {
        const angleInRadians = (angleInDegrees - 90) * Math.PI / 180.0;
        return {
          x: centerX + (radius * Math.cos(angleInRadians)),
          y: centerY + (radius * Math.sin(angleInRadians))
        };
      },
      
      highlightSlice(index) {
        // 高亮饼图切片
        const slices = document.querySelectorAll('.pie-slice');
        if (slices[index]) {
          slices[index].style.transform = 'scale(1.05)';
          slices[index].style.opacity = '0.9';
        }
      },
      
      resetHighlight() {
        // 重置饼图切片
        const slices = document.querySelectorAll('.pie-slice');
        slices.forEach(slice => {
          slice.style.transform = 'scale(1)';
          slice.style.opacity = '1';
        });
      },
      
      showSvgTooltip(point, index) {
        const svgElement = document.querySelector('.line-chart-svg');
        const svgRect = svgElement.getBoundingClientRect();
        
        this.tooltip = {
          visible: true,
          x: svgRect.left + (point.x / this.svgWidth) * svgRect.width,
          y: svgRect.top + (point.y / this.svgHeight) * svgRect.height - 50,
          date: point.date,
          value: point.value
        };
      },
      
      showBarTooltip(item, index) {
        const barElements = document.querySelectorAll('.trend-bar');
        if (barElements[index]) {
          const rect = barElements[index].getBoundingClientRect();
          const container = document.querySelector('.trend-main-content');
          const containerRect = container.getBoundingClientRect();
          
          this.tooltip = {
            visible: true,
            x: rect.left + rect.width / 2 - containerRect.left,
            y: rect.top - containerRect.top - 40,
            date: item.date,
            value: item.value
          };
        }
      },
      
      hideTooltip() {
        this.tooltip.visible = false;
      }
    },
  };
  </script>
  
  <style scoped>
  /* 基础页面样式 */
  .report-stats-page {
    padding: 20px;
    background: radial-gradient(ellipse at center, #081529 0%, #030d1f 100%);
    min-height: 100vh;
    color: #fff;
    font-family: "Orbitron", "Microsoft YaHei", sans-serif;
    overflow-x: hidden;
  }
  
  /* 头部样式 */
  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 30px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(0, 240, 255, 0.2);
    position: relative;
  }
  
  .title-section {
    display: flex;
    flex-direction: column;
    flex: 1;
  }
  
  .title {
    font-size: 26px;
    font-weight: bold;
    color: #00f0ff;
    text-shadow: 0 0 10px #00eaff;
    margin-bottom: 5px;
    letter-spacing: 1px;
  }
  
  .update-time {
    font-size: 13px;
    color: #a0d2ff;
    margin-bottom: 5px;
    font-weight: 500;
  }
  
  .loading-indicator {
    font-size: 12px;
    color: #00f0ff;
    display: flex;
    align-items: center;
    gap: 5px;
    font-weight: 500;
  }
  
  .action-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
  
  /* 按钮样式 */
  .refresh-btn {
    background: linear-gradient(90deg, #009dff, #00f0ff);
    border: none;
    box-shadow: 0 0 12px #00eaff;
    color: #fff;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
  
  .trend-dialog-btn {
    background: linear-gradient(90deg, #7e57c2, #b39ddb);
    border: none;
    box-shadow: 0 0 12px rgba(126, 87, 194, 0.6);
    color: #fff;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
  
  .trend-dialog-btn:hover {
    background: linear-gradient(90deg, #6a48a8, #9e89c7);
    box-shadow: 0 0 15px rgba(126, 87, 194, 0.8);
  }
  
  /* 统计卡片样式 */
  .stats-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 22px;
    margin-bottom: 35px;
  }
  
  .stat-card {
    background: rgba(6, 30, 93, 0.6);
    border: 1px solid rgba(0, 255, 255, 0.4);
    border-radius: 12px;
    padding: 22px;
    display: flex;
    align-items: center;
    transition: all 0.3s ease;
    backdrop-filter: blur(5px);
  }
  
  .stat-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 0 25px rgba(0, 255, 255, 0.4);
    border-color: rgba(0, 255, 255, 0.7);
  }
  
  .card-icon {
    font-size: 36px;
    margin-right: 18px;
    opacity: 0.9;
    filter: drop-shadow(0 0 5px currentColor);
  }
  
  .card-content {
    flex: 1;
  }
  
  .card-value {
    font-size: 28px;
    font-weight: bold;
    color: #00f0ff;
    margin-bottom: 6px;
    text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
    letter-spacing: 0.5px;
  }
  
  .card-title {
    font-size: 14px;
    color: #d1e8ff;
    font-weight: 500;
    letter-spacing: 0.3px;
  }
  
  .card-trend {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    font-weight: 600;
  }
  
  .card-trend.up {
    color: #26fcd8;
    text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
  }
  
  .card-trend.down {
    color: #ff6b6b;
    text-shadow: 0 0 5px rgba(255, 107, 107, 0.5);
  }
  
  .card-trend.stable {
    color: #ffb830;
    text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
  }
  
  /* 内容区域 */
  .content {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 32px;
    margin-bottom: 35px;
  }
  
  .panel {
    background: rgba(6, 30, 93, 0.6);
    border: 1px solid rgba(0, 255, 255, 0.4);
    border-radius: 14px;
    padding: 24px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    height: 520px;
    backdrop-filter: blur(5px);
  }
  
  .panel-header {
    margin-bottom: 22px;
  }
  
  .panel-title {
    font-size: 20px;
    font-weight: bold;
    color: #00f0ff;
    text-shadow: 0 0 8px #00ffff;
    margin-bottom: 6px;
    letter-spacing: 0.5px;
  }
  
  .panel-subtitle {
    font-size: 13px;
    color: #b3d9ff;
    opacity: 0.9;
    font-weight: 500;
  }
  
  .glow {
    box-shadow: 0 0 18px rgba(0, 255, 255, 0.2);
    transition: all 0.3s ease;
  }
  
  .glow:hover {
    box-shadow: 0 0 25px rgba(0, 255, 255, 0.3);
    transform: translateY(-3px);
    border-color: rgba(0, 255, 255, 0.6);
  }
  
  /* 排名列表样式 */
  .ranking-container {
    flex: 1;
    overflow-y: auto;
    padding-right: 8px;
  }
  
  .ranking-container::-webkit-scrollbar {
    width: 6px;
  }
  
  .ranking-container::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
  }
  
  .ranking-container::-webkit-scrollbar-thumb {
    background: rgba(0, 240, 255, 0.5);
    border-radius: 3px;
  }
  
  .ranking-container::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 240, 255, 0.7);
  }
  
  .ranking-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.15);
    transition: all 0.3s ease;
  }
  
  .ranking-item:hover {
    background: rgba(0, 240, 255, 0.15);
    transform: translateX(8px);
    border-radius: 8px;
    padding-left: 12px;
    margin-right: -12px;
  }
  
  .ranking-item.top-three {
    background: linear-gradient(90deg, rgba(0, 240, 255, 0.2), transparent);
    border-left: 3px solid #00f0ff;
  }
  
  .rank-index {
    position: relative;
    width: 44px;
    text-align: center;
    margin-right: 18px;
  }
  
  .rank-number {
    font-size: 18px;
    font-weight: bold;
    color: #00f0ff;
    text-shadow: 0 0 5px rgba(0, 240, 255, 0.5);
  }
  
  .rank-medal {
    position: absolute;
    top: -6px;
    right: -8px;
    font-size: 16px;
    filter: drop-shadow(0 0 3px currentColor);
  }
  
  .rank-medal .el-icon-medal-1 { color: gold; }
  .rank-medal .el-icon-medal { color: silver; }
  .rank-medal .el-icon-trophy { color: #cd7f32; }
  
  .rank-name {
    flex: 1;
    font-size: 15px;
    color: #fff;
    font-weight: 500;
    letter-spacing: 0.3px;
  }
  
  .rank-value {
    width: 70px;
    text-align: right;
    font-size: 15px;
    color: #00f0ff;
    margin-right: 18px;
    font-weight: 600;
    text-shadow: 0 0 5px rgba(0, 240, 255, 0.3);
  }
  
  .rank-bar {
    width: 120px;
    height: 10px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 5px;
    overflow: hidden;
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.3);
  }
  
  .bar-fill {
    height: 100%;
    background: linear-gradient(90deg, #00f0ff, #009dff);
    border-radius: 5px;
    transition: width 0.5s ease;
    box-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
  }
  
  /* 饼图样式 */
  .chart-container {
    display: flex;
    height: 100%;
    gap: 24px;
  }
  
  .pie-chart {
    flex: 1;
    overflow-y: auto;
    padding-right: 8px;
  }
  
  .pie-chart::-webkit-scrollbar {
    width: 6px;
  }
  
  .pie-chart::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
  }
  
  .pie-chart::-webkit-scrollbar-thumb {
    background: rgba(0, 240, 255, 0.5);
    border-radius: 3px;
  }
  
  .pie-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.2s ease;
  }
  
  .pie-item:hover {
    background: rgba(255, 255, 255, 0.05);
    border-radius: 6px;
    padding-left: 8px;
    margin-right: -8px;
  }
  
  .pie-color {
    width: 14px;
    height: 14px;
    border-radius: 50%;
    margin-right: 12px;
    box-shadow: 0 0 5px currentColor;
  }
  
  .pie-info {
    flex: 1;
  }
  
  .pie-name {
    font-size: 14px;
    color: #fff;
    font-weight: 500;
    margin-bottom: 2px;
  }
  
  .pie-value {
    font-size: 12px;
    color: #b3d9ff;
    font-weight: 500;
  }
  
  .pie-visual {
    position: relative;
    width: 220px;
    height: 220px;
    margin: auto;
  }
  
  .pie-chart-svg {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 0 10px rgba(0, 240, 255, 0.3));
  }
  
  .pie-slice {
    transition: all 0.3s ease;
    cursor: pointer;
  }
  
  .pie-slice:hover {
    opacity: 0.9;
    transform: scale(1.05);
    filter: brightness(1.2);
  }
  
  .pie-total {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
  }
  
  .total-label {
    font-size: 13px;
    color: #b3d9ff;
    font-weight: 500;
    margin-bottom: 4px;
  }
  
  .total-value {
    font-size: 22px;
    font-weight: bold;
    color: #00f0ff;
    text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
  }
  
  /* 趋势图弹窗样式 */
  .trend-dialog-content {
    padding: 10px 0;
  }
  
  .trend-dialog-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 0 10px;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .trend-dialog-chart {
    background: rgba(6, 30, 93, 0.6);
    border: 1px solid rgba(0, 255, 255, 0.4);
    border-radius: 14px;
    padding: 20px;
    backdrop-filter: blur(5px);
    box-shadow: 0 0 18px rgba(0, 255, 255, 0.2);
  }
  
  .chart-type-btn {
    background: rgba(0, 240, 255, 0.1);
    border: 1px solid rgba(0, 240, 255, 0.3);
    color: #00f0ff;
  }
  
  .chart-type-btn:hover {
    background: rgba(0, 240, 255, 0.2);
    border-color: rgba(0, 240, 255, 0.5);
  }
  
  /* 趋势图容器 */
  .trend-chart-container {
    display: flex;
    height: 300px;
    position: relative;
  }
  
  .trend-y-axis {
    width: 60px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 30px 0;
    flex-shrink: 0;
  }
  
  .y-tick {
    font-size: 12px;
    color: #b3d9ff;
    text-align: right;
    padding-right: 10px;
    font-weight: 500;
  }
  
  .trend-main-content {
    flex: 1;
    position: relative;
    padding: 30px 0;
    overflow-x: auto;
    min-width: 500px;
  }
  
  .trend-main-content::-webkit-scrollbar {
    height: 6px;
  }
  
  .trend-main-content::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 3px;
  }
  
  .trend-main-content::-webkit-scrollbar-thumb {
    background: rgba(0, 240, 255, 0.5);
    border-radius: 3px;
  }
  
  .trend-grid {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }
  
  .grid-line {
    border-top: 1px solid rgba(255, 255, 255, 0.15);
    height: 1px;
  }
  
  .trend-chart {
    height: 100%;
    position: relative;
  }
  
  /* 折线图样式 */
  .line-chart-wrapper {
    position: relative;
    width: 100%;
    height: 100%;
  }
  
  .line-chart-svg {
    width: 100%;
    height: 100%;
  }
  
  .line-path {
    filter: drop-shadow(0 0 4px rgba(0, 240, 255, 0.6));
  }
  
  .data-point {
    cursor: pointer;
    transition: all 0.2s ease;
  }
  
  .data-point:hover {
    transform: scale(1.3);
    filter: drop-shadow(0 0 8px rgba(0, 240, 255, 0.8));
  }
  
  /* 柱状图样式 */
  .bar-chart-wrapper {
    width: 100%;
    height: 100%;
    padding: 0 15px;
  }
  
  .bar-chart {
    display: flex;
    align-items: flex-end;
    justify-content: space-around;
    height: 100%;
    min-width: 500px;
  }
  
  .trend-bar-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    max-width: 60px;
    height: 100%;
  }
  
  .trend-bar {
    width: 30px;
    background: linear-gradient(to top, #00f0ff, #009dff);
    border-radius: 6px 6px 0 0;
    position: relative;
    transition: all 0.3s ease;
    cursor: pointer;
    box-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
    min-height: 10px;
  }
  
  .trend-bar:hover {
    transform: scale(1.15);
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.6);
    background: linear-gradient(to top, #00f0ff, #00c8ff);
  }
  
  .bar-value {
    position: absolute;
    top: -24px;
    left: 50%;
    transform: translateX(-50%);
    font-size: 11px;
    color: #00f0ff;
    white-space: nowrap;
    font-weight: 600;
    text-shadow: 0 0 4px rgba(0, 240, 255, 0.5);
  }
  
  .bar-date {
    margin-top: 10px;
    font-size: 11px;
    color: #b3d9ff;
    text-align: center;
    font-weight: 500;
  }
  
  /* 图例和统计 */
  .trend-legend {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 15px;
    padding: 12px 15px;
    background: rgba(0, 240, 255, 0.05);
    border-radius: 8px;
    border: 1px solid rgba(0, 240, 255, 0.2);
    flex-wrap: wrap;
    gap: 15px;
  }
  
  .legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #b3d9ff;
    font-weight: 500;
  }
  
  .legend-color {
    width: 14px;
    height: 14px;
    border-radius: 4px;
    background: #00f0ff;
    box-shadow: 0 0 4px rgba(0, 240, 255, 0.5);
  }
  
  .trend-stats {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
  }
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
  }
  
  .stat-label {
    color: #b3d9ff;
    font-weight: 500;
  }
  
  .stat-value {
    color: #00f0ff;
    font-weight: 600;
    text-shadow: 0 0 4px rgba(0, 240, 255, 0.3);
  }
  
  .stat-value.positive {
    color: #26fcd8;
  }
  
  .stat-value.negative {
    color: #ff6b6b;
  }
  
  /* 趋势图提示框 */
  .trend-tooltip {
    position: absolute;
    background: rgba(6, 30, 93, 0.95);
    border: 1px solid rgba(0, 240, 255, 0.6);
    border-radius: 8px;
    padding: 10px 14px;
    color: white;
    font-size: 12px;
    box-shadow: 0 0 12px rgba(0, 240, 255, 0.3);
    backdrop-filter: blur(10px);
    z-index: 1000;
    pointer-events: none;
    transform: translate(-50%, -100%);
  }
  
  .trend-tooltip::after {
    content: '';
    position: absolute;
    bottom: -5px;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-top: 5px solid rgba(0, 240, 255, 0.6);
  }
  
  .tooltip-date {
    font-weight: 600;
    color: #00f0ff;
    margin-bottom: 3px;
  }
  
  .tooltip-value {
    color: #b3d9ff;
    font-weight: 500;
  }
  
  /* 加载和空状态 */
  .loading-container, .no-data {
    text-align: center;
    color: #b3d9ff;
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    gap: 12px;
    font-weight: 500;
    height: 200px;
  }
  
  .loading-container i, .no-data i {
    font-size: 32px;
    margin-bottom: 10px;
    color: #00f0ff;
  }
  
  .no-data i {
    color: #ffb830;
  }
  
  /* 弹窗样式定制 */
  ::v-deep .trend-dialog {
    background: rgba(8, 21, 41, 0.95);
    border: 1px solid rgba(0, 240, 255, 0.4);
    border-radius: 14px;
    box-shadow: 0 0 30px rgba(0, 240, 255, 0.3);
    backdrop-filter: blur(10px);
    max-width: 95%;
  }
  
  ::v-deep .trend-dialog .el-dialog__header {
    background: rgba(6, 30, 93, 0.8);
    border-bottom: 1px solid rgba(0, 240, 255, 0.3);
    border-radius: 14px 14px 0 0;
    padding: 15px 20px;
  }
  
  ::v-deep .trend-dialog .el-dialog__title {
    color: #00f0ff;
    font-weight: bold;
    font-size: 18px;
    text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
  }
  
  ::v-deep .trend-dialog .el-dialog__headerbtn {
    top: 15px;
  }
  
  ::v-deep .trend-dialog .el-dialog__headerbtn .el-dialog__close {
    color: #00f0ff;
    font-size: 18px;
  }
  
  ::v-deep .trend-dialog .el-dialog__headerbtn:hover .el-dialog__close {
    color: #fff;
    text-shadow: 0 0 10px rgba(0, 240, 255, 0.8);
  }
  
  ::v-deep .trend-dialog .el-dialog__body {
    padding: 20px;
    background: radial-gradient(ellipse at center, #081529 0%, #030d1f 100%);
    color: #fff;
    max-height: 70vh;
    overflow-y: auto;
  }
  
  ::v-deep .trend-dialog .el-dialog__footer {
    background: rgba(6, 30, 93, 0.8);
    border-top: 1px solid rgba(0, 240, 255, 0.3);
    border-radius: 0 0 14px 14px;
    padding: 15px 20px;
  }
  
  /* 响应式布局 */
  @media (max-width: 1200px) {
    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }
  }
  
  @media (max-width: 1024px) {
    .content {
      grid-template-columns: 1fr;
      gap: 25px;
    }
    
    .panel {
      height: auto;
      min-height: 450px;
    }
    
    .stats-cards {
      grid-template-columns: 1fr;
    }
    
    .chart-container {
      flex-direction: column;
    }
    
    .pie-chart {
      max-height: 200px;
    }
    
    .header {
      flex-direction: column;
      align-items: flex-start;
      gap: 18px;
    }
    
    .action-buttons {
      width: 100%;
      justify-content: flex-start;
    }
  }
  
  @media (max-width: 768px) {
    .trend-chart-container {
      flex-direction: column;
      height: auto;
    }
    
    .trend-y-axis {
      width: 100%;
      flex-direction: row;
      padding: 0 20px;
      margin-bottom: 10px;
      height: auto;
    }
    
    .trend-dialog-controls {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .trend-legend {
      flex-direction: column;
      align-items: flex-start;
    }
    
    .trend-stats {
      width: 100%;
      justify-content: space-between;
    }
    
    .title {
      font-size: 22px;
    }
    
    .stat-card {
      padding: 18px;
    }
    
    .card-value {
      font-size: 24px;
    }
    
    .action-buttons {
      gap: 8px;
    }
    
    .action-buttons .el-button {
      flex: 1;
      min-width: 120px;
    }
  }
  </style>