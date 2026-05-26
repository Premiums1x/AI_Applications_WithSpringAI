# ChatRobot - AI 智能助手平台

一个基于 **Spring Boot + Spring AI + Vue 3** 的全栈 AI 聊天应用，支持流式输出、多会话管理、暗色主题等功能。项目使用本地部署的 Ollama 大模型（deepseek-r1:1.5b），不依赖云 API，适合学习全栈开发和 AI 集成。

## 项目预览

首页提供四个功能模块入口（对话机器人、哄哄模拟器、智能客服、ChatPDF），点击后进入对应的聊天界面。聊天界面左侧是会话历史列表，右侧是消息区域，支持流式打字机效果和思考过程展示。

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.6 | 应用框架 |
| Spring AI | 2.0.0-M6 | AI 模型集成 |
| Ollama | deepseek-r1:1.5b | 本地大语言模型 |
| MySQL Connector | - | 数据库驱动（已引入，暂未使用） |
| Lombok | - | 简化 Java 代码 |
| Project Reactor | (随 Spring AI 引入) | 响应式流式输出 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5.x | 前端框架 |
| Vue Router | 5.x | 路由管理 |
| Vite | 8.x | 构建工具 / 开发服务器 |
| Axios | 1.16.x | HTTP 客户端（已引入） |

## 项目结构

```
ChatRobot/
├── pom.xml                              # Maven 依赖管理
├── src/main/java/
│   ├── ModulesEnum/
│   │   └── ServiceTypes.java            # 业务类型枚举(chat/honghong/service/chatPDF)
│   └── com/lancer/ai/
│       ├── ChatRobotApplication.java    # Spring Boot 启动类
│       ├── config/
│       │   ├── CommonConfiguration.java # ChatClient + ChatMemory 配置
│       │   └── MvcConfiguration.java    # CORS 跨域配置
│       ├── constants/
│       │   └── SystemConstants.java     # 哄哄模拟器系统提示词
│       ├── controller/
│       │   ├── ChatController.java      # 聊天接口（普通 + 流式）
│       │   ├── GameController.java      # 哄哄模拟器游戏接口
│       │   └── ChatHistoryController.java # 历史记录查询接口
│       ├── entity/vo/
│       │   └── MessageVO.java           # 返回给前端的消息视图对象
│       └── repository/
│           ├── ChatHistoryRepository.java       # 会话历史仓储接口
│           └── InMemoryChatHistoryRepository.java # 内存实现
└── src/main/resources/
    ├── application.yaml                 # 应用配置
    └── static/chat-robot-frontend/      # Vue 前端项目
        ├── package.json
        ├── vite.config.js               # Vite 配置（含代理）
        └── src/
            ├── main.js                  # 入口 + 全局主题管理
            ├── App.vue                  # 根组件
            ├── style.css                # 全局样式 + CSS 变量（亮/暗主题）
            ├── router/index.js          # 路由定义
            └── views/
                ├── HomeView.vue         # 首页（模块卡片）
                ├── ChatBotView.vue      # 对话机器人（多会话管理）
                └── HongHongSimulatorView.vue # 哄哄模拟器（单次游戏）
```

## 架构与数据流

### 整体架构

```
┌─────────────────────────────────────────────────────────┐
│                    浏览器 (Vue 3)                        │
│  ┌──────────────┐    ┌──────────────────────────────┐   │
│  │  HomeView    │    │  ChatBotView /               │   │
│  │  模块卡片    │───▶│  HongHongSimulatorView      │   │
│  └──────────────┘    │  (流式读取 + 打字机效果)      │   │
│                      └──────────┬───────────────────┘   │
└─────────────────────────────────┼───────────────────────┘
                                  │ HTTP (GET /api/ai/chat/stream)
                                  │        (GET /api/ai/game)
                                  ▼
┌─────────────────────────────────────────────────────────┐
│              Vite Dev Server (代理)                      │
│         /api/*  →  http://localhost:8080/*               │
└─────────────────────────────────┼───────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Boot 后端                            │
│                                                         │
│  ChatController   GameController   ChatHistoryController │
│  /ai/chat/stream  /ai/game         /ai/history/...      │
│        │              │                  │               │
│        ▼              ▼                  ▼               │
│  ChatClient      gameChatClient   ChatMemory + Repo     │
│  (编码助手)       (女友角色扮演)   (会话记忆 + ID管理)    │
│        │              │                                 │
│        └──────────────┘                                 │
│               │                                         │
│               ▼                                         │
│        OllamaChatModel                                   │
│        http://localhost:11434                            │
│               │                                         │
│               ▼                                         │
│        Ollama 本地服务 (deepseek-r1:1.5b)                │
└─────────────────────────────────────────────────────────┘
```

