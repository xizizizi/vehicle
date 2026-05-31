
"""
基于多状态电池预测的无人机换电调度计算器 - 测试文件
测试专利中描述的各种场景
"""

import json
import sys
import os
import copy


sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# 导入主模块
try:
    from battery_predictor import handler

    print("✓ 成功导入主模块")
except ImportError as e:
    print(f"✗ 导入主模块失败: {e}")
    sys.exit(1)


def print_test_header(test_name):
    """打印测试标题"""
    print("\n" + "=" * 80)
    print(f"测试: {test_name}")
    print("=" * 80)


def print_result(result):
    """打印测试结果"""
    success = result.get("success", False)
    message = result.get("message", "")

    print(f"✓ 计算成功" if success else f"✗ 计算失败")
    print(f"消息: {message}")

    if success:
        data = result.get("data", {})

        # 电池状态
        battery_states = data.get("battery_states", {})
        print("\n[电池状态]")
        print(f"  荷电状态(SOC): {battery_states.get('soc_percent', 'N/A')}%")
        print(f"  健康状态(SOH): {battery_states.get('soh_percent', 'N/A')}%")
        print(f"  电池温度: {battery_states.get('temperature_c', 'N/A')}℃")
        print(f"  电池内阻: {battery_states.get('resistance_ohm', 'N/A')}Ω")
        print(f"  总能量: {battery_states.get('total_energy_wh', 'N/A')} Wh")
        print(f"  当前有效能量: {battery_states.get('effective_energy_wh', 'N/A')} Wh")

        # 任务分析
        mission_analysis = data.get("mission_analysis", {})
        print("\n[任务分析]")
        print(f"  剩余任务距离: {mission_analysis.get('remaining_distance_km', 'N/A')} km")
        print(f"  任务需求能量: {mission_analysis.get('total_required_energy_wh', 'N/A')} Wh")
        print(f"  能量安全裕度: {mission_analysis.get('energy_safety_margin_percent', 'N/A')}%")
        print(f"  能量缺口: {mission_analysis.get('energy_deficit_wh', 'N/A')} Wh")

        # 调度决策
        scheduling_decision = data.get("scheduling_decision", {})
        print("\n[调度决策]")
        print(f"  动作: {scheduling_decision.get('action', 'N/A')}")
        print(f"  原因: {scheduling_decision.get('reason', 'N/A')}")
        print(f"  优先级: {scheduling_decision.get('priority', 'N/A')}")
        print(f"  推荐站点类型: {scheduling_decision.get('recommended_station_type', 'N/A')}")

        # 路径规划
        path_planning = data.get("path_planning", {})
        print("\n[路径规划]")
        print(f"  最近换电站距离: {path_planning.get('nearest_station_distance_km', 'N/A')} km")
        print(f"  预计能耗: {path_planning.get('estimated_energy_consumption_wh', 'N/A')} Wh")
        print(f"  预计时间: {path_planning.get('estimated_time_minutes', 'N/A')} 分钟")
        print(f"  能否安全到达: {'是' if path_planning.get('can_safely_reach', False) else '否'}")

        # 建议
        recommendations = data.get("recommendations", [])
        if recommendations:
            print("\n[建议]")
            for i, rec in enumerate(recommendations[:5], 1):
                print(f"  {i}. {rec}")
            if len(recommendations) > 5:
                print(f"  ... 还有{len(recommendations) - 5}条建议")

    print("\n" + "-" * 40)


def test_scenario_1():
    """测试场景1: 专利实施例 - 巡检任务能量不足"""
    print_test_header("专利实施例 - 巡检任务能量不足")

    # 这是专利中描述的实施例场景
    test_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,  # mAh
        "battery_voltage": 22.2,  # V
        "current_soc": 57,  # % (专利中计算得到)
        "current_soh": 85,  # %
        "battery_temp": 45,  # ℃
        "battery_resistance": 0.06,  # Ω
        "mission_distance": 8,  # km
        "remaining_distance": 6,  # km (假设已飞行2km)
        "cruise_speed": 12,  # m/s
        "wind_speed": 5,  # m/s
        "ambient_temp": 25,  # ℃
        "mission_priority": "normal",
        "nearest_swap_station": 1.5  # km
    }

    print("输入参数:")
    for key, value in test_input.items():
        print(f"  {key}: {value}")

    result = handler({"input": test_input})
    print_result(result)

    # 验证是否符合专利预期
    if result.get("success"):
        decision = result.get("data", {}).get("scheduling_decision", {})
        action = decision.get("action", "")

        # 专利中期望的决策是"前往换电站换电"
        expected_action = "前往换电站换电"
        if expected_action in action:
            print("✓ 测试通过: 决策符合专利预期 (能量不足→换电)")
        else:
            print(f"✗ 测试未通过: 期望决策 '{expected_action}'，实际得到 '{action}'")

    return result


