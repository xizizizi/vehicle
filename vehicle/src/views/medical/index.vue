<template>
  <div class="medical-rescue-page">
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 标题区域 -->
      <div class="header-section">
        <div class="header-title">
          <h1><i class="el-icon-first-aid-kit"></i> 医疗无人机紧急救援系统</h1>
          <p>实时监测医疗任务，快速部署无人机进行救援</p>
        </div>
        <div class="header-actions">
          <el-button
            type="primary"
            size="small"
            @click="refreshTasks"
            :loading="refreshing"
            class="action-btn"
          >
            <i class="el-icon-refresh"></i>
            {{ refreshing ? "刷新中..." : "刷新数据" }}
          </el-button>
        </div>
      </div>

      <!-- 状态信息卡片 -->
      <div class="stats-grid">
        <div class="stat-card" v-for="card in statCards" :key="card.title">
          <div class="stat-icon" :style="{ color: card.color }">
            <i :class="card.icon"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-title">{{ card.title }}</div>
          </div>
          <div class="stat-trend" :class="card.trend">
            <i :class="card.trendIcon"></i>
            {{ card.trendValue }}
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-layout">
        <!-- 左侧：任务列表和部署表单 -->
        <div class="left-content">
          <!-- 任务筛选和分页控制 -->
          <div class="task-controls">
            <div class="filter-section">
              <div class="filter-item">
                <span class="filter-label">时间排序：</span>
                <el-select
                  v-model="timeOrder"
                  size="small"
                  placeholder="请选择排序方式"
                  @change="handleTimeOrderChange"
                  style="width: 120px"
                  class="tech-select"
                >
                  <el-option label="最新优先" value="desc"></el-option>
                  <el-option label="最早优先" value="asc"></el-option>
                </el-select>
              </div>
              <div class="filter-item">
                <span class="filter-label">紧急程度：</span>
                <el-select
                  v-model="severityFilter"
                  size="small"
                  placeholder="请选择紧急程度"
                  multiple
                  collapse-tags
                  @change="handleSeverityFilterChange"
                  style="width: 180px"
                  class="tech-select"
                >
                  <el-option label="危急" value="CRITICAL"></el-option>
                  <el-option label="高" value="HIGH"></el-option>
                  <el-option label="中" value="MEDIUM"></el-option>
                  <el-option label="普通" value="NORMAL"></el-option>
                </el-select>
              </div>
              <div class="filter-item">
                <span class="filter-label">任务状态：</span>
                <el-select
                  v-model="statusFilter"
                  size="small"
                  placeholder="请选择任务状态"
                  multiple
                  collapse-tags
                  @change="handleStatusFilterChange"
                  style="width: 150px"
                  class="tech-select"
                >
                  <el-option label="待处理" value="PENDING"></el-option>
                  <el-option label="执行中" value="EXECUTING"></el-option>
                  <el-option label="已完成" value="COMPLETED"></el-option>
                </el-select>
              </div>
              <el-button
                type="text"
                size="small"
                @click="resetFilters"
                class="reset-btn tech-btn"
              >
                <i class="el-icon-refresh-right"></i> 重置筛选
              </el-button>
            </div>

            <div class="pagination-info">
              <span
                >共 {{ totalTasks }} 个任务，当前显示
                {{ currentTasks.length }} 个</span
              >
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="currentPage"
                :page-sizes="[3, 6, 9, 12]"
                :page-size="pageSize"
                layout="sizes, prev, pager, next"
                :total="filteredTasks.length"
                small
                background
                class="tech-pagination"
              >
              </el-pagination>
            </div>
          </div>

          <!-- 医疗任务列表 -->
          <div class="panel task-panel">
            <div class="panel-header">
              <div class="panel-title">
                <i class="el-icon-s-order"></i> 医疗任务列表
              </div>
              <div class="panel-subtitle">
                <el-tag size="small" type="info" class="tech-tag">
                  第 {{ currentPage }} 页 / 共
                  {{ Math.ceil(filteredTasks.length / pageSize) }} 页
                </el-tag>
                <el-tag
                  size="small"
                  type="success"
                  class="tech-tag"
                  v-if="availableUAVsCount > 0"
                >
                  可用无人机: {{ availableUAVsCount }}
                </el-tag>
                <el-tag size="small" type="warning" class="tech-tag" v-else>
                  暂无可用无人机
                </el-tag>
              </div>
            </div>

            <div class="panel-content">
              <el-empty
                v-if="currentTasks.length === 0 && !loading"
                description="暂无符合条件的医疗任务"
                :image-size="60"
                class="tech-empty"
              >
                <el-button
                  type="primary"
                  size="mini"
                  @click="resetFilters"
                  class="tech-btn"
                >
                  重置筛选条件
                </el-button>
              </el-empty>

              <div v-else class="task-grid">
                <div
                  v-for="task in currentTasks"
                  :key="task.id"
                  class="task-card"
                  :class="[
                    getSeverityClass(task.severityLevel),
                    {
                      'task-disabled':
                        task.status === 'EXECUTING' ||
                        task.status === 'COMPLETED',
                    },
                  ]"
                  @click="selectTask(task)"
                >
                  <div
                    class="task-badge"
                    :class="getStatusBadgeClass(task.status)"
                  >
                    {{ getStatusText(task.status) }}
                  </div>

                  <div class="task-header">
                    <el-tag
                      :type="getSeverityTagType(task.severityLevel)"
                      size="small"
                      class="severity-tag tech-tag"
                    >
                      {{ getSeverityText(task.severityLevel) }}
                    </el-tag>
                    <span class="task-name">{{ task.taskName }}</span>
                  </div>

                  <div class="task-description">
                    {{ truncateText(task.description, 60) }}
                  </div>

                  <div class="task-meta">
                    <div class="meta-row">
                      <i class="el-icon-location-information"></i>
                      <span
                        >{{
                          task.latitude ? task.latitude.toFixed(4) : "未知"
                        }},
                        {{
                          task.longitude ? task.longitude.toFixed(4) : "未知"
                        }}</span
                      >
                    </div>
                    <div class="meta-row">
                      <i class="el-icon-time"></i>
                      <span>{{ formatTime(task.captureTime) }}</span>
                    </div>
                    <div class="meta-row">
                      <i class="el-icon-timer"></i>
                      <span class="response-time"
                        >响应时间: {{ task.responseTime }}分钟</span
                      >
                    </div>
                  </div>

                  <div class="task-footer">
                    <div class="task-info">
                      <span v-if="task.droneId" class="drone-info">
                        <i class="el-icon-position"></i> {{ task.droneId }}
                      </span>
                      <span v-if="task.fileCount" class="file-info">
                        <i class="el-icon-document"></i>
                        {{ task.fileCount }}个文件
                      </span>
                    </div>
                    <div class="task-actions">
                      <el-button
                        type="primary"
                        size="mini"
                        @click.stop="handleDeployForTask(task)"
                        :loading="
                          deployLoading &&
                          selectedTask &&
                          selectedTask.id === task.id
                        "
                        :disabled="
                          task.status === 'EXECUTING' ||
                          task.status === 'COMPLETED' ||
                          task.droneId ||
                          availableUAVsCount === 0
                        "
                        class="deploy-btn tech-btn"
                      >
                        <i class="el-icon-position"></i>
                        {{ getDeployButtonText(task) }}
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：操作日志 -->
        <div class="right-content">
          <div class="panel logs-panel">
            <div class="panel-header">
              <div class="panel-title">
                <i class="el-icon-document"></i> 操作日志
              </div>
              <div class="panel-actions">
                <el-button
                  size="small"
                  @click="clearLogs"
                  class="log-btn tech-btn"
                >
                  清空日志
                </el-button>
              </div>
            </div>

            <div class="panel-content logs-container">
              <div v-if="logs.length === 0" class="empty-logs">
                <i class="el-icon-document"></i>
                <p>暂无操作日志</p>
              </div>

              <div v-else class="log-list">
                <div
                  v-for="(log, index) in logs"
                  :key="index"
                  class="log-item"
                  :class="'log-' + log.type"
                >
                  <div class="log-time">[{{ formatTime(log.timestamp) }}]</div>
                  <div class="log-message">{{ log.message }}</div>
                  <div class="log-type" :class="'type-' + log.type">
                    {{
                      log.type === "success"
                        ? "成功"
                        : log.type === "error"
                        ? "错误"
                        : "信息"
                    }}
                  </div>
                </div>
              </div>
            </div>

            <div class="panel-footer">
              <div class="log-stats">
                <span>共 {{ logs.length }} 条日志</span>
                <span class="log-type-count">
                  <span class="type-success"
                    >{{ getLogTypeCount("success") }} 成功</span
                  >
                  <span class="type-error"
                    >{{ getLogTypeCount("error") }} 错误</span
                  >
                  <span class="type-info"
                    >{{ getLogTypeCount("info") }} 信息</span
                  >
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 全屏无人机部署对话框 -->
    <el-dialog
      :visible.sync="fullscreenDialogVisible"
      fullscreen
      :close-on-click-modal="false"
      :show-close="true"
      custom-class="fullscreen-deploy-dialog"
      @close="cancelDeploy"
    >
      <template slot="title">
        <div class="dialog-title">
          <i class="el-icon-setting"></i> 无人机部署配置 -
          {{ selectedTask ? selectedTask.taskName : "医疗救援任务" }}
          <el-tag
            v-if="selectedTask"
            :type="getSeverityTagType(selectedTask.severityLevel)"
            size="small"
            style="margin-left: 10px"
            class="tech-tag"
          >
            {{ getSeverityText(selectedTask.severityLevel) }}
          </el-tag>
        </div>
      </template>

      <div class="fullscreen-deploy-content">
        <template v-if="selectedTask">
          <el-form
            ref="deployForm"
            :model="deployForm"
            :rules="rules"
            label-width="120px"
            class="deploy-form"
          >
            <div class="fullscreen-form-sections">
              <div class="form-section">
                <div class="section-title">
                  <i class="el-icon-position"></i> 选择可用无人机
                </div>
                <el-form-item label="选择无人机" prop="uavId" required>
                  <el-select
                    v-model="deployForm.uavId"
                    placeholder="请选择可用无人机"
                    class="form-select tech-select"
                    size="medium"
                    @change="handleUavSelect"
                    filterable
                    :disabled="availableUAVs.length === 0"
                  >
                    <el-option-group
                      v-for="group in uavOptions"
                      :key="group.label"
                      :label="group.label"
                    >
                      <el-option
                        v-for="uav in group.options"
                        :key="uav.id"
                        :label="getUavLabel(uav)"
                        :value="uav.id"
                        :disabled="uav.batteryLevel < 30"
                      >
                        <span style="float: left">{{ uav.sn }}</span>
                        <span
                          style="float: right; color: #8492a6; font-size: 13px"
                        >
                          {{ uav.model }} | 电量: {{ uav.batteryLevel }}% |
                          状态: {{ getUavStatusText(uav.status) }}
                        </span>
                      </el-option>
                    </el-option-group>
                  </el-select>
                  <div v-if="availableUAVs.length === 0" class="no-uav-warning">
                    <i class="el-icon-warning"></i>
                    <span>当前没有可用的无人机，请等待无人机空闲</span>
                  </div>
                </el-form-item>

                <!-- 显示选中无人机信息 -->
                <div v-if="selectedUav" class="selected-uav-info">
                  <div class="info-row">
                    <span class="info-label">序列号：</span>
                    <span class="info-value highlight">{{
                      selectedUav.sn
                    }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">当前电量：</span>
                    <span
                      class="info-value"
                      :class="getBatteryClass(selectedUav.batteryLevel)"
                    >
                      {{ selectedUav.batteryLevel }}%
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">当前位置：</span>
                    <span class="info-value">
                      {{ getUavLocation(selectedUav) }}
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">负载能力：</span>
                    <span class="info-value"
                      >{{ selectedUav.loadCapacity }} kg</span
                    >
                  </div>
                </div>
              </div>

              <div class="form-section">
                <div class="section-title">
                  <i class="el-icon-map-location"></i> 飞行路线配置
                </div>
                <el-row :gutter="30">
                  <el-col :span="12">
                    <el-form-item label="起始地点" prop="fromLocation" required>
                      <el-select
                        v-model="deployForm.fromLocation"
                        placeholder="请选择无人机起飞点"
                        class="form-select tech-select"
                        size="medium"
                        :disabled="!selectedUav"
                      >
                        <el-option
                          v-for="location in availableLocations"
                          :key="location"
                          :label="location"
                          :value="location"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="目的地点" prop="toLocation" required>
                      <el-select
                        v-model="deployForm.toLocation"
                        placeholder="请选择任务目的地"
                        class="form-select tech-select"
                        size="medium"
                        :disabled="!selectedUav"
                      >
                        <el-option
                          v-for="location in availableLocations"
                          :key="location"
                          :label="location"
                          :value="location"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>

              <div class="form-section">
                <div class="section-title">
                  <i class="el-icon-box"></i> 负载配置
                </div>
                <el-row :gutter="30">
                  <el-col :span="24">
                    <el-form-item label="物资重量(kg)" prop="weight" required>
                      <el-input-number
                        v-model="deployForm.weight"
                        :min="0.1"
                        :max="selectedUav ? selectedUav.loadCapacity : 10"
                        :step="0.1"
                        controls-position="right"
                        class="form-number tech-input"
                        size="medium"
                        :disabled="!selectedUav"
                      ></el-input-number>
                      <span class="form-unit">千克</span>
                      <span v-if="selectedUav" class="capacity-info">
                        可用负载:
                        {{
                          (
                            selectedUav.loadCapacity - deployForm.weight
                          ).toFixed(1)
                        }}
                        kg
                      </span>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </div>

            <div
              v-if="selectedUav && selectedUav.batteryLevel < 50"
              class="status-warning"
            >
              <i class="el-icon-warning"></i>
              <span
                >当前无人机电量较低 ({{
                  selectedUav.batteryLevel
                }}%)，建议选择电量更高的无人机</span
              >
            </div>

            <div class="fullscreen-info-grid">
              <div class="info-card">
                <div class="info-card-title">
                  <i class="el-icon-map-location"></i> 配送路线信息
                </div>
                <div class="info-card-content">
                  <div class="info-row">
                    <span class="info-label">起始地点：</span>
                    <span class="info-value highlight">{{
                      deployForm.fromLocation || "未选择"
                    }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">目的地点：</span>
                    <span class="info-value highlight">{{
                      deployForm.toLocation || "未选择"
                    }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">物资重量：</span>
                    <span class="info-value">{{ deployForm.weight }} kg</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">负载余量：</span>
                    <span
                      class="info-value"
                      :class="getLoadCapacityClass(selectedUav)"
                    >
                      {{
                        selectedUav
                          ? (
                              selectedUav.loadCapacity - deployForm.weight
                            ).toFixed(1)
                          : 0
                      }}
                      kg
                    </span>
                  </div>
                </div>
              </div>

              <div class="info-card">
                <div class="info-card-title">
                  <i class="el-icon-document"></i> 任务信息概览
                </div>
                <div class="info-card-content">
                  <div class="info-row">
                    <span class="info-label">紧急程度：</span>
                    <el-tag
                      size="small"
                      :type="getSeverityTagType(selectedTask.severityLevel)"
                      class="tech-tag"
                    >
                      {{ getSeverityText(selectedTask.severityLevel) }}
                    </el-tag>
                  </div>
                  <div class="info-row">
                    <span class="info-label">响应要求：</span>
                    <span class="info-value warning"
                      >{{ selectedTask.responseTime }} 分钟</span
                    >
                  </div>
                  <div class="info-row">
                    <span class="info-label">上报时间：</span>
                    <span class="info-value">{{
                      formatTime(selectedTask.captureTime)
                    }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">预计飞行：</span>
                    <span class="info-value"
                      >{{ calculateFlightTime() }} 分钟</span
                    >
                  </div>
                </div>
              </div>

              <div class="info-card">
                <div class="info-card-title">
                  <i class="el-icon-cpu"></i> 系统检查
                </div>
                <div class="info-card-content">
                  <div class="info-row">
                    <span class="info-label">无人机状态：</span>
                    <span
                      class="info-value"
                      :class="getUavStatusClass(selectedUav)"
                    >
                      {{ getUavStatusText(selectedUav) }}
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">电池电量：</span>
                    <span
                      class="info-value"
                      :class="
                        getBatteryClass(
                          selectedUav ? selectedUav.batteryLevel : 0
                        )
                      "
                    >
                      {{
                        selectedUav ? selectedUav.batteryLevel + "%" : "未选择"
                      }}
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">负载检查：</span>
                    <span
                      class="info-value"
                      :class="getLoadCheckClass(selectedUav)"
                    >
                      {{ getLoadCheckText(selectedUav) }}
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">路线验证：</span>
                    <span class="info-value" :class="getRouteCheckClass()">
                      {{ getRouteCheckText() }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div class="fullscreen-action-bar">
              <div class="action-bar-left">
                <el-button
                  type="info"
                  @click="cancelDeploy"
                  size="medium"
                  class="tech-btn"
                >
                  <i class="el-icon-close"></i> 取消部署
                </el-button>
              </div>
              <div class="action-bar-right">
                <el-button
                  type="primary"
                  @click="deployUAV"
                  :loading="deployLoading"
                  :disabled="
                    !selectedUav ||
                    !deployForm.fromLocation ||
                    !deployForm.toLocation ||
                    availableUAVs.length === 0
                  "
                  size="medium"
                  class="deploy-submit-btn tech-btn"
                >
                  <i class="el-icon-position"></i> 确认部署无人机并创建救援订单
                </el-button>
              </div>
            </div>
          </el-form>
        </template>
        <div v-else class="empty-deploy-content">
          <el-empty description="未选择医疗任务" :image-size="100">
            <p style="color: #b3d9ff; margin-top: 10px">请先选择一个医疗任务</p>
          </el-empty>
        </div>
      </div>
    </el-dialog>

    <!-- 加载遮罩 -->
    <el-dialog
      :visible.sync="loading"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      width="300px"
      center
      class="loading-dialog"
    >
      <div class="loading-content">
        <el-progress
          type="circle"
          :percentage="loadProgress"
          :status="loadProgress === 100 ? 'success' : undefined"
          :color="progressColors"
        ></el-progress>
        <p class="loading-message">{{ loadingMessage }}</p>
      </div>
    </el-dialog>
  </div>
</template>
  
  <script>
import axios from "axios";

export default {
  name: "MedicalRescue",
  data() {
    return {
      // 医疗任务数据
      medicalTasks: [],
      selectedTask: null,

      // 分页和筛选
      currentPage: 1,
      pageSize: 3,
      timeOrder: "desc",
      severityFilter: [],
      statusFilter: [],

      // 可用无人机数据
      availableUAVs: [],
      selectedUav: null,
      uavOptions: [],

      // 可用地点列表
      availableLocations: [
        "南昌市第一医院",
        "南昌市洪都中医院",
        "南昌大学第一附属医院",
        "江西省人民医院",
        "江西省儿童医院",
        "江西省妇幼保健院(九龙湖院区)",
        "八一广场",
        "南昌站",
        "南昌西站",
        "昌北机场",
        "秋水广场",
        "红谷滩万达广场",
        "中山路商业街",
        "滕王阁",
        "八一公园",
        "华东交通大学",
        "南昌大学",
        "江西师范大学",
        "南昌航空大学",
      ],

      // 部署表单
      deployForm: {
        uavId: null,
        fromLocation: "",
        toLocation: "",
        weight: 1.0,
      },

      // 表单验证规则
      rules: {
        uavId: [{ required: true, message: "请选择无人机", trigger: "change" }],
        fromLocation: [
          { required: true, message: "请选择起飞地点", trigger: "change" },
        ],
        toLocation: [
          { required: true, message: "请选择任务地点", trigger: "change" },
        ],
        weight: [
          { required: true, message: "请输入物资重量", trigger: "blur" },
        ],
      },

      // 统计卡片
      statCards: [
        {
          title: "总医疗任务",
          value: "0",
          icon: "el-icon-document",
          color: "#00f0ff",
          trend: "up",
          trendIcon: "el-icon-top",
          trendValue: "+0%",
        },
        {
          title: "危急任务",
          value: "0",
          icon: "el-icon-warning-outline",
          color: "#ff6b6b",
          trend: "up",
          trendIcon: "el-icon-top",
          trendValue: "+0",
        },
        {
          title: "可用无人机",
          value: "0",
          icon: "el-icon-position",
          color: "#26fcd8",
          trend: "stable",
          trendIcon: "el-icon-minus",
          trendValue: "0",
        },
        {
          title: "平均响应",
          value: "0分",
          icon: "el-icon-timer",
          color: "#ffb830",
          trend: "down",
          trendIcon: "el-icon-bottom",
          trendValue: "-0%",
        },
      ],

      // 加载状态
      loading: false,
      loadProgress: 0,
      loadingMessage: "",
      deployLoading: false,
      refreshing: false,

      // 操作日志
      logs: [],

      // 进度条颜色
      progressColors: [
        { color: "#00f0ff", percentage: 20 },
        { color: "#26fcd8", percentage: 40 },
        { color: "#ffb830", percentage: 60 },
        { color: "#ff6b6b", percentage: 80 },
        { color: "#a29bfe", percentage: 100 },
      ],

      // 全屏对话框显示控制
      fullscreenDialogVisible: false,

      // 定时器引用
      refreshInterval: null,

      // 当前字体大小基准
      baseFontSize: 14,
    };
  },
  computed: {
    filteredTasks() {
      let tasks = [...this.medicalTasks];

      if (this.severityFilter.length > 0) {
        tasks = tasks.filter((task) =>
          this.severityFilter.includes(task.severityLevel)
        );
      }

      if (this.statusFilter.length > 0) {
        tasks = tasks.filter((task) => this.statusFilter.includes(task.status));
      }

      tasks.sort((a, b) => {
        const timeA = new Date(a.captureTime).getTime();
        const timeB = new Date(b.captureTime).getTime();
        return this.timeOrder === "desc" ? timeB - timeA : timeA - timeB;
      });

      return tasks;
    },

    currentTasks() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredTasks.slice(start, end);
    },

    totalTasks() {
      return this.medicalTasks.length;
    },

    availableUAVsCount() {
      return this.availableUAVs.length;
    },
  },
  created() {
    this.loadAllData();
    this.refreshInterval = setInterval(() => {
      this.refreshTasks();
    }, 3000000);

    window.addEventListener("resize", this.handleResize);
    this.handleResize();
  },
  beforeDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
    window.removeEventListener("resize", this.handleResize);
  },
  methods: {
    // 获取部署按钮文本
    getDeployButtonText(task) {
      if (this.availableUAVsCount === 0) return "无可用无人机";
      if (task.droneId) return "已部署";
      if (task.status === "EXECUTING") return "执行中";
      if (task.status === "COMPLETED") return "已完成";
      return "立即救援";
    },

    // 获取无人机位置信息
    getUavLocation(uav) {
      if (!uav) return "未知位置";
      const lng = uav.currentLng ? uav.currentLng.toFixed(4) : "未知";
      const lat = uav.currentLat ? uav.currentLat.toFixed(4) : "未知";
      return `${lng}, ${lat}`;
    },

    // 获取负载容量样式类
    getLoadCapacityClass(uav) {
      if (!uav) return "error";
      const remaining = uav.loadCapacity - this.deployForm.weight;
      return remaining >= 0 ? "success" : "error";
    },

    // 获取无人机状态样式类
    getUavStatusClass(uav) {
      if (!uav) return "";
      return uav.status === "IDLE" ? "success" : "warning";
    },

    // 获取无人机状态文本
    getUavStatusText(uav) {
      if (!uav) return "未选择";
      if (uav.status === "IDLE") return "✓ 正常";
      if (uav.status === "CHARGING") return "⚠ 待命";
      return "✗ 不可用";
    },

    // 获取负载检查样式类
    getLoadCheckClass(uav) {
      if (!uav) return "error";
      return this.deployForm.weight <= uav.loadCapacity ? "success" : "error";
    },

    // 获取负载检查文本
    getLoadCheckText(uav) {
      if (!uav) return "未选择";
      return this.deployForm.weight <= uav.loadCapacity ? "✓ 正常" : "✗ 超载";
    },

    // 获取路线检查样式类
    getRouteCheckClass() {
      const valid =
        this.deployForm.fromLocation &&
        this.deployForm.toLocation &&
        this.deployForm.fromLocation !== this.deployForm.toLocation;
      return valid ? "success" : "error";
    },

    // 获取路线检查文本
    getRouteCheckText() {
      const valid =
        this.deployForm.fromLocation &&
        this.deployForm.toLocation &&
        this.deployForm.fromLocation !== this.deployForm.toLocation;
      return valid ? "✓ 正常" : "✗ 无效";
    },

    handleResize() {
      const width = window.innerWidth;
      if (width < 768) {
        this.baseFontSize = 12;
      } else if (width < 992) {
        this.baseFontSize = 13;
      } else if (width < 1200) {
        this.baseFontSize = 14;
      } else {
        this.baseFontSize = 15;
      }

      document.documentElement.style.fontSize = `${this.baseFontSize}px`;
    },

    async loadAllData() {
      this.loading = true;
      this.loadProgress = 0;

      try {
        this.loadingMessage = "正在加载医疗任务数据...";
        await this.loadMedicalTasks();
        this.loadProgress = 33;

        this.loadingMessage = "正在加载无人机数据...";
        await this.loadAvailableUAVs();
        this.loadProgress = 66;

        this.loadingMessage = "正在更新统计信息...";
        this.updateStats();
        this.loadProgress = 100;

        this.addLog("success", "系统初始化完成，数据加载成功！");

        setTimeout(() => {
          this.loading = false;
        }, 500);
      } catch (error) {
        this.addLog("error", `数据加载失败: ${error.message}`);
        this.$message.error("数据加载失败，请检查网络连接");
        this.loading = false;
      }
    },

    async loadMedicalTasks() {
      try {
        const response = await axios.get(
          "http://localhost:8080/api/reports/data/list",
          {
            params: {
              systemType: "MEDICAL",
            },
          }
        );

        if (response.data && Array.isArray(response.data)) {
          this.medicalTasks = response.data.map((task) => {
            let status = "PENDING";
            if (task.droneId) {
              status = "EXECUTING";
            }

            return {
              ...task,
              status: status,
            };
          });

          this.addLog(
            "success",
            `成功加载 ${this.medicalTasks.length} 个医疗任务`
          );
        } else {
          this.medicalTasks = this.getMockMedicalTasks();
          this.addLog("info", "使用模拟医疗任务数据");
        }
      } catch (error) {
        console.error("加载医疗任务失败:", error);
        this.addLog("error", `加载医疗任务失败: ${error.message}`);
        this.medicalTasks = this.getMockMedicalTasks();
      }
    },

    async loadAvailableUAVs() {
      try {
        const [idleResponse, chargingResponse] = await Promise.all([
          axios.get("http://localhost:8080/api/uav-manage/status/IDLE"),
          axios.get("http://localhost:8080/api/uav-manage/status/CHARGING"),
        ]);

        const idleUAVs = idleResponse.data?.success
          ? idleResponse.data.data
          : [];
        const chargingUAVs = chargingResponse.data?.success
          ? chargingResponse.data.data
          : [];

        this.availableUAVs = [...idleUAVs, ...chargingUAVs];

        this.uavOptions = [
          {
            label: "空闲无人机",
            options: idleUAVs,
          },
          {
            label: "待命无人机",
            options: chargingUAVs,
          },
        ];

        this.addLog(
          "success",
          `成功加载 ${this.availableUAVs.length} 架可用无人机 (空闲: ${idleUAVs.length}, 待命: ${chargingUAVs.length})`
        );
      } catch (error) {
        console.error("加载无人机数据失败:", error);
        this.addLog("error", `加载无人机数据失败: ${error.message}`);

        this.availableUAVs = this.getMockUAVs();
        this.uavOptions = [
          {
            label: "空闲无人机",
            options: this.availableUAVs.filter((uav) => uav.status === "IDLE"),
          },
          {
            label: "待命无人机",
            options: this.availableUAVs.filter(
              (uav) => uav.status === "CHARGING"
            ),
          },
        ];
      }
    },

    updateStats() {
      const totalTasks = this.medicalTasks.length;
      const criticalTasks = this.medicalTasks.filter(
        (task) => task.severityLevel === "CRITICAL"
      ).length;

      let avgResponseTime = 0;
      if (this.medicalTasks.length > 0) {
        const totalResponseTime = this.medicalTasks.reduce(
          (sum, task) => sum + (task.responseTime || 0),
          0
        );
        avgResponseTime = Math.round(
          totalResponseTime / this.medicalTasks.length
        );
      }

      this.statCards[0].value = totalTasks.toString();
      this.statCards[1].value = criticalTasks.toString();
      this.statCards[2].value = this.availableUAVsCount.toString();
      this.statCards[3].value = `${avgResponseTime}分`;

      this.statCards[0].trendValue = `+${Math.floor(Math.random() * 15) + 5}%`;
      this.statCards[1].trendValue = `+${Math.floor(Math.random() * 3) + 1}`;
      this.statCards[3].trendValue = `-${Math.floor(Math.random() * 10) + 5}%`;
    },

    selectTask(task) {
      if (task.status === "EXECUTING" || task.status === "COMPLETED") {
        this.$message.warning("该任务已经部署或完成，无法再次部署");
        return;
      }

      this.selectedTask = task;
      this.autoSuggestLocations(task);
    },

    autoSuggestLocations(task) {
      this.deployForm.fromLocation = "南昌大学第一附属医院";

      const taskDesc = task.description || "";
      if (taskDesc.includes("八一广场")) {
        this.deployForm.toLocation = "八一广场";
      } else if (taskDesc.includes("南昌站")) {
        this.deployForm.toLocation = "南昌站";
      } else if (taskDesc.includes("医院")) {
        const hospitals = this.availableLocations.filter((loc) =>
          loc.includes("医院")
        );
        if (hospitals.length > 1) {
          this.deployForm.toLocation =
            hospitals.find((h) => h !== "南昌大学第一附属医院") || hospitals[0];
        }
      } else {
        const nonHospital = this.availableLocations.find(
          (loc) => !loc.includes("医院")
        );
        this.deployForm.toLocation = nonHospital || this.availableLocations[0];
      }
    },

    handleUavSelect(uavId) {
      this.selectedUav = this.availableUAVs.find((uav) => uav.id === uavId);

      if (this.selectedUav) {
        if (this.deployForm.weight > this.selectedUav.loadCapacity) {
          this.deployForm.weight = this.selectedUav.loadCapacity;
        }
      }
    },

    getUavLabel(uav) {
      return `${uav.sn} | ${uav.model} | ${this.getUavStatusText(uav)} | ${
        uav.batteryLevel
      }%`;
    },

    getBatteryClass(batteryLevel) {
      if (batteryLevel >= 70) return "success";
      if (batteryLevel >= 40) return "warning";
      return "error";
    },

    handleDeployForTask(task) {
      if (task.status === "EXECUTING" || task.status === "COMPLETED") {
        this.$message.warning("该任务已经部署或完成，无法再次部署");
        return;
      }

      if (this.availableUAVs.length === 0) {
        this.$message.warning("当前没有可用的无人机，请等待无人机空闲");
        return;
      }

      this.selectTask(task);
      this.fullscreenDialogVisible = true;

      if (this.availableUAVs.length > 0) {
        this.deployForm.uavId = this.availableUAVs[0].id;
        this.selectedUav = this.availableUAVs[0];
      }
    },

    calculateFlightTime() {
      const baseTime = 15;
      const weightFactor = this.selectedUav
        ? this.deployForm.weight / this.selectedUav.loadCapacity
        : 0;
      return Math.round(baseTime * (1 + weightFactor * 0.5));
    },

    async deployUAV() {
      try {
        await this.$refs.deployForm.validate();
      } catch (error) {
        this.$message.error("请填写完整的部署信息");
        return;
      }

      if (!this.selectedTask) {
        this.$message.error("请先选择任务");
        return;
      }

      if (!this.selectedUav) {
        this.$message.error("请先选择无人机");
        return;
      }

      if (this.selectedUav.batteryLevel < 30) {
        this.$message.warning("无人机电量过低，建议充电后使用");
        if (
          !(await this.$confirm("无人机电量低于30%，是否继续部署？", "提示", {
            confirmButtonText: "继续",
            cancelButtonText: "取消",
            type: "warning",
          }))
        ) {
          return;
        }
      }

      if (this.deployForm.weight > this.selectedUav.loadCapacity) {
        this.$message.error("物资重量超过无人机负载能力");
        return;
      }

      this.deployLoading = true;

      try {
        this.addLog("info", `开始部署无人机 ${this.selectedUav.sn}...`);

        // 注意：这里不要手动更新无人机状态！
        // 后端创建订单时会自动处理无人机状态

        this.addLog("info", "开始创建医疗救援订单...");

        const orderData = {
          fromLocation: this.deployForm.fromLocation,
          toLocation: this.deployForm.toLocation,
          weight: parseFloat(this.deployForm.weight),
          systemType: "MEDICAL",
          severityLevel: this.selectedTask.severityLevel,
          hospitalName: this.getHospitalName(this.deployForm.fromLocation),
          medicalDataId: this.selectedTask.id,
          droneSn: this.selectedUav.sn,
          responseTime: this.selectedTask.responseTime,
          assignedUavId: this.selectedUav.id, // 新增：明确指定无人机ID
        };

        console.log("创建订单数据:", orderData);

        const orderResponse = await axios.post(
          "http://localhost:8080/api/orders/medical",
          orderData
        );

        console.log("订单响应:", orderResponse.data);

        if (orderResponse.data && orderResponse.data.success) {
          const orderSn = orderResponse.data.data?.orderSn || "未知";
          this.addLog("success", `医疗救援订单创建成功！订单号: ${orderSn}`);

          // 更新本地任务状态
          this.updateTaskStatus(
            this.selectedTask.id,
            "EXECUTING",
            this.selectedUav.sn
          );

          // 同步医疗任务状态
          await this.syncMedicalTaskStatus(
            this.selectedTask.id,
            "EXECUTING",
            this.selectedUav.sn
          );

          this.$message.success(
            `无人机部署和救援订单创建成功！订单号: ${orderSn}`
          );

          this.fullscreenDialogVisible = false;
          this.cancelDeploy();

          setTimeout(() => {
            this.refreshTasks();
            this.loadAvailableUAVs();
          }, 1000);
        } else {
          throw new Error(orderResponse.data?.message || "订单创建失败");
        }
      } catch (error) {
        console.error("部署过程出错:", error);
        this.addLog("error", `部署过程出错: ${error.message}`);
        this.$message.error(`操作失败: ${error.message}`);
      } finally {
        this.deployLoading = false;
      }
    },

    async syncMedicalTaskStatus(taskId, status, droneId) {
      try {
        const response = await axios.post(
          `http://localhost:8080/api/medical-tasks/${taskId}/sync-status`,
          {
            status: status,
            droneId: droneId,
          }
        );

        if (response.data && response.data.success) {
          this.addLog("success", `医疗任务状态同步成功`);
          return true;
        } else {
          this.addLog("error", `状态同步失败: ${response.data?.message}`);
          return false;
        }
      } catch (error) {
        console.error("同步状态失败:", error);
        this.addLog("error", `同步状态失败: ${error.message}`);
        return false;
      }
    },

    updateTaskStatus(taskId, status, droneId = null) {
      const taskIndex = this.medicalTasks.findIndex(
        (task) => task.id === taskId
      );
      if (taskIndex !== -1) {
        this.medicalTasks[taskIndex].status = status;
        if (droneId) {
          this.medicalTasks[taskIndex].droneId = droneId;
        }
        this.$set(this.medicalTasks, taskIndex, this.medicalTasks[taskIndex]);
        this.addLog(
          "info",
          `更新任务 ${taskId} 状态为 ${this.getStatusText(status)}`
        );
      }
    },

    updateUavStatus(uavId, status) {
      const uavIndex = this.availableUAVs.findIndex((uav) => uav.id === uavId);
      if (uavIndex !== -1) {
        this.availableUAVs[uavIndex].status = status;
        this.$set(this.availableUAVs, uavIndex, this.availableUAVs[uavIndex]);
        this.addLog(
          "info",
          `更新无人机 ${
            this.availableUAVs[uavIndex].sn
          } 状态为 ${this.getUavStatusText(status)}`
        );
      }
    },

    getHospitalName(locationName) {
      if (locationName.includes("医院")) {
        return locationName;
      }
      return "南昌大学第一附属医院";
    },

    cancelDeploy() {
      this.selectedTask = null;
      this.selectedUav = null;
      this.fullscreenDialogVisible = false;
      this.deployForm = {
        uavId: null,
        fromLocation: "",
        toLocation: "",
        weight: 1.0,
      };
      if (this.$refs.deployForm) {
        this.$refs.deployForm.resetFields();
      }
    },

    async refreshTasks() {
      this.refreshing = true;
      try {
        await this.loadMedicalTasks();
        await this.loadAvailableUAVs();
        this.updateStats();
        this.addLog("info", "任务列表已刷新");
        this.$message.success("数据刷新成功");
      } catch (error) {
        console.error("刷新任务失败:", error);
        this.$message.error("刷新失败");
      } finally {
        this.refreshing = false;
      }
    },

    addLog(type, message) {
      this.logs.unshift({
        timestamp: new Date(),
        type: type,
        message: message,
      });

      if (this.logs.length > 100) {
        this.logs = this.logs.slice(0, 100);
      }
    },

    clearLogs() {
      this.logs = [];
      this.$message.info("操作日志已清空");
    },

    getLogTypeCount(type) {
      return this.logs.filter((log) => log.type === type).length;
    },

    formatTime(time) {
      if (!time) return "未知时间";
      const date = new Date(time);
      return date.toLocaleString("zh-CN");
    },

    truncateText(text, length) {
      if (!text) return "";
      if (text.length <= length) return text;
      return text.substring(0, length) + "...";
    },

    getSeverityClass(severity) {
      switch (severity) {
        case "CRITICAL":
          return "severity-critical";
        case "HIGH":
          return "severity-high";
        case "MEDIUM":
          return "severity-medium";
        case "NORMAL":
          return "severity-normal";
        default:
          return "";
      }
    },

    getSeverityTagType(severity) {
      switch (severity) {
        case "CRITICAL":
          return "danger";
        case "HIGH":
          return "warning";
        case "MEDIUM":
          return "info";
        case "NORMAL":
          return "success";
        default:
          return "info";
      }
    },

    getSeverityText(severity) {
      switch (severity) {
        case "CRITICAL":
          return "危急";
        case "HIGH":
          return "高";
        case "MEDIUM":
          return "中";
        case "NORMAL":
          return "普通";
        default:
          return severity;
      }
    },

    getStatusBadgeClass(status) {
      switch (status) {
        case "PENDING":
          return "status-pending";
        case "EXECUTING":
          return "status-executing";
        case "COMPLETED":
          return "status-completed";
        default:
          return "status-pending";
      }
    },

    getStatusText(status) {
      switch (status) {
        case "PENDING":
          return "待处理";
        case "EXECUTING":
          return "执行中";
        case "COMPLETED":
          return "已完成";
        default:
          return "未知";
      }
    },

    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
    },

    handleCurrentChange(val) {
      this.currentPage = val;
    },

    handleTimeOrderChange(val) {
      this.currentPage = 1;
    },

    handleSeverityFilterChange(val) {
      this.currentPage = 1;
    },

    handleStatusFilterChange(val) {
      this.currentPage = 1;
    },

    resetFilters() {
      this.timeOrder = "desc";
      this.severityFilter = [];
      this.statusFilter = [];
      this.currentPage = 1;
      this.$message.info("筛选条件已重置");
    },

    getMockMedicalTasks() {
      const mockTasks = [];
      const severityLevels = ["CRITICAL", "HIGH", "MEDIUM", "NORMAL"];
      const taskTypes = [
        "急救药品配送",
        "医疗器械运输",
        "血液样本转运",
        "医疗设备维修",
      ];
      const locations = [
        "八一广场",
        "南昌站",
        "南昌西站",
        "秋水广场",
        "红谷滩万达广场",
      ];
      const hospitals = [
        "南昌市第一医院",
        "南昌大学第一附属医院",
        "江西省人民医院",
      ];

      for (let i = 1; i <= 20; i++) {
        const severity =
          severityLevels[Math.floor(Math.random() * severityLevels.length)];
        const taskType =
          taskTypes[Math.floor(Math.random() * taskTypes.length)];
        const location =
          locations[Math.floor(Math.random() * locations.length)];
        const hospital =
          hospitals[Math.floor(Math.random() * hospitals.length)];

        const captureTime = new Date(
          Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000
        );

        mockTasks.push({
          id: 100 + i,
          taskName: `${taskType} - ${location}`,
          description: `${hospital}需要向${location}紧急配送${taskType}`,
          latitude: 28.68 + Math.random() * 0.02,
          longitude: 115.89 + Math.random() * 0.02,
          severityLevel: severity,
          responseTime:
            severity === "CRITICAL"
              ? Math.floor(Math.random() * 5) + 5
              : severity === "HIGH"
              ? Math.floor(Math.random() * 10) + 10
              : severity === "MEDIUM"
              ? Math.floor(Math.random() * 15) + 15
              : Math.floor(Math.random() * 20) + 20,
          captureTime: captureTime.toISOString(),
          fileCount: Math.floor(Math.random() * 5) + 1,
          systemType: "MEDICAL",
          status: i <= 5 ? "PENDING" : i <= 10 ? "EXECUTING" : "COMPLETED",
          droneId: i > 5 && i <= 10 ? `UAV-${100 + i}` : undefined,
        });
      }

      return mockTasks;
    },

    getMockUAVs() {
      const mockUAVs = [
        {
          id: 27,
          sn: "MED-UAV-735",
          model: "DJI Mavic 3",
          status: "IDLE",
          batteryLevel: 100,
          currentLng: 115.868863,
          currentLat: 28.597035,
          loadCapacity: 5,
          currentMission: null,
          createdTime: "2026-02-02 14:05:33",
          updatedTime: "2026-02-02 14:05:33",
        },
        {
          id: 20,
          sn: "DJI-MED-726",
          model: "DJI Mavic 3",
          status: "IDLE",
          batteryLevel: 85,
          currentLng: 115.842114,
          currentLat: 28.667088,
          loadCapacity: 5,
          currentMission: null,
          createdTime: "2026-01-31 22:52:35",
          updatedTime: "2026-01-31 22:52:35",
        },
        {
          id: 19,
          sn: "DJI-MED-296",
          model: "DJI Mavic 3",
          status: "CHARGING",
          batteryLevel: 65,
          currentLng: 115.842114,
          currentLat: 28.667088,
          loadCapacity: 5,
          currentMission: null,
          createdTime: "2026-01-31 22:49:31",
          updatedTime: "2026-01-31 22:49:31",
        },
        {
          id: 14,
          sn: "DJI-915",
          model: "DJI Mavic 3",
          status: "IDLE",
          batteryLevel: 45,
          currentLng: 115.857,
          currentLat: 28.683,
          loadCapacity: 5,
          currentMission: null,
          createdTime: "2025-12-22 15:51:29",
          updatedTime: "2025-12-22 15:51:29",
        },
      ];

      return mockUAVs;
    },
  },
};
</script>
  
<style scoped>
/* 基础页面样式 - 响应式修复版（保留原始视觉） */
.medical-rescue-page {
  padding: 20px;
  background: radial-gradient(ellipse at center, #081529 0%, #030d1f 100%);
  min-height: 100vh;          /* 允许内容撑开 */
  height: auto;               /* 移除固定高度 */
  color: #fff;
  font-family: "Orbitron", "Microsoft YaHei", sans-serif;
  overflow-y: auto;           /* 内容过多时滚动 */
  box-sizing: border-box;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;              /* flex收缩允许 */
  height: auto;               /* 自适应高度 */
}

/* 头部区域 - 响应式换行 */
.header-section {
  background: rgba(6, 30, 93, 0.6);
  border: 1px solid rgba(0, 255, 255, 0.4);
  border-radius: 14px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.2);
  flex-wrap: wrap;
  gap: 15px;
}

.header-title h1 {
  color: #00f0ff;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
  letter-spacing: 1px;
}

.header-title p {
  color: #b3d9ff;
  font-size: 14px;
  opacity: 0.9;
  text-shadow: 0 0 5px rgba(179, 217, 255, 0.3);
}

.action-btn {
  background: linear-gradient(90deg, #009dff, #00f0ff);
  border: none;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.6);
  color: #fff;
  font-weight: 600;
  letter-spacing: 0.5px;
  border-radius: 8px;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: linear-gradient(90deg, #0088e6, #00d9ff);
  box-shadow: 0 0 18px rgba(0, 240, 255, 0.8);
  transform: translateY(-2px);
}

/* 统计卡片 - 网格自动换行 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(6, 30, 93, 0.6);
  border: 1px solid rgba(0, 255, 255, 0.4);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 25px rgba(0, 240, 255, 0.4);
  border-color: rgba(0, 255, 255, 0.7);
}

.stat-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.1),
    transparent
  );
  transition: all 0.6s ease;
}

.stat-card:hover::before {
  left: 100%;
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
  filter: drop-shadow(0 0 8px currentColor);
  z-index: 1;
}

.stat-content {
  flex: 1;
  z-index: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #00f0ff;
  margin-bottom: 4px;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
  letter-spacing: 0.5px;
}

.stat-title {
  font-size: 13px;
  color: #d1e8ff;
  font-weight: 500;
  letter-spacing: 0.3px;
  text-shadow: 0 0 3px rgba(209, 232, 255, 0.3);
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  z-index: 1;
}

.stat-trend.up {
  color: #26fcd8;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
}

.stat-trend.down {
  color: #ff6b6b;
  text-shadow: 0 0 5px rgba(255, 107, 107, 0.5);
}

.stat-trend.stable {
  color: #ffb830;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
}

/* 内容布局 - 窄屏改为单列 */
.content-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  flex: 1;
  min-height: 0;
}

.left-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0;
  overflow: hidden;
}

.right-content {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

/* 面板通用样式 */
.panel {
  background: rgba(6, 30, 93, 0.6);
  border: 1px solid rgba(0, 255, 255, 0.4);
  border-radius: 14px;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.task-panel {
  flex: 1;
  min-height: 300px;
}

.logs-panel {
  height: 100%;
  min-height: 500px;
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  flex-wrap: wrap;            /* 窄屏换行 */
  justify-content: space-between;
  align-items: center;
  background: rgba(22, 42, 66, 0.8);
  flex-shrink: 0;
  gap: 10px;
}

.panel-title {
  font-size: 18px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: 0.5px;
}

.panel-subtitle {
  font-size: 14px;
  color: #b3d9ff;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  opacity: 0.9;
}

.panel-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.panel-content {
  padding: 20px;
  flex: 1;
  overflow-y: auto;
  position: relative;
}

.panel-content::-webkit-scrollbar {
  width: 6px;
}
.panel-content::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}
.panel-content::-webkit-scrollbar-thumb {
  background: rgba(0, 240, 255, 0.5);
  border-radius: 3px;
}
.panel-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 240, 255, 0.7);
}

