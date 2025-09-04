# RAG-Chat 智能客服系统

一个完整的全栈智能客服解决方案，支持 **Dify API** 和**本地 Ollama RAG** 的智能对话系统，具备智能路由和降级机制。

## 🌟 项目特色

- **🔄 全栈架构**：Spring Boot 后端 + Vue 3 前端，开箱即用
- **🤖 Dify API 集成**：支持调用 Dify 平台的工作流进行智能对话
- **🧠 本地 RAG 问答**：基于 Ollama 的向量化检索和语义理解
- **🔀 智能路由**：自动选择最佳服务（RAG优先/Dify优先/并行调用）
- **🛡️ 降级机制**：确保服务可用性，支持多层降级
- **📚 知识库管理**：支持 Markdown 格式的知识库动态加载
- **💬 实时对话界面**：现代化的聊天UI，支持打字机效果
- **🔧 健康监控**：实时监控各服务状态
- **📱 响应式设计**：支持桌面和移动端访问

## 🏗️ 技术架构

### 前端技术栈
- **Vue 3**: 现代化响应式框架
- **Vite**: 高性能构建工具
- **Element Plus**: 企业级UI组件库
- **Pinia**: 状态管理
- **Vue Router**: 路由管理
- **Axios**: HTTP客户端
- **SCSS**: CSS预处理器

### 后端技术栈
- **Spring Boot 2.7**: 微服务框架
- **Java 17**: JDK版本
- **LangChain4j**: AI应用开发框架
- **Ollama**: 本地大模型推理
- **WebFlux**: 响应式编程
- **Maven**: 项目管理

### 系统架构图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Vue 3 前端    │    │   Spring Boot   │    │   Dify API      │
│                 │───▶│                 │───▶│                 │
│ Element Plus UI │    │  智能路由服务   │    │ 工作流对话      │
└─────────────────┘    │                 │    └─────────────────┘
                       │                 │    
                       │                 │    ┌─────────────────┐
                       │                 │───▶│ 本地 Ollama RAG │
                       └─────────────────┘    │                 │
                                              │ 向量检索+生成    │
                                              └─────────────────┘
```

### 项目结构

```
RAG-chat/
├── frontend/                 # Vue 3 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── stores/          # Pinia状态管理
│   │   ├── router/          # 路由配置
│   │   ├── api/             # API接口
│   │   └── style/           # 样式文件
│   ├── package.json         # 前端依赖
│   └── vite.config.js       # Vite配置
├── src/main/java/           # Java后端代码
│   └── com/echo/ragtry/
│       ├── controller/      # REST控制器
│       ├── service/         # 业务服务
│       ├── config/          # 配置类
│       ├── entity/          # 实体类
│       └── vo/              # 数据传输对象
├── src/main/resources/
│   ├── application.yml      # 应用配置
│   └── knowledge/           # 知识库文件
├── pom.xml                  # Maven配置
└── README.md                # 项目文档
```

## 🛠️ 环境要求

### 后端环境
- **Java**: 17+
- **Maven**: 3.6+
- **Ollama**: 本地部署（可选）
- **Dify**: API 访问权限（可选）

### 前端环境
- **Node.js**: 16+
- **npm**: 8+ 或 **yarn**: 1.22+

## 📦 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/your-username/RAG-chat.git
cd RAG-chat
```

### 2. 安装后端依赖
```bash
# 在项目根目录
mvn clean install
```

### 3. 安装前端依赖
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
# 或使用 yarn
yarn install
```

### 4. 配置应用
编辑 `src/main/resources/application.yml`：

```yaml
# Dify API配置
dify:
  api:
    base-url: http://your-dify-url/v1
    api-key: your-dify-api-key
    debug: true

# RAG配置
rag:
  ollama:
    base-url: http://localhost:11434
    embedding-model: nomic-embed-text
    chat-model: qwen2:7b
  search:
    similarity-threshold: 0.7
    max-results: 5

# 智能路由配置
smart-routing:
  enabled: true
  strategy: rag-first  # rag-first, dify-first, parallel
  fallback-enabled: true
```

### 5. 准备 Ollama（可选）
```bash
# 安装 Ollama
curl -fsSL https://ollama.com/install.sh | sh

# 启动服务
ollama serve

# 下载模型
ollama pull nomic-embed-text  # embedding 模型
ollama pull qwen2:7b         # 对话模型
```

### 6. 启动应用

#### 启动后端服务
```bash
# 在项目根目录
mvn spring-boot:run
```

#### 启动前端服务
```bash
# 在 frontend 目录
npm run dev
# 或使用 yarn
yarn dev
```

### 7. 访问应用
- 🌐 **前端界面**: http://localhost:5173
- 🔧 **后端API**: http://localhost:8080

### 8. 测试服务
```bash
# 系统健康检查
curl http://localhost:8080/api/system/health

# 智能聊天
curl -X POST http://localhost:8080/api/chat/send \
  -H "Content-Type: application/json" \
  -d '{"message": "什么是RAG？"}'

