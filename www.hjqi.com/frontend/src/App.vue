<template>
  <el-config-provider :locale="elementPlusLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import { useLocaleStore } from '@/stores/locale'

const localeStore = useLocaleStore()

// 根据当前语言动态切换 Element Plus 的语言
const elementPlusLocale = computed(() => {
  return localeStore.currentLocale === 'en-US' ? en : zhCn
})

// 初始化时从服务器获取平台语言设置
onMounted(async () => {
  await localeStore.initLocale()
})
</script>

<style>
#app {
  width: 100%;
  height: 100%;
}
</style>
