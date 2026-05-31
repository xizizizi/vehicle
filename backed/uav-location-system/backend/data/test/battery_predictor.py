# ============================
# 1. 定义元数据
# ============================
Metadata = {
    "name": "multi_state_battery_predictor",
    "description": "基于多状态电池预测的无人机换电调度计算器",
    "input": {
        "type": "object",
        "properties": {
            "drone_model": {"type": "string", "default": "巡检无人机", "description": "无人机型号"},
            "battery_capacity": {"type": "number", "default": 6000, "description": "电池标称容量(mAh)"},
            "battery_voltage": {"type": "number", "default": 22.2, "description": "电池标称电压(V)"},
            "current_soc": {"type": "number", "default": 80, "description": "当前荷电状态(%)"},
            "current_soh": {"type": "number", "default": 90, "description": "当前健康状态(%)"},
            "battery_temp": {"type": "number", "default": 35, "description": "电池温度(℃)"},
            "battery_resistance": {"type": "number", "default": 0.05, "description": "电池内阻(Ω)"},
            "mission_distance": {"type": "number", "default": 10, "description": "任务飞行距离(km)"},
            "remaining_distance": {"type": "number", "default": 5, "description": "剩余任务距离(km)"},
            "cruise_speed": {"type": "number", "default": 12, "description": "巡航速度(m/s)"},
            "wind_speed": {"type": "number", "default": 5, "description": "环境风速(m/s)"},
            "ambient_temp": {"type": "number", "default": 25, "description": "环境温度(℃)"},
            "mission_priority": {"type": "string", "default": "normal", "description": "任务优先级(high/normal/low)"},
            "nearest_swap_station": {"type": "number", "default": 2.0, "description": "最近换电站距离(km)"}
        }
    },
    "output": {
        "type": "object",
        "properties": {
            "success": {"type": "boolean", "description": "计算是否成功"},
            "message": {"type": "string", "description": "提示信息"},
            "data": {"type": "object", "description": "计算结果数据"}
        }
    }
}