.panel-footer {
  padding: 15px 20px;
  border-top: 1px solid rgba(0, 240, 255, 0.2);
  background: rgba(22, 42, 66, 0.8);
  flex-shrink: 0;
}

/* 任务控制区 - 响应式调整 */
.task-controls {
  background: rgba(6, 30, 93, 0.6);
  border: 1px solid rgba(0, 255, 255, 0.4);
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
  backdrop-filter: blur(10px);
}

.filter-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #b3d9ff;
  white-space: nowrap;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
}

.reset-btn {
  margin-left: auto;
  font-size: 14px;
  color: #00f0ff !important;
  text-shadow: 0 0 5px rgba(0, 240, 255, 0.5);
  background: transparent !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  padding: 5px 12px !important;
  border-radius: 6px !important;
  white-space: nowrap;
}

.reset-btn:hover {
  background: rgba(0, 240, 255, 0.1) !important;
  border-color: rgba(0, 240, 255, 0.6) !important;
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #b3d9ff;
  flex-wrap: wrap;
  gap: 10px;
}

/* 科技风格UI组件（保留原始所有样式） */
.tech-select ::v-deep .el-input__inner {
  background: rgba(0, 240, 255, 0.1) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  color: #00f0ff !important;
  border-radius: 6px !important;
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.2) inset;
}
.tech-select ::v-deep .el-input__inner::placeholder {
  color: rgba(0, 240, 255, 0.6) !important;
}
.tech-select ::v-deep .el-select-dropdown {
  background: rgba(6, 30, 93, 0.95) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  backdrop-filter: blur(10px);
}
.tech-btn {
  background: linear-gradient(
    90deg,
    rgba(0, 240, 255, 0.2),
    rgba(0, 157, 255, 0.2)
  ) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  color: #00f0ff !important;
  text-shadow: 0 0 5px rgba(0, 240, 255, 0.5);
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.3) !important;
  transition: all 0.3s ease !important;
  position: relative !important;
  z-index: 1000 !important;
}
.tech-btn:hover {
  background: linear-gradient(
    90deg,
    rgba(0, 240, 255, 0.3),
    rgba(0, 157, 255, 0.3)
  ) !important;
  border-color: rgba(0, 240, 255, 0.7) !important;
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.5) !important;
  transform: translateY(-2px);
}
.tech-btn:disabled {
  background: rgba(255, 255, 255, 0.1) !important;
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.5) !important;
  box-shadow: none !important;
  transform: none !important;
}
.tech-tag {
  background: rgba(0, 240, 255, 0.15) !important;
  border: 1px solid rgba(0, 240, 255, 0.3) !important;
  color: #00f0ff !important;
  text-shadow: 0 0 3px rgba(0, 240, 255, 0.5);
  border-radius: 10px !important;
}

