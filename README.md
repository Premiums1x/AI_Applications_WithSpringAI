# ChatRobot - AI 智能助手平台

一个基于 **Spring Boot + Spring AI + Vue 3** 的全栈 AI 智能助手平台，支持流式输出、多会话管理、暗色主题等功能。项目使用 SiliconFlow 云 API（OpenAI 兼容）作为主要模型，同时支持 Ollama 本地模型部署，涵盖对话机器人、哄哄模拟器、智能客服、ChatPDF 四大模块。

## 项目预览

首页提供四个功能模块入口：
- **对话机器人** — 通用 AI 聊天，支持多模态（文本 + 文件附件），流式打字机效果，思考过程展示，多会话管理
- **哄哄模拟器** — AI 角色扮演游戏（哄女友），带原谅值打分系统，游戏化 UI
- **智能客服** — 黑马程序员课程咨询助手，对接数据库查询课程、校区、预约信息
- **ChatPDF** — PDF 文档对话，基于 RAG（向量检索增强生成）实现语义搜索问答

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.6 | 应用框架 |
| Spring AI | 2.0.0-M6 | AI 模型集成（ChatClient / ChatMemory / Advisor / VectorStore） |
| SiliconFlow（OpenAI 兼容） | Qwen3.6-35B-A3B | 主要大语言模型（云 API） |
| SiliconFlow Embedding | Qwen3-Embedding-0.6B | 文本嵌入模型（1024 维，用于向量检索） |
| Ollama | deepseek-r1:1.5b | 本地大语言模型（可选） |
| MySQL | 8.x | 关系型数据库 |
| MyBatis-Plus | 3.5.10.1 | ORM 框架 |
| Redis | 7.x | 缓存 / 向量存储（已配置，当前使用内存向量库） |
| Spring AI PDF Reader | - | PDF 文档解析 |
| Lombok | - | 简化 Java 代码 |
| Project Reactor | (随 Spring AI) | 响应式流式输出（Flux） |
| Maven | 3.9.15 | 构建工具 |
| Java | 17 | 开发语言 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5.x | 前端框架 |
| Vue Router | 5.x | 路由管理 |
| Vite | 8.x | 构建工具 / 开发服务器 |
| Axios | 1.16.x | HTTP 客户端 |

## 项目结构