### 核心数据流（一次聊天请求的完整链路）

1. **用户输入** → 前端 `ChatBotView.vue` 中用户输入问题，点击发送
2. **前端发起请求** → 通过 `fetch` 调用 `/api/ai/chat/stream?prompt=xxx&chatID=xxx`
3. **Vite 代理转发** → 开发环境下 `/api` 前缀被去掉，转发到 `localhost:8080/ai/chat/stream`
4. **Controller 接收** → `ChatController.streamChat()` 方法处理请求
5. **保存会话ID** → 调用 `ChatHistoryRepository.save()` 记录当前会话
6. **构建 AI 请求** → 通过 `ChatClient` 设置用户输入，绑定 `chatID` 到 `ChatMemory`
7. **流式调用模型** → `.stream().content()` 返回 `Flux<String>`，逐 token 推送
8. **顾问链处理** → `SimpleLoggerAdvisor` 记录日志，`MessageChatMemoryAdvisor` 管理上下文记忆
9. **响应流式返回** → `text/html;charset=UTF-8` 编码，数据逐 chunk 发送到前端
10. **前端解析流** → `ReadableStream` 逐块读取，解析 thinking 标签，推入打字机队列
11. **打字机效果** → `setInterval` 每 15ms 从队列取字符渲染，实现逐字显示

### 关键设计点

#### Spring AI ChatClient 的使用

```java
// CommonConfiguration.java - 配置 ChatClient Bean
ChatClient.builder(model)
    .defaultSystem("你是一个智能编码助手，名称叫Lcode...")
    .defaultAdvisors(
        new SimpleLoggerAdvisor(),                          // 日志
        MessageChatMemoryAdvisor.builder(chatMemory).build() // 对话记忆
    )
    .build();
```

- `ChatClient` 是 Spring AI 提供的高层抽象，屏蔽了直接调用模型的细节
- `defaultSystem` 设置系统提示词，定义 AI 的角色
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
- 顾问链中的 `MessageChatMemoryAdvisor` 自动将历史消息注入请求上下文

#### 流式输出与前端打字机效果

后端使用 `Flux<String>` 返回响应式流，前端通过 `ReadableStream` API 逐 chunk 读取：

```javascript
// ChatBotView.vue - 流式读取
const reader = response.body.getReader()
const decoder = new TextDecoder()
while (true) {
    const { done, value } = await reader.read()
    if (value) {
        rawBuffer += decoder.decode(value, { stream: true })
        // 解析 thinking 标签，推入队列
    }
    if (done) break
}
```

打字机效果通过 `setInterval(processTypewriter, 15)` 实现，每 15ms 从队列取字符渲染。队列积压较多时自动加速（批量取 5 个字符），保证流畅性。

#### 多角色 ChatClient 配置

```java
// CommonConfiguration.java
@Bean
public ChatClient chatClient(OllamaChatModel model, ChatMemory chatMemory) {
    return ChatClient.builder(model)
        .defaultSystem("你是一个智能编码助手，名称叫Lcode...")
        .defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
}

@Bean
public ChatClient gameChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
    return ChatClient.builder(model)
        .defaultSystem(SystemConstants.GAME_PROMPT)  // 女友角色扮演提示词
        .defaultAdvisors(new SimpleLoggerAdvisor(), MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();
}
```

不同模块使用独立的 `ChatClient` Bean，通过不同的 `defaultSystem` 提示词实现角色切换：
- `chatClient`：智能编码助手 Lcode
- `gameChatClient`：哄哄模拟器女友角色

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

