<template>
    <el-dialog
      title="创建巡航任务"
      :visible.sync="visible"
      width="600px"
      @closed="resetForm"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="任务名称">
          <el-input v-model="form.name" placeholder="请输入任务名称"></el-input>
        </el-form-item>
        
        <el-form-item label="预设路线">
          <el-select 
            v-model="selectedPresetRoute" 
            placeholder="选择预设路线" 
            style="width: 100%"
            @change="loadPresetRoute"
          >
            <el-option label="交通枢纽环线" value="hub_loop"></el-option>
            <el-option label="机场快线" value="airport_express"></el-option>
          </el-select>
        </el-form-item>
  
        <el-form-item label="巡航路线">
          <div class="route-points">
            <div 
              v-for="(point, index) in form.routePoints" 
              :key="index"
              class="route-point-item"
            >
              <span>{{ point.name }}</span>
              <span class="coord">({{ point.lng.toFixed(4) }}, {{ point.lat.toFixed(4) }})</span>
              <el-button 
                type="text" 
                icon="el-icon-delete" 
                @click="removeRoutePoint(index)"
              ></el-button>
            </div>
            <div v-if="form.routePoints.length === 0" class="no-route-points">
              <i class="el-icon-warning"></i>
              <span>暂无路线点，请添加位置</span>
            </div>
          </div>
        </el-form-item>
  
        <el-form-item label="添加位置">
          <el-select 
            v-model="selectedLocation" 
            placeholder="选择位置" 
            style="width: 100%"
            @change="addLocationPoint"
          >
            <el-option 
              v-for="(location, name) in locations" 
              :key="name" 
              :label="name" 
              :value="{ name, ...location }"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="$emit('close')">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleCreate"
          :loading="creating"
        >
          创建任务
        </el-button>
      </div>
    </el-dialog>
  </template>
  
  <script>
  export default {
    name: 'CreateMissionDialog',
    props: {
      visible: {
        type: Boolean,
        default: false
      },
      locations: {
        type: Object,
        default: () => ({})
      }
    },
    data() {
      return {
        form: {
          name: '',
          routePoints: []
        },
        selectedPresetRoute: '',
        selectedLocation: '',
        creating: false,
        
        presetRoutes: {
          hub_loop: [
            { name: '昌北机场', lng: 115.900, lat: 28.865, stopTime: 120 },
            { name: '南昌站', lng: 115.907, lat: 28.662, stopTime: 90 },
            { name: '南昌西站', lng: 115.768, lat: 28.620, stopTime: 120 },
            { name: '南昌东站', lng: 115.983, lat: 28.625, stopTime: 90 }
          ],
          airport_express: [
            { name: '昌北机场', lng: 115.900, lat: 28.865, stopTime: 60 },
            { name: '八一广场', lng: 115.899, lat: 28.679, stopTime: 30 },
            { name: '南昌站', lng: 115.907, lat: 28.662, stopTime: 60 }
          ]
        }
      }
    },
    methods: {
      handleCreate() {
        if (!this.form.name) {
          this.$message.warning('请输入任务名称')
          return
        }
        
        if (this.form.routePoints.length === 0) {
          this.$message.warning('请添加至少一个路线点')
          return
        }
        
        this.creating = true
        this.$emit('create', {
          missionName: this.form.name,
          routePoints: this.form.routePoints
        })
        this.creating = false
      },
      
      loadPresetRoute() {
        if (this.presetRoutes[this.selectedPresetRoute]) {
          this.form.routePoints = [...this.presetRoutes[this.selectedPresetRoute]]
          this.form.name = this.selectedPresetRoute === 'hub_loop' ? '交通枢纽环线' : '机场快线'
        }
      },
      
      addLocationPoint() {
        if (this.selectedLocation) {
          this.form.routePoints.push(this.selectedLocation)
          this.selectedLocation = ''
        }
      },
      
      removeRoutePoint(index) {
        this.form.routePoints.splice(index, 1)
      },
      
      resetForm() {
        this.form = {
          name: '',
          routePoints: []
        }
        this.selectedPresetRoute = ''
        this.selectedLocation = ''
        this.creating = false
      }
    }
  }
  </script>
  
  <style scoped>
  .route-points {
    max-height: 200px;
    overflow-y: auto;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
    padding: 8px;
    min-height: 60px;
  }
  
  .route-point-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 6px 8px;
    background: #f5f7fa;
    border-radius: 4px;
    margin-bottom: 4px;
  }
  
  .route-point-item:last-child {
    margin-bottom: 0;
  }
  
  .coord {
    color: #909399;
    font-size: 12px;
  }
  
  .no-route-points {
    text-align: center;
    color: #909399;
    padding: 12px;
  }
  
  .no-route-points i {
    margin-right: 4px;
  }
  </style>