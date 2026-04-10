import { createRouter, createWebHistory } from 'vue-router'
import HealthView from '../views/HealthView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/health'
    },
    {
      path: '/health',
      name: 'health',
      component: HealthView
    }
    // 后续可在此添加商品、购物车、订单等路由
  ]
})

export default router
