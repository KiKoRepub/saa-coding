# saa-coding
Spring-ai-alibaba coding repository

# Ai Agent 综合烹饪辅助智能体 (cook-pro 模块)

## 项目概述
CookPro 是一个基于 Spring AI Alibaba 框架开发的综合烹饪辅助智能体应用。该系统集成了多种先进的人工智能技术，包括 Retrieval-Augmented Generation (RAG)、Human-In-The-Loop (HITL) 人工干预机制、流式响应输出等，为用户提供智能化的烹饪指导和食谱管理服务。

## 核心功能
- **智能烹饪对话**：基于 React Agent 的多轮对话系统，支持自然语言交互
- **工具调用集成**：动态加载和执行烹饪相关工具，如网络搜索、食谱查询等
- **RAG 增强检索**：基于向量数据库的食谱检索和知识增强
- **HITL 人工干预**：关键操作的人工审核机制，确保安全和准确性
- **流式响应**：实时流式输出，支持中断与恢复
- **SSE 实时通知**：服务器推送事件，支持实时状态更新

## 技术架构

### 后端技术栈
- **框架**：Spring Boot 3.x + Spring AI Alibaba
- **AI 模型**：通义千问 (DashScope) + Ollama 本地模型
- **向量数据库**：Milvus
- **关系数据库**：MySQL 8.x + MyBatis-Plus
- **缓存**：Redis
- **消息队列**：自定义内存队列
- **文件存储**：MinIO
- **响应式编程**：Project Reactor

### 系统架构
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Client    │◄──►│   REST API      │◄──►│   AI Agent       │
│   (SSE/HTTP)    │    │   (Controllers) │    │   (ReactAgent)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                             │                        │
                             ▼                        ▼
                      ┌─────────────────┐    ┌─────────────────┐
                      │   Services      │    │   Tools & Hooks  │
                      │   (Business     │    │   (HITL, RAG)    │
                      │    Logic)       │    │                 │
                      └─────────────────┘    └─────────────────┘
                             │                        │
                             ▼                        ▼
                      ┌─────────────────┐    ┌─────────────────┐
                      │   Data Access   │    │   External       │
                      │   (MyBatis-Plus)│    │   Services       │
                      │                 │    │   (DashScope,    │
                      └─────────────────┘    │    Milvus, etc.) │
                                             └─────────────────┘
