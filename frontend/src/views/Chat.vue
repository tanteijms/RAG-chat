<template>
  <div class="chat-container full-height">
    <el-container class="full-height">
      <!-- 侧边栏 -->
      <el-aside width="320px" class="chat-sidebar">
        <!-- Logo -->
        <div class="sidebar-header">
          <div class="logo" @click="goHome">
            <span class="logo-icon">🤖</span>
            <span class="logo-text">RAG-Try</span>
          </div>
        </div>

        <!-- 系统状态 -->
        <div class="status-section">
          <div class="section-title">
            <span>系统状态</span>
            <el-button 
              size="small" 
              text 
              @click="refreshStatus"
              :loading="statusLoading"
              icon="Refresh"
            />
          </div>
          
          <div class="status-grid">
            <div class="status-item">
              <span class="status-label">系统总体</span>
              <el-tag 
                :type="getStatusType(systemHealth.overall)"
                size="small"
                effect="dark"
              >
                {{ getStatusText(systemHealth.overall) }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">Dify API</span>
              <el-tag 
                :type="getStatusType(systemHealth.services?.dify)"
                size="small"
                effect="dark"
              >
                {{ getStatusText(systemHealth.services?.dify) }}
              </el-tag>
            </div>
            <div class="status-item">
              <span class="status-label">本地 RAG</span>
              <el-tag 
                :type="getStatusType(systemHealth.services?.rag)"
                size="small"
                effect="dark"
              >
                {{ getStatusText(systemHealth.services?.rag) }}
              </el-tag>
            </div>
          </div>
          
          <div v-if="systemHealth.recommendedStrategy" class="recommended-strategy">
            <el-text size="small" type="info">
              推荐策略: {{ getStrategyName(systemHealth.recommendedStrategy) }}
            </el-text>
          </div>
        </div>

        <!-- 模式选择 -->
        <div class="mode-section">
          <div class="section-title">对话模式</div>
          <div class="mode-options">
            <div 
              v-for="mode in modes" 
              :key="mode.key"
              class="mode-option"
              :class="{ active: currentMode === mode.key }"
              @click="setMode(mode.key)"
            >
              <div class="mode-header">
                <span class="mode-icon">{{ mode.icon }}</span>
                <span class="mode-title">{{ mode.title }}</span>
              </div>
              <div class="mode-desc">{{ mode.description }}</div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="quick-actions">
          <div class="section-title">快捷操作</div>
          <el-button 
            size="small" 
            @click="clearChat"
            :disabled="!hasMessages"
            icon="Delete"
          >
            清除对话
          </el-button>
          <el-button 
            size="small" 
            @click="runQuickTest"
            :loading="testLoading"
            icon="Operation"
          >
            API测试
          </el-button>
        </div>
      </el-aside>

      <!-- 主聊天区域 -->
      <el-main class="chat-main">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-title-section">
            <h2 class="chat-title">{{ modeConfig.title }}</h2>
            <p class="chat-subtitle">{{ modeConfig.subtitle }}</p>
          </div>
          <div class="chat-actions">
            <el-button 
              size="small" 
              @click="goHome"
              icon="House"
            >
              返回首页
            </el-button>
          </div>
        </div>

        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="!hasMessages" class="welcome-section">
            <div class="welcome-icon">🤖</div>
            <h3>欢迎使用 RAG-Try 智能客服</h3>
            <p>请选择对话模式并开始您的提问</p>
            
            <!-- 快捷问题 -->
            <div class="quick-questions">
              <el-button 
                v-for="question in quickQuestions"
                :key="question"
                size="small"
                plain
                @click="sendQuickMessage(question)"
              >
                {{ question }}
              </el-button>
            </div>
          </div>

          <!-- 消息列表 -->
          <div v-else class="messages-list">
            <transition-group name="slide-up" tag="div">
              <div 
                v-for="message in messages"
                :key="message.id"
                class="message-wrapper"
                :class="message.type"
              >
                <div class="message-content">
                  <div class="message-text">{{ message.content }}</div>
                  <div class="message-info">
                    <span class="message-time">
                      {{ formatTime(message.timestamp) }}
                    </span>
                    <span v-if="message.source && message.type === 'assistant'" class="message-source">
                      来源: {{ message.source }}
                    </span>
                    <span v-if="message.responseTime && message.type === 'assistant'" class="message-time-cost">
                      耗时: {{ message.responseTime }}ms
                    </span>
                  </div>
                </div>
              </div>
            </transition-group>
          </div>

          <!-- 加载指示器 -->
          <div v-if="isLoading" class="loading-message">
            <div class="message-wrapper assistant">
              <div class="message-content">
                <div class="typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-section">
          <div class="input-container">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="2"
              placeholder="请输入您的问题..."
              @keydown.enter="handleEnterKey"
              :disabled="isLoading"
              resize="none"
              class="message-input"
            />
            <el-button 
              type="primary"
              @click="sendMessage"
              :loading="isLoading"
              :disabled="!inputMessage.trim()"
              class="send-button"
            >
              <el-icon v-if="!isLoading"><Send /></el-icon>
              {{ isLoading ? '发送中...' : '发送' }}
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>

    <!-- 测试结果弹窗 -->
    <el-dialog
      v-model="showTestDialog"
      title="API测试结果"
      width="600px"
    >
      <div v-if="testResults">
        <div v-for="(result, service) in testResults" :key="service" class="test-result-item">
          <div class="test-header">
            <span class="test-service">{{ getServiceName(service) }}</span>
            <el-tag 
              :type="result.success ? 'success' : 'danger'"
              effect="dark"
            >
              {{ result.success ? '成功' : '失败' }}
            </el-tag>
          </div>
          <div v-if="!result.success" class="test-error">
            <el-text type="danger">错误: {{ result.error }}</el-text>
          </div>
          <div v-else-if="result.data?.answer" class="test-answer">
            <el-text type="info">回答: {{ result.data.answer.substring(0, 200) }}...</el-text>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { ElMessage } from 'element-plus'
import { House, Refresh, Delete, Operation, Send } from '@element-plus/icons-vue'

const router = useRouter()
const chatStore = useChatStore()

// 响应式数据
const inputMessage = ref('')
const statusLoading = ref(false)
const testLoading = ref(false)
const testResults = ref(null)
const showTestDialog = ref(false)
const messagesContainer = ref(null)

// 从store获取的计算属性
const messages = computed(() => chatStore.messages)
const currentMode = computed(() => chatStore.currentMode)
const isLoading = computed(() => chatStore.isLoading)
const hasMessages = computed(() => chatStore.hasMessages)
const modeConfig = computed(() => chatStore.modeConfig)
const systemHealth = computed(() => chatStore.systemHealth)

// 模式配置
const modes = [
  {
    key: 'smart',
    icon: '🧠',
    title: '智能路由',
    description: '自动选择最佳服务'
  },
  {
    key: 'dify',
    icon: '☁️',
    title: 'Dify API',
    description: '使用云端工作流'
  },
  {
    key: 'rag',
    icon: '📚',
    title: '本地 RAG',
    description: '本地知识库问答'
  }
]

// 快捷问题
const quickQuestions = [
  '什么是RAG？',
  '如何使用这个系统？',
  '系统有哪些功能？',
  '如何配置Ollama？',
  '故障排除指南'
]

// 设置模式
const setMode = (mode) => {
  chatStore.setMode(mode)
}

// 获取状态类型
const getStatusType = (status) => {
  if (status === true) return 'success'
  if (status === false) return 'danger'
  return 'warning'
}

// 获取状态文本
const getStatusText = (status) => {
  if (status === true) return '正常'
  if (status === false) return '离线'
  return '未知'
}

// 获取策略名称
const getStrategyName = (strategy) => {
  const strategyMap = {
    'rag-first': 'RAG 优先',
    'dify-first': 'Dify 优先',
    'parallel': '并行调用'
  }
  return strategyMap[strategy] || strategy
}

// 获取服务名称
const getServiceName = (service) => {
  const serviceMap = {
    'smart': '智能路由',
    'dify': 'Dify API',
    'rag': '本地 RAG'
  }
  return serviceMap[service] || service
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 处理回车键
const handleEnterKey = (event) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) return

  inputMessage.value = ''
  
  try {
    await chatStore.sendMessage(content)
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送消息失败')
  }
}

