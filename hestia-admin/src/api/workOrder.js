import request from '@/utils/request'

export const pageWorkOrder = params => request({ url: '/admin/work-order/page', method: 'get', params })
export const getWorkOrder = id => request({ url: `/admin/work-order/${id}`, method: 'get' })
export const createWorkOrder = data => request({ url: '/admin/work-order', method: 'post', data })
export const acceptWorkOrder = id => request({ url: `/admin/work-order/${id}/accept`, method: 'put' })
export const dispatchWorkOrder = (id, data) =>
  request({ url: `/admin/work-order/${id}/dispatch`, method: 'put', data })
export const autoDispatch = id => request({ url: `/admin/work-order/${id}/auto-dispatch`, method: 'put' })
export const dispatchCandidates = id => request({ url: `/admin/work-order/${id}/candidates`, method: 'get' })
export const cancelWorkOrder = (id, data) =>
  request({ url: `/admin/work-order/${id}/cancel`, method: 'put', data })