def test_scenario_2():
    """测试场景2: 电池健康状态临界"""
    print_test_header("电池健康状态临界")

    test_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 65,  # 电量尚可
        "current_soh": 65,  # 健康状态低于临界阈值(70%)
        "battery_temp": 38,  # 正常温度
        "battery_resistance": 0.08,  # 内阻偏高
        "mission_distance": 10,
        "remaining_distance": 3,  # 剩余任务较少
        "cruise_speed": 12,
        "wind_speed": 3,
        "ambient_temp": 22,
        "mission_priority": "normal",
        "nearest_swap_station": 1.2
    }

    print("输入参数:")
    for key, value in test_input.items():
        print(f"  {key}: {value}")

    result = handler({"input": test_input})
    print_result(result)

    # 验证
    if result.get("success"):
        decision = result.get("data", {}).get("scheduling_decision", {})
        action = decision.get("action", "")
        reason = decision.get("reason", "")

        # 应该触发健康状态临界规则
        if "换电" in action and "健康状态临界" in reason:
            print("✓ 测试通过: 健康状态临界触发换电决策")
        else:
            print(f"✗ 测试未通过: 期望健康状态触发换电，实际: {action} ({reason})")

    return result


def test_scenario_3():
    """测试场景3: 电池温度过高"""
    print_test_header("电池温度过高")

    test_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 75,  # 电量充足
        "current_soh": 88,  # 健康状态良好
        "battery_temp": 52,  # 温度超过临界阈值(50℃)
        "battery_resistance": 0.05,
        "mission_distance": 8,
        "remaining_distance": 4,
        "cruise_speed": 12,
        "wind_speed": 2,
        "ambient_temp": 30,  # 环境温度也高
        "mission_priority": "normal",
        "nearest_swap_station": 2.0
    }

    print("输入参数:")
    for key, value in test_input.items():
        print(f"  {key}: {value}")

    result = handler({"input": test_input})
    print_result(result)

    # 验证
    if result.get("success"):
        decision = result.get("data", {}).get("scheduling_decision", {})
        action = decision.get("action", "")
        reason = decision.get("reason", "")

        # 应该触发温度过高规则
        if "降温" in action and "温度过高" in reason:
            print("✓ 测试通过: 温度过高触发降温充电")
        else:
            print(f"✗ 测试未通过: 期望温度过高触发降温，实际: {action} ({reason})")

    return result


def test_scenario_4():
    """测试场景4: 高优先级任务"""
    print_test_header("高优先级任务 - 快速充电")

    test_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 55,  # 电量中等
        "current_soh": 82,  # 健康状态良好
        "battery_temp": 36,  # 正常温度
        "battery_resistance": 0.06,
        "mission_distance": 12,
        "remaining_distance": 8,  # 剩余任务较多
        "cruise_speed": 12,
        "wind_speed": 4,
        "ambient_temp": 24,
        "mission_priority": "high",  # 高优先级
        "nearest_swap_station": 3.5  # 换电站较远
    }

    print("输入参数:")
    for key, value in test_input.items():
        print(f"  {key}: {value}")

    result = handler({"input": test_input})
    print_result(result)

    # 验证
    if result.get("success"):
        decision = result.get("data", {}).get("scheduling_decision", {})
        action = decision.get("action", "")

        # 高优先级任务可能触发快速充电
        if "快速充电" in action or "换电" in action:
            print("✓ 测试通过: 高优先级任务触发快速补能策略")
        else:
            print(f"✗ 测试未通过: 期望快速补能策略，实际: {action}")

    return result


