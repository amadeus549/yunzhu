import request from '@/utils/request'

export const pageServiceItem = params => request({ url: '/admin/service-item/page', method: 'get', params })
export const listServiceItem = categoryId =>
  request({ url: '/admin/service-item/list', method: 'get', params: { categoryId } })
export const getServiceItem = id => request({ url: `/admin/service-item/${id}`, method: 'get' })
export const saveServiceItem = data => request({ url: '/admin/service-item', method: 'post', data })
export const updateServiceItem = data => request({ url: '/admin/service-item', method: 'put', data })
export const deleteServiceItem = id => request({ url: `/admin/service-item/${id}`, method: 'delete' })
export const updateServiceItemStatus = (id, status) =>
  request({ url: `/admin/service-item/${id}/status`, method: 'put', params: { status } })

export const pageServicePackage = params => request({ url: '/admin/service-package/page', method: 'get', params })
export const getServicePackage = id => request({ url: `/admin/service-package/${id}`, method: 'get' })
export const saveServicePackage = data => request({ url: '/admin/service-package', method: 'post', data })
export const updateServicePackage = data => request({ url: '/admin/service-package', method: 'put', data })
export const deleteServicePackage = id => request({ url: `/admin/service-package/${id}`, method: 'delete' })
export const updateServicePackageStatus = (id, status) =>
  request({ url: `/admin/service-package/${id}/status`, method: 'put', params: { status } })