```
ChatRobot/
├── pom.xml                                    # Maven 依赖管理
├── README.md                                  # 项目文档
├── .gitignore
├── .gitattributes
├── mvnw / mvnw.cmd                            # Maven Wrapper
├── chat-pdf.properties                        # ChatPDF session → 文件名映射（运行时生成）
├── chat-pdf.json                              # 向量库持久化文件（运行时生成）
├── 国家教育.pdf                                # ChatPDF 测试用 PDF 文件
│
├── .mvn/wrapper/
│   └── maven-wrapper.properties               # Maven Wrapper 版本配置
│
├── src/main/java/
│   ├── ModulesEnum/
│   │   └── ServiceTypes.java                  # 业务类型枚举（chat / honghong / service / chatPDF）
│   │
│   └── com/lancer/ai/
│       ├── ChatRobotApplication.java          # Spring Boot 启动类
│       │
│       ├── config/
│       │   ├── CommonConfiguration.java       # ChatClient（4 种角色）、ChatMemory、VectorStore、EmbeddingModel 配置
│       │   ├── MvcConfiguration.java          # CORS 跨域配置
│       │   └── MyBatisPlusConfig.java         # MyBatis-Plus SqlSessionFactory 配置
│       │
│       ├── constants/
│       │   └── SystemConstants.java           # 哄哄模拟器 & 智能客服系统提示词
│       │
│       ├── controller/
│       │   ├── ChatController.java            # 对话机器人接口（/ai/chat、/ai/chat/stream）
│       │   ├── GameController.java            # 哄哄模拟器接口（/ai/game）
│       │   ├── CustomerController.java        # 智能客服接口（/ai/service）
│       │   ├── PdfController.java             # ChatPDF 接口（/ai/pdf/upload、chat、preview、download）
│       │   └── ChatHistoryController.java     # 会话历史查询接口（/ai/history/{type}）
│       │
│       ├── entity/
│       │   ├── po/                            # 数据库实体
│       │   │   ├── Course.java                # 课程表
│       │   │   ├── School.java                # 校区表
│       │   │   └── CourseReservation.java     # 课程预约表
│       │   ├── query/
│       │   │   └── CourseQuery.java           # 课程查询 DTO（含排序）
│       │   └── vo/
│       │       ├── MessageVO.java             # 前端消息视图对象
│       │       └── Result.java                # 通用 API 响应包装
│       │
│       ├── service/                           # MyBatis-Plus 服务层
│       │   ├── ICourseService.java
│       │   ├── ISchoolService.java
│       │   ├── ICourseReservationService.java
│       │   └── impl/
│       │       ├── CourseServiceImpl.java
│       │       ├── SchoolServiceImpl.java
│       │       └── CourseReservationServiceImpl.java
│       │
│       ├── mapper/                            # MyBatis-Plus Mapper 层
│       │   ├── CourseMapper.java
│       │   ├── SchoolMapper.java
│       │   └── CourseReservationMapper.java
│       │
│       ├── tools/
│       │   └── CourseTools.java              # Spring AI @Tool 注解函数（供智能客服调用）
│       │
│       ├── repository/                        # 仓储层
│       │   ├── ChatHistoryRepository.java     # 会话历史仓储接口
│       │   ├── InMemoryChatHistoryRepository.java  # 内存实现
│       │   ├── FileRepository.java            # PDF 文件仓储接口
│       │   └── LocalPdfFileRepository.java    # 本地文件系统 + Properties 实现
│       │
│       └── util/
│           └── VectorDistanceUtils.java       # 欧氏距离 & 余弦距离计算工具
│
├── src/main/resources/
│   ├── application.yaml                       # Spring Boot 配置（MySQL、Redis、Ollama、OpenAI/SiliconFlow）
│   ├── mapper/                                # MyBatis XML Mapper（预留，实际使用注解）
│   │   ├── CourseMapper.xml
│   │   ├── SchoolMapper.xml
│   │   └── CourseReservationMapper.xml
│   └── static/
│       ├── Frontend.md                        # 前端开发需求文档
│       └── chat-robot-frontend/               # Vue 3 + Vite 前端项目
│           ├── package.json
│           ├── vite.config.js                 # Vite 配置（/api 代理 → localhost:8080）
│           ├── index.html
│           └── src/
│               ├── main.js                    # 入口 + 全局主题管理
│               ├── App.vue                    # 根组件
│               ├── style.css                  # 全局样式 + CSS 变量（亮/暗主题）
│               ├── router/index.js            # 路由定义
│               └── views/
│                   ├── HomeView.vue           # 首页（4 个模块卡片入口）
│                   ├── ChatBotView.vue        # 对话机器人（多会话管理）
│                   ├── HongHongSimulatorView.vue  # 哄哄模拟器（单局游戏）
│                   ├── CustomerServiceView.vue    # 智能客服
│                   └── ChatPdfView.vue        # ChatPDF（PDF 预览 + 对话面板）
│
└── src/test/java/
    └── com/lancer/ai/
        └── ChatRobotApplicationTests.java     # 测试类（嵌入向量、向量距离、PDF 向量库）
```

## 四个功能模块详解

### 1. 对话机器人（ChatBot）

- **AI 角色**：智能编码助手 Lcode
- **后端**：`ChatController` — `/ai/chat`（阻塞式）、`/ai/chat/stream`（流式，含 multipart 多模态）
- **能力**：文本对话 + 文件附件上传（多模态），流式打字机效果，`<thinking>` 标签解析展示思考过程
- **前端**：ChatGPT 风格布局（左侧会话历史 + 右侧消息区），多会话管理，每 15ms 打字机字符渲染

### 2. 哄哄模拟器（HongHong Simulator）

- **AI 角色**：生气的女朋友
- **后端**：`GameController` — `/ai/game`（流式），初始原谅值 20/100，根据回复 ±10 分
- **AI 回复格式**：`（心情）{speech}\n得分：{score}\n原值：{value}/100`
- **前端**：原谅值渐变进度条（红→黄→绿），心情/得分标签展示，通关/失败判定，正则解析结构化输出

### 3. 智能客服（Smart Customer Service）

