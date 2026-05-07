import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

// 获取保存的语言设置，默认中文
const getDefaultLocale = () => {
  const savedLocale = localStorage.getItem('platform_locale')
  if (savedLocale) {
    return savedLocale
  }
  return 'zh-CN'
}

const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: getDefaultLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

export default i18n

// 导出切换语言的方法
export const setLocale = (locale) => {
  i18n.global.locale.value = locale
  localStorage.setItem('platform_locale', locale)
  // 更新 HTML lang 属性
  document.documentElement.lang = locale === 'zh-CN' ? 'zh' : 'en'
}

// 导出获取当前语言的方法
export const getLocale = () => {
  return i18n.global.locale.value
}

// 导出语言列表
export const localeOptions = [
  { value: 'zh-CN', label: '简体中文', flag: '🇨🇳' },
  { value: 'en-US', label: 'English', flag: '🇺🇸' }
]

