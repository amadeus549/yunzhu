import request from '@/utils/request'

export const login = data => request({ url: '/admin/auth/login', method: 'post', data })
export const getInfo = () => request({ url: '/admin/auth/info', method: 'get' })
export const logout = () => request({ url: '/admin/auth/logout', method: 'post' })
export const editPassword = data => request({ url: '/admin/auth/password', method: 'put', data })
