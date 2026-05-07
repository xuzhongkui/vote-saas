import request from '@/utils/request'

// ==================== 分类 ====================
// 必须传入 merchantId 参数
export const getCategories = (merchantId) => {
  if (!merchantId) {
    console.error('getCategories: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get('/user/categories', { 
    params: { merchantId } 
  })
}

// ==================== 商品 ====================
// 必须传入 merchantId 参数
export const getProducts = (params) => {
  if (!params.merchantId) {
    console.error('getProducts: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get('/user/products', { params })
}

export const getProductDetail = (id, merchantId) => {
  if (!merchantId) {
    console.error('getProductDetail: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get(`/user/products/${id}`, {
    params: { merchantId }
  })
}

// 推荐商品必须传入 merchantId
export const getRecommendProducts = (params) => {
  if (!params.merchantId) {
    console.error('getRecommendProducts: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get('/user/products/recommend', { params })
}

// ==================== 购物车 ====================
export const getCart = () => request.get('/user/cart')
export const addToCart = (data) => request.post('/user/cart', null, { params: data })
export const updateCart = (id, data) => request.put(`/user/cart/${id}`, null, { params: data })
export const deleteCart = (id) => request.delete(`/user/cart/${id}`)

// ==================== 订单 ====================
// 创建订单必须包含 merchantId
export const createOrder = (data) => {
  if (!data.merchantId) {
    console.error('createOrder: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.post('/user/orders', data)
}

// 获取订单列表必须传入 merchantId
export const getOrders = (merchantId, page = 1, size = 20) => {
  if (!merchantId) {
    console.error('getOrders: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get('/user/orders', { 
    params: { merchantId, page, size } 
  })
}

export const getUserOrders = (merchantId, page = 1, size = 20) => {
  if (!merchantId) {
    console.error('getUserOrders: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get('/user/orders', { 
    params: { merchantId, page, size } 
  })
}

// 订单详情必须传入 merchantId
export const getOrderDetail = (id, merchantId) => {
  if (!merchantId) {
    console.error('getOrderDetail: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get(`/user/orders/${id}`, {
    params: { merchantId }
  })
}

export const mockPayOrder = (id, merchantId) => {
  if (!merchantId) {
    console.error('mockPayOrder: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.post(`/user/orders/${id}/pay/mock`, null, {
    params: { merchantId }
  })
}

export const confirmReceive = (id, merchantId) => {
  if (!merchantId) {
    console.error('confirmReceive: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.post(`/user/orders/${id}/confirm-receive`, null, {
    params: { merchantId }
  })
}

export const cancelOrder = (id, merchantId, data) => {
  if (!merchantId) {
    console.error('cancelOrder: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.post(`/user/orders/${id}/cancel`, data, {
    params: { merchantId }
  })
}

// ==================== 地址 ====================
export const getAddresses = () => request.get('/user/addresses')
export const getAddress = (id) => request.get(`/user/addresses/${id}`)
export const createAddress = (data) => request.post('/user/addresses', data)
export const updateAddress = (id, data) => request.put(`/user/addresses/${id}`, data)
export const deleteAddress = (id) => request.delete(`/user/addresses/${id}`)

// ==================== 个人资料 ====================
export const getProfile = () => request.get('/user/profile')
export const updateProfile = (data) => request.put('/user/profile', data)

// ==================== 绑定商家（首次登录）====================
export const bindMerchant = (data) => request.post('/user/bind-merchant', data)

// ==================== 换绑商家 ====================
export const changeMerchant = (data) => request.post('/user/change-merchant', data)

// ==================== 获取商家信息 ====================
export const getMerchantInfo = (merchantId) => request.get(`/user/merchant/${merchantId}`)

// ==================== 用户统计数据（会员中心）====================
export const getUserStatistics = (merchantId) => {
  if (!merchantId) {
    console.error('getUserStatistics: merchantId is required')
    return Promise.reject(new Error('merchantId is required'))
  }
  return request.get('/user/statistics', {
    params: { merchantId }
  })
}

