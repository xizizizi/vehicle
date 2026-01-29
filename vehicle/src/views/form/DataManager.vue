<template>
  <div class="data-manager">
    <div class="header">
      <h2 class="title">🚀 巡查数据管理平台</h2>
      <div class="actions">
        <el-button type="primary" @click="showUpload = true" class="neon-btn"
          >上传巡查数据</el-button
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

    <!-- 上传弹窗 -->
    <el-dialog
      title="上传巡查数据"
      :visible.sync="showUpload"
      width="600px"
      class="dark-dialog"
      @opened="onDialogOpened"
    >
      <el-form :model="uploadForm" label-width="100px">
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
      showUpload: false,
      uploadForm: { taskName: "", droneId: "", description: "" },
      fileList: [],
      showDetail: false,
      detail: null,
    };
  },
  mounted() {
    this.fetchList();
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
  },
  methods: {
    // 在 addGlobalStyles 方法中更新样式
    addGlobalStyles() {
      if (document.getElementById("data-manager-styles")) return;

      const style = document.createElement("style");
      style.id = "data-manager-styles";
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
    },

    async fetchList() {
      const params = {
        taskName: this.filters.taskName || undefined,
        droneId: this.filters.droneId || undefined,
        offset: (this.page - 1) * this.limit,
        limit: this.limit,
      };
      if (this.filters.range?.length === 2) {
        params.startTime = this.filters.range[0];
        params.endTime = this.filters.range[1];
      }
      const res = await axios.get("/api/reports/data/list", { params });
      // 前端排序：按 id 升序
      let data = res.data.list || res.data;
      data = data.sort((a, b) => a.id - b.id);
      this.list = res.data.list || res.data;
      this.total = res.data.total || 1000;
    },
    resetFilters() {
      this.filters = { taskName: "", droneId: "", range: null };
      this.fetchList();
    },
    onPageChange(p) {
      this.page = p;
      this.fetchList();
    },
    // 上传前验证
    beforeFile(file) {
      return true;
    },
    onChange(file, fileList) {
      this.fileList = fileList;
    },
    onRemove(file, fileList) {
      this.fileList = fileList;
    },

    // 上传方法
    async submitUpload() {
      if (!this.fileList.length) return this.$message.warning("请选择文件");

      const form = new FormData();
      form.append("taskName", this.uploadForm.taskName);
      form.append("droneId", this.uploadForm.droneId);
      form.append("description", this.uploadForm.description);
      form.append("uploaderId", 1);

      this.fileList.forEach((f) => {
        console.log("上传文件:", f.raw.name); // 调试文件名
        form.append("files", f.raw);
      });

      try {
        await axios.post("/api/reports/data/upload", form, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        this.$message.success("上传成功");
        this.showUpload = false;
        this.fetchList();
      } catch (e) {
        console.error("上传失败:", e);
        this.$message.error("上传失败");
      }
    },

    // 下载单个文件
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

    // 下载所有文件
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
  },
};
</script>

<style scoped>
/* 在 <style scoped> 部分更新以下样式 */
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
</style>