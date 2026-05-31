import requests
import time
import logging
from config import BAIDU_MAP_AK, NANCHANG_BOUNDS, POI_CATEGORIES

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class BaiduMapService:
    def __init__(self):
        self.ak = BAIDU_MAP_AK
        self.base_url = "http://api.map.baidu.com/place/v2/search"

    def search_poi(self, query, tag, region="南昌市", page_size=20, retry_count=3):
        """搜索POI数据"""
        pois = []
        page_num = 0

        while len(pois) < 200:  # 限制最大数量
            try:
                params = {
                    'query': query,
                    'tag': tag,
                    'region': region,
                    'output': 'json',
                    'ak': self.ak,
                    'page_size': page_size,
                    'page_num': page_num
                }

                response = requests.get(self.base_url, params=params, timeout=10)
                data = response.json()

                if data.get('status') == 0:
                    results = data.get('results', [])
                    if not results:
                        break

                    for result in results:
                        poi_data = self._parse_poi(result)
                        if poi_data and self._is_in_nanchang(poi_data):
                            pois.append(poi_data)

                    logger.info(f"第{page_num + 1}页获取到{len(results)}个POI，当前总数{len(pois)}")
                    page_num += 1

                    # 避免请求过快
                    time.sleep(0.2)
                else:
                    logger.error(f"百度API错误: {data.get('message')}")
                    if retry_count > 0:
                        retry_count -= 1
                        time.sleep(1)
                        continue
                    break

            except Exception as e:
                logger.error(f"请求失败: {e}")
                if retry_count > 0:
                    retry_count -= 1
                    time.sleep(1)
                    continue
                break

        return pois

    def _parse_poi(self, result):
        """解析POI数据"""
        try:
            location = result.get('location', {})
            if not location:
                return None

            name = result.get('name', '').strip()
            address = result.get('address', '')
            lng = float(location.get('lng', 0))
            lat = float(location.get('lat', 0))

            # 分类POI类型
            poi_type = self._classify_poi_type(name, result.get('detail_info', {}))

            return {
                'name': name,
                'lng': lng,
                'lat': lat,
                'address': address,
                'type': poi_type,
                'weight': POI_CATEGORIES.get(poi_type, {}).get('weight', 3),
                'detail_info': result.get('detail_info', {})
            }
        except Exception as e:
            logger.warning(f"解析POI失败: {e}")
            return None

    def _classify_poi_type(self, name, detail_info):
        """分类POI类型"""
        name_lower = name.lower()

        for poi_type, config in POI_CATEGORIES.items():
            for keyword in config['tags']:
                if keyword in name_lower:
                    return poi_type

        return 'other'

    def _is_in_nanchang(self, poi):
        """检查是否在南昌范围内"""
        lng, lat = poi['lng'], poi['lat']
        return (NANCHANG_BOUNDS['min_lng'] <= lng <= NANCHANG_BOUNDS['max_lng'] and
                NANCHANG_BOUNDS['min_lat'] <= lat <= NANCHANG_BOUNDS['max_lat'])

    def get_nanchang_key_locations(self):
        """获取南昌市关键地点数据"""
        all_pois = []

        for category_id, category in POI_CATEGORIES.items():
            logger.info(f"获取 {category['name']} 数据...")

            for tag in category['tags']:
                pois = self.search_poi("", tag, "南昌市")
                all_pois.extend(pois)
                time.sleep(0.3)  # 避免请求过快

        # 去重
        unique_pois = []
        seen = set()

        for poi in all_pois:
            key = f"{poi['lng']:.6f},{poi['lat']:.6f}"
            if key not in seen:
                seen.add(key)
                unique_pois.append(poi)

        logger.info(f"总共获取到 {len(unique_pois)} 个唯一地点")
        return unique_pois


# 全局地图服务实例
map_service = BaiduMapService()