#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
基于多状态电池预测的无人机换电调度计算器 - 实际场景测试
模拟真实世界中的无人机任务场景
"""

import sys
import os
import json
import time
import random

# 将当前目录添加到路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

try:
    from battery_predictor import handler

    print("✓ 成功导入主模块")
except ImportError as e:
    print(f"✗ 导入主模块失败: {e}")
    sys.exit(1)


class RealWorldScenarioSimulator:
    """真实世界场景模拟器"""

    def __init__(self):
        self.scenarios = []
        self.results = []

    def add_scenario(self, name, description, test_input):
        """添加测试场景"""
        self.scenarios.append({
            "name": name,
            "description": description,
            "input": test_input
        })

    def run_scenario(self, scenario):
        """运行单个场景"""
        print(f"\n{'=' * 80}")
        print(f"场景: {scenario['name']}")
        print(f"{'=' * 80}")
        print(f"描述: {scenario['description']}")

        # 显示关键输入参数
        print("\n关键参数:")
        params = scenario['input']

        # 电池相关参数
        battery_params = ["current_soc", "current_soh", "battery_temp", "battery_resistance"]
        print("  电池状态:")
        for param in battery_params:
            if param in params:
                unit = "%" if "soc" in param or "soh" in param else "℃" if "temp" in param else "Ω"
                print(f"    {param}: {params[param]} {unit}")

        # 任务相关参数
        task_params = ["remaining_distance", "mission_priority", "wind_speed", "nearest_swap_station"]
        print("  任务状态:")
        for param in task_params:
            if param in params:
                unit = "km" if "distance" in param or "station" in param else "m/s" if "speed" in param else ""
                print(f"    {param}: {params[param]} {unit}")

        # 执行测试
        start_time = time.time()
        result = handler({"input": params})
        end_time = time.time()

        execution_time = (end_time - start_time) * 1000

        print(f"\n执行时间: {execution_time:.2f}ms")
        print(f"成功: {result.get('success', False)}")

        if result.get("success"):
            data = result.get("data", {})

            # 显示决策
            decision = data.get("scheduling_decision", {})
            action = decision.get("action", "未知")
            reason = decision.get("reason", "")

            print(f"调度决策: {action}")
            if reason:
                print(f"决策原因: {reason}")

            # 显示能量分析
            mission_analysis = data.get("mission_analysis", {})
            energy_margin = mission_analysis.get("energy_safety_margin_percent", 0)
            energy_deficit = mission_analysis.get("energy_deficit_wh", 0)

            print(f"能量安全裕度: {energy_margin:.1f}%")
            print(f"能量缺口: {energy_deficit:.2f} Wh")

            # 显示建议
            recommendations = data.get("recommendations", [])
            if recommendations:
                print("\n建议:")
                for rec in recommendations[:3]:  # 只显示前3条建议
                    print(f"  • {rec}")

        return {
            "scenario_name": scenario['name'],
            "success": result.get("success", False),
            "message": result.get("message", ""),
            "decision": result.get("data", {}).get("scheduling_decision", {}),
            "execution_time_ms": execution_time,
            "result_data": result.get("data", {})
        }

    def run_all_scenarios(self):
        """运行所有场景"""
        print("基于多状态电池预测的无人机换电调度计算器 - 真实场景测试")
        print("=" * 80)

        for scenario in self.scenarios:
            result = self.run_scenario(scenario)
            self.results.append(result)

            # 添加分隔线
            print("\n" + "-" * 40)

        return self.results

    def generate_report(self):
        """生成测试报告"""
        if not self.results:
            print("没有测试结果可生成报告")
            return

        print("\n" + "=" * 80)
        print("真实场景测试报告")
        print("=" * 80)

        # 统计信息
        total_scenarios = len(self.results)
        successful_scenarios = sum(1 for r in self.results if r["success"])
        failed_scenarios = total_scenarios - successful_scenarios

        # 决策分布
        decision_counts = {}
        for result in self.results:
            if result["success"]:
                action = result["decision"].get("action", "未知")
                decision_counts[action] = decision_counts.get(action, 0) + 1

        # 执行时间统计
        execution_times = [r["execution_time_ms"] for r in self.results if r["success"]]
        avg_execution_time = sum(execution_times) / len(execution_times) if execution_times else 0
        max_execution_time = max(execution_times) if execution_times else 0
        min_execution_time = min(execution_times) if execution_times else 0

        print(f"场景总数: {total_scenarios}")
        print(f"成功场景: {successful_scenarios}")
        print(f"失败场景: {failed_scenarios}")
        print(f"成功率: {successful_scenarios / total_scenarios * 100:.1f}%" if total_scenarios > 0 else "N/A")

        print(f"\n平均执行时间: {avg_execution_time:.2f}ms")
        print(f"最短执行时间: {min_execution_time:.2f}ms")
        print(f"最长执行时间: {max_execution_time:.2f}ms")

        print("\n决策分布:")
        for action, count in decision_counts.items():
            percentage = count / successful_scenarios * 100 if successful_scenarios > 0 else 0
            print(f"  {action}: {count}次 ({percentage:.1f}%)")

        # 显示失败场景
        if failed_scenarios > 0:
            print("\n失败场景:")
            for result in self.results:
                if not result["success"]:
                    print(f"  - {result['scenario_name']}: {result['message']}")

        # 保存详细报告
        report = {
            "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
            "summary": {
                "total_scenarios": total_scenarios,
                "successful_scenarios": successful_scenarios,
                "failed_scenarios": failed_scenarios,
                "success_rate": successful_scenarios / total_scenarios * 100 if total_scenarios > 0 else 0,
                "execution_time_stats": {
                    "average_ms": avg_execution_time,
                    "min_ms": min_execution_time,
                    "max_ms": max_execution_time
                }
            },
            "decision_distribution": decision_counts,
            "scenario_results": self.results
        }

        with open("real_scenarios_test_report.json", "w", encoding="utf-8") as f:
            json.dump(report, f, ensure_ascii=False, indent=2)

        print(f"\n✓ 详细报告已保存到: real_scenarios_test_report.json")

        return report


def create_real_world_scenarios():
    """创建真实世界场景"""
    simulator = RealWorldScenarioSimulator()

    # 1. 电力巡检场景（专利中的主要应用）
    simulator.add_scenario(
        "电力线路巡检 - 正常情况",
        "无人机执行常规电力线路巡检任务，电池状态良好，任务进展顺利",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 75,
            "current_soh": 92,
            "battery_temp": 32,
            "battery_resistance": 0.04,
            "mission_distance": 15,
            "remaining_distance": 6,
            "cruise_speed": 12,
            "wind_speed": 3,
            "ambient_temp": 22,
            "mission_priority": "normal",
            "nearest_swap_station": 2.5
        }
    )

    # 2. 电力巡检 - 突发大风
    simulator.add_scenario(
        "电力巡检 - 突发大风天气",
        "巡检过程中突然遇到大风，能耗增加，需要重新评估",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 65,
            "current_soh": 88,
            "battery_temp": 38,
            "battery_resistance": 0.05,
            "mission_distance": 15,
            "remaining_distance": 8,
            "cruise_speed": 10,  # 大风中降低速度
            "wind_speed": 12,  # 大风
            "ambient_temp": 18,
            "mission_priority": "normal",
            "nearest_swap_station": 3.0
        }
    )

    # 3. 电力巡检 - 电池老化
    simulator.add_scenario(
        "电力巡检 - 电池老化情况",
        "无人机电池已使用较长时间，健康状态下降，需要特别注意",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 70,
            "current_soh": 72,  # 低于临界阈值
            "battery_temp": 36,
            "battery_resistance": 0.08,  # 内阻增加
            "mission_distance": 12,
            "remaining_distance": 5,
            "cruise_speed": 12,
            "wind_speed": 4,
            "ambient_temp": 25,
            "mission_priority": "normal",
            "nearest_swap_station": 1.8
        }
    )

    # 4. 紧急抢修任务
    simulator.add_scenario(
        "电力紧急抢修任务",
        "发生电力故障，需要无人机紧急前往查看，任务优先级高",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 55,
            "current_soh": 85,
            "battery_temp": 40,
            "battery_resistance": 0.06,
            "mission_distance": 20,
            "remaining_distance": 12,  # 距离较远
            "cruise_speed": 15,  # 紧急任务，较快速度
            "wind_speed": 5,
            "ambient_temp": 28,
            "mission_priority": "high",  # 高优先级
            "nearest_swap_station": 4.0  # 换电站较远
        }
    )

    # 5. 夏季高温巡检
    simulator.add_scenario(
        "夏季高温环境巡检",
        "夏季高温天气执行巡检任务，电池温度容易升高",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 68,
            "current_soh": 87,
            "battery_temp": 48,  # 高温
            "battery_resistance": 0.05,
            "mission_distance": 10,
            "remaining_distance": 4,
            "cruise_speed": 12,
            "wind_speed": 2,
            "ambient_temp": 35,  # 环境温度高
            "mission_priority": "normal",
            "nearest_swap_station": 2.2
        }
    )

    # 6. 冬季低温巡检
    simulator.add_scenario(
        "冬季低温环境巡检",
        "冬季低温天气执行巡检任务，电池性能下降",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 80,
            "current_soh": 90,
            "battery_temp": 8,  # 低温
            "battery_resistance": 0.07,  # 低温下内阻增加
            "mission_distance": 8,
            "remaining_distance": 3,
            "cruise_speed": 11,  # 低温下适当降低速度
            "wind_speed": 6,
            "ambient_temp": -5,  # 环境温度低
            "mission_priority": "normal",
            "nearest_swap_station": 1.5
        }
    )

    # 7. 物流配送场景
    simulator.add_scenario(
        "物流无人机配送任务",
        "物流无人机执行包裹配送任务，需要精确的能量管理",
        {
            "drone_model": "物流无人机",
            "battery_capacity": 10000,  # 物流无人机电池更大
            "battery_voltage": 22.2,
            "current_soc": 65,
            "current_soh": 88,
            "battery_temp": 35,
            "battery_resistance": 0.05,
            "mission_distance": 25,
            "remaining_distance": 10,
            "cruise_speed": 15,
            "wind_speed": 4,
            "ambient_temp": 24,
            "mission_priority": "normal",
            "nearest_swap_station": 3.5
        }
    )

    # 8. 农业植保场景
    simulator.add_scenario(
        "农业植保无人机作业",
        "农业植保无人机执行农药喷洒任务，任务区域大，能耗高",
        {
            "drone_model": "巡检无人机",  # 使用相同配置
            "battery_capacity": 8000,  # 农业无人机电池容量较大
            "battery_voltage": 22.2,
            "current_soc": 45,  # 低电量，需要频繁换电
            "current_soh": 83,
            "battery_temp": 42,  # 长时间作业温度较高
            "battery_resistance": 0.06,
            "mission_distance": 50,  # 农业作业距离长
            "remaining_distance": 20,
            "cruise_speed": 8,  # 植保作业速度较慢
            "wind_speed": 3,
            "ambient_temp": 30,
            "mission_priority": "normal",
            "nearest_swap_station": 1.2  # 农田中换电站较近
        }
    )

    # 9. 紧急医疗物资配送
    simulator.add_scenario(
        "紧急医疗物资配送",
        "紧急医疗物资配送，时间紧迫，任务优先级最高",
        {
            "drone_model": "物流无人机",
            "battery_capacity": 10000,
            "battery_voltage": 22.2,
            "current_soc": 40,  # 电量较低
            "current_soh": 90,
            "battery_temp": 44,
            "battery_resistance": 0.05,
            "mission_distance": 30,
            "remaining_distance": 15,
            "cruise_speed": 18,  # 最快速度
            "wind_speed": 8,
            "ambient_temp": 26,
            "mission_priority": "high",  # 高优先级
            "nearest_swap_station": 5.0  # 距离换电站很远
        }
    )

    # 10. 夜间巡逻任务
    simulator.add_scenario(
        "夜间安全巡逻任务",
        "夜间执行安全巡逻任务，需要保守的能量管理策略",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 70,
            "current_soh": 86,
            "battery_temp": 28,  # 夜间温度较低
            "battery_resistance": 0.05,
            "mission_distance": 12,
            "remaining_distance": 7,
            "cruise_speed": 10,  # 夜间降低速度
            "wind_speed": 5,
            "ambient_temp": 15,  # 夜间温度低
            "mission_priority": "normal",
            "nearest_swap_station": 2.8
        }
    )

    # 11. 多无人机协同任务
    simulator.add_scenario(
        "多无人机协同巡检",
        "多架无人机协同执行大面积巡检，需要考虑换电站负载",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 58,
            "current_soh": 89,
            "battery_temp": 37,
            "battery_resistance": 0.05,
            "mission_distance": 18,
            "remaining_distance": 9,
            "cruise_speed": 12,
            "wind_speed": 4,
            "ambient_temp": 24,
            "mission_priority": "normal",
            "nearest_swap_station": 2.0,
            "additional_context": "多机协同，换电站可能繁忙"
        }
    )

    # 12. 山区复杂地形巡检
    simulator.add_scenario(
        "山区复杂地形巡检",
        "在山区执行巡检，地形复杂，风速变化大",
        {
            "drone_model": "巡检无人机",
            "battery_capacity": 6000,
            "battery_voltage": 22.2,
            "current_soc": 62,
            "current_soh": 84,
            "battery_temp": 34,
            "battery_resistance": 0.06,
            "mission_distance": 15,
            "remaining_distance": 8,
            "cruise_speed": 10,  # 山区降低速度
            "wind_speed": 7,  # 山区风速较大
            "ambient_temp": 20,
            "mission_priority": "normal",
            "nearest_swap_station": 3.5  # 山区换电站距离较远
        }
    )

    return simulator


def run_task_simulation():
    """运行任务过程模拟 - 模拟一个完整任务的多个阶段"""
    print("\n" + "=" * 80)
    print("任务过程模拟 - 无人机从任务开始到结束的完整过程")
    print("=" * 80)

    # 模拟一个任务的多个阶段
    task_stages = [
        {
            "stage": "任务开始",
            "description": "无人机开始执行20km的电力线路巡检任务",
            "remaining_distance": 20,
            "current_soc": 95,
            "current_soh": 92,
            "battery_temp": 25
        },
        {
            "stage": "任务进行25%",
            "description": "已完成5km巡检，电池正常消耗",
            "remaining_distance": 15,
            "current_soc": 78,
            "current_soh": 92,
            "battery_temp": 32
        },
        {
            "stage": "任务进行50%",
            "description": "已完成10km巡检，遇到侧风，能耗增加",
            "remaining_distance": 10,
            "current_soc": 62,
            "current_soh": 92,
            "battery_temp": 38,
            "wind_speed": 8  # 遇到侧风
        },
        {
            "stage": "任务进行75%",
            "description": "已完成15km巡检，电池温度上升",
            "remaining_distance": 5,
            "current_soc": 48,
            "current_soh": 92,
            "battery_temp": 42
        },
        {
            "stage": "任务即将完成",
            "description": "只剩最后2km，但电量紧张",
            "remaining_distance": 2,
            "current_soc": 25,
            "current_soh": 92,
            "battery_temp": 44
        }
    ]

    # 基础参数
    base_params = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "battery_resistance": 0.05,
        "mission_distance": 20,
        "cruise_speed": 12,
        "ambient_temp": 28,
        "mission_priority": "normal",
        "nearest_swap_station": 3.0
    }

    stage_results = []

    for i, stage in enumerate(task_stages):
        print(f"\n阶段 {i + 1}: {stage['stage']}")
        print(f"描述: {stage['description']}")

        # 构建输入参数
        params = base_params.copy()
        params.update({
            "remaining_distance": stage["remaining_distance"],
            "current_soc": stage["current_soc"],
            "current_soh": stage["current_soh"],
            "battery_temp": stage["battery_temp"]
        })

        # 如果有风速参数
        if "wind_speed" in stage:
            params["wind_speed"] = stage["wind_speed"]
        else:
            params["wind_speed"] = 4  # 默认风速

        print(
            f"关键状态: SOC={stage['current_soc']}%, 剩余距离={stage['remaining_distance']}km, 温度={stage['battery_temp']}℃")

        # 执行计算
        result = handler({"input": params})

        if result.get("success"):
            data = result.get("data", {})
            decision = data.get("scheduling_decision", {})
            action = decision.get("action", "未知")

            mission_analysis = data.get("mission_analysis", {})
            energy_margin = mission_analysis.get("energy_safety_margin_percent", 0)

            print(f"决策: {action}")
            print(f"能量安全裕度: {energy_margin:.1f}%")

            stage_results.append({
                "stage": stage["stage"],
                "soc": stage["current_soc"],
                "remaining_distance": stage["remaining_distance"],
                "decision": action,
                "energy_margin": energy_margin
            })
        else:
            print(f"计算失败: {result.get('message', '未知错误')}")

        print("-" * 40)

    # 输出任务过程总结
    print("\n" + "=" * 80)
    print("任务过程模拟总结")
    print("=" * 80)

    print("\n阶段\t\t\tSOC\t剩余距离\t决策\t\t\t安全裕度")
    print("-" * 70)

    for result in stage_results:
        stage = result["stage"]
        soc = result["soc"]
        distance = result["remaining_distance"]
        decision = result["decision"]
        margin = result["energy_margin"]

        # 简化决策显示
        if "继续任务" in decision:
            decision_display = "继续任务"
        elif "换电" in decision:
            decision_display = "前往换电"
        elif "充电" in decision:
            decision_display = "前往充电"
        else:
            decision_display = decision[:12] + "..." if len(decision) > 12 else decision

        print(f"{stage:15}\t{soc:3}%\t{distance:6}km\t{decision_display:15}\t{margin:6.1f}%")

    # 分析决策变化
    print("\n决策变化分析:")
    decisions = [r["decision"] for r in stage_results]

    if "前往换电站换电" in decisions:
        first_swap_index = next((i for i, d in enumerate(decisions) if "前往换电站换电" in d), -1)
        if first_swap_index >= 0:
            print(
                f"  首次建议换电发生在: {stage_results[first_swap_index]['stage']} (SOC={stage_results[first_swap_index]['soc']}%)")

    # 检查安全裕度趋势
    margins = [r["energy_margin"] for r in stage_results]
    if len(margins) > 1:
        margin_trend = "下降" if margins[-1] < margins[0] else "上升" if margins[-1] > margins[0] else "稳定"
        print(f"  安全裕度趋势: {margin_trend} (从{margins[0]:.1f}%到{margins[-1]:.1f}%)")

    return stage_results


def main():
    """主函数"""
    print("基于多状态电池预测的无人机换电调度计算器 - 真实场景测试套件")
    print("=" * 80)

    # 创建并运行真实场景测试
    simulator = create_real_world_scenarios()
    results = simulator.run_all_scenarios()

    # 生成报告
    report = simulator.generate_report()

    # 运行任务过程模拟
    print("\n" + "=" * 80)
    print("额外测试: 任务过程模拟")
    print("=" * 80)

    stage_results = run_task_simulation()

    # 保存完整的测试结果
    complete_results = {
        "real_world_scenarios": results,
        "task_simulation": stage_results,
        "summary": report.get("summary", {}) if report else {}
    }

    with open("complete_real_scenarios_test.json", "w", encoding="utf-8") as f:
        json.dump(complete_results, f, ensure_ascii=False, indent=2)

    print(f"\n✓ 完整测试结果已保存到: complete_real_scenarios_test.json")

    # 返回测试结果
    total_scenarios = len(results)
    successful_scenarios = sum(1 for r in results if r["success"])

    if successful_scenarios == total_scenarios:
        print(f"\n🎉 所有真实场景测试通过! ({successful_scenarios}/{total_scenarios})")
        return 0
    else:
        print(f"\n⚠ 有 {total_scenarios - successful_scenarios} 个真实场景测试失败!")
        return 1


if __name__ == "__main__":
    exit_code = main()
    sys.exit(exit_code)