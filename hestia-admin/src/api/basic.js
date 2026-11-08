import request from '@/utils/request'

// 工单类型
export const listCategory = () => request({ url: '/admin/category/list', method: 'get' })
export const pageCategory = params => request({ url: '/admin/category/page', method: 'get', params })
export const saveCategory = data => request({ url: '/admin/category', method: 'post', data })
export const updateCategory = data => request({ url: '/admin/category', method: 'put', data })
export const updateCategoryStatus = (id, status) =>
  request({ url: `/admin/category/${id}/status`, method: 'put', params: { status } })

// 住户
export const pageResident = params => request({ url: '/admin/resident/page', method: 'get', params })
export const listResident = () => request({ url: '/admin/resident/list', method: 'get' })
export const saveResident = data => request({ url: '/admin/resident', method: 'post', data })
export const updateResident = data => request({ url: '/admin/resident', method: 'put', data })

// 房屋点位
export const listPremise = () => request({ url: '/admin/premise/list', method: 'get' })
export const savePremise = data => request({ url: '/admin/premise', method: 'post', data })
export const updatePremise = data => request({ url: '/admin/premise', method: 'put', data })
export const deletePremise = id => request({ url: `/admin/premise/${id}`, method: 'delete' })

// 员工
export const pageStaff = params => request({ url: '/admin/staff/page', method: 'get', params })
export const listEngineers = params => request({ url: '/admin/staff/engineers', method: 'get', params })
export const saveStaff = data => request({ url: '/admin/staff', method: 'post', data })
export const updateStaff = data => request({ url: '/admin/staff', method: 'put', data })
export const updateStaffStatus = (id, status) =>
  request({ url: `/admin/staff/${id}/status`, method: 'put', params: { status } })

// 通知
export const listNotice = () => request({ url: '/admin/notice/list', method: 'get' })
export const unreadCount = () => request({ url: '/admin/notice/unread-count', method: 'get' })
export const readAllNotice = () => request({ url: '/admin/notice/read-all', method: 'put' })