- **AI 角色**：小黑（黑马程序员课程顾问）
- **后端**：`CustomerController` — `/ai/service`，通过 `CourseTools` 中的 `@Tool` 注解函数调用 MyBatis-Plus 查询数据库
- **支持的工具调用**：
  - `queryCourse()` — 按课程类型、学历筛选课程
  - `querySchool()` — 查询所有校区
  - `createCourseReservation()` — 创建课程预约订单
- **前端**：类似 ChatBot 布局，支持 Markdown 表格渲染课程信息

### 4. ChatPDF — 基于 RAG 的 PDF 对话

- **后端**：`PdfController` — PDF 上传解析 → 向量嵌入 → 向量库存储 → RAG 问答
- **RAG 管线**：PDF → `PagePdfDocumentReader`（逐页拆分） → `VectorStore.add()`（嵌入存储）→ `QuestionAnswerAdvisor`（检索增强生成）
- **向量库**：`SimpleVectorStore`（内存，持久化到 `chat-pdf.json`），按 `file_name` 过滤，相似度阈值 0.6
- **端点**：
  - `POST /ai/pdf/upload/{chatId}` — 上传 PDF
  - `GET /ai/pdf/chat` — 流式 RAG 问答
  - `GET /ai/pdf/preview/{chatId}` — PDF 内嵌预览
  - `GET /ai/pdf/file/{chatId}` — PDF 文件下载
- **前端**：左右分栏布局（左侧 PDF iframe 预览 + 右侧聊天），拖拽上传，面板可折叠

## 架构与数据流

### 整体架构

```
┌──────────────────────────────────────────────────────────────────┐
│                        浏览器 (Vue 3)                             │
│  ┌──────────────┐    ┌───────────────────────────────────────┐   │
│  │  HomeView    │    │  ChatBotView / HongHongSimulatorView  │   │
│  │  4 个模块卡片 │───▶│  CustomerServiceView / ChatPdfView   │   │
│  └──────────────┘    │  (流式读取 + 打字机效果)               │   │
│                      └──────────┬────────────────────────────┘   │
└─────────────────────────────────┼────────────────────────────────┘
                                  │ HTTP (GET/POST /api/ai/*)
                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                Vite Dev Server (代理)                             │
│           /api/*  →  http://localhost:8080/*                      │
└─────────────────────────────────┼────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│                    Spring Boot 后端                               │
│                                                                  │
│  ChatController   GameController   CustomerController   PdfController    ChatHistoryController │
│  /ai/chat/stream  /ai/game         /ai/service          /ai/pdf/*         /ai/history/*       │
│        │              │               │                   │                    │              │
│        ▼              ▼               ▼                   ▼                    ▼              │
│  ChatClient    gameChatClient   serviceChatClient   pdfChatClient         ChatMemory + Repo  │
│  (编码助手)     (女友角色扮演)    (课程顾问 + Tool)    (RAG 问答)           (会话记忆 + ID管理)  │
│        │              │               │                   │                                   │
│        └──────────────┴───────────────┴───────────────────┘                                   │
│                              │                                                                │
│                    ┌─────────┴─────────┐                                                      │
│                    ▼                   ▼                                                      │
│            OpenAiChatModel      OllamaChatModel                                               │
│        (SiliconFlow 云 API)   (本地 deepseek-r1)                                              │
│                    │                   │                                                      │
│                    ▼                   ▼                                                      │
│          SiliconFlow 平台          Ollama 本地服务                                              │
│         (Qwen3.6-35B-A3B)     (http://localhost:11434)                                        │
└──────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌──────────────────────────────────────────────────────────────────┐
│              数据库 / 向量库 / 文件系统                            │
│  MySQL (课程/校区/预约)   Redis (缓存)   向量库 (SimpleVectorStore) │
│  chat-pdf.json (向量持久化)   chat-pdf.properties (文件映射)       │
└──────────────────────────────────────────────────────────────────┘
```

### 核心数据流（一次聊天请求的完整链路）