// 发送快捷消息
const sendQuickMessage = async (content) => {
  try {
    await chatStore.sendMessage(content)
    scrollToBottom()
  } catch (error) {
    ElMessage.error('发送消息失败')
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 清除聊天记录
const clearChat = () => {
  chatStore.clearMessages()
  ElMessage.success('聊天记录已清除')
}

// 刷新状态
const refreshStatus = async () => {
  statusLoading.value = true
  try {
    await chatStore.checkSystemHealth()
    ElMessage.success('状态刷新成功')
  } catch (error) {
    ElMessage.error('状态刷新失败')
  } finally {
    statusLoading.value = false
  }
}

// 运行快速测试
const runQuickTest = async () => {
  testLoading.value = true
  
  try {
    const results = await chatStore.testAllAPIs()
    testResults.value = results
    showTestDialog.value = true
    
    const successCount = Object.values(results).filter(r => r.success).length
    const totalCount = Object.keys(results).length
    
    if (successCount === totalCount) {
      ElMessage.success('所有API测试通过')
    } else if (successCount > 0) {
      ElMessage.warning(`${successCount}/${totalCount} 个API测试通过`)
    } else {
      ElMessage.error('所有API测试失败')
    }
  } catch (error) {
    ElMessage.error('测试执行失败')
  } finally {
    testLoading.value = false
  }
}

// 返回首页
const goHome = () => {
  router.push('/')
}

// 定时检查状态
let statusInterval = null

onMounted(async () => {
  await refreshStatus()
  // 每30秒检查一次状态
  statusInterval = setInterval(refreshStatus, 30000)
})

onUnmounted(() => {
  if (statusInterval) {
    clearInterval(statusInterval)
  }
})
</script>

<style lang="scss" scoped>
.chat-container {
  background: #f5f7fa;
}

.chat-sidebar {
  background: white;
  border-right: 1px solid var(--border-light);
  padding: 20px;
  overflow-y: auto;
}

.sidebar-header {
  margin-bottom: 30px;
  
  .logo {
    display: flex;
    align-items: center;
    cursor: pointer;
    padding: 10px;
    border-radius: 8px;
    transition: background 0.3s;
    
    &:hover {
      background: var(--background-color);
    }
    
    .logo-icon {
      font-size: 32px;
      margin-right: 10px;
    }
    
    .logo-text {
      font-size: 20px;
      font-weight: bold;
      color: var(--primary-color);
    }
  }
}

.status-section, .mode-section, .quick-actions {
  margin-bottom: 25px;
  
  .section-title {
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 15px;
    font-size: 14px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.status-grid {
  .status-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .status-label {
      font-size: 13px;
      color: var(--text-secondary);
    }
  }
}

.recommended-strategy {
  margin-top: 10px;
  padding: 8px;
  background: var(--background-color);
  border-radius: 6px;
  text-align: center;
}

.mode-options {
  .mode-option {
    padding: 12px;
    border: 2px solid var(--border-light);
    border-radius: 8px;
    margin-bottom: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      border-color: var(--primary-color);
    }
    
    &.active {
      border-color: var(--primary-color);
      background: rgba(102, 126, 234, 0.05);
    }
    
    .mode-header {
      display: flex;
      align-items: center;
      margin-bottom: 4px;
      
      .mode-icon {
        font-size: 18px;
        margin-right: 8px;
      }
      
      .mode-title {
        font-weight: 600;
        color: var(--text-primary);
      }
    }
    
    .mode-desc {
      font-size: 12px;
      color: var(--text-secondary);
    }
  }
}

.quick-actions {
  .el-button {
    display: block;
    width: 100%;
    margin-bottom: 8px;
  }
}

.chat-main {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 0;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-bottom: 1px solid var(--border-light);
  
  .chat-title-section {
    .chat-title {
      margin: 0;
      color: var(--text-primary);
      font-size: 20px;
    }
    
    .chat-subtitle {
      margin: 5px 0 0 0;
      color: var(--text-secondary);
      font-size: 14px;
    }
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-section {
  text-align: center;
  padding: 60px 20px;
  
  .welcome-icon {
    font-size: 80px;
    margin-bottom: 20px;
  }
  
  h3 {
    color: var(--text-primary);
    margin-bottom: 10px;
  }
  
  p {
    color: var(--text-secondary);
    margin-bottom: 30px;
  }
  
  .quick-questions {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
    
    .el-button {
      margin: 0;
    }
  }
}

.messages-list {
  .message-wrapper {
    margin-bottom: 20px;
    display: flex;
    
    &.user {
      justify-content: flex-end;
      
      .message-content {
        background: var(--primary-color);
        color: white;
        border-radius: 18px 18px 4px 18px;
      }
    }
    
    &.assistant {
      justify-content: flex-start;
      
      .message-content {
        background: white;
        color: var(--text-primary);
        border-radius: 18px 18px 18px 4px;
        border: 1px solid var(--border-light);
      }
    }
    
    .message-content {
      max-width: 70%;
      padding: 12px 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      
      .message-text {
        line-height: 1.5;
        word-wrap: break-word;
      }
      
      .message-info {
        font-size: 11px;
        margin-top: 6px;
        opacity: 0.7;
        
        span {
          margin-right: 8px;
        }
      }
    }
  }
}

.loading-message {
  .message-wrapper {
    justify-content: flex-start;
    
    .message-content {
      background: white;
      border: 1px solid var(--border-light);
      border-radius: 18px 18px 18px 4px;
      padding: 16px;
    }
  }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  
  span {
    width: 6px;
    height: 6px;
    background: var(--text-secondary);
    border-radius: 50%;
    animation: typing 1.4s infinite;
    
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-10px); }
}

.input-section {
  padding: 20px;
  background: white;
  border-top: 1px solid var(--border-light);
  
  .input-container {
    display: flex;
    gap: 12px;
    align-items: flex-end;
    
    .message-input {
      flex: 1;
    }
    
    .send-button {
      height: 64px;
      padding: 0 20px;
    }
  }
}

.test-result-item {
  padding: 15px 0;
  border-bottom: 1px solid var(--border-light);
  
  &:last-child {
    border-bottom: none;
  }
  
  .test-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .test-service {
      font-weight: bold;
    }
  }
  
  .test-error, .test-answer {
    margin-top: 8px;
  }
}

@media (max-width: 768px) {
  .el-aside {
    width: 100% !important;
    height: 200px;
    overflow-y: auto;
  }
  
  .chat-main {
    height: calc(100vh - 200px);
  }
  
  .messages-list .message-wrapper .message-content {
    max-width: 85%;
  }
  
  .input-container {
    flex-direction: column;
    gap: 10px;
    
    .send-button {
      width: 100%;
      height: 44px;
    }
  }
}
</style>
