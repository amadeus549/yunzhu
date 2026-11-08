import request from '@/utils/request'

export const pageDevice = params => request({ url: '/admin/device/page', method: 'get', params })
export const getDevice = id => request({ url: `/admin/device/${id}`, method: 'get' })
export const saveDevice = data => request({ url: '/admin/device', method: 'post', data })
export const updateDevice = data => request({ url: '/admin/device', method: 'put', data })
export const deleteDevice = id => request({ url: `/admin/device/${id}`, method: 'delete' })
export const deviceCategories = () => request({ url: '/admin/device/categories', method: 'get' })
export const maintainDue = () => request({ url: '/admin/device/maintain-due', method: 'get' })
export const maintainDueCount = () => request({ url: '/admin/device/maintain-due/count', method: 'get' })
