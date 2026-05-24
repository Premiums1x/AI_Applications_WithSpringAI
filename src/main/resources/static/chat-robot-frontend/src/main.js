import { createApp, ref, provide, readonly } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'

// 全局主题状态 - 默认跟随系统，也可手动切换
const systemDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
const savedTheme = localStorage.getItem('theme')
const isDark = ref(savedTheme ? savedTheme === 'dark' : systemDark)

// 应用主题到document
const applyTheme = (dark) => {
  if (dark) {
    document.documentElement.setAttribute('data-theme', 'dark')
  } else {
    document.documentElement.removeAttribute('data-theme')
  }
  localStorage.setItem('theme', dark ? 'dark' : 'light')
}

applyTheme(isDark.value)

// 监听系统主题变化
if (window.matchMedia) {
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    if (!localStorage.getItem('theme')) {
      isDark.value = e.matches
      applyTheme(e.matches)
    }
  })
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  applyTheme(isDark.value)
}

// 提供全局主题
export const themeSymbol = Symbol('theme')

const app = createApp(App)
app.provide(themeSymbol, {
  isDark: readonly(isDark),
  toggleTheme
})
app.use(router)
app.mount('#app')
