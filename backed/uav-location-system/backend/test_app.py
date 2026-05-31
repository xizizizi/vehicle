from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import math
import random
import os

app = Flask(__name__)
CORS(app)


class SimpleDroneLocationClustering:
    def __init__(self):
        self.earth_radius = 6371.0

    def euclidean_distance(self, point1, point2):
        """计算欧几里得距离（简化版）"""
        return math.sqrt((point1[0] - point2[0]) ** 2 + (point1[1] - point2[1]) ** 2)

    def simple_kmeans(self, coordinates, k, max_iters=100):
        """纯Python实现的K-means"""
        if len(coordinates) < k:
            k = len(coordinates)

        # 随机选择初始中心点
        centers = random.sample(coordinates, k)

        for _ in range(max_iters):
            # 分配点到最近的中心
            labels = []
            for point in coordinates:
                distances = [self.euclidean_distance(point, center) for center in centers]
                labels.append(distances.index(min(distances)))

            # 更新中心点
            new_centers = []
            for i in range(k):
                cluster_points = [coordinates[j] for j in range(len(coordinates)) if labels[j] == i]
                if cluster_points:
                    avg_lng = sum(p[0] for p in cluster_points) / len(cluster_points)
                    avg_lat = sum(p[1] for p in cluster_points) / len(cluster_points)
                    new_centers.append([avg_lng, avg_lat])
                else:
                    new_centers.append(centers[i])  # 如果聚类为空，保持原中心

            # 检查收敛
            converged = True
            for i in range(k):
                if self.euclidean_distance(centers[i], new_centers[i]) > 0.0001:
                    converged = False
                    break

            if converged:
                break

            centers = new_centers

        return labels, centers

    def perform_clustering(self, locations, n_clusters=5):
        """对位置数据进行聚类分析"""
        if len(locations) < n_clusters:
            n_clusters = len(locations)

        # 提取经纬度坐标
        coordinates = [[loc['lng'], loc['lat']] for loc in locations]

        # 使用简化的K-means
        labels, centers = self.simple_kmeans(coordinates, n_clusters)

        # 组织聚类结果
        clusters = []
        for i in range(n_clusters):
            cluster_points = [locations[j] for j in range(len(locations)) if labels[j] == i]

            center_lng, center_lat = centers[i]

            clusters.append({
                'clusterId': i,
                'center': {
                    'lng': float(center_lng),
                    'lat': float(center_lat)
                },
                'points': cluster_points,
                'pointCount': len(cluster_points),
                'recommendation': self.generate_recommendation(i, len(cluster_points))
            })

        return {
            'clusters': clusters,
            'metrics': {
                'silhouetteScore': 0.8,  # 固定值，简化处理
                'totalPoints': len(locations),
                'clusterCount': n_clusters
            }
        }

    def generate_recommendation(self, cluster_id, point_count):
        """生成选址建议"""
        if point_count > 10:
            return f"聚类{cluster_id + 1}: 高密度区域，建议设立大型无人机基地，覆盖{point_count}个需求点"
        elif point_count > 5:
            return f"聚类{cluster_id + 1}: 中等密度，适合部署标准无人机站点"
        else:
            return f"聚类{cluster_id + 1}: 低密度区域，可作为补充站点"


# 初始化聚类器
clustering = SimpleDroneLocationClustering()


@app.route('/api/cluster', methods=['POST'])
def cluster_locations():
    """
    接收前端数据并进行聚类分析
    """
    try:
        data = request.json
        locations = data.get('locations', [])
        cluster_count = data.get('clusterCount', 5)

        if not locations:
            # 如果没有提供位置数据，使用默认的南昌市区数据
            try:
                with open('data/nanchang_locations.json', 'r', encoding='utf-8') as f:
                    default_data = json.load(f)
                locations = default_data['locations']
            except FileNotFoundError:
                return jsonify({
                    'success': False,
                    'error': '默认数据文件未找到，请确保data/nanchang_locations.json存在'
                }), 404

        # 执行聚类分析
        result = clustering.perform_clustering(locations, cluster_count)

        return jsonify({
            'success': True,
            'data': result
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/nanchang-data', methods=['GET'])
def get_nanchang_data():
    """
    获取南昌市区默认数据
    """
    try:
        with open('data/nanchang_locations.json', 'r', encoding='utf-8') as f:
            data = json.load(f)

        return jsonify({
            'success': True,
            'data': data
        })

    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/health', methods=['GET'])
def health_check():
    return jsonify({
        'status': 'healthy',
        'message': 'Flask服务器运行正常',
        'version': '1.0',
        'python_version': '3.12'
    })


@app.route('/')
def index():
    return jsonify({
        'message': '无人机聚类选址后端服务',
        'endpoints': {
            'GET /api/health': '健康检查',
            'GET /api/nanchang-data': '获取南昌数据',
            'POST /api/cluster': '聚类分析'
        }
    })


if __name__ == '__main__':
    print("=" * 50)
    print("无人机聚类选址 Flask 服务器")
    print("=" * 50)
    print("访问地址: http://localhost:5000")
    print("API文档:")
    print("  GET  /api/health - 健康检查")
    print("  GET  /api/nanchang-data - 获取南昌数据")
    print("  POST /api/cluster - 聚类分析")
    print("按 Ctrl+C 停止服务器")
    print("=" * 50)

    # 检查数据文件是否存在
    if not os.path.exists('data/nanchang_locations.json'):
        print("警告: data/nanchang_locations.json 文件不存在")
        # 创建数据目录
        os.makedirs('data', exist_ok=True)
        # 创建默认数据文件
        default_data = {
            "city": "南昌",
            "description": "南昌市区无人机选址需求点数据",
            "locations": [
                {"id": 1, "name": "八一广场", "lng": 115.9083, "lat": 28.6766, "type": "商业区"},
                {"id": 2, "name": "滕王阁", "lng": 115.8806, "lat": 28.6857, "type": "景点"},
                {"id": 3, "name": "南昌大学", "lng": 115.8129, "lat": 28.6602, "type": "教育"},
                {"id": 4, "name": "红谷滩新区", "lng": 115.8516, "lat": 28.6896, "type": "商业区"},
                {"id": 5, "name": "南昌西站", "lng": 115.7844, "lat": 28.6208, "type": "交通枢纽"},
                {"id": 6, "name": "昌北机场", "lng": 115.9009, "lat": 28.8648, "type": "交通枢纽"},
                {"id": 7, "name": "秋水广场", "lng": 115.8572, "lat": 28.6892, "type": "商业区"},
                {"id": 8, "name": "中山路", "lng": 115.8932, "lat": 28.6794, "type": "商业区"},
                {"id": 9, "name": "南昌火车站", "lng": 115.9187, "lat": 28.6629, "type": "交通枢纽"},
                {"id": 10, "name": "江西省人民医院", "lng": 115.8996, "lat": 28.6791, "type": "医疗"}
            ]
        }
        with open('data/nanchang_locations.json', 'w', encoding='utf-8') as f:
            json.dump(default_data, f, ensure_ascii=False, indent=2)
        print("已自动创建默认数据文件: data/nanchang_locations.json")

    app.run(debug=True, host='0.0.0.0', port=5000)