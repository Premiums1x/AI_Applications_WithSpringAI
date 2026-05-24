<template>
  <div class="home-container">
    <!-- 主题切换按钮 -->
    <button class="theme-toggle" @click="toggleTheme" title="切换主题">
      <span v-if="isDark">☀️</span>
      <span v-else>🌙</span>
    </button>

    <div class="home-content">
      <header class="home-header">
        <h1 class="main-title">AI 智能助手平台</h1>
        <p class="subtitle">探索人工智能的无限可能</p>
      </header>
      
      <div class="modules-flex">
        <div 
          v-for="module in modules" 
          :key="module.id"
          class="module-card"
          :class="{ 'disabled': !module.available }"
          @click="navigateToModule(module)"
        >
          <div class="card-icon" v-html="module.icon"></div>
          <h3 class="card-title">{{ module.name }}</h3>
          <p class="card-desc">{{ module.description }}</p>
          <div class="card-arrow">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { inject } from 'vue'
import { useRouter } from 'vue-router'
import { themeSymbol } from '../main.js'

const router = useRouter()
const { isDark, toggleTheme } = inject(themeSymbol)

const modules = [
  {
    id: 'chatbot',
    name: '对话机器人',
    description: '基于大模型的智能对话助手，支持流式输出与深度思考',
    icon: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
    </svg>`,
    available: true,
    route: '/chat/chatbot'
  },
  {
    id: 'honghong',
    name: '哄哄模拟器',
    description: '模拟情感交流场景，提升沟通技巧与情商',
    icon: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
    </svg>`,
    available: true,
    route: '/chat/honghong'
  },
  {
    id: 'customerservice',
    name: '智能客服',
    description: '7x24小时智能客服解决方案，快速响应客户需求',
    icon: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>`,
    available: true,
    route: '/chat/customerservice'
  },
  {
    id: 'chatpdf',
    name: 'ChatPDF',
    description: '与PDF文档智能对话，快速提取关键信息与知识问答',
    icon: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
      <polyline points="14 2 14 8 20 8"/>
      <line x1="16" y1="13" x2="8" y2="13"/>
      <line x1="16" y1="17" x2="8" y2="17"/>
      <polyline points="10 9 9 9 8 9"/>
    </svg>`,
    available: true,
    route: '/chat/chatpdf'
  }
]

const navigateToModule = (module) => {
  if (module.available) {
    router.push(`/chat/${module.id}`)
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background-color: var(--bg-primary);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  transition: var(--transition);
}

.theme-toggle {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  color: var(--text-primary);
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition);
  z-index: 100;
  box-shadow: var(--shadow-sm);
}

.theme-toggle:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-md);
}

.home-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 1000px;
  width: 100%;
}

.home-header {
  text-align: center;
  margin-bottom: 60px;
}

.main-title {
  font-size: 2.8rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 12px 0;
  transition: var(--transition);
}

.subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin: 0;
  transition: var(--transition);
}

.modules-flex {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 24px;
  width: 100%;
}

.module-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 32px 24px;
  width: 220px;
  cursor: pointer;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: var(--shadow-sm);
}

.module-card:not(.disabled):hover {
  transform: translateY(-6px);
  box-shadow: var(--shadow-lg);
  border-color: var(--accent-primary);
}

.module-card:not(.disabled):hover .card-icon {
  color: var(--accent-primary);
  transform: scale(1.1);
}

.module-card:not(.disabled):hover .card-arrow {
  opacity: 1;
  transform: translateX(0);
}

.module-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.module-card.disabled .card-icon {
  color: var(--text-tertiary);
}

.card-icon {
  color: var(--text-secondary);
  margin-bottom: 20px;
  transition: var(--transition);
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  transition: var(--transition);
}

.card-desc {
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.5;
  margin: 0;
  transition: var(--transition);
}

.card-arrow {
  position: absolute;
  bottom: 16px;
  right: 16px;
  opacity: 0;
  transform: translateX(8px);
  transition: var(--transition);
  color: var(--accent-primary);
}

@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
  }
  
  .module-card {
    width: 100%;
    max-width: 320px;
  }
}
</style>
