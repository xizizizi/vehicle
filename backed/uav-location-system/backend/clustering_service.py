import math
import random
import logging
from geopy.distance import geodesic

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class SimpleLocationClusteringService:
    def __init__(self):
        self.earth_radius = 6371000  # 地球半径（米）

    def find_optimal_k(self, coordinates, max_k=10):
        """简化版的最优K值查找"""
        if len(coordinates) <= 2:
            return min(2, len(coordinates))

        # 简化方法：基于数据点数量自动选择K值
        n_points = len(coordinates)
        if n_points <= 10:
            return 2
        elif n_points <= 30:
            return 3
        elif n_points <= 60:
            return 4
        elif n_points <= 100:
            return 5
        else:
            return min(6, max_k)

    def kmeans_simple(self, points, k, max_iterations=100):
        """简化版K-means算法实现"""
        # 随机选择初始中心点
        centroids = random.sample(points, k)

        for iteration in range(max_iterations):
            # 分配点到最近的中心点
            clusters = [[] for _ in range(k)]
            for point in points:
                distances = [self._calculate_distance(point, centroid) for centroid in centroids]
                closest_centroid_idx = distances.index(min(distances))
                clusters[closest_centroid_idx].append(point)

            # 重新计算中心点
            new_centroids = []
            for cluster in clusters:
                if cluster:
                    avg_lng = sum(p['lng'] for p in cluster) / len(cluster)
                    avg_lat = sum(p['lat'] for p in cluster) / len(cluster)
                    # 找到距离平均值最近的实际点作为新中心
                    distances = [self._calculate_distance({'lng': avg_lng, 'lat': avg_lat}, p) for p in cluster]
                    closest_point = cluster[distances.index(min(distances))]
                    new_centroids.append(closest_point)
                else:
                    # 如果聚类为空，随机选择一个点
                    new_centroids.append(random.choice(points))

            # 检查是否收敛
            moved = False
            for i in range(k):
                if new_centroids[i] != centroids[i]:
                    moved = True
                    break

            if not moved:
                break

            centroids = new_centroids

        return clusters, centroids

    def perform_clustering(self, pois, k=None):
        """执行聚类分析"""
        if not pois:
            raise ValueError("POI数据为空")

        # 准备坐标数据
        coordinates = []
        for poi in pois:
            coordinates.append({
                'lng': poi['lng'],
                'lat': poi['lat'],
                'poi': poi
            })

        # 确定K值
        if k is None:
            k = self.find_optimal_k(coordinates)
        else:
            k = min(k, len(pois))

        # 执行K-means聚类
        clusters, centroids = self.kmeans_simple(coordinates, k)

        # 计算聚类指标
        cluster_results = []
        for cluster_id, (cluster_points, centroid) in enumerate(zip(clusters, centroids)):
            if not cluster_points:
                continue

            # 计算几何中心
            center_lng = sum(p['lng'] for p in cluster_points) / len(cluster_points)
            center_lat = sum(p['lat'] for p in cluster_points) / len(cluster_points)

            # 找到距离中心最近的点作为推荐位置
            distances = [
                self._calculate_distance({'lng': center_lng, 'lat': center_lat}, p)
                for p in cluster_points
            ]
            nearest_idx = distances.index(min(distances))
            recommended_poi = cluster_points[nearest_idx]['poi']

            # 计算聚类半径
            cluster_radius = max(distances)
            avg_distance = sum(distances) / len(distances)

            # 计算优先级得分
            priority_score = self._calculate_priority_score([p['poi'] for p in cluster_points])

            # 统计类型分布
            type_stats = {}
            for p in cluster_points:
                poi_type = p['poi']['type']
                type_stats[poi_type] = type_stats.get(poi_type, 0) + 1

            cluster_result = {
                'cluster_id': cluster_id + 1,
                'center_lng': round(center_lng, 6),
                'center_lat': round(center_lat, 6),
                'point_count': len(cluster_points),
                'cluster_radius': round(cluster_radius),
                'average_distance': round(avg_distance),
                'priority_score': round(priority_score, 2),
                'recommended_location': recommended_poi,
                'type_statistics': type_stats,
                'covered_points': [p['poi'] for p in cluster_points[:8]]  # 前8个点
            }

            cluster_results.append(cluster_result)

        # 按优先级排序
        cluster_results.sort(key=lambda x: x['priority_score'], reverse=True)

        return {
            'clusters': cluster_results,
            'total_points': len(pois),
            'k_value': k,
            'algorithm': 'Simple K-means'
        }

    def _calculate_priority_score(self, pois):
        """计算聚类优先级得分"""
        if not pois:
            return 0

        total_score = 0
        for poi in pois:
            total_score += poi.get('weight', 1)

        return total_score / len(pois)

    def _calculate_distance(self, point1, point2):
        """计算两点间距离（米）"""
        try:
            return geodesic((point1['lat'], point1['lng']), (point2['lat'], point2['lng'])).meters
        except:
            # 简化计算 - Haversine公式
            lng1, lat1, lng2, lat2 = map(math.radians, [
                point1['lng'], point1['lat'],
                point2['lng'], point2['lat']
            ])

            dlon = lng2 - lng1
            dlat = lat2 - lat1
            a = math.sin(dlat / 2) ** 2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon / 2) ** 2
            c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
            return self.earth_radius * c


# 全局聚类服务实例
clustering_service = SimpleLocationClusteringService()