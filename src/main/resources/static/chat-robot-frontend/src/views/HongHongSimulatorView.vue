<template>
  <div class="chatbot-container">
    <!-- 左侧侧边栏：只放新游戏按钮 -->
    <aside class="sidebar" :class="{ 'collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <button class="new-chat-btn" @click="startNewChat">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新游戏
        </button>
        <button class="toggle-btn" @click="toggleSidebar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M11 17l-5-5 5-5M18 17l-5-5 5-5"/>
          </svg>
        </button>
      </div>

      <div class="sidebar-hint">
        <p>通关或失败后点击「新游戏」重新开始</p>
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
          <h2 class="chat-title">哄哄模拟器</h2>
        </div>
        <div class="header-actions">
          <button class="action-btn" @click="toggleTheme" title="切换主题">
            <span v-if="isDark">☀️</span>
            <span v-else>🌙</span>
          </button>
        </div>
      </header>

      <!-- 原谅值进度条 -->
      <div v-if="messages.length > 0" class="game-status-bar">
        <div class="game-stats">
          <span v-if="currentMood" class="mood-tag">{{ currentMood }}</span>
          <span v-if="lastScore" class="score-tag" :class="{ 'positive': lastScore.startsWith('+'), 'negative': lastScore.startsWith('-') }">
            得分 {{ lastScore }}
          </span>
        </div>
        <div class="forgiveness-track">
          <div class="forgiveness-fill" :style="{ width: Math.max(0, Math.min(100, forgivenessValue)) + '%' }"
               :class="{ 'win': forgivenessValue >= 100, 'lose': forgivenessValue <= 0 }"></div>
          <span class="forgiveness-label">原谅值 {{ forgivenessValue }}/100</span>
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="messages-area" ref="messagesContainer">
        <div v-if="messages.length === 0" class="welcome-screen">
          <div class="welcome-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
          </div>
          <h2>哄哄模拟器</h2>
          <p>你的女朋友生气了，快说正确的话哄她开心吧！<br>初始原谅值 20/100，达到 100 通关，降到 0 失败。</p>
        </div>

        <div
          v-for="(message, index) in messages"
          :key="index"
          class="message"
          :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'assistant' }"
        >
          <div class="message-avatar">
            <div v-if="message.role === 'user'" class="avatar user-avatar">我</div>
            <div v-else class="avatar ai-avatar">女友</div>
          </div>
          <div class="message-content">
            <div v-if="!message.isComplete" class="message-text">{{ message.content }}<span v-if="message.isTyping" class="typing-cursor">|</span></div>
            <div v-else class="message-text" v-html="formatMessage(message.content)"></div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="isLoading" class="message ai-message loading-message">
          <div class="message-avatar">
            <div class="avatar ai-avatar">女友</div>
          </div>
          <div class="message-content">
            <div class="loading-dots">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>

        <!-- 通关/失败提示 -->
        <div v-if="gameState !== 'playing'" class="game-result-banner" :class="gameState">
          <div v-if="gameState === 'win'">🎉 恭喜你通关了！女朋友已经原谅你了！</div>
          <div v-if="gameState === 'lose'">💔 游戏结束，你的女朋友已经甩了你！</div>
          <button class="restart-btn" @click="startNewChat">再来一局</button>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-area" v-if="gameState === 'playing'">
        <div class="input-container">
          <textarea
            v-model="userInput"
            class="chat-input"
            placeholder="输入哄她的话..."
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

  <!-- 生气理由弹窗 -->
  <div v-if="showReasonDialog" class="dialog-overlay" @click.self="showReasonDialog = false">
    <div class="dialog-box">
      <h3 class="dialog-title">💢 女朋友为什么生气了？</h3>
      <p class="dialog-desc">你可以输入一个理由，也可以让我们随机生成一个~</p>
      <textarea
        v-model="reasonInput"
        class="dialog-input"
        placeholder="比如：忘记恋爱纪念日、打游戏忽略了她..."
        rows="3"
      ></textarea>
      <div class="dialog-actions">
        <button class="dialog-btn secondary" @click="showReasonDialog = false">取消</button>
        <button class="dialog-btn secondary" @click="startGameWithReason()">🎲 随机生成</button>
        <button class="dialog-btn primary" @click="startGameWithReason(reasonInput.trim())">开始游戏</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch, inject, onMounted } from 'vue'
import { themeSymbol } from '../main.js'

const { isDark, toggleTheme } = inject(themeSymbol)

// 每次进入页面，如果没有进行中的游戏，自动弹出理由选择
onMounted(() => {
  if (messages.value.length === 0) {
    startNewChat()
  }
})

const sidebarCollapsed = ref(false)
const userInput = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const inputRef = ref(null)

