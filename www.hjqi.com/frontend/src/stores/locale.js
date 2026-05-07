import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { setLocale as setI18nLocale, getLocale, localeOptions } from '@/i18n'
import request from '@/utils/request'

export const useLocaleStore = defineStore('locale', () => {
  // 当前语言
  const currentLocale = ref(getLocale())
  
  // 是否正在加载
  const loading = ref(false)

  // 当前语言信息
  const currentLocaleInfo = computed(() => {
    return localeOptions.find(item => item.value === currentLocale.value) || localeOptions[0]
  })

  // 是否是中文
  const isChinese = computed(() => currentLocale.value === 'zh-CN')

  // 是否是英文
  const isEnglish = computed(() => currentLocale.value === 'en-US')

  /**
   * 从服务器获取平台语言设置
   */
  async function fetchPlatformLocale() {
    try {
      const res = await request.get('/public/platform-locale')
      if (res && res.locale) {
        await switchLocale(res.locale, false)
      }
    } catch (error) {
      console.warn('获取平台语言设置失败，使用本地设置:', error)
    }
  }

  /**
   * 切换语言
   * @param {string} locale - 语言代码 'zh-CN' 或 'en-US'
   * @param {boolean} saveToServer - 是否保存到服务器
   * @param {string} userType - 用户类型: 'admin' | 'merchant' | 'user'，决定调用哪个API
   */
  async function switchLocale(locale, saveToServer = false, userType = 'admin') {
    if (!localeOptions.find(item => item.value === locale)) {
      console.error('不支持的语言:', locale)
      return false
    }

    loading.value = true
    try {
      // 如果需要保存到服务器
      if (saveToServer) {
        // 根据用户类型调用不同的API
        const apiPath = getApiPath(userType)
        await request.put(apiPath, { locale })
      }

      // 更新本地语言
      currentLocale.value = locale
      setI18nLocale(locale)

      return true
    } catch (error) {
      console.error('切换语言失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  /**
   * 根据用户类型获取API路径
   * @param {string} userType - 用户类型
   */
  function getApiPath(userType) {
    switch (userType) {
      case 'merchant':
        return '/merchant/platform-locale'
      case 'user':
        return '/user/platform-locale'
      case 'admin':
      default:
        return '/admin/platform-locale'
    }
  }

  /**
   * 快捷切换语言（切换中英文）
   * @param {string} userType - 用户类型: 'admin' | 'merchant' | 'user'
   */
  async function toggleLocale(userType = 'admin') {
    const newLocale = isChinese.value ? 'en-US' : 'zh-CN'
    return await switchLocale(newLocale, true, userType)
  }

  /**
   * 初始化语言设置
   * 优先从服务器获取平台设置的语言
   */
  async function initLocale() {
    await fetchPlatformLocale()
  }

  return {
    currentLocale,
    currentLocaleInfo,
    loading,
    isChinese,
    isEnglish,
    localeOptions,
    switchLocale,
    toggleLocale,
    fetchPlatformLocale,
    initLocale
  }
})

