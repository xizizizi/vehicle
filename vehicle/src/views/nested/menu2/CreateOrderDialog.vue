<template>
    <el-dialog
      title="创建快递订单"
      :visible.sync="visible"
      width="600px"
      @closed="resetForm"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="起始地点">
          <el-select 
            v-model="form.fromLocation" 
            placeholder="选择起始地点" 
            style="width: 100%"
          >
            <el-option 
              v-for="(location, name) in locations" 
              :key="name" 
              :label="name" 
              :value="name"
            ></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="目的地点">
          <el-select 
            v-model="form.toLocation" 
            placeholder="选择目的地点" 
            style="width: 100%"
          >
            <el-option 
              v-for="(location, name) in locations" 
              :key="name" 
              :label="name" 
              :value="name"
            ></el-option>
          </el-select>
        </el-form-item>
  
        <el-form-item label="包裹重量">
          <el-input-number 
            v-model="form.weight" 
            :min="0.1" 
            :max="5" 
            :step="0.1"
            controls-position="right"
          ></el-input-number>
          <span style="margin-left: 8px">kg</span>
        </el-form-item>
      </el-form>
      
      <div slot="footer">
        <el-button @click="$emit('close')">取消</el-button>
        <el-button 
          type="success" 
          @click="handleCreate"
          :loading="creating"
        >创建订单
        </el-button>
      </div>
    </el-dialog>
  </template>
  
  <script>
  export default {
    name: 'CreateOrderDialog',
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
          fromLocation: '',
          toLocation: '',
          weight: 1.0
        },
        creating: false
      }
    },
    methods: {
      handleCreate() {
        if (!this.form.fromLocation || !this.form.toLocation) {
          this.$message.warning('请选择起始地点和目的地点')
          return
        }
        
        this.creating = true
        this.$emit('create', this.form)
        this.creating = false
      },
      
      resetForm() {
        this.form = {
          fromLocation: '',
          toLocation: '',
          weight: 1.0
        }
        this.creating = false
      }
    }
  }
  </script>
  