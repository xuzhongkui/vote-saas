import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    console.log('请求拦截器 - Token:', token ? token.substring(0, 50) + '...' : 'null')
    console.log('请求拦截器 - URL:', config.url)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // ==================== 数据隔离：自动注入 merchant_id ====================
    // 获取当前用户信息中的 merchantId
    const userInfo = localStorage.getItem('userInfo')
    let merchantId = null
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo)
        merchantId = user.merchantId
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }

    // 需要强制 merchant_id 的接口列表（用户端）
    const requireMerchantIdPaths = [
      '/user/categories',      // 分类
      '/user/products',        // 商品
      '/user/orders',          // 订单
    ]
    
    // 用户端聊天接口需要 merchantId（但商家端聊天接口不需要）
    const isUserChatApi = config.url.includes('/chat/') && 
                          !config.url.includes('/chat/merchant/') && 
                          !config.url.includes('/chat/admin/') &&
                          !config.url.includes('/chat/users')

    // 检查是否是需要 merchant_id 的接口
    const needsMerchantId = requireMerchantIdPaths.some(path => config.url.includes(path)) || isUserChatApi
    
    if (needsMerchantId && merchantId) {
      // 自动注入 merchantId 到请求参数中
      if (config.method === 'get' || config.method === 'delete') {
        config.params = config.params || {}
        // 如果参数中没有 merchantId，自动添加
        if (!config.params.merchantId) {
          config.params.merchantId = merchantId
          console.log('自动注入 merchantId 到 params:', merchantId)
        }
      } else if (config.method === 'post' || config.method === 'put') {
        // POST/PUT 请求，检查 params 和 data
        if (config.params && !config.params.merchantId) {
          config.params.merchantId = merchantId
          console.log('自动注入 merchantId 到 params:', merchantId)
        }
        if (config.data && typeof config.data === 'object' && !config.data.merchantId) {
          config.data.merchantId = merchantId
          console.log('自动注入 merchantId 到 data:', merchantId)
        }
      }
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      const url = error.config?.url || ''
      
      // 登录接口的特殊处理 - 不显示通用错误，由登录页面自己处理
      if (url.includes('/auth/') && url.includes('/login')) {
        return Promise.reject(error)
      }
      
      // 绑定商家接口的特殊处理 - 不显示通用错误，由绑定页面自己处理
      if (url.includes('/bind-merchant')) {
        return Promise.reject(error)
      }
      
      if (status === 401 || status === 403) {
        // 401 未授权 或 403 禁止访问 都视为登录过期，跳转到登录页面
        const message = data?.message || ''
        
        // 如果是商家端缴费相关的错误，引导用户去缴费页面
        if (status === 403 && (message.includes('缴费') || message.includes('支付'))) {
          ElMessage.error(message)
          setTimeout(() => {
            if (router.currentRoute.value.path !== '/merchant/payment') {
              router.push('/merchant/payment')
            }
          }, 1500)
        } else {
          // 检查是否已经在登录页面或者 token 已经被清除（避免重复弹窗）
          const currentPath = router.currentRoute.value.path
          const isLoginPage = currentPath.includes('/login')
          const hasToken = localStorage.getItem('token')
          
          // 只有在非登录页面且还有 token 时才弹窗（说明是真正的过期）
          if (!isLoginPage && hasToken) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            localStorage.removeItem('userType')
            
            // 根据当前路径判断跳转到哪个登录页面
            if (currentPath.startsWith('/merchant')) {
              router.push('/merchant/login')
            } else if (currentPath.startsWith('/admin')) {
              router.push('/admin/login')
            } else {
              router.push('/login')
            }
          }
        }
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error(data?.message || '服务器错误')
      }
      // 其他错误不在这里显示，由调用方自己处理，避免重复弹窗
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request