1. **用户输入** → 前端 Vue 组件中用户输入问题，点击发送
2. **前端发起请求** → 通过 `fetch` 调用 `/api/ai/chat/stream?prompt=xxx&chatID=xxx`
3. **Vite 代理转发** → 开发环境下 `/api` 前缀被去掉，转发到 `localhost:8080/ai/chat/stream`
4. **Controller 接收** → 对应 Controller 方法处理请求
5. **保存会话ID** → 调用 `ChatHistoryRepository.save()` 记录当前会话
6. **构建 AI 请求** → 通过对应角色的 `ChatClient` 设置用户输入，绑定 `chatID` 到 `ChatMemory`
7. **Advisor 链处理** → `SimpleLoggerAdvisor` 记录日志，`MessageChatMemoryAdvisor` 管理上下文记忆
8. **流式调用模型** → `.stream().content()` 返回 `Flux<String>`，逐 token 推送
9. **响应流式返回** → `text/html;charset=UTF-8` 编码，数据逐 chunk 发送到前端
10. **前端解析流** → `ReadableStream` 逐块读取，解析 `<thinking>` 标签，推入打字机队列
11. **打字机效果** → `setInterval` 每 15ms 从队列取字符渲染，实现逐字显示

### 关键设计点

#### 多角色 ChatClient 配置

`CommonConfiguration.java` 中配置了 4 个独立的 `ChatClient` Bean，通过不同的 `defaultSystem` 提示词实现角色切换：

| Bean | 角色 | 模型 | 附加能力 |
|------|------|------|----------|
| `chatClient` | 智能编码助手 Lcode | OpenAiChatModel | 多模态文件附件 |
| `gameChatClient` | 生气的女朋友 | OpenAiChatModel | 游戏规则解析 |
| `serviceChatClient` | 课程顾问小黑 | OpenAiChatModel | @Tool 数据库查询 |
| `pdfChatClient` | PDF 文档助手 | OpenAiChatModel | QuestionAnswerAdvisor（RAG） |

#### Spring AI ChatClient 的使用

```java
// CommonConfiguration.java - 配置 ChatClient Bean
ChatClient.builder(model)
    .defaultSystem("你是一个智能编码助手，名称叫Lcode...")
    .defaultAdvisors(
        new SimpleLoggerAdvisor(),                           // 日志记录
        MessageChatMemoryAdvisor.builder(chatMemory).build() // 对话记忆管理
    )
    .build();
```

- `ChatClient` 是 Spring AI 提供的高层抽象，屏蔽了直接调用模型的细节
- `defaultSystem` 设置系统提示词，定义 AI 的角色和行为
- `Advisor` 机制类似 Spring 的拦截器，可以在请求前后做额外处理

#### ChatMemory 对话记忆

```java
// CommonConfiguration.java
@Bean
public ChatMemory chatMemory() {
    return MessageWindowChatMemory.builder().build();
}
```

- `MessageWindowChatMemory` 维护一个滑动窗口，保存每个会话的历史消息
- 通过 `chatID` 区分不同会话，每个会话有独立的消息历史
- `MessageChatMemoryAdvisor` 自动将历史消息注入请求上下文

#### 流式输出与前端打字机效果

后端使用 `Flux<String>` 返回响应式流，前端通过 `ReadableStream` API 逐 chunk 读取：

```javascript
// 流式读取
const reader = response.body.getReader()
const decoder = new TextDecoder()
while (true) {
    const { done, value } = await reader.read()
    if (value) {
        rawBuffer += decoder.decode(value, { stream: true })
        // 解析 <thinking> 标签，推入队列
    }
    if (done) break
}
```

打字机效果通过 `setInterval(processTypewriter, 15)` 实现，每 15ms 从队列取字符渲染。队列积压较多时自动加速（批量取 5 个字符），保证流畅性。

#### RAG — 检索增强生成（ChatPDF 模块）

```
┌─────────────────────────────────────────────────────────┐
│  上传阶段                                                │
│  PDF 文件 → PagePdfDocumentReader（逐页拆分）            │
│           → EmbeddingModel（文本嵌入，1024 维向量）      │
│           → SimpleVectorStore（向量库存储 + JSON 持久化） │
├─────────────────────────────────────────────────────────┤
│  问答阶段                                                │
│  用户问题 → EmbeddingModel（问题向量化）                  │
│          → VectorStore.similaritySearch()（语义检索）    │
│          → QuestionAnswerAdvisor（检索结果注入上下文）    │
│          → OpenAiChatModel（生成回答）                   │
└─────────────────────────────────────────────────────────┘
```

关键技术点：
- PDF 按页拆分，每页生成独立 Document
- 元数据 `file_name` 用于过滤，确保只在指定 PDF 内检索
- 相似度阈值 0.6，过滤不相关结果
- 向量库运行时在内存中，关闭时持久化到 `chat-pdf.json`