/* 分页器完整样式修复 */
::v-deep .el-pagination .btn-prev,
::v-deep .el-pagination .btn-next,
::v-deep .el-pagination .el-pager li {
  background: rgba(0, 240, 255, 0.15) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  color: #00f0ff !important;
  border-radius: 6px !important;
  min-width: 32px !important;
  height: 32px !important;
  line-height: 32px !important;
  margin: 0 4px !important;
  text-align: center !important;
  transition: all 0.3s ease !important;
  text-shadow: 0 0 5px rgba(0, 240, 255, 0.5);
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.3) !important;
  font-size: 14px !important;
  font-weight: normal !important;
  padding: 0 !important;
}
::v-deep .el-pagination .btn-prev .el-icon,
::v-deep .el-pagination .btn-next .el-icon {
  color: #00f0ff !important;
  font-size: 14px !important;
  font-weight: bold !important;
  text-shadow: 0 0 5px rgba(0, 240, 255, 0.8);
}
::v-deep .el-pagination .btn-prev:hover,
::v-deep .el-pagination .btn-next:hover,
::v-deep .el-pagination .el-pager li:hover {
  background: rgba(0, 240, 255, 0.3) !important;
  border-color: rgba(0, 240, 255, 0.7) !important;
  color: #ffffff !important;
  box-shadow: 0 0 12px rgba(0, 240, 255, 0.6) !important;
  transform: translateY(-2px);
}
::v-deep .el-pagination .btn-prev:hover .el-icon,
::v-deep .el-pagination .btn-next:hover .el-icon {
  color: #ffffff !important;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}
::v-deep .el-pagination .el-pager li.active {
  background: rgba(0, 240, 255, 0.4) !important;
  border-color: rgba(0, 240, 255, 0.8) !important;
  color: #ffffff !important;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.9) !important;
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.8) !important;
  font-weight: bold !important;
}
::v-deep .el-pagination .btn-prev:disabled,
::v-deep .el-pagination .btn-next:disabled {
  background: rgba(255, 255, 255, 0.05) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: rgba(255, 255, 255, 0.3) !important;
  cursor: not-allowed !important;
  box-shadow: none !important;
  transform: none !important;
}
::v-deep .el-pagination__total,
::v-deep .el-pagination__jump {
  color: #b3d9ff !important;
  font-size: 13px !important;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
}
::v-deep .el-pagination__sizes .el-input .el-input__inner {
  background: rgba(0, 240, 255, 0.1) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  color: #00f0ff !important;
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.2) inset;
  border-radius: 6px !important;
  font-size: 13px !important;
  height: 32px !important;
  line-height: 32px !important;
}

