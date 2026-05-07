<template>
  <el-popover
    placement="bottom-end"
    :width="380"
    trigger="click"
    v-model:visible="visible"
  >
    <template #reference>
      <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0" class="notification-badge">
        <el-button :icon="Bell" circle />
      </el-badge>
    </template>

    <div class="notification-center">
      <div class="notification-header">
        <h4>通知中心</h4>
        <el-button type="primary" link @click="markAllAsRead" v-if="totalUnreadCount > 0">
          全部已读
        </el-button>
      </div>

      <!-- 分类标签 -->
      <div class="notification-tabs">
        <div 
          :class="['tab-item', { active: activeTab === 'all' }]"
          @click="activeTab = 'all'"
        >
          全部
          <el-badge :value="totalUnreadCount" :hidden="totalUnreadCount === 0" type="danger" />
        </div>
        <div 
          :class="['tab-item', { active: activeTab === 'chat' }]"
          @click="activeTab = 'chat'"
        >
          <el-icon><ChatDotRound /></el-icon>
          客服消息
          <el-badge :value="chatUnreadCount" :hidden="chatUnreadCount === 0" type="danger" />
        </div>
        <div 
          :class="['tab-item', { active: activeTab === 'order' }]"
          @click="activeTab = 'order'"
        >
          <el-icon><ShoppingBag /></el-icon>
          订单通知
          <el-badge :value="orderUnreadCount" :hidden="orderUnreadCount === 0" type="danger" />
        </div>
      </div>

      <div class="notification-list">
        <div 
          v-for="notification in filteredNotifications" 
          :key="notification.id"
          :class="['notification-item', { 'unread': !notification.read }]"
          @click="handleNotificationClick(notification)"
        >
          <div class="notification-icon" :class="notification.type">
            <el-icon v-if="notification.type === 'CHAT'">
              <ChatDotRound />
            </el-icon>
            <el-icon v-else-if="notification.type === 'ORDER_STATUS'">
              <ShoppingBag />
            </el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-desc" v-if="notification.content">{{ notification.content }}</div>
            <div class="notification-time">{{ formatTime(notification.timestamp) }}</div>
          </div>
          <div class="notification-action" v-if="notification.type === 'CHAT'">
            <el-button type="primary" size="small" link>回复</el-button>
          </div>
        </div>

        <el-empty v-if="filteredNotifications.length === 0" description="暂无通知" :image-size="80" />
      </div>

      <!-- 底部操作 -->
      <div class="notification-footer" v-if="notifications.length > 0">
        <el-button type="primary" link @click="viewAllNotifications">查看全部</el-button>
        <el-button type="danger" link @click="clearAllNotifications">清空通知</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, ShoppingBag, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { subscribeToTopic } from '@/utils/websocket-stomp'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const visible = ref(false)
const notifications = ref([])
const activeTab = ref('all')

// 计算各类未读数（只统计客服消息和订单通知）
const chatUnreadCount = computed(() => 
  notifications.value.filter(n => n.type === 'CHAT' && !n.read).length
)
const orderUnreadCount = computed(() => 
  notifications.value.filter(n => n.type === 'ORDER_STATUS' && !n.read).length
)
const totalUnreadCount = computed(() => 
  chatUnreadCount.value + orderUnreadCount.value
)

// 过滤通知（只显示客服消息和订单通知）
const filteredNotifications = computed(() => {
  // 只显示客服消息和订单通知
  const validNotifications = notifications.value.filter(n => 
    n.type === 'CHAT' || n.type === 'ORDER_STATUS'
  )
  
  if (activeTab.value === 'all') {
    return validNotifications
  } else if (activeTab.value === 'chat') {
    return validNotifications.filter(n => n.type === 'CHAT')
  } else if (activeTab.value === 'order') {
    return validNotifications.filter(n => n.type === 'ORDER_STATUS')
  }
  return validNotifications
})

// 初始化WebSocket订阅
const initNotificationSubscription = () => {
  const userInfo = userStore.userInfo
  console.log('🔔 通知中心初始化 - userInfo:', userInfo)
  
  if (!userInfo?.id) {
    console.error('❌ 无法初始化通知订阅：用户信息不存在')
    return
  }

  // 使用 userStore.isMerchant 判断用户类型
  const userType = userStore.isMerchant ? 'merchant' : 'user'
  const userId = userInfo.id

  console.log('✅ 初始化通知中心订阅:', { userType, userId, isMerchant: userStore.isMerchant })

  // 订阅聊天消息通知
  const chatTopic = `/topic/${userType}/${userId}/chat`
  console.log('📢 订阅聊天主题:', chatTopic)
  subscribeToTopic(chatTopic, (data) => {
    console.log('💬 收到聊天通知:', data)
    // 处理所有聊天消息（不限制 type，因为后端可能发送不同格式）
    handleChatNotification(data)
  })

  // 订阅主通知频道（订单状态通知）
  // 后端发送到 /topic/user/{userId} 或 /topic/merchant/{merchantId}
  const mainTopic = `/topic/${userType}/${userId}`
  console.log('📢 订阅主频道:', mainTopic)
  subscribeToTopic(mainTopic, (data) => {
    console.log('📨 收到主频道通知:', data)
    if (data.type === 'ORDER_STATUS') {
      console.log('🛒 处理订单通知:', data)
      handleOrderNotification(data)
    } else if (data.type === 'CHAT') {
      // 也处理聊天消息（兼容不同的推送路径）
      console.log('💬 处理聊天通知:', data)
      handleChatNotification(data)
    } else {
      console.log('⚠️ 未知通知类型:', data.type, data)
    }
    // 移除系统通知处理
  })

  // 商家端：订阅管理员消息
  if (userStore.isMerchant) {
    const adminChatTopic = `/topic/merchant/${userId}/admin/chat`
    console.log('📢 订阅管理员消息主题:', adminChatTopic)
    subscribeToTopic(adminChatTopic, (data) => {
      console.log('👨‍💼 收到管理员消息:', data)
      handleChatNotification({
        ...data,
        senderName: '平台客服',
        isAdmin: true
      })
    })
  }
}

