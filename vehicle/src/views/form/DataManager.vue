<template>
    <div class="data-manager">
      <div class="header">
        <h2 class="title">🚀 巡查数据管理平台</h2>
        <div class="actions">
          <el-button type="primary" @click="showUpload = true" class="neon-btn"
            >上传巡查数据</el-button
          >
          <!-- 新增任务创建按钮 -->
          <el-button type="success" @click="showForestTask = true" class="neon-btn forest-btn"
            >🌲 森林任务</el-button
          >
          <el-button type="warning" @click="showPowerTask = true" class="neon-btn power-btn"
            >⚡ 电力巡检</el-button
          >
          <el-button type="danger" @click="showMedicalTask = true" class="neon-btn medical-btn"
            >🚑 医疗急救</el-button
          >
        </div>
      </div>
  
      <!-- 筛选栏 - 优化布局 -->
      <div class="filter-bar">
        <div class="filter-inputs">
          <el-input
            v-model="filters.taskName"
            placeholder="任务名称"
            class="dark-input"
          />
          <el-input
            v-model="filters.droneId"
            placeholder="无人机编号"
            class="dark-input"
          />
          <el-date-picker
            v-model="filters.range"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="dark-picker"
          />
        </div>
        <div class="filter-actions">
          <el-button
            @click="fetchList"
            type="primary"
            size="small"
            class="neon-btn"
            >查询</el-button
          >
          <el-button @click="resetFilters" size="small" class="neon-btn-alt"
            >重置</el-button
          >
        </div>
      </div>
  
      <!-- 表格 -->
      <el-table
        ref="dataTable"
        :data="list"
        stripe
        border
        class="dark-table"
        :cell-style="tableCellStyle"
        :header-cell-style="tableHeaderCellStyle"
        :row-style="tableRowStyle"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskName" label="任务名称" />
        <el-table-column prop="droneId" label="无人机编号" width="140" />
        <el-table-column prop="captureTime" label="采集时间" width="180" />
        <el-table-column prop="fileCount" label="文件数" width="100" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button
              type="text"
              size="small"
              @click="viewDetail(row.id)"
              class="link-btn"
              >查看</el-button
            >
            <el-button
              type="text"
              size="small"
              @click="downloadAll(row.id)"
              class="link-btn"
              >下载</el-button
            >
          </template>
        </el-table-column>
      </el-table>
  
      <el-pagination
        class="pagination"
        :page-size="limit"
        :current-page="page"
        :total="total"
        @current-change="onPageChange"
      />
  
      <!-- 原有上传巡查数据弹窗 -->
      <el-dialog
        title="上传巡查数据"
        :visible.sync="showUpload"
        width="600px"
        class="dark-dialog"
        @opened="onDialogOpened"
      >
        <el-form :model="uploadForm" label-width="100px" class="dialog-form">
          <el-form-item label="任务名称">
            <el-input v-model="uploadForm.taskName" class="dark-input" />
          </el-form-item>
          <el-form-item label="无人机编号">
            <el-input v-model="uploadForm.droneId" class="dark-input" />
          </el-form-item>
          <el-form-item label="说明">
            <el-input
              type="textarea"
              v-model="uploadForm.description"
              rows="3"
              class="dark-input"
            />
          </el-form-item>
          <el-form-item label="文件">
            <el-upload
              ref="uploader"
              :file-list="fileList"
              :auto-upload="false"
              :action="null"
              multiple
              drag
              :before-upload="beforeFile"
              :on-remove="onRemove"
              :on-change="onChange"
              class="dark-upload"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">
                将文件拖拽到此，或<em>点击上传</em>
              </div>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showUpload = false" class="neon-btn-alt"
            >取消</el-button
          >
          <el-button type="primary" @click="submitUpload" class="neon-btn"
            >开始上传</el-button
          >
        </template>
      </el-dialog>
  
      <!-- 森林任务弹窗 -->
      <el-dialog
        title="🌲 创建森林任务"
        :visible.sync="showForestTask"
        width="500px"
        class="dark-dialog"
        @opened="onDialogOpened"
      >
        <el-form :model="forestForm" label-width="100px" class="dialog-form">
          <el-form-item label="任务类型" required>
            <el-select v-model="forestForm.taskType" class="dark-input" style="width: 100%">
              <el-option label="火险监测" value="FIRE_MONITORING"></el-option>
              <el-option label="病虫害监测" value="PEST_MONITORING"></el-option>
              <el-option label="野生动物监测" value="WILDLIFE_MONITORING"></el-option>
              <el-option label="自定义任务" value="CUSTOM"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="任务名称">
            <el-input v-model="forestForm.taskName" placeholder="森林火险监测 - 地点" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="地点" required>
            <el-input v-model="forestForm.location" placeholder="梅岭国家森林公园" class="dark-input" />
          </el-form-item>
          
          <!-- 火险监测特有字段 -->
          <div v-if="forestForm.taskType === 'FIRE_MONITORING'">
            <el-form-item label="风险等级">
              <el-select v-model="forestForm.riskLevel" class="dark-input" style="width: 100%">
                <el-option label="低" value="低"></el-option>
                <el-option label="中" value="中"></el-option>
                <el-option label="高" value="高"></el-option>
                <el-option label="极高" value="极高"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="温度(℃)">
              <el-input-number 
                v-model="forestForm.temperature" 
                :min="0" 
                :max="50" 
                controls-position="right"
                class="dark-input-number"
                style="width: 100%" 
              />
            </el-form-item>
            <el-form-item label="湿度(%)">
              <el-input-number 
                v-model="forestForm.humidity" 
                :min="0" 
                :max="100" 
                controls-position="right"
                class="dark-input-number"
                style="width: 100%" 
              />
            </el-form-item>
          </div>
          
          <!-- 病虫害监测特有字段 -->
          <div v-if="forestForm.taskType === 'PEST_MONITORING'">
            <el-form-item label="病虫害类型">
              <el-input v-model="forestForm.pestType" placeholder="松材线虫" class="dark-input" />
            </el-form-item>
            <el-form-item label="树种">
              <el-input v-model="forestForm.treeSpecies" placeholder="马尾松" class="dark-input" />
            </el-form-item>
            <el-form-item label="影响面积">
              <el-input v-model="forestForm.affectedArea" placeholder="10公顷" class="dark-input" />
            </el-form-item>
          </div>
          
          <!-- 野生动物监测特有字段 -->
          <div v-if="forestForm.taskType === 'WILDLIFE_MONITORING'">
            <el-form-item label="动物类型">
              <el-input v-model="forestForm.animalType" placeholder="候鸟" class="dark-input" />
            </el-form-item>
            <el-form-item label="监测时期">
              <el-input v-model="forestForm.monitoringPeriod" placeholder="春季" class="dark-input" />
            </el-form-item>
            <el-form-item label="种群数量">
              <el-input-number 
                v-model="forestForm.population" 
                :min="0" 
                controls-position="right"
                class="dark-input-number"
                style="width: 100%" 
              />
            </el-form-item>
          </div>
          
          <el-form-item label="区域ID">
            <el-input-number 
              v-model="forestForm.areaId" 
              :min="1" 
              controls-position="right"
              class="dark-input-number"
              style="width: 100%" 
            />
          </el-form-item>
          
          <el-form-item label="严重程度">
            <el-select v-model="forestForm.severityLevel" class="dark-input" style="width: 100%">
              <el-option label="正常" value="NORMAL"></el-option>
              <el-option label="中等" value="MEDIUM"></el-option>
              <el-option label="高" value="HIGH"></el-option>
              <el-option label="紧急" value="CRITICAL"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="响应时间(分)">
            <el-input-number 
              v-model="forestForm.responseTime" 
              :min="5" 
              :max="240" 
              controls-position="right"
              class="dark-input-number"
              style="width: 100%" 
            />
          </el-form-item>
          
          <el-form-item label="任务描述">
            <el-input
              type="textarea"
              v-model="forestForm.description"
              rows="3"
              placeholder="请输入任务详细描述..."
              class="dark-input"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showForestTask = false" class="neon-btn-alt">取消</el-button>
          <el-button type="success" @click="createForestTask" class="neon-btn">创建任务</el-button>
        </template>
      </el-dialog>
  
      <!-- 电力巡检弹窗 -->
      <el-dialog
        title="⚡ 创建电力巡检任务"
        :visible.sync="showPowerTask"
        width="500px"
        class="dark-dialog"
        @opened="onDialogOpened"
      >
        <el-form :model="powerForm" label-width="100px" class="dialog-form">
          <el-form-item label="线路类型">
            <el-input v-model="powerForm.lineType" placeholder="输电线路" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="电压等级">
            <el-input v-model="powerForm.voltageLevel" placeholder="220kV" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="巡检类型">
            <el-select v-model="powerForm.inspectionType" class="dark-input" style="width: 100%">
              <el-option label="常规巡检" value="常规巡检"></el-option>
              <el-option label="特殊巡检" value="特殊巡检"></el-option>
              <el-option label="故障巡检" value="故障巡检"></el-option>
              <el-option label="夜间巡检" value="夜间巡检"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="问题类型">
            <el-input v-model="powerForm.issueType" placeholder="绝缘子损坏、导线断股等" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="地点">
            <el-input v-model="powerForm.location" placeholder="红谷滩新区" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="区域ID">
            <el-input-number 
              v-model="powerForm.areaId" 
              :min="1" 
              controls-position="right"
              class="dark-input-number"
              style="width: 100%" 
            />
          </el-form-item>
          
          <el-form-item label="严重程度">
            <el-select v-model="powerForm.severityLevel" class="dark-input" style="width: 100%">
              <el-option label="正常" value="NORMAL"></el-option>
              <el-option label="中等" value="MEDIUM"></el-option>
              <el-option label="高" value="HIGH"></el-option>
              <el-option label="紧急" value="CRITICAL"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="响应时间(分)">
            <el-input-number 
              v-model="powerForm.responseTime" 
              :min="10" 
              :max="240" 
              controls-position="right"
              class="dark-input-number"
              style="width: 100%" 
            />
          </el-form-item>
          
          <el-form-item label="任务描述">
            <el-input
              type="textarea"
              v-model="powerForm.description"
              rows="3"
              placeholder="请输入任务详细描述..."
              class="dark-input"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showPowerTask = false" class="neon-btn-alt">取消</el-button>
          <el-button type="warning" @click="createPowerTask" class="neon-btn">创建任务</el-button>
        </template>
      </el-dialog>
  
      <!-- 医疗急救弹窗 -->
      <el-dialog
        title="🚑 创建医疗急救任务"
        :visible.sync="showMedicalTask"
        width="500px"
        class="dark-dialog"
        @opened="onDialogOpened"
      >
        <el-form :model="medicalForm" label-width="100px" class="dialog-form">
          <el-form-item label="患者状况" required>
            <el-input v-model="medicalForm.patientCondition" placeholder="心脏骤停、呼吸困难等" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="地点" required>
            <el-input v-model="medicalForm.location" placeholder="红谷滩新区" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="所需药品">
            <el-input v-model="medicalForm.requiredMedication" placeholder="肾上腺素、氧气等" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="区域ID">
            <el-input-number 
              v-model="medicalForm.areaId" 
              :min="1" 
              controls-position="right"
              class="dark-input-number"
              style="width: 100%" 
            />
          </el-form-item>
          
          <el-form-item label="严重程度">
            <el-select v-model="medicalForm.severityLevel" class="dark-input" style="width: 100%">
              <el-option label="正常" value="NORMAL"></el-option>
              <el-option label="中等" value="MEDIUM"></el-option>
              <el-option label="高" value="HIGH"></el-option>
              <el-option label="紧急" value="CRITICAL"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="响应时间(分)">
            <el-input-number 
              v-model="medicalForm.responseTime" 
              :min="1" 
              :max="60" 
              controls-position="right"
              class="dark-input-number"
              style="width: 100%" 
            />
          </el-form-item>
          
          <el-form-item label="无人机编号">
            <el-input v-model="medicalForm.droneId" placeholder="DJI-MED001" class="dark-input" />
          </el-form-item>
          
          <el-form-item label="附加信息">
            <el-input
              type="textarea"
              v-model="medicalForm.additionalInfo"
              rows="3"
              placeholder="请输入患者详细信息..."
              class="dark-input"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showMedicalTask = false" class="neon-btn-alt">取消</el-button>
          <el-button type="danger" @click="createMedicalTask" class="neon-btn">创建任务</el-button>
        </template>
      </el-dialog>
  
      <!-- 详情弹窗 -->
      <el-dialog
        :visible.sync="showDetail"
        width="800px"
        title="巡查详情"
        class="dark-dialog"
        @opened="onDialogOpened"
      >
        <div v-if="detail">
          <h3 class="detail-title">{{ detail.data.taskName }}</h3>
          <p class="detail-info">
            <span>无人机：{{ detail.data.droneId }}</span>
            <span>时间：{{ detail.data.captureTime }}</span>
          </p>
          <p class="detail-desc">{{ detail.data.description }}</p>
  
          <div class="media-list">
            <div v-for="m in detail.media" :key="m.id" class="media-card">
              <img
                v-if="isImage(m.mimeType)"
                :src="toUrl(m.storagePath)"
                @click="openPreview(m)"
              />
              <video
                v-else-if="isVideo(m.mimeType)"
                :src="toUrl(m.storagePath)"
                controls
              ></video>
  
              <div class="media-info">
                {{ m.fileName }}
                <a @click="downloadFile(m.id, m.storagePath)">下载</a>
              </div>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    name: "DataManager",
    data() {
      return {
        list: [],
        page: 1,
        limit: 10,
        total: 0,
        filters: { taskName: "", droneId: "", range: null },
        
        // 弹窗显示控制
        showUpload: false,
        showForestTask: false,
        showPowerTask: false,
        showMedicalTask: false,
        showDetail: false,
        
        // 表单数据
        uploadForm: { taskName: "", droneId: "", description: "" },
        forestForm: {
          taskType: "FIRE_MONITORING",
          taskName: "",
          location: "梅岭国家森林公园",
          riskLevel: "高",
          temperature: 32,
          humidity: 45,
          pestType: "松材线虫",
          treeSpecies: "马尾松",
          affectedArea: "10公顷",
          animalType: "候鸟",
          monitoringPeriod: "春季",
          population: null,
          areaId: 25,
          severityLevel: "HIGH",
          responseTime: 15,
          description: ""
        },
        powerForm: {
          lineType: "输电线路",
          voltageLevel: "220kV",
          inspectionType: "常规巡检",
          issueType: "",
          location: "红谷滩新区",
          areaId: 18,
          severityLevel: "MEDIUM",
          responseTime: 30,
          description: ""
        },
        medicalForm: {
          patientCondition: "心脏骤停",
          location: "红谷滩新区",
          requiredMedication: "肾上腺素",
          areaId: 18,
          severityLevel: "CRITICAL",
          responseTime: 5,
          droneId: "DJI-MED001",
          additionalInfo: ""
        },
        
        fileList: [],
        detail: null,
        allTasks: [], // 新增：存储所有任务用于前端分页和排序
      };
    },
    mounted() {
      this.fetchAllTasks(); // 修改：获取所有任务
      this.addGlobalStyles();
    },
    watch: {
      list() {
        this.$nextTick(() => {
          this.forceTableStyles();
        });
      },
      showUpload(val) {
        if (val) {
          this.$nextTick(() => {
            this.styleDialog();
          });
        }
      },
      showDetail(val) {
        if (val) {
          this.$nextTick(() => {
            this.styleDialog();
          });
        }
      },
      showForestTask(val) {
        if (val) {
          this.$nextTick(() => {
            this.styleDialog();
          });
        }
      },
      showPowerTask(val) {
        if (val) {
          this.$nextTick(() => {
            this.styleDialog();
          });
        }
      },
      showMedicalTask(val) {
        if (val) {
          this.$nextTick(() => {
            this.styleDialog();
          });
        }
      },
      // 监听森林任务类型变化，自动生成任务名称
      "forestForm.taskType"(newVal) {
        if (newVal && this.forestForm.location) {
          switch (newVal) {
            case "FIRE_MONITORING":
              this.forestForm.taskName = `森林火险监测 - ${this.forestForm.location}`;
              this.forestForm.severityLevel = "HIGH";
              this.forestForm.responseTime = 15;
              break;
            case "PEST_MONITORING":
              this.forestForm.taskName = `森林病虫害监测 - ${this.forestForm.pestType || '未知病虫害'} - ${this.forestForm.location}`;
              this.forestForm.severityLevel = "MEDIUM";
              this.forestForm.responseTime = 60;
              break;
            case "WILDLIFE_MONITORING":
              this.forestForm.taskName = `野生动物监测 - ${this.forestForm.animalType || '未知动物'} - ${this.forestForm.location}`;
              this.forestForm.severityLevel = "NORMAL";
              this.forestForm.responseTime = 120;
              break;
            default:
              this.forestForm.taskName = `森林巡护任务 - ${this.forestForm.location}`;
          }
        }
      },
      "forestForm.location"(newVal) {
        if (newVal && this.forestForm.taskType) {
          this.forestForm.taskType = this.forestForm.taskType; // 触发taskType的watch
        }
      }
    },
    methods: {
      // 修改：获取所有任务数据
      async fetchAllTasks() {
        try {
          const params = {
            taskName: this.filters.taskName || undefined,
            droneId: this.filters.droneId || undefined,
            offset: 0, // 获取所有数据
            limit: 10000, // 设置一个大数获取所有数据
          };
          
          if (this.filters.range?.length === 2) {
            params.startTime = this.filters.range[0];
            params.endTime = this.filters.range[1];
          }
          
          const res = await axios.get("/api/reports/data/list", { params });
          
          // 获取所有任务数据
          let allData = res.data || [];
          if (Array.isArray(allData)) {
            // 按ID升序排序
            this.allTasks = allData.sort((a, b) => a.id - b.id);
            this.total = this.allTasks.length;
            
            // 更新当前页数据
            this.updateCurrentPageData();
          } else if (res.data.list) {
            // 如果返回的是分页格式
            this.allTasks = res.data.list.sort((a, b) => a.id - b.id);
            this.total = res.data.total || this.allTasks.length;
            
            // 更新当前页数据
            this.updateCurrentPageData();
          } else {
            this.allTasks = [];
            this.total = 0;
            this.list = [];
          }
          
          console.log("获取到所有任务:", this.total, "条记录，按ID升序排列");
          console.log("ID范围:", this.allTasks.length > 0 ? 
            `ID ${this.allTasks[0].id} 到 ${this.allTasks[this.allTasks.length-1].id}` : '无数据');
          
        } catch (error) {
          console.error("获取任务列表失败:", error);
          this.$message.error("获取任务列表失败: " + (error.response?.data?.error || error.message));
        }
      },
      
      // 更新当前页数据
      updateCurrentPageData() {
        if (this.allTasks.length === 0) {
          this.list = [];
          return;
        }
        
        // 计算当前页的起始和结束位置
        const start = (this.page - 1) * this.limit;
        const end = Math.min(start + this.limit, this.allTasks.length);
        
        // 获取当前页数据
        this.list = this.allTasks.slice(start, end);
        
        console.log(`第${this.page}页: 显示ID ${start+1} 到 ${end}, 共${this.list.length}条数据`);
      },
      
      // 修改：获取任务列表 - 现在使用本地存储的所有任务
      async fetchList() {
        // 重新获取所有任务并更新显示
        await this.fetchAllTasks();
      },
      
      // 重置筛选
      resetFilters() {
        this.filters = { taskName: "", droneId: "", range: null };
        this.page = 1; // 重置到第1页
        this.fetchList();
      },
      
      // 分页改变
      onPageChange(p) {
        this.page = p;
        this.updateCurrentPageData();
      },
      
      // 创建森林任务
      async createForestTask() {
        try {
          // 构建请求数据
          const requestData = {
            taskType: this.forestForm.taskType,
            location: this.forestForm.location,
            areaId: this.forestForm.areaId || null,
            severityLevel: this.forestForm.severityLevel,
            responseTime: this.forestForm.responseTime,
            description: this.forestForm.description || undefined,
            taskName: this.forestForm.taskName || undefined
          };
          
          // 根据任务类型添加特定字段
          switch (this.forestForm.taskType) {
            case "FIRE_MONITORING":
              requestData.riskLevel = this.forestForm.riskLevel;
              requestData.temperature = this.forestForm.temperature;
              requestData.humidity = this.forestForm.humidity;
              break;
            case "PEST_MONITORING":
              requestData.pestType = this.forestForm.pestType;
              requestData.treeSpecies = this.forestForm.treeSpecies;
              requestData.affectedArea = this.forestForm.affectedArea;
              break;
            case "WILDLIFE_MONITORING":
              requestData.animalType = this.forestForm.animalType;
              requestData.monitoringPeriod = this.forestForm.monitoringPeriod;
              if (this.forestForm.population) {
                requestData.population = this.forestForm.population;
              }
              break;
          }
          
          console.log("发送森林任务数据:", requestData);
          
          const response = await axios.post("/api/multi-system/forest/task", requestData);
          
          if (response.data.success) {
            this.$message.success("森林任务创建成功");
            this.showForestTask = false;
            this.resetForestForm();
            this.page = 1; // 回到第1页
            this.fetchList(); // 重新获取所有任务
          } else {
            this.$message.error(response.data.message || "创建任务失败");
          }
        } catch (error) {
          console.error("创建森林任务失败:", error);
          this.$message.error("创建森林任务失败: " + (error.response?.data?.message || error.message));
        }
      },
      
      // 创建电力巡检任务
      async createPowerTask() {
        try {
          const requestData = {
            lineType: this.powerForm.lineType,
            voltageLevel: this.powerForm.voltageLevel,
            inspectionType: this.powerForm.inspectionType,
            issueType: this.powerForm.issueType || undefined,
            location: this.powerForm.location,
            areaId: this.powerForm.areaId || null,
            severityLevel: this.powerForm.severityLevel,
            responseTime: this.powerForm.responseTime,
            description: this.powerForm.description || undefined
          };
          
          console.log("发送电力巡检数据:", requestData);
          
          const response = await axios.post("/api/multi-system/power/inspection", requestData);
          
          if (response.data.success) {
            this.$message.success("电力巡检任务创建成功");
            this.showPowerTask = false;
            this.resetPowerForm();
            this.page = 1; // 回到第1页
            this.fetchList(); // 重新获取所有任务
          } else {
            this.$message.error(response.data.message || "创建任务失败");
          }
        } catch (error) {
          console.error("创建电力巡检任务失败:", error);
          this.$message.error("创建电力巡检任务失败: " + (error.response?.data?.message || error.message));
        }
      },
      
      // 创建医疗急救任务
      async createMedicalTask() {
        try {
          const requestData = {
            patientCondition: this.medicalForm.patientCondition,
            location: this.medicalForm.location,
            requiredMedication: this.medicalForm.requiredMedication,
            severityLevel: this.medicalForm.severityLevel,
            responseTime: this.medicalForm.responseTime,
            areaId: this.medicalForm.areaId || null
          };
          
          console.log("发送医疗急救数据:", requestData);
          
          const response = await axios.post("/api/multi-system/medical/emergency", requestData);
          
          if (response.data.success) {
            this.$message.success("医疗急救任务创建成功");
            this.showMedicalTask = false;
            this.resetMedicalForm();
            this.page = 1; // 回到第1页
            this.fetchList(); // 重新获取所有任务
          } else {
            this.$message.error(response.data.message || "创建任务失败");
          }
        } catch (error) {
          console.error("创建医疗急救任务失败:", error);
          this.$message.error("创建医疗急救任务失败: " + (error.response?.data?.message || error.message));
        }
      },
      
      // 重置表单
      resetForestForm() {
        this.forestForm = {
          taskType: "FIRE_MONITORING",
          taskName: "",
          location: "梅岭国家森林公园",
          riskLevel: "高",
          temperature: 32,
          humidity: 45,
          pestType: "松材线虫",
          treeSpecies: "马尾松",
          affectedArea: "10公顷",
          animalType: "候鸟",
          monitoringPeriod: "春季",
          population: null,
          areaId: 25,
          severityLevel: "HIGH",
          responseTime: 15,
          description: ""
        };
      },
      
      resetPowerForm() {
        this.powerForm = {
          lineType: "输电线路",
          voltageLevel: "220kV",
          inspectionType: "常规巡检",
          issueType: "",
          location: "红谷滩新区",
          areaId: 18,
          severityLevel: "MEDIUM",
          responseTime: 30,
          description: ""
        };
      },
      
      resetMedicalForm() {
        this.medicalForm = {
          patientCondition: "心脏骤停",
          location: "红谷滩新区",
          requiredMedication: "肾上腺素",
          areaId: 18,
          severityLevel: "CRITICAL",
          responseTime: 5,
          droneId: "DJI-MED001",
          additionalInfo: ""
        };
      },
      
      // 以下为原有方法（保持兼容）
      beforeFile(file) {
        return true;
      },
      
      onChange(file, fileList) {
        this.fileList = fileList;
      },
      
      onRemove(file, fileList) {
        this.fileList = fileList;
      },
      
      async submitUpload() {
        if (!this.fileList.length) return this.$message.warning("请选择文件");
  
        const form = new FormData();
        form.append("taskName", this.uploadForm.taskName);
        form.append("droneId", this.uploadForm.droneId);
        form.append("description", this.uploadForm.description);
        form.append("uploaderId", 1);
  
        this.fileList.forEach((f) => {
          console.log("上传文件:", f.raw.name);
          form.append("files", f.raw);
        });
  
        try {
          await axios.post("/api/reports/data/upload", form, {
            headers: { "Content-Type": "multipart/form-data" },
          });
          this.$message.success("上传成功");
          this.showUpload = false;
          this.page = 1; // 回到第1页
          this.fetchList(); // 重新获取所有任务
        } catch (e) {
          console.error("上传失败:", e);
          this.$message.error("上传失败");
        }
      },
      
      async downloadFile(id, path) {
        try {
          const url = this.toUrl(path);
          const res = await axios.get(url, { responseType: "blob" });
          const blob = new Blob([res.data]);
          const link = document.createElement("a");
          link.href = window.URL.createObjectURL(blob);
          link.download = path.split("/").pop();
          link.click();
          window.URL.revokeObjectURL(link.href);
        } catch (e) {
          console.error("下载失败:", e);
          this.$message.error("下载失败");
        }
      },
      
      async downloadAll(id) {
        try {
          const res = await axios.get(`/api/reports/data/download-zip/${id}`, {
            responseType: "blob",
          });
          const blob = new Blob([res.data]);
          const link = document.createElement("a");
          link.href = window.URL.createObjectURL(blob);
          link.download = `巡查数据-${id}.zip`;
          link.click();
          window.URL.revokeObjectURL(link.href);
        } catch (e) {
          console.error("下载失败:", e);
          this.$message.error("下载失败");
        }
      },
      
      async viewDetail(id) {
        try {
          const res = await axios.get(`/api/reports/data/${id}`);
          this.detail = res.data;
          this.showDetail = true;
        } catch (e) {
          this.$message.error("获取详情失败");
        }
      },
      
      toUrl(path) {
        if (!path) return "";
        return path.startsWith("http")
          ? path
          : `${window.location.origin}${path}`;
      },
      
      isImage(mime) {
        return mime?.startsWith("image/");
      },
      
      isVideo(mime) {
        return mime?.startsWith("video/");
      },
      
      openPreview(media) {
        window.open(this.toUrl(media.storagePath), "_blank");
      },
      
      // 样式相关方法
      addGlobalStyles() {
        if (document.getElementById("data-manager-styles")) return;
  
        const style = document.createElement("style");
        style.id = "data-manager-styles";
        
        // 添加您原有的样式
        style.textContent = `
          .data-manager .el-table {
            background: transparent !important;
          }
          .data-manager .el-table th {
            background-color: #1981f6 !important;
            color: white !important;
            border-bottom: 1px solid rgba(0, 255, 255, 0.3) !important;
          }
          .data-manager .el-table td {
            background-color: rgba(0, 44, 81, 0.8) !important;
            color: #b0d5ff !important;
            border-bottom: 1px solid rgba(0, 255, 255, 0.1) !important;
          }
          .data-manager .el-table .el-table__row--striped td {
            background-color: rgba(10, 29, 50, 0.8) !important;
          }
          .data-manager .el-table .el-table__body tr:hover td {
            background-color: rgba(0, 150, 255, 0.3) !important;
          }
          .data-manager .el-table--border {
            border: 1px solid rgba(0, 255, 255, 0.2) !important;
          }
          .data-manager .el-table--border th,
          .data-manager .el-table--border td {
            border-right: 1px solid rgba(0, 255, 255, 0.1) !important;
          }
          
          /* 弹窗深色样式 */
          .dark-dialog .el-dialog {
            background: #041024 !important;
            border: 1px solid rgba(0, 255, 255, 0.2) !important;
            border-radius: 8px !important;
          }
          .dark-dialog .el-dialog__header {
            background: #1981f6 !important;
            color: white !important;
            border-radius: 8px 8px 0 0 !important;
            padding: 15px 20px !important;
          }
          .dark-dialog .el-dialog__title {
            color: white !important;
            font-weight: 600 !important;
          }
          .dark-dialog .el-dialog__body {
            background: #041024 !important;
            color: #b0d5ff !important;
            padding: 20px !important;
            max-height: 60vh !important;
            overflow-y: auto !important;
          }
          .dark-dialog .el-dialog__footer {
            background: #041024 !important;
            border-top: 1px solid rgba(0, 255, 255, 0.1) !important;
            padding: 15px 20px !important;
            border-radius: 0 0 8px 8px !important;
          }
          .dark-dialog .el-form-item__label {
            color: #b0d5ff !important;
          }
          .dark-upload .el-upload-dragger {
            background: rgba(255, 255, 255, 0.05) !important;
            border: 1px dashed rgba(0, 255, 255, 0.3) !important;
            color: #b0d5ff !important;
          }
          .dark-upload .el-upload-dragger:hover {
            border-color: #00d9ff !important;
          }
          .dark-upload .el-icon-upload {
            color: #00d9ff !important;
          }
          
          /* 所有输入框统一深色样式 */
          .data-manager .dark-input .el-input__inner,
          .data-manager .dark-picker .el-input__inner,
          .dark-dialog .el-input__inner,
          .dark-dialog .el-textarea__inner {
            background: rgba(255, 255, 255, 0.05) !important;
            color: #b0d5ff !important;
            border: 1px solid rgba(0, 255, 255, 0.3) !important;
            border-radius: 6px !important;
          }
          .data-manager .dark-input .el-input__inner:focus,
          .data-manager .dark-picker .el-input__inner:focus,
          .dark-dialog .el-input__inner:focus,
          .dark-dialog .el-textarea__inner:focus {
            border-color: #00d9ff !important;
            box-shadow: 0 0 5px rgba(0, 217, 255, 0.3) !important;
          }
          .data-manager .dark-input .el-input__inner::placeholder,
          .data-manager .dark-picker .el-input__inner::placeholder,
          .dark-dialog .el-input__inner::placeholder,
          .dark-dialog .el-textarea__inner::placeholder {
            color: rgba(176, 213, 255, 0.6) !important;
          }
          
          /* 修复：数字输入框深色样式 */
          .dark-input-number .el-input-number__decrease,
          .dark-input-number .el-input-number__increase {
            background: rgba(255, 255, 255, 0.05) !important;
            color: #b0d5ff !important;
            border: 1px solid rgba(0, 255, 255, 0.3) !important;
          }
          
          .dark-input-number .el-input-number__decrease:hover,
          .dark-input-number .el-input-number__increase:hover {
            background: rgba(255, 255, 255, 0.1) !important;
            color: #00d9ff !important;
          }
          
          .dark-input-number .el-input-number__decrease {
            border-right: none !important;
            border-radius: 6px 0 0 6px !important;
          }
          
          .dark-input-number .el-input-number__increase {
            border-left: none !important;
            border-radius: 0 6px 6px 0 !important;
          }
          
          .dark-input-number .el-input .el-input__inner {
            background: rgba(255, 255, 255, 0.05) !important;
            color: #b0d5ff !important;
            border: 1px solid rgba(0, 255, 255, 0.3) !important;
            border-radius: 6px !important;
          }
          
          /* 日期选择器完整深色样式 - 包括输入框和下拉面板 */
          .data-manager .el-date-editor {
            background: rgba(255, 255, 255, 0.05) !important;
            border: 1px solid rgba(0, 255, 255, 0.3) !important;
            border-radius: 6px !important;
          }
          
          .data-manager .el-date-editor .el-input__inner {
            background: transparent !important;
            color: #b0d5ff !important;
            border: none !important;
          }
          
          .data-manager .el-date-editor .el-input__prefix,
          .data-manager .el-date-editor .el-input__suffix {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-date-editor .el-input__prefix .el-input__icon,
          .data-manager .el-date-editor .el-input__suffix .el-input__icon {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-picker-panel {
            background: #041024 !important;
            border: 1px solid rgba(0, 255, 255, 0.2) !important;
            color: #b0d5ff !important;
          }
          
          .data-manager .el-picker-panel .el-picker-panel__body,
          .data-manager .el-picker-panel .el-picker-panel__content {
            background: #041024 !important;
          }
          
          .data-manager .el-picker-panel .el-date-picker__header,
          .data-manager .el-picker-panel .el-date-range-picker__header {
            background: #041024 !important;
            color: #b0d5ff !important;
            border-bottom: 1px solid rgba(0, 255, 255, 0.1) !important;
          }
          
          .data-manager .el-picker-panel .el-date-picker__header-label,
          .data-manager .el-picker-panel .el-date-range-picker__header div {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-picker-panel .el-date-range-picker__content {
            border-color: rgba(0, 255, 255, 0.1) !important;
          }
          
          .data-manager .el-picker-panel .el-date-table th {
            color: #b0d5ff !important;
            border-bottom: 1px solid rgba(0, 255, 255, 0.1) !important;
          }
          
          .data-manager .el-picker-panel .el-date-table td {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-picker-panel .el-date-table td.available:hover {
            color: #00d9ff !important;
          }
          
          .data-manager .el-picker-panel .el-date-table td.current:not(.disabled) {
            color: #00d9ff !important;
          }
          
          .data-manager .el-picker-panel .el-date-table td.today span {
            color: #00d9ff !important;
            font-weight: bold;
          }
          
          .data-manager .el-picker-panel .el-date-table td.in-range div,
          .data-manager .el-picker-panel .el-date-table td.start-date div,
          .data-manager .el-picker-panel .el-date-table td.end-date div {
            background: rgba(0, 217, 255, 0.3) !important;
          }
          
          .data-manager .el-picker-panel .el-picker-panel__icon-btn {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-picker-panel .el-picker-panel__icon-btn:hover {
            color: #00d9ff !important;
          }
          
          .data-manager .el-picker-panel .el-time-panel {
            background: #041024 !important;
            border: 1px solid rgba(0, 255, 255, 0.2) !important;
          }
          
          .data-manager .el-picker-panel .el-time-spinner__wrapper {
            background: #041024 !important;
            border-color: rgba(0, 255, 255, 0.1) !important;
          }
          
          .data-manager .el-picker-panel .el-time-spinner__item {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-picker-panel .el-time-spinner__item.active:not(.disabled) {
            color: #00d9ff !important;
          }
          
          .data-manager .el-picker-panel .el-time-panel__footer {
            background: #041024 !important;
            border-top: 1px solid rgba(0, 255, 255, 0.1) !important;
          }
          
          .data-manager .el-picker-panel .el-button {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-picker-panel .el-button:hover {
            color: #00d9ff !important;
          }
          
          /* 分页组件深色样式 */
          .data-manager .el-pagination {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-pagination .btn-prev,
          .data-manager .el-pagination .btn-next,
          .data-manager .el-pagination .el-pager li {
            background: rgba(255, 255, 255, 0.05) !important;
            color: #b0d5ff !important;
            border: 1px solid rgba(0, 255, 255, 0.3) !important;
          }
          
          .data-manager .el-pagination .btn-prev:hover,
          .data-manager .el-pagination .btn-next:hover,
          .data-manager .el-pagination .el-pager li:hover {
            color: #00d9ff !important;
            border-color: #00d9ff !important;
          }
          
          .data-manager .el-pagination .el-pager li.active {
            background: rgba(0, 217, 255, 0.3) !important;
            color: #00d9ff !important;
            border-color: #00d9ff !important;
          }
          
          .data-manager .el-pagination .el-pagination__jump {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-pagination .el-pagination__editor.el-input {
            background: rgba(255, 255, 255, 0.05) !important;
            border: 1px solid rgba(0, 255, 255, 0.3) !important;
          }
          
          .data-manager .el-pagination .el-pagination__editor .el-input__inner {
            background: transparent !important;
            color: #b0d5ff !important;
            border: none !important;
          }
          
          /* 日期选择器范围输入框深色样式 */
          .data-manager .el-date-editor .el-range-input {
            background: transparent !important;
            color: #b0d5ff !important;
          }
          
          .data-manager .el-date-editor .el-range-input::placeholder {
            color: rgba(176, 213, 255, 0.6) !important;
          }
          
          .data-manager .el-date-editor .el-range-separator {
            color: #b0d5ff !important;
          }
          
          .data-manager .el-date-editor .el-range__close-icon {
            color: #b0d5ff !important;
          }
          
          /* 新增按钮样式 */
          .forest-btn {
            background: linear-gradient(90deg, #10b981, #059669) !important;
            box-shadow: 0 0 10px #10b981 !important;
          }
          
          .power-btn {
            background: linear-gradient(90deg, #f59e0b, #d97706) !important;
            box-shadow: 0 0 10px #f59e0b !important;
          }
          
          .medical-btn {
            background: linear-gradient(90deg, #ef4444, #dc2626) !important;
            box-shadow: 0 0 10px #ef4444 !important;
          }
          
          .forest-btn:hover {
            box-shadow: 0 0 20px #10b981 !important;
          }
          
          .power-btn:hover {
            box-shadow: 0 0 20px #f59e0b !important;
          }
          
          .medical-btn:hover {
            box-shadow: 0 0 20px #ef4444 !important;
          }
        `;
        document.head.appendChild(style);
      },
  
      // 强制更新表格样式
      forceTableStyles() {
        if (!this.$refs.dataTable) return;
  
        const table = this.$refs.dataTable.$el;
        const headers = table.querySelectorAll("th");
        const cells = table.querySelectorAll("td");
        const rows = table.querySelectorAll("tr");
  
        headers.forEach((header) => {
          header.style.backgroundColor = "#1981f6";
          header.style.color = "white";
          header.style.borderBottom = "1px solid rgba(0, 255, 255, 0.3)";
        });
  
        cells.forEach((cell) => {
          cell.style.backgroundColor = "rgba(0, 44, 81, 0.8)";
          cell.style.color = "#b0d5ff";
          cell.style.borderBottom = "1px solid rgba(0, 255, 255, 0.1)";
        });
  
        rows.forEach((row, index) => {
          if (index % 2 === 1) {
            const rowCells = row.querySelectorAll("td");
            rowCells.forEach((cell) => {
              cell.style.backgroundColor = "rgba(10, 29, 50, 0.8)";
            });
          }
        });
      },
  
      // 弹窗打开后的样式处理
      onDialogOpened() {
        this.styleDialog();
      },
  
      // 样式化弹窗
      styleDialog() {
        const dialogs = document.querySelectorAll(".el-dialog");
        dialogs.forEach((dialog) => {
          if (dialog.closest(".dark-dialog")) {
            dialog.style.backgroundColor = "#041024";
            dialog.style.border = "1px solid rgba(0, 255, 255, 0.2)";
            dialog.style.borderRadius = "8px";
  
            const header = dialog.querySelector(".el-dialog__header");
            if (header) {
              header.style.backgroundColor = "#1981f6";
              header.style.color = "white";
              header.style.borderRadius = "8px 8px 0 0";
            }
  
            const body = dialog.querySelector(".el-dialog__body");
            if (body) {
              body.style.backgroundColor = "#041024";
              body.style.color = "#b0d5ff";
              body.style.maxHeight = "60vh";
              body.style.overflowY = "auto";
            }
  
            const footer = dialog.querySelector(".el-dialog__footer");
            if (footer) {
              footer.style.backgroundColor = "#041024";
              footer.style.borderTop = "1px solid rgba(0, 255, 255, 0.1)";
            }
  
            // 设置弹窗内输入框样式
            const inputs = dialog.querySelectorAll(
              ".el-input__inner, .el-textarea__inner"
            );
            inputs.forEach((input) => {
              input.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
              input.style.color = "#b0d5ff";
              input.style.border = "1px solid rgba(0, 255, 255, 0.3)";
              input.style.borderRadius = "6px";
            });
            
            // 设置数字输入框样式
            const numberInputs = dialog.querySelectorAll(".el-input-number");
            numberInputs.forEach((input) => {
              const decreaseBtn = input.querySelector(".el-input-number__decrease");
              const increaseBtn = input.querySelector(".el-input-number__increase");
              const innerInput = input.querySelector(".el-input__inner");
              
              if (decreaseBtn) {
                decreaseBtn.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
                decreaseBtn.style.color = "#b0d5ff";
                decreaseBtn.style.border = "1px solid rgba(0, 255, 255, 0.3)";
              }
              
              if (increaseBtn) {
                increaseBtn.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
                increaseBtn.style.color = "#b0d5ff";
                increaseBtn.style.border = "1px solid rgba(0, 255, 255, 0.3)";
              }
              
              if (innerInput) {
                innerInput.style.backgroundColor = "rgba(255, 255, 255, 0.05)";
                innerInput.style.color = "#b0d5ff";
                innerInput.style.border = "1px solid rgba(0, 255, 255, 0.3)";
              }
            });
          }
        });
      },
  
      // 表格样式方法
      tableCellStyle({ row, column, rowIndex, columnIndex }) {
        return {
          backgroundColor:
            rowIndex % 2 === 0 ? "rgba(0, 44, 81, 0.8)" : "rgba(10, 29, 50, 0.8)",
          color: "#b0d5ff",
          borderBottom: "1px solid rgba(0, 255, 255, 0.1)",
        };
      },
  
      tableHeaderCellStyle({ row, column, rowIndex, columnIndex }) {
        return {
          backgroundColor: "#1981f6",
          color: "white",
          borderBottom: "1px solid rgba(0, 255, 255, 0.3)",
        };
      },
  
      tableRowStyle({ row, rowIndex }) {
        return {
          backgroundColor: "transparent",
        };
      }
    }
  };
  </script>
  
  <style scoped>
  /* 保持您的原始样式，仅做必要调整 */
  .data-manager {
    background: radial-gradient(circle at top left, #071a2f, #030b16 60%);
    min-height: 100vh;
    padding: 20px;
    color: #b0d5ff;
  }
  
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .title {
    color: #00d9ff;
    font-weight: 300;
    letter-spacing: 1px;
    text-shadow: 0 0 5px #00bcd4;
    margin: 0;
  }
  
  .actions {
    display: flex;
    gap: 10px;
  }
  
  /* 筛选栏优化布局 */
  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(0, 30, 60, 0.4);
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
    gap: 15px;
  }
  
  .filter-inputs {
    display: flex;
    flex: 1;
    gap: 12px;
    align-items: center;
  }
  
  .filter-inputs .dark-input,
  .filter-inputs .dark-picker {
    flex: 1;
    min-width: 0;
  }
  
  .filter-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
  }
  
  /* 统一输入框样式 */
  .dark-input,
  .dark-picker {
    flex-shrink: 0;
  }
  
  /* 表格样式 */
  .dark-table {
    width: 100%;
    margin-bottom: 20px;
    border: 1px solid rgba(0, 255, 255, 0.2);
    border-radius: 8px;
    overflow: hidden;
  }
  
  .link-btn {
    color: #00d9ff !important;
  }
  
  .neon-btn {
    background: linear-gradient(90deg, #007cf0, #00dfd8);
    color: #fff;
    border: none;
    box-shadow: 0 0 10px #00eaff;
  }
  
  .neon-btn:hover {
    box-shadow: 0 0 20px #00eaff;
  }
  
  .neon-btn-alt {
    background: rgba(0, 50, 80, 0.6);
    border: 1px solid #00dfd8;
    color: #00dfd8;
  }
  
  /* 分页样式 */
  .pagination {
    text-align: right;
    margin-top: 20px;
  }
  
  /* 详情样式 */
  .detail-title {
    color: #00d9ff;
    margin-bottom: 10px;
  }
  
  .detail-info {
    display: flex;
    gap: 20px;
    margin-bottom: 10px;
    color: #b0d5ff;
  }
  
  .detail-desc {
    color: #b0d5ff;
    margin-bottom: 20px;
  }
  
  .media-list {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .media-card {
    width: 180px;
    background: rgba(0, 40, 60, 0.5);
    border-radius: 10px;
    padding: 6px;
    box-shadow: 0 0 10px rgba(0, 255, 255, 0.1);
    transition: 0.3s;
  }
  
  .media-card:hover {
    box-shadow: 0 0 18px rgba(0, 255, 255, 0.4);
    transform: scale(1.03);
  }
  
  .media-card img,
  .media-card video {
    width: 100%;
    height: 110px;
    object-fit: cover;
    border-radius: 6px;
  }
  
  .media-info {
    margin-top: 6px;
    font-size: 12px;
    color: #b0d5ff;
    text-align: center;
  }
  
  .media-info a {
    color: #00d9ff;
    margin-left: 8px;
    cursor: pointer;
  }
  
  /* 弹窗表单样式 */
  .dialog-form {
    max-height: 50vh;
    overflow-y: auto;
    padding-right: 5px;
  }
  
  /* 响应式调整 */
  @media (max-width: 1200px) {
    .actions {
      flex-wrap: wrap;
    }
  }
  
  @media (max-width: 768px) {
    .header {
      flex-direction: column;
      align-items: flex-start;
      gap: 15px;
    }
    
    .actions {
      width: 100%;
      flex-wrap: wrap;
    }
    
    .filter-bar {
      flex-direction: column;
      align-items: stretch;
    }
    
    .filter-inputs {
      flex-direction: column;
    }
    
    .filter-inputs .dark-input,
    .filter-inputs .dark-picker {
      width: 100%;
      flex: none;
    }
  }
  </style>