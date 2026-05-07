import request from '@/utils/request'

// ==================== 用户认证 ====================
export const userLogin = (data) => request.post('/auth/user/login', data)
export const userRegister = (data) => request.post('/auth/user/register', data)

// ==================== 商家认证 ====================
export const merchantLogin = (data) => request.post('/auth/merchant/login', data)
export const merchantRegister = (data) => request.post('/auth/merchant/register', data)

// ==================== 管理员认证 ====================
export const adminLogin = (data) => request.post('/auth/admin/login', data)

// ==================== 密码重置 ====================
export const forgotPassword = (data) => request.post('/auth/password/forgot', data)
export const verifyCode = (data) => request.post('/auth/password/verify', data)
export const resetPassword = (data) => request.post('/auth/password/reset', data)

