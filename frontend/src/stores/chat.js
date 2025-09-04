import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { systemAPI, chatAPI, difyAPI, ragAPI } from '@/api'

export const useChatStore = defineStore('chat', () => {
  // 状态
  const messages = ref([])
  const currentMode = ref('smart')
  const isLoading = ref(false)
  const userId = ref(`web-user-${Date.now()}`)
  
  // 系统状态
  const systemHealth = ref({
    overall: false,
    services: {
      dify: false,
      rag: false
    },
    recommendedStrategy: ''
  })

  // 计算属性
  const hasMessages = computed(() => messages.value.length > 0)
  
  const modeConfig = computed(() => {
    const configs = {
      smart: {
        title: '🧠 智能路由',
        subtitle: '系统将自动选择最佳服务为您回答',
        description: '根据服务状态智能选择最优方案'
      },
      dify: {
        title: '☁️ Dify API',
        subtitle: '直接使用 Dify 云端工作流进行对话',
        description: '基于云端工作流的智能对话'
      },
      rag: {
        title: '📚 本地 RAG',
        subtitle: '基于本地知识库的向量检索问答',
        description: '本地知识库向量检索和生成'
      }
    }
    return configs[currentMode.value] || configs.smart
  })

  // 动作
  const setMode = (mode) => {
    currentMode.value = mode
    console.log(`📝 切换到模式: ${mode}`)
  }

  const addMessage = (type, content, source = '', responseTime = 0) => {
    const message = {
      id: Date.now() + Math.random(),
      type,
      content,
      source,
      responseTime,
      timestamp: new Date()
    }
    messages.value.push(message)
    return message
  }

  const clearMessages = () => {
    messages.value = []
  }

  // 检查系统健康状态
  const checkSystemHealth = async () => {
    try {
      const result = await systemAPI.getHealth()
      systemHealth.value = result.data || {
        overall: false,
        services: { dify: false, rag: false },
        recommendedStrategy: ''
      }
      return systemHealth.value
    } catch (error) {
      console.error('❌ 健康检查失败:', error)
      systemHealth.value = {
        overall: false,
        services: { dify: false, rag: false },
        recommendedStrategy: ''
      }
      return systemHealth.value
    }
  }

  // 发送消息
  const sendMessage = async (content) => {
    if (!content.trim() || isLoading.value) return

    // 添加用户消息
    addMessage('user', content)
    
    // 设置加载状态
    isLoading.value = true

    try {
      let response
      
      // 根据模式调用不同API
      switch (currentMode.value) {
        case 'smart':
          response = await chatAPI.sendMessage(
            { message: content }, 
            userId.value
          )
          break
        case 'dify':
          response = await difyAPI.chat(
            { message: content }, 
            userId.value
          )
          break
        case 'rag':
          response = await ragAPI.query({ question: content })
          break
        default:
          throw new Error('未知的对话模式')
      }

      // 添加AI回复
      if (response.data && response.data.answer) {
        addMessage(
          'assistant', 
          response.data.answer,
          response.data.source || currentMode.value.toUpperCase(),
          response.data.responseTime || 0
        )
      } else {
        addMessage('assistant', '抱歉，我无法回答您的问题，请稍后再试。', 'System')
      }

    } catch (error) {
      console.error('❌ 发送消息失败:', error)
      addMessage('assistant', `发生错误：${error.message}`, 'Error')
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // 快速发送预设消息
  const sendQuickMessage = (content) => {
    return sendMessage(content)
  }

  // 测试所有API
  const testAllAPIs = async () => {
    console.log('🧪 开始API测试...')
    const testMessage = '什么是RAG？'
    const results = {}

    // 测试智能路由
    try {
      console.log('📡 测试智能路由API...')
      const smartResult = await chatAPI.sendMessage(
        { message: testMessage }, 
        userId.value
      )
      results.smart = { success: true, data: smartResult.data }
      console.log('✅ 智能路由:', smartResult.data)
    } catch (error) {
      results.smart = { success: false, error: error.message }
      console.log('❌ 智能路由失败:', error.message)
    }

    // 测试Dify API
    try {
      console.log('☁️ 测试Dify API...')
      const difyResult = await difyAPI.chat(
        { message: testMessage }, 
        userId.value
      )
      results.dify = { success: true, data: difyResult.data }
      console.log('✅ Dify API:', difyResult.data)
    } catch (error) {
      results.dify = { success: false, error: error.message }
      console.log('❌ Dify API失败:', error.message)
    }

    // 测试RAG API
    try {
      console.log('📚 测试RAG API...')
      const ragResult = await ragAPI.query({ question: testMessage })
      results.rag = { success: true, data: ragResult.data }
      console.log('✅ RAG API:', ragResult.data)
    } catch (error) {
      results.rag = { success: false, error: error.message }
      console.log('❌ RAG API失败:', error.message)
    }

    console.log('🏁 API测试完成', results)
    return results
  }

  return {
    // 状态
    messages,
    currentMode,
    isLoading,
    userId,
    systemHealth,
    
    // 计算属性
    hasMessages,
    modeConfig,
    
    // 动作
    setMode,
    addMessage,
    clearMessages,
    checkSystemHealth,
    sendMessage,
    sendQuickMessage,
    testAllAPIs
  }
})
