import request from '@/utils/request'

// ==================== 二维码生成 ====================
/**
 * 生成商家邀请码二维码
 */
export const generateMerchantInviteQr = () => {
  return request.get('/qrcode/merchant/invite')
}

/**
 * 生成商品分享二维码
 */
export const generateProductShareQr = (productId) => {
  return request.get(`/qrcode/product/${productId}`)
}

/**
 * 生成店铺二维码
 */
export const generateShopQr = (merchantId) => {
  return request.get(`/qrcode/shop/${merchantId}`)
}

