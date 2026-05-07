<template>
  <div class="customer-service-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('merchantChat.title') }}</h3>
        </div>
      </template>

      <div class="chat-container">
        <!-- 用户列表（左侧） -->
        <div class="user-list">
          <div class="user-list-header">
            <h4>{{ $t('merchantChat.chatList') }}</h4>
            <el-input
              v-model="searchKeyword"
              :placeholder="$t('merchantChat.searchUser')"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="user-list-content">
            <!-- 平台客服（管理员） -->
            <div
              :class="['user-item', 'admin-chat-item', chatMode === 'admin' ? 'active' : '']"
              @click="selectAdminChat"
            >
              <div class="user-avatar">
                <el-avatar :size="40" style="background: #409eff">
                  <el-icon><Service /></el-icon>
                </el-avatar>
                <el-badge
                  v-if="adminUnreadCount > 0"
                  :value="adminUnreadCount"
                  class="unread-badge"
                />
              </div>
              <div class="user-info">
                <div class="user-name">{{ $t('merchantChat.platformService') }}</div>
                <div class="user-last-message">
                  <span v-if="adminLastMessage">{{ adminLastMessage }}</span>
                  <span v-else class="no-message">{{ $t('merchantChat.noMessages') }}</span>
                  <span v-if="adminLastMessageTime" class="message-time">{{ formatRelativeTime(adminLastMessageTime) }}</span>
                </div>
              </div>
            </div>

            <!-- 用户列表 -->
            <div
              v-for="user in filteredUsers"
              :key="user.id"
              :class="['user-item', chatMode === 'user' && selectedUserId === user.id ? 'active' : '']"
              @click="selectUser(user)"
            >
              <div class="user-avatar">
                <el-avatar :size="40" :src="user.avatar || ''">
                  <img v-if="user.avatar" :src="user.avatar" alt="" />
                  <span v-else>{{ user.nickname?.[0] || user.username?.[0] || 'U' }}</span>
                </el-avatar>
                <el-badge
                  v-if="getUnreadCount(user.id) > 0"
                  :value="getUnreadCount(user.id)"
                  class="unread-badge"
                />
              </div>
              <div class="user-info">
                <div class="user-name">{{ user.nickname || user.username || $t('merchantChat.unnamedUser') }}</div>
                <div class="user-last-message">
                  <span v-if="getLastMessage(user.id) !== $t('merchantChat.noMessages')">{{ getLastMessage(user.id) }}</span>
                  <span v-else class="no-message">{{ $t('merchantChat.noMessages') }}</span>
                  <span v-if="getLastMessageTime(user.id)" class="message-time">{{ formatRelativeTime(getLastMessageTime(user.id)) }}</span>
                </div>
              </div>
            </div>
            
            <el-empty v-if="filteredUsers.length === 0 && chatMode !== 'admin'" :description="$t('merchantChat.noUsers')" />
          </div>
        </div>

        <!-- 聊天窗口（右侧） -->
        <div class="chat-window">
          <div v-if="chatMode" class="chat-content">
            <div class="chat-header">
              <div class="chat-user-info">
                <el-avatar :size="32" :src="chatMode === 'admin' ? null : selectedUser?.avatar" style="background: #409eff" v-if="chatMode === 'admin'">
                  <el-icon><Service /></el-icon>
                </el-avatar>
                <el-avatar :size="32" :src="selectedUser?.avatar" v-else>
                  {{ selectedUser?.nickname?.[0] || selectedUser?.username?.[0] }}
                </el-avatar>
                <span>{{ chatMode === 'admin' ? $t('merchantChat.platformService') : (selectedUser?.nickname || selectedUser?.username) }}</span>
              </div>
            </div>

            <div class="chat-messages" ref="messagesContainer">
              <div
                v-for="message in messages"
                :key="message.id"
                :class="['message-item', message.senderType === 'MERCHANT' ? 'message-right' : 'message-left']"
              >
                <!-- 左侧消息：显示对方头像和名称 -->
                <template v-if="message.senderType !== 'MERCHANT'">
                  <div class="message-sender-info">
                    <el-avatar 
                      :size="40" 
                      :src="getSenderAvatar(message)"
                      class="message-avatar"
                      style="background: #409eff"
                    >
                      <el-icon v-if="message.senderType === 'ADMIN'"><Service /></el-icon>
                      <img v-else-if="getSenderAvatar(message)" :src="getSenderAvatar(message)" alt="" />
                      <span v-else>{{ getSenderName(message)?.[0] || 'U' }}</span>
                    </el-avatar>
                    <div class="sender-name">{{ getSenderName(message) }}</div>
                  </div>
                  
                  <div class="message-wrapper">
                    <div class="message-content">
                      <div class="message-text">{{ message.content }}</div>
                    </div>
                    <div class="message-time">{{ formatTime(message.createdAt) }}</div>
                  </div>
                </template>
                
                <!-- 右侧消息：显示自己头像和名称 -->
                <template v-else>
                  <div class="message-wrapper">
                    <div class="message-content">
                      <div class="message-text">{{ message.content }}</div>
                    </div>
                    <div class="message-time">{{ formatTime(message.createdAt) }}</div>
                  </div>
                  
                  <div class="message-sender-info">
                    <el-avatar 
                      :size="40" 
                      :src="merchantAvatar"
                      class="message-avatar"
                    >
                      <img v-if="merchantAvatar" :src="merchantAvatar" alt="" />
                      <span v-else>{{ merchantName?.[0] || 'M' }}</span>
                    </el-avatar>
                    <div class="sender-name">{{ merchantName }}</div>
                  </div>
                </template>
              </div>
              <div v-if="messages.length === 0" class="empty-messages">
                <el-empty :description="$t('merchantChat.startChat')" />
              </div>
            </div>

            <div class="chat-input">
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="3"
                :placeholder="$t('merchantChat.inputPlaceholder')"
                @keyup.ctrl.enter="sendMessage"
                :disabled="!chatMode"
              />
              <div class="input-actions">
                <el-button type="primary" @click="sendMessage" :disabled="!inputMessage || !chatMode">
                  {{ $t('merchantChat.sendButton') }}
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-else class="chat-placeholder">
            <el-empty :description="$t('merchantChat.selectChatHint')" />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Search, Service } from '@element-plus/icons-vue'
