<template>
  <div class="power-inspection-page">
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 标题区域 -->
      <div class="header-section">
        <div class="header-title">
          <h1><i class="el-icon-lightning"></i> 电力无人机巡检系统</h1>
          <p>实时监控输电线路，智能调度无人机进行巡检维护</p>
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
                <span class="filter-label">故障等级：</span>
                <el-select
                  v-model="faultLevelFilter"
                  size="small"
                  placeholder="请选择故障等级"
                  multiple
                  collapse-tags
                  @change="handleFaultLevelFilterChange"
                  style="width: 180px"
                  class="tech-select"
                >
                  <el-option label="危急" value="CRITICAL"></el-option>
                  <el-option label="严重" value="SEVERE"></el-option>
                  <el-option label="一般" value="MODERATE"></el-option>
                  <el-option label="轻微" value="MINOR"></el-option>
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
                  <el-option label="巡检中" value="INSPECTING"></el-option>
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
                >共 {{ totalTasks }} 个巡检任务，当前显示
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

          <!-- 电力巡检任务列表 -->
          <div class="panel task-panel">
            <div class="panel-header">
              <div class="panel-title">
                <i class="el-icon-s-order"></i> 电力巡检任务列表
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
                description="暂无符合条件的电力巡检任务"
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
                    task.faultLevel ? getFaultLevelClass(task.faultLevel) : '',
                    getFaultLevelClass(task.faultLevel),
                    {
                      'task-disabled':
                        task.status === 'INSPECTING' ||
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
                      :type="getFaultLevelTagType(task.faultLevel)"
                      size="small"
                      class="fault-tag tech-tag"
                    >
                      {{ getFaultLevelText(task.faultLevel) }}
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
                        >预估巡检: {{ task.responseTime }}分钟</span
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
                        type="success"
                        size="mini"
                        @click.stop="viewTaskDetail(task)"
                        class="detail-btn tech-btn"
                      >
                        <i class="el-icon-view"></i>
                        查看详情
                      </el-button>
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
                          task.status === 'INSPECTING' ||
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

    <!-- 任务详情对话框 -->
    <el-dialog
      :visible.sync="detailDialogVisible"
      title="巡检任务详情"
      width="800px"
      :close-on-click-modal="false"
      custom-class="task-detail-dialog"
    >
      <template v-if="selectedTask">
        <div class="detail-content">
          <div class="detail-section">
            <h3><i class="el-icon-info"></i> 任务基本信息</h3>
            <div class="detail-grid">
              <div class="detail-row">
                <span class="detail-label">任务名称：</span>
                <span class="detail-value">{{ selectedTask.taskName }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">故障等级：</span>
                <el-tag
                  size="small"
                  :type="getFaultLevelTagType(selectedTask.faultLevel)"
                  class="tech-tag"
                >
                  {{ getFaultLevelText(selectedTask.faultLevel) }}
                </el-tag>
              </div>
              <div class="detail-row">
                <span class="detail-label">上报时间：</span>
                <span class="detail-value">{{
                  formatTime(selectedTask.captureTime)
                }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">经纬度：</span>
                <span class="detail-value">
                  {{
                    selectedTask.latitude
                      ? selectedTask.latitude.toFixed(4)
                      : "未知"
                  }},
                  {{
                    selectedTask.longitude
                      ? selectedTask.longitude.toFixed(4)
                      : "未知"
                  }}
                </span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h3><i class="el-icon-document"></i> 任务描述</h3>
            <div class="description-box">{{ selectedTask.description }}</div>
          </div>

          <div
            class="detail-section"
            v-if="selectedTask.files && selectedTask.files.length > 0"
          >
            <h3><i class="el-icon-picture"></i> 附件文件</h3>
            <div class="file-list">
              <div
                v-for="(file, index) in selectedTask.files"
                :key="index"
                class="file-item"
              >
                <i class="el-icon-document"></i>
                <span class="file-name">{{ file.name }}</span>
                <span class="file-size">({{ formatFileSize(file.size) }})</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h3><i class="el-icon-setting"></i> 巡检要求</h3>
            <div class="requirements">
              <div class="requirement-item">
                <i class="el-icon-time"></i>
                <span>预计巡检时间：{{ selectedTask.responseTime }}分钟</span>
              </div>
              <div class="requirement-item">
                <i class="el-icon-aim"></i>
                <span
                  >重点检查：{{
                    selectedTask.inspectionFocus ||
                    "线路连接处、绝缘子、杆塔基础"
                  }}</span
                >
              </div>
              <div class="requirement-item">
                <i class="el-icon-warning"></i>
                <span
                  >安全要求：{{
                    selectedTask.safetyRequirements ||
                    "保持安全距离，避免电磁干扰"
                  }}</span
                >
              </div>
            </div>
          </div>
        </div>
      </template>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
        <el-button
          type="primary"
          @click="startDeployFromDetail"
          :disabled="
            !selectedTask ||
            selectedTask.status === 'INSPECTING' ||
            selectedTask.status === 'COMPLETED' ||
            selectedTask.droneId ||
            availableUAVsCount === 0
          "
        >
          立即部署无人机
        </el-button>
      </span>
    </el-dialog>

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
          {{ selectedTask ? selectedTask.taskName : "电力巡检任务" }}
          <el-tag
            v-if="selectedTask"
            :type="getFaultLevelTagType(selectedTask.faultLevel)"
            size="small"
            style="margin-left: 10px"
            class="tech-tag"
          >
            {{ getFaultLevelText(selectedTask.faultLevel) }}
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
                    <span class="info-label">续航时间：</span>
                    <span class="info-value"
                      >{{ selectedUav.endurance || 45 }}分钟</span
                    >
                  </div>
                </div>
              </div>

              <div class="form-section">
                <div class="section-title">
                  <i class="el-icon-location"></i> 巡检路线配置
                </div>

                <el-row :gutter="30">
                  <el-col :span="12">
                    <el-form-item label="任务名称" prop="missionName" required>
                      <el-input
                        v-model="deployForm.missionName"
                        placeholder="请输入巡检任务名称"
                        class="form-input tech-input"
                        size="medium"
                      ></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item
                      label="巡检区域"
                      prop="inspectionArea"
                      required
                    >
                      <el-select
                        v-model="deployForm.inspectionArea"
                        placeholder="请选择巡检区域"
                        class="form-select tech-select"
                        size="medium"
                      >
                        <el-option
                          v-for="area in inspectionAreas"
                          :key="area.value"
                          :label="area.label"
                          :value="area.value"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>

                <!-- 起点终点显示 -->
                <el-form-item label="巡检路线" required>
                  <div class="route-display-container">
                    <div class="route-point">
                      <div class="point-label">
                        <i class="el-icon-location-outline"></i>
                        起点（无人机当前位置）
                      </div>
                      <div class="point-coordinates">
                        <span class="coord-label">经度:</span>
                        <span class="coord-value">{{
                          startPoint.lng.toFixed(6)
                        }}</span>
                        <span class="coord-label" style="margin-left: 15px"
                          >纬度:</span
                        >
                        <span class="coord-value">{{
                          startPoint.lat.toFixed(6)
                        }}</span>
                      </div>
                      <div class="point-info">
                        <el-tag size="small" type="info">自动获取</el-tag>
                      </div>
                    </div>

                    <div class="route-arrow">
                      <i class="el-icon-right"></i>
                    </div>

                    <div class="route-point">
                      <div class="point-label">
                        <i class="el-icon-location"></i> 终点（电力巡检点）
                      </div>
                      <div class="point-coordinates">
                        <span class="coord-label">经度:</span>
                        <span class="coord-value">{{
                          endPoint.lng.toFixed(6)
                        }}</span>
                        <span class="coord-label" style="margin-left: 15px"
                          >纬度:</span
                        >
                        <span class="coord-value">{{
                          endPoint.lat.toFixed(6)
                        }}</span>
                      </div>
                      <div class="point-info">
                        <el-tag size="small" type="success">任务位置</el-tag>
                      </div>
                    </div>
                  </div>

                  <div class="route-info">
                    <i class="el-icon-info"></i>
                    <span
                      >系统将自动规划从无人机当前位置到电力巡检点的直线飞行路线</span
                    >
                  </div>
                </el-form-item>
              </div>

              <div class="form-section">
                <div class="section-title">
                  <i class="el-icon-setting"></i> 巡检参数配置
                </div>
                <el-row :gutter="30">
                  <el-col :span="8">
                    <el-form-item label="飞行高度(m)" prop="altitude" required>
                      <el-input-number
                        v-model="deployForm.altitude"
                        :min="50"
                        :max="500"
                        :step="10"
                        controls-position="right"
                        class="form-number tech-input"
                        size="medium"
                      ></el-input-number>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="飞行速度(m/s)" prop="speed" required>
                      <el-input-number
                        v-model="deployForm.speed"
                        :min="1"
                        :max="20"
                        :step="0.5"
                        controls-position="right"
                        class="form-number tech-input"
                        size="medium"
                      ></el-input-number>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="悬停时间(s)" prop="hoverTime" required>
                      <el-input-number
                        v-model="deployForm.hoverTime"
                        :min="0"
                        :max="60"
                        :step="5"
                        controls-position="right"
                        class="form-number tech-input"
                        size="medium"
                      ></el-input-number>
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
                  <i class="el-icon-map-location"></i> 巡检路线信息
                </div>
                <div class="info-card-content">
                  <div class="info-row">
                    <span class="info-label">任务名称：</span>
                    <span class="info-value highlight">{{
                      deployForm.missionName || "未填写"
                    }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">巡检区域：</span>
                    <span class="info-value highlight">{{
                      deployForm.inspectionArea || "未选择"
                    }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">飞行高度：</span>
                    <span class="info-value">{{ deployForm.altitude }}米</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">飞行速度：</span>
                    <span class="info-value">{{ deployForm.speed }}米/秒</span>
                  </div>
                </div>
              </div>

              <div class="info-card">
                <div class="info-card-title">
                  <i class="el-icon-document"></i> 任务信息概览
                </div>
                <div class="info-card-content">
                  <div class="info-row">
                    <span class="info-label">故障等级：</span>
                    <el-tag
                      size="small"
                      :type="getFaultLevelTagType(selectedTask.faultLevel)"
                      class="tech-tag"
                    >
                      {{ getFaultLevelText(selectedTask.faultLevel) }}
                    </el-tag>
                  </div>
                  <div class="info-row">
                    <span class="info-label">预估时间：</span>
                    <span class="info-value warning"
                      >{{ calculateInspectionTime() }}分钟</span
                    >
                  </div>
                  <div class="info-row">
                    <span class="info-label">上报时间：</span>
                    <span class="info-value">{{
                      formatTime(selectedTask.captureTime)
                    }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">位置坐标：</span>
                    <span class="info-value">
                      {{
                        selectedTask.latitude
                          ? selectedTask.latitude.toFixed(4)
                          : "未知"
                      }},
                      {{
                        selectedTask.longitude
                          ? selectedTask.longitude.toFixed(4)
                          : "未知"
                      }}
                    </span>
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
                    <span class="info-label">续航检查：</span>
                    <span
                      class="info-value"
                      :class="getEnduranceCheckClass(selectedUav)"
                    >
                      {{ getEnduranceCheckText(selectedUav) }}
                    </span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">参数验证：</span>
                    <span class="info-value" :class="getParamCheckClass()">
                      {{ getParamCheckText() }}
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
                    !deployForm.missionName ||
                    !deployForm.inspectionArea ||
                    availableUAVs.length === 0
                  "
                  size="medium"
                  class="deploy-submit-btn tech-btn"
                >
                  <i class="el-icon-position"></i> 确认部署无人机并创建巡检任务
                </el-button>
              </div>
            </div>
          </el-form>
        </template>
        <div v-else class="empty-deploy-content">
          <el-empty description="未选择巡检任务" :image-size="100">
            <p style="color: #b3d9ff; margin-top: 10px">请先选择一个巡检任务</p>
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
  name: "PowerInspection",
  data() {
    return {
      // 电力巡检任务数据
      powerTasks: [],
      selectedTask: null,

      // 分页和筛选
      currentPage: 1,
      pageSize: 3,
      timeOrder: "desc",
      faultLevelFilter: [],
      statusFilter: [],

      // 可用无人机数据
      availableUAVs: [],
      selectedUav: null,
      uavOptions: [],

      // 巡检区域选项
      inspectionAreas: [
        { label: "35kV输电线路-西山段", value: "35kV_west_mountain" },
        { label: "110kV输电线路-主干线", value: "110kV_main_line" },
        { label: "220kV跨海输电线路", value: "220kV_sea_crossing" },
        { label: "10kV市区配电线路", value: "10kV_urban_distribution" },
        { label: "变电站设备巡检区", value: "substation_equipment" },
        { label: "山区输电线路", value: "mountain_transmission" },
        { label: "沿海输电线路", value: "coastal_transmission" },
        { label: "工业区配电网络", value: "industrial_distribution" },
      ],

      // 起点和终点
      startPoint: { lng: 115.9043, lat: 28.6718 },
      endPoint: { lng: 115.9043, lat: 28.6718 },

      // 部署表单
      deployForm: {
        uavId: null,
        missionName: "",
        inspectionArea: "",
        altitude: 100,
        speed: 8,
        hoverTime: 10,
        routePoints: [],
      },

      // 表单验证规则
      rules: {
        uavId: [{ required: true, message: "请选择无人机", trigger: "change" }],
        missionName: [
          { required: true, message: "请输入任务名称", trigger: "blur" },
        ],
        inspectionArea: [
          { required: true, message: "请选择巡检区域", trigger: "change" },
        ],
        altitude: [
          { required: true, message: "请输入飞行高度", trigger: "blur" },
        ],
        speed: [{ required: true, message: "请输入飞行速度", trigger: "blur" }],
      },

      // 统计卡片
      statCards: [
        {
          title: "总巡检任务",
          value: "0",
          icon: "el-icon-document",
          color: "#00f0ff",
          trend: "up",
          trendIcon: "el-icon-top",
          trendValue: "+0%",
        },
        {
          title: "危急故障",
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
          title: "巡检完成率",
          value: "0%",
          icon: "el-icon-success",
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

      // 对话框显示控制
      detailDialogVisible: false,
      fullscreenDialogVisible: false,

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

      // 定时器引用
      refreshInterval: null,
    };
  },
  computed: {
    filteredTasks() {
      let tasks = [...this.powerTasks];

      if (this.faultLevelFilter.length > 0) {
        tasks = tasks.filter((task) =>
          this.faultLevelFilter.includes(task.faultLevel)
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
      return this.powerTasks.length;
    },

    availableUAVsCount() {
      return this.availableUAVs.length;
    },
  },
  created() {
    this.loadAllData();
    this.refreshInterval = setInterval(() => {
      this.refreshTasks();
    }, 300000);

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
      if (task.status === "INSPECTING") return "巡检中";
      if (task.status === "COMPLETED") return "已完成";
      return "立即巡检";
    },

    // 获取无人机位置信息
    getUavLocation(uav) {
      if (!uav) return "未知位置";
      const lng = uav.currentLng ? uav.currentLng.toFixed(4) : "未知";
      const lat = uav.currentLat ? uav.currentLat.toFixed(4) : "未知";
      return `${lng}, ${lat}`;
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

    // 获取电池样式类
    getBatteryClass(batteryLevel) {
      if (batteryLevel >= 70) return "success";
      if (batteryLevel >= 40) return "warning";
      return "error";
    },

    // 获取续航检查样式类
    getEnduranceCheckClass(uav) {
      if (!uav) return "error";
      const estimatedTime = this.calculateInspectionTime();
      const endurance = uav.endurance || 45;
      return estimatedTime <= endurance * 0.8 ? "success" : "warning";
    },

    // 获取续航检查文本
    getEnduranceCheckText(uav) {
      if (!uav) return "未选择";
      const estimatedTime = this.calculateInspectionTime();
      const endurance = uav.endurance || 45;
      return estimatedTime <= endurance * 0.8 ? "✓ 充足" : "⚠ 注意";
    },

    // 获取参数检查样式类
    getParamCheckClass() {
      const valid =
        this.deployForm.altitude >= 50 &&
        this.deployForm.altitude <= 500 &&
        this.deployForm.speed >= 1 &&
        this.deployForm.speed <= 20;
      return valid ? "success" : "error";
    },

    // 获取参数检查文本
    getParamCheckText() {
      const valid =
        this.deployForm.altitude >= 50 &&
        this.deployForm.altitude <= 500 &&
        this.deployForm.speed >= 1 &&
        this.deployForm.speed <= 20;
      return valid ? "✓ 正常" : "✗ 异常";
    },

    // 设置起点和终点
    setRoutePoints() {
      if (this.selectedUav) {
        this.startPoint = {
          lng: this.selectedUav.currentLng || 115.9043,
          lat: this.selectedUav.currentLat || 28.6718,
        };
      }

      if (this.selectedTask) {
        this.endPoint = {
          lng: this.selectedTask.longitude || 115.9043,
          lat: this.selectedTask.latitude || 28.6718,
        };
      }
    },

    handleResize() {
      // 响应式处理
    },

    async loadAllData() {
      this.loading = true;
      this.loadProgress = 0;

      try {
        this.loadingMessage = "正在加载电力巡检任务数据...";
        await this.loadPowerTasks();
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

    async loadPowerTasks() {
      try {
        const response = await axios.get(
          "http://localhost:8080/api/reports/data/list",
          {
            params: {
              systemType: "POWER",
            },
          }
        );

        if (response.data && Array.isArray(response.data)) {
          this.powerTasks = response.data.map((task) => {
            let status = "PENDING";
            if (task.droneId) {
              status = "INSPECTING";
            }
            // 确保有faultLevel字段
            if (!task.faultLevel) {
              task.faultLevel = this.assignFaultLevel(task);
            }

            return {
              ...task,
              status: status,
            };
          });

          this.addLog(
            "success",
            `成功加载 ${this.powerTasks.length} 个电力巡检任务`
          );
        } else {
          this.powerTasks = this.getMockPowerTasks();
          this.addLog("info", "使用模拟电力巡检任务数据");
        }
      } catch (error) {
        console.error("加载电力巡检任务失败:", error);
        this.addLog("error", `加载电力巡检任务失败: ${error.message}`);
        this.powerTasks = this.getMockPowerTasks();
      }
    },

    assignFaultLevel(task) {
      // 根据任务描述或严重程度分配故障等级
      const desc = task.description || "";
      if (desc.includes("危急") || desc.includes("严重")) return "CRITICAL";
      if (desc.includes("一般")) return "MODERATE";
      if (desc.includes("轻微")) return "MINOR";
      return "SEVERE";
    },

    async loadAvailableUAVs() {
      try {
        // 改为使用统一的可用无人机接口
        const response = await axios.get(
          "http://localhost:8080/api/uavs/status/realtime"
        );

        if (response.data && response.data.success) {
          const allUavs = response.data.data || [];

          // 过滤出空闲或充电中的无人机，且电量大于20%
          this.availableUAVs = allUavs.filter((uav) => {
            const status = uav.status || uav.uavStatus;
            const batteryLevel = uav.batteryLevel || 0;
            return (
              (status === "IDLE" || status === "CHARGING") && batteryLevel >= 20
            );
          });

          // 按状态分组
          const idleUAVs = this.availableUAVs.filter(
            (uav) => uav.status === "IDLE"
          );
          const chargingUAVs = this.availableUAVs.filter(
            (uav) => uav.status === "CHARGING"
          );

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
        } else {
          throw new Error(response.data?.message || "加载失败");
        }
      } catch (error) {
        console.error("加载无人机数据失败:", error);
        this.addLog("error", `加载无人机数据失败: ${error.message}`);

        // 使用模拟数据
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
      const totalTasks = this.powerTasks.length;
      const criticalTasks = this.powerTasks.filter(
        (task) => task.faultLevel === "CRITICAL"
      ).length;

      const completedTasks = this.powerTasks.filter(
        (task) => task.status === "COMPLETED"
      ).length;
      const completionRate =
        totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

      this.statCards[0].value = totalTasks.toString();
      this.statCards[1].value = criticalTasks.toString();
      this.statCards[2].value = this.availableUAVsCount.toString();
      this.statCards[3].value = `${completionRate}%`;

      this.statCards[0].trendValue = `+${Math.floor(Math.random() * 15) + 5}%`;
      this.statCards[1].trendValue = `+${Math.floor(Math.random() * 3) + 1}`;
      this.statCards[3].trendValue = `+${Math.floor(Math.random() * 5) + 1}%`;
    },

    selectTask(task) {
      if (task.status === "INSPECTING" || task.status === "COMPLETED") {
        this.$message.warning("该任务已经部署或完成");
        return;
      }

      this.selectedTask = task;
      this.setRoutePoints();
      this.autoFillMissionInfo(task);
    },

    autoFillMissionInfo(task) {
      // 自动填充任务名称
      this.deployForm.missionName = `${
        task.taskName
      }_巡检_${new Date().getTime()}`;

      // 根据任务描述选择巡检区域
      const taskDesc = task.description || "";
      if (taskDesc.includes("西山")) {
        this.deployForm.inspectionArea = "35kV_west_mountain";
      } else if (taskDesc.includes("主干线")) {
        this.deployForm.inspectionArea = "110kV_main_line";
      } else if (taskDesc.includes("跨海")) {
        this.deployForm.inspectionArea = "220kV_sea_crossing";
      } else if (taskDesc.includes("市区")) {
        this.deployForm.inspectionArea = "10kV_urban_distribution";
      } else if (taskDesc.includes("变电站")) {
        this.deployForm.inspectionArea = "substation_equipment";
      } else {
        this.deployForm.inspectionArea = this.inspectionAreas[0].value;
      }
    },

    handleUavSelect(uavId) {
      this.selectedUav = this.availableUAVs.find((uav) => uav.id === uavId);
      this.setRoutePoints();
    },

    getUavLabel(uav) {
      return `${uav.sn} | ${uav.model} | ${this.getUavStatusText(uav)} | ${
        uav.batteryLevel
      }%`;
    },

    viewTaskDetail(task) {
      this.selectedTask = task;
      this.detailDialogVisible = true;
    },

    startDeployFromDetail() {
      this.detailDialogVisible = false;
      this.handleDeployForTask(this.selectedTask);
    },

    handleDeployForTask(task) {
      if (task.status === "INSPECTING" || task.status === "COMPLETED") {
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
        this.setRoutePoints();
      }
    },

    calculateInspectionTime() {
      // 简单计算：距离除以速度加上悬停时间
      const distance = this.calculateDistance(
        this.startPoint.lat,
        this.startPoint.lng,
        this.endPoint.lat,
        this.endPoint.lng
      );
      const flightTime = distance / (this.deployForm.speed * 60); // 分钟
      const hoverTime = this.deployForm.hoverTime / 60; // 分钟
      return Math.round((flightTime + hoverTime) * 10) / 10;
    },

    calculateDistance(lat1, lon1, lat2, lon2) {
      const R = 6371; // 地球半径，单位千米
      const dLat = this.toRad(lat2 - lat1);
      const dLon = this.toRad(lon2 - lon1);
      const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(this.toRad(lat1)) *
          Math.cos(this.toRad(lat2)) *
          Math.sin(dLon / 2) *
          Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c * 1000; // 返回米
    },

    toRad(deg) {
      return deg * (Math.PI / 180);
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

      if (this.selectedUav.batteryLevel < 20) {
        this.$message.warning("无人机电量过低，建议充电后使用");
        if (
          !(await this.$confirm("无人机电量低于20%，是否继续部署？", "提示", {
            confirmButtonText: "继续",
            cancelButtonText: "取消",
            type: "warning",
          }))
        ) {
          return;
        }
      }

      this.deployLoading = true;

      try {
        this.addLog("info", `开始部署无人机 ${this.selectedUav.sn}...`);

        // 准备路线点数据
        const routePoints = [
          {
            lat: this.startPoint.lat,
            lng: this.startPoint.lng,
            altitude: this.deployForm.altitude,
            name: "无人机当前位置",
            stopTime: 0,
          },
          {
            lat: this.endPoint.lat,
            lng: this.endPoint.lng,
            altitude: this.deployForm.altitude,
            name: this.selectedTask.taskName || "电力巡检点",
            stopTime: this.deployForm.hoverTime || 30,
          },
        ];

        // 创建电力巡航任务
        this.addLog("info", "开始创建电力巡检巡航任务...");

        const missionData = {
          missionName: this.deployForm.missionName,
          routePoints: routePoints,
          missionType: "CRUISE", // 使用正确的枚举值
          systemType: "POWER",
          inspectionDataId: this.selectedTask.id,
          severityLevel: this.selectedTask.faultLevel || "NORMAL",
          responseTime: this.deployForm.hoverTime || 30,
          droneSn: this.selectedUav.sn,
          assignedUavId: this.selectedUav.id,
        };

        console.log("创建电力巡航任务数据:", missionData);

        // 使用专门的电力任务接口
        const missionResponse = await axios.post(
          "http://localhost:8080/api/missions/power-cruise",
          missionData,
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );

        console.log("电力巡航任务响应:", missionResponse.data);

        if (missionResponse.data && missionResponse.data.success) {
          const mission = missionResponse.data.data;
          this.addLog(
            "success",
            `电力巡检巡航任务创建成功！任务ID: ${mission.id}`
          );

          // 直接更新本地任务状态
          this.updateTaskStatus(
            this.selectedTask.id,
            "EXECUTING",
            this.selectedUav.sn
          );

          // 同步巡检数据状态
          await this.syncInspectionDataStatus(
            this.selectedTask.id,
            "EXECUTING",
            this.selectedUav.sn
          );

          this.$message.success({
            message: `无人机部署和巡检任务创建成功！任务ID: ${mission.id}`,
            duration: 5000,
          });

          this.fullscreenDialogVisible = false;
          this.cancelDeploy();

          // 刷新数据
          setTimeout(() => {
            this.refreshTasks();
            this.loadAvailableUAVs();
          }, 1000);
        } else {
          const errorMsg =
            missionResponse.data?.message ||
            missionResponse.data?.error ||
            "任务创建失败";
          throw new Error(errorMsg);
        }
      } catch (error) {
        console.error("部署过程出错:", error);
        this.addLog("error", `部署过程出错: ${error.message}`);

        // 更友好的错误提示
        if (error.response) {
          this.$message.error(
            `请求失败 (${error.response.status}): ${
              error.response.data?.message || error.message
            }`
          );
        } else if (error.request) {
          this.$message.error("无法连接到服务器，请检查网络连接和后端服务");
        } else {
          this.$message.error(`操作失败: ${error.message}`);
        }
      } finally {
        this.deployLoading = false;
      }
    },

    // 更新任务状态时使用正确的枚举值
    updateTaskStatus(taskId, status, droneId = null) {
      const taskIndex = this.powerTasks.findIndex((task) => task.id === taskId);
      if (taskIndex !== -1) {
        // 转换为后端可接受的状态值
        let backendStatus;
        switch (status) {
          case "PENDING":
          case "待处理":
            backendStatus = "PENDING";
            break;
          case "INSPECTING":
          case "EXECUTING":
          case "执行中":
            backendStatus = "EXECUTING"; // 使用 EXECUTING 而不是 INSPECTING
            break;
          case "COMPLETED":
          case "已完成":
            backendStatus = "COMPLETED";
            break;
          default:
            backendStatus = "PENDING";
        }

        this.powerTasks[taskIndex].status = backendStatus;
        if (droneId) {
          this.powerTasks[taskIndex].droneId = droneId;
        }
        this.$set(this.powerTasks, taskIndex, this.powerTasks[taskIndex]);
        this.addLog("info", `更新任务 ${taskId} 状态为 ${backendStatus}`);
      }
    },

    // 同步巡检数据状态时也使用正确的值
    async syncInspectionDataStatus(taskId, status, droneId) {
      try {
        // 转换状态值
        let backendStatus;
        if (status === "INSPECTING") {
          backendStatus = "EXECUTING";
        } else {
          backendStatus = status;
        }

        const response = await axios.post(
          `http://localhost:8080/api/inspection-data/${taskId}/status`,
          {
            status: backendStatus, // 使用转换后的状态
            droneId: droneId,
            updatedAt: new Date().toISOString(),
          }
        );

        if (response.data && response.data.success) {
          this.addLog("success", `巡检数据状态同步成功`);
          return true;
        }
      } catch (error) {
        console.error("同步状态失败:", error);
        this.addLog("error", `同步状态失败: ${error.message}`);
        return false;
      }
    },
    cancelDeploy() {
      this.selectedTask = null;
      this.selectedUav = null;
      this.fullscreenDialogVisible = false;
      this.deployForm = {
        uavId: null,
        missionName: "",
        inspectionArea: "",
        altitude: 100,
        speed: 8,
        hoverTime: 10,
        routePoints: [],
      };
      this.startPoint = { lng: 115.9043, lat: 28.6718 };
      this.endPoint = { lng: 115.9043, lat: 28.6718 };
      if (this.$refs.deployForm) {
        this.$refs.deployForm.resetFields();
      }
    },

    async refreshTasks() {
      this.refreshing = true;
      try {
        await this.loadPowerTasks();
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

    formatFileSize(size) {
      if (!size) return "0 B";
      const units = ["B", "KB", "MB", "GB"];
      let index = 0;
      while (size >= 1024 && index < units.length - 1) {
        size /= 1024;
        index++;
      }
      return `${size.toFixed(1)} ${units[index]}`;
    },

    truncateText(text, length) {
      if (!text) return "";
      if (text.length <= length) return text;
      return text.substring(0, length) + "...";
    },

    getFaultLevelClass(faultLevel) {
      switch (faultLevel) {
        case "CRITICAL":
          return "fault-critical";
        case "SEVERE":
          return "fault-severe";
        case "MODERATE":
          return "fault-moderate";
        case "MINOR":
          return "fault-minor";
        default:
          return "";
      }
    },

    getFaultLevelTagType(faultLevel) {
      switch (faultLevel) {
        case "CRITICAL":
          return "danger";
        case "SEVERE":
          return "warning";
        case "MODERATE":
          return "info";
        case "MINOR":
          return "success";
        default:
          return "info";
      }
    },

    getFaultLevelText(faultLevel) {
      switch (faultLevel) {
        case "CRITICAL":
          return "危急";
        case "SEVERE":
          return "严重";
        case "MODERATE":
          return "一般";
        case "MINOR":
          return "轻微";
        default:
          return faultLevel;
      }
    },

    getStatusBadgeClass(status) {
      switch (status) {
        case "PENDING":
          return "status-pending";
        case "INSPECTING":
          return "status-inspecting";
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
        case "INSPECTING":
          return "巡检中";
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

    handleFaultLevelFilterChange(val) {
      this.currentPage = 1;
    },

    handleStatusFilterChange(val) {
      this.currentPage = 1;
    },

    resetFilters() {
      this.timeOrder = "desc";
      this.faultLevelFilter = [];
      this.statusFilter = [];
      this.currentPage = 1;
      this.$message.info("筛选条件已重置");
    },

    getMockPowerTasks() {
      const mockTasks = [];
      const faultLevels = ["CRITICAL", "SEVERE", "MODERATE", "MINOR"];
      const taskTypes = [
        "输电线路巡检",
        "变电站设备检查",
        "杆塔绝缘子检测",
        "线路连接处测温",
        "避雷器状态检查",
        "线路弧垂测量",
        "通道树障清理",
        "金具腐蚀检查",
      ];
      const locations = [
        "35kV输电线路-西山段",
        "110kV输电线路-主干线",
        "220kV跨海输电线路",
        "10kV市区配电线路",
        "110kV变电站主变区",
        "35kV开关站设备区",
        "山区输电线路",
        "沿海输电线路",
      ];

      for (let i = 1; i <= 20; i++) {
        const faultLevel =
          faultLevels[Math.floor(Math.random() * faultLevels.length)];
        const taskType =
          taskTypes[Math.floor(Math.random() * taskTypes.length)];
        const location =
          locations[Math.floor(Math.random() * locations.length)];

        const captureTime = new Date(
          Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000
        );

        mockTasks.push({
          id: 200 + i,
          taskName: `${taskType} - ${location}`,
          description: `${location}发现${
            faultLevel === "CRITICAL" ? "危急" : "一般"
          }故障，需要立即巡检`,
          latitude: 28.68 + Math.random() * 0.02,
          longitude: 115.89 + Math.random() * 0.02,
          faultLevel: faultLevel,
          responseTime: Math.floor(Math.random() * 30) + 15, // 15-45分钟
          captureTime: captureTime.toISOString(),
          fileCount: Math.floor(Math.random() * 5) + 1,
          systemType: "POWER",
          status: i <= 5 ? "PENDING" : i <= 10 ? "INSPECTING" : "COMPLETED",
          droneId: i > 5 && i <= 10 ? `POWER-UAV-${200 + i}` : undefined,
          files: [
            {
              name: `故障照片_${i}.jpg`,
              size: 1024 * 1024 * (Math.random() * 2 + 1),
            },
            {
              name: `检测报告_${i}.pdf`,
              size: 1024 * 1024 * (Math.random() * 0.5 + 0.1),
            },
          ],
          inspectionFocus:
            faultLevel === "CRITICAL"
              ? "线路连接处、绝缘子、杆塔基础"
              : "常规巡检",
          safetyRequirements: "保持安全距离，避免电磁干扰",
        });
      }

      return mockTasks;
    },

    getMockUAVs() {
      const mockUAVs = [
        {
          id: 28,
          sn: "POWER-UAV-836",
          model: "DJI Matrice 300 RTK",
          status: "IDLE",
          batteryLevel: 95,
          currentLng: 115.878863,
          currentLat: 28.627035,
          loadCapacity: 8,
          endurance: 55,
          currentMission: null,
          createdTime: "2026-02-02 14:10:33",
          updatedTime: "2026-02-02 14:10:33",
        },
        {
          id: 21,
          sn: "DJI-POWER-827",
          model: "DJI Matrice 300 RTK",
          status: "IDLE",
          batteryLevel: 80,
          currentLng: 115.862114,
          currentLat: 28.677088,
          loadCapacity: 8,
          endurance: 55,
          currentMission: null,
          createdTime: "2026-01-31 22:55:35",
          updatedTime: "2026-01-31 22:55:35",
        },
        {
          id: 18,
          sn: "DJI-POWER-397",
          model: "DJI Matrice 300 RTK",
          status: "CHARGING",
          batteryLevel: 60,
          currentLng: 115.862114,
          currentLat: 28.677088,
          loadCapacity: 8,
          endurance: 55,
          currentMission: null,
          createdTime: "2026-01-31 22:48:31",
          updatedTime: "2026-01-31 22:48:31",
        },
        {
          id: 15,
          sn: "DJI-1016",
          model: "DJI Matrice 300 RTK",
          status: "IDLE",
          batteryLevel: 40,
          currentLng: 115.867,
          currentLat: 28.673,
          loadCapacity: 8,
          endurance: 55,
          currentMission: null,
          createdTime: "2025-12-22 15:52:29",
          updatedTime: "2025-12-22 15:52:29",
        },
      ];

      return mockUAVs;
    },
  },
};
</script>
<style scoped>
/* 基础页面样式 - 响应式修复版（保留原始视觉） */
.power-inspection-page {
  padding: 20px;
  background: radial-gradient(ellipse at center, #081529 0%, #030d1f 100%);
  min-height: 100vh; /* 允许内容撑开，不固定高度 */
  height: auto; /* 移除固定高度 */
  color: #fff;
  font-family: "Orbitron", "Microsoft YaHei", sans-serif;
  overflow-y: auto; /* 内容过多时滚动 */
  box-sizing: border-box;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  min-height: 0; /* flex 收缩允许 */
  height: auto; /* 自适应高度 */
}

/* 头部区域 - 响应式换行 */
.header-section {
  background: rgba(6, 30, 93, 0.6);
  border: 1px solid rgba(255, 184, 48, 0.4);
  border-radius: 14px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(255, 184, 48, 0.2);
  flex-wrap: wrap; /* 窄屏换行 */
  gap: 15px;
}

.header-title h1 {
  color: #ffb830;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(255, 184, 48, 0.5);
  letter-spacing: 1px;
}

.header-title p {
  color: #b3d9ff;
  font-size: 14px;
  opacity: 0.9;
  text-shadow: 0 0 5px rgba(179, 217, 255, 0.3);
}

/* 行动按钮（保留原始样式） */
.action-btn {
  background: linear-gradient(90deg, #ffb830, #ffd166) !important;
  border: none !important;
  box-shadow: 0 0 12px rgba(255, 184, 48, 0.6) !important;
  color: #333 !important;
  font-weight: 600;
  letter-spacing: 0.5px;
  border-radius: 8px;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}
.action-btn:hover {
  background: linear-gradient(90deg, #e6a324, #ffc145) !important;
  box-shadow: 0 0 18px rgba(255, 184, 48, 0.8) !important;
  transform: translateY(-2px);
}

/* 统计卡片 - 响应式网格自适应 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(6, 30, 93, 0.6);
  border: 1px solid rgba(255, 184, 48, 0.4);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(255, 184, 48, 0.2);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 25px rgba(255, 184, 48, 0.4);
  border-color: rgba(255, 184, 48, 0.7);
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
  color: #ffb830;
  margin-bottom: 4px;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
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

/* 内容布局 - 左右两列，窄屏改为单列 */
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
  border: 1px solid rgba(255, 184, 48, 0.4);
  border-radius: 14px;
  box-shadow: 0 0 20px rgba(255, 184, 48, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.task-panel {
  flex: 1;
  min-height: 300px; /* 保证最小高度，防止压缩 */
}

.logs-panel {
  height: 100%;
  min-height: 500px;
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 184, 48, 0.2);
  display: flex;
  flex-wrap: wrap; /* 窄屏换行 */
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 184, 48, 0.1);
  flex-shrink: 0;
  gap: 10px;
}

.panel-title {
  font-size: 18px;
  font-weight: bold;
  color: #ffb830;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
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
  background: rgba(255, 184, 48, 0.5);
  border-radius: 3px;
}
.panel-content::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 184, 48, 0.7);
}

.panel-footer {
  padding: 15px 20px;
  border-top: 1px solid rgba(255, 184, 48, 0.2);
  background: rgba(255, 184, 48, 0.1);
  flex-shrink: 0;
}

/* 任务控制区 - 响应式调整 */
.task-controls {
  background: rgba(6, 30, 93, 0.6);
  border: 1px solid rgba(255, 184, 48, 0.4);
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 0 15px rgba(255, 184, 48, 0.2);
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
  flex-wrap: wrap;
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
  color: #ffb830 !important;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
  background: transparent !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  padding: 5px 12px !important;
  border-radius: 6px !important;
  white-space: nowrap;
}

.reset-btn:hover {
  background: rgba(255, 184, 48, 0.1) !important;
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

/* 科技风格UI组件（保留原始样式） */
.tech-select ::v-deep .el-input__inner {
  background: rgba(255, 184, 48, 0.1) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
  border-radius: 6px !important;
  box-shadow: 0 0 8px rgba(255, 184, 48, 0.2) inset;
}
.tech-select ::v-deep .el-input__inner::placeholder {
  color: rgba(255, 184, 48, 0.6) !important;
}
.tech-select ::v-deep .el-select-dropdown {
  background: rgba(6, 30, 93, 0.95) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  backdrop-filter: blur(10px);
}
.tech-btn {
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.2),
    rgba(255, 157, 0, 0.2)
  ) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
  box-shadow: 0 0 10px rgba(255, 184, 48, 0.3) !important;
  transition: all 0.3s ease !important;
  position: relative !important;
  z-index: 1000 !important;
}
.tech-btn:hover {
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.3),
    rgba(255, 157, 0, 0.3)
  ) !important;
  border-color: rgba(255, 184, 48, 0.7) !important;
  box-shadow: 0 0 15px rgba(255, 184, 48, 0.5) !important;
  transform: translateY(-2px);
}
.tech-btn:disabled {
  background: rgba(255, 255, 255, 0.05) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
  color: rgba(255, 255, 255, 0.5) !important;
  box-shadow: none !important;
  transform: none !important;
}
.tech-tag {
  background: rgba(255, 184, 48, 0.15) !important;
  border: 1px solid rgba(255, 184, 48, 0.3) !important;
  color: #ffb830 !important;
  text-shadow: 0 0 3px rgba(255, 184, 48, 0.5);
  border-radius: 10px !important;
}

/* 分页器（保留原始样式） */
::v-deep .el-pagination .btn-prev,
::v-deep .el-pagination .btn-next,
::v-deep .el-pagination .el-pager li {
  background: rgba(255, 184, 48, 0.15) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
  border-radius: 6px !important;
  min-width: 32px !important;
  height: 32px !important;
  line-height: 32px !important;
  margin: 0 4px !important;
  text-align: center !important;
  transition: all 0.3s ease !important;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
  box-shadow: 0 0 8px rgba(255, 184, 48, 0.3) !important;
  font-size: 14px !important;
  font-weight: normal !important;
  padding: 0 !important;
}
::v-deep .el-pagination .btn-prev .el-icon,
::v-deep .el-pagination .btn-next .el-icon {
  color: #ffb830 !important;
  font-size: 14px !important;
  font-weight: bold !important;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.8);
}
::v-deep .el-pagination .btn-prev:hover,
::v-deep .el-pagination .btn-next:hover,
::v-deep .el-pagination .el-pager li:hover {
  background: rgba(255, 184, 48, 0.3) !important;
  border-color: rgba(255, 184, 48, 0.7) !important;
  color: #ffffff !important;
  box-shadow: 0 0 12px rgba(255, 184, 48, 0.6) !important;
  transform: translateY(-2px);
}
::v-deep .el-pagination .btn-prev:hover .el-icon,
::v-deep .el-pagination .btn-next:hover .el-icon {
  color: #ffffff !important;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}
::v-deep .el-pagination .el-pager li.active {
  background: rgba(255, 184, 48, 0.4) !important;
  border-color: rgba(255, 184, 48, 0.8) !important;
  color: #ffffff !important;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.9) !important;
  box-shadow: 0 0 15px rgba(255, 184, 48, 0.8) !important;
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
  background: rgba(255, 184, 48, 0.1) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
  box-shadow: 0 0 8px rgba(255, 184, 48, 0.2) inset;
  border-radius: 6px !important;
  font-size: 13px !important;
  height: 32px !important;
  line-height: 32px !important;
}

.tech-input ::v-deep .el-input__inner {
  background: rgba(255, 184, 48, 0.1) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
  border-radius: 6px !important;
  box-shadow: 0 0 8px rgba(255, 184, 48, 0.2) inset;
}
.tech-input ::v-deep .el-input__inner::placeholder {
  color: rgba(255, 184, 48, 0.6) !important;
}
.tech-input ::v-deep .el-input-group__append {
  background: rgba(255, 184, 48, 0.15) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  border-left: none !important;
}
.tech-empty ::v-deep .el-empty__image {
  filter: drop-shadow(0 0 10px rgba(255, 184, 48, 0.5));
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
  box-shadow: 0 10px 25px rgba(255, 184, 48, 0.3);
  border-color: rgba(255, 184, 48, 0.6);
  background: rgba(255, 184, 48, 0.1);
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
.status-inspecting {
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
.fault-tag {
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
  white-space: nowrap; /* 保留原始，窄屏时通过媒体查询换行 */
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
  color: #ffb830;
  width: 16px;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
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
  color: #ffb830;
  display: flex;
  align-items: center;
  gap: 4px;
  text-shadow: 0 0 3px rgba(255, 184, 48, 0.5);
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
  z-index: 11 !important;
  pointer-events: auto !important;
  position: relative !important;
}
.detail-btn {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.2),
    rgba(255, 184, 48, 0.1)
  ) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
  margin-right: 8px;
  z-index: 11 !important;
  pointer-events: auto !important;
  position: relative !important;
}
.detail-btn:hover {
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.3),
    rgba(255, 184, 48, 0.2)
  ) !important;
}

/* 故障等级边框（保留原始） */
.fault-critical {
  border-left: 5px solid #ff6b6b;
  background: linear-gradient(
    90deg,
    rgba(255, 107, 107, 0.15),
    rgba(255, 107, 107, 0.05)
  );
}
.fault-severe {
  border-left: 5px solid #ffb830;
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.15),
    rgba(255, 184, 48, 0.05)
  );
}
.fault-moderate {
  border-left: 5px solid #00f0ff;
  background: linear-gradient(
    90deg,
    rgba(0, 240, 255, 0.15),
    rgba(0, 240, 255, 0.05)
  );
}
.fault-minor {
  border-left: 5px solid #26fcd8;
  background: linear-gradient(
    90deg,
    rgba(38, 252, 216, 0.15),
    rgba(38, 252, 216, 0.05)
  );
}

/* 选中的无人机信息 */
.selected-uav-info {
  background: rgba(255, 184, 48, 0.1);
  border: 1px solid rgba(255, 184, 48, 0.3);
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
  color: #ffb830;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
}

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
.form-number {
  width: 100%;
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
  color: rgba(255, 184, 48, 0.5);
  filter: drop-shadow(0 0 10px rgba(255, 184, 48, 0.3));
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
  border-bottom: 1px solid rgba(255, 184, 48, 0.2);
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
  background: rgba(255, 184, 48, 0.08);
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
  border-left-color: #ffb830;
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
  background: rgba(255, 184, 48, 0.2);
  color: #ffb830;
  border: 1px solid rgba(255, 184, 48, 0.4);
  box-shadow: 0 0 8px rgba(255, 184, 48, 0.3);
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
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  border-radius: 14px !important;
  box-shadow: 0 0 30px rgba(255, 184, 48, 0.3) !important;
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
  color: #ffb830;
  font-weight: bold;
  letter-spacing: 0.5px;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
}

/* 路线显示样式 - 窄屏纵向 */
.route-display-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  flex-wrap: wrap;
}
.route-point {
  flex: 1;
  background: rgba(6, 30, 93, 0.4);
  border: 1px solid rgba(255, 184, 48, 0.3);
  border-radius: 10px;
  padding: 15px;
  min-height: 80px;
  min-width: 200px;
}
.point-label {
  color: #ffb830;
  font-weight: 600;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.point-coordinates {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 8px;
}
.coord-label {
  color: #b3d9ff;
  font-size: 13px;
}
.coord-value {
  color: #fff;
  font-weight: 600;
  font-family: monospace;
  font-size: 13px;
}
.point-info {
  margin-top: 5px;
}
.route-arrow {
  margin: 0 20px;
  color: #ffb830;
  font-size: 24px;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
}
.route-info {
  margin-top: 15px;
  background: rgba(0, 240, 255, 0.1);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px;
  padding: 12px;
  color: #00f0ff;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 详情对话框样式（保留原始） */
::v-deep .task-detail-dialog .el-dialog {
  background: rgba(6, 30, 93, 0.95) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  border-radius: 14px !important;
  box-shadow: 0 0 30px rgba(255, 184, 48, 0.3) !important;
  backdrop-filter: blur(10px) !important;
}
::v-deep .task-detail-dialog .el-dialog__header {
  background: rgba(6, 30, 93, 0.8) !important;
  border-bottom: 1px solid rgba(255, 184, 48, 0.4) !important;
}
::v-deep .task-detail-dialog .el-dialog__title {
  color: #ffb830 !important;
  text-shadow: 0 0 10px rgba(255, 184, 48, 0.5);
}
::v-deep .task-detail-dialog .el-dialog__footer {
  background: rgba(6, 30, 93, 0.9) !important;
  border-top: 1px solid rgba(255, 184, 48, 0.3) !important;
}
::v-deep .task-detail-dialog .el-button {
  background: rgba(6, 30, 93, 0.8) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
}
::v-deep .task-detail-dialog .el-button:hover {
  background: rgba(6, 30, 93, 1) !important;
  border-color: rgba(255, 184, 48, 0.7) !important;
}
::v-deep .task-detail-dialog .el-button--primary {
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.2),
    rgba(255, 157, 0, 0.2)
  ) !important;
}
::v-deep .task-detail-dialog .el-button--primary:hover {
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.3),
    rgba(255, 157, 0, 0.3)
  ) !important;
}
.detail-content {
  background: rgba(3, 15, 31, 0.8);
  border-radius: 8px;
  padding: 20px;
}
.detail-section {
  margin-bottom: 25px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 184, 48, 0.3);
  border-radius: 8px;
  padding: 20px;
}
.detail-section h3 {
  color: #ffb830;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(255, 184, 48, 0.5);
  text-shadow: 0 0 10px rgba(255, 184, 48, 0.5);
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}
.detail-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  background: rgba(6, 30, 93, 0.4);
  border-radius: 6px;
  border: 1px solid rgba(255, 184, 48, 0.2);
}
.detail-label {
  color: #8ab4f8;
  min-width: 80px;
  font-weight: 500;
  text-shadow: 0 0 3px rgba(138, 180, 248, 0.3);
}
.detail-value {
  color: #fff;
  font-weight: 600;
}
.description-box {
  background: rgba(6, 30, 93, 0.4);
  border: 1px solid rgba(255, 184, 48, 0.3);
  border-radius: 8px;
  padding: 20px;
  line-height: 1.6;
  color: #b3d9ff;
  min-height: 80px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.3);
}
.file-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.file-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 15px;
  background: rgba(6, 30, 93, 0.4);
  border-radius: 6px;
  border: 1px solid rgba(0, 240, 255, 0.3);
  transition: all 0.3s ease;
}
.file-name {
  color: #fff;
  flex: 1;
  font-weight: 500;
}
.file-size {
  color: #8a9baf;
  font-size: 12px;
}
.requirements {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.file-item:hover {
  background: rgba(6, 30, 93, 0.6);
  transform: translateX(5px);
  border-color: rgba(255, 184, 48, 0.5);
}
.requirement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #b3d9ff;
  padding: 8px 12px;
  background: rgba(6, 30, 93, 0.4);
  border-radius: 6px;
  border-left: 3px solid #ffb830;
  transition: all 0.3s ease;
}
.requirement-item:hover {
  background: rgba(6, 30, 93, 0.6);
  transform: translateX(5px);
}
.requirement-item i {
  color: #ffb830;
  font-size: 16px;
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
  border-bottom: 1px solid rgba(255, 184, 48, 0.4) !important;
  padding: 20px !important;
  backdrop-filter: blur(10px) !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__title {
  color: #ffb830 !important;
  text-shadow: 0 0 10px rgba(255, 184, 48, 0.5);
  font-size: 18px !important;
  font-weight: bold !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__close {
  color: #ffb830 !important;
  font-size: 20px !important;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.8);
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__close:hover {
  background: rgba(6, 30, 93, 0.9) !important;
  border-bottom: 1px solid rgba(255, 184, 48, 0.4) !important;
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
  color: #ffb830;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
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
  border: 1px solid rgba(255, 184, 48, 0.3);
  border-radius: 12px;
  padding: 25px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(255, 184, 48, 0.2);
}
.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #ffb830;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
}
.section-title i {
  font-size: 18px;
  text-shadow: 0 0 8px currentColor;
}
.fullscreen-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin: 30px 0;
}
.info-card {
  background: rgba(6, 30, 93, 0.4);
  border: 1px solid rgba(255, 184, 48, 0.3);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(255, 184, 48, 0.2);
  transition: all 0.3s ease;
}
.info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 25px rgba(255, 184, 48, 0.4);
  border-color: rgba(255, 184, 48, 0.6);
}
.info-card-title {
  font-size: 16px;
  font-weight: bold;
  color: #ffb830;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(255, 184, 48, 0.3);
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
  color: #ffb830;
  text-shadow: 0 0 8px rgba(255, 184, 48, 0.5);
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
  border: 1px solid rgba(255, 184, 48, 0.3);
  border-radius: 12px;
  margin-top: 30px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(255, 184, 48, 0.2);
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
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.2),
    rgba(255, 157, 0, 0.2)
  ) !important;
  border: 1px solid rgba(255, 184, 48, 0.4) !important;
  color: #ffb830 !important;
}
.deploy-submit-btn:hover:not(:disabled) {
  background: linear-gradient(
    90deg,
    rgba(255, 184, 48, 0.3),
    rgba(255, 157, 0, 0.3)
  ) !important;
  border-color: rgba(255, 184, 48, 0.7) !important;
}
::v-deep .el-select-dropdown__item {
  padding: 8px 20px !important;
  font-size: 14px !important;
}
::v-deep .el-select-dropdown__item.selected {
  background: rgba(255, 184, 48, 0.2) !important;
  color: #ffb830 !important;
}
::v-deep .el-select-group__title {
  background: rgba(22, 42, 66, 0.8) !important;
  color: #ffb830 !important;
  font-weight: bold !important;
  border-bottom: 1px solid rgba(255, 184, 48, 0.3) !important;
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
  .detail-grid {
    grid-template-columns: 1fr;
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
  .route-display-container {
    flex-direction: column;
    gap: 15px;
  }
  .route-arrow {
    transform: rotate(90deg);
    margin: 10px 0;
  }
}

@media (max-width: 768px) {
  .power-inspection-page {
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
  .fault-tag {
    margin-bottom: 8px;
  }
  .task-name {
    white-space: normal; /* 窄屏下允许换行 */
  }
  .task-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .task-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  .detail-btn,
  .deploy-btn {
    width: 100%;
    justify-content: center;
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
    min-width: auto;
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