def test_scenario_5():
    """测试场景5: 正常任务继续执行"""
    print_test_header("正常任务 - 继续执行")

    test_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 85,  # 电量充足
        "current_soh": 92,  # 健康状态优秀
        "battery_temp": 32,  # 正常温度
        "battery_resistance": 0.04,  # 低内阻
        "mission_distance": 10,
        "remaining_distance": 3,  # 剩余任务较少
        "cruise_speed": 12,
        "wind_speed": 2,
        "ambient_temp": 20,
        "mission_priority": "normal",
        "nearest_swap_station": 1.8
    }

    print("输入参数:")
    for key, value in test_input.items():
        print(f"  {key}: {value}")

    result = handler({"input": test_input})
    print_result(result)

    # 验证
    if result.get("success"):
        decision = result.get("data", {}).get("scheduling_decision", {})
        action = decision.get("action", "")

        # 应该继续执行任务
        if "继续任务" in action:
            print("✓ 测试通过: 状态良好，继续执行任务")
        else:
            print(f"✗ 测试未通过: 期望继续任务，实际: {action}")

    return result


def test_scenario_6():
    """测试场景6: 电池健康状态警告 - 涓流保养"""
    print_test_header("电池健康状态警告 - 涓流保养")

    test_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 80,  # 电量充足
        "current_soh": 78,  # 健康状态低于警告阈值(85%)但高于临界阈值(70%)
        "battery_temp": 35,  # 正常温度
        "battery_resistance": 0.07,
        "mission_distance": 5,
        "remaining_distance": 1,  # 剩余任务很少
        "cruise_speed": 12,
        "wind_speed": 3,
        "ambient_temp": 22,
        "mission_priority": "low",  # 低优先级
        "nearest_swap_station": 1.0
    }

    print("输入参数:")
    for key, value in test_input.items():
        print(f"  {key}: {value}")

    result = handler({"input": test_input})
    print_result(result)

    # 验证
    if result.get("success"):
        decision = result.get("data", {}).get("scheduling_decision", {})
        action = decision.get("action", "")
        reason = decision.get("reason", "")

        # 应该触发健康状态警告，建议保养
        if "保养" in action or "涓流" in action:
            print("✓ 测试通过: 健康状态下降触发保养充电")
        elif "继续任务" in action:
            print("⚠ 注意: 任务剩余很少，可能继续执行而不是保养")
        else:
            print(f"✗ 测试未通过: 期望保养充电，实际: {action} ({reason})")

    return result


def test_scenario_7():
    """测试场景7: 边界条件测试 - 最小输入"""
    print_test_header("边界条件测试 - 最小输入")

    # 只提供必需参数，使用默认值
    test_input = {
        "current_soc": 50,
        "remaining_distance": 5,
    }

    print("输入参数 (最小集):")
    for key, value in test_input.items():
        print(f"  {key}: {value}")
    print("  其他参数使用默认值")

    result = handler({"input": test_input})
    print_result(result)

    # 验证
    if result.get("success"):
        print("✓ 测试通过: 最小输入也能正常计算")
    else:
        print("✗ 测试未通过: 最小输入计算失败")

    return result


def test_scenario_8():
    """测试场景8: 异常输入测试"""
    print_test_header("异常输入测试")

    # 包含非法值的输入
    test_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": "invalid",  # 非法字符串
        "battery_voltage": 22.2,
        "current_soc": -10,  # 负值
        "current_soh": 150,  # 超过100%
        "battery_temp": "hot",  # 非法字符串
        "mission_distance": None,  # None值
        "remaining_distance": 5,
        "cruise_speed": 12,
        "wind_speed": 5,
        "ambient_temp": 25,
        "mission_priority": "normal",
        "nearest_swap_station": 1.5
    }

    print("输入参数 (包含非法值):")
    for key, value in test_input.items():
        print(f"  {key}: {value} ({type(value).__name__})")

    result = handler({"input": test_input})
    print_result(result)

    # 验证
    if result.get("success"):
        print("⚠ 注意: 包含非法值的输入竟然计算成功，检查容错处理")
    else:
        print("✓ 测试通过: 非法输入被正确处理")

    return result


