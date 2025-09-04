import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    console.log(`🚀 发起请求: ${config.method?.toUpperCase()} ${config.url}`)
    return config
  },
  error => {
    console.error('❌ 请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    const { data } = response
    console.log(`✅ 请求成功: ${response.config.url}`, data)
    
    // 如果接口返回的code不是200，显示错误信息
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    
    return data
  },
  error => {
    console.error('❌ 请求失败:', error)
    
    let message = '网络错误'
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 400:
          message = data.message || '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 503:
          message = '服务不可用'
          break
        default:
          message = data.message || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// API接口定义
export const systemAPI = {
  // 获取系统信息
  getInfo: () => api.get('/system/info'),
  
  // 系统健康检查
  getHealth: () => api.get('/system/health'),
  
  // 获取系统状态
  getStatus: () => api.get('/system/status'),
  
  // 快速测试
  quickTest: (message = '你好') => api.post('/system/test', null, { 
    params: { message } 
  })
}

export const chatAPI = {
  // 智能聊天
  sendMessage: (data, userId = 'web-user') => 
    api.post(`/chat/send/${userId}`, data),
  
  // 获取推荐策略
  getRecommendedStrategy: () => api.get('/chat/strategy/recommend'),
  
  // 聊天健康检查
  getHealth: () => api.get('/chat/health')
}

export const difyAPI = {
  // Dify聊天
  chat: (data, userId = 'web-user') => 
    api.post(`/dify/chat/${userId}`, data),
  
  // 清除会话
  clearConversation: (userId) => 
    api.delete(`/dify/conversation/${userId}`),
  
  // 获取会话ID
  getConversationId: (userId) => 
    api.get(`/dify/conversation/${userId}`),
  
  // Dify健康检查
  getHealth: () => api.get('/dify/health')
}

export const ragAPI = {
  // RAG查询
  query: (data) => api.post('/rag/query', data),
  
  // 重新加载知识库
  reload: () => api.post('/rag/reload'),
  
  // RAG健康检查
  getHealth: () => api.get('/rag/health')
}

export default api