// 游戏状态
const gameState = ref('playing') // 'playing' | 'win' | 'lose'
const forgivenessValue = ref(20) // 0-100
const currentMood = ref('')
const lastScore = ref('')

// 消息列表
const messages = ref([])
const chatID = ref('')

// 生气理由弹窗
const showReasonDialog = ref(false)
const reasonInput = ref('')

const generateChatID = () => {
  return 'chat_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const startNewChat = () => {
  showReasonDialog.value = true
  reasonInput.value = ''
}

const startGameWithReason = async (reason) => {
  showReasonDialog.value = false

  // 重置游戏
  messages.value = []
  gameState.value = 'playing'
  forgivenessValue.value = 20
  currentMood.value = ''
  lastScore.value = ''
  chatID.value = generateChatID()

  // 发送理由作为第一条消息
  const prompt = reason || '请随机生成一个女友生气的理由开始游戏'

  const userMessage = { role: 'user', content: prompt }
  messages.value.push(userMessage)
  isLoading.value = true
  scrollToBottom()

  try {
    const response = await fetch(`/api/ai/game?prompt=${encodeURIComponent(prompt)}&chatID=${encodeURIComponent(chatID.value)}`)
    if (!response.ok) throw new Error('网络请求失败')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    messages.value.push({
      role: 'assistant',
      content: '',
      isTyping: true,
      isComplete: false
    })

    const msgIdx = messages.value.length - 1
    isLoading.value = false

    let rawBuffer = ''
    let contentQueue = []
    let typewriterTimer = null

    const processTypewriter = () => {
      if (contentQueue.length > 0) {
        const batch = Math.min(contentQueue.length, contentQueue.length > 200 ? 5 : 2)
        for (let i = 0; i < batch; i++) {
          messages.value[msgIdx].content += contentQueue.shift()
        }
        scrollToBottom()
      }
      if (contentQueue.length === 0 && messages.value[msgIdx].isComplete) {
        clearInterval(typewriterTimer)
        typewriterTimer = null
        messages.value[msgIdx].isTyping = false
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
        for (const char of rawBuffer) contentQueue.push(char)
        rawBuffer = ''
        startTypewriter()
      }
      if (done) break
    }

    messages.value[msgIdx].isComplete = true

    await new Promise(resolve => {
      const check = setInterval(() => {
        if (contentQueue.length === 0 && !typewriterTimer) {
          clearInterval(check)
          resolve()
        }
      }, 50)
    })

    // 解析女友回复
    const result = parseGameResult(messages.value[msgIdx].content)
    messages.value[msgIdx].content = result.speech
    currentMood.value = result.mood
    lastScore.value = result.score
    forgivenessValue.value = result.forgiveness
    if (result.forgiveness >= 100) {
      gameState.value = 'win'
    } else if (result.forgiveness <= 0) {
      gameState.value = 'lose'
    }

  } catch (error) {
    console.error('发送消息失败:', error)
    isLoading.value = false
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发生了错误，请稍后重试。',
      isTyping: false,
      isComplete: true
    })
  }

  scrollToBottom()
}

