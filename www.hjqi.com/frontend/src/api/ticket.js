import request from '@/utils/request'

// ==================== 工单系统 ====================
/**
 * 创建工单
 */
export const createTicket = (data) => {
  return request.post('/ticket/create', null, {
    params: data
  })
}

/**
 * 获取工单列表（管理员）
 */
export const getTickets = (params) => {
  return request.get('/ticket/admin/list', { params })
}

/**
 * 获取商家工单列表
 */
export const getMerchantTickets = (params) => {
  return request.get('/ticket/merchant/list', { params })
}

/**
 * 处理工单（管理员）
 */
export const handleTicket = (ticketId, adminReply, status) => {
  return request.post('/ticket/handle', null, {
    params: {
      ticketId,
      adminReply,
      status
    }
  })
}

