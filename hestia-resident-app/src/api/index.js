import { get, post, put, del } from '@/utils/request'

/** 登录（手机号 + 验证码，演示验证码 123456） */
export const login = (phone, code) => post('/auth/login', { phone, code })

/** 当前住户信息 */
export const getInfo = () => get('/auth/info')

/** 报修可选的工单类型（启用中） */
export const categoryList = () => get('/common/category-list')

/** 按工单类型列出上架服务项 */
export const serviceList = (categoryId) => get('/common/service-list', { categoryId })

/** 我的房屋 */
export const myPremise = () => get('/premise/list')

/** 提交报修 */
export const submitOrder = (data) => post('/work-order', data)

/** 我的工单分页 */
export const myOrders = (page, pageSize, status) =>
  get('/work-order/page', { page, pageSize, status })

/** 工单详情 */
export const orderDetail = (id) => get(`/work-order/${id}`)

/** 验收通过并评价 */
export const confirmOrder = (id, rating, comment) =>
  put(`/work-order/${id}/confirm`, { rating, comment })

/** 验收驳回 */
export const rejectOrder = (id, reason) => put(`/work-order/${id}/reject`, { reason })

/** 取消工单 */
export const cancelOrder = (id, reason) => put(`/work-order/${id}/cancel`, { reason })

/** 催单 */
export const urgeOrder = (id) => post(`/work-order/${id}/urge`)

/** 服务清单 */
export const cartList = () => get('/cart/list')
export const cartAdd = (data) => post('/cart', data)
export const cartUpdate = (id, quantity) => put(`/cart/${id}`, { quantity })
export const cartRemove = (id) => del(`/cart/${id}`)
export const cartClear = () => del('/cart/clear')

/** 消息通知 */
export const noticeList = () => get('/notice/list')
export const noticeUnread = () => get('/notice/unread-count')
export const noticeReadAll = () => put('/notice/read-all')