#### Spring AI Tool Calling — Function Calling（智能客服模块）

`CourseTools.java` 使用 Spring AI 的 `@Tool` 注解将 Java 方法暴露为 AI 可调用的工具函数：

```java
@Tool(description = "查询课程信息，支持按课程类型、学历筛选")
public List<Course> queryCourse(String type, String edu, CourseQuery query) { ... }

@Tool(description = "查询所有校区信息")
public List<School> querySchool(SchoolQuery query) { ... }

@Tool(description = "创建课程预约")
public CourseReservation createCourseReservation(CourseReservation reservation) { ... }
```

当用户询问课程相关信息时，AI 会自动决定调用哪个工具，Spring AI 框架会将工具返回的数据库结果注入对话上下文。

#### 哄哄模拟器游戏机制

**后端规则**（`SystemConstants.GAME_PROMPT`）：
- 用户扮演男友，AI 扮演生气的女朋友
- 初始原谅值 20/100，每次交互根据回复内容增减
- 得分等级：-10（非常生气）→ -5（生气）→ 0（正常）→ +5（开心）→ +10（非常开心）
- 原谅值 ≥ 100 通关，≤ 0 失败

**AI 回复格式**：
```
（心情）{女友说的话}
得分：{+-原值增减}
原值：{当前原值}/100
```

**前端解析**：
- 提取 `（心情）` → 显示心情标签
- 提取 `得分：+-x` → 显示得分标签
- 提取 `原值：x/100` → 更新原谅值进度条
- 去除元信息 → 只保留女友说的话显示在聊天区

#### Thinking 过程解析

对话机器人中，模型会在回答中插入 `<thinking>` 标签包裹思考过程。前端通过 `parseBuffer()` 函数实时解析流数据，将思考内容和正式回答分开显示：
- 思考过程：橙黄色背景，可折叠
- 正式回答：青绿色主题色高亮

## API 接口一览

| 方法 | 接口 | 模块 | 说明 |
|------|------|------|------|
| `GET` | `/ai/chat?prompt=xxx` | ChatBot | 阻塞式聊天 |
| `GET` | `/ai/chat/stream?prompt=xxx&chatID=xxx` | ChatBot | 流式聊天（含会话记忆） |
| `POST` | `/ai/chat/stream`（multipart） | ChatBot | 流式多模态聊天（含文件附件） |
| `GET` | `/ai/game?prompt=xxx&chatID=xxx` | 哄哄模拟器 | 流式游戏交互 |
| `GET` | `/ai/service?prompt=xxx&chatID=xxx` | 智能客服 | 客服聊天（含 Tool Calling） |
| `POST` | `/ai/pdf/upload/{chatId}` | ChatPDF | 上传 PDF（解析 + 向量化） |
| `GET` | `/ai/pdf/chat?prompt=xxx&chatID=xxx` | ChatPDF | 流式 RAG 问答 |
| `GET` | `/ai/pdf/preview/{chatId}` | ChatPDF | PDF 内嵌预览 |
| `GET` | `/ai/pdf/file/{chatId}` | ChatPDF | PDF 文件下载 |
| `GET` | `/ai/history/{type}` | 共享 | 获取某业务类型下所有会话 ID |
| `GET` | `/ai/history/{type}/{chatID}` | 共享 | 获取某会话的详细消息历史 |

**业务类型（type）**：`chat` / `honghong` / `service` / `chatPDF`

## 前端亮点

### 暗色/亮色主题

通过 CSS 变量实现全局主题切换，`main.js` 中管理主题状态：

```javascript
// 检测系统主题偏好 + 本地存储记忆
const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
const savedTheme = localStorage.getItem('theme')
const isDark = ref(savedTheme ? savedTheme === 'dark' : systemDark)
```

CSS 中定义了 `:root` 和 `[data-theme="dark"]` 两套变量，切换时修改 `data-theme` 属性即可。

### 响应式布局

- 侧边栏支持折叠
- 移动端侧边栏变为 fixed 定位的抽屉
- 消息区域自适应宽度，最大 900px 居中
- ChatPDF 左右分栏支持面板折叠

### 多视图模式

