# 无人机调度与智能任务规划系统部署指南

## 项目简介

本系统是一个基于人工智能的无人机集群调度与智能任务规划平台，集成了以下核心功能：

- **多状态电池健康预测**：基于 LSTM 模型，融合电压、电流、温度、内阻、环境风速等参数，实现 SOC 预测误差 ≤3%、SOH 估计误差 ≤2%
- **多目标智能调度**：改进蚁群算法，综合距离、能耗、负载、电池健康度，支持突发任务动态插入
- **云-边-端协同架构**：前端 Vue + 百度地图可视化，后端 Spring Boot 业务逻辑 + Flask 算法服务
- **多场景业务模块**：物流管理、医疗救助、森林巡检、电力巡检、巡护数据管理、AI 智能助手

本项目完全由学生团队开发，已获得多项发明专利和软件著作权。

---

## 系统架构

```
前端 (Vue.js)  →  Nginx (可选)  →  Spring Boot (Java, 端口 8080)
                                        ↓
                                   MySQL (端口 3306)
                                   Redis (端口 6379)

Flask (Python, 端口 5000) ← 算法服务 (聚类、预测等)
```

---

## 环境要求

| 组件         | 版本要求          | 说明                       |
| ------------ | ----------------- | -------------------------- |
| JDK          | 17                | Spring Boot 运行环境       |
| Python       | 3.9               | Flask 算法服务             |
| Node.js      | 18.x              | 前端构建工具               |
| MySQL        | 8.0               | 主数据库                   |
| Redis        | 6.x               | 缓存（可选，默认需启动）   |
| Maven        | 3.8+              | Java 项目构建              |
| Git          | 任意              | 克隆代码                   |

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/xizizizi/vehicle.git
cd vehicle
```

项目主要目录结构：

```
vehicle/
├── backed/
│   ├── vehicle/                # Java Spring Boot 后端
│   │   └── vehicle_admin/      # 主工程（含 pom.xml）
│   └── uav-location-system/    # Python 聚类服务
│       └── backend/
│           └── app_real.py     # Flask 主程序
├── vehicle/                    # Vue 前端项目
│   ├── src/
│   └── package.json
└── database/
    └── vue.sql                 # 数据库初始化脚本
```

### 2. 数据库初始化

- 启动 MySQL 服务，创建数据库：

```sql
CREATE DATABASE vue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

- 导入初始化脚本（如果已有数据文件）：

```bash
mysql -u root -p vue < database/vue.sql
```

若没有现成脚本，Spring Boot 项目启动时会自动建表（需配置 `spring.jpa.hibernate.ddl-auto=update`）。

### 3. 配置 Redis（可选但推荐）

从 [Microsoft Archive Redis](https://github.com/microsoftarchive/redis/releases) 下载 Windows 安装包（或使用 Linux 包管理器安装）。

启动 Redis：

```bash
redis-server
```

默认端口 6379，无需密码（若需要密码，请修改后端配置）。

### 4. 启动 Java 后端

- 进入后端目录：

```bash
cd backed/vehicle/vehicle_admin
```

- 修改配置文件 `src/main/resources/application.properties`，确保数据库、Redis 等配置正确：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vue?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的数据库密码
spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379
# Coze API 配置（可选）
coze.api-key=你的Coze令牌
coze.bot-id=你的Bot ID
```

- 打包并运行：

```bash
mvn clean package -DskipTests
cd target
java -jar vehicle_admin-0.0.1-SNAPSHOT.jar
```

后端默认监听 `http://localhost:8080`。

### 5. 启动 Python 聚类服务

- 进入 Flask 服务目录：

```bash
cd backed/uav-location-system/backend
```

- 安装依赖（推荐使用虚拟环境）：

```bash
pip install -r requirements.txt
# 或手动安装
pip install flask flask-cors scikit-learn numpy requests
```

- 修改 `app_real.py` 中的百度地图 AK（如需实时 POI 数据）：

```python
BAIDU_MAP_AK = "你的百度地图AK"
```

- 启动服务：

```bash
python app_real.py
```

默认监听 `http://localhost:5000`，输出 `Running on http://0.0.0.0:5000` 表示成功。

### 6. 启动前端

- 进入前端目录：

```bash
cd vehicle
```

- 安装依赖：

```bash
npm install
# 如果速度慢，使用淘宝镜像
npm install cnpm -g --registry=https://registry.npmmirror.com
cnpm install
```

- 修改后端 API 地址（`src/server/config.ts` 或 `.env.development`）：

```javascript
export const baseURL = 'http://localhost:8080/api'       // Java 后端地址
export const flaskBaseURL = 'http://localhost:5000'      // Flask 地址
```

- 启动开发服务器：

```bash
npm run serve
```

访问 `http://localhost:8080`（默认前端端口），使用默认账号 `admin` / `11111` 登录。

---

## 生产环境部署（可选）

若需部署到服务器，建议使用 Nginx 反向代理：

1. 前端打包：

```bash
npm run build:prod
```

将生成的 `dist` 目录内容复制到 Nginx 的 `html` 目录下。

2. 配置 Nginx 反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
    }
    location /flask/ {
        proxy_pass http://localhost:5000/;
    }
}
```

3. 使用 `nohup` 或 systemd 后台运行 Java 和 Flask 服务。

---

## 验证系统是否正常

- **Java 后端**：访问 `http://localhost:8080/api/health`（如果存在），应返回 JSON 状态。
- **Flask 服务**：访问 `http://localhost:5000/api/health`，应返回 `{"status":"healthy"}`。
- **前端**：在地图上尝试规划路径或进行聚类分析，观察是否正常调用后端接口。

---

## 常见问题

### 1. 端口被占用

- 默认端口：8080（Java）、5000（Flask）、3306（MySQL）、6379（Redis）。
- 如需修改，请在相应配置文件中调整。

### 2. 数据库连接失败

- 检查 MySQL 服务是否启动。
- 确认用户名、密码、数据库名是否正确。
- 若使用云服务器，需在安全组开放 3306 端口。

### 3. npm install 失败

- 更换淘宝镜像：`npm config set registry https://registry.npmmirror.com`。

### 4. 百度地图无法显示

- 检查 `index.html` 中引入的百度地图 AK 是否有效。

### 5. Redis 未启动导致 Spring Boot 报错

- 如果不需要 Redis，可在 `application.properties` 中注释掉 Redis 相关配置，并移除 `@EnableCaching` 注解（若启用）。

---

## 更多帮助

- 项目文档详见 `docs/` 目录。
- 如有问题，欢迎提交 [Issue](https://github.com/xizizizi/vehicle/issues) 或联系项目团队。

---

**祝您使用愉快！**
