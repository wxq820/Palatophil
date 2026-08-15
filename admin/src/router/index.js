import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'user',      name: 'User',      component: () => import('@/views/user/User.vue'),           meta: { title: '用户管理' } },
      { path: 'ingredient',name: 'Ingredient',component: () => import('@/views/ingredient/Ingredient.vue'), meta: { title: '食材库审核' } },
      { path: 'recipe',    name: 'Recipe',    component: () => import('@/views/recipe/Recipe.vue'),       meta: { title: '食谱审核' } },
      { path: 'order',     name: 'Order',     component: () => import('@/views/order/Order.vue'),         meta: { title: '采购单管理' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    return next()
  }
  if (!userStore.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  next()
})

export default router
