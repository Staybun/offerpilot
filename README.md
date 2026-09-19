# OfferPilot

OfferPilot 是一个面向程序员面试备考的在线学习平台，提供题目与题库管理、内容检索、帖子互动、用户签到以及 AI 题目解析等能力。项目以 Spring Boot 为核心，通过 Redis、RabbitMQ、自定义线程池、HotKey 和 LangChain4j 完成缓存、异步解耦、热点保护与大模型接入。

## 核心功能

- 用户体系：基于 Sa-Token 实现注册登录、权限校验、多端登录控制和账号踢下线。
- 题目与题库：支持题目、题库及关联关系的增删改查、分页浏览和 MySQL 关键词检索。
- 帖子互动：支持帖子发布、点赞和收藏；高频统计更新通过 RabbitMQ 异步消费。
- 消息可靠性：消费端手动 ACK，以 `messageId` 做幂等校验，失败消息进入死信队列；定时任务每日校准点赞、收藏统计。
- 多级缓存：帖子详情采用 Cache Aside，结合 Bloom Filter、空值缓存和随机 TTL 防止缓存穿透、击穿与雪崩。
- 热点保护：使用 HotKey 识别热点题库并写入本地缓存，降低 Redis 与数据库压力。
- 签到统计：基于 Redis Bitmap 保存签到记录，使用 Redisson 分布式锁保护并发积分发放。
- 访问控制：使用 Redis + Lua 原子统计访问频率，对异常高频请求进行接口限流。
- AI 解析：通过 LangChain4j 接入 OpenAI 兼容模型，支持题目分析、多轮问答、会话记忆和工具调用。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 基础框架 | Spring Boot 2.7.2、Java 17、Maven |
| 数据访问 | MyBatis-Plus、MySQL、Druid |
| 登录鉴权 | Sa-Token |
| 缓存与并发 | Redis、Redisson、HotKey |
| 消息队列 | RabbitMQ |
| AI | LangChain4j 0.30.0、OpenAI 兼容接口 |
| 接口文档 | Knife4j |
| 部署 | Docker |

## 项目结构

```text
offerpilot
├─ sql/                         # 建表与初始化数据
├─ src/main/java/com/offerpilot
│  ├─ ai/                       # AI 助手、会话记忆与工具调用
│  ├─ controller/               # HTTP 接口
│  ├─ job/                      # BloomFilter 重建、统计校准任务
│  ├─ manager/                  # 计数器与缓存封装
│  ├─ mq/                       # RabbitMQ 生产、消费及死信队列
│  ├─ satoken/                  # 登录鉴权与权限实现
│  └─ service/                  # 业务服务
├─ src/main/resources
│  ├─ application.yml           # 开发环境配置
│  └─ application-prod.yml      # 生产环境配置
├─ Dockerfile
└─ pom.xml
```

## 环境要求

- JDK 17
- MySQL 8.x
- Redis 6.x 或更高版本
- RabbitMQ 3.x
- Maven 3.8+（项目已提供 Maven Wrapper）
- 可选：etcd 与 HotKey Worker（启用热点探测时使用）
- 可选：OpenAI 兼容的大模型 API（启用 AI 功能时使用）

## 本地启动

### 1. 克隆项目

```bash
git clone https://github.com/StayBun/offerpilot.git
cd offerpilot
```

### 2. 初始化数据库

按顺序执行：

```text
sql/create_table.sql
sql/init_data.sql
```

脚本会创建并使用 `offerpilot` 数据库。

### 3. 修改配置

编辑 `src/main/resources/application.yml`，至少确认以下配置与本机环境一致：

- `spring.datasource`：MySQL 地址、用户名和密码
- `spring.redis`：Redis 地址、端口和密码
- `spring.rabbitmq`：RabbitMQ 地址、账号和虚拟主机
- `hotkey.etcd-server`：HotKey 使用的 etcd 地址

如需启用 AI 功能，请为 LangChain4j 配置 OpenAI 兼容接口，例如：

```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${DASHSCOPE_API_KEY}
      model-name: qwen-plus
```

不要将真实密码或 API Key 提交到 Git 仓库，生产环境建议全部通过环境变量注入。

### 4. 启动依赖服务

启动 MySQL、Redis 和 RabbitMQ。若使用 HotKey，再启动 etcd 与 HotKey Worker。

### 5. 启动后端

Windows：

```powershell
mvn spring-boot:run
```

macOS / Linux：

```bash
mvn spring-boot:run
```

默认服务地址为 `http://localhost:8101/api`，Knife4j 文档地址为：

```text
http://localhost:8101/api/doc.html
```

## 构建与测试

```powershell
# 编译
mvn -DskipTests compile

# 运行测试（需先启动 MySQL、Redis 等测试依赖）
mvn test

# 打包
mvn clean package -DskipTests
```

## Docker 部署

```bash
docker build -t offerpilot .
docker run -d --name offerpilot -p 8101:8101 offerpilot
```

容器使用 `prod` 配置启动。部署前请修改 `application-prod.yml`，或在实际部署平台中通过环境变量覆盖数据库、Redis、RabbitMQ 与大模型配置。

## 定时任务

- 每天 02:00 重建帖子 Bloom Filter。
- 每天 03:00 以点赞、收藏关系表为准校准帖子统计字段。
