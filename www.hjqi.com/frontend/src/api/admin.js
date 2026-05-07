import request from '@/utils/request'

// ==================== 商家管理 ====================
export const getMerchants = (params) => request.get('/admin/merchants', { params })
export const getMerchant = (id) => request.get(`/admin/merchants/${id}`)
export const auditMerchant = (id, data) => request.post(`/admin/merchants/${id}/audit`, data)
export const updateMerchantStatus = (id, status) => request.put(`/admin/merchants/${id}/status`, null, {
  params: { status }
})

// ==================== 系统配置 ====================
export const getSystemConfigs = (configGroup) => request.get('/admin/system-configs', {
  params: { configGroup }
})
export const getSystemConfig = (id) => request.get(`/admin/system-configs/${id}`)
export const getSystemConfigByKey = (configKey) => request.get(`/admin/system-configs/key/${configKey}`)
export const createSystemConfig = (data) => request.post('/admin/system-configs', data)
export const updateSystemConfig = (id, data) => request.put(`/admin/system-configs/${id}`, data)
export const deleteSystemConfig = (id) => request.delete(`/admin/system-configs/${id}`)

// ==================== 套餐管理 ====================
export const getPlans = () => request.get('/admin/plans')
export const getPlan = (id) => request.get(`/admin/plans/${id}`)
export const createPlan = (data) => request.post('/admin/plans', data)
export const updatePlan = (id, data) => request.put(`/admin/plans/${id}`, data)
export const deletePlan = (id) => request.delete(`/admin/plans/${id}`)

// ==================== 内容管理 ====================
export const getContents = (params) => request.get('/admin/content', { params })
export const getContent = (id) => request.get(`/admin/content/${id}`)
export const createContent = (data) => request.post('/admin/content', data)
export const updateContent = (id, data) => request.put(`/admin/content/${id}`, data)
export const deleteContent = (id) => request.delete(`/admin/content/${id}`)

// ==================== 操作日志 ====================
export const getLogs = (params) => request.get('/admin/logs', { params })

// ==================== 账单管理 ====================
export const getAdminBills = (params) => request.get('/admin/bills', { params })
export const getAdminBill = (id) => request.get(`/admin/bills/${id}`)
export const createBill = (data) => request.post('/admin/bills', data)

// ==================== 数据统计 ====================
export const getStatistics = () => request.get('/admin/statistics')

// ==================== 文件上传 ====================
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// ==================== 管理员与商家聊天 ====================
export const sendAdminMessage = (merchantId, content, messageType = 'TEXT') => {
  return request.post('/chat/admin/send', null, {
    params: {
      merchantId,
      content,
      messageType
    }
  })
}

export const getAdminChatHistory = (merchantId, page = 1, size = 20) => {
  return request.get('/chat/admin/history', {
    params: {
      merchantId,
      page,
      size
    }
  })
}

export const markAdminAsRead = (merchantId, senderType) => {
  return request.post('/chat/admin/read', null, {
    params: {
      merchantId,
      senderType
    }
  })
}

export const getAdminUnreadCount = (merchantId) => {
  return request.get('/chat/admin/unread', {
    params: {
      merchantId
    }
  })
}

// ==================== 语言设置 ====================
export const getPlatformLocale = () => request.get('/public/platform-locale')
export const setPlatformLocale = (locale) => request.put('/admin/platform-locale', { locale })

