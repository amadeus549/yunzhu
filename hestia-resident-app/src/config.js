/**
 * 全局配置：住户端接口地址
 *
 * H5 版部署在 nginx 同源下（/m/ 目录），走相对路径即可命中
 * nginx 的 /resident/ 反向代理，无跨域问题；
 * 小程序在微信开发者工具中调试时使用完整地址（需勾选"不校验合法域名"）。
 */
// #ifdef H5
export const BASE_URL = '/resident'
// #endif
// #ifndef H5
export const BASE_URL = 'http://localhost/resident'
// #endif

export const TOKEN_KEY = 'hestia_resident_token'
export const USER_KEY = 'hestia_resident_user'

/** 演示环境固定验证码提示 */
export const DEMO_CODE = '123456'
