import request from '@/utils/request'

// ==================== 分类管理 ====================
export const getCategories = () => request.get('/merchant/categories')
export const createCategory = (data) => request.post('/merchant/categories', data)
export const updateCategory = (id, data) => request.put(`/merchant/categories/${id}`, data)
export const deleteCategory = (id) => request.delete(`/merchant/categories/${id}`)

// ==================== 商品管理 ====================
export const getProducts = (params) => request.get('/merchant/products', { params })
export const getProduct = (id) => request.get(`/merchant/products/${id}`)
export const createProduct = (data) => request.post('/merchant/products', data)
export const updateProduct = (id, data) => request.put(`/merchant/products/${id}`, data)
export const deleteProduct = (id) => request.delete(`/merchant/products/${id}`)

// ==================== SKU管理 ====================
export const getProductSkus = (productId) => request.get(`/merchant/products/${productId}/skus`)
export const createProductSku = (data) => request.post('/merchant/products/skus', data)
export const updateProductSku = (id, data) => request.put(`/merchant/products/skus/${id}`, data)
export const deleteProductSku = (id) => request.delete(`/merchant/products/skus/${id}`)

// ==================== 订单管理 ====================
export const getOrders = (params) => request.get('/merchant/orders', { params })
export const getOrder = (id) => request.get(`/merchant/orders/${id}`)
export const confirmOrder = (id, data) => request.post(`/merchant/orders/${id}/confirm`, data)
export const shipOrder = (id, data) => request.post(`/merchant/orders/${id}/ship`, data)
export const completeOrder = (id) => request.post(`/merchant/orders/${id}/complete`)
export const cancelOrder = (id, data) => request.post(`/merchant/orders/${id}/cancel`, data)
export const updateOrderRemark = (id, data) => request.put(`/merchant/orders/${id}/remark`, data)

// ==================== 店铺配置 ====================
export const getMerchantConfig = () => request.get('/merchant/config')
export const updateMerchantConfig = (data) => request.put('/merchant/config', data)

// ==================== 店铺信息 ====================
export const getMerchantInfo = (merchantId) => {
  if (merchantId) {
    return request.get(`/merchant/info/${merchantId}`)
  }
  return request.get('/merchant/info')
}
export const updateMerchantInfo = (data) => request.put('/merchant/info', data)

// ==================== 数据统计 ====================
export const getStatistics = () => request.get('/merchant/statistics')

// ==================== 账单管理 ====================
export const getBills = (params) => request.get('/merchant/bills', { params })
export const getBill = (id) => request.get(`/merchant/bills/${id}`)
export const payBill = (id) => request.post(`/merchant/bills/${id}/pay`)

// ==================== 推广管理 ====================
export const getPromotionLink = () => request.get('/merchant/promotion/link')
export const getPromotionStatistics = () => request.get('/merchant/promotion/statistics')
export const getPromotionRecords = (params) => request.get('/merchant/promotion/records', { params })
// 推广码验证（公开接口，注册时使用）
export const verifyPromotionCode = (code) => request.get(`/auth/merchant/promotion/verify/${code}`)
export const applyWithdrawal = (data) => request.post('/merchant/promotion/withdrawal', data)
export const getWithdrawalRecords = () => request.get('/merchant/promotion/withdrawals')

// ==================== 二维码管理 ====================
export const generateInviteQrCode = () => request.post('/qrcode/merchant/invite/generate')
export const getInviteQrCode = () => request.get('/qrcode/merchant/invite')

// ==================== 支付管理 ====================
export const createMerchantPaymentOrder = (merchantId) => {
  if (merchantId) {
    return request.post(`/merchant/payment/registration/create?merchantId=${merchantId}`)
  }
  return request.post('/merchant/payment/registration/create')
}
export const getMerchantPaymentOrder = (merchantId) => {
  if (merchantId) {
    return request.get(`/merchant/payment/order?merchantId=${merchantId}`)
  }
  return request.get('/merchant/payment/order')
}
export const getPaymentOrderStatus = (paymentNo) => request.get(`/merchant/payment/status/${paymentNo}`)
export const mockPaymentSuccess = (paymentNo) => request.post(`/merchant/payment/mock-success/${paymentNo}`)

// ==================== 发票管理 ====================
export const getMerchantInvoices = () => request.get('/merchant/invoices')
export const getInvoiceDetail = (id) => request.get(`/merchant/invoices/${id}`)
export const resendInvoiceEmail = (id) => request.post(`/merchant/invoices/${id}/resend`)
export const downloadInvoicePdf = (id) => request.get(`/merchant/invoices/${id}/pdf`, { responseType: 'blob' })
export const previewInvoicePdf = (id) => request.get(`/merchant/invoices/${id}/preview`, { responseType: 'blob' })

// ==================== 售后规则管理 ====================
export const getAfterSaleRules = (params) => request.get('/merchant/after-sale-rules', { params })
export const createAfterSaleRule = (data) => request.post('/merchant/after-sale-rules', data)
export const updateAfterSaleRule = (id, data) => request.put(`/merchant/after-sale-rules/${id}`, data)
export const deleteAfterSaleRule = (id) => request.delete(`/merchant/after-sale-rules/${id}`)

// ==================== 邮箱配置管理 ====================
export const getMerchantEmails = () => request.get('/merchant/emails')
export const createMerchantEmail = (data) => request.post('/merchant/emails', data)
export const updateMerchantEmail = (id, data) => request.put(`/merchant/emails/${id}`, data)
export const deleteMerchantEmail = (id) => request.delete(`/merchant/emails/${id}`)

// ==================== 通知配置管理 ====================
export const getMerchantNotificationConfigs = () => request.get('/merchant/notification-configs')
export const getMerchantNotificationConfigsGrouped = () => request.get('/merchant/notification-configs/grouped')
export const saveMerchantNotificationConfig = (data) => request.post('/merchant/notification-configs', data)
export const saveMerchantNotificationConfigsBatch = (data) => request.post('/merchant/notification-configs/batch', data)
export const deleteMerchantNotificationConfig = (id) => request.delete(`/merchant/notification-configs/${id}`)

// ==================== 品牌管理 ====================
export const getBrands = () => request.get('/brand/all')
export const getBrandsPage = (params) => request.get('/brand/list', { params })
export const createBrand = (data) => request.post('/brand/create', null, { params: data })
export const updateBrand = (id, data) => request.put(`/brand/${id}`, null, { params: data })
export const deleteBrand = (id) => request.delete(`/brand/${id}`)

// ==================== 运费模板管理 ====================
export const getShippingTemplates = () => request.get('/merchant/shipping-templates')
export const getShippingTemplate = (id) => request.get(`/merchant/shipping-templates/${id}`)
export const createShippingTemplate = (data) => request.post('/merchant/shipping-templates', data)
export const updateShippingTemplate = (id, data) => request.put(`/merchant/shipping-templates/${id}`, data)
export const deleteShippingTemplate = (id) => request.delete(`/merchant/shipping-templates/${id}`)
export const getShippingTemplateRules = (id) => request.get(`/merchant/shipping-templates/${id}/rules`)
export const addShippingTemplateRule = (id, data) => request.post(`/merchant/shipping-templates/${id}/rules`, data)
export const deleteShippingTemplateRule = (ruleId) => request.delete(`/merchant/shipping-templates/rules/${ruleId}`)