import { 
  getChatUsers, 
  getMerchantUserChatHistory,
  getMerchantUserUnreadCount,
  sendMerchantMessage, 
  markMerchantUserAsRead,
  sendMerchantToAdminMessage,
  getMerchantAdminChatHistory,
  getMerchantAdminUnreadCount,
  markMerchantAdminAsRead
} from '@/api/chat'
import { useUserStore } from '@/stores/user'
import { useLocaleStore } from '@/stores/locale'
import { initStompClient, subscribeToTopic, disconnectStomp } from '@/utils/websocket-stomp'

const { t } = useI18n()
const userStore = useUserStore()
const localeStore = useLocaleStore()

const searchKeyword = ref('')
const users = ref([])
const selectedUserId = ref(null)
const selectedUser = ref(null)
const chatMode = ref(null)
const messages = ref([])
const inputMessage = ref('')
const messagesContainer = ref(null)
const unreadCounts = ref({})
const lastMessages = ref({})
const lastMessageTimes = ref({})
const adminUnreadCount = ref(0)
const adminLastMessage = ref('')
const adminLastMessageTime = ref(null)
const stompClient = ref(null)

const merchantAvatar = computed(() => userStore.userInfo?.avatar || '')
const merchantName = computed(() => userStore.userInfo?.shopName || userStore.userInfo?.username || t('merchantChat.merchant'))

const getSenderAvatar = (message) => {
  if (message.senderType === 'ADMIN') {
    return null
  } else if (message.senderType === 'USER' && selectedUser.value) {
    return selectedUser.value.avatar || null
  }
  return null
}

const getSenderName = (message) => {
  if (message.senderType === 'ADMIN') {
    return t('merchantChat.platformService')
  } else if (message.senderType === 'USER' && selectedUser.value) {
    return selectedUser.value.nickname || selectedUser.value.username || t('merchantChat.user')
  }
  return t('merchantChat.unknown')
}

