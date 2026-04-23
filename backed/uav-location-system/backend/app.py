from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import math
import random
import requests
import numpy as np
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
import os
import time
import logging

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# 百度地图API配置
BAIDU_MAP_AK = "B1d61NuTDv2wqQAI9FiDKQJjzAdfE2aE"


class RealDroneLocationClustering:
    def __init__(self):
        pass

    def perform_clustering(self, locations, n_clusters=5):
        """使用真实K-means算法进行聚类"""
        logger.info(f"开始聚类分析，数据点数量: {len(locations)}, 聚类数: {n_clusters}")

        if len(locations) < n_clusters:
            n_clusters = len(locations)

        # 提取经纬度坐标
        coordinates = np.array([[loc['lng'], loc['lat']] for loc in locations])

        # 使用scikit-learn的K-means
        kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
        labels = kmeans.fit_predict(coordinates)
        centers = kmeans.cluster_centers_

        # 计算轮廓系数
        if len(set(labels)) > 1:
            silhouette_avg = silhouette_score(coordinates, labels)
        else:
            silhouette_avg = 0

        # 计算每个聚类的半径和覆盖范围
        clusters = []
        for i in range(n_clusters):
            cluster_points = [locations[j] for j in range(len(locations)) if labels[j] == i]
            cluster_coords = coordinates[labels == i]

            center_lng, center_lat = centers[i]

            # 计算聚类半径（最大距离）
            if len(cluster_coords) > 0:
                distances = [self.haversine_distance(center_lat, center_lng, coord[1], coord[0])
                             for coord in cluster_coords]
                max_distance = max(distances) if distances else 0
                avg_distance = sum(distances) / len(distances) if distances else 0
            else:
                max_distance = 0
                avg_distance = 0

            # 分析聚类内POI类型分布
            type_distribution = {}
            for point in cluster_points:
                poi_type = point.get('type', '未知')
                type_distribution[poi_type] = type_distribution.get(poi_type, 0) + 1

            clusters.append({
                'clusterId': i,
                'center': {
                    'lng': float(center_lng),
                    'lat': float(center_lat)
                },
                'points': cluster_points,
                'pointCount': len(cluster_points),
                'maxDistance': round(max_distance, 2),
                'avgDistance': round(avg_distance, 2),
                'typeDistribution': type_distribution,
                'recommendation': self.generate_detailed_recommendation(i, len(cluster_points), max_distance,
                                                                        type_distribution)
            })

        logger.info(f"聚类分析完成，轮廓系数: {silhouette_avg:.3f}")
        return {
            'clusters': clusters,
            'metrics': {
                'silhouetteScore': float(silhouette_avg),
                'totalPoints': len(locations),
                'clusterCount': n_clusters,
                'inertia': float(kmeans.inertia_)
            }
        }

    def haversine_distance(self, lat1, lon1, lat2, lon2):
        """计算两个经纬度坐标之间的Haversine距离（公里）"""
        R = 6371.0

        lat1_rad = math.radians(lat1)
        lon1_rad = math.radians(lon1)
        lat2_rad = math.radians(lat2)
        lon2_rad = math.radians(lon2)

        dlon = lon2_rad - lon1_rad
        dlat = lat2_rad - lat1_rad

        a = math.sin(dlat / 2) ** 2 + math.cos(lat1_rad) * math.cos(lat2_rad) * math.sin(dlon / 2) ** 2
        c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

        return R * c

    def generate_detailed_recommendation(self, cluster_id, point_count, max_distance, type_distribution):
        """生成详细的选址建议"""
        recommendation = f"聚类{cluster_id + 1}: "

        if point_count > 15:
            recommendation += f"高密度区域({point_count}个点)，"
        elif point_count > 8:
            recommendation += f"中等密度区域({point_count}个点)，"
        else:
            recommendation += f"低密度区域({point_count}个点)，"

        if max_distance > 5:
            recommendation += f"覆盖半径{max_distance}km，"
        else:
            recommendation += f"覆盖半径{max_distance}km，"

        # 基于类型给出建议
        main_type = max(type_distribution, key=type_distribution.get) if type_distribution else "综合"
        if main_type == "医疗":
            recommendation += "医疗资源集中，建议部署急救物资无人机"
        elif main_type == "教育":
            recommendation += "教育资源密集，适合部署教育服务无人机"
        elif main_type == "商业":
            recommendation += "商业活动频繁，建议部署物流配送无人机"
        else:
            recommendation += "适合部署综合服务无人机"

        return recommendation


