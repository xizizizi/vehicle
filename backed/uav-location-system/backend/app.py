from flask import Flask, request, jsonify
from flask_cors import CORS
import json
import time
from datetime import datetime
import os

from config import DEBUG, HOST, PORT
from map_service import map_service
from clustering_service import clustering_service

app = Flask(__name__)
CORS(app)  # 允许跨域请求

# 创建缓存目录
os.makedirs('cache', exist_ok=True)


@app.route('/')
def index():
    """首页"""
    return jsonify({
        "message": "🚁 无人机选址聚类分析服务",
        "version": "1.0.0",
        "endpoints": {
            "/api/clustering/kmeans": "K-means聚类分析",
            "/api/poi-data": "获取POI数据",
            "/api/health": "服务健康检查"
        }
    })


@app.route('/api/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "service": "UAV Location Clustering Service"
    })


@app.route('/api/poi-data', methods=['GET'])
def get_poi_data():
    """获取POI数据"""
    try:
        use_cache = request.args.get('use_cache', 'true').lower() == 'true'
        cache_file = 'cache/nanchang_pois.json'

        if use_cache and os.path.exists(cache_file):
            # 使用缓存数据
            with open(cache_file, 'r', encoding='utf-8') as f:
                pois = json.load(f)
            message = "使用缓存数据"
        else:
            # 从百度地图获取新数据
            pois = map_service.get_nanchang_key_locations()
            # 保存到缓存
            with open(cache_file, 'w', encoding='utf-8') as f:
                json.dump(pois, f, ensure_ascii=False, indent=2)
            message = "从百度地图API获取新数据"

        # 统计类型分布
        type_stats = {}
        for poi in pois:
            poi_type = poi['type']
            type_stats[poi_type] = type_stats.get(poi_type, 0) + 1

        return jsonify({
            "success": True,
            "data": pois,
            "statistics": {
                "total_count": len(pois),
                "type_distribution": type_stats
            },
            "message": message,
            "timestamp": datetime.now().isoformat()
        })

    except Exception as e:
        return jsonify({
            "success": False,
            "message": f"获取POI数据失败: {str(e)}"
        }), 500


@app.route('/api/clustering/kmeans', methods=['POST'])
def kmeans_clustering():
    """K-means聚类分析"""
    try:
        data = request.get_json() or {}

        k = data.get('k')
        use_sample_data = data.get('use_sample_data', False)

        # 获取POI数据
        if use_sample_data:
            # 使用示例数据（避免频繁调用百度API）
            pois = get_sample_data()
        else:
            cache_file = 'cache/nanchang_pois.json'
            if os.path.exists(cache_file):
                with open(cache_file, 'r', encoding='utf-8') as f:
                    pois = json.load(f)
            else:
                pois = map_service.get_nanchang_key_locations()
                with open(cache_file, 'w', encoding='utf-8') as f:
                    json.dump(pois, f, ensure_ascii=False, indent=2)

        if not pois:
            return jsonify({
                "success": False,
                "message": "没有可用的POI数据进行聚类分析"
            }), 400

        # 执行聚类分析
        start_time = time.time()
        clustering_result = clustering_service.perform_clustering(pois, k)
        processing_time = round(time.time() - start_time, 2)

        # 构建响应
        response = {
            "success": True,
            "data": clustering_result,
            "processing_time": f"{processing_time}秒",
            "timestamp": datetime.now().isoformat()
        }

        return jsonify(response)

    except Exception as e:
        return jsonify({
            "success": False,
            "message": f"聚类分析失败: {str(e)}"
        }), 500


@app.route('/api/clustering/regional', methods=['POST'])
def regional_clustering():
    """区域化聚类分析"""
    try:
        data = request.get_json() or {}

        regions = data.get('regions', 4)
        centers_per_region = data.get('centers_per_region', 2)

        # 这里可以实现区域化聚类逻辑
        # 由于代码较长，这里返回示例响应
        return jsonify({
            "success": True,
            "message": "区域化聚类功能开发中",
            "parameters": {
                "regions": regions,
                "centers_per_region": centers_per_region
            }
        })

    except Exception as e:
        return jsonify({
            "success": False,
            "message": f"区域化聚类失败: {str(e)}"
        }), 500


def get_sample_data():
    """获取示例数据（避免频繁调用API）"""
    # 南昌市的一些示例地点
    return [
        {
            "name": "南昌大学第一附属医院",
            "lng": 115.89231,
            "lat": 28.68897,
            "address": "江西省南昌市东湖区永外正街17号",
            "type": "hospital",
            "weight": 10
        },
        {
            "name": "南昌市消防支队",
            "lng": 115.89542,
            "lat": 28.69015,
            "address": "江西省南昌市东湖区阳明路",
            "type": "fire_station",
            "weight": 9
        },
        {
            "name": "南昌大学",
            "lng": 115.81724,
            "lat": 28.65718,
            "address": "江西省南昌市红谷滩新区学府大道999号",
            "type": "university",
            "weight": 7
        },
        {
            "name": "南昌市公安局",
            "lng": 115.89912,
            "lat": 28.68105,
            "address": "江西省南昌市东湖区阳明路133号",
            "type": "police_station",
            "weight": 8
        }
    ]


if __name__ == '__main__':
    print("🚀 启动无人机选址聚类服务...")
    print(f"📍 服务地址: http://{HOST}:{PORT}")
    print("📊 请确保已配置正确的百度地图AK")

    app.run(debug=DEBUG, host=HOST, port=PORT)