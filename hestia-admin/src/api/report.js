import request from '@/utils/request'

export const workspaceOverview = () => request({ url: '/admin/workspace/overview', method: 'get' })
export const statusDistribution = () => request({ url: '/admin/workspace/status-distribution', method: 'get' })
export const workOrderTrend = days =>
  request({ url: '/admin/workspace/trend', method: 'get', params: { days } })

export const slaReport = params => request({ url: '/admin/report/sla', method: 'get', params })
export const reportTrend = params => request({ url: '/admin/report/trend', method: 'get', params })
export const categoryReport = params => request({ url: '/admin/report/category', method: 'get', params })
export const staffReport = params => request({ url: '/admin/report/staff', method: 'get', params })
export const exportReport = params =>
  request({ url: '/admin/report/export', method: 'get', params, responseType: 'blob' })
