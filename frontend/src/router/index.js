import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/chat',
      name: 'Chat', 
      component: () => import('@/views/Chat.vue')
    }
  ]
})

export default router
