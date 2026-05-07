<template>
  <div class="merchant-customer-service-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminMerchantCustomerService.title') }}</h3>
        </div>
      </template>

      <div class="chat-container">
        <!-- 商家列表（左侧） -->
        <div class="merchant-list">
          <div class="merchant-list-header">
            <h4>{{ $t('adminMerchantCustomerService.merchantList') }}</h4>
            <el-input
              v-model="searchKeyword"
              :placeholder="$t('adminMerchantCustomerService.searchMerchant')"
              clearable
              style="width: 200px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="merchant-list-content">
            <div
              v-for="merchant in filteredMerchants"
              :key="merchant.id"
              :class="['merchant-item', selectedMerchantId === merchant.id ? 'active' : '']"
              @click="selectMerchant(merchant)"
            >
              <div class="merchant-avatar">
                <el-avatar :size="40" :src="merchant.logo">
                  {{ merchant.shopNameZh?.[0] || merchant.username?.[0] }}
                </el-avatar>
                <el-badge
                  v-if="getUnreadCount(merchant.id) > 0"
                  :value="getUnreadCount(merchant.id)"
                  class="unread-badge"
                />
              </div>
              <div class="merchant-info">
                <div class="merchant-name">{{ merchant.shopNameZh || merchant.username }}</div>
                <div class="merchant-last-message">{{ getLastMessage(merchant.id) }}</div>
              </div>
            </div>
            
            <el-empty v-if="filteredMerchants.length === 0" :description="$t('adminMerchantCustomerService.noMerchants')" />
          </div>
        </div>

        <!-- 聊天窗口（右侧） -->
        <div class="chat-window">
          <div v-if="selectedMerchantId" class="chat-content">
            <div class="chat-header">
              <div class="chat-merchant-info">
                <el-avatar :size="32" :src="selectedMerchant?.logo">
                  {{ selectedMerchant?.shopNameZh?.[0] || selectedMerchant?.username?.[0] }}
                </el-avatar>
                <span>{{ selectedMerchant?.shopNameZh || selectedMerchant?.username }}</span>
              </div>
            </div>

            <div class="chat-messages" ref="messagesContainer">
              <div
                v-for="message in messages"
                :key="message.id"
                :class="['message-item', message.senderType === 'ADMIN' ? 'message-right' : 'message-left']"
              >
                <!-- 左侧消息：显示商家头像和名称 -->
                <template v-if="message.senderType !== 'ADMIN'">
                  <div class="message-sender-info">
                    <el-avatar 
                      :size="40" 
                      :src="selectedMerchant?.logo"
                      class="message-avatar"
                    >
                      <img v-if="selectedMerchant?.logo" :src="selectedMerchant?.logo" alt="" />
                      <span v-else>{{ selectedMerchant?.shopNameZh?.[0] || selectedMerchant?.username?.[0] || 'M' }}</span>
                    </el-avatar>
                    <div class="sender-name">{{ selectedMerchant?.shopNameZh || selectedMerchant?.username || $t('adminMerchantCustomerService.merchant') }}</div>
                  </div>
                  
                  <div class="message-wrapper">
                    <div class="message-content">
                      <div class="message-text">{{ message.content }}</div>
                    </div>
                    <div class="message-time">{{ formatTime(message.createdAt) }}</div>
                  </div>
                </template>
                
                <!-- 右侧消息：显示管理员头像和名称 -->
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
                      class="message-avatar"
                      style="background: #409eff"
                    >
                      <el-icon><Service /></el-icon>
                    </el-avatar>
                    <div class="sender-name">{{ $t('adminMerchantCustomerService.platformService') }}</div>
                  </div>
                </template>
              </div>
              <div v-if="messages.length === 0" class="empty-messages">
                <el-empty :description="$t('adminMerchantCustomerService.startChat')" />
              </div>
            </div>

            <div class="chat-input">
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="3"
                :placeholder="$t('adminMerchantCustomerService.inputPlaceholder')"
                @keyup.ctrl.enter="sendMessage"
                :disabled="!selectedMerchantId"
              />
              <div class="input-actions">
                <el-button type="primary" @click="sendMessage" :disabled="!inputMessage || !selectedMerchantId">
                  {{ $t('adminMerchantCustomerService.sendButton') }}
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-else class="chat-placeholder">
            <el-empty :description="$t('adminMerchantCustomerService.selectMerchantHint')" />
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
import { getMerchants } from '@/api/admin'
import {
  getAdminChatHistory,
  sendAdminMessage,
  markAdminAsRead,
  getAdminUnreadCount
} from '@/api/admin'
import { initStompClient, subscribeToTopic, disconnectStomp } from '@/utils/websocket-stomp'

