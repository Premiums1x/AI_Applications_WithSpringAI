import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ChatBotView from '../views/ChatBotView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/chatbot',
    redirect: '/chat/chatbot'
  },
  {
    path: '/chat/:moduleName',
    name: 'chat',
    component: ChatBotView,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
