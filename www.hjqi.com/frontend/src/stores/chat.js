import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount, markChatAsRead } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  // 用户端：商家消息未读数
  const merchantUnreadCount = ref(0)
  
  // 商家端：用户消息未读数（按用户ID分组）
  const userUnreadCounts = ref({})
  
  // 总未读数
  const totalUnreadCount = ref(0)

  /**
   * 加载用户端未读消息数（来自商家的消息）
   */
  async function loadMerchantUnreadCount(merchantId) {
    try {
      if (!merchantId) {
        console.warn('loadMerchantUnreadCount: merchantId is required')
        return 0
      }
      const response = await getUnreadCount(merchantId, null, 'MERCHANT')
      merchantUnreadCount.value = response?.count || 0
      return merchantUnreadCount.value
    } catch (error) {
      console.log('加载未读消息数失败:', error?.message)
      return 0
    }
  }

  /**
   * 标记商家消息为已读（用户端）
   */
  async function markMerchantMessagesAsRead(merchantId) {
    try {
      await markChatAsRead(merchantId, null, 'MERCHANT')
      merchantUnreadCount.value = 0
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  /**
   * 增加商家消息未读数
   */
  function incrementMerchantUnread() {
    merchantUnreadCount.value++
  }

  /**
   * 清零商家消息未读数
   */
  function clearMerchantUnread() {
    merchantUnreadCount.value = 0
  }

  /**
   * 设置商家消息未读数
   */
  function setMerchantUnreadCount(count) {
    merchantUnreadCount.value = count
  }

  return {
    merchantUnreadCount,
    userUnreadCounts,
    totalUnreadCount,
    loadMerchantUnreadCount,
    markMerchantMessagesAsRead,
    incrementMerchantUnread,
    clearMerchantUnread,
    setMerchantUnreadCount
  }
})