# ============================
# 2. 多状态电池预测计算器类
# ============================
class MultiStateBatteryPredictor:
    def __init__(self):
        # 无人机配置（基于专利中的巡检无人机参数）
        self.drone_configs = {
            "巡检无人机": {
                "weight": 4.5,  # kg
                "hover_power": 400,  # W
                "cruise_power": 350,  # W
                "climb_power": 550,  # W
                "descend_power": 200,  # W,
                "base_consumption": 50  # 基础系统功耗(W)
            },
            "物流无人机": {
                "weight": 8.0,
                "hover_power": 600,
                "cruise_power": 500,
                "climb_power": 800,
                "descend_power": 300,
                "base_consumption": 80
            }
        }

        # 决策阈值（基于专利中的阈值设置）
        self.thresholds = {
            "soh_critical": 70,  # 健康状态临界阈值(%)
            "soh_warning": 85,  # 健康状态警告阈值(%)
            "temp_critical": 50,  # 温度临界阈值(℃)
            "temp_warning": 40,  # 温度警告阈值(℃)
            "safety_margin": 20,  # 安全裕度(%)
            "min_return_soc": 15  # 最低返航电量(%)
        }

    def safe_float(self, value, default=0.0):
        """安全转换为浮点数"""
        if value is None:
            return default
        try:
            return float(value)
        except (ValueError, TypeError):
            return default

    def safe_str(self, value, default=""):
        """安全转换为字符串"""
        if value is None:
            return default
        try:
            return str(value)
        except:
            return default

    def calculate_multi_state(self, params: dict) -> dict:
        """基于专利的多状态电池预测计算"""
        try:
            # 提取参数
            drone_model = self.safe_str(params.get("drone_model"), "巡检无人机")
            battery_capacity = self.safe_float(params.get("battery_capacity"), 6000)  # mAh
            battery_voltage = self.safe_float(params.get("battery_voltage"), 22.2)  # V
            current_soc = self.safe_float(params.get("current_soc"), 80)  # %
            current_soh = self.safe_float(params.get("current_soh"), 90)  # %
            battery_temp = self.safe_float(params.get("battery_temp"), 35)  # ℃
            battery_resistance = self.safe_float(params.get("battery_resistance"), 0.05)  # Ω
            mission_distance = self.safe_float(params.get("mission_distance"), 10)  # km
            remaining_distance = self.safe_float(params.get("remaining_distance"), 5)  # km
            cruise_speed = self.safe_float(params.get("cruise_speed"), 12)  # m/s
            wind_speed = self.safe_float(params.get("wind_speed"), 5)  # m/s
            ambient_temp = self.safe_float(params.get("ambient_temp"), 25)  # ℃
            mission_priority = self.safe_str(params.get("mission_priority"), "normal")
            nearest_swap_dist = self.safe_float(params.get("nearest_swap_station"), 2.0)  # km

            # 获取无人机配置
            if drone_model in self.drone_configs:
                drone = self.drone_configs[drone_model]
            else:
                drone = self.drone_configs["巡检无人机"]
                drone_model = "巡检无人机"

            # ================= 步骤1: 计算多状态参数 =================
            # 1.1 电池总能量计算
            battery_energy_wh = battery_capacity * battery_voltage / 1000  # Wh

            # 1.2 当前可用能量 (基于SOC)
            current_energy_wh = current_soc / 100 * battery_energy_wh

            # 1.3 基于SOH的有效能量 (考虑电池老化)
            effective_energy_wh = current_energy_wh * (current_soh / 100)

            # 1.4 功率状态计算 (基于专利公式: P = V²/R)
            # 最大持续放电功率 (考虑内阻)
            if battery_resistance > 0:
                max_power_w = (battery_voltage ** 2) / (4 * battery_resistance)
            else:
                max_power_w = 1000  # 默认值

            # 1.5 温度状态评估
            temp_factor = self._calculate_temp_factor(battery_temp)

            # ================= 步骤2: 任务能耗预测 =================
            # 2.1 环境因子计算
            wind_factor = self._calculate_wind_factor(wind_speed)
            temp_effect_factor = self._calculate_temp_effect_factor(battery_temp, ambient_temp)

            # 2.2 计算剩余任务能耗
            remaining_distance_m = remaining_distance * 1000  # 转换为米
            remaining_time_h = remaining_distance_m / (cruise_speed * 3600)  # 小时

            # 巡航功率 (考虑环境因子)
            cruise_power = drone["cruise_power"] * wind_factor * temp_effect_factor
            remaining_mission_energy_wh = cruise_power * remaining_time_h

            # 2.3 计算返航能耗
            # 假设返航距离与到最近换电站距离相同，取较大值
            return_distance_km = max(remaining_distance, nearest_swap_dist)
            return_distance_m = return_distance_km * 1000
            return_time_h = return_distance_m / (cruise_speed * 3600)

            # 返航考虑安全裕度，功率增加20%
            return_power = cruise_power * 1.2
            return_energy_wh = return_power * return_time_h

            # 2.4 总需求能量 (专利中的E_required)
            total_required_energy_wh = remaining_mission_energy_wh + return_energy_wh
            total_required_with_margin_wh = total_required_energy_wh * (1 + self.thresholds["safety_margin"] / 100)

            # ================= 步骤3: 能量安全评估 =================
            # 可用能量 vs 需求能量
            energy_deficit_wh = effective_energy_wh - total_required_with_margin_wh

            # 能量安全裕度百分比
            if total_required_with_margin_wh > 0:
                energy_safety_margin_percent = (effective_energy_wh / total_required_with_margin_wh - 1) * 100
            else:
                energy_safety_margin_percent = 100

            # ================= 步骤4: 调度决策生成 =================
            decision_result = self._generate_scheduling_decision(
                effective_energy_wh=effective_energy_wh,
                total_required_energy=total_required_with_margin_wh,
                energy_safety_margin=energy_safety_margin_percent,
                battery_soh=current_soh,
                battery_temp=battery_temp,
                mission_priority=mission_priority,
                nearest_swap_distance=nearest_swap_dist
            )

            # ================= 步骤5: 路径规划评估 =================
            path_planning = self._evaluate_path_planning(
                nearest_swap_distance=nearest_swap_dist,
                remaining_energy=effective_energy_wh,
                battery_soh=current_soh,
                wind_speed=wind_speed,
                mission_priority=mission_priority
            )

            # ================= 构建结果 =================
            result = {
                "success": True,
                "message": "多状态电池预测计算完成",
                "data": {
                    "drone_model": drone_model,
                    "battery_states": {
                        "soc_percent": round(current_soc, 1),
                        "soh_percent": round(current_soh, 1),
                        "temperature_c": round(battery_temp, 1),
                        "resistance_ohm": round(battery_resistance, 4),
                        "total_energy_wh": round(battery_energy_wh, 2),
                        "current_energy_wh": round(current_energy_wh, 2),
                        "effective_energy_wh": round(effective_energy_wh, 2),
                        "max_power_w": round(max_power_w, 1),
                        "temp_factor": round(temp_factor, 3)
                    },
                    "mission_analysis": {
                        "remaining_distance_km": round(remaining_distance, 2),
                        "remaining_mission_energy_wh": round(remaining_mission_energy_wh, 2),
                        "return_energy_wh": round(return_energy_wh, 2),
                        "total_required_energy_wh": round(total_required_with_margin_wh, 2),
                        "energy_deficit_wh": round(energy_deficit_wh, 2),
                        "energy_safety_margin_percent": round(energy_safety_margin_percent, 1)
                    },
                    "environment_factors": {
                        "wind_speed_mps": round(wind_speed, 1),
                        "ambient_temp_c": round(ambient_temp, 1),
                        "wind_factor": round(wind_factor, 3),
                        "temp_effect_factor": round(temp_effect_factor, 3)
                    },
                    "scheduling_decision": decision_result,
                    "path_planning": path_planning,
                    "recommendations": self._generate_recommendations(
                        decision_result["action"],
                        energy_safety_margin_percent,
                        current_soh,
                        battery_temp
                    )
                }
            }

            return result

        except Exception as e:
            return {
                "success": False,
                "message": f"计算失败: {str(e)}",
                "data": {}
            }

    def _calculate_temp_factor(self, battery_temp: float) -> float:
        """计算温度因子（专利中的温度状态影响）"""
        if 20 <= battery_temp <= 30:
            return 1.0
        elif battery_temp > 50:
            return 0.4  # 高温严重衰减
        elif battery_temp > 40:
            return 0.7  # 高温衰减
        elif battery_temp < 0:
            return 0.5  # 低温衰减
        elif battery_temp < 10:
            return 0.8  # 低温衰减
        else:
            # 线性插值
            if battery_temp > 30:
                return 1.0 - (battery_temp - 30) * 0.03
            else:  # 10-20度
                return 0.8 + (battery_temp - 10) * 0.02

    def _calculate_wind_factor(self, wind_speed: float) -> float:
        """计算风阻因子（专利中的环境风速影响）"""
        if wind_speed <= 3:
            return 1.0
        elif wind_speed <= 6:
            return 1.1
        elif wind_speed <= 9:
            return 1.25
        elif wind_speed <= 12:
            return 1.4
        else:
            return 1.6

    def _calculate_temp_effect_factor(self, battery_temp: float, ambient_temp: float) -> float:
        """计算温度效应因子（电池温度与环境温度差异）"""
        temp_diff = abs(battery_temp - ambient_temp)
        if temp_diff <= 5:
            return 1.0
        elif temp_diff <= 10:
            return 1.05
        elif temp_diff <= 15:
            return 1.1
        else:
            return 1.15

    def _generate_scheduling_decision(self, **kwargs) -> dict:
        """生成调度决策（基于专利的决策规则）"""
        effective_energy = kwargs["effective_energy_wh"]
        total_required = kwargs["total_required_energy"]
        energy_margin = kwargs["energy_safety_margin"]
        soh = kwargs["battery_soh"]
        temp = kwargs["battery_temp"]
        priority = kwargs["mission_priority"]

        # 决策逻辑（基于专利步骤四）
        action = "继续任务"
        reason = ""

        # 规则1: 能量安全优先（专利中的能量安全裕度为负）
        if effective_energy < total_required or energy_margin < 0:
            action = "前往换电站换电"
            reason = f"能量安全裕度不足 ({energy_margin:.1f}%)，无法保证安全返航"

        # 规则2: 健康状态临界（专利中的第一预设阈值）
        elif soh < self.thresholds["soh_critical"]:
            action = "前往换电站换电"
            reason = f"电池健康状态临界 ({soh:.1f}% < {self.thresholds['soh_critical']}%)"

        # 规则3: 温度过高（专利中的第二预设阈值）
        elif temp > self.thresholds["temp_critical"]:
            action = "前往充电站执行降温快充"
            reason = f"电池温度过高 ({temp:.1f}℃ > {self.thresholds['temp_critical']}℃)"

        # 规则4: 健康状态警告（专利中的第四预设阈值）
        elif soh < self.thresholds["soh_warning"] and energy_margin > 10:
            action = "执行涓流保养充电"
            reason = f"电池健康状态下降 ({soh:.1f}%)，建议保养"

        # 规则5: 高温警告
        elif temp > self.thresholds["temp_warning"]:
            action = "前往充电站执行降温充电"
            reason = f"电池温度偏高 ({temp:.1f}℃)"

        # 规则6: 高优先级任务快速充电
        elif priority == "high" and energy_margin < 20:
            action = "前往充电站执行快速充电"
            reason = "高优先级任务，需快速补充能量"

        return {
            "action": action,
            "reason": reason,
            "priority": priority,
            "recommended_station_type": "换电站" if "换电" in action else "充电站"
        }

    def _evaluate_path_planning(self, **kwargs) -> dict:
        """评估路径规划（基于改进蚁群算法理念）"""
        distance = kwargs["nearest_swap_distance"]
        remaining_energy = kwargs["remaining_energy"]
        soh = kwargs["battery_soh"]
        wind_speed = kwargs["wind_speed"]

        # 简化路径代价计算（专利中的路径代价函数）
        # 代价 = 距离代价 + 能耗代价 + 时间代价 + 健康因子

        # 距离代价
        distance_cost = distance * 0.5

        # 能耗代价（考虑风阻）
        wind_factor = self._calculate_wind_factor(wind_speed)
        energy_consumption_wh = distance * wind_factor * 30  # 简化能耗模型
        energy_cost = energy_consumption_wh / 50

        # 时间代价
        time_cost = distance * 0.3

        # 健康因子（专利中的电池健康状态因子）
        health_factor = 1.0 if soh > 80 else (1.0 + (80 - soh) / 100)

        # 总代价
        total_cost = (distance_cost + energy_cost + time_cost) * health_factor

        # 安全评估
        energy_required = energy_consumption_wh * 1.2  # 加20%安全裕度
        can_reach = remaining_energy > energy_required

        return {
            "nearest_station_distance_km": round(distance, 2),
            "estimated_energy_consumption_wh": round(energy_consumption_wh, 2),
            "estimated_time_minutes": round(distance * 3, 1),  # 约3分钟/公里
            "total_path_cost": round(total_cost, 3),
            "can_safely_reach": can_reach,
            "remaining_energy_after_reaching_wh": round(remaining_energy - energy_required, 2) if can_reach else 0,
            "health_factor_impact": round(health_factor, 3)
        }

    def _generate_recommendations(self, action: str, energy_margin: float, soh: float, temp: float) -> list:
        """生成建议列表"""
        recommendations = []

        if "换电" in action:
            recommendations.append("🔋 立即前往换电站更换电池")
            recommendations.append("📊 考虑电池健康状态选择最佳路径")
            recommendations.append("⏱️ 预估换电时间：5-8分钟")

        elif "充电" in action:
            if "降温" in action:
                recommendations.append("❄️ 执行降温充电，避免高温损伤电池")
                recommendations.append("⏳ 降温充电时间：15-20分钟")
            elif "快速" in action:
                recommendations.append("⚡ 执行快速充电，尽快恢复任务")
                recommendations.append("⚠️ 快速充电可能影响电池寿命")
            elif "涓流" in action:
                recommendations.append("🔋 执行涓流保养充电，延长电池寿命")
                recommendations.append("⏳ 保养充电时间：60-90分钟")

        elif action == "继续任务":
            recommendations.append("✅ 当前状态允许继续执行任务")
            recommendations.append(f"📈 能量安全裕度：{energy_margin:.1f}%")

        # 通用建议
        if soh < 80:
            recommendations.append("🔄 电池健康度下降，考虑近期更换")

        if temp > 40:
            recommendations.append("🌡️ 注意电池温度，避免长时间高温运行")

        if energy_margin < 15:
            recommendations.append("⚠️ 能量裕度较低，建议提前规划返航")

        recommendations.append("📋 定期检查电池状态和维护记录")

        return recommendations


