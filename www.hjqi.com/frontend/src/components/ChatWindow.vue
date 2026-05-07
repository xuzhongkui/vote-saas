<template>
  <div class="chat-window-overlay" @click.self="$emit('close')">
    <div class="chat-window">
      <div class="chat-header">
        <div class="header-left">
          <el-avatar :size="36" style="background: #409eff">
            <el-icon><Service /></el-icon>
          </el-avatar>
          <div class="merchant-info">
            <span class="name">{{ merchantName || '在线客服' }}</span>
            <span class="status">在线</span>
          </div>
        </div>
        <div class="header-actions">
          <el-tooltip content="打开完整聊天页面">
            <el-button :icon="FullScreen" circle size="small" @click="openFullChat" />
          </el-tooltip>
          <el-button :icon="Close" circle size="small" @click="$emit('close')" />
        </div>
      </div>

      <div class="chat-messages" ref="messagesContainer">
        <div
          v-for="message in messages"
          :key="message.id"
          :class="['message-item', message.senderType === 'USER' ? 'message-right' : 'message-left']"
        >
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
          </div>
          <div class="message-time">{{ formatTime(message.createdAt) }}</div>
        </div>
        
        <div v-if="messages.length === 0 && !loading" class="empty-messages">
          <p>您好！有什么可以帮助您的吗？</p>
          <div class="quick-questions">
            <el-tag 
              v-for="question in quickQuestions" 
              :key="question"
              @click="sendQuickQuestion(question)"
              class="question-tag"
              size="small"
            >
              {{ question }}
            </el-tag>
          </div>
        </div>
        
        <div v-if="loading" class="loading-messages">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>
      </div>

      <div class="chat-input">
        <el-input
          v-model="inputMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          :disabled="sending"
        >
          <template #append>
            <el-button 
              type="primary" 
              :icon="Promotion" 
              @click="sendMessage"
              :loading="sending"
              :disabled="!inputMessage.trim()"
            />
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Service, Close, FullScreen, Promotion, Loading } from '@element-plus/icons-vue'
import { getChatHistory, sendChatMessage, markChatAsRead } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { subscribeToTopic } from '@/utils/websocket-stomp'

const props = defineProps({
  merchantId: {
    type: [Number, String],
    required: true
  },
  merchantName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close'])

const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const messages = ref([])
const inputMessage = ref('')
const messagesContainer = ref(null)
const loading = ref(false)
const sending = ref(false)

// 快捷问题
const quickQuestions = [
  '商品什么时候发货？',
  '可以修改地址吗？',
  '如何申请退款？'
]

// 加载聊天记录
const loadChatHistory = async () => {
  if (!props.merchantId) return
  
  loading.value = true
  try {
    const response = await getChatHistory(props.merchantId, null, 1, 20)
    if (response.records) {
      messages.value = response.records.reverse()
    }
    scrollToBottom()
    
    // 标记消息为已读，并清零 store 中的未读数
    await markChatAsRead(props.merchantId, null, 'MERCHANT')
    chatStore.clearMerchantUnread()
  } catch (error) {
    console.error('加载聊天记录失败:', error)
  } finally {
    loading.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || sending.value) return
  
  sending.value = true
  const content = inputMessage.value.trim()
  
  try {
    await sendChatMessage(props.merchantId, content, 'TEXT')
    
    messages.value.push({
      id: Date.now(),
      content: content,
      senderType: 'USER',
      senderId: userStore.userInfo?.id,
      createdAt: new Date().toISOString()
    })
    
    inputMessage.value = ''
    scrollToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

// 发送快捷问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 打开完整聊天页面
const openFullChat = () => {
  emit('close')
  router.push('/user/chat')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 初始化WebSocket订阅
const initWebSocketSubscription = () => {
  const userId = userStore.userInfo?.id
  if (!userId) return

  subscribeToTopic(`/topic/user/${userId}/chat`, (data) => {
    if (data.senderType === 'MERCHANT') {
      messages.value.push({
        id: Date.now(),
        content: data.content,
        senderType: 'MERCHANT',
        senderId: data.senderId,
        createdAt: new Date().toISOString()
      })
      scrollToBottom()
      
      // 在聊天窗口收到消息，立即标记为已读
      if (props.merchantId) {
        markChatAsRead(props.merchantId, null, 'MERCHANT')
          .then(() => {
            chatStore.clearMerchantUnread()
          })
          .catch(() => {})
      }
    }
  })
}

onMounted(() => {
  loadChatHistory()
  initWebSocketSubscription()
})
</script>

<style scoped lang="scss">
.chat-window-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-window {
  width: 400px;
  height: 550px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 12px 16px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
    
    .merchant-info {
      display: flex;
      flex-direction: column;
      
      .name {
        font-weight: 500;
        font-size: 14px;
      }
      
      .status {
        font-size: 12px;
        opacity: 0.9;
      }
    }
  }
  
  .header-actions {
    display: flex;
    gap: 8px;
    
    .el-button {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      color: #fff;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
  
  .empty-messages {
    text-align: center;
    padding: 20px;
    color: #666;
    
    p {
      margin-bottom: 16px;
    }
    
    .quick-questions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      justify-content: center;
      
      .question-tag {
        cursor: pointer;
        
        &:hover {
          background: #409eff;
          color: #fff;
          border-color: #409eff;
        }
      }
    }
  }
  
  .loading-messages {
    text-align: center;
    padding: 20px;
    color: #909399;
  }
}

.message-item {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  
  &.message-right {
    align-items: flex-end;
    
    .message-content {
      background: #409eff;
      color: #fff;
      border-radius: 12px 12px 4px 12px;
    }
  }
  
  &.message-left {
    align-items: flex-start;
    
    .message-content {
      background: #fff;
      color: #333;
      border-radius: 12px 12px 12px 4px;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    }
  }
  
  .message-content {
    max-width: 80%;
    padding: 10px 14px;
    
    .message-text {
      word-wrap: break-word;
      line-height: 1.5;
      font-size: 14px;
    }
  }
  
  .message-time {
    font-size: 11px;
    color: #909399;
    margin-top: 4px;
    padding: 0 4px;
  }
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
  
  :deep(.el-input-group__append) {
    padding: 0;
    
    .el-button {
      margin: 0;
      border-radius: 0 4px 4px 0;
    }
  }
}
</style>