class BaiduMapService:
    def __init__(self):
        self.ak = BAIDU_MAP_AK
        self.base_url = "http://api.map.baidu.com/place/v2/search"

    def test_ak_validity(self):
        """测试AK是否有效"""
        test_url = f"http://api.map.baidu.com/place/v2/search?query=医院&region=南昌&output=json&ak={self.ak}"
        try:
            response = requests.get(test_url, timeout=10)
            data = response.json()
            logger.info(f"AK测试结果: {data}")
            return data.get('status') == 0
        except Exception as e:
            logger.error(f"AK测试失败: {e}")
            return False

    def get_nanchang_poi_data(self):
        """从百度地图获取南昌市真实的POI数据"""
        logger.info("开始从百度地图获取南昌POI数据...")

        # 首先测试AK有效性
        if not self.test_ak_validity():
            logger.error("百度地图AK无效，无法获取数据")
            return []

        # POI搜索关键词和分类
        search_queries = [
            {"query": "医院", "tag": "医疗"},
            {"query": "学校", "tag": "教育"},
            {"query": "商场", "tag": "商业"},
            {"query": "公园", "tag": "景点"},
            {"query": "地铁站", "tag": "交通"}
        ]

        all_pois = []

        for search in search_queries:
            try:
                logger.info(f"正在获取 {search['query']} 数据...")
                pois = self.search_poi(search["query"], search["tag"], "南昌市")
                all_pois.extend(pois)
                logger.info(f"获取 {search['query']} 数据: {len(pois)} 个")
                time.sleep(1)  # 避免请求过快
            except Exception as e:
                logger.error(f"获取 {search['query']} 数据失败: {e}")
                continue

        # 去重
        unique_pois = []
        seen_locations = set()

        for poi in all_pois:
            location_key = f"{poi['lng']:.4f},{poi['lat']:.4f}"
            if location_key not in seen_locations:
                seen_locations.add(location_key)
                unique_pois.append(poi)

        logger.info(f"总共获取到 {len(unique_pois)} 个唯一POI")
        return unique_pois

    def search_poi(self, query, tag, region="南昌市", page_size=10):
        """搜索POI数据"""
        pois = []

        try:
            params = {
                'query': query,
                'tag': tag,
                'region': region,
                'output': 'json',
                'ak': self.ak,
                'page_size': min(page_size, 10),
                'page_num': 0
            }

            logger.info(f"请求百度API: {params}")
            response = requests.get(self.base_url, params=params, timeout=10)
            data = response.json()
            logger.info(f"百度API响应: {data}")

            if data.get('status') == 0:
                results = data.get('results', [])
                for result in results:
                    poi_data = self.parse_poi_data(result, tag)
                    if poi_data:
                        pois.append(poi_data)
            else:
                error_msg = data.get('message', '未知错误')
                logger.error(f"百度API错误: {error_msg}, 状态码: {data.get('status')}")

        except Exception as e:
            logger.error(f"搜索POI失败: {e}")

        return pois

    def parse_poi_data(self, result, default_type):
        """解析POI数据"""
        try:
            location = result.get('location', {})
            if not location:
                return None

            name = result.get('name', '').strip()
            address = result.get('address', '')
            lng = float(location.get('lng', 0))
            lat = float(location.get('lat', 0))

            # 确保在南昌市区范围内
            if not (115.7 <= lng <= 116.1 and 28.4 <= lat <= 29.0):
                return None

            return {
                'id': hash(f"{lng}{lat}{name}"),
                'name': name,
                'lng': lng,
                'lat': lat,
                'address': address,
                'type': default_type
            }
        except Exception as e:
            logger.error(f"解析POI数据失败: {e}")
            return None