```

## 项目结构（cook-pro 模块）

- **后端应用**
  - `src/main/java/org/cookpro`：核心代码，包含 `CookProApplication` 启动类、`controller`、`service`、`mapper`、`config`、`tools`、`hooks` 等。
  - `src/main/resources/application.yaml`：主配置文件，导入 `config/agent.yml`、`config/vector.yml`、`config/mysql.yml`、`config/minio.yml`、`config/redis.yml`、`config/tool-env.yml` 以及可选的 `.env[.properties]`。
  - `src/main/resources/config/*.yml`：向量数据库、MySQL、Redis、MinIO、工具环境等子配置。
- **前端/调试页面**
  - `web/stream-test.html`：流式输出 / HITL 测试页面。
  - `web/aa.js`：浏览器端流式读取 / SSE 解析逻辑。

## 主要组件

### Controllers
- **ChatController**：聊天接口，支持普通对话和增强功能对话
- **TestController**：测试接口，用于功能验证和调试

### Services
- **HITLService**：人工干预服务，管理审核流程和状态
- **RecipeService**：食谱基础 CRUD 服务
- **RecipeRAGService**：食谱向量化服务，支持向量存储和检索
- **ToolService**：工具管理服务，动态加载工具配置
- **SSEService**：服务器发送事件服务，支持实时推送
- **MemoryCacheService**：内存缓存服务，用于流式中断管理

### Entities
- **Recipe**：食谱实体，包含菜名、食材、步骤、图片等信息
- **User**：用户实体
- **ChatRecord**：聊天记录实体
- **HITLEntity**：人工干预记录实体
- **ToolEntity**：工具配置实体

### 配置管理
- **多环境配置**：通过 YAML 文件分离不同环境的配置
- **Agent 配置**：DashScope API 和 Ollama 模型配置
- **向量数据库**：Milvus 连接和嵌入模型配置
- **工具环境**：外部工具的 API 密钥和配置

## API 接口（部分）

### 聊天 & RAG 相关
```
POST /chat/chatMore            # 增强聊天（支持工具、RAG、HITL）
POST /chat/stream/chatMore     # 流式增强聊天
GET  /chat/chat                # 基础聊天
POST /rag/recipe/search        # 食谱 RAG 检索
POST /rag/recipe/rebuild       # 重建/刷新食谱向量索引
```

### HITL / 流式测试接口
```
GET  /test/humanInLoop         # HITL 测试
GET  /test/stream/humanInLoop  # 流式 HITL 测试（web/stream-test.html 默认使用）
POST /test/approve             # HITL 审核通过
```

### 其他常用接口（示例）
> 详细参数与完整接口请通过 Swagger/OpenAPI 文档查看：`http://localhost:13002/swagger-ui/index.html`

- `RecipeController`：食谱增删改查、列表分页等。
- `FileController`：基于 MinIO 的文件上传、下载与预览。
- `ToolController`：工具配置管理、工具触发测试等。
- `HITLController` / `SSEUserRecordController`：HITL 审核流程、SSE 推送记录。
- `UserController` / `UserPreferenceController`：用户与偏好设置管理。

## 数据模型

### 核心数据表
- **recipe**：食谱表，存储菜谱信息（JSON 格式存储步骤）
- **user**：用户表
- **chat_record**：聊天记录表
- **hitl_entity**：人工干预记录表
- **tool_entity**：工具配置表

## 部署与运行

### 环境要求
- Java 17+
- MySQL 8.x
- Redis 6.x
- Milvus 2.x
- MinIO
- Ollama（可选，用于本地嵌入）

### 配置步骤
1. 安装并启动依赖服务：**MySQL、Redis、Milvus、MinIO**。
2. 在 `src/main/resources/config/mysql.yml`、`redis.yml`、`minio.yml`、`vector.yml`、`tool-env.yml` 中配置数据库、缓存、对象存储、向量库和外部工具参数。
3. 如需隐藏敏感信息（API Key 等），可在工程根目录或类路径下配置 `.env` / `.env.properties`，并在其中维护密钥，`application.yaml` 会自动导入。
4. 如需使用本地嵌入模型，安装并启动 Ollama，并拉取所需模型：
   ```bash
   ollama pull embeddinggemma
   ```

### 启动方式
- **IDE 运行**：导入为 Gradle 项目，找到 `org.cookpro.CookProApplication`，直接运行 main 方法。
- **命令行运行（示例）**：
  ```bash
  # 在仓库根目录
  ./gradlew :cook-pro:build
  # 使用 IDE 或 java -jar 运行生成的可执行 Jar（如已配置 Spring Boot 打包）
  ```

### 端口配置
- 应用端口：13002（见 `application.yaml`）
- MySQL：3306
- Redis：6379
- Milvus：19530
- MinIO：9000

## 开发指南

### 构建
```bash
./gradlew build
```

### 测试
```bash
./gradlew test
```

### 本地流式 / HITL 测试
1. 确保后端已在本地 `13002` 端口启动。
2. 直接用浏览器打开 `cook-pro/web/stream-test.html`（本地文件即可，不必通过 Web 服务器托管）。
3. 点击“开始流式请求”按钮，页面会调用 `GET http://localhost:13002/test/stream/humanInLoop`，并在界面上实时展示流式 / SSE 返回内容。

### 添加新工具
1. 在 `ToolEntity` 表中添加工具配置
2. 实现对应的 `ToolCallback`
3. 在 `ToolFactory` 中注册工具

### HITL 流程
1. Agent 触发工具调用中断
2. 系统创建 HITL 记录并通知审核人
3. 审核人通过 SSE 接收通知并进行审核
4. 审核通过后恢复 Agent 执行
5. 结果通过流式响应返回给用户

## 特色功能详解

### RAG 增强检索
- 基于 Milvus 向量数据库存储食谱向量
- 支持语义相似度搜索
- 自动嵌入和索引管理

### HITL 人工干预
- 关键工具调用前的人工审核
- 异步审核流程，支持多级审核
- 实时状态推送和通知

### 流式响应
- Reactor 响应式流实现
- 中断缓存和恢复机制
- SSE 兼容的流式输出

## 扩展性
- **插件化工具系统**：易于添加新的烹饪工具
- **多模型支持**：支持多种 AI 模型切换
- **分布式部署**：支持水平扩展
- **多租户架构**：支持多用户隔离

## 许可证

当前尚未明确指定开源许可证，默认保留所有权利。如需在生产环境或商业场景中使用，请先与作者沟通确认。

## 贡献
欢迎提交 Issue 和 Pull Request 来改进项目。

## 联系方式

- Telegram：待补充
