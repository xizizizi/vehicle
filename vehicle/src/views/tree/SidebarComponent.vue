<template>
    <div class="sidebar">
      <div class="filter-section">
        <h5 class="filter-title"><i class="fas fa-filter me-2"></i>类型筛选</h5>
        <div class="filter-buttons">
          <button 
            v-for="type in poiTypes" 
            :key="type.value"
            class="filter-btn"
            :class="[type.class, { active: activeTypes.includes(type.value) }]"
            @click="toggleFilter(type.value)"
          >
            <i :class="type.icon" class="me-1"></i> {{ type.label }}
          </button>
        </div>
        <div class="input-group mb-3">
          <input 
            type="text" 
            class="form-control" 
            placeholder="搜索地点名称..." 
            v-model="searchQuery"
            @keyup.enter="performSearch"
          >
          <button class="btn btn-outline-primary" type="button" @click="performSearch">
            <i class="fas fa-search"></i>
          </button>
        </div>
      </div>
      
      <div class="filter-section">
        <h5 class="filter-title"><i class="fas fa-list me-2"></i>POI列表</h5>
        <div class="poi-list" v-if="filteredLocations.length > 0">
          <div 
            v-for="location in filteredLocations" 
            :key="location.id"
            class="poi-item"
            @click="$emit('select-location', location)"
            :class="{ active: selectedLocation && selectedLocation.id === location.id }"
          >
            <div class="poi-name">{{ location.name }}</div>
            <div class="poi-address">{{ location.address }}</div>
            <div>
              <span class="poi-type" :class="`${location.type}-badge`">
                <i :class="typeIcons[location.type]" class="me-1"></i>{{ location.type }}
              </span>
            </div>
          </div>
        </div>
        <div v-else class="no-data">
          <i class="fas fa-search fa-2x mb-2"></i>
          <p>未找到匹配的地点</p>
        </div>
      </div>
      
      <div v-if="selectedLocation" class="selected-location">
        <h6 class="selected-title"><i class="fas fa-check-circle me-2"></i>已选位置</h6>
        <div>
          <div class="poi-name">{{ selectedLocation.name }}</div>
          <div class="poi-address">{{ selectedLocation.address }}</div>
          <div class="mt-2">
            <span class="poi-type" :class="`${selectedLocation.type}-badge`">
              <i :class="typeIcons[selectedLocation.type]" class="me-1"></i>{{ selectedLocation.type }}
            </span>
          </div>
          <div class="mt-2">
            <small><strong>坐标:</strong> {{ selectedLocation.lat.toFixed(6) }}, {{ selectedLocation.lng.toFixed(6) }}</small>
          </div>
        </div>
        <div class="action-buttons">
          <button class="btn btn-primary action-btn" @click="$emit('route-plan')">
            <i class="fas fa-route me-1"></i> 路径规划
          </button>
          <button class="btn btn-outline-secondary action-btn" @click="$emit('clear-selection')">
            <i class="fas fa-times me-1"></i> 清除
          </button>
        </div>
      </div>
      
      <div class="stats-section">
        <h5 class="filter-title"><i class="fas fa-chart-bar me-2"></i>数据统计</h5>
        <div>
          <div v-for="(count, type) in typeCounts" :key="type" class="stat-item">
            <span>{{ type }}:</span>
            <span class="stat-value">{{ count }} 个</span>
          </div>
          <div class="stat-item">
            <span><strong>总计:</strong></span>
            <span class="stat-value"><strong>{{ totalCount }} 个</strong></span>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script>
  // 模拟POI数据
  const nanchangPOIData = {
    "data": {
      "city": "南昌",
      "count": 50,
      "description": "从百度地图API实时获取的POI数据",
      "locations": [
        {
          "address": "江西省南昌市南昌县东岳大道1519号",
          "id": 6600513869935474052,
          "lat": 28.597035,
          "lng": 115.868863,
          "name": "南昌大学第一附属医院(象湖院区)",
          "type": "医疗"
        },
        {
          "address": "江西省南昌市东湖区永外正街17号",
          "id": 6600513869935474053,
          "lat": 28.689578,
          "lng": 115.897229,
          "name": "南昌大学第一附属医院(东湖院区)",
          "type": "医疗"
        },
        {
          "address": "江西省南昌市东湖区八一大道463号",
          "id": 6600513869935474054,
          "lat": 28.674892,
          "lng": 115.904367,
          "name": "南昌大学第二附属医院",
          "type": "医疗"
        },
        {
          "address": "江西省南昌市青山湖区南京西路181号",
          "id": 6600513869935474055,
          "lat": 28.688367,
          "lng": 115.927756,
          "name": "江西省人民医院",
          "type": "医疗"
        },
        {
          "address": "江西省南昌市东湖区民德路1号",
          "id": 6600513869935474056,
          "lat": 28.681945,
          "lng": 115.892578,
          "name": "南昌市第一医院",
          "type": "医疗"
        },
        {
          "address": "江西省南昌市红谷滩区学府大道999号",
          "id": 6600513869935474057,
          "lat": 28.663356,
          "lng": 115.806945,
          "name": "南昌大学",
          "type": "教育"
        },
        {
          "address": "江西省南昌市青山湖区北京东路339号",
          "id": 6600513869935474058,
          "lat": 28.678923,
          "lng": 115.952367,
          "name": "江西师范大学",
          "type": "教育"
        },
        {
          "address": "江西省南昌市青山湖区双港东大街169号",
          "id": 6600513869935474059,
          "lat": 28.749856,
          "lng": 115.857234,
          "name": "华东交通大学",
          "type": "教育"
        },
        {
          "address": "江西省南昌市红谷滩区丰和南大道696号",
          "id": 6600513869935474060,
          "lat": 28.648912,
          "lng": 115.826745,
          "name": "南昌航空大学",
          "type": "教育"
        },
        {
          "address": "江西省南昌市新建区长征西路",
          "id": 6600513869935474061,
          "lat": 28.692345,
          "lng": 115.817634,
          "name": "南昌西站",
          "type": "交通"
        },
        {
          "address": "江西省南昌市西湖区站前路",
          "id": 6600513869935474062,
          "lat": 28.662189,
          "lng": 115.908756,
          "name": "南昌站",
          "type": "交通"
        },
        {
          "address": "江西省南昌市青云谱区井冈山大道",
          "id": 6600513869935474063,
          "lat": 28.635678,
          "lng": 115.912345,
          "name": "徐坊客运站",
          "type": "交通"
        },
        {
          "address": "江西省南昌市东湖区八一大道",
          "id": 6600513869935474064,
          "lat": 28.681234,
          "lng": 115.893456,
          "name": "八一广场",
          "type": "景点"
        },
        {
          "address": "江西省南昌市东湖区仿古街58号",
          "id": 6600513869935474065,
          "lat": 28.688945,
          "lng": 115.889067,
          "name": "滕王阁",
          "type": "景点"
        },
        {
          "address": "江西省南昌市西湖区中山路",
          "id": 6600513869935474066,
          "lat": 28.679834,
          "lng": 115.895678,
          "name": "中山路商业街",
          "type": "商业"
        },
        {
          "address": "江西省南昌市红谷滩区会展路",
          "id": 6600513869935474067,
          "lat": 28.674523,
          "lng": 115.852189,
          "name": "红谷滩万达广场",
          "type": "商业"
        }
      ],
      "source": "baidu_map_api",
      "update_time": "2025-11-26 15:40:49"
    },
    "success": true
  }
  
  export default {
    name: 'SidebarComponent',
    props: {
      selectedLocation: Object
    },
    data() {
      return {
        poiData: nanchangPOIData.data.locations,
        activeTypes: ['医疗', '教育', '商业', '景点', '交通'],
        searchQuery: '',
        poiTypes: [
          { value: '医疗', label: '医疗', class: 'medical', icon: 'fas fa-hospital' },
          { value: '教育', label: '教育', class: 'education', icon: 'fas fa-school' },
          { value: '商业', label: '商业', class: 'commercial', icon: 'fas fa-shopping-cart' },
          { value: '景点', label: '景点', class: 'attraction', icon: 'fas fa-tree' },
          { value: '交通', label: '交通', class: 'transport', icon: 'fas fa-subway' }
        ],
        typeIcons: {
          '医疗': 'fas fa-hospital',
          '教育': 'fas fa-school',
          '商业': 'fas fa-shopping-cart',
          '景点': 'fas fa-tree',
          '交通': 'fas fa-subway'
        }
      }
    },
    computed: {
      filteredLocations() {
        return this.poiData.filter(location => {
          const typeMatch = this.activeTypes.includes(location.type)
          const searchMatch = !this.searchQuery || 
            location.name.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            location.address.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            location.type.toLowerCase().includes(this.searchQuery.toLowerCase())
          return typeMatch && searchMatch
        })
      },
      typeCounts() {
        const counts = {}
        this.poiData.forEach(location => {
          counts[location.type] = (counts[location.type] || 0) + 1
        })
        return counts
      },
      totalCount() {
        return this.poiData.length
      }
    },
    methods: {
      toggleFilter(type) {
        const index = this.activeTypes.indexOf(type)
        if (index > -1) {
          this.activeTypes.splice(index, 1)
        } else {
          this.activeTypes.push(type)
        }
      },
      performSearch() {
        if (this.searchQuery.trim()) {
          const found = this.poiData.find(location => 
            location.name.toLowerCase().includes(this.searchQuery.toLowerCase()) || 
            location.address.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
            location.type.toLowerCase().includes(this.searchQuery.toLowerCase())
          )
          if (found) {
            this.$emit('select-location', found)
          } else {
            alert('未找到匹配的地点')
          }
        }
      }
    }
  }
  </script>
  
  <style scoped>
  .sidebar {
    width: 320px;
    background-color: white;
    box-shadow: 2px 0 8px rgba(0,0,0,0.1);
    padding: 1.5rem;
    overflow-y: auto;
    z-index: 1000;
  }
  
  .filter-section {
    margin-bottom: 1.5rem;
  }
  
  .filter-title {
    font-weight: 600;
    margin-bottom: 0.75rem;
    color: #333;
    border-bottom: 1px solid #eee;
    padding-bottom: 0.5rem;
  }
  
  .filter-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-bottom: 1rem;
  }
  
  .filter-btn {
    border: none;
    border-radius: 20px;
    padding: 6px 12px;
    font-size: 0.85rem;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    gap: 5px;
    cursor: pointer;
  }
  
  .filter-btn.active {
    color: white;
  }
  
  .filter-btn.medical { background-color: #e8f4fd; color: #0d6efd; }
  .filter-btn.medical.active { background-color: #0d6efd; }
  
  .filter-btn.education { background-color: #e7f7ed; color: #198754; }
  .filter-btn.education.active { background-color: #198754; }
  
  .filter-btn.commercial { background-color: #fff3cd; color: #ffc107; }
  .filter-btn.commercial.active { background-color: #ffc107; color: #000; }
  
  .filter-btn.attraction { background-color: #f8d7da; color: #dc3545; }
  .filter-btn.attraction.active { background-color: #dc3545; }
  
  .filter-btn.transport { background-color: #e2e3e5; color: #6c757d; }
  .filter-btn.transport.active { background-color: #6c757d; }
  
  .poi-list {
    max-height: 400px;
    overflow-y: auto;
  }
  
  .poi-item {
    border: none;
    border-bottom: 1px solid #eee;
    padding: 12px 15px;
    transition: all 0.2s;
    cursor: pointer;
  }
  
  .poi-item:hover {
    background-color: #f8f9fa;
  }
  
  .poi-item.active {
    background-color: #e3f2fd;
    border-left: 3px solid #2196f3;
  }
  
  .poi-item:last-child {
    border-bottom: none;
  }
  
  .poi-name {
    font-weight: 600;
    margin-bottom: 4px;
    font-size: 0.95rem;
  }
  
  .poi-address {
    font-size: 0.8rem;
    color: #666;
    margin-bottom: 6px;
    line-height: 1.3;
  }
  
  .poi-type {
    display: inline-block;
    padding: 3px 8px;
    border-radius: 4px;
    font-size: 0.75rem;
    margin-right: 8px;
  }
  
  .medical-badge { background-color: #e8f4fd; color: #0d6efd; }
  .education-badge { background-color: #e7f7ed; color: #198754; }
  .commercial-badge { background-color: #fff3cd; color: #856404; }
  .attraction-badge { background-color: #f8d7da; color: #721c24; }
  .transport-badge { background-color: #e2e3e5; color: #383d41; }
  
  .selected-location {
    background-color: #e3f2fd;
    border-left: 4px solid #1e88e5;
    padding: 15px;
    margin-top: 1.5rem;
    border-radius: 8px;
  }
  
  .selected-title {
    font-weight: 600;
    margin-bottom: 10px;
    color: #1e88e5;
  }
  
  .action-buttons {
    display: flex;
    gap: 10px;
    margin-top: 1.5rem;
  }
  
  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-size: 0.85rem;
    padding: 8px 12px;
  }
  
  .stats-section {
    margin-top: 1.5rem;
    padding-top: 1rem;
    border-top: 1px solid #eee;
  }
  
  .stat-item {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 0.9rem;
  }
  
  .stat-value {
    font-weight: 600;
    color: #1e88e5;
  }
  
  .no-data {
    text-align: center;
    padding: 2rem 1rem;
    color: #6c757d;
  }
  
  .no-data i {
    opacity: 0.5;
  }
  
  .no-data p {
    margin: 0;
    font-size: 0.9rem;
  }
  
  @media (max-width: 768px) {
    .sidebar {
      width: 100%;
      max-height: 40vh;
    }
  }
  </style>