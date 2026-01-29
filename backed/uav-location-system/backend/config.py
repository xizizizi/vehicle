# 百度地图API配置 - 您需要申请百度地图开发者账号获取AK
BAIDU_MAP_AK = "t12Jbls6aC4P07QJ98Z7Ke0e4lbUpnxX"

# 应用配置
DEBUG = True
HOST = '0.0.0.0'
PORT = 5000

# 南昌市地理范围
NANCHANG_BOUNDS = {
    "min_lng": 115.700,
    "max_lng": 116.100,
    "min_lat": 28.400,
    "max_lat": 29.000
}

# POI类型配置
POI_CATEGORIES = {
    "hospital": {
        "name": "医院",
        "weight": 10,
        "tags": ["医院", "三甲医院", "综合医院", "专科医院"]
    },
    "fire_station": {
        "name": "消防站",
        "weight": 9,
        "tags": ["消防", "消防站", "消防队"]
    },
    "police_station": {
        "name": "公安局",
        "weight": 8,
        "tags": ["公安局", "派出所", "警察局"]
    },
    "university": {
        "name": "大学",
        "weight": 7,
        "tags": ["大学", "学院", "高校"]
    },
    "school": {
        "name": "学校",
        "weight": 6,
        "tags": ["中学", "高中", "初中", "小学"]
    },
    "subway": {
        "name": "地铁站",
        "weight": 5,
        "tags": ["地铁站", "地铁"]
    },
    "shopping": {
        "name": "商场",
        "weight": 4,
        "tags": ["商场", "购物中心", "百货"]
    },
    "park": {
        "name": "公园",
        "weight": 3,
        "tags": ["公园", "广场"]
    }
}