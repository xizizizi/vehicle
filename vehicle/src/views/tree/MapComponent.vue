<template>
  <div class="map-container">
    <div id="map" ref="mapContainer"></div>

    <div class="legend">
      <div class="legend-title">图例</div>
      <div class="legend-item">
        <div class="legend-color" style="background-color: #0d6efd"></div>
        <span>医疗设施</span>
      </div>
      <div class="legend-item">
        <div class="legend-color" style="background-color: #198754"></div>
        <span>教育机构</span>
      </div>
      <div class="legend-item">
        <div class="legend-color" style="background-color: #ffc107"></div>
        <span>商业场所</span>
      </div>
      <div class="legend-item">
        <div class="legend-color" style="background-color: #dc3545"></div>
        <span>景点公园</span>
      </div>
      <div class="legend-item">
        <div class="legend-color" style="background-color: #6c757d"></div>
        <span>交通设施</span>
      </div>
    </div>
  </div>
</template>
  
  <script>
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// POI数据 - 与SidebarComponent中相同
const nanchangPOIData = {
  data: {
    city: "南昌",
    count: 50,
    description: "从百度地图API实时获取的POI数据",
    locations: [
      // 这里放入完整的POI数据
      // 由于数据较长，这里省略，实际使用时需要完整复制
    ],
    source: "baidu_map_api",
    update_time: "2025-11-26 15:40:49",
  },
  success: true,
};

export default {
  name: "MapComponent",
  props: {
    selectedLocation: Object,
  },
  data() {
    return {
      map: null,
      markers: [],
      selectedMarker: null,
      typeColors: {
        医疗: "#0d6efd",
        教育: "#198754",
        商业: "#ffc107",
        景点: "#dc3545",
        交通: "#6c757d",
      },
    };
  },
  mounted() {
    this.initMap();
    this.initPOIData();
  },
  watch: {
    selectedLocation(newLocation) {
      this.highlightSelectedLocation(newLocation);
    },
  },
  methods: {
    initMap() {
      this.map = L.map(this.$refs.mapContainer).setView([28.68, 115.89], 12);

      L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      }).addTo(this.map);
    },
    initPOIData() {
      const locations = nanchangPOIData.data.locations;

      // 清空现有标记
      this.markers.forEach((marker) => this.map.removeLayer(marker));
      this.markers = [];

      // 创建标记
      locations.forEach((location) => {
        const marker = L.marker([location.lat, location.lng], {
          icon: L.divIcon({
            className: "custom-marker",
            html: `<div style="background-color: ${
              this.typeColors[location.type]
            }; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
            iconSize: [16, 16],
            iconAnchor: [8, 8],
          }),
        }).addTo(this.map);

        // 添加弹出窗口
        marker.bindPopup(`
            <div class="popup-content">
              <h6>${location.name}</h6>
              <p class="mb-1"><strong>类型:</strong> ${location.type}</p>
              <p class="mb-1"><strong>地址:</strong> ${location.address}</p>
              <p class="mb-0"><strong>坐标:</strong> ${location.lat.toFixed(
                6
              )}, ${location.lng.toFixed(6)}</p>
              <button class="btn btn-sm btn-primary mt-2 w-100 select-location-btn" data-id="${
                location.id
              }">
                <i class="fas fa-check me-1"></i> 选择此位置
              </button>
            </div>
          `);

        // 存储标记和位置数据
        marker.locationData = location;
        this.markers.push(marker);

        // 添加点击事件
        marker.on("click", () => {
          this.$emit("location-selected", location);
        });

        // 为弹出窗口中的按钮添加事件
        marker.on("popupopen", () => {
          const popup = marker.getPopup();
          const popupElement = popup.getElement();
          const selectButton = popupElement.querySelector(
            ".select-location-btn"
          );
          if (selectButton) {
            selectButton.addEventListener("click", () => {
              this.$emit("location-selected", location);
            });
          }
        });
      });
    },
    highlightSelectedLocation(location) {
      // 清除之前选中的标记
      if (this.selectedMarker) {
        this.selectedMarker.setIcon(
          L.divIcon({
            className: "custom-marker",
            html: `<div style="background-color: ${
              this.typeColors[this.selectedMarker.locationData.type]
            }; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
            iconSize: [16, 16],
            iconAnchor: [8, 8],
          })
        );
      }

      if (location) {
        // 找到对应的标记
        this.selectedMarker = this.markers.find(
          (marker) => marker.locationData.id === location.id
        );

        if (this.selectedMarker) {
          // 高亮选中的标记
          this.selectedMarker.setIcon(
            L.divIcon({
              className: "custom-marker selected",
              html: `<div style="background-color: #e53935; width: 16px; height: 16px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 6px rgba(0,0,0,0.4);"></div>`,
              iconSize: [22, 22],
              iconAnchor: [11, 11],
            })
          );

          // 居中显示选中的标记
          this.map.setView([location.lat, location.lng], 15);
        }
      } else {
        this.selectedMarker = null;
      }
    },
    beforeDestroy() {
      if (this.map) {
        this.map.remove();
      }
    },
  },
};
</script>
  
  <style scoped>
.map-container {
  flex: 1;
  position: relative;
}

#map {
  height: 100%;
  width: 100%;
}

.legend {
  position: absolute;
  bottom: 20px;
  right: 20px;
  background: white;
  padding: 10px 15px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  z-index: 1000;
}

.legend-title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 0.8rem;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

@media (max-width: 768px) {
  .map-container {
    height: 60vh;
  }
}
</style>