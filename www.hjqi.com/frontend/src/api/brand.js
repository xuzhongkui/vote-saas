import request from '@/utils/request'

// ==================== 品牌管理 ====================
/**
 * 创建品牌
 */
export const createBrand = (data) => {
  return request.post('/brand/create', null, {
    params: data
  })
}

/**
 * 获取品牌列表
 */
export const getBrands = (page = 1, size = 10) => {
  return request.get('/brand/list', {
    params: { page, size }
  })
}

/**
 * 获取所有品牌（不分页）
 */
export const getAllBrands = () => {
  return request.get('/brand/all')
}

/**
 * 更新品牌
 */
export const updateBrand = (id, data) => {
  return request.put(`/brand/${id}`, null, {
    params: data
  })
}

/**
 * 删除品牌
 */
export const deleteBrand = (id) => {
  return request.delete(`/brand/${id}`)
}

