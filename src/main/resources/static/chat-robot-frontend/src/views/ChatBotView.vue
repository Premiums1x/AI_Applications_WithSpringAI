<template>
  <div class="chatbot-container">
    <!-- 左侧侧边栏 -->
    <aside class="sidebar" :class="{ 'collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <button class="new-chat-btn" @click="startNewChat">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新建对话
        </button>
        <button class="toggle-btn" @click="toggleSidebar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 17l-5-5 5-5M18 17l-5-5 5-5"/>
          </svg>
        </button>
      </div>
      
      <div class="chat-history">
        <div 
          v-for="(chat, index) in chatHistory" 
          :key="index"
          class="history-item"
          :class="{ 'active': currentChatIndex === index }"
          @click="selectChat(index)"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
          </svg>
          <span class="history-title">{{ chat.title || '新对话' }}</span>
        </div>
      </div>
    </aside>

    <!-- 主聊天区域 -->
    <main class="chat-main">
      <!-- 顶部导航 -->
      <header class="chat-header">
        <div class="header-left">
          <button v-if="sidebarCollapsed" class="menu-btn" @click="toggleSidebar">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="12" x2="21" y2="12"/>
              <line x1="3" y1="6" x2="21" y2="6"/>
              <line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
          </button>
          <h2 class="chat-title">{{ moduleNames[props.moduleName] || props.moduleName }}</h2>
        </div>
        <div class="header-actions">
          <button class="action-btn" @click="toggleTheme" title="切换主题">
            <span v-if="isDark">☀️</span>
            <span v-else>🌙</span>
          </button>
          <button class="action-btn" @click="clearCurrentChat" title="清除对话">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
            </svg>
          </button>
        </div>
      </header>

      <!-- 消息区域 -->
      <div class="messages-area" ref="messagesContainer">
        <div v-if="currentMessages.length === 0" class="welcome-screen">
          <div class="welcome-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
            </svg>
          </div>
          <h2>有什么可以帮你的？</h2>
          <p>我是你的智能编码助手，可以帮你解答编程问题、优化代码、提供技术建议等。</p>
        </div>

        <div 
          v-for="(message, index) in currentMessages" 
          :key="index"
          class="message"
          :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'assistant' }"
        >
          <div class="message-avatar">
            <div v-if="message.role === 'user'" class="avatar user-avatar">我</div>
            <div v-else class="avatar ai-avatar">AI</div>
          </div>
          <div class="message-content">
            <!-- 思考过程 - 橙黄色 -->
            <div v-if="message.thinking || message.isThinking" class="thinking-section">
              <div class="thinking-header" @click="message.showThinking = !message.showThinking">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :class="{ 'rotated': message.showThinking }">
                  <polyline points="6 9 12 15 18 9"/>
                </svg>
                <span v-if="message.isThinking && !message.isComplete" class="thinking-pulse">💭 正在思考</span>
                <span v-else>💭 思考过程</span>
              </div>
              <div v-show="message.showThinking" class="thinking-content">
                <pre>{{ message.thinking }}</pre>
                <span v-if="message.isThinking && !message.isComplete" class="typing-cursor">|</span>
              </div>
            </div>
            
            <!-- 回答内容 - 青绿色 -->
            <div v-if="!message.isComplete" class="message-text">{{ message.content }}<span v-if="message.isTyping" class="typing-cursor">|</span></div>
            <div v-else class="message-text" v-html="formatMessage(message.content)"></div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="isLoading" class="message ai-message loading-message">
          <div class="message-avatar">
            <div class="avatar ai-avatar">AI</div>
          </div>
          <div class="message-content">
            <div class="loading-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

      </div>

      <!-- 输入区域 -->
      <div class="input-area">
        <div class="input-container">
          <textarea
            v-model="userInput"
            class="chat-input"
            placeholder="输入你的问题..."
            rows="1"
            @keydown.enter.prevent="sendMessage"
            @input="autoResize"
            ref="inputRef"
          ></textarea>
          <button 
            class="send-btn"
            :class="{ 'active': userInput.trim() && !isLoading }"
            @click="sendMessage"
            :disabled="!userInput.trim() || isLoading"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="22" y1="2" x2="11" y2="13"/>
              <polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
        <p class="input-tip">按 Enter 发送，Shift + Enter 换行</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, inject, onMounted } from 'vue'
import { themeSymbol } from '../main.js'

const { isDark, toggleTheme } = inject(themeSymbol)

const props = defineProps({
  moduleName: {
    type: String,
    required: true
  }
})

const moduleNames = {
  chatbot: '对话机器人',
  customerservice: '智能客服',
  chatpdf: 'ChatPDF'
}

