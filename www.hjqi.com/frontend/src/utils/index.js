import dayjs from 'dayjs'

/**
 * 格式化日期时间
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化日期
 */
export function formatDate(date, format = 'YYYY-MM-DD') {
  if (!date) return ''
  return dayjs(date).format(format)
}

/**
 * 格式化金额（分转元）
 * 注意：此函数用于处理以"分"为单位的金额（如前端计算的金额）
 */
export function formatMoney(amount) {
  if (amount === null || amount === undefined || isNaN(amount)) return '0.00'
  const num = Number(amount)
  if (isNaN(num)) return '0.00'
  return (num / 100).toFixed(2)
}

/**
 * 格式化金额（直接显示，用于后端返回的BigDecimal）
 * 注意：此函数用于处理后端返回的以"元"为单位的金额
 */
export function formatMoneyDirect(amount) {
  if (amount === null || amount === undefined || isNaN(amount)) return '0.00'
  const num = Number(amount)
  if (isNaN(num)) return '0.00'
  return num.toFixed(2)
}

/**
 * 格式化金额（元转分）
 */
export function toFen(amount) {
  if (amount === null || amount === undefined) return 0
  return Math.round(amount * 100)
}

/**
 * 订单状态映射
 */
export const ORDER_STATUS_MAP = {
  PENDING: { label: '待支付', type: 'warning' },
  CONFIRMED: { label: '已确认', type: 'success' },
  SHIPPING: { label: '配送中', type: 'primary' },
  COMPLETED: { label: '已完成', type: 'success' },
  CANCELLED: { label: '已取消', type: 'info' }
}

/**
 * 商家状态映射
 */
export const MERCHANT_STATUS_MAP = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' },
  3: { label: '已禁用', type: 'info' }
}

/**
 * 账单状态映射
 */
export const BILL_STATUS_MAP = {
  0: { label: '待支付', type: 'warning' },
  1: { label: '已支付', type: 'success' },
  2: { label: '已取消', type: 'info' }
}

/**
 * 内容类型映射
 */
export const CONTENT_TYPE_MAP = {
  SEO: 'SEO内容',
  ACTIVITY: '活动',
  NEWS: '新闻',
  ANNOUNCEMENT: '公告'
}

/**
 * 用户类型映射
 */
export const USER_TYPE_MAP = {
  ADMIN: '管理员',
  MERCHANT: '商家',
  USER: '用户'
}

/**
 * 操作类型映射
 */
export const OPERATION_MAP = {
  CREATE: '创建',
  UPDATE: '更新',
  DELETE: '删除',
  AUDIT: '审核',
  LOGIN: '登录'
}

/**
 * 防抖函数
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 */
export function throttle(fn, delay = 300) {
  let timer = null
  return function(...args) {
    if (timer) return
    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }
}

/**
 * 深拷贝
 */
export function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj)
  if (obj instanceof Array) return obj.map(item => deepClone(item))
  
  const cloneObj = {}
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      cloneObj[key] = deepClone(obj[key])
    }
  }
  return cloneObj
}