# 初始化服务
clustering = RealDroneLocationClustering()
map_service = BaiduMapService()


@app.route('/api/cluster', methods=['POST'])
def cluster_locations():
    """执行聚类分析"""
    try:
        data = request.json
        locations = data.get('locations', [])
        cluster_count = data.get('clusterCount', 5)

        if not locations:
            locations = get_real_nanchang_data()

        result = clustering.perform_clustering(locations, cluster_count)

        return jsonify({
            'success': True,
            'data': result
        })

    except Exception as e:
        logger.error(f"聚类分析失败: {e}")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500


@app.route('/api/nanchang-real-data', methods=['GET'])
def get_nanchang_real_data():
    """获取真实的南昌POI数据"""
    try:
        logger.info("接收到实时数据请求")
        # 尝试从百度地图获取实时数据
        real_data = map_service.get_nanchang_poi_data()

        if real_data and len(real_data) > 0:
            # 保存获取的数据
            save_real_data(real_data)
            logger.info(f"成功获取 {len(real_data)} 个实时POI")
            return jsonify({
                'success': True,
                'data': {
                    'city': '南昌',
                    'description': '从百度地图API实时获取的POI数据',
                    'locations': real_data,
                    'source': 'baidu_map_api',
                    'count': len(real_data),
                    'update_time': time.strftime('%Y-%m-%d %H:%M:%S')
                }
            })
        else:
            # 如果实时获取失败，使用之前保存的数据
            logger.warning("实时获取失败，使用本地数据")
            local_data = get_nanchang_data().get_json()
            local_data['data']['source'] = 'local_fallback'
            local_data['data']['description'] = '本地数据（实时获取失败）'
            return jsonify(local_data)

    except Exception as e:
        logger.error(f"获取真实数据失败: {e}")
        return jsonify({
            'success': False,
            'error': f'获取实时数据失败: {str(e)}',
            'data': get_nanchang_data().get_json()
        })


@app.route('/api/nanchang-data', methods=['GET'])
def get_nanchang_data():
    """获取南昌数据（备用）"""
    try:
        with open('data/nanchang_real_locations.json', 'r', encoding='utf-8') as f:
            data = json.load(f)

        return jsonify({
            'success': True,
            'data': data
        })
    except FileNotFoundError:
        # 如果文件不存在，使用默认数据
        default_data = {
            "city": "南昌",
            "description": "南昌市真实POI数据",
            "locations": load_default_real_data(),
            "source": "default_data"
        }
        return jsonify({
            'success': True,
            'data': default_data
        })


@app.route('/api/debug-ak', methods=['GET'])
def debug_ak():
    """调试AK有效性"""
    is_valid = map_service.test_ak_validity()
    return jsonify({
        'ak': BAIDU_MAP_AK,
        'is_valid': is_valid,
        'test_url': f'http://api.map.baidu.com/place/v2/search?query=医院&region=南昌&output=json&ak={BAIDU_MAP_AK}'
    })


@app.route('/api/health', methods=['GET'])
def health_check():
    ak_status = "未知"
    try:
        ak_status = "有效" if map_service.test_ak_validity() else "无效"
    except:
        ak_status = "检查失败"

    return jsonify({
        'status': 'healthy',
        'message': 'Flask服务器运行正常',
        'clustering': '使用真实K-means算法',
        'baidu_ak_status': ak_status,
        'endpoints': {
            '/api/health': '健康检查',
            '/api/debug-ak': '调试AK',
            '/api/nanchang-data': '获取本地数据',
            '/api/nanchang-real-data': '获取实时数据',
            '/api/cluster': '聚类分析'
        }
    })


@app.route('/')
def index():
    return jsonify({
        'message': '无人机聚类选址后端服务 - 调试版本',
        'version': '2.1',
        'features': [
            '真实百度地图POI数据',
            'scikit-learn K-means聚类',
            '实时数据获取',
            '详细错误日志'
        ]
    })