const { t } = useI18n()

const searchKeyword = ref('')
const merchants = ref([])
const selectedMerchantId = ref(null)
const selectedMerchant = ref(null)
const messages = ref([])
const inputMessage = ref('')
const messagesContainer = ref(null)
const unreadCounts = ref({})
const lastMessages = ref({})
const stompClient = ref(null)

const filteredMerchants = computed(() => {
  if (!searchKeyword.value) {
    return merchants.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return merchants.value.filter(merchant => 
    (merchant.username?.toLowerCase().includes(keyword)) ||
    (merchant.shopNameZh?.toLowerCase().includes(keyword)) ||
    (merchant.shopNameEn?.toLowerCase().includes(keyword))
  )
})

// 加载商家列表
const loadMerchants = async () => {
  try {
    const result = await getMerchants({ page: 1, size: 1000 })
    merchants.value = result.data?.records || result.records || []
    // 加载每个商家的未读消息数
    for (const merchant of merchants.value) {
      await loadUnreadCount(merchant.id)
      await loadLastMessage(merchant.id)
    }
  } catch (error) {
    console.error('加载商家列表失败:', error)
    ElMessage.error(t('adminMerchantCustomerService.loadMerchantsFailed'))
  }
}

// 加载未读消息数
const loadUnreadCount = async (merchantId) => {
  try {
    const response = await getAdminUnreadCount(merchantId)
    unreadCounts.value[merchantId] = response.count || 0
  } catch (error) {
    console.error('加载未读消息数失败:', error)
  }
}

// 加载最后一条消息
const loadLastMessage = async (merchantId) => {
  try {
    const response = await getAdminChatHistory(merchantId, 1, 1)
    if (response.records && response.records.length > 0) {
      lastMessages.value[merchantId] = response.records[0].content
    }
  } catch (error) {
    console.error('加载最后一条消息失败:', error)
  }
}

// 获取未读消息数
const getUnreadCount = (merchantId) => {
  return unreadCounts.value[merchantId] || 0
}

// 获取最后一条消息
const getLastMessage = (merchantId) => {
  return lastMessages.value[merchantId] || t('adminMerchantCustomerService.noMessages')
}

// 选择商家
const selectMerchant = async (merchant) => {
  selectedMerchantId.value = merchant.id
  selectedMerchant.value = merchant
  await loadChatHistory()
  // 标记消息为已读
  if (getUnreadCount(merchant.id) > 0) {
    await markMessagesAsRead(merchant.id)
    await loadUnreadCount(merchant.id)
  }
}

// 加载聊天记录
const loadChatHistory = async () => {
  if (!selectedMerchantId.value) return
  
  try {
    const response = await getAdminChatHistory(selectedMerchantId.value, 1, 50)
    if (response.records) {
      messages.value = response.records.reverse() // 反转顺序，最新的在底部
    }
    scrollToBottom()
  } catch (error) {
    console.error('加载聊天记录失败:', error)
    ElMessage.error(t('adminMerchantCustomerService.loadHistoryFailed'))
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || !selectedMerchantId.value) return

  try {
    await sendAdminMessage(selectedMerchantId.value, inputMessage.value, 'TEXT')
    
    // 添加到消息列表
    messages.value.push({
      id: Date.now(),
      content: inputMessage.value,
      senderType: 'ADMIN',
      senderId: null,
      createdAt: new Date().toISOString()
    })
    
    inputMessage.value = ''
    scrollToBottom()
    // 更新最后一条消息
    lastMessages.value[selectedMerchantId.value] = messages.value[messages.value.length - 1].content
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error(error.response?.data?.message || t('adminMerchantCustomerService.sendFailed'))
  }
}

// 标记消息为已读
const markMessagesAsRead = async (merchantId) => {
  try {
    await markAdminAsRead(merchantId, 'MERCHANT')
  } catch (error) {
    console.error('标记消息为已读失败:', error)
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

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 初始化WebSocket
const initWebSocket = () => {
  try {
    // 管理员订阅所有商家的聊天消息
    const client = initStompClient('ADMIN', null)
    stompClient.value = client

    // 订阅管理员聊天消息
    subscribeToTopic('/topic/admin/chat', (data) => {
      if (data.type === 'CHAT' && data.merchantId === selectedMerchantId.value) {
        messages.value.push({
          id: Date.now(),
          content: data.content,
          senderType: 'MERCHANT',
          senderId: data.merchantId,
          createdAt: new Date().toISOString()
        })
        scrollToBottom()
        // 更新未读消息数
        if (data.merchantId === selectedMerchantId.value) {
          loadUnreadCount(data.merchantId)
        } else {
          unreadCounts.value[data.merchantId] = (unreadCounts.value[data.merchantId] || 0) + 1
        }
        // 更新最后一条消息
        lastMessages.value[data.merchantId] = data.content
      }
    })

    // 定期刷新未读消息数
    setInterval(() => {
      merchants.value.forEach(merchant => {
        if (merchant.id !== selectedMerchantId.value) {
          loadUnreadCount(merchant.id)
        }
      })
    }, 5000)
  } catch (error) {
    console.error('WebSocket初始化失败:', error)
  }
}

watch(selectedMerchantId, () => {
  if (selectedMerchantId.value) {
    loadChatHistory()
  }
})

onMounted(() => {
  loadMerchants()
  initWebSocket()
})

onUnmounted(() => {
  if (stompClient.value) {
    disconnectStomp()
  }
})
</script>

<style scoped lang="scss">
.merchant-customer-service-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
    }
  }

  .chat-container {
    display: flex;
    height: 900px;
    gap: 16px;

    .merchant-list {
      width: 300px;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      display: flex;
      flex-direction: column;

      .merchant-list-header {
        padding: 16px;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        justify-content: space-between;
        align-items: center;

        h4 {
          margin: 0;
          font-size: 16px;
        }
      }

      .merchant-list-content {
        flex: 1;
        overflow-y: auto;

        .merchant-item {
          padding: 12px 16px;
          display: flex;
          align-items: center;
          gap: 12px;
          cursor: pointer;
          border-bottom: 1px solid #f5f7fa;
          transition: background-color 0.2s;

          &:hover {
            background-color: #f5f7fa;
          }

          &.active {
            background-color: #e6f7ff;
          }

          .merchant-avatar {
            position: relative;

            .unread-badge {
              position: absolute;
              top: -5px;
              right: -5px;
            }
          }

          .merchant-info {
            flex: 1;
            min-width: 0;

            .merchant-name {
              font-weight: 500;
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .merchant-last-message {
              font-size: 12px;
              color: #909399;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
      }
    }

    .chat-window {
      flex: 1;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      display: flex;
      flex-direction: column;

      .chat-content {
        display: flex;
        flex-direction: column;
        height: 100%;

        .chat-header {
          padding: 16px;
          border-bottom: 1px solid #e4e7ed;
          display: flex;
          align-items: center;
          gap: 12px;

          .chat-merchant-info {
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 500;
          }
        }

        .chat-messages {
          flex: 1;
          overflow-y: auto;
          padding: 16px;
          background: #f5f7fa;

          .empty-messages {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
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
              padding: 10px 14px;
              border-radius: 8px;
              position: relative;

              .message-text {
                word-wrap: break-word;
                line-height: 1.5;
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
              background: #409eff;
              color: white;
            }
          }

          .message-left {
            .message-content {
              background: white;
              color: #333;
              box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
            }
          }
        }

        .chat-input {
          padding: 16px;
          border-top: 1px solid #e4e7ed;
          background: white;

          .input-actions {
            margin-top: 8px;
            display: flex;
            justify-content: flex-end;
          }
        }
      }

      .chat-placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
      }
    }
  }
}
</style>

