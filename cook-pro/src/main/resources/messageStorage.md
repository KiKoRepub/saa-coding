
我给你一套 **比较接近真实 AI 产品的数据库设计**（适合 Agent / Tool / Streaming / 人工审核）。
这种结构在很多 AI 系统里都类似，比如基于 LangChain 或 LlamaIndex 构建的应用。

我会用 **MySQL + MongoDB 混合架构** 来设计。

---

# 一、整体架构

```
MySQL（关系数据）
│
├─ user
├─ conversation
├─ tool
├─ tool_execution
├─ audit_task
└─ workflow

MongoDB（聊天数据）
│
├─ messages
├─ agent_events
└─ stream_chunks
```

原则：

* **关系数据 → MySQL**
* **聊天 JSON → MongoDB**

---

# 二、MySQL 表结构

## 1 用户表

```sql
user
```

| 字段          | 说明   |
| ----------- | ---- |
| id          | 用户ID |
| username    | 用户名  |
| email       | 邮箱   |
| create_time | 创建时间 |

---

## 2 会话表

```sql
conversation
```

| 字段          | 说明              |
| ----------- | --------------- |
| id          | 会话ID            |
| user_id     | 用户ID            |
| title       | 会话标题            |
| status      | active / closed |
| create_time | 创建时间            |
| update_time | 更新时间            |

说明：

* 一次聊天 = 一个 conversation

---

## 3 工具表

```sql
tool
```

| 字段          | 说明                         |
| ----------- | -------------------------- |
| id          | 工具ID                       |
| name        | 工具名称                       |
| description | 工具描述                       |
| tool_type   | http / function / workflow |
| config      | JSON配置                     |
| create_time | 创建时间                       |

---

## 4 工具执行表

```sql
tool_execution
```

| 字段              | 说明                       |
| --------------- | ------------------------ |
| id              | 执行ID                     |
| conversation_id | 会话                       |
| message_id      | 触发的消息                    |
| tool_id         | 调用的工具                    |
| status          | running / success / fail |
| input           | JSON                     |
| output          | JSON                     |
| create_time     | 创建时间                     |

用于记录 **Agent工具调用**。

---

## 5 审核任务表

```sql
audit_task
```

| 字段              | 说明                          |
| --------------- | --------------------------- |
| id              | 审核任务ID                      |
| conversation_id | 会话                          |
| message_id      | 消息                          |
| audit_type      | tool / content              |
| status          | pending / approved / reject |
| reviewer        | 审核人                         |
| create_time     | 创建时间                        |
| finish_time     | 完成时间                        |

用于：

* 人工审核
* HITL (human in the loop)

---

# 三、MongoDB 结构

## 1 messages 集合

核心聊天记录。

```json
{
  "_id": "msg_123",
  "conversation_id": "conv_001",
  "role": "assistant",
  "content": "可乐鸡翅的做法是...",
  "tokens": 120,
  "metadata": {
    "model": "gpt-4",
    "temperature": 0.7
  },
  "tool_calls": [
    {
      "tool_name": "recipe_search",
      "arguments": {
        "food": "可乐鸡翅"
      }
    }
  ],
  "create_time": "2026-03-10T12:00:00"
}
```

字段说明：

| 字段              | 说明                        |
| --------------- | ------------------------- |
| conversation_id | 会话                        |
| role            | user / assistant / system |
| content         | 内容                        |
| tool_calls      | 工具调用                      |
| metadata        | 模型信息                      |

---

## 2 agent_events 集合

Agent执行过程记录。

```json
{
  "_id": "event_1",
  "conversation_id": "conv_001",
  "type": "tool_call",
  "message_id": "msg_123",
  "data": {
    "tool": "recipe_search",
    "args": {
      "food": "可乐鸡翅"
    }
  },
  "create_time": "2026-03-10T12:00:01"
}
```

用于记录：

* reasoning
* tool call
* observation
* error

方便 **debug agent**。

---

## 3 stream_chunks 集合

用于 **流式输出**

```json
{
  "_id": "chunk_1",
  "message_id": "msg_123",
  "index": 1,
  "content": "可乐鸡翅",
  "create_time": "2026-03-10T12:00:02"
}
```

作用：

* 断线恢复
* streaming回放

---

# 四、核心查询流程

### 用户聊天

```
用户发送消息
      ↓
写入 Mongo message
      ↓
Agent运行
      ↓
调用工具（MySQL记录）
      ↓
输出message
      ↓
流式输出
```

---

### 查询历史记录

查询：

```
conversation_id
```

Mongo：

```
messages
```

排序：

```
create_time
```

---

# 五、MongoDB 索引（非常重要）

必须建索引：

```javascript
db.messages.createIndex({ conversation_id: 1, create_time: 1 })
```

否则聊天历史会变慢。

---

# 六、为什么不用 MySQL 存 message？

如果 message 在 MySQL：

问题：

```
tool_calls
metadata
reasoning
```

全是 JSON。

表会变成：

```sql
message
- id
- conversation_id
- role
- content
- metadata JSON
- tool_calls JSON
```

最后 **80% 是 JSON**。

那就不如 Mongo。

---

# 七、大厂常见结构

类似结构在很多 AI 产品里都存在：

| 系统          | 结构                   |
| ----------- | -------------------- |
| ChatGPT 类系统 | SQL + document store |
| Agent平台     | SQL + Mongo          |
| RAG系统       | SQL + VectorDB       |

向量数据库例如：

* Milvus
* Weaviate
* Pinecone

用于：

* embedding
* 语义搜索

---

# 八、AI聊天系统完整数据流

完整结构：

```
             用户
              │
              ▼
         API Server
              │
      ┌───────┴────────┐
      ▼                ▼
   MySQL            MongoDB
(user,conv)        (messages)
      │                │
      ▼                ▼
   Agent Engine ---- events
      │
      ▼
 Tool Execution
      │
      ▼
   Streaming
```

---

如果你愿意，我可以再给你一份 **AI聊天系统的“生产级表设计”**（很多 AI SaaS 都这样做），里面会包括：

* message版本控制
* token统计
* prompt记录
* 模型调用日志
* RAG检索记录

这一套会非常接近 **真正上线的 AI 产品架构**。