def get_real_nanchang_data():
    """获取真实南昌数据"""
    try:
        with open('data/nanchang_real_locations.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
        return data['locations']
    except FileNotFoundError:
        return load_default_real_data()


def load_default_real_data():
    """加载默认的真实数据"""
    return [
        [
            {"id": 1, "name": "江西省人民医院", "lng": 115.879606, "lat": 28.711791, "type": "医疗"},
            {"id": 2, "name": "南昌大学第一附属医院", "lng": 115.868863, "lat": 28.597035, "type": "医疗"},
            {"id": 3, "name": "南昌大学", "lng": 115.807177, "lat": 28.661912, "type": "教育"},
            {"id": 4, "name": "江西师范大学", "lng": 116.038100, "lat": 28.685261, "type": "教育"},
            {"id": 5, "name": "八一广场", "lng": 115.910978, "lat": 28.680175, "type": "景点"},
            {"id": 6, "name": "滕王阁", "lng": 115.887051, "lat": 28.686511, "type": "景点"},
            {"id": 7, "name": "南昌西站", "lng": 115.799611, "lat": 28.628861, "type": "交通"},
            {"id": 8, "name": "南昌火车站", "lng": 115.926112,"lat": 28.668320, "type": "交通"},
            {"id": 9, "name": "昌北机场", "lng": 115.918639, "lat": 28.864072, "type": "交通"},
            {"id": 10, "name": "红谷滩万达广场", "lng": 115.855417, "lat": 28.698629, "type": "商业"},
            {"id": 11, "name": "中山路天虹商场", "lng":115.898607,"lat":28.681253, "type": "商业"},
            {"id": 12, "name": "南昌市第一中学", "lng": 115.876291, "lat": 28.629699, "type": "教育"},
            {"id": 13, "name": "南昌市第二中学", "lng": 115.859801, "lat": 28.705898, "type": "教育"},
            {"id": 14, "name": "南昌市洪都中医院", "lng": 115.899238, "lat": 28.683892, "type": "医疗"},
            {"id": 15, "name": "南昌大学第二附属医院", "lng": 115.842114, "lat": 28.667088, "type": "医疗"},
            {"id": 16, "name": "秋水广场", "lng": 115.869658, "lat": 28.688643, "type": "景点"},
            {"id": 17, "name": "中山路商业街", "lng": 115.898978, "lat": 28.681788, "type": "商业"},
            {"id": 18, "name": "八一公园", "lng": 115.903784, "lat": 28.684104, "type": "景点"},
            {"id": 19, "name": "江西省儿童医院", "lng": 115.902300, "lat": 28.693298, "type": "医疗"},
            {"id": 20, "name": "南昌市公安局", "lng": 115.867836, "lat": 28.706614, "type": "政府"}
        ]
    ]


def save_real_data(data):
    """保存真实数据到文件"""
    os.makedirs('data', exist_ok=True)
    with open('data/nanchang_real_locations.json', 'w', encoding='utf-8') as f:
        json.dump({
            'city': '南昌',
            'description': '从百度地图获取的真实POI数据',
            'locations': data,
            'update_time': time.strftime('%Y-%m-%d %H:%M:%S')
        }, f, ensure_ascii=False, indent=2)


if __name__ == '__main__':
    print("=" * 60)
    print("无人机聚类选址 Flask 服务器 - 调试版本")
    print("=" * 60)
    print("使用真实百度地图数据和scikit-learn K-means算法")
    print("访问地址: http://localhost:5000")
    print("调试端点:")
    print("  GET  /api/debug-ak - 检查AK有效性")
    print("  GET  /api/health - 健康检查")
    print("  GET  /api/nanchang-data - 获取本地数据")
    print("  GET  /api/nanchang-real-data - 获取实时数据")
    print("  POST /api/cluster - 聚类分析")
    print("=" * 60)

    # 确保数据文件存在
    if not os.path.exists('data/nanchang_real_locations.json'):
        print("初始化真实数据...")
        real_data = load_default_real_data()
        save_real_data(real_data)
        print(f"已创建包含 {len(real_data)} 个真实POI的数据文件")

    app.run(debug=True, host='0.0.0.0', port=5000)