**前端解析**（`parseGameResult`）：
- 提取 `（心情）` → 显示心情标签
- 提取 `得分：+-x` → 显示得分标签
- 提取 `原值：x/100` → 更新原谅值进度条
- 去除元信息 → 只保留女友说的话显示在聊天区

#### Thinking 过程解析

deepseek-r1 模型会在回答中插入 `<thinking>` 标签包裹思考过程。前端通过 `parseBuffer()` 函数实时解析流数据，将思考内容和正式回答分开显示：

- 思考过程：橙黄色背景，可折叠
- 正式回答：青绿色主题色高亮

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

### 双视图模式

**ChatBotView（对话机器人）**：
- 多会话管理：侧边栏显示历史会话列表，支持切换
- 持久化：通过 `chatID` 关联后端 `ChatMemory`，可恢复历史消息
- 通用视图：服务于 `chatbot` / `customerservice` / `chatpdf` 三个模块

**HongHongSimulatorView（哄哄模拟器）**：
- 单次游戏模式：无多会话管理，点进来即玩一局
- 游戏化 UI：原谅值进度条、心情标签、得分标签、通关/失败提示
- 弹窗交互：开始新游戏时弹出

## 后端亮点

### RESTful 接口设计

| 接口 | 方法 | 说明 |
|------|------|------|
| `/ai/chat?prompt=xxx` | GET | 普通阻塞式聊天（对话机器人） |
| `/ai/chat/stream?prompt=xxx&chatID=xxx` | GET | 流式聊天（对话机器人） |
| `/ai/game?prompt=xxx&chatID=xxx` | GET | 哄哄模拟器游戏流式接口 |
| `/ai/history/{type}` | GET | 获取某业务类型下的所有会话ID |
| `/ai/history/{type}/{chatID}` | GET | 获取某会话的详细消息历史 |

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
    // ...
}
```

定义接口 + 内存实现的模式，方便后续切换为数据库存储（MySQL 已在依赖中引入）。

### VO 视图对象

`MessageVO` 将 Spring AI 内部的 `Message` 对象转换为前端需要的格式：

```java
public class ServiceTypes { chat, honghong, service, chatPDF }
```

支持四种业务类型，通过 `ServiceTypes` 枚举管理，前端路由参数映射到对应的业务类型。

## 快速启动

### 环境要求

- JDK 17+
- Node.js 18+
- Ollama（已安装并拉取 deepseek-r1:1.5b 模型）

### 1. 启动 Ollama

```bash
ollama serve
ollama pull deepseek-r1:1.5b
```

### 2. 启动后端

```bash
cd ChatRobot
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`

### 3. 启动前端（开发模式）

```bash
cd ChatRobot/src/main/resources/static/chat-robot-frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，通过 Vite 代理将 `/api` 请求转发到后端。

### 4. 访问

浏览器打开 `http://localhost:5173`，点击"对话机器人"即可开始聊天。

## 学习价值

这个项目覆盖了一个全栈 AI 应用的核心知识点：

**后端方面：**
- Spring Boot 应用搭建与配置
- Spring AI 框架集成本地大模型
- ChatClient / ChatMemory / Advisor 等 AI 抽象的使用
- 多角色 ChatClient 配置（不同系统提示词实现角色切换）
- 流式响应（Flux / Server-Sent Events 思路）
- RESTful API 设计与接口版本管理
- 接口 + 实现的仓储模式（为后续持久化做准备）

**前端方面：**
- Vue 3 Composition API（`<script setup>`、`ref`、`computed`、`inject`）
- Vue Router 路由配置与动态参数
- 双视图模式：通用多会话视图 vs 单次游戏视图
- ReadableStream 流式数据读取与解析
- 打字机效果的实现（队列 + 定时器）
- 正则解析结构化文本（游戏元信息提取）
- CSS 变量实现暗色/亮色主题
- 响应式布局与移动端适配

**全栈协作方面：**
- 前后端分离架构与代理配置
- 数据格式约定（MessageVO）
- 会话管理（chatID 生成与传递）
- 跨域问题解决（CORS 配置）