| 视图 | 布局 | 特点 |
|------|------|------|
| `ChatBotView` | 左侧会话列表 + 右侧聊天 | 多会话管理，通用视图 |
| `HongHongSimulatorView` | 单局游戏模式 | 进度条、心情/得分标签、通关判定 |
| `CustomerServiceView` | 左侧会话列表 + 右侧聊天 | Markdown 表格渲染，欢迎页 |
| `ChatPdfView` | 左侧 PDF 预览 + 右侧聊天 | 拖拽上传，iframe 预览，面板折叠 |

## 后端亮点

### 仓储模式（Repository Pattern）

```java
// 接口定义
public interface ChatHistoryRepository {
    void save(String type, String chatID);
    List<String> getChatIDs(String type);
}

// 内存实现（当前使用）
@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository {
    private final Map<String, List<String>> chatHistory = new HashMap<>();
}

// PDF 文件仓储同理
public interface FileRepository { ... }
@Component public class LocalPdfFileRepository implements FileRepository { ... }
```

定义接口 + 多种实现的模式，方便后续切换为数据库 / 云存储。

### VO 视图对象

`MessageVO` 将 Spring AI 内部的 `Message` 对象转换为前端需要的格式：

```java
public record MessageVO(String role, String context) {}
```

`Result` 通用响应包装：

```java
public record Result(boolean ok, String msg) {}
```

### MyBatis-Plus 数据库集成

智能客服模块使用 MyBatis-Plus 管理课程、校区、预约三张表，采用经典的三层架构：

```
Controller → Service (IService) → Mapper (BaseMapper) → MySQL
                     ↑
              CourseTools (@Tool 注解)
```

## 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.x（数据库 `itheima_ai`，用户 `root`，密码 `123456`）
- Redis 7.x（默认 `localhost:6379`）
- （可选）Ollama — 本地模型

### 1. 启动基础设施

确保 MySQL 和 Redis 服务已启动，并创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS itheima_ai;
```

### 2. 配置环境变量

```bash
# 设置 SiliconFlow API Key
set OPEN_AI_KEY=your-siliconflow-api-key
```

### 3. 启动后端

```bash
cd ChatRobot
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 4. 启动前端（开发模式）

```bash
cd ChatRobot/src/main/resources/static/chat-robot-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，通过 Vite 代理将 `/api` 请求转发到后端。

### 5. 访问

浏览器打开 `http://localhost:5173`，即可使用全部四个模块。

## 学习价值

这个项目覆盖了一个全栈 AI 应用的核心知识点：

**后端方面：**
- Spring Boot 应用搭建与配置
- Spring AI 框架集成大模型（OpenAI 兼容 + 本地 Ollama）
- ChatClient / ChatMemory / Advisor 等 AI 抽象的使用
- 多角色 ChatClient 配置（不同系统提示词实现角色切换）
- 流式响应（Flux / Server-Sent Events 思路）
- RESTful API 设计与接口版本管理
- 接口 + 实现的仓储模式（为后续持久化做准备）
- **RAG（检索增强生成）** — PDF 解析 → 向量嵌入 → 向量库存储 → 语义检索 → 增强生成
- **Tool Calling（Function Calling）** — AI 自动调用 Java 方法查询数据库
- **向量嵌入与相似度检索** — EmbeddingModel + SimpleVectorStore
- **MyBatis-Plus ORM** — 三层架构（Controller → Service → Mapper → DB）
- 多模态输入（文本 + 文件附件）

**前端方面：**
- Vue 3 Composition API（`<script setup>`、`ref`、`computed`、`inject`）
- Vue Router 路由配置与动态参数
- 多视图模式：通用多会话视图 / 单次游戏视图 / 左右分栏视图
- ReadableStream 流式数据读取与解析
- 打字机效果的实现（队列 + 定时器）
- 正则解析结构化文本（游戏元信息提取）
- CSS 变量实现暗色/亮色主题
- 响应式布局与移动端适配
- 拖拽上传（ChatPDF）
- iframe 内嵌 PDF 预览

**全栈协作方面：**
- 前后端分离架构与代理配置
- 数据格式约定（MessageVO / Result）
- 会话管理（chatID 生成与传递）
- 跨域问题解决（CORS 配置）
- AI 模型接口抽象（本地 Ollama ↔ 云 SiliconFlow 灵活切换）
