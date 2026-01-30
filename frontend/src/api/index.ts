import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResponse } from './types'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
  headers: {
    'Content-Type': 'multipart/form-data'
  }
})

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 如果响应类型是 blob，直接返回（用于文件下载）
    if (response.config.responseType === 'blob') {
      return response
    }
    const data = response.data as ApiResponse
    if (data.code === '200') {
      return response
    }
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    return Promise.reject(new Error(message))
  }
)

export default request