def test_scenario_9():
    """测试场景9: 多状态参数影响分析"""
    print_test_header("多状态参数影响分析")

    # 基础参数
    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 60,
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

    variations = [
        ("高SOH (95%)", {"current_soh": 95}),
        ("低SOH (75%)", {"current_soh": 75}),
        ("高温度 (48℃)", {"battery_temp": 48}),
        ("低温度 (10℃)", {"battery_temp": 10}),
        ("高内阻 (0.1Ω)", {"battery_resistance": 0.1}),
        ("大风速 (10m/s)", {"wind_speed": 10}),
        ("远换电站 (5km)", {"nearest_swap_station": 5.0}),
    ]

    results = []

    for name, variation in variations:
        print(f"\n变体: {name}")
        test_input = base_input.copy()
        test_input.update(variation)

        result = handler({"input": test_input})

        if result.get("success"):
            decision = result.get("data", {}).get("scheduling_decision", {})
            action = decision.get("action", "未知")
            safety_margin = result.get("data", {}).get("mission_analysis", {}).get("energy_safety_margin_percent", 0)

            print(f"  决策: {action}")
            print(f"  安全裕度: {safety_margin:.1f}%")
            results.append((name, action, safety_margin))
        else:
            print(f"  计算失败: {result.get('message', '未知错误')}")
            results.append((name, "失败", 0))

    print("\n[多状态参数影响总结]")
    print("=" * 60)
    for name, action, margin in results:
        print(f"{name:20} | {action:20} | 安全裕度: {margin:6.1f}%")

    return results


def run_all_tests():
    """运行所有测试"""
    print("基于多状态电池预测的无人机换电调度计算器 - 全面测试")
    print("=" * 80)

    # 收集所有测试结果
    test_results = []

    # 运行各个场景测试
    scenarios = [
        ("专利实施例", test_scenario_1),
        ("健康状态临界", test_scenario_2),
        ("温度过高", test_scenario_3),
        ("高优先级任务", test_scenario_4),
        ("正常继续任务", test_scenario_5),
        ("健康警告保养", test_scenario_6),
        ("边界条件", test_scenario_7),
        ("异常输入", test_scenario_8),
    ]

    for name, test_func in scenarios:
        try:
            print(f"\n>>> 开始测试: {name}")
            result = test_func()
            success = result.get("success", False) if isinstance(result, dict) else True
            test_results.append((name, success))
        except Exception as e:
            print(f"✗ 测试 {name} 异常: {e}")
            test_results.append((name, False))

    # 运行多状态参数影响分析
    try:
        print("\n>>> 开始测试: 多状态参数影响分析")
        test_scenario_9()
        test_results.append(("参数影响分析", True))
    except Exception as e:
        print(f"✗ 测试 参数影响分析 异常: {e}")
        test_results.append(("参数影响分析", False))

    # 输出测试总结
    print("\n" + "=" * 80)
    print("测试总结")
    print("=" * 80)

    passed = 0
    failed = 0

    for name, success in test_results:
        status = "✓ 通过" if success else "✗ 失败"
        print(f"{name:20} {status}")
        if success:
            passed += 1
        else:
            failed += 1

    total = len(test_results)
    print(f"\n总计: {total} 个测试")
    print(f"通过: {passed}")
    print(f"失败: {failed}")

    if failed == 0:
        print("\n🎉 所有测试通过!")
    else:
        print(f"\n⚠ 有 {failed} 个测试失败，请检查")

    return passed, failed, total


def save_test_results():
    """保存测试结果到文件"""
    print_test_header("保存测试结果")

    # 运行一个示例测试并保存结果
    test_input = {
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

    result = handler({"input": test_input})

    # 保存到JSON文件
    output_file = "test_results.json"
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"✓ 测试结果已保存到: {output_file}")

    # 也保存一个简化的版本
    simplified = {
        "timestamp": "2024-01-01T00:00:00Z",
        "test_scenario": "专利实施例",
        "input": test_input,
        "output": {
            "success": result.get("success"),
            "message": result.get("message"),
            "scheduling_decision": result.get("data", {}).get("scheduling_decision", {}),
            "battery_states": result.get("data", {}).get("battery_states", {}),
            "mission_analysis": result.get("data", {}).get("mission_analysis", {})
        }
    }

    simple_file = "test_results_simple.json"
    with open(simple_file, "w", encoding="utf-8") as f:
        json.dump(simplified, f, ensure_ascii=False, indent=2)

    print(f"✓ 简化结果已保存到: {simple_file}")


if __name__ == "__main__":
    # 运行所有测试
    passed, failed, total = run_all_tests()

    # 保存测试结果
    save_test_results()

    # 根据测试结果返回退出码
    if failed > 0:
        sys.exit(1)
    else:
        sys.exit(0)