.tech-input ::v-deep .el-input__inner {
  background: rgba(0, 240, 255, 0.1) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  color: #00f0ff !important;
  border-radius: 6px !important;
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.2) inset;
}
.tech-input ::v-deep .el-input__inner::placeholder {
  color: rgba(0, 240, 255, 0.6) !important;
}
.tech-input ::v-deep .el-input-group__append {
  background: rgba(0, 240, 255, 0.15) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  border-left: none !important;
}
.tech-slider ::v-deep .el-slider__runway {
  background: rgba(0, 240, 255, 0.1) !important;
  border: 1px solid rgba(0, 240, 255, 0.3) !important;
}
.tech-slider ::v-deep .el-slider__bar {
  background: linear-gradient(90deg, #00f0ff, #009dff) !important;
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.5) !important;
}
.tech-slider ::v-deep .el-slider__button {
  background: #00f0ff !important;
  border: 2px solid #fff !important;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.8) !important;
}
.tech-empty ::v-deep .el-empty__image {
  filter: drop-shadow(0 0 10px rgba(0, 240, 255, 0.5));
}
.tech-empty ::v-deep .el-empty__description p {
  color: #b3d9ff !important;
  text-shadow: 0 0 5px rgba(179, 217, 255, 0.3);
}

/* 任务网格 - 响应式卡片列数自动调整 */
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.task-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  backdrop-filter: blur(5px);
}
.task-card:hover:not(.task-disabled) {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 240, 255, 0.3);
  border-color: rgba(0, 240, 255, 0.6);
  background: rgba(0, 240, 255, 0.1);
}
.task-disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.task-disabled:hover {
  transform: none;
  box-shadow: none;
  border-color: rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
}
.task-badge {
  position: absolute;
  top: 15px;
  right: 15px;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: bold;
  letter-spacing: 0.5px;
  text-shadow: 0 0 3px currentColor;
  z-index: 2;
}
.status-pending {
  background: rgba(255, 184, 48, 0.2);
  color: #ffb830;
  border: 1px solid rgba(255, 184, 48, 0.4);
  box-shadow: 0 0 8px rgba(255, 184, 48, 0.3);
}
.status-executing {
  background: rgba(0, 240, 255, 0.2);
  color: #00f0ff;
  border: 1px solid rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
}
.status-completed {
  background: rgba(38, 252, 216, 0.2);
  color: #26fcd8;
  border: 1px solid rgba(38, 252, 216, 0.4);
  box-shadow: 0 0 8px rgba(38, 252, 216, 0.3);
}
.task-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-right: 70px;
  flex-wrap: wrap;
  gap: 8px;
}
.severity-tag {
  margin-right: 10px;
  font-weight: bold;
  letter-spacing: 0.5px;
  text-shadow: 0 0 3px currentColor;
}
.task-name {
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;      /* 保持原始效果，窄屏下通过媒体查询覆盖 */
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.3);
}
.task-description {
  color: #b3d9ff;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 12px;
  flex: 1;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.2);
}
.task-meta {
  margin-bottom: 12px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #a0d2ff;
  margin-bottom: 6px;
  flex-wrap: wrap;
  text-shadow: 0 0 2px rgba(160, 210, 255, 0.3);
}
.meta-row i {
  font-size: 14px;
  color: #00f0ff;
  width: 16px;
  text-shadow: 0 0 5px rgba(0, 240, 255, 0.5);
}
.response-time {
  color: #ff6b6b;
  font-weight: bold;
  text-shadow: 0 0 5px rgba(255, 107, 107, 0.5);
}
.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  flex-wrap: wrap;
  gap: 10px;
}
.task-info {
  display: flex;
  gap: 12px;
  font-size: 12px;
  flex-wrap: wrap;
}
.drone-info {
  color: #00f0ff;
  display: flex;
  align-items: center;
  gap: 4px;
  text-shadow: 0 0 3px rgba(0, 240, 255, 0.5);
}
.file-info {
  color: #b3d9ff;
  display: flex;
  align-items: center;
  gap: 4px;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
}
.deploy-btn {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
}
/* 严重程度边框 */
.severity-critical {
  border-left: 5px solid #ff6b6b;
  background: linear-gradient(
    90deg,
    rgba(255, 107, 107, 0.15),
    rgba(255, 107, 107, 0.05)
  );
}
.severity-high {
  border-left: 5px solid #ffb830;
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.15),
    rgba(255, 184, 48, 0.05)
  );
}
.severity-medium {
  border-left: 5px solid #00f0ff;
  background: linear-gradient(
    90deg,
    rgba(0, 240, 255, 0.15),
    rgba(0, 240, 255, 0.05)
  );
}
.severity-normal {
  border-left: 5px solid #26fcd8;
  background: linear-gradient(
    90deg,
    rgba(38, 252, 216, 0.15),
    rgba(38, 252, 216, 0.05)
  );
}