const sendMessage = async () => {
  const message = userInput.value.trim()
  if (!message || isLoading.value) return

  const userMessage = { role: 'user', content: message }
  messages.value.push(userMessage)
  userInput.value = ''
  isLoading.value = true

  if (inputRef.value) inputRef.value.style.height = 'auto'
  scrollToBottom()

  try {
    const response = await fetch(`/api/ai/game?prompt=${encodeURIComponent(message)}&chatID=${encodeURIComponent(chatID.value)}`)
    if (!response.ok) throw new Error('网络请求失败')

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    messages.value.push({
      role: 'assistant',
      content: '',
      isTyping: true,
      isComplete: false
    })

    const msgIdx = messages.value.length - 1
    isLoading.value = false

    let rawBuffer = ''
    let contentQueue = []
    let typewriterTimer = null

    const processTypewriter = () => {
      let hasNew = false

      if (contentQueue.length > 0) {
        const batch = Math.min(contentQueue.length, contentQueue.length > 200 ? 5 : 2)
        for (let i = 0; i < batch; i++) {
          messages.value[msgIdx].content += contentQueue.shift()
        }
        hasNew = true
      }

      if (hasNew) scrollToBottom()

      if (contentQueue.length === 0 && messages.value[msgIdx].isComplete) {
        clearInterval(typewriterTimer)
        typewriterTimer = null
        messages.value[msgIdx].isTyping = false
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
        for (const char of rawBuffer) contentQueue.push(char)
        rawBuffer = ''
        startTypewriter()
      }
      if (done) break
    }

    messages.value[msgIdx].isComplete = true

    await new Promise(resolve => {
      const check = setInterval(() => {
        if (contentQueue.length === 0 && !typewriterTimer) {
          clearInterval(check)
          resolve()
        }
      }, 50)
    })

    // 解析女友回复
    const result = parseGameResult(messages.value[msgIdx].content)
    messages.value[msgIdx].content = result.speech
    gameState.value = result.forgiveness >= 100 ? 'win' : result.forgiveness <= 0 ? 'lose' : 'playing'
    forgivenessValue.value = result.forgiveness
    currentMood.value = result.mood
    lastScore.value = result.score

  } catch (error) {
    console.error('发送消息失败:', error)
    isLoading.value = false
    messages.value.push({
      role: 'assistant',
      content: '抱歉，发生了错误，请稍后重试。',
      isTyping: false,
      isComplete: true
    })
  }

  scrollToBottom()
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

// 解析女友回复格式
const parseGameResult = (content) => {
  const moodMatch = content.match(/（([^）]+)）/)
  const mood = moodMatch ? moodMatch[1] : ''

  const scoreMatch = content.match(/得分：([+-]?\d+)/)
  const score = scoreMatch ? scoreMatch[1] : ''

  const forgivenessMatch = content.match(/原值：(\d+)\/100/)
  const forgiveness = forgivenessMatch ? parseInt(forgivenessMatch[1]) : 20

  let speech = content
    .replace(/（[^）]+）/, '')
    .replace(/得分：[^\n]+/g, '')
    .replace(/原值：[^\n]+/g, '')
    .trim()

  return { mood, score, forgiveness, speech }
}

watch(messages, scrollToBottom, { deep: true })
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

.sidebar-hint {
  padding: 16px;
  color: var(--text-tertiary);
  font-size: 13px;
  text-align: center;
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
  background: var(--bg-hover);
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

/* ===== 哄哄模拟器游戏状态栏 ===== */
.game-status-bar {
  padding: 12px 20px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.game-stats {
  display: flex;
  gap: 12px;
  align-items: center;
}

.mood-tag {
  padding: 4px 10px;
  border-radius: 12px;
  background: var(--accent-light);
  color: var(--accent-primary);
  font-size: 13px;
  font-weight: 600;
}

.score-tag {
  padding: 4px 10px;
  border-radius: 12px;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  font-size: 13px;
}

.score-tag.positive {
  background: rgba(82, 196, 26, 0.15);
  color: #52c41a;
}

.score-tag.negative {
  background: rgba(255, 77, 79, 0.15);
  color: #ff4d4f;
}

.forgiveness-track {
  position: relative;
  height: 24px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  overflow: hidden;
}

.forgiveness-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff4d4f 0%, #faad14 50%, #52c41a 100%);
  border-radius: 12px;
  transition: width 0.6s ease;
}

.forgiveness-fill.win {
  background: #52c41a;
}

.forgiveness-fill.lose {
  background: #ff4d4f;
}

.forgiveness-label {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  text-shadow: 0 0 2px var(--bg-secondary);
}

/* ===== 通关/失败提示 ===== */
.game-result-banner {
  text-align: center;
  padding: 20px;
  margin: 12px auto;
  max-width: 600px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.game-result-banner.win {
  background: rgba(82, 196, 26, 0.15);
  color: #52c41a;
  border: 1px solid rgba(82, 196, 26, 0.3);
}

.game-result-banner.lose {
  background: rgba(255, 77, 79, 0.15);
  color: #ff4d4f;
  border: 1px solid rgba(255, 77, 79, 0.3);
}

.restart-btn {
  padding: 10px 24px;
  border-radius: 8px;
  background: var(--accent-primary);
  color: var(--text-inverse);
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
  margin: 0 auto;
}

.restart-btn:hover {
  opacity: 0.9;
}

/* ===== 弹窗 ===== */
.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  backdrop-filter: blur(4px);
}

.dialog-box {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 28px;
  width: 100%;
  max-width: 440px;
  box-shadow: var(--shadow-lg);
}

.dialog-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.dialog-desc {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.dialog-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-color);
  border-radius: 10px;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-size: 15px;
  resize: none;
  outline: none;
  margin-bottom: 20px;
  font-family: inherit;
}

.dialog-input:focus {
  border-color: var(--accent-primary);
}

.dialog-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.dialog-btn {
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.dialog-btn.primary {
  background: var(--accent-primary);
  color: var(--text-inverse);
}

.dialog-btn.primary:hover {
  opacity: 0.9;
}

.dialog-btn.secondary {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
}

.dialog-btn.secondary:hover {
  background: var(--bg-hover);
  color: var(--text-primary);
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
