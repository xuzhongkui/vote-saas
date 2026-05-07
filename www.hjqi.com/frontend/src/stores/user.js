import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

// 安全解析 JSON
function safeParseJSON(str) {
  if (!str || str === 'undefined' || str === 'null') {
    return null
  }
  try {
    return JSON.parse(str)
  } catch (e) {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = ref(safeParseJSON(userInfoStr))
  const userType = ref(localStorage.getItem('userType') || '') // USER, MERCHANT, ADMIN

  // 调试日志
  console.log('🏪 UserStore 初始化')
  console.log('📋 localStorage token:', token.value ? '存在' : '不存在')
  console.log('📋 localStorage userInfo (原始):', userInfoStr)
  console.log('📋 localStorage userInfo (解析后):', userInfo.value)
  console.log('📋 localStorage userType:', userType.value)

  const isLoggedIn = computed(() => !!token.value)
  const isUser = computed(() => userType.value === 'USER')
  const isMerchant = computed(() => userType.value === 'MERCHANT')
  const isAdmin = computed(() => userType.value === 'ADMIN')

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info) {
    userInfo.value = info
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  function setUserType(type) {
    userType.value = type
    localStorage.setItem('userType', type)
  }

  function login(loginData) {
    console.log('🔐 UserStore.login 接收到的数据:', loginData)
    console.log('🔐 Token:', loginData.token)
    console.log('🔐 UserInfo:', loginData.userInfo || loginData.merchant || loginData.admin)
    console.log('🔐 UserType:', loginData.userType)
    
    setToken(loginData.token)
    const userInfoToSave = loginData.userInfo || loginData.merchant || loginData.admin
    console.log('🔐 准备保存的 userInfo:', userInfoToSave)
    setUserInfo(userInfoToSave)
    setUserType(loginData.userType)
    
    console.log('🔐 保存后 - token:', token.value)
    console.log('🔐 保存后 - userInfo:', userInfo.value)
    console.log('🔐 保存后 - userType:', userType.value)
    console.log('🔐 保存后 - localStorage token:', localStorage.getItem('token'))
    console.log('🔐 保存后 - localStorage userInfo:', localStorage.getItem('userInfo'))
    console.log('🔐 保存后 - localStorage userType:', localStorage.getItem('userType'))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    userType.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userType')
  }

  return {
    token,
    userInfo,
    userType,
    isLoggedIn,
    isUser,
    isMerchant,
    isAdmin,
    setToken,
    setUserInfo,
    setUserType,
    login,
    logout
  }
})