/* 选中的无人机信息 */
.selected-uav-info {
  background: rgba(0, 240, 255, 0.1);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px;
  padding: 15px;
  margin-top: 15px;
  backdrop-filter: blur(5px);
}
.selected-uav-info .info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}
.selected-uav-info .info-row:last-child {
  margin-bottom: 0;
}
.selected-uav-info .info-label {
  color: #b3d9ff;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
}
.selected-uav-info .info-value {
  color: #fff;
  font-weight: 600;
  text-shadow: 0 0 3px rgba(255, 255, 255, 0.3);
}
.selected-uav-info .info-value.highlight {
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
}

/* 没有可用无人机警告 */
.no-uav-warning {
  background: rgba(255, 184, 48, 0.15);
  border: 1px solid rgba(255, 184, 48, 0.4);
  border-radius: 6px;
  padding: 10px;
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffb830;
  font-size: 13px;
}
.no-uav-warning i {
  font-size: 16px;
}
.capacity-info {
  margin-left: 15px;
  color: #26fcd8;
  font-size: 13px;
  font-weight: 500;
  text-shadow: 0 0 3px rgba(38, 252, 216, 0.3);
}

/* 部署表单 */
.deploy-form {
  font-size: 14px;
}
.form-input,
.form-select,
.form-slider,
.form-number {
  width: 100%;
}
::v-deep .form-slider .el-slider__input {
  width: 80px !important;
}
.form-unit {
  margin-left: 10px;
  color: #b3d9ff;
  font-size: 14px;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
}
.status-warning {
  background: rgba(255, 184, 48, 0.15);
  border: 1px solid rgba(255, 184, 48, 0.4);
  border-radius: 8px;
  padding: 12px;
  margin: 15px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffb830;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.3);
}
.status-warning i {
  font-size: 18px;
  color: #ffb830;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
}

