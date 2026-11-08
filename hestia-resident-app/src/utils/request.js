import { BASE_URL, TOKEN_KEY } from '@/config'

/**
 * uni.request 的 Promise 封装
 * - 自动携带住户端 token（请求头名 authentication，与后端 resident-token-name 对应）
 * - 统一解包 Result{code,msg,data}；code!==1 抛错并 toast
 * - 401 清登录态并跳登录页
 */
const request = (options = {}) => {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        authentication: uni.getStorageSync(TOKEN_KEY) || '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 401) {
          uni.removeStorageSync(TOKEN_KEY)
          uni.removeStorageSync('hestia_resident_user')
          uni.showToast({ title: '请先登录', icon: 'none' })
          setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 600)
          return reject(new Error('unauthorized'))
        }
        const body = res.data || {}
        if (body.code === 1) {
          resolve(body.data)
        } else {
          uni.showToast({ title: body.msg || '请求失败', icon: 'none' })
          reject(new Error(body.msg || 'error'))
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常，请检查后端与 nginx 是否运行', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

export default request
