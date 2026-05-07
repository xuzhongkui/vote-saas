import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import i18n, { getLocale } from './i18n'
import './styles/index.scss'

const app = createApp(App)
const pinia = createPinia()

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 根据当前语言设置 Element Plus 的语言
const getElementPlusLocale = () => {
  const locale = getLocale()
  return locale === 'en-US' ? en : zhCn
}

app.use(pinia)
app.use(router)
app.use(i18n)
app.use(ElementPlus, { locale: getElementPlusLocale() })

app.mount('#app')
