import request from '@/utils/request'

export const pagePlan = params => request({ url: '/admin/inspection/plan/page', method: 'get', params })
export const savePlan = data => request({ url: '/admin/inspection/plan', method: 'post', data })
export const updatePlan = data => request({ url: '/admin/inspection/plan', method: 'put', data })
export const updatePlanStatus = (id, status) =>
  request({ url: `/admin/inspection/plan/${id}/status`, method: 'put', params: { status } })
export const generateTasks = id => request({ url: `/admin/inspection/plan/${id}/generate`, method: 'post' })

export const pageTask = params => request({ url: '/admin/inspection/task/page', method: 'get', params })
export const myTasks = params => request({ url: '/admin/inspection/task/mine', method: 'get', params })
export const executeTask = data => request({ url: '/admin/inspection/task/execute', method: 'post', data })