/* 操作日志 */
.logs-container {
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.empty-logs {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #b3d9ff;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(179, 217, 255, 0.3);
}
.empty-logs i {
  font-size: 48px;
  margin-bottom: 12px;
  color: rgba(0, 240, 255, 0.5);
  filter: drop-shadow(0 0 10px rgba(0, 240, 255, 0.3));
}
.empty-logs p {
  margin: 0;
}
.log-list {
  flex: 1;
  overflow-y: auto;
  padding-right: 5px;
}
.log-item {
  padding: 12px 15px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.03);
  border-left: 4px solid transparent;
  border-radius: 4px;
  margin-bottom: 6px;
}
.log-item:hover {
  background: rgba(0, 240, 255, 0.08);
  transform: translateX(5px);
  border-left-color: currentColor;
}
.log-success {
  border-left-color: #26fcd8;
}
.log-error {
  border-left-color: #ff6b6b;
}
.log-info {
  border-left-color: #00f0ff;
}
.log-time {
  min-width: 150px;
  color: #a0d2ff;
  font-size: 12px;
  font-family: "Monaco", "Menlo", "Ubuntu Mono", monospace;
  text-shadow: 0 0 3px rgba(160, 210, 255, 0.3);
}
.log-message {
  flex: 1;
  color: #fff;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
  text-shadow: 0 0 3px rgba(255, 255, 255, 0.2);
}
.log-type {
  min-width: 50px;
  padding: 3px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: bold;
  text-align: center;
  letter-spacing: 0.5px;
  text-shadow: 0 0 3px rgba(255, 255, 255, 0.5);
}
.type-success {
  background: rgba(38, 252, 216, 0.2);
  color: #26fcd8;
  border: 1px solid rgba(38, 252, 216, 0.4);
  box-shadow: 0 0 8px rgba(38, 252, 216, 0.3);
}
.type-error {
  background: rgba(255, 107, 107, 0.2);
  color: #ff6b6b;
  border: 1px solid rgba(255, 107, 107, 0.4);
  box-shadow: 0 0 8px rgba(255, 107, 107, 0.3);
}
.type-info {
  background: rgba(0, 240, 255, 0.2);
  color: #00f0ff;
  border: 1px solid rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
}
.log-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #b3d9ff;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
  flex-wrap: wrap;
  gap: 10px;
}
.log-type-count {
  display: flex;
  gap: 10px;
}
.log-type-count span {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: bold;
}