// 处理聊天通知
const handleChatNotification = (data) => {
  const notification = {
    id: Date.now(),
    type: 'CHAT',
    title: data.isAdmin ? '平台客服消息' : `来自 ${data.senderName || '用户'} 的消息`,
    content: data.content?.substring(0, 50) + (data.content?.length > 50 ? '...' : ''),
    timestamp: data.timestamp || Date.now(),
    read: false,
    data: data
  }

  notifications.value.unshift(notification)

  // 显示浏览器通知
  showBrowserNotification(notification.title, notification.content)
  
  // 播放提示音
  playNotificationSound()
}

// 处理订单通知
const handleOrderNotification = (data) => {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'CONFIRMED': '已确认',
    'SHIPPING': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUNDING': '退款中',
    'REFUNDED': '已退款'
  }
  
  const notification = {
    id: Date.now(),
    type: 'ORDER_STATUS',
    title: `订单状态更新`,
    content: `订单 ${data.orderNo} ${statusMap[data.status] || data.status}`,
    timestamp: data.timestamp || Date.now(),
    read: false,
    data: data
  }

  notifications.value.unshift(notification)
  showBrowserNotification(notification.title, notification.content)
}

// 显示浏览器通知
const showBrowserNotification = (title, body) => {
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification(title, {
      body: body,
      icon: '/favicon.ico'
    })
  }
}

// 播放提示音
const playNotificationSound = () => {
  try {
    const audio = new Audio('/notification.mp3')
    audio.volume = 0.5
    audio.play().catch(() => {})
  } catch (e) {
    // 忽略音频播放错误
  }
}

// 标记全部已读
const markAllAsRead = () => {
  notifications.value.forEach(n => n.read = true)
  ElMessage.success('已全部标记为已读')
}

// 处理通知点击
const handleNotificationClick = (notification) => {
  notification.read = true
  visible.value = false

  // 根据通知类型跳转
  if (notification.type === 'CHAT') {
    if (userStore.isMerchant) {
      // 商家端跳转到客服管理
      router.push('/merchant/customer-service')
    } else {
      // 用户端跳转到客服聊天
      router.push('/user/chat')
    }
  } else if (notification.type === 'ORDER_STATUS' && notification.data?.orderId) {
    if (userStore.isMerchant) {
      router.push(`/merchant/orders`)
    } else {
      router.push(`/user/orders/${notification.data.orderId}`)
    }
  }
}

// 查看全部通知
const viewAllNotifications = () => {
  visible.value = false
  // 可以跳转到通知列表页面
  ElMessage.info('通知列表功能开发中')
}

// 清空通知
const clearAllNotifications = () => {
  notifications.value = []
  ElMessage.success('已清空所有通知')
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 请求浏览器通知权限
const requestNotificationPermission = () => {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission()
  }
}

onMounted(() => {
  // 使用 nextTick 确保 store 已经初始化
  nextTick(() => {
    initNotificationSubscription()
    requestNotificationPermission()
  })
})

// 监听 userInfo 变化，如果从 null 变为有值，则初始化订阅
watch(() => userStore.userInfo, (newVal, oldVal) => {
  console.log('👀 NotificationCenter - userInfo 变化:', { oldVal, newVal })
  if (!oldVal && newVal?.id) {
    console.log('🔄 userInfo 已加载，重新初始化通知订阅')
    initNotificationSubscription()
  }
})
</script>

<style scoped lang="scss">
.notification-badge {
  cursor: pointer;
}

.notification-center {
  max-height: 550px;
  display: flex;
  flex-direction: column;
}

.notification-header {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  h4 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
  }
}

.notification-tabs {
  display: flex;
  padding: 8px 12px;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafafa;
  
  .tab-item {
    display: flex;
    align-items: center;
    gap: 4px;
    padding: 6px 12px;
    border-radius: 16px;
    cursor: pointer;
    font-size: 13px;
    color: #666;
    transition: all 0.2s;
    
    &:hover {
      background: #e6f7ff;
      color: #409eff;
    }
    
    &.active {
      background: #409eff;
      color: #fff;
      
      :deep(.el-badge__content) {
        background: #fff;
        color: #409eff;
      }
    }
    
    .el-icon {
      font-size: 14px;
    }
  }
}

.notification-list {
  flex: 1;
  max-height: 350px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: #f5f5f5;
  }
  
  &.unread {
    background-color: #f0f9ff;
    
    &:hover {
      background-color: #e6f4ff;
    }
  }
}

.notification-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  
  &.CHAT {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  &.ORDER_STATUS {
    background: #fff7e6;
    color: #fa8c16;
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
}

.notification-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-action {
  flex-shrink: 0;
}

.notification-footer {
  padding: 10px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  background: #fafafa;
}
</style>
