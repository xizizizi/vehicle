<template>
  <div class="forest-inspection-page">
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 标题区域 -->
      <div class="header-section">
        <div class="header-title">
          <h1><i class="el-icon-forest"></i> 森林无人机巡检系统</h1>
          <p>实时监控森林资源，智能调度无人机进行防火监测与生态巡查</p>
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
                <span class="filter-label">风险等级：</span>
                <el-select
                  v-model="riskLevelFilter"
                  size="small"
                  placeholder="请选择风险等级"
                  multiple
                  collapse-tags
                  @change="handleRiskLevelFilterChange"
                  style="width: 180px"
                  class="tech-select"
                >
                  <el-option label="火情高危" value="FIRE_HIGH"></el-option>
                  <el-option label="火情中危" value="FIRE_MEDIUM"></el-option>
                  <el-option label="虫害监测" value="PEST_MONITOR"></el-option>
                  <el-option label="生态巡查" value="ECOLOGY"></el-option>
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

          <!-- 森林巡检任务列表 -->
          <div class="panel task-panel">
            <div class="panel-header">
              <div class="panel-title">
                <i class="el-icon-s-order"></i> 森林巡检任务列表
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
                description="暂无符合条件的森林巡检任务"
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
                    task.riskLevel ? getRiskLevelClass(task.riskLevel) : '',
                    getRiskLevelClass(task.riskLevel),
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
                      :type="getRiskLevelTagType(task.riskLevel)"
                      size="small"
                      class="risk-tag tech-tag"
                    >
                      {{ getRiskLevelText(task.riskLevel) }}
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
                <span class="detail-label">风险等级：</span>
                <el-tag
                  size="small"
                  :type="getRiskLevelTagType(selectedTask.riskLevel)"
                  class="tech-tag"
                >
                  {{ getRiskLevelText(selectedTask.riskLevel) }}
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
                    selectedTask.inspectionFocus || "林区火情、植被状况、病虫害"
                  }}</span
                >
              </div>
              <div class="requirement-item">
                <i class="el-icon-warning"></i>
                <span
                  >安全要求：{{
                    selectedTask.safetyRequirements ||
                    "保持安全高度，避免惊扰野生动物"
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
          {{ selectedTask ? selectedTask.taskName : "森林巡检任务" }}
          <el-tag
            v-if="selectedTask"
            :type="getRiskLevelTagType(selectedTask.riskLevel)"
            size="small"
            style="margin-left: 10px"
            class="tech-tag"
          >
            {{ getRiskLevelText(selectedTask.riskLevel) }}
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
                        <i class="el-icon-location"></i> 终点（森林巡检点）
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
                      >系统将自动规划从无人机当前位置到森林巡检点的直线飞行路线</span
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
                        :min="100"
                        :max="800"
                        :step="50"
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
                        :min="5"
                        :max="25"
                        :step="1"
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
                        :min="10"
                        :max="120"
                        :step="10"
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
                    <span class="info-label">风险等级：</span>
                    <el-tag
                      size="small"
                      :type="getRiskLevelTagType(selectedTask.riskLevel)"
                      class="tech-tag"
                    >
                      {{ getRiskLevelText(selectedTask.riskLevel) }}
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
  name: "ForestInspection",
  data() {
    return {
      // 森林巡检任务数据
      forestTasks: [],
      selectedTask: null,

      // 分页和筛选
      currentPage: 1,
      pageSize: 3,
      timeOrder: "desc",
      riskLevelFilter: [],
      statusFilter: [],

      // 可用无人机数据
      availableUAVs: [],
      selectedUav: null,
      uavOptions: [],

      // 巡检区域选项（森林相关）
      inspectionAreas: [
        { label: "梅岭国家森林公园-核心区", value: "meiling_core" },
        { label: "艾溪湖湿地公园-保护区", value: "aixihu_wetland" },
        { label: "瑶湖郊野森林公园", value: "yaohu_forest_park" },
        { label: "西山森林公园-防火区", value: "xishan_fire_prevention" },
        { label: "鄱阳湖湿地生态区", value: "poyang_lake_ecology" },
        { label: "庐山自然保护区", value: "lushan_nature_reserve" },
        { label: "武功山森林公园", value: "wugong_mountain" },
        { label: "三清山防火监测区", value: "sanqing_fire_monitor" },
      ],

      // 起点和终点
      startPoint: { lng: 115.8572, lat: 28.6845 },
      endPoint: { lng: 115.8572, lat: 28.6845 },

      // 部署表单
      deployForm: {
        uavId: null,
        missionName: "",
        inspectionArea: "",
        altitude: 200,
        speed: 12,
        hoverTime: 30,
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
          color: "#26fcd8",
          trend: "up",
          trendIcon: "el-icon-top",
          trendValue: "+0%",
        },
        {
          title: "火情预警",
          value: "0",
          icon: "el-icon-fire",
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
          color: "#26fcd8",
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
        { color: "#26fcd8", percentage: 20 },
        { color: "#2ecc71", percentage: 40 },
        { color: "#3498db", percentage: 60 },
        { color: "#9b59b6", percentage: 80 },
        { color: "#1abc9c", percentage: 100 },
      ],

      // 定时器引用
      refreshInterval: null,
    };
  },
  computed: {
    filteredTasks() {
      let tasks = [...this.forestTasks];

      if (this.riskLevelFilter.length > 0) {
        tasks = tasks.filter((task) =>
          this.riskLevelFilter.includes(task.riskLevel)
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
      return this.forestTasks.length;
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
        this.deployForm.altitude >= 100 &&
        this.deployForm.altitude <= 800 &&
        this.deployForm.speed >= 5 &&
        this.deployForm.speed <= 25;
      return valid ? "success" : "error";
    },

    // 获取参数检查文本
    getParamCheckText() {
      const valid =
        this.deployForm.altitude >= 100 &&
        this.deployForm.altitude <= 800 &&
        this.deployForm.speed >= 5 &&
        this.deployForm.speed <= 25;
      return valid ? "✓ 正常" : "✗ 异常";
    },

    // 设置起点和终点
    setRoutePoints() {
      if (this.selectedUav) {
        this.startPoint = {
          lng: this.selectedUav.currentLng || 115.8572,
          lat: this.selectedUav.currentLat || 28.6845,
        };
      }

      if (this.selectedTask) {
        this.endPoint = {
          lng: this.selectedTask.longitude || 115.8572,
          lat: this.selectedTask.latitude || 28.6845,
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
        this.loadingMessage = "正在加载森林巡检任务数据...";
        await this.loadForestTasks();
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

    async loadForestTasks() {
      try {
        const response = await axios.get(
          "http://localhost:8080/api/reports/data/list",
          {
            params: {
              systemType: "FOREST",
            },
          }
        );

        if (response.data && Array.isArray(response.data)) {
          this.forestTasks = response.data.map((task) => {
            let status = "PENDING";
            if (task.droneId) {
              status = "INSPECTING";
            }
            // 确保有riskLevel字段
            if (!task.riskLevel) {
              task.riskLevel = this.assignRiskLevel(task);
            }

            return {
              ...task,
              status: status,
            };
          });

          this.addLog(
            "success",
            `成功加载 ${this.forestTasks.length} 个森林巡检任务`
          );
        } else {
          this.forestTasks = this.getMockForestTasks();
          this.addLog("info", "使用模拟森林巡检任务数据");
        }
      } catch (error) {
        console.error("加载森林巡检任务失败:", error);
        this.addLog("error", `加载森林巡检任务失败: ${error.message}`);
        this.forestTasks = this.getMockForestTasks();
      }
    },

    assignRiskLevel(task) {
      // 根据任务描述或严重程度分配风险等级
      const desc = task.description || "";
      if (desc.includes("火情") || desc.includes("火灾")) return "FIRE_HIGH";
      if (desc.includes("虫害")) return "PEST_MONITOR";
      if (desc.includes("监测")) return "ECOLOGY";
      return "FIRE_MEDIUM";
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
      const totalTasks = this.forestTasks.length;
      const fireWarningTasks = this.forestTasks.filter(
        (task) => task.riskLevel === "FIRE_HIGH"
      ).length;

      const completedTasks = this.forestTasks.filter(
        (task) => task.status === "COMPLETED"
      ).length;
      const completionRate =
        totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;

      this.statCards[0].value = totalTasks.toString();
      this.statCards[1].value = fireWarningTasks.toString();
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
      if (taskDesc.includes("梅岭")) {
        this.deployForm.inspectionArea = "meiling_core";
      } else if (taskDesc.includes("艾溪湖")) {
        this.deployForm.inspectionArea = "aixihu_wetland";
      } else if (taskDesc.includes("瑶湖")) {
        this.deployForm.inspectionArea = "yaohu_forest_park";
      } else if (taskDesc.includes("西山")) {
        this.deployForm.inspectionArea = "xishan_fire_prevention";
      } else if (taskDesc.includes("鄱阳湖")) {
        this.deployForm.inspectionArea = "poyang_lake_ecology";
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
            name: this.selectedTask.taskName || "森林巡检点",
            stopTime: this.deployForm.hoverTime || 60,
          },
        ];

        // 创建森林巡航任务
        this.addLog("info", "开始创建森林巡检巡航任务...");

        const missionData = {
          missionName: this.deployForm.missionName,
          routePoints: routePoints,
          missionType: "CRUISE",
          systemType: "FOREST",
          inspectionDataId: this.selectedTask.id,
          severityLevel: this.selectedTask.riskLevel || "MEDIUM",
          responseTime: this.deployForm.hoverTime || 60,
          droneSn: this.selectedUav.sn,
          assignedUavId: this.selectedUav.id,
        };

        console.log("创建森林巡航任务数据:", missionData);

        // 使用电力任务接口（后端需要支持FOREST系统类型）
        const missionResponse = await axios.post(
          "http://localhost:8080/api/missions/power-cruise",
          missionData,
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );

        console.log("森林巡航任务响应:", missionResponse.data);

        if (missionResponse.data && missionResponse.data.success) {
          const mission = missionResponse.data.data;
          this.addLog(
            "success",
            `森林巡检巡航任务创建成功！任务ID: ${mission.id}`
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
      const taskIndex = this.forestTasks.findIndex(
        (task) => task.id === taskId
      );
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
            backendStatus = "EXECUTING";
            break;
          case "COMPLETED":
          case "已完成":
            backendStatus = "COMPLETED";
            break;
          default:
            backendStatus = "PENDING";
        }

        this.forestTasks[taskIndex].status = backendStatus;
        if (droneId) {
          this.forestTasks[taskIndex].droneId = droneId;
        }
        this.$set(this.forestTasks, taskIndex, this.forestTasks[taskIndex]);
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
            status: backendStatus,
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
        altitude: 200,
        speed: 12,
        hoverTime: 30,
        routePoints: [],
      };
      this.startPoint = { lng: 115.8572, lat: 28.6845 };
      this.endPoint = { lng: 115.8572, lat: 28.6845 };
      if (this.$refs.deployForm) {
        this.$refs.deployForm.resetFields();
      }
    },

    async refreshTasks() {
      this.refreshing = true;
      try {
        await this.loadForestTasks();
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

    getRiskLevelClass(riskLevel) {
      switch (riskLevel) {
        case "FIRE_HIGH":
          return "risk-fire-high";
        case "FIRE_MEDIUM":
          return "risk-fire-medium";
        case "PEST_MONITOR":
          return "risk-pest";
        case "ECOLOGY":
          return "risk-ecology";
        default:
          return "";
      }
    },

    getRiskLevelTagType(riskLevel) {
      switch (riskLevel) {
        case "FIRE_HIGH":
          return "danger";
        case "FIRE_MEDIUM":
          return "warning";
        case "PEST_MONITOR":
          return "info";
        case "ECOLOGY":
          return "success";
        default:
          return "info";
      }
    },

    getRiskLevelText(riskLevel) {
      switch (riskLevel) {
        case "FIRE_HIGH":
          return "火情高危";
        case "FIRE_MEDIUM":
          return "火情中危";
        case "PEST_MONITOR":
          return "虫害监测";
        case "ECOLOGY":
          return "生态巡查";
        default:
          return riskLevel;
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

    handleRiskLevelFilterChange(val) {
      this.currentPage = 1;
    },

    handleStatusFilterChange(val) {
      this.currentPage = 1;
    },

    resetFilters() {
      this.timeOrder = "desc";
      this.riskLevelFilter = [];
      this.statusFilter = [];
      this.currentPage = 1;
      this.$message.info("筛选条件已重置");
    },

    getMockForestTasks() {
      const mockTasks = [];
      const riskLevels = [
        "FIRE_HIGH",
        "FIRE_MEDIUM",
        "PEST_MONITOR",
        "ECOLOGY",
      ];
      const taskTypes = [
        "森林火情监测",
        "林区生态巡查",
        "病虫害监测",
        "植被覆盖调查",
        "野生动物监测",
        "森林资源清查",
        "林区边界巡护",
        "水源地保护巡查",
      ];
      const locations = [
        "梅岭国家森林公园",
        "艾溪湖湿地公园",
        "瑶湖郊野森林公园",
        "西山森林公园",
        "鄱阳湖湿地生态区",
        "庐山自然保护区",
        "武功山森林公园",
        "三清山防火监测区",
      ];

      for (let i = 1; i <= 20; i++) {
        const riskLevel =
          riskLevels[Math.floor(Math.random() * riskLevels.length)];
        const taskType =
          taskTypes[Math.floor(Math.random() * taskTypes.length)];
        const location =
          locations[Math.floor(Math.random() * locations.length)];

        const captureTime = new Date(
          Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000
        );

        mockTasks.push({
          id: 300 + i,
          taskName: `${taskType} - ${location}`,
          description: `${location}发现${
            riskLevel === "FIRE_HIGH" ? "火情高危" : "需要常规巡查"
          }，需要立即巡检`,
          latitude: 28.68 + Math.random() * 0.02,
          longitude: 115.89 + Math.random() * 0.02,
          riskLevel: riskLevel,
          responseTime: Math.floor(Math.random() * 45) + 30, // 30-75分钟
          captureTime: captureTime.toISOString(),
          fileCount: Math.floor(Math.random() * 5) + 1,
          systemType: "FOREST",
          status: i <= 5 ? "PENDING" : i <= 10 ? "INSPECTING" : "COMPLETED",
          droneId: i > 5 && i <= 10 ? `FOREST-UAV-${300 + i}` : undefined,
          files: [
            {
              name: `森林照片_${i}.jpg`,
              size: 1024 * 1024 * (Math.random() * 3 + 1),
            },
            {
              name: `监测报告_${i}.pdf`,
              size: 1024 * 1024 * (Math.random() * 0.8 + 0.2),
            },
          ],
          inspectionFocus:
            riskLevel === "FIRE_HIGH"
              ? "火情监测、烟点识别、热源探测"
              : "常规巡查",
          safetyRequirements: "保持安全高度，避免惊扰野生动物",
        });
      }

      return mockTasks;
    },

    getMockUAVs() {
      const mockUAVs = [
        {
          id: 28,
          sn: "FOREST-UAV-001",
          model: "DJI Matrice 300 RTK",
          status: "IDLE",
          batteryLevel: 95,
          currentLng: 115.8572,
          currentLat: 28.6845,
          loadCapacity: 6,
          endurance: 50,
          currentMission: null,
          createdTime: "2026-02-02 14:10:33",
          updatedTime: "2026-02-02 14:10:33",
        },
        {
          id: 21,
          sn: "DJI-FOREST-827",
          model: "DJI Mavic 3",
          status: "IDLE",
          batteryLevel: 80,
          currentLng: 115.8129,
          currentLat: 28.6602,
          loadCapacity: 4,
          endurance: 40,
          currentMission: null,
          createdTime: "2026-01-31 22:55:35",
          updatedTime: "2026-01-31 22:55:35",
        },
        {
          id: 18,
          sn: "DJI-FOREST-397",
          model: "DJI Matrice 300 RTK",
          status: "CHARGING",
          batteryLevel: 60,
          currentLng: 115.8129,
          currentLat: 28.6602,
          loadCapacity: 6,
          endurance: 50,
          currentMission: null,
          createdTime: "2026-01-31 22:48:31",
          updatedTime: "2026-01-31 22:48:31",
        },
        {
          id: 15,
          sn: "DJI-FOREST-016",
          model: "DJI Mavic 3",
          status: "IDLE",
          batteryLevel: 40,
          currentLng: 115.867,
          currentLat: 28.673,
          loadCapacity: 4,
          endurance: 40,
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
.forest-inspection-page {
  padding: 20px;
  background: radial-gradient(ellipse at center, #090e09 0%, #0f1312 100%);
  min-height: 100vh;          /* 允许内容撑开，不固定高度 */
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 14px;
  padding: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(38, 252, 216, 0.2);
  flex-wrap: wrap;            /* 窄屏换行 */
  gap: 15px;
}

.header-title h1 {
  color: #26fcd8;
  margin-bottom: 8px;
  font-size: 24px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(38, 252, 216, 0.5);
  letter-spacing: 1px;
}

.header-title p {
  color: #b3d9ff;
  font-size: 14px;
  opacity: 0.9;
  text-shadow: 0 0 5px rgba(179, 217, 255, 0.3);
}

/* 行动按钮 */
.action-btn {
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  box-shadow: 0 0 12px rgba(38, 252, 216, 0.3) !important;
  color: #26fcd8 !important;
  font-weight: 600;
  letter-spacing: 0.5px;
  border-radius: 8px;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(15, 40, 25, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.6) !important;
  box-shadow: 0 0 18px rgba(38, 252, 216, 0.5) !important;
  transform: translateY(-2px);
}

/* 统计卡片 - 响应式网格自适应 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); /* 自动换行 */
  gap: 20px;
}

.stat-card {
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  transition: all 0.3s ease;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(38, 252, 216, 0.2);
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 25px rgba(38, 252, 216, 0.4);
  border-color: rgba(38, 252, 216, 0.7);
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
    rgba(38, 252, 216, 0.1),
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
  color: #26fcd8;
  margin-bottom: 4px;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
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
  color: #ffb830;
  text-shadow: 0 0 5px rgba(255, 184, 48, 0.5);
}

.stat-trend.down {
  color: #ff6b6b;
  text-shadow: 0 0 5px rgba(255, 107, 107, 0.5);
}

.stat-trend.stable {
  color: #26fcd8;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 14px;
  box-shadow: 0 0 20px rgba(38, 252, 216, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.task-panel {
  flex: 1;
  min-height: 300px;          /* 保证最小高度 */
}

.logs-panel {
  height: 100%;
  min-height: 500px;
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid rgba(38, 252, 216, 0.2);
  display: flex;
  flex-wrap: wrap;            /* 窄屏换行 */
  justify-content: space-between;
  align-items: center;
  background: rgba(38, 252, 216, 0.1);
  flex-shrink: 0;
  gap: 10px;
}

.panel-title {
  font-size: 18px;
  font-weight: bold;
  color: #26fcd8;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
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
  background: rgba(38, 252, 216, 0.5);
  border-radius: 3px;
}
.panel-content::-webkit-scrollbar-thumb:hover {
  background: rgba(38, 252, 216, 0.7);
}

.panel-footer {
  padding: 15px 20px;
  border-top: 1px solid rgba(38, 252, 216, 0.2);
  background: rgba(38, 252, 216, 0.1);
  flex-shrink: 0;
}

/* 任务控制区 */
.task-controls {
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 0 15px rgba(38, 252, 216, 0.2);
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
  color: #26fcd8 !important;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
  background: transparent !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  padding: 5px 12px !important;
  border-radius: 6px !important;
}

.reset-btn:hover {
  background: rgba(10, 30, 20, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.6) !important;
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
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  border-radius: 6px !important;
  box-shadow: 0 0 8px rgba(38, 252, 216, 0.2) inset;
}
.tech-select ::v-deep .el-input__inner::placeholder {
  color: rgba(38, 252, 216, 0.6) !important;
}
.tech-select ::v-deep .el-select-dropdown {
  background: rgba(10, 30, 20, 0.95) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  backdrop-filter: blur(10px);
}
.tech-btn {
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
  box-shadow: 0 0 10px rgba(38, 252, 216, 0.2) !important;
  transition: all 0.3s ease !important;
  position: relative !important;
  z-index: 1000 !important;
}
.tech-btn:hover {
  background: rgba(15, 40, 25, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.6) !important;
  box-shadow: 0 0 15px rgba(38, 252, 216, 0.4) !important;
  transform: translateY(-2px);
}
.tech-btn:disabled {
  background: rgba(10, 30, 20, 0.5) !important;
  border-color: rgba(38, 252, 216, 0.2) !important;
  color: rgba(38, 252, 216, 0.4) !important;
  box-shadow: none !important;
  transform: none !important;
}
.tech-tag {
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  text-shadow: 0 0 3px rgba(38, 252, 216, 0.5);
  border-radius: 10px !important;
}

/* 分页器（保留原始样式，只添加响应式换行） */
::v-deep .el-pagination .btn-prev,
::v-deep .el-pagination .btn-next,
::v-deep .el-pagination .el-pager li {
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  border-radius: 6px !important;
  min-width: 32px !important;
  height: 32px !important;
  line-height: 32px !important;
  margin: 0 4px !important;
  text-align: center !important;
  transition: all 0.3s ease !important;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
  box-shadow: 0 0 8px rgba(38, 252, 216, 0.2) !important;
  font-size: 14px !important;
  font-weight: normal !important;
  padding: 0 !important;
}
::v-deep .el-pagination .btn-prev .el-icon,
::v-deep .el-pagination .btn-next .el-icon {
  color: #26fcd8 !important;
  font-size: 14px !important;
  font-weight: bold !important;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.8);
}
::v-deep .el-pagination .btn-prev:hover,
::v-deep .el-pagination .btn-next:hover,
::v-deep .el-pagination .el-pager li:hover {
  background: rgba(15, 40, 25, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.7) !important;
  color: #ffffff !important;
  box-shadow: 0 0 12px rgba(38, 252, 216, 0.4) !important;
  transform: translateY(-2px);
}
::v-deep .el-pagination .btn-prev:hover .el-icon,
::v-deep .el-pagination .btn-next:hover .el-icon {
  color: #ffffff !important;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
}
::v-deep .el-pagination .el-pager li.active {
  background: rgba(38, 252, 216, 0.2) !important;
  border-color: rgba(38, 252, 216, 0.8) !important;
  color: #ffffff !important;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.9) !important;
  box-shadow: 0 0 15px rgba(38, 252, 216, 0.6) !important;
  font-weight: bold !important;
}
::v-deep .el-pagination .btn-prev:disabled,
::v-deep .el-pagination .btn-next:disabled {
  background: rgba(10, 30, 20, 0.5) !important;
  border-color: rgba(38, 252, 216, 0.2) !important;
  color: rgba(38, 252, 216, 0.4) !important;
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
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  box-shadow: 0 0 8px rgba(38, 252, 216, 0.2) inset;
  border-radius: 6px !important;
  font-size: 13px !important;
  height: 32px !important;
  line-height: 32px !important;
}

.tech-input ::v-deep .el-input__inner {
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  border-radius: 6px !important;
  box-shadow: 0 0 8px rgba(38, 252, 216, 0.2) inset;
}
.tech-input ::v-deep .el-input__inner::placeholder {
  color: rgba(38, 252, 216, 0.6) !important;
}
.tech-input ::v-deep .el-input-group__append {
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  border-left: none !important;
}
.tech-empty ::v-deep .el-empty__image {
  filter: drop-shadow(0 0 10px rgba(38, 252, 216, 0.5));
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
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
  box-shadow: 0 10px 25px rgba(38, 252, 216, 0.3);
  border-color: rgba(38, 252, 216, 0.6);
  background: rgba(15, 40, 25, 0.9);
}
.task-disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
.task-disabled:hover {
  transform: none;
  box-shadow: none;
  border-color: rgba(38, 252, 216, 0.4);
  background: rgba(10, 30, 20, 0.9);
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
/* 状态徽章 */
.status-pending {
  background: rgba(38, 252, 216, 0.2);
  color: #26fcd8;
  border: 1px solid rgba(38, 252, 216, 0.4);
  box-shadow: 0 0 8px rgba(38, 252, 216, 0.3);
}
.status-inspecting {
  background: rgba(0, 240, 255, 0.2);
  color: #00f0ff;
  border: 1px solid rgba(0, 240, 255, 0.4);
  box-shadow: 0 0 8px rgba(0, 240, 255, 0.3);
}
.status-completed {
  background: rgba(255, 184, 48, 0.2);
  color: #ffb830;
  border: 1px solid rgba(255, 184, 48, 0.4);
  box-shadow: 0 0 8px rgba(255, 184, 48, 0.3);
}
.task-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding-right: 70px;
  flex-wrap: wrap;
  gap: 8px;
}
.risk-tag {
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
  color: #26fcd8;
  width: 16px;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
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
  border-top: 1px solid rgba(38, 252, 216, 0.2);
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
  color: #26fcd8;
  display: flex;
  align-items: center;
  gap: 4px;
  text-shadow: 0 0 3px rgba(38, 252, 216, 0.5);
}
.file-info {
  color: #b3d9ff;
  display: flex;
  align-items: center;
  gap: 4px;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
}
.detail-btn {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  margin-right: 8px;
  z-index: 11 !important;
  pointer-events: auto !important;
  position: relative !important;
}
.detail-btn:hover {
  background: rgba(15, 40, 25, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.6) !important;
  box-shadow: 0 0 10px rgba(38, 252, 216, 0.3) !important;
}
.deploy-btn {
  font-size: 12px;
  padding: 6px 12px;
  border-radius: 6px;
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
  z-index: 11 !important;
  pointer-events: auto !important;
  position: relative !important;
}
.deploy-btn:hover:not(:disabled) {
  background: rgba(15, 40, 25, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.6) !important;
  box-shadow: 0 0 10px rgba(38, 252, 216, 0.3) !important;
}
.deploy-btn:disabled {
  background: rgba(10, 30, 20, 0.5) !important;
  border: 1px solid rgba(100, 100, 100, 0.3) !important;
  color: rgba(200, 200, 200, 0.5) !important;
  box-shadow: none !important;
  cursor: not-allowed !important;
}
.risk-fire-high {
  border-left: 5px solid #ff6b6b;
  background: rgba(10, 30, 20, 0.9) !important;
}
.risk-fire-medium {
  border-left: 5px solid #ffb830;
  background: rgba(10, 30, 20, 0.9) !important;
}
.risk-pest {
  border-left: 5px solid #3498db;
  background: rgba(10, 30, 20, 0.9) !important;
}
.risk-ecology {
  border-left: 5px solid #2ecc71;
  background: rgba(10, 30, 20, 0.9) !important;
}

/* 选中的无人机信息 */
.selected-uav-info {
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
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
  color: #26fcd8;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
}
.no-uav-warning {
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 6px;
  padding: 10px;
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #26fcd8;
  font-size: 13px;
}
.no-uav-warning i {
  font-size: 16px;
}
.capacity-info {
  margin-left: 15px;
  color: #ffb830;
  font-size: 13px;
  font-weight: 500;
  text-shadow: 0 0 3px rgba(255, 184, 48, 0.3);
}

/* 部署表单 */
.deploy-form {
  font-size: 14px;
}
.form-input, .form-select, .form-number {
  width: 100%;
}
.form-unit {
  margin-left: 10px;
  color: #b3d9ff;
  font-size: 14px;
  text-shadow: 0 0 3px rgba(179, 217, 255, 0.3);
}
.status-warning {
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 8px;
  padding: 12px;
  margin: 15px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #26fcd8;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.3);
}
.status-warning i {
  font-size: 18px;
  color: #26fcd8;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
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
  color: rgba(38, 252, 216, 0.5);
  filter: drop-shadow(0 0 10px rgba(38, 252, 216, 0.3));
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
  border-bottom: 1px solid rgba(38, 252, 216, 0.2);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  transition: all 0.3s ease;
  background: rgba(10, 30, 20, 0.9);
  border-left: 4px solid transparent;
  border-radius: 4px;
  margin-bottom: 6px;
  border: 1px solid rgba(38, 252, 216, 0.2) !important;
}
.log-item:hover {
  background: rgba(15, 40, 25, 0.9);
  transform: translateX(5px);
  border-left-color: currentColor;
  border-color: rgba(38, 252, 216, 0.4) !important;
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
  background: rgba(10, 30, 20, 0.95) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  border-radius: 14px !important;
  box-shadow: 0 0 30px rgba(38, 252, 216, 0.3) !important;
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
  color: #26fcd8;
  font-weight: bold;
  letter-spacing: 0.5px;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 10px;
  padding: 15px;
  min-height: 80px;
  min-width: 200px;
}
.point-label {
  color: #26fcd8;
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
  color: #26fcd8;
  font-size: 24px;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
}
.route-info {
  margin-top: 15px;
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 8px;
  padding: 12px;
  color: #26fcd8;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 详情对话框样式（保留原始） */
::v-deep .task-detail-dialog .el-dialog {
  background: rgba(20, 20, 20, 0.98) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  border-radius: 14px !important;
  box-shadow: 0 0 30px rgba(38, 252, 216, 0.3) !important;
  backdrop-filter: blur(10px) !important;
}
::v-deep .task-detail-dialog .el-dialog__header {
  background: rgba(10, 30, 20, 0.9) !important;
  border-bottom: 1px solid rgba(38, 252, 216, 0.4) !important;
}
::v-deep .task-detail-dialog .el-dialog__title {
  color: #26fcd8 !important;
  text-shadow: 0 0 10px rgba(38, 252, 216, 0.5);
}
::v-deep .task-detail-dialog .el-dialog__footer {
  background: rgba(20, 20, 20, 0.95) !important;
  border-top: 2px solid rgba(38, 252, 216, 0.5) !important;
  padding: 20px !important;
}
::v-deep .task-detail-dialog .el-button {
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
}
::v-deep .task-detail-dialog .el-button:hover {
  background: rgba(15, 40, 25, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.6) !important;
}
::v-deep .task-detail-dialog .el-button--primary {
  background: rgba(38, 252, 216, 0.2) !important;
}
::v-deep .task-detail-dialog .el-button--primary:hover {
  background: rgba(38, 252, 216, 0.3) !important;
}
.detail-content {
  background: rgba(10, 30, 20, 0.9);
  border-radius: 8px;
  padding: 20px;
  border: 1px solid rgba(38, 252, 216, 0.2);
}
.detail-section {
  margin-bottom: 25px;
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.3);
  border-radius: 8px;
  padding: 20px;
}
.detail-section h3 {
  color: #26fcd8;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid rgba(38, 252, 216, 0.5);
  text-shadow: 0 0 10px rgba(38, 252, 216, 0.5);
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
  background: rgba(10, 30, 20, 0.9);
  border-radius: 6px;
  border: 1px solid rgba(38, 252, 216, 0.2);
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.3);
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
  background: rgba(10, 30, 20, 0.9);
  border-radius: 6px;
  border: 1px solid rgba(38, 252, 216, 0.3);
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
  background: rgba(15, 40, 25, 0.9);
  transform: translateX(5px);
  border-color: rgba(38, 252, 216, 0.5);
}
.requirement-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #b3d9ff;
  padding: 8px 12px;
  background: rgba(10, 30, 20, 0.9);
  border-radius: 6px;
  border-left: 3px solid #26fcd8;
  border-right: 1px solid rgba(38, 252, 216, 0.2);
  border-top: 1px solid rgba(38, 252, 216, 0.2);
  border-bottom: 1px solid rgba(38, 252, 216, 0.2);
  transition: all 0.3s ease;
}
.requirement-item:hover {
  background: rgba(15, 40, 25, 0.9);
  transform: translateX(5px);
}
.requirement-item i {
  color: #26fcd8;
  font-size: 16px;
}

/* 全屏无人机部署对话框样式 */
::v-deep .fullscreen-deploy-dialog {
  background: rgba(10, 30, 20, 0.95) !important;
  backdrop-filter: blur(10px) !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header {
  background: rgba(20, 20, 20, 0.95) !important;
  border-bottom: 2px solid rgba(38, 252, 216, 0.5) !important;
  padding: 20px !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__title {
  color: #26fcd8 !important;
  text-shadow: 0 0 10px rgba(38, 252, 216, 0.5);
  font-size: 18px !important;
  font-weight: bold !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__close {
  color: #26fcd8 !important;
  font-size: 20px !important;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.8);
}
::v-deep .fullscreen-deploy-dialog .el-dialog__header .el-dialog__close:hover {
  background: rgba(10, 30, 20, 0.9) !important;
  border-bottom: 1px solid rgba(38, 252, 216, 0.4) !important;
}
::v-deep .fullscreen-deploy-dialog .el-dialog__body {
  padding: 0 !important;
  height: calc(100vh - 60px) !important;
  overflow: auto !important;
  background: rgba(10, 30, 20, 0.95) !important;
}
.dialog-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #26fcd8;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 12px;
  padding: 25px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(38, 252, 216, 0.2);
}
.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #26fcd8;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(38, 252, 216, 0.2);
  transition: all 0.3s ease;
}
.info-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 25px rgba(38, 252, 216, 0.4);
  border-color: rgba(38, 252, 216, 0.6);
}
.info-card-title {
  font-size: 16px;
  font-weight: bold;
  color: #26fcd8;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-shadow: 0 0 5px rgba(38, 252, 216, 0.5);
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(38, 252, 216, 0.3);
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
  color: #26fcd8;
  text-shadow: 0 0 8px rgba(38, 252, 216, 0.5);
}
.info-value.success {
  color: #2ecc71;
  text-shadow: 0 0 5px rgba(46, 204, 113, 0.5);
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
  background: rgba(10, 30, 20, 0.9);
  border: 1px solid rgba(38, 252, 216, 0.4);
  border-radius: 12px;
  margin-top: 30px;
  backdrop-filter: blur(5px);
  box-shadow: 0 0 15px rgba(38, 252, 216, 0.2);
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
  background: rgba(10, 30, 20, 0.9) !important;
  border: 1px solid rgba(38, 252, 216, 0.4) !important;
  color: #26fcd8 !important;
}
.deploy-submit-btn:hover:not(:disabled) {
  background: rgba(15, 40, 25, 0.9) !important;
  border-color: rgba(38, 252, 216, 0.6) !important;
}
::v-deep .el-select-dropdown__item {
  padding: 8px 20px !important;
  font-size: 14px !important;
}
::v-deep .el-select-dropdown__item.selected {
  background: rgba(38, 252, 216, 0.2) !important;
  color: #26fcd8 !important;
}
::v-deep .el-select-group__title {
  background: rgba(10, 30, 20, 0.9) !important;
  color: #26fcd8 !important;
  font-weight: bold !important;
  border-bottom: 1px solid rgba(38, 252, 216, 0.3) !important;
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
  .forest-inspection-page {
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
  .risk-tag {
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
  .task-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    width: 100%;
  }
  .detail-btn, .deploy-btn {
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
  .action-bar-left, .action-bar-right {
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
