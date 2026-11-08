import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/layout/index.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'el-icon-data-line' }
      },
      {
        path: 'work-order',
        name: 'WorkOrder',
        component: () => import('@/views/work-order/index.vue'),
        meta: { title: '工单管理', icon: 'el-icon-tickets' }
      },
      {
        path: 'work-order/:id',
        name: 'WorkOrderDetail',
        component: () => import('@/views/work-order/detail.vue'),
        meta: { title: '工单详情', hidden: true }
      },
      {
        path: 'dispatch',
        name: 'Dispatch',
        component: () => import('@/views/dispatch/index.vue'),
        meta: { title: '派单看板', icon: 'el-icon-s-promotion' }
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/index.vue'),
        meta: { title: '设备台账', icon: 'el-icon-cpu' }
      },
      {
        path: 'inspection/plan',
        name: 'InspectionPlan',
        component: () => import('@/views/inspection/plan.vue'),
        meta: { title: '巡检计划', icon: 'el-icon-date' }
      },
      {
        path: 'inspection/task',
        name: 'InspectionTask',
        component: () => import('@/views/inspection/task.vue'),
        meta: { title: '巡检任务', icon: 'el-icon-finished' }
      },
      {
        path: 'service/item',
        name: 'ServiceItem',
        component: () => import('@/views/service/item.vue'),
        meta: { title: '服务项', icon: 'el-icon-s-goods' }
      },
      {
        path: 'service/package',
        name: 'ServicePackage',
        component: () => import('@/views/service/package.vue'),
        meta: { title: '服务包', icon: 'el-icon-box' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '工单类型', icon: 'el-icon-collection-tag' }
      },
      {
        path: 'resident',
        name: 'Resident',
        component: () => import('@/views/resident/index.vue'),
        meta: { title: '住户管理', icon: 'el-icon-user' }
      },
      {
        path: 'premise',
        name: 'Premise',
        component: () => import('@/views/premise/index.vue'),
        meta: { title: '房屋点位', icon: 'el-icon-office-building' }
      },
      {
        path: 'staff',
        name: 'Staff',
        component: () => import('@/views/staff/index.vue'),
        meta: { title: '员工管理', icon: 'el-icon-s-custom' }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/index.vue'),
        meta: { title: '运营报表', icon: 'el-icon-pie-chart' }
      }
    ]
  },
  {
    path: '*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { hidden: true }
  }
]

const router = new VueRouter({
  mode: 'hash',
  routes
})

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('hestia_token')
  if (to.path === '/login') {
    next()
    return
  }
  if (!token) {
    next('/login')
    return
  }
  document.title = (to.meta && to.meta.title ? to.meta.title + ' · ' : '') + '云筑 Hestia'
  next()
})

export default router