# 快速测试
curl -X POST "http://localhost:8080/api/system/test?message=你好"
```

## 📚 API 接口

### 智能聊天接口
- `POST /api/chat/send` - 智能路由聊天
- `POST /api/chat/send/{userId}` - 带用户ID的聊天

### Dify 接口
- `POST /api/dify/chat` - 直接调用 Dify
- `DELETE /api/dify/conversation/{userId}` - 清除会话
- `GET /api/dify/health` - Dify 健康检查

### RAG 接口
- `POST /api/rag/query` - RAG 问答
- `POST /api/rag/reload` - 重新加载知识库
- `GET /api/rag/health` - RAG 健康检查

### 系统接口
- `GET /api/system/info` - 系统信息
- `GET /api/system/health` - 综合健康检查
- `GET /api/system/status` - 系统状态
- `POST /api/system/test` - 快速测试

## 🔧 配置说明

### 智能路由策略

1. **rag-first**: 优先使用 RAG，失败时使用 Dify
2. **dify-first**: 优先使用 Dify，失败时使用 RAG  
3. **parallel**: 并行调用，返回最快的结果

### 知识库配置

知识库文件位于 `src/main/resources/knowledge/bot.md`，格式如下：

```markdown
## 分类名称

### Q: 问题内容
**A:** 答案内容

### Q: 另一个问题
**A:** 另一个答案
```

### 降级机制

1. **增强 RAG** → 向量检索 + 语义生成
2. **关键词匹配** → 简单文本匹配  
3. **Dify API** → 外部工作流调用
4. **默认回复** → 友好错误提示

## 🚨 故障排除

### Ollama 连接失败
```bash
# 检查服务状态
ollama list

# 重启服务
ollama serve

# 检查端口
netstat -tlnp | grep 11434
```

### Dify API 调用失败
1. 检查 API Key 是否正确
2. 确认服务地址可访问
3. 查看日志获取详细错误

### 知识库加载失败
1. 检查 MD 文件格式
2. 确认文件编码为 UTF-8
3. 查看启动日志

## 📊 监控与日志

### 日志配置
```yaml
logging:
  level:
    com.echo.ragtry: DEBUG
    dev.langchain4j: DEBUG
```

### 健康检查
```bash
# 检查所有服务
curl http://localhost:8080/api/system/health

# 检查单个服务
curl http://localhost:8080/api/dify/health
curl http://localhost:8080/api/rag/health
```

## 🔧 开发指南

### 开发环境搭建

1. **后端开发**
```bash
# 使用IDE（推荐IntelliJ IDEA）打开项目
# 配置JDK 17
# 导入Maven项目
# 启动Spring Boot应用
```

2. **前端开发**
```bash
cd frontend
npm run dev
# 支持热重载，修改即生效
```

### 代码规范

- **Java**: 遵循阿里巴巴Java开发手册
- **Vue**: 使用Vue 3 Composition API
- **样式**: 使用SCSS预处理器
- **提交**: 使用Conventional Commits规范

### 调试技巧

1. **后端调试**
   - 使用IDE断点调试
   - 查看Spring Boot Actuator端点
   - 检查日志文件

2. **前端调试**
   - 使用Vue DevTools
   - 浏览器开发者工具
   - 网络请求监控

## 🚀 部署指南

### 生产环境部署

#### 1. 后端部署
```bash
# 构建JAR包
mvn clean package -DskipTests

# 运行
java -jar target/rag-try-1.0.0.jar

# 或使用Docker
docker build -t rag-chat-backend .
docker run -p 8080:8080 rag-chat-backend
```

#### 2. 前端部署
```bash
cd frontend

# 构建生产版本
npm run build

# 部署到Nginx/Apache
cp -r dist/* /var/www/html/
```

#### 3. Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 环境变量配置

```bash
# 生产环境变量
export SPRING_PROFILES_ACTIVE=prod
export DIFY_API_KEY=your-production-key
export OLLAMA_BASE_URL=http://ollama-server:11434
```

## 🔮 扩展功能

- [ ] WebSocket 实时流式对话
- [ ] 用户会话持久化
- [ ] 知识库向量数据库存储（如Chroma、Pinecone）
- [ ] 多租户支持
- [ ] 对话分析与统计
- [ ] 用户权限管理
- [ ] 多语言支持
- [ ] 移动端APP
- [ ] 语音对话功能
- [ ] 图片理解能力

## 📈 性能优化

### 后端优化
- 连接池配置
- 缓存策略（Redis）
- 异步处理
- 数据库优化

### 前端优化
- 组件懒加载
- 资源压缩
- CDN部署
- 缓存策略

## 📄 许可证

MIT License

## 🤝 贡献指南

我们欢迎各种形式的贡献！

### 贡献方式
1. **报告Bug**: 提交详细的问题报告
2. **功能建议**: 提出新功能想法
3. **代码贡献**: 提交Pull Request
4. **文档改进**: 完善项目文档

### 贡献流程
1. Fork本项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request

### 开发规范
- 遵循现有代码风格
- 添加必要的测试
- 更新相关文档
- 确保CI/CD通过

## 📞 联系我们

- **项目地址**: https://github.com/your-username/RAG-chat
- **问题反馈**: https://github.com/your-username/RAG-chat/issues
- **讨论交流**: https://github.com/your-username/RAG-chat/discussions

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [LangChain4j](https://github.com/langchain4j/langchain4j)
- [Ollama](https://ollama.com/)

---

⭐ 如果这个项目对你有帮助，请给我们一个星标！

