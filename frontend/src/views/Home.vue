<template>
  <div class="home-container full-height flex-center">
    <div class="home-content">
      <!-- Logo和标题 -->
      <div class="text-center mb-20">
        <div class="logo-icon">🤖</div>
        <h1 class="title">RAG-Try</h1>
        <h2 class="subtitle">智能客服系统</h2>
        <p class="description">
          支持 Dify API 和本地 Ollama RAG 的智能客服解决方案
        </p>
      </div>

      <!-- 功能特性 -->
      <div class="features mb-20">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">☁️</div>
              <h3>Dify API 集成</h3>
              <p>支持调用 Dify 平台的工作流进行智能对话</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">🧠</div>
              <h3>本地 RAG 问答</h3>
              <p>基于 Ollama 的向量化检索和语义理解</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">🔀</div>
              <h3>智能路由</h3>
              <p>自动选择最佳服务，支持多层降级机制</p>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 系统状态 -->
      <div class="system-status mb-20">
        <el-card>
          <template #header>
            <div class="flex-between">
              <span>系统状态</span>
              <el-button 
                size="small" 
                @click="refreshStatus"
                :loading="statusLoading"
                icon="Refresh"
              >
                刷新
              </el-button>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="status-item">
                <div class="status-label">系统总体</div>
                <div class="status-value">
                  <el-tag 
                    :type="getStatusType(systemHealth.overall)"
                    effect="dark"
                  >
                    {{ getStatusText(systemHealth.overall) }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="status-item">
                <div class="status-label">Dify API</div>
                <div class="status-value">
                  <el-tag 
                    :type="getStatusType(systemHealth.services?.dify)"
                    effect="dark"
                  >
                    {{ getStatusText(systemHealth.services?.dify) }}
                  </el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="status-item">
                <div class="status-label">本地 RAG</div>
                <div class="status-value">
                  <el-tag 
                    :type="getStatusType(systemHealth.services?.rag)"
                    effect="dark"
                  >
                    {{ getStatusText(systemHealth.services?.rag) }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
          
          <div class="mt-10" v-if="systemHealth.recommendedStrategy">
            <el-alert
              :title="`推荐策略: ${getStrategyName(systemHealth.recommendedStrategy)}`"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </el-card>
      </div>

      <!-- 操作按钮 -->
      <div class="actions text-center">
        <el-button 
          type="primary" 
          size="large"
          @click="goToChat"
          :disabled="!systemHealth.overall"
        >
          <el-icon><ChatDotRound /></el-icon>
          开始对话
        </el-button>
        
        <el-button 
          size="large"
          @click="runQuickTest"
          :loading="testLoading"
        >
          <el-icon><Operation /></el-icon>
          快速测试
        </el-button>
      </div>

      <!-- 测试结果 -->
      <div v-if="testResults" class="test-results mt-20">
        <el-card>
          <template #header>
            <span>测试结果</span>
          </template>
          <div v-for="(result, service) in testResults" :key="service" class="test-item">
            <div class="flex-between">
              <span class="test-service">{{ getServiceName(service) }}</span>
              <el-tag 
                :type="result.success ? 'success' : 'danger'"
                effect="dark"
              >
                {{ result.success ? '成功' : '失败' }}
              </el-tag>
            </div>
            <div v-if="!result.success" class="test-error">
              错误: {{ result.error }}
            </div>
            <div v-else-if="result.data?.answer" class="test-answer">
              回答: {{ result.data.answer.substring(0, 100) }}...
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Operation, Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const chatStore = useChatStore()

// 响应式数据
const statusLoading = ref(false)
const testLoading = ref(false)
const testResults = ref(null)

// 计算属性
const systemHealth = chatStore.systemHealth

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
  testResults.value = null
  
  try {
    const results = await chatStore.testAllAPIs()
    testResults.value = results
    
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

// 跳转到聊天页面
const goToChat = () => {
  router.push('/chat')
}

// 组件挂载时检查系统状态
onMounted(async () => {
  await refreshStatus()
})
</script>

<style lang="scss" scoped>
.home-container {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.home-content {
  width: 90%;
  max-width: 1000px;
  padding: 40px 20px;
}

.logo-icon {
  font-size: 120px;
  margin-bottom: 20px;
}

.title {
  font-size: 48px;
  font-weight: bold;
  color: white;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 15px;
  font-weight: 300;
}

.description {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.features {
  .feature-card {
    text-align: center;
    height: 180px;
    
    .feature-icon {
      font-size: 48px;
      margin-bottom: 15px;
    }
    
    h3 {
      color: var(--text-primary);
      margin-bottom: 10px;
      font-size: 18px;
    }
    
    p {
      color: var(--text-secondary);
      font-size: 14px;
      line-height: 1.5;
    }
  }
}

.system-status {
  .status-item {
    text-align: center;
    
    .status-label {
      color: var(--text-secondary);
      font-size: 14px;
      margin-bottom: 8px;
    }
    
    .status-value {
      font-weight: bold;
    }
  }
}

.actions {
  .el-button {
    margin: 0 10px;
    padding: 15px 30px;
    font-size: 16px;
  }
}

.test-results {
  .test-item {
    padding: 10px 0;
    border-bottom: 1px solid var(--border-light);
    
    &:last-child {
      border-bottom: none;
    }
    
    .test-service {
      font-weight: bold;
      color: var(--text-primary);
    }
    
    .test-error {
      color: var(--danger-color);
      font-size: 14px;
      margin-top: 5px;
    }
    
    .test-answer {
      color: var(--text-secondary);
      font-size: 14px;
      margin-top: 5px;
    }
  }
}

@media (max-width: 768px) {
  .home-content {
    width: 95%;
    padding: 20px 10px;
  }
  
  .title {
    font-size: 36px;
  }
  
  .subtitle {
    font-size: 20px;
  }
  
  .logo-icon {
    font-size: 80px;
  }
  
  .actions .el-button {
    margin: 5px;
    padding: 12px 20px;
    font-size: 14px;
  }
}
</style>
