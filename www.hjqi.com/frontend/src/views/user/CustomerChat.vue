<template>
  <div class="customer-chat-page">
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-avatar :size="40" style="background: #409eff">
              <el-icon><Service /></el-icon>
            </el-avatar>
            <div class="merchant-info">
              <h3>{{ merchantName || $t('customerChat.title') }}</h3>
              <span class="status online">{{ $t('customerChat.online') }}</span>
            </div>
          </div>
          <div class="header-right">
            <el-tooltip :content="$t('customerChat.refreshMessages')">
              <el-button :icon="Refresh" circle @click="loadChatHistory" />
            </el-tooltip>
          </div>
        </div>
      </template>

      <div class="chat-container">
        <div class="chat-messages" ref="messagesContainer">
          <div class="load-more" v-if="hasMore">
            <el-button type="primary" link @click="loadMoreMessages" :loading="loadingMore">
              {{ $t('customerChat.loadMore') }}
            </el-button>
          </div>
          
          <div
            v-for="message in messages"
            :key="message.id"
            :class="['message-item', message.senderType === 'USER' ? 'message-right' : 'message-left']"
          >
            <template v-if="message.senderType !== 'USER'">
              <div class="message-sender-info">
                <el-avatar :size="40" class="message-avatar" style="background: #409eff">
                  <el-icon><Service /></el-icon>
                </el-avatar>
                <div class="sender-name">{{ merchantName || $t('customerChat.service') }}</div>
              </div>
              <div class="message-wrapper">
                <div class="message-content">
                  <div class="message-text">{{ message.content }}</div>
                </div>
                <div class="message-time">{{ formatTime(message.createdAt) }}</div>
              </div>
            </template>
            
            <template v-else>
              <div class="message-wrapper">
                <div class="message-content">
                  <div class="message-text">{{ message.content }}</div>
                </div>
                <div class="message-time">{{ formatTime(message.createdAt) }}</div>
              </div>
              <div class="message-sender-info">
                <el-avatar :size="40" :src="userAvatar" class="message-avatar">
                  <span>{{ userName?.[0] || 'U' }}</span>
                </el-avatar>
                <div class="sender-name">{{ userName || $t('customerChat.me') }}</div>
              </div>
            </template>
          </div>
          
          <div v-if="messages.length === 0 && !loading" class="empty-messages">
            <el-empty :description="$t('customerChat.noMessages')">
              <template #image>
                <el-icon :size="60" color="#c0c4cc"><ChatDotRound /></el-icon>
              </template>
            </el-empty>
            <div class="quick-questions">
              <p>{{ $t('customerChat.quickQuestionsHint') }}</p>
              <div class="question-tags">
                <el-tag v-for="question in quickQuestions" :key="question" @click="sendQuickQuestion(question)" class="question-tag">
                  {{ question }}
                </el-tag>
              </div>
            </div>
          </div>
          
          <div v-if="loading" class="loading-messages">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>{{ $t('customerChat.loading') }}</span>
          </div>
        </div>

        <div class="chat-input">
          <el-input v-model="inputMessage" type="textarea" :rows="3" :placeholder="$t('customerChat.inputPlaceholder')" @keyup.ctrl.enter="sendMessage" :disabled="!canSendMessage" maxlength="500" show-word-limit />
          <div class="input-actions">
            <span class="hint">{{ $t('customerChat.sendHint') }}</span>
            <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim() || !canSendMessage" :loading="sending">
              <el-icon><Promotion /></el-icon>
              {{ $t('customerChat.sendButton') }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog v-model="showBindDialog" :title="$t('customerChat.notBoundTitle')" width="400px" :close-on-click-modal="false">
      <div class="bind-dialog-content">
        <el-icon :size="48" color="#E6A23C"><Warning /></el-icon>
        <p>{{ $t('customerChat.notBoundMessage') }}</p>
        <p class="sub-text">{{ $t('customerChat.notBoundSubMessage') }}</p>
      </div>
      <template #footer>
        <el-button @click="$router.push('/user/products')">{{ $t('customerChat.backToProducts') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Service, Refresh, ChatDotRound, Loading, Promotion, Warning } from '@element-plus/icons-vue'
import { getChatHistory, sendChatMessage, markChatAsRead } from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { useLocaleStore } from '@/stores/locale'
import { subscribeToTopic } from '@/utils/websocket-stomp'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()
const localeStore = useLocaleStore()
const { t } = useI18n()

const messages = ref([])
const inputMessage = ref('')
const messagesContainer = ref(null)
const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const hasMore = ref(false)
const currentPage = ref(1)
const showBindDialog = ref(false)

const quickQuestions = computed(() => [
  t('customerChat.quickQuestion1'),
  t('customerChat.quickQuestion2'),
  t('customerChat.quickQuestion3'),
  t('customerChat.quickQuestion4')
])

const userAvatar = computed(() => userStore.userInfo?.avatar || '')
const userName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || t('customerChat.me'))
const merchantId = computed(() => userStore.userInfo?.merchantId)
const merchantName = computed(() => userStore.userInfo?.merchantName || t('customerChat.title'))
const canSendMessage = computed(() => merchantId.value && !sending.value)

const loadChatHistory = async () => {
  if (!merchantId.value) {
    showBindDialog.value = true
    return
  }
  loading.value = true
  currentPage.value = 1
  try {
    const response = await getChatHistory(merchantId.value, null, 1, 30)
    if (response.records) {
      messages.value = response.records.reverse()
      hasMore.value = response.total > response.records.length
    }
    scrollToBottom()
    await markChatAsRead(merchantId.value, null, 'MERCHANT')
    chatStore.clearMerchantUnread()
  } catch (error) {
    console.error('Load chat history failed:', error)
    ElMessage.error(t('customerChat.loadFailed'))
  } finally {
    loading.value = false
  }
}

const loadMoreMessages = async () => {
  if (!merchantId.value || loadingMore.value) return
  loadingMore.value = true
  currentPage.value++
  try {
    const response = await getChatHistory(merchantId.value, null, currentPage.value, 30)
    if (response.records) {
      const oldMessages = response.records.reverse()
      messages.value = [...oldMessages, ...messages.value]
      hasMore.value = response.total > messages.value.length
    }
  } catch (error) {
    console.error('Load more messages failed:', error)
    currentPage.value--
  } finally {
    loadingMore.value = false
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !canSendMessage.value) return
  sending.value = true
  const content = inputMessage.value.trim()
  try {
    await sendChatMessage(merchantId.value, content, 'TEXT')
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
    console.error('Send message failed:', error)
    ElMessage.error(error.response?.data?.message || t('customerChat.sendFailed'))
  } finally {
    sending.value = false
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  const locale = localeStore.isEnglish ? 'en-US' : 'zh-CN'
  if (isToday) {
    return date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' })
  } else {
    return date.toLocaleDateString(locale, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
  }
}

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
      if (merchantId.value) {
        markChatAsRead(merchantId.value, null, 'MERCHANT').then(() => chatStore.clearMerchantUnread()).catch(() => {})
      }
    }
  })
}