const filteredUsers = computed(() => {
  if (!searchKeyword.value) {
    return users.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(user => 
    (user.username?.toLowerCase().includes(keyword)) ||
    (user.nickname?.toLowerCase().includes(keyword))
  )
})

const loadUsers = async () => {
  try {
    users.value = await getChatUsers()
    for (const user of users.value) {
      await loadUnreadCount(user.id)
      await loadLastMessage(user.id)
    }
    await loadAdminUnreadCount()
    await loadAdminLastMessage()
  } catch (error) {
    console.error('Load users failed:', error)
    ElMessage.error(t('merchantChat.loadUsersFailed'))
  }
}

const loadUnreadCount = async (userId) => {
  try {
    const response = await getMerchantUserUnreadCount(userId, 'USER')
    unreadCounts.value[userId] = response.count || 0
  } catch (error) {
    console.error('Load unread count failed:', error)
  }
}

const loadLastMessage = async (userId) => {
  try {
    const response = await getMerchantUserChatHistory(userId, 1, 1)
    if (response.records && response.records.length > 0) {
      const lastMsg = response.records[0]
      lastMessages.value[userId] = lastMsg.content
      lastMessageTimes.value[userId] = lastMsg.createdAt
    } else {
      lastMessages.value[userId] = t('merchantChat.noMessages')
      lastMessageTimes.value[userId] = null
    }
  } catch (error) {
    console.error('Load last message failed:', error)
  }
}

const getUnreadCount = (userId) => {
  return unreadCounts.value[userId] || 0
}

const getLastMessage = (userId) => {
  return lastMessages.value[userId] || t('merchantChat.noMessages')
}

const getLastMessageTime = (userId) => {
  return lastMessageTimes.value[userId] || null
}

const selectAdminChat = async () => {
  chatMode.value = 'admin'
  selectedUserId.value = null
  selectedUser.value = null
  await loadAdminChatHistory()
  if (adminUnreadCount.value > 0) {
    await markAdminMessagesAsRead()
    await loadAdminUnreadCount()
  }
}

const selectUser = async (user) => {
  chatMode.value = 'user'
  selectedUserId.value = user.id
  selectedUser.value = user
  await loadChatHistory()
  if (getUnreadCount(user.id) > 0) {
    await markMessagesAsRead(user.id)
    await loadUnreadCount(user.id)
  }
}

const loadChatHistory = async () => {
  if (!selectedUserId.value) return
  
  try {
    const response = await getMerchantUserChatHistory(selectedUserId.value, 1, 50)
    if (response.records) {
      messages.value = response.records.reverse()
    }
    scrollToBottom()
  } catch (error) {
    console.error('Load chat history failed:', error)
    ElMessage.error(t('merchantChat.loadHistoryFailed'))
  }
}

const loadAdminChatHistory = async () => {
  try {
    const response = await getMerchantAdminChatHistory(1, 50)
    if (response.records) {
      messages.value = response.records.reverse()
    }
    scrollToBottom()
  } catch (error) {
    console.error('Load admin chat history failed:', error)
    ElMessage.error(t('merchantChat.loadHistoryFailed'))
  }
}

const loadAdminUnreadCount = async () => {
  try {
    const response = await getMerchantAdminUnreadCount()
    adminUnreadCount.value = response.count || 0
  } catch (error) {
    console.error('Load admin unread count failed:', error)
  }
}

const loadAdminLastMessage = async () => {
  try {
    const response = await getMerchantAdminChatHistory(1, 1)
    if (response.records && response.records.length > 0) {
      const lastMsg = response.records[0]
      adminLastMessage.value = lastMsg.content
      adminLastMessageTime.value = lastMsg.createdAt
    } else {
      adminLastMessage.value = ''
      adminLastMessageTime.value = null
    }
  } catch (error) {
    console.error('Load admin last message failed:', error)
  }
}

const markAdminMessagesAsRead = async () => {
  try {
    await markMerchantAdminAsRead('ADMIN')
  } catch (error) {
    console.error('Mark admin messages as read failed:', error)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || !chatMode.value) return

  try {
    if (chatMode.value === 'admin') {
      await sendMerchantToAdminMessage(inputMessage.value, 'TEXT')
      
      messages.value.push({
        id: Date.now(),
        content: inputMessage.value,
        senderType: 'MERCHANT',
        senderId: userStore.userInfo?.id,
        createdAt: new Date().toISOString()
      })
      
      inputMessage.value = ''
      scrollToBottom()
      adminLastMessage.value = messages.value[messages.value.length - 1].content
      adminLastMessageTime.value = new Date().toISOString()
    } else {
      await sendMerchantMessage(selectedUserId.value, inputMessage.value, 'TEXT')
      
      messages.value.push({
        id: Date.now(),
        content: inputMessage.value,
        senderType: 'MERCHANT',
        senderId: userStore.userInfo?.id,
        createdAt: new Date().toISOString()
      })
      
      inputMessage.value = ''
      scrollToBottom()
      lastMessages.value[selectedUserId.value] = messages.value[messages.value.length - 1].content
      lastMessageTimes.value[selectedUserId.value] = new Date().toISOString()
    }
  } catch (error) {
    console.error('Send message failed:', error)
    ElMessage.error(error.response?.data?.message || t('merchantChat.sendFailed'))
  }
}

const markMessagesAsRead = async (userId) => {
  try {
    await markMerchantUserAsRead(userId, 'USER')
  } catch (error) {
    console.error('Mark messages as read failed:', error)
  }
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
  const locale = localeStore.isEnglish ? 'en-US' : 'zh-CN'
  return date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' })
}

const formatRelativeTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const messageTime = new Date(time)
  const diff = now - messageTime
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (seconds < 60) {
    return t('merchantChat.justNow')
  } else if (minutes < 60) {
    return t('merchantChat.minutesAgo', { n: minutes })
  } else if (hours < 24) {
    return t('merchantChat.hoursAgo', { n: hours })
  } else if (days < 7) {
    return t('merchantChat.daysAgo', { n: days })
  } else {
    const locale = localeStore.isEnglish ? 'en-US' : 'zh-CN'
    return messageTime.toLocaleDateString(locale, { month: 'short', day: 'numeric' })
  }
}

