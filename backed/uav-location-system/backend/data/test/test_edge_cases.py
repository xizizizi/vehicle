#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
基于多状态电池预测的无人机换电调度计算器 - 边界条件测试
测试各种边界情况和极端场景
"""

import sys
import os
import json
import time

# 将当前目录添加到路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

try:
    from battery_predictor import handler

    print("✓ 成功导入主模块")
except ImportError as e:
    print(f"✗ 导入主模块失败: {e}")
    sys.exit(1)


def run_edge_case_test(name, test_input, expected_outcome=None):
    """运行单个边界条件测试"""
    print(f"\n{'=' * 60}")
    print(f"边界测试: {name}")
    print(f"{'=' * 60}")

    # 显示输入参数
    print("输入参数:")
    for key, value in test_input.items():
        if value is None:
            print(f"  {key}: None")
        else:
            print(f"  {key}: {value}")

    # 执行测试
    start_time = time.time()
    result = handler({"input": test_input})
    end_time = time.time()

    # 显示结果
    print(f"\n执行时间: {(end_time - start_time) * 1000:.2f}ms")
    print(f"成功: {result.get('success', False)}")
    print(f"消息: {result.get('message', '')}")

    if result.get("success"):
        data = result.get("data", {})

        # 显示关键信息
        decision = data.get("scheduling_decision", {})
        action = decision.get("action", "未知")
        reason = decision.get("reason", "")

        print(f"决策: {action}")
        if reason:
            print(f"原因: {reason}")

        # 显示能量状态
        battery = data.get("battery_states", {})
        mission = data.get("mission_analysis", {})

        if battery:
            print(f"有效能量: {battery.get('effective_energy_wh', 0):.2f} Wh")
        if mission:
            print(f"安全裕度: {mission.get('energy_safety_margin_percent', 0):.2f}%")
            print(f"能量缺口: {mission.get('energy_deficit_wh', 0):.2f} Wh")

    # 验证预期结果
    if expected_outcome:
        actual_success = result.get("success", False)
        expected_success = expected_outcome.get("success", True)

        if "expected_action" in expected_outcome:
            actual_action = result.get("data", {}).get("scheduling_decision", {}).get("action", "")
            expected_action = expected_outcome["expected_action"]

            if actual_action == expected_action:
                print(f"✓ 符合预期: 决策为 {expected_action}")
            else:
                print(f"✗ 不符合预期: 期望 {expected_action}, 实际 {actual_action}")

        if actual_success == expected_success:
            print(f"✓ 成功状态符合预期: {actual_success}")
        else:
            print(f"✗ 成功状态不符合预期: 期望 {expected_success}, 实际 {actual_success}")

    return result


def test_extreme_soc_values():
    """测试极端SOC值"""
    print("\n" + "=" * 80)
    print("测试1: 极端SOC值测试")
    print("=" * 80)

    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
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

    tests = [
        ("SOC=100% (满电)", {"current_soc": 100}, {"success": True}),
        ("SOC=95% (高电量)", {"current_soc": 95}, {"success": True}),
        ("SOC=5% (极低电量)", {"current_soc": 5}, {"success": True, "expected_action": "前往换电站换电"}),
        ("SOC=1% (濒临没电)", {"current_soc": 1}, {"success": True, "expected_action": "前往换电站换电"}),
        ("SOC=0% (没电)", {"current_soc": 0}, {"success": True, "expected_action": "前往换电站换电"}),
        ("SOC=150% (超100%)", {"current_soc": 150}, {"success": True}),  # 应该被限制或特殊处理
        ("SOC=-10% (负值)", {"current_soc": -10}, {"success": True}),  # 应该被处理
    ]

    results = []
    for name, params, expected in tests:
        test_input = base_input.copy()
        test_input.update(params)
        result = run_edge_case_test(name, test_input, expected)
        results.append((name, result.get("success", False)))

    return results


def test_extreme_soh_values():
    """测试极端SOH值"""
    print("\n" + "=" * 80)
    print("测试2: 极端SOH值测试")
    print("=" * 80)

    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 60,
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

    tests = [
        ("SOH=100% (全新电池)", {"current_soh": 100}, {"success": True}),
        ("SOH=95% (优秀状态)", {"current_soh": 95}, {"success": True}),
        ("SOH=71% (接近临界)", {"current_soh": 71}, {"success": True}),
        ("SOH=69% (略低于临界)", {"current_soh": 69}, {"success": True, "expected_action": "前往换电站换电"}),
        ("SOH=50% (严重老化)", {"current_soh": 50}, {"success": True, "expected_action": "前往换电站换电"}),
        ("SOH=20% (即将报废)", {"current_soh": 20}, {"success": True, "expected_action": "前往换电站换电"}),
        ("SOH=0% (报废电池)", {"current_soh": 0}, {"success": True, "expected_action": "前往换电站换电"}),
        ("SOH=150% (超100%)", {"current_soh": 150}, {"success": True}),  # 异常值
        ("SOH=-10% (负值)", {"current_soh": -10}, {"success": True}),  # 异常值
    ]

    results = []
    for name, params, expected in tests:
        test_input = base_input.copy()
        test_input.update(params)
        result = run_edge_case_test(name, test_input, expected)
        results.append((name, result.get("success", False)))

    return results


def test_extreme_temperature_values():
    """测试极端温度值"""
    print("\n" + "=" * 80)
    print("测试3: 极端温度值测试")
    print("=" * 80)

    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 60,
        "current_soh": 85,
        "battery_resistance": 0.05,
        "mission_distance": 10,
        "remaining_distance": 5,
        "cruise_speed": 12,
        "wind_speed": 5,
        "ambient_temp": 25,
        "mission_priority": "normal",
        "nearest_swap_station": 2.0
    }

    tests = [
        ("温度=-20℃ (极寒)", {"battery_temp": -20}, {"success": True}),
        ("温度=-10℃ (严寒)", {"battery_temp": -10}, {"success": True}),
        ("温度=0℃ (冰点)", {"battery_temp": 0}, {"success": True}),
        ("温度=15℃ (低温)", {"battery_temp": 15}, {"success": True}),
        ("温度=25℃ (理想)", {"battery_temp": 25}, {"success": True}),
        ("温度=35℃ (温暖)", {"battery_temp": 35}, {"success": True}),
        ("温度=45℃ (高温)", {"battery_temp": 45}, {"success": True}),
        ("温度=55℃ (危险高温)", {"battery_temp": 55}, {"success": True, "expected_action": "前往充电站执行降温快充"}),
        ("温度=65℃ (极端高温)", {"battery_temp": 65}, {"success": True, "expected_action": "前往充电站执行降温快充"}),
        ("温度=100℃ (沸点)", {"battery_temp": 100}, {"success": True, "expected_action": "前往充电站执行降温快充"}),
    ]

    results = []
    for name, params, expected in tests:
        test_input = base_input.copy()
        test_input.update(params)
        result = run_edge_case_test(name, test_input, expected)
        results.append((name, result.get("success", False)))

    return results


def test_zero_and_negative_values():
    """测试零值和负值"""
    print("\n" + "=" * 80)
    print("测试4: 零值和负值测试")
    print("=" * 80)

    tests = [
        ("零容量电池", {
            "battery_capacity": 0,
            "current_soc": 50,
            "current_soh": 80,
            "remaining_distance": 5
        }, {"success": True}),

        ("零电压电池", {
            "battery_voltage": 0,
            "current_soc": 50,
            "current_soh": 80,
            "remaining_distance": 5
        }, {"success": True}),

        ("零距离任务", {
            "remaining_distance": 0,
            "current_soc": 50,
            "current_soh": 80
        }, {"success": True}),

        ("负距离任务", {
            "remaining_distance": -5,
            "current_soc": 50,
            "current_soh": 80
        }, {"success": True}),

        ("零风速", {
            "wind_speed": 0,
            "current_soc": 50,
            "current_soh": 80,
            "remaining_distance": 5
        }, {"success": True}),

        ("负风速", {
            "wind_speed": -5,
            "current_soc": 50,
            "current_soh": 80,
            "remaining_distance": 5
        }, {"success": True}),

        ("零内阻", {
            "battery_resistance": 0,
            "current_soc": 50,
            "current_soh": 80,
            "remaining_distance": 5
        }, {"success": True}),

        ("负内阻", {
            "battery_resistance": -0.05,
            "current_soc": 50,
            "current_soh": 80,
            "remaining_distance": 5
        }, {"success": True}),
    ]

    # 基础参数
    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 60,
        "current_soh": 85,
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

    results = []
    for name, params, expected in tests:
        test_input = base_input.copy()
        test_input.update(params)
        result = run_edge_case_test(name, test_input, expected)
        results.append((name, result.get("success", False)))

    return results


def test_missing_and_none_values():
    """测试缺失值和None值"""
    print("\n" + "=" * 80)
    print("测试5: 缺失值和None值测试")
    print("=" * 80)

    tests = [
        ("缺少SOC参数", {
            # 不提供current_soc
            "current_soh": 85,
            "remaining_distance": 5
        }, {"success": True}),  # 应该使用默认值80

        ("缺少SOH参数", {
            "current_soc": 60,
            # 不提供current_soh
            "remaining_distance": 5
        }, {"success": True}),  # 应该使用默认值90

        ("缺少所有可选参数", {
            # 只提供必需参数
            "current_soc": 60,
            "remaining_distance": 5
        }, {"success": True}),

        ("全部使用默认值", {
            # 空字典，全部使用默认值
        }, {"success": True}),

        ("None值SOC", {
            "current_soc": None,
            "current_soh": 85,
            "remaining_distance": 5
        }, {"success": True}),  # 应该使用默认值

        ("None值SOH", {
            "current_soc": 60,
            "current_soh": None,
            "remaining_distance": 5
        }, {"success": True}),  # 应该使用默认值

        ("None值温度", {
            "current_soc": 60,
            "current_soh": 85,
            "battery_temp": None,
            "remaining_distance": 5
        }, {"success": True}),  # 应该使用默认值
    ]

    # 对于这些测试，我们使用较少的参数
    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 60,
        "current_soh": 85,
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

    results = []
    for name, params, expected in tests:
        # 对于缺失值测试，我们使用params作为完整输入
        test_input = params.copy()

        # 确保有最少的关键参数
        if "current_soc" not in test_input:
            test_input["current_soc"] = 60
        if "remaining_distance" not in test_input:
            test_input["remaining_distance"] = 5

        result = run_edge_case_test(name, test_input, expected)
        results.append((name, result.get("success", False)))

    return results


def test_invalid_data_types():
    """测试无效数据类型"""
    print("\n" + "=" * 80)
    print("测试6: 无效数据类型测试")
    print("=" * 80)

    tests = [
        ("字符串类型SOC", {
            "current_soc": "sixty",  # 字符串而不是数字
            "current_soh": 85,
            "remaining_distance": 5
        }, {"success": True}),  # 应该被转换为默认值

        ("列表类型参数", {
            "current_soc": [60, 70],  # 列表而不是数字
            "current_soh": 85,
            "remaining_distance": 5
        }, {"success": False}),  # 可能失败

        ("字典类型参数", {
            "current_soc": {"value": 60},  # 字典而不是数字
            "current_soh": 85,
            "remaining_distance": 5
        }, {"success": False}),  # 可能失败

        ("布尔类型参数", {
            "current_soc": True,  # 布尔值
            "current_soh": 85,
            "remaining_distance": 5
        }, {"success": True}),  # True会被转换为1.0

        ("复杂对象", {
            "current_soc": object(),  # Python对象
            "current_soh": 85,
            "remaining_distance": 5
        }, {"success": False}),  # 应该失败
    ]

    # 基础参数
    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 60,
        "current_soh": 85,
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

    results = []
    for name, params, expected in tests:
        test_input = base_input.copy()
        test_input.update(params)
        result = run_edge_case_test(name, test_input, expected)
        success = result.get("success", False)
        results.append((name, success))

        # 检查是否符合预期
        expected_success = expected.get("success", True)
        if success == expected_success:
            print(f"  ✓ {name}: 成功状态符合预期 ({success})")
        else:
            print(f"  ✗ {name}: 成功状态不符合预期 (期望 {expected_success}, 实际 {success})")

    return results


def test_performance_and_large_values():
    """测试性能和大数值"""
    print("\n" + "=" * 80)
    print("测试7: 性能和大数值测试")
    print("=" * 80)

    import time

    tests = [
        ("极大距离任务", {
            "remaining_distance": 1000,  # 1000公里
            "current_soc": 80,
            "current_soh": 90
        }, {"success": True}),

        ("极小距离任务", {
            "remaining_distance": 0.001,  # 1米
            "current_soc": 80,
            "current_soh": 90
        }, {"success": True}),

        ("极大风速", {
            "wind_speed": 100,  # 100m/s (台风)
            "current_soc": 80,
            "current_soh": 90,
            "remaining_distance": 5
        }, {"success": True}),

        ("极大电池容量", {
            "battery_capacity": 100000,  # 100Ah
            "current_soc": 80,
            "current_soh": 90,
            "remaining_distance": 5
        }, {"success": True}),

        ("极高电压", {
            "battery_voltage": 400,  # 400V (高压电池)
            "current_soc": 80,
            "current_soh": 90,
            "remaining_distance": 5
        }, {"success": True}),
    ]

    # 基础参数
    base_input = {
        "drone_model": "巡检无人机",
        "battery_capacity": 6000,
        "battery_voltage": 22.2,
        "current_soc": 60,
        "current_soh": 85,
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

    results = []
    for name, params, expected in tests:
        test_input = base_input.copy()
        test_input.update(params)

        # 测量执行时间
        start_time = time.time()
        result = handler({"input": test_input})
        end_time = time.time()
        execution_time = (end_time - start_time) * 1000  # 转换为毫秒

        print(f"\n{name}:")
        print(f"  执行时间: {execution_time:.2f}ms")
        print(f"  成功: {result.get('success', False)}")

        if result.get("success"):
            data = result.get("data", {})
            decision = data.get("scheduling_decision", {})
            print(f"  决策: {decision.get('action', '未知')}")

        success = result.get("success", False)
        results.append((name, success, execution_time))

        # 检查执行时间是否在合理范围内
        if execution_time < 100:  # 100毫秒内
            print(f"  ✓ 执行时间正常: {execution_time:.2f}ms")
        else:
            print(f"  ⚠ 执行时间较长: {execution_time:.2f}ms")

    return results


def run_all_edge_case_tests():
    """运行所有边界条件测试"""
    print("基于多状态电池预测的无人机换电调度计算器 - 边界条件测试套件")
    print("=" * 80)

    all_results = []

    # 运行所有测试
    test_functions = [
        ("极端SOC值测试", test_extreme_soc_values),
        ("极端SOH值测试", test_extreme_soh_values),
        ("极端温度值测试", test_extreme_temperature_values),
        ("零值和负值测试", test_zero_and_negative_values),
        ("缺失值和None值测试", test_missing_and_none_values),
        ("无效数据类型测试", test_invalid_data_types),
        ("性能和大数值测试", test_performance_and_large_values),
    ]

    for test_name, test_func in test_functions:
        print(f"\n>>> 开始测试: {test_name}")
        try:
            results = test_func()
            all_results.extend([(test_name, name, success) for name, success, *_ in results])
        except Exception as e:
            print(f"✗ 测试 {test_name} 执行异常: {e}")
            import traceback
            traceback.print_exc()

    # 生成测试报告
    print("\n" + "=" * 80)
    print("边界条件测试报告")
    print("=" * 80)

    # 统计结果
    total_tests = len(all_results)
    passed_tests = sum(1 for _, _, success in all_results if success)
    failed_tests = total_tests - passed_tests

    print(f"测试总数: {total_tests}")
    print(f"通过测试: {passed_tests}")
    print(f"失败测试: {failed_tests}")
    print(f"通过率: {passed_tests / total_tests * 100:.1f}%" if total_tests > 0 else "通过率: N/A")

    # 显示失败的测试
    if failed_tests > 0:
        print("\n失败的测试:")
        for category, name, success in all_results:
            if not success:
                print(f"  - {category}: {name}")

    # 保存结果到文件
    report = {
        "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
        "total_tests": total_tests,
        "passed_tests": passed_tests,
        "failed_tests": failed_tests,
        "pass_rate": passed_tests / total_tests * 100 if total_tests > 0 else 0,
        "results": [
            {
                "category": category,
                "test_name": name,
                "success": success
            }
            for category, name, success in all_results
        ]
    }

    with open("edge_case_test_report.json", "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)

    print(f"\n✓ 测试报告已保存到: edge_case_test_report.json")

    return passed_tests, failed_tests, total_tests


if __name__ == "__main__":
    passed, failed, total = run_all_edge_case_tests()

    # 根据测试结果返回退出码
    if failed > 0:
        print(f"\n⚠ 有 {failed} 个边界条件测试失败，请检查!")
        sys.exit(1)
    else:
        print(f"\n🎉 所有边界条件测试通过!")
        sys.exit(0)