watch(() => route.path, (newPath) => {
  if (newPath === '/user/chat' && merchantId.value) {
    markChatAsRead(merchantId.value, null, 'MERCHANT').then(() => chatStore.clearMerchantUnread()).catch(() => {})
  }
})

onMounted(() => {
  if (!merchantId.value) {
    showBindDialog.value = true
  } else {
    loadChatHistory()
    initWebSocketSubscription()
  }
})
</script>


<style scoped lang="scss">
.customer-chat-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.chat-card {
  height: calc(100vh - 180px);
  min-height: 600px;
  display: flex;
  flex-direction: column;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  :deep(.el-card__body) {
    flex: 1;
    padding: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .merchant-info {
      h3 {
        margin: 0 0 4px 0;
        font-size: 16px;
      }
      
      .status {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 10px;
        
        &.online {
          background: #f0f9eb;
          color: #67c23a;
        }
      }
    }
  }
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  
  .load-more {
    text-align: center;
    padding: 10px 0;
  }
  
  .empty-messages {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    
    .quick-questions {
      margin-top: 20px;
      text-align: center;
      
      p {
        color: #909399;
        margin-bottom: 12px;
      }
      
      .question-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        justify-content: center;
        max-width: 400px;
        
        .question-tag {
          cursor: pointer;
          transition: all 0.2s;
          
          &:hover {
            background: #409eff;
            color: #fff;
            border-color: #409eff;
          }
        }
      }
    }
  }
  
  .loading-messages {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 20px;
    color: #909399;
  }
}

.message-item {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
  gap: 12px;

  &.message-right {
    justify-content: flex-end;
  }

  &.message-left {
    justify-content: flex-start;
  }

  .message-sender-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;

    .message-avatar {
      flex-shrink: 0;
    }

    .sender-name {
      font-size: 12px;
      color: #909399;
      max-width: 60px;
      text-align: center;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .message-wrapper {
    display: flex;
    flex-direction: column;
    max-width: 70%;
    gap: 4px;
  }

  .message-content {
    padding: 12px 16px;
    border-radius: 12px;
    position: relative;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);

    .message-text {
      word-wrap: break-word;
      line-height: 1.6;
      font-size: 14px;
    }
  }

  .message-time {
    font-size: 12px;
    color: #909399;
    padding: 0 4px;
  }
}

.message-right {
  .message-content {
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    color: white;
    border-bottom-right-radius: 4px;
  }
  
  .message-time {
    text-align: right;
  }
}

.message-left {
  .message-content {
    background: white;
    color: #333;
    border-bottom-left-radius: 4px;
  }
}

.chat-input {
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  background: white;

  .input-actions {
    margin-top: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .hint {
      font-size: 12px;
      color: #909399;
    }
  }
}

.bind-dialog-content {
  text-align: center;
  padding: 20px 0;
  
  p {
    margin: 16px 0 8px;
    font-size: 16px;
    color: #333;
  }
  
  .sub-text {
    font-size: 14px;
    color: #909399;
  }
}
</style>