const initWebSocket = () => {
  const merchantId = userStore.userInfo?.id
  if (!merchantId) return

  try {
    const client = initStompClient('MERCHANT', merchantId)
    stompClient.value = client

    subscribeToTopic(`/topic/merchant/${merchantId}/chat`, (data) => {
      if (data.type === 'CHAT') {
        if (data.userId && data.userId === selectedUserId.value && chatMode.value === 'user') {
          messages.value.push({
            id: Date.now(),
            content: data.content,
            senderType: 'USER',
            senderId: data.userId,
            createdAt: new Date().toISOString()
          })
          scrollToBottom()
          loadUnreadCount(data.userId)
          lastMessages.value[data.userId] = data.content
          lastMessageTimes.value[data.userId] = new Date().toISOString()
        } else if (data.userId) {
          unreadCounts.value[data.userId] = (unreadCounts.value[data.userId] || 0) + 1
          lastMessages.value[data.userId] = data.content
          lastMessageTimes.value[data.userId] = new Date().toISOString()
        }
      }
    })

    subscribeToTopic(`/topic/merchant/${merchantId}/admin/chat`, (data) => {
      if (data.type === 'CHAT' && data.senderType === 'ADMIN') {
        if (chatMode.value === 'admin') {
          messages.value.push({
            id: Date.now(),
            content: data.content,
            senderType: 'ADMIN',
            senderId: data.senderId,
            createdAt: new Date().toISOString()
          })
          scrollToBottom()
          loadAdminUnreadCount()
          adminLastMessage.value = data.content
          adminLastMessageTime.value = new Date().toISOString()
        } else {
          adminUnreadCount.value = (adminUnreadCount.value || 0) + 1
          adminLastMessage.value = data.content
          adminLastMessageTime.value = new Date().toISOString()
        }
      }
    })

    setInterval(() => {
      users.value.forEach(user => {
        if (user.id !== selectedUserId.value) {
          loadUnreadCount(user.id)
        }
      })
      if (chatMode.value !== 'admin') {
        loadAdminUnreadCount()
      }
    }, 5000)
  } catch (error) {
    console.error('WebSocket init failed:', error)
  }
}

watch([selectedUserId, chatMode], () => {
  if (chatMode.value === 'user' && selectedUserId.value) {
    loadChatHistory()
  } else if (chatMode.value === 'admin') {
    loadAdminChatHistory()
  }
})

onMounted(() => {
  loadUsers()
  initWebSocket()
})

onUnmounted(() => {
  if (stompClient.value) {
    disconnectStomp()
  }
})
</script>


<style scoped lang="scss">
.customer-service-page {
  padding: 20px;
}

.card-header {
  h3 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
  }
}

.chat-container {
  display: flex;
  height: calc(100vh - 280px);
  min-height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
}

// 左侧用户列表
.user-list {
  width: 300px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  background: #fafafa;

  .user-list-header {
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
    background: white;

    h4 {
      margin: 0 0 12px 0;
      font-size: 14px;
      color: #606266;
    }
  }

  .user-list-content {
    flex: 1;
    overflow-y: auto;
  }
}

.user-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #f0f0f0;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: #ecf5ff;
    border-left: 3px solid #409eff;
  }

  &.admin-chat-item {
    background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
    border-bottom: 2px solid #e4e7ed;

    &:hover {
      background: linear-gradient(135deg, #e6f7ff 0%, #d6f0ff 100%);
    }

    &.active {
      background: linear-gradient(135deg, #d6f0ff 0%, #c6e9ff 100%);
    }
  }

  .user-avatar {
    position: relative;
    margin-right: 12px;

    .unread-badge {
      position: absolute;
      top: -5px;
      right: -5px;
    }
  }

  .user-info {
    flex: 1;
    min-width: 0;

    .user-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .user-last-message {
      font-size: 12px;
      color: #909399;
      display: flex;
      align-items: center;
      gap: 8px;

      span:first-child {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .no-message {
        color: #c0c4cc;
        font-style: italic;
      }

      .message-time {
        flex-shrink: 0;
        font-size: 11px;
        color: #c0c4cc;
      }
    }
  }
}

// 右侧聊天窗口
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.chat-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  background: white;

  .chat-user-info {
    display: flex;
    align-items: center;
    gap: 10px;

    span {
      font-size: 15px;
      font-weight: 500;
      color: #303133;
    }
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);

  .empty-messages {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
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
    max-width: 60%;
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
  border-top: 1px solid #e4e7ed;
  background: white;

  .input-actions {
    margin-top: 12px;
    display: flex;
    justify-content: flex-end;
  }
}

// 响应式适配
@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
    height: calc(100vh - 200px);
  }

  .user-list {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }

  .message-wrapper {
    max-width: 80% !important;
  }
}
</style>
