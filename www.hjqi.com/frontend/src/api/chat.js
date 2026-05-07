import request from '@/utils/request'

// ==================== 客服聊天 ====================
/**
 * 发送消息（用户端必须传入 merchantId，商家端可选）
 */
export const sendChatMessage = (merchantId, content, messageType = 'TEXT') => {
  // 用户端必须传 merchantId，商家端可以不传（后端从 token 获取）
  if (!merchantId) {
    console.warn('sendChatMessage: merchantId not provided, will use token-based merchantId')
  }
  return request.post('/chat/send', null, {
    params: {
      merchantId,
      content,
      messageType
    }
  })
}

/**
 * 获取聊天记录（用户端必须传入 merchantId，商家端可选）
 */
export const getChatHistory = (merchantId, userId, page = 1, size = 20) => {
  // 用户端必须传 merchantId，商家端可以不传（后端从 token 获取）
  if (!merchantId) {
    console.warn('getChatHistory: merchantId not provided, will use token-based merchantId')
  }
  return request.get('/chat/history', {
    params: {
      merchantId,
      userId,
      page,
      size
    }
  })
}

/**
 * 标记消息为已读（用户端必须传入 merchantId，商家端可选）
 */
export const markChatAsRead = (merchantId, userId, senderType) => {
  // 用户端必须传 merchantId，商家端可以不传（后端从 token 获取）
  if (!merchantId) {
    console.warn('markChatAsRead: merchantId not provided, will use token-based merchantId')
  }
  return request.post('/chat/read', null, {
    params: {
      merchantId,
      userId,
      senderType
    }
  })
}

/**
 * 获取未读消息数（用户端必须传入 merchantId，商家端可选）
 */
export const getUnreadCount = (merchantId, userId, senderType) => {
  // 用户端必须传 merchantId，商家端可以不传（后端从 token 获取）
  if (!merchantId) {
    console.warn('getUnreadCount: merchantId not provided, will use token-based merchantId')
  }
  return request.get('/chat/unread', {
    params: {
      merchantId,
      userId,
      senderType
    }
  })
}

/**
 * 发送消息（商家）
 */
export const sendMerchantMessage = (userId, content, messageType = 'TEXT') => {
  return request.post('/chat/merchant/send', null, {
    params: {
      userId,
      content,
      messageType
    }
  })
}

/**
 * 获取聊天用户列表（商家）
 */
export const getChatUsers = () => {
  return request.get('/chat/users')
}

/**
 * 获取商家与用户的聊天记录（商家端）
 */
export const getMerchantUserChatHistory = (userId, page = 1, size = 20) => {
  return request.get('/chat/merchant/history', {
    params: {
      userId,
      page,
      size
    }
  })
}

/**
 * 获取商家与用户的未读消息数（商家端）
 */
export const getMerchantUserUnreadCount = (userId, senderType) => {
  return request.get('/chat/merchant/unread', {
    params: {
      userId,
      senderType
    }
  })
}

/**
 * 标记商家与用户的消息为已读（商家端）
 */
export const markMerchantUserAsRead = (userId, senderType) => {
  return request.post('/chat/merchant/read', null, {
    params: {
      userId,
      senderType
    }
  })
}

// ==================== 商家与管理员聊天 ====================
/**
 * 发送消息给管理员（商家）
 */
export const sendMerchantToAdminMessage = (content, messageType = 'TEXT') => {
  return request.post('/chat/merchant/admin/send', null, {
    params: {
      content,
      messageType
    }
  })
}

/**
 * 获取商家与管理员的聊天记录
 */
export const getMerchantAdminChatHistory = (page = 1, size = 20) => {
  return request.get('/chat/merchant/admin/history', {
    params: {
      page,
      size
    }
  })
}

/**
 * 获取商家未读管理员消息数
 */
export const getMerchantAdminUnreadCount = () => {
  return request.get('/chat/merchant/admin/unread')
}

/**
 * 标记商家消息为已读（管理员）
 */
export const markMerchantAdminAsRead = (senderType) => {
  return request.post('/chat/merchant/admin/read', null, {
    params: {
      senderType
    }
  })
}

