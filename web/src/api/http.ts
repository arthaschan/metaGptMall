import axios from 'axios'

// 创建 axios 实例，统一配置 baseURL 和超时时间
const http = axios.create({
  baseURL: '/api', // 所有请求会自动添加 /api 前缀，由 Vite 代理转发到后端
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：可在此处统一添加 token 等
http.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token 并添加到请求头
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：可在此处统一处理错误
http.interceptors.response.use(
  response => response.data, // 直接返回 response.data，简化调用
  error => {
    console.error('API Error:', error)
    // 可以在此处根据状态码进行统一错误处理，例如跳转到登录页
    if (error.response?.status === 401) {
      // 未授权，清除 token 并跳转到登录页
      localStorage.removeItem('accessToken')
      window.location.href = '/login' // 需要后续实现登录页
    }
    return Promise.reject(error)
  }
)

export default http