// 前端模块名 → 后端 ServiceTypes 枚举值映射
const serviceTypeMap = {
  chatbot: 'chat',
  customerservice: 'service',
  chatpdf: 'chatPDF'
}

const sidebarCollapsed = ref(false)
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const inputRef = ref(null)
const currentChatIndex = ref(0)

const generateChatID = () => {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const chatHistory = ref([])

onMounted(async () => {
  const serviceType = serviceTypeMap[props.moduleName] || props.moduleName
  let chatIds = []
  try {
    const res = await fetch(`/api/ai/history/${serviceType}`)
    if (res.ok) {
      chatIds = await res.json()
    }
  } catch (e) {
    console.error('获取历史记录失败:', e)
  }

  if (chatIds && chatIds.length > 0) {
    chatIds.forEach(id => {
      const shortTitle = id.length > 20 ? id.slice(0, 17) + '...' : id
      chatHistory.value.push({
        title: shortTitle,
        messages: [],
        chatID: id
      })
    })
  }

  chatHistory.value.push({
    title: '新对话',
    messages: [],
    chatID: generateChatID()
  })
  currentChatIndex.value = chatHistory.value.length - 1
})

const currentMessages = computed(() => {
  return chatHistory.value[currentChatIndex.value]?.messages || []
})

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const startNewChat = () => {
  chatHistory.value.unshift({ title: '新对话', messages: [], chatID: generateChatID() })
  currentChatIndex.value = 0
}

const selectChat = async (index) => {
  currentChatIndex.value = index
  const chat = chatHistory.value[index]
  if (!chat || chat.messages.length > 0) return

  // 从后端拉取该会话的历史消息
  const serviceType = serviceTypeMap[props.moduleName] || props.moduleName
  try {
    const res = await fetch(`/api/ai/history/${serviceType}/${chat.chatID}`)
    if (res.ok) {
      const messages = await res.json()
      chat.messages = messages.map(msg => ({
        role: msg.role,
        content: msg.context,
        thinking: '',
        showThinking: false,
        isThinking: false,
        isTyping: false,
        isComplete: true
      }))
    }
  } catch (e) {
    console.error('加载对话消息失败:', e)
  }
}

const clearCurrentChat = () => {
  if (chatHistory.value[currentChatIndex.value]) {
    chatHistory.value[currentChatIndex.value].messages = []
  }
}

const autoResize = () => {
  const textarea = inputRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px'
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (content) => {
  if (!content) return ''
  return content
    .replace(/```(\w+)?\n([\s\S]*?)```/g, '<pre><code>$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

// ========== 打字机效果 + 流式处理 ==========

const THINK_START_TAGS = ['<thinking>', ' thinking']
const THINK_END_TAGS = ['</thinking>', ' thinking']

const parseBuffer = (buffer, currentInThinking) => {
  let content = ''
  let thinking = ''
  let text = buffer
  let inThinking = currentInThinking

  while (text.length > 0) {
    if (!inThinking) {
      let startIdx = -1
      let startTag = ''
      for (const tag of THINK_START_TAGS) {
        const idx = text.indexOf(tag)
        if (idx !== -1 && (startIdx === -1 || idx < startIdx)) {
          startIdx = idx
          startTag = tag
        }
      }

      if (startIdx === -1) {
        content += text
        text = ''
      } else {
        content += text.slice(0, startIdx)
        text = text.slice(startIdx + startTag.length)
        inThinking = true
      }
    } else {
      let endIdx = -1
      let endTag = ''
      for (const tag of THINK_END_TAGS) {
        const idx = text.indexOf(tag)
        if (idx !== -1 && (endIdx === -1 || idx < endIdx)) {
          endIdx = idx
          endTag = tag
        }
      }

      if (endIdx === -1) {
        const safeLen = Math.max(0, text.length - 50)
        thinking += text.slice(0, safeLen)
        const remaining = text.slice(safeLen)
        return { content, thinking, remaining, inThinking: true }
      } else {
        thinking += text.slice(0, endIdx)
        text = text.slice(endIdx + endTag.length)
        inThinking = false
      }
    }
  }

  return { content, thinking, remaining: '', inThinking }
}

const sendMessage = async () => {
  const message = userInput.value.trim()
  if (!message || isLoading.value) return

  const userMessage = { role: 'user', content: message }

  if (!chatHistory.value[currentChatIndex.value]) {
    chatHistory.value[currentChatIndex.value] = { title: message.slice(0, 20), messages: [] }
  }

  chatHistory.value[currentChatIndex.value].messages.push(userMessage)
  userInput.value = ''
  isLoading.value = true

  if (inputRef.value) inputRef.value.style.height = 'auto'
  scrollToBottom()

  try {
    const currentChat = chatHistory.value[currentChatIndex.value]
    const chatID = currentChat?.chatID || generateChatID()
    if (!currentChat.chatID) currentChat.chatID = chatID

    const response = await fetch(`/api/ai/chat/stream?prompt=${encodeURIComponent(message)}&chatID=${encodeURIComponent(chatID)}`)
    if (!response.ok) throw new Error('网络请求失败')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    chatHistory.value[currentChatIndex.value].messages.push({
      role: 'assistant',
      content: '',
      thinking: '',
      showThinking: true,
      isThinking: false,
      isTyping: true,
      isComplete: false
    })

    const msgList = chatHistory.value[currentChatIndex.value].messages
    const msgIdx = msgList.length - 1
    isLoading.value = false

    let rawBuffer = ''
    let contentQueue = []
    let thinkingQueue = []
    let typewriterTimer = null
    let inThinking = false

    const processTypewriter = () => {
      let hasNew = false

      if (thinkingQueue.length > 0) {
        const batch = Math.min(thinkingQueue.length, thinkingQueue.length > 200 ? 5 : 2)
        for (let i = 0; i < batch; i++) {
          msgList[msgIdx].thinking += thinkingQueue.shift()
        }
        hasNew = true
      }

      if (contentQueue.length > 0) {
        const batch = Math.min(contentQueue.length, contentQueue.length > 200 ? 5 : 2)
        for (let i = 0; i < batch; i++) {
          msgList[msgIdx].content += contentQueue.shift()
        }
        hasNew = true
      }

      if (hasNew) scrollToBottom()

      if (thinkingQueue.length === 0 && contentQueue.length === 0 && msgList[msgIdx].isComplete) {
        clearInterval(typewriterTimer)
        typewriterTimer = null
        msgList[msgIdx].isTyping = false
      }
    }

    const startTypewriter = () => {
      if (typewriterTimer) return
      typewriterTimer = setInterval(processTypewriter, 15)
    }

    while (true) {
      const { done, value } = await reader.read()

      if (value) {
        rawBuffer += decoder.decode(value, { stream: true })
        const result = parseBuffer(rawBuffer, inThinking)

        if (result.thinking) {
          for (const char of result.thinking) thinkingQueue.push(char)
        }
        if (result.content) {
          for (const char of result.content) contentQueue.push(char)
        }

        inThinking = result.inThinking
        msgList[msgIdx].isThinking = inThinking
        rawBuffer = result.remaining
        startTypewriter()
      }

      if (done) break
    }

    // 流接收完毕，处理剩余
    if (rawBuffer) {
      const result = parseBuffer(rawBuffer, inThinking)
      if (result.thinking) {
        for (const char of result.thinking) thinkingQueue.push(char)
      }
      if (result.content) {
        for (const char of result.content) contentQueue.push(char)
      }
      inThinking = result.inThinking
      // 如果还有 remaining（流已结束，不再有后续 chunk），直接归入当前模式
      if (result.remaining) {
        if (inThinking) {
          for (const char of result.remaining) thinkingQueue.push(char)
        } else {
          for (const char of result.remaining) contentQueue.push(char)
        }
      }
    }

    msgList[msgIdx].isComplete = true
    msgList[msgIdx].isThinking = false

    // 等待打字机完成
    await new Promise(resolve => {
      const check = setInterval(() => {
        if (thinkingQueue.length === 0 && contentQueue.length === 0 && !typewriterTimer) {
          clearInterval(check)
          resolve()
        }
      }, 50)
    })

    if (chatHistory.value[currentChatIndex.value].title === '新对话') {
      chatHistory.value[currentChatIndex.value].title = message.slice(0, 20)
    }

  } catch (error) {
    console.error('发送消息失败:', error)
    isLoading.value = false
    chatHistory.value[currentChatIndex.value].messages.push({
      role: 'assistant',
      content: '抱歉，发生了错误，请稍后重试。',
      thinking: '',
      showThinking: false,
      isThinking: false,
      isTyping: false,
      isComplete: true
    })
  }

  scrollToBottom()
}

watch(currentMessages, scrollToBottom, { deep: true })
</script>

<style scoped>
.chatbot-container {
  display: flex;
  height: 100vh;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: var(--transition);
}

.sidebar {
  width: 260px;
  background-color: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  flex-shrink: 0;
}

.sidebar.collapsed {
  width: 0;
  overflow: hidden;
  border-right: none;
}

.sidebar-header {
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
}

.new-chat-btn {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: var(--transition);
}

.new-chat-btn:hover {
  background: var(--bg-hover);
  border-color: var(--accent-primary);
}

.toggle-btn {
  padding: 10px;
  background: transparent;
  border: none;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.toggle-btn:hover {
  background: var(--bg-hover);
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 4px;
  color: var(--text-secondary);
  font-size: 14px;
}

.history-item:hover {
  background: var(--bg-hover);
}

.history-item.active {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.history-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background-color: var(--bg-primary);
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-color);
  gap: 12px;
  background-color: var(--bg-secondary);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.menu-btn {
  padding: 8px;
  background: transparent;
  border: none;
  color: var(--text-primary);
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
}

.menu-btn:hover {
  background: var(--bg-hover);
}

.chat-title {
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: var(--text-primary);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 8px;
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
  font-size: 18px;
}

.action-btn:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.welcome-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

.welcome-icon {
  color: var(--accent-primary);
  margin-bottom: 20px;
}

.welcome-screen h2 {
  font-size: 24px;
  margin: 0 0 12px 0;
  color: var(--text-primary);
}

.welcome-screen p {
  font-size: 14px;
  max-width: 400px;
  line-height: 1.6;
}

.message {
  display: flex;
  gap: 16px;
  padding: 20px 40px;
  max-width: 900px;
  margin: 0 auto;
}

.user-message {
  background-color: var(--bg-primary);
}

.ai-message {
  background-color: var(--bg-secondary);
  border-top: 1px solid var(--border-light);
  border-bottom: 1px solid var(--border-light);
}

.message-avatar {
  flex-shrink: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-inverse);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.ai-avatar {
  background: var(--accent-primary);
}

.message-content {
  flex: 1;
  min-width: 0;
  line-height: 1.6;
}

.message-text {
  font-size: 15px;
  color: var(--text-primary);
  white-space: pre-wrap;
  word-wrap: break-word;
}

.message-text :deep(pre) {
  background: var(--bg-code);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
  border: 1px solid var(--border-color);
}

.message-text :deep(code) {
  background: var(--bg-code);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  color: var(--accent-primary);
}

.message-text :deep(pre code) {
  background: transparent;
  padding: 0;
  color: var(--text-primary);
}

/* 思考过程 - 橙黄色 */
.thinking-section {
  margin-bottom: 16px;
  border: 1px solid var(--thinking-border);
  border-radius: 8px;
  overflow: hidden;
  background: var(--bg-thinking);
}

.thinking-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  cursor: pointer;
  font-size: 13px;
  color: var(--thinking-text);
  font-weight: 600;
  transition: background 0.2s;
}

.thinking-header:hover {
  background: rgba(255, 213, 79, 0.2);
}

.thinking-header svg {
  transition: transform 0.2s;
  flex-shrink: 0;
}

.thinking-header svg.rotated {
  transform: rotate(180deg);
}

.thinking-pulse {
  animation: thinkingPulse 1.5s ease-in-out infinite;
}

@keyframes thinkingPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.thinking-content {
  padding: 14px;
  background: rgba(255, 213, 79, 0.1);
  font-size: 13px;
  color: var(--thinking-text);
  max-height: 400px;
  overflow-y: auto;
}

.thinking-content pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
}

/* 打字机光标 */
.typing-cursor {
  color: var(--accent-primary);
  font-weight: 300;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 加载动画 */
.loading-dots {
  display: flex;
  gap: 6px;
  padding: 8px 0;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: var(--accent-primary);
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 输入区域 */
.input-area {
  padding: 20px 40px;
  border-top: 1px solid var(--border-color);
  background-color: var(--bg-secondary);
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  max-width: 900px;
  margin: 0 auto;
  background: var(--bg-primary);
  border-radius: 12px;
  padding: 10px 14px;
  border: 1px solid var(--border-color);
  transition: border-color 0.2s;
}

.input-container:focus-within {
  border-color: var(--accent-primary);
}

.chat-input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 15px;
  resize: none;
  outline: none;
  max-height: 200px;
  line-height: 1.5;
  font-family: inherit;
}

.chat-input::placeholder {
  color: var(--text-tertiary);
}

.send-btn {
  padding: 8px;
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn.active {
  color: var(--accent-primary);
}

.send-btn.active:hover {
  background: var(--accent-light);
}

.send-btn:disabled {
  cursor: not-allowed;
}

.input-tip {
  text-align: center;
  font-size: 12px;
  color: var(--text-tertiary);
  margin: 8px 0 0 0;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 100;
  }

  .message {
    padding: 16px 20px;
  }

  .input-area {
    padding: 12px 20px;
  }
}
</style>