/* 加载对话框 */
.loading-dialog {
  backdrop-filter: blur(5px);
}
::v-deep .loading-dialog .el-dialog {
  background: rgba(6, 30, 93, 0.95) !important;
  border: 1px solid rgba(0, 240, 255, 0.4) !important;
  border-radius: 14px !important;
  box-shadow: 0 0 30px rgba(0, 240, 255, 0.3) !important;
  backdrop-filter: blur(10px) !important;
}
::v-deep .loading-dialog .el-dialog__body {
  padding: 40px !important;
  background: transparent !important;
}
.loading-content {
  text-align: center;
}
.loading-message {
  margin-top: 20px;
  color: #00f0ff;
  font-weight: bold;
  letter-spacing: 0.5px;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
}

/* 全屏无人机部署对话框样式 */
::v-deep .fullscreen-deploy-dialog {
  background: radial-gradient(
    ellipse at center,
    #081529 0%,
    #030d1f 100%
  ) !important;
  backdrop-filter: blur(10px) !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header {
  background: rgba(6, 30, 93, 0.8) !important;
  border-bottom: 1px solid rgba(0, 240, 255, 0.4) !important;
  padding: 20px !important;
  backdrop-filter: blur(10px) !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__title {
  color: #00f0ff !important;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.5);
  font-size: 18px !important;
  font-weight: bold !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__close {
  color: #00f0ff !important;
  font-size: 20px !important;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.8);
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__close:hover {
  color: #fff !important;
  text-shadow: 0 0 12px rgba(255, 255, 255, 0.9);
}
::v-deep .fullscreen-deploy-dialog .el-dialog__body {
  padding: 0 !important;
  height: calc(100vh - 60px) !important;
  overflow: auto !important;
  background: radial-gradient(
    ellipse at center,
    #081529 0%,
    #030d1f 100%
  ) !important;
}
.dialog-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
}
.dialog-title i {
  margin-right: 10px;
  font-size: 20px;
}
.fullscreen-deploy-content {
  padding: 30px;
  height: 100%;
  overflow-y: auto;
}
.fullscreen-form-sections {
  display: flex;
  flex-direction: column;
  gap: 30px;
  margin-bottom: 30px;
}
.form-section {
  background: rgba(6, 30, 93, 0.4);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 12px;
  padding: 25px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
}
.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #00f0ff;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 0 0 5px rgba(0, 240, 255, 0.5);
}
.section-title i {
  font-size: 18px;
  text-shadow: 0 0 8px currentColor;
}
.fullscreen-info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin: 30px 0;
}
.info-card {
  background: rgba(6, 30, 93, 0.4);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
  transition: all 0.3s ease;
}
.info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 25px rgba(0, 240, 255, 0.4);
  border-color: rgba(0, 240, 255, 0.6);
}
.info-card-title {
  font-size: 16px;
  font-weight: bold;
  color: #00f0ff;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 0 0 5px rgba(0, 240, 255, 0.5);
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.3);
}
.info-card-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  flex-wrap: wrap;
  gap: 8px;
}
.info-label {
  color: #b3d9ff;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
  font-weight: 500;
}
.info-value {
  color: #fff;
  font-weight: 600;
  text-shadow: 0 0 3px rgba(255, 255, 255, 0.3);
}
.info-value.highlight {
  color: #00f0ff;
  text-shadow: 0 0 8px rgba(0, 240, 255, 0.5);
}
.info-value.success {
  color: #26fcd8;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
}
.info-value.warning {
  color: #ffb830;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
}
.info-value.error {
  color: #ff6b6b;
  text-shadow: 0 0 5px rgba(255, 107, 107, 0.5);
}
.fullscreen-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: rgba(6, 30, 93, 0.4);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 12px;
  margin-top: 30px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.2);
  flex-wrap: wrap;
  gap: 15px;
}
.action-bar-left,
.action-bar-right {
  flex: 1;
}
.action-bar-right {
  display: flex;
  justify-content: flex-end;
}
.deploy-submit-btn {
  min-width: 250px;
  padding: 12px 30px !important;
  font-size: 16px !important;
  font-weight: bold !important;
}
::v-deep .el-select-dropdown__item {
  padding: 8px 20px !important;
  font-size: 14px !important;
}
::v-deep .el-select-dropdown__item.selected {
  background: rgba(0, 240, 255, 0.2) !important;
  color: #00f0ff !important;
}
::v-deep .el-select-group__title {
  background: rgba(22, 42, 66, 0.8) !important;
  color: #00f0ff !important;
  font-weight: bold !important;
  border-bottom: 1px solid rgba(0, 240, 255, 0.3) !important;
}
.empty-deploy-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 300px;
}

