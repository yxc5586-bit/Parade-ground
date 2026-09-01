# 程序员技术练兵场

程序员技术练兵场是一个面向程序员的 AI 闯关训练平台。项目把真实企业业务场景、技术方案选择、AI 出题、AI 判题和薪资成长反馈结合在一起，让用户通过一关关方案设计题训练需求分析、技术选型、架构权衡和工程落地能力。

平台不是传统选择题系统。每一关都会围绕一个业务需求展开，用户需要从多个候选技术方案、流程设计、组件选型、风险处理和工程实践中选出合理答案。系统会根据用户当前薪资动态生成不同难度的关卡，并在提交后给出评分、薪资变化、答案解析和模拟投递建议。

## 在线演示

- 演示站：[http://203.195.203.14/](http://203.195.203.14/)
- 健康检查：[http://203.195.203.14/api/actuator/health](http://203.195.203.14/api/actuator/health)

公网演示使用 OpenResty 托管前端并反向代理 Spring Boot API，MySQL、Redis 和后端 8123 端口只绑定服务器回环地址。演示账号密码不写入公开仓库，请使用站内已有账号或自行注册。

## 线上实测结果

测试时间：2026-09-01。以下数字均来自公网环境和可复核测试报告，不是估算值。

- 完成“登录 → AI 生成关卡 → 展示选项 → 提交 → AI 评分 → 薪资变化 → 历史记录 → 结果详情”真实闭环；实测得分 100，薪资从 10,000 增至 12,000。
- 同一用户、同一关卡顺序重复提交时返回同一条记录且薪资不再变化；20 次、10 并发重复提交全部成功并返回同一记录，数据库最终只有 1 条作答记录。
- 评分规则完成 4096 种选项组合穷举回归；重复作答、并发结算和事务相关核心测试通过。
- 30 组固定 AI 关卡输入覆盖 5 个薪资档位和 6 个技术方向，生成成功率、结构完整率、12～18 选项合规率、答案映射率和薪资档位匹配率均为 100%。
- 30 组关卡均生成 14 个选项；平均生成耗时 31.18 秒，P95 35.98 秒，最快 26.37 秒，最慢 36.98 秒。
- 认证接口 `GET /api/user/me`：100 请求、10 并发、100% 成功，吞吐 162.92 请求/秒，平均 56.02 ms，P50 51.96 ms，P95 93.36 ms，P99 97.33 ms。
- 当前业务接口没有暴露模型 Token usage，因此不在 README 中估算 Token 消耗。
- 每日 MySQL 备份已通过 gzip 完整性检查，并成功恢复到临时数据库；练兵场 30 个关卡和 1 条最终作答记录恢复一致。

线上服务还完成了 OpenResty/MySQL/Redis/Java 逐项重启、Redis Session 持久化、systemd 自动恢复、日志轮转、端口隔离和资源余量检查。

## 项目亮点

- AI 动态生成关卡：根据用户当前薪资和偏好方向生成贴近真实企业业务场景的技术题。
- 游戏化成长反馈：以月薪作为成长指标，答题表现会影响用户当前薪资。
- 拖拽式答题体验：用户将认为正确的选项拖入答题区，交互更接近闯关游戏。
- AI 智能判题报告：提交后生成得分、评价、薪资调整、标准答案和详细方案解析。
- 精选关卡系统：支持浏览精选关卡，复用高质量题目进行训练。
- 个人记录追踪：查看历史闯关记录、得分、薪资变化和详细报告。
- 管理员后台：支持关卡分页查询、编辑、删除、精选配置等管理能力。
- 安全配置管理：数据库密码、AI API Key 等敏感信息通过环境变量或本地配置文件注入。

## 技术栈

### 前端

- Vue 3
- Vue Router
- Element Plus
- Axios
- Markdown-It
- Vite

### 后端

- Java 21
- Spring Boot 3.5
- MyBatis-Plus
- MySQL
- Redis
- Spring Session Redis
- LangChain4j
- Knife4j / OpenAPI

## 项目结构

```text
Parade-ground-github
├── frontend                     # 前端项目
│   ├── src
│   │   ├── api                  # 接口请求封装
│   │   ├── components           # 通用组件
│   │   ├── router               # 前端路由
│   │   ├── styles               # 全局样式
│   │   ├── utils                # 工具函数
│   │   └── views                # 页面视图
│   ├── package.json
│   └── vite.config.js
├── backend                      # 后端项目
│   ├── src/main/java            # Java 业务代码
│   ├── src/main/resources
│   │   ├── mapper               # MyBatis XML
│   │   ├── prompts              # AI 提示词
│   │   ├── sql                  # 初始化 SQL
│   │   ├── application.yml      # 公开配置
│   │   └── application-example.yml
│   └── pom.xml
└── README.md
```

## 核心功能

### 用户能力

- 注册、登录、退出登录
- 查看当前薪资和训练状态
- 生成下一关技术训练题
- 浏览关卡详情和候选选项
- 拖拽选择方案并提交答案
- 查看 AI 生成的判题报告
- 查看历史闯关记录
- 浏览精选关卡

### AI 能力

- 按薪资区间控制关卡难度
- 生成真实业务场景题面
- 生成正确选项、错误选项、干扰项和迷惑项
- 根据用户答案输出评分
- 解释得分原因和正确方案
- 给出薪资调整和模拟投递建议

### 管理能力

- 关卡分页查询
- 编辑关卡信息
- 删除关卡
- 设置或取消精选关卡
- 管理关卡优先级

## 快速开始

### 1. 准备环境

请先准备以下环境：

- Node.js 22 或更高版本
- JDK 21
- Maven
- MySQL 8
- Redis

### 2. 初始化数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE `parade_ground` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后执行初始化脚本：

```text
backend/src/main/resources/sql/init_parade_ground.sql
```

### 3. 配置后端环境变量

后端默认会读取环境变量，也支持创建本地私密配置文件：

```text
backend/src/main/resources/application-local.yml
```

可以参考：

```text
backend/src/main/resources/application-example.yml
```

常用配置项：

```text
MYSQL_URL=jdbc:mysql://localhost:3306/parade_ground?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_mysql_password
REDIS_HOST=localhost
REDIS_PORT=6379
OPENROUTER_API_KEY=your_openrouter_api_key
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
OPENROUTER_MODEL_NAME=qwen/qwen-plus
```

注意：`application-local.yml`、`.env`、日志和构建产物已经被忽略，不会提交到 GitHub。

### 4. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

Windows 可以使用：

```bash
cd backend
mvnw.cmd spring-boot:run
```

后端默认地址：

```text
http://localhost:8123/api
```

接口文档地址：

```text
http://localhost:8123/api/doc.html
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

Vite 已配置 `/api` 代理到后端 `http://localhost:8123`。

## 构建与测试

前端构建：

```bash
cd frontend
npm run build
```

后端测试：

```bash
cd backend
./mvnw test
```

Windows：

```bash
cd backend
mvnw.cmd test
```

生产构建会生成固定文件名 `backend/target/parade-ground.jar`。腾讯云轻量应用服务器和 1Panel 的直接部署模板位于 `deploy/`，真实密码和 API Key 只写入服务器受限环境文件。



## 适用场景

- 程序员技术选型训练
- 系统设计和业务建模练习
- 面试前方案设计能力训练
- AI 生成题库和智能判题实践
- Vue 3 + Spring Boot 前后端分离项目学习

