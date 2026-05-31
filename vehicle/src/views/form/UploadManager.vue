<template>
    <div class="upload-manager">
      <el-upload
        ref="uploader"
        :file-list="fileList"
        :auto-upload="false"
        :multiple="false"
        drag
        :before-upload="beforeUpload"
        :on-remove="handleRemove"
        :on-change="handleChange"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">拖拽或点击选择文件（支持分片断点续传）</div>
      </el-upload>
  
      <div style="margin-top:12px">
        <el-button type="primary" :loading="uploading" @click="startUpload">开始上传（分片）</el-button>
      </div>
  
      <div v-if="uploading" style="margin-top:12px">
        <div v-for="(p, idx) in progressList" :key="idx" style="margin-bottom:8px">
          <div>{{ p.name }} - {{ Math.round(p.percent) }}%</div>
          <el-progress :percentage="Math.round(p.percent)"></el-progress>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  import SparkMD5 from 'spark-md5';
  import axios from 'axios';
  
  export default {
    name: 'UploadManager',
    props: {
      chunkSize: {
        type: Number,
        default: 2 * 1024 * 1024 // 2MB 每片
      }
    },
    data() {
      return {
        fileList: [],
        uploading: false,
        progressList: [] // [{name, percent}]
      };
    },
    methods: {
      beforeUpload(file) {
        // 只允许单个文件（或调整为多个循环上传）
        return true;
      },
      handleChange(file, fileList) {
        this.fileList = fileList;
      },
      handleRemove(file, fileList) {
        this.fileList = fileList;
      },
  
      async calcFileMd5(file) {
        return new Promise((resolve, reject) => {
          const chunkSize = 2 * 1024 * 1024; // 2MB
          const chunks = Math.ceil(file.size / chunkSize);
          let currentChunk = 0;
          const spark = new SparkMD5.ArrayBuffer();
          const fileReader = new FileReader();
  
          fileReader.onload = (e) => {
            spark.append(e.target.result);
            currentChunk++;
            if (currentChunk < chunks) {
              loadNext();
            } else {
              const md5 = spark.end();
              resolve(md5);
            }
          };
  
          fileReader.onerror = () => {
            reject('MD5 计算失败');
          };
  
          function loadNext() {
            const start = currentChunk * chunkSize;
            const end = Math.min(start + chunkSize, file.size);
            const blobSlice = file.slice(start, end);
            fileReader.readAsArrayBuffer(blobSlice);
          }
  
          loadNext();
        });
      },
  
      async startUpload() {
        if (!this.fileList.length) {
          this.$message.warning('请选择文件');
          return;
        }
        // 支持多文件：这里先做单文件示例
        for (const f of this.fileList) {
          const file = f.raw;
          await this.uploadFileByChunks(file);
        }
        this.$emit('uploaded');
      },
  
      async uploadFileByChunks(file) {
        this.uploading = true;
        // 初始化进度显示
        const prog = { name: file.name, percent: 0 };
        this.progressList = [prog];
  
        const fileMd5 = await this.calcFileMd5(file);
        const chunkSize = this.chunkSize;
        const totalChunks = Math.ceil(file.size / chunkSize);
  
        for (let i = 0; i < totalChunks; i++) {
          const start = i * chunkSize;
          const end = Math.min(start + chunkSize, file.size);
          const chunk = file.slice(start, end);
  
          const form = new FormData();
          form.append('file', chunk);
          form.append('fileMd5', fileMd5);
          form.append('chunkNumber', i + 1);
          form.append('totalChunks', totalChunks);
  
          try {
            await axios.post('/api/upload/chunk', form, {
              headers: { 'Content-Type': 'multipart/form-data' },
              onUploadProgress: (ev) => {
                const chunkPct = (ev.loaded / ev.total) * (1 / totalChunks) * 100;
                // 如果多并行上传可累加，但此处串行上传，直接设置
                prog.percent = ((i) / totalChunks) * 100 + chunkPct;
              }
            });
          } catch (err) {
            this.$message.error('分片上传失败: ' + err);
            this.uploading = false;
            throw err;
          }
        }
  
        // 所有分片上传完毕，通知合并
        try {
          await axios.post('/api/upload/merge', new URLSearchParams({
            fileMd5: fileMd5,
            filename: file.name
          }));
          prog.percent = 100;
          this.$message.success(file.name + ' 上传合并完成');
        } catch (err) {
          this.$message.error('合并分片失败: ' + err);
          throw err;
        } finally {
          this.uploading = false;
        }
      }
    }
  };
  </script>
  
  <style scoped>
  .upload-manager { padding: 8px; }
  </style>
  