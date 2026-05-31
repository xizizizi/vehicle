-- 创建数据库
CREATE DATABASE vue DEFAULT CHARACTER SET utf8;

-- 创建数据库表
CREATE TABLE user(
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID,主键自增',
    username VARCHAR(100) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    avatar VARCHAR(255) DEFAULT 'https://img1.baidu.com/it/u=1637142835,3949135594&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500' COMMENT '头像',
    token VARCHAR(255) UNIQUE NOT NULL COMMENT '令牌'
)DEFAULT CHARSET='UTF8' COMMENT='用户表';

-- 角色表
CREATE TABLE role(
    role_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色编号, 主键自增',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名'
)DEFAULT CHARSET='UTF8' COMMENT='角色表';


-- 用户角色关系表
CREATE TABLE role(
     role_id INT NOT NULL COMMENT '角色编号',
     user_id INT NOT NULL COMMENT '用户编号',
    KEY(role_id, user_id),
    FOREIGN KEY (role_id) REFERENCES role(role_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
)DEFAULT CHARSET='UTF8' COMMENT='角色表';

CREATE TABLE menu(
                     menu_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单编号,主键自增',
                     name VARCHAR(100) DEFAULT NULL COMMENT '组件名称',
                     redirect VARCHAR(100) DEFAULT NULL COMMENT '重定向地址',
                     path VARCHAR(255) DEFAULT NULL COMMENT '组件地址',
                     component VARCHAR(255) DEFAULT NULL COMMENT '组件对象',
                     meta json DEFAULT NULL COMMENT '路由的元配置',
                     parent_id INT DEFAULT 0 COMMENT '父ID,关联菜单主键,默认一级残带'
)DEFAULT CHARSET='UTF8' COMMENT='菜单表';


-- 区域基础表
CREATE TABLE areas (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(50) NOT NULL COMMENT '区域名称',
                       type ENUM(
                           '桥梁',
                           '涵洞隧道',
                           '交通干道',
                           '商业区',
                           '住宅区',
                           '工业区',
                           '自然区域',
                           '特殊区域'
                           ) NOT NULL COMMENT '区域类型',
                       unit ENUM('平方公里', '座', '个', '条', '公顷') NOT NULL COMMENT '计量单位',
                       base_value DECIMAL(12, 2) NOT NULL COMMENT '基础数值',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 巡查上报类型表 (独立出来保证数据一致性)
CREATE TABLE report_types (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              type_name VARCHAR(20) NOT NULL UNIQUE COMMENT '上报类型名称'
);

-- 预置上报类型数据
INSERT INTO report_types (type_name) VALUES
                                         ('日常养护'), ('交通事故'), ('路面问题'), ('桥通问题'), ('农林病害'),
                                         ('路基问题'), ('交安设施'), ('除雪作业'), ('绿化养护');

-- 巡查上报记录表
CREATE TABLE inspection_reports (
                                    id INT PRIMARY KEY AUTO_INCREMENT,
                                    area_id INT NOT NULL COMMENT '关联区域',
                                    report_type_id INT NOT NULL COMMENT '上报类型',
                                    quantity INT UNSIGNED NOT NULL DEFAULT 1 COMMENT '上报数量',
                                    report_time DATETIME NOT NULL COMMENT '上报时间',
                                    details TEXT COMMENT '详情描述',
                                    FOREIGN KEY (area_id) REFERENCES areas(id),
                                    FOREIGN KEY (report_type_id) REFERENCES report_types(id),
                                    INDEX idx_report_time (report_time)
);

-- 任务类型表 (独立出来保证数据一致性)
CREATE TABLE task_types (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            type_name VARCHAR(20) NOT NULL UNIQUE COMMENT '任务类型名称'
);

-- 预置任务类型数据
INSERT INTO task_types (type_name) VALUES
                                       ('交通事故处理'), ('消防救援'), ('测绘勘测'), ('物流运输'), ('安防巡逻'),
                                       ('活动拍摄'), ('环境调查'), ('设备维护'), ('农林监测');

-- 调度任务表
CREATE TABLE dispatch_tasks (
                                id INT PRIMARY KEY AUTO_INCREMENT,
                                task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
                                task_type_id INT NOT NULL COMMENT '任务类型',
                                task_time DATETIME NOT NULL COMMENT '任务时间',
                                quantity INT NOT NULL DEFAULT 1 COMMENT '任务数量',
                                status ENUM('已完成', '执行中', '待处理') NOT NULL DEFAULT '待处理',
                                FOREIGN KEY (task_type_id) REFERENCES task_types(id),
                                INDEX idx_task_time (task_time),
                                INDEX idx_status (status)
);

-- 区域统计表 (每日快照)
CREATE TABLE area_daily_stats (
                                  id INT PRIMARY KEY AUTO_INCREMENT,
                                  area_id INT NOT NULL COMMENT '关联区域',
                                  stat_date DATE NOT NULL COMMENT '统计日期',
                                  total_area DECIMAL(12, 2) NOT NULL COMMENT '区域总面积',
                                  report_count INT NOT NULL DEFAULT 0 COMMENT '上报总数',
                                  abnormal_count INT NOT NULL DEFAULT 0 COMMENT '异常数量',
                                  FOREIGN KEY (area_id) REFERENCES areas(id),
                                  UNIQUE KEY uniq_area_date (area_id, stat_date)
);



/* DigitalFlop组件 */
SELECT type AS title, base_value AS number, unit
FROM areas;

/* RankingBoard组件 */
SELECT t.type_name AS name, COUNT(r.id) AS value
FROM report_types t
         LEFT JOIN inspection_reports r ON t.id = r.report_type_id
GROUP BY t.id
ORDER BY value DESC;

/* RoseChart组件 */
SELECT t.type_name AS name, COUNT(*) AS value
FROM dispatch_tasks d
         JOIN task_types t ON d.task_type_id = t.id
WHERE d.status = '已完成'
GROUP BY t.id;

/* ScrollBoard组件 */
SELECT task_time, task_name, quantity, status
FROM dispatch_tasks
ORDER BY task_time DESC
LIMIT 10;

/* Cards组件 */
SELECT
    a.name AS title,
    a.base_value AS total,
    s.report_count,
    ROUND((s.abnormal_count / s.report_count) * 100) AS abnormal_ratio
FROM area_daily_stats s
         JOIN areas a ON s.area_id = a.id
WHERE s.stat_date = CURDATE();

START TRANSACTION;

-- 1. 创建备份
CREATE TABLE areas_backup AS SELECT * FROM areas;

-- 2. 添加临时列
ALTER TABLE areas ADD COLUMN temp_type VARCHAR(20);
ALTER TABLE areas ADD COLUMN temp_unit VARCHAR(20);

-- 3. 转换数据
UPDATE areas SET temp_type =
                     CASE TYPE
                         WHEN '桥梁' THEN 'BRIDGE'
                         WHEN '涵洞隧道' THEN 'TUNNEL'
                         WHEN '交通干道' THEN 'ROAD'
                         WHEN '商业区' THEN 'COMMERCIAL'
                         WHEN '住宅区' THEN 'RESIDENTIAL'
                         WHEN '工业区' THEN 'INDUSTRIAL'
                         WHEN '自然区域' THEN 'NATURAL'
                         WHEN '特殊区域' THEN 'SPECIAL'
                         ELSE 'SPECIAL'
                         END;

UPDATE areas SET temp_unit =
                     CASE unit
                         WHEN '平方公里' THEN 'SQUARE_KM'
                         WHEN '座' THEN 'SEAT'
                         WHEN '个' THEN 'PIECE'
                         WHEN '条' THEN 'ROAD'
                         WHEN '公顷' THEN 'HECTARE'
                         ELSE 'HECTARE'
                         END;

-- 4. 删除旧列
ALTER TABLE areas DROP COLUMN TYPE;
ALTER TABLE areas DROP COLUMN unit;

-- 5. 重命名新列
ALTER TABLE areas CHANGE temp_type TYPE VARCHAR(20);
ALTER TABLE areas CHANGE temp_unit unit VARCHAR(20);

-- 6. 修改为 ENUM 类型
ALTER TABLE areas
    MODIFY COLUMN TYPE ENUM(
        'BRIDGE',
        'TUNNEL',
        'ROAD',
        'COMMERCIAL',
        'RESIDENTIAL',
        'INDUSTRIAL',
        'NATURAL',
        'SPECIAL'
        ) NOT NULL COMMENT '区域类型';

ALTER TABLE areas
    MODIFY COLUMN unit ENUM(
        'SQUARE_KM',
        'SEAT',
        'PIECE',
        'ROAD',
        'HECTARE'
        ) NOT NULL COMMENT '计量单位';

-- 7. 验证数据
SELECT * FROM areas;

COMMIT;


DELIMITER $$

CREATE TRIGGER after_insert_report
    AFTER INSERT ON inspection_reports
    FOR EACH ROW
BEGIN
    -- 更新或插入相应区域的统计数据
    INSERT INTO area_daily_stats (area_id, stat_date, total_area, report_count, abnormal_count)
    VALUES (
               NEW.area_id,
               CURDATE(),  -- 假设我们按当天统计
               (SELECT base_value FROM areas WHERE id = NEW.area_id),  -- 获取区域面积
               1,  -- 每次插入新的上报记录时，加1
               IF(NEW.report_type_id IN (SELECT id FROM report_types WHERE type_name IN ('BRIDGE', 'TUNNEL', 'ROAD', 'COMMERCIAL', 'RESIDENTIAL', 'INDUSTRIAL', 'NATURAL', 'SPECIAL')), 1, 0)  -- 假设这几个类型是异常类型
           )
    ON DUPLICATE KEY UPDATE
                         report_count = report_count + 1,
                         abnormal_count = abnormal_count + IF(NEW.report_type_id IN (SELECT id FROM report_types WHERE type_name IN ('BRIDGE', 'TUNNEL', 'ROAD', 'COMMERCIAL', 'RESIDENTIAL', 'INDUSTRIAL', 'NATURAL', 'SPECIAL')), 1, 0);
END$$

DELIMITER ;

DELIMITER $$

CREATE TRIGGER after_update_report
    AFTER UPDATE ON inspection_reports
    FOR EACH ROW
BEGIN
    -- 更新或插入相应区域的统计数据
    INSERT INTO area_daily_stats (area_id, stat_date, total_area, report_count, abnormal_count)
    VALUES (
               NEW.area_id,
               CURDATE(),  -- 假设我们按当天统计
               (SELECT base_value FROM areas WHERE id = NEW.area_id),  -- 获取区域面积
               1,  -- 每次插入新的上报记录时，加1
               IF(NEW.report_type_id IN (SELECT id FROM report_types WHERE type_name IN ('BRIDGE', 'TUNNEL', 'ROAD', 'COMMERCIAL', 'RESIDENTIAL', 'INDUSTRIAL', 'NATURAL', 'SPECIAL')), 1, 0)  -- 假设这几个类型是异常类型
           )
    ON DUPLICATE KEY UPDATE
                         report_count = report_count + 1,
                         abnormal_count = abnormal_count + IF(NEW.report_type_id IN (SELECT id FROM report_types WHERE type_name IN ('BRIDGE', 'TUNNEL', 'ROAD', 'COMMERCIAL', 'RESIDENTIAL', 'INDUSTRIAL', 'NATURAL', 'SPECIAL')), 1, 0);
END$$

DELIMITER ;
DELIMITER $$

CREATE TRIGGER after_delete_report
    AFTER DELETE ON inspection_reports
    FOR EACH ROW
BEGIN
    -- 减少统计中的报告数量和异常数量
    UPDATE area_daily_stats
    SET
        report_count = report_count - 1,
        abnormal_count = abnormal_count - IF(OLD.report_type_id IN (SELECT id FROM report_types WHERE type_name IN ('BRIDGE', 'TUNNEL', 'ROAD', 'COMMERCIAL', 'RESIDENTIAL', 'INDUSTRIAL', 'NATURAL', 'SPECIAL')), 1, 0)
    WHERE area_id = OLD.area_id AND stat_date = CURDATE();
END$$

DELIMITER ;
