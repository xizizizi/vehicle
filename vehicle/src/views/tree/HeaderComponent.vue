<template>
  <div class="header">
    <div class="container">
      <div class="row align-items-center">
        <div class="col-md-8">
          <h1><i class="fas fa-map-marker-alt me-2"></i>南昌POI数据选址系统</h1>
          <p>基于百度地图API的南昌市兴趣点数据可视化与选址平台</p>
        </div>
        <div class="col-md-4 text-md-end">
          <div class="text-white-50">数据更新时间: {{ currentTime }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
  
  <script>
export default {
  name: "HeaderComponent",
  data() {
    return {
      currentTime: "",
    };
  },
  mounted() {
    this.updateTime();
    // 每60秒更新时间
    this.timer = setInterval(this.updateTime, 60000);
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer);
    }
  },
  methods: {
    updateTime() {
      const now = new Date();
      this.currentTime = now
        .toLocaleString("zh-CN", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
          hour12: false,
        })
        .replace(/\//g, "-");
    },
  },
};
</script>
  
  <style scoped>
.header {
  background: linear-gradient(135deg, #1e5799 0%, #207cca 100%);
  color: white;
  padding: 1.5rem 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header h1 {
  font-weight: 700;
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
}

.header p {
  opacity: 0.9;
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .header h1 {
    font-size: 1.4rem;
  }

  .header p {
    font-size: 0.9rem;
  }
}
</style>