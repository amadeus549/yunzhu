import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import store from '@/store'
import router from '@/router'

const service = axios.create({
  timeout: 20000
})

// 请求拦截：注入管理端 token
service.interceptors.request.use(
  config => {
    // user 模块是 namespaced: true，getter 全名为 'user/token'。
    // 早期写成 store.getters.token 会取到 undefined，导致所有请求不带 token、后端返回 401。
    // 再用 localStorage 兜底，避免 store 尚未初始化或页面刷新瞬间取不到值。
    const token = store.getters['user/token'] || localStorage.getItem('hestia_token')
    if (token) {
      config.headers['token'] = token
    }
    return config
  },
  error => Promise.reject(error)
)

// 并发请求同时 401 时，只弹一次重新登录确认框
let reloginPromptShown = false

// 响应拦截：统一处理 code / 401
service.interceptors.response.use(
  response => {
    // 二进制流（报表导出）直接返回，不做 code 判断
    if (response.config && response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code === 1) {
      return res
    }
    Message.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || 'Error'))
  },
  error => {
    if (error.response && error.response.status === 401) {
      if (!reloginPromptShown) {
        reloginPromptShown = true
        MessageBox.confirm('登录状态已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          store.dispatch('user/logout')
          router.replace('/login')
          reloginPromptShown = false
        }).catch(() => {
          reloginPromptShown = false
        })
      }
    } else {
      Message.error(error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default service