# ============================
# 3. 【必须】handler 函数
# ============================
def handler(args):
    """
    主处理函数
    """
    try:
        # 获取输入参数
        if isinstance(args, dict):
            input_params = args.get('input', args)
        else:
            try:
                input_params = args.input
            except AttributeError:
                input_params = args

        # 确保input_params是字典
        if not isinstance(input_params, dict):
            try:
                import json
                if isinstance(input_params, str):
                    input_params = json.loads(input_params)
                else:
                    input_params = {}
            except:
                input_params = {}

        # 初始化计算器
        predictor = MultiStateBatteryPredictor()

        # 执行计算
        result = predictor.calculate_multi_state(input_params)

        # 返回结果
        return result

    except Exception as e:
        return {
            "success": False,
            "message": f"系统错误: {str(e)}",
            "data": {}
        }


# ============================
# 4. 【可选】本地测试
# ============================
if __name__ == "__main__":
    print("基于多状态电池预测的无人机换电调度计算器 - 本地测试")
    print("=" * 60)

    # 测试用例1: 正常巡检任务
    test1 = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 57,
        "current_soh": 85,
        "battery_temp": 45,
        "battery_resistance": 0.06,
        "mission_distance": 8,
        "remaining_distance": 6,
        "cruise_speed": 12,
        "wind_speed": 5,
        "ambient_temp": 25,
        "mission_priority": "normal",
        "nearest_swap_station": 1.5
    }

    # 测试用例2: 健康状态临界
    test2 = {
        "current_soc": 60,
        "current_soh": 65,
        "battery_temp": 38,
        "remaining_distance": 3,
        "nearest_swap_station": 2.2
    }

    # 测试用例3: 温度过高
    test3 = {
        "current_soc": 70,
        "current_soh": 90,
        "battery_temp": 52,
        "remaining_distance": 4,
        "nearest_swap_station": 1.8
    }

    tests = [
        ("专利实施例（巡检任务）", test1),
        ("健康状态临界", test2),
        ("温度过高", test3)
    ]

    for name, test_input in tests:
        print(f"\n测试: {name}")
        print(f"输入参数: {test_input}")

        # 补充默认值
        default_test = {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 80,
            "current_soh": 90,
            "battery_temp": 35,
            "battery_resistance": 0.05,
            "mission_distance": 10,
            "remaining_distance": 5,
            "cruise_speed": 12,
            "wind_speed": 5,
            "ambient_temp": 25,
            "mission_priority": "normal",
            "nearest_swap_station": 2.0
        }
        default_test.update(test_input)

        mock_args = {"input": default_test}
        result = handler(mock_args)

        print(f"计算结果:")
        print(f"  成功: {result.get('success', False)}")
        print(f"  消息: {result.get('message', '')}")

        if result.get("success"):
            data = result.get("data", {})
            decision = data.get('scheduling_decision', {})
            print(f"  调度决策: {decision.get('action', '未知')}")
            print(f"  决策原因: {decision.get('reason', '')}")

            states = data.get('battery_states', {})
            print(f"  电池状态:")
            print(f"    SOC: {states.get('soc_percent', 0)}%")
            print(f"    SOH: {states.get('soh_percent', 0)}%")
            print(f"    温度: {states.get('temperature_c', 0)}℃")

            analysis = data.get('mission_analysis', {})
            print(f"  能量分析:")
            print(f"    可用能量: {analysis.get('effective_energy_wh', 0):.1f} Wh")
            print(f"    需求能量: {analysis.get('total_required_energy_wh', 0):.1f} Wh")
            print(f"    安全裕度: {analysis.get('energy_safety_margin_percent', 0):.1f}%")

            recs = data.get('recommendations', [])
            if recs:
                print(f"  建议:")
                for rec in recs[:3]:
                    print(f"    - {rec}")
        else:
            print(f"  错误详情: {result.get('message', '未知错误')}")

        print("-" * 60)

    print("\n测试完成!")