/* ========== 响应式设计（保留原始视觉，增加布局适配） ========== */
@media (max-width: 1200px) {
  .content-layout {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  .right-content {
    margin-top: 0;
  }
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  }
  .fullscreen-info-grid {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
}

@media (max-width: 992px) {
  .fullscreen-info-grid {
    grid-template-columns: 1fr;
  }
  .fullscreen-form-sections .el-row {
    flex-direction: column;
  }
  .fullscreen-form-sections .el-col {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .medical-rescue-page {
    padding: 15px;
  }
  .header-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
    padding: 20px;
  }
  .header-title h1 {
    font-size: 20px;
  }
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  .stat-card {
    padding: 15px;
  }
  .stat-value {
    font-size: 20px;
  }
  .task-grid {
    grid-template-columns: 1fr;
  }
  .filter-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .filter-item {
    width: 100%;
  }
  .reset-btn {
    margin-left: 0;
    align-self: flex-start;
  }
  .pagination-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .task-header {
    flex-direction: column;
    align-items: flex-start;
    padding-right: 0;
  }
  .severity-tag {
    margin-bottom: 8px;
  }
  .task-name {
    white-space: normal;      /* 窄屏下允许换行 */
  }
  .task-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .fullscreen-deploy-content {
    padding: 15px;
  }
  .form-section {
    padding: 15px;
  }
  .fullscreen-action-bar {
    flex-direction: column;
    gap: 15px;
  }
  .action-bar-left,
  .action-bar-right {
    width: 100%;
  }
  .deploy-submit-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .panel-actions {
    align-self: flex-end;
  }
  .log-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .log-time {
    min-width: auto;
  }
}

/* 关键修复：确保所有按钮可点击 */
::v-deep .el-button {
  position: relative !important;
  z-index: 1000 !important;
}
.task-card .task-actions {
  position: relative;
  z-index: 1000 !important;
}
.task-card:hover .task-actions .el-button {
  z-index: 1001 !important;
}
::v-deep .el-dialog .el-button {
  z-index: 2000 !important;
}
.task-card::before,
.task-card::after {
  pointer-events: none !important;
}
.el-button-group .el-button {
  z-index: 1001 !important;
}
.task-card * {
  pointer-events: auto !important;
}
</style>

