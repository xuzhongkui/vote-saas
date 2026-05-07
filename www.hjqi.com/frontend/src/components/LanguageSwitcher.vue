<template>
  <el-dropdown 
    :placement="placement" 
    @command="handleLanguageChange"
    :disabled="loading"
  >
    <el-button :size="size" :loading="loading">
      <span>{{ currentLocaleInfo.flag }} {{ currentLocaleInfo.label }}</span>
    </el-button>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item 
          v-for="locale in localeOptions" 
          :key="locale.value"
          :command="locale.value"
          :disabled="locale.value === currentLocale"
        >
          <span style="margin-right: 8px;">{{ locale.flag }}</span>
          {{ locale.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLocaleStore } from '@/stores/locale'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  userType: {
    type: String,
    default: 'user',
    validator: (value) => ['admin', 'merchant', 'user'].includes(value)
  },
  placement: {
    type: String,
    default: 'bottom',
    validator: (value) => ['top', 'bottom', 'left', 'right'].includes(value)
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['large', 'default', 'small'].includes(value)
  }
})

// Composables
const { t } = useI18n()
const localeStore = useLocaleStore()

// Computed
const currentLocale = computed(() => localeStore.currentLocale)
const currentLocaleInfo = computed(() => localeStore.currentLocaleInfo)
const localeOptions = computed(() => localeStore.localeOptions)
const loading = computed(() => localeStore.loading)

// Methods
const handleLanguageChange = async (locale) => {
  try {
    const success = await localeStore.switchLocale(locale, true, props.userType)
    if (success) {
      ElMessage.success(t('language.switchSuccess'))
    } else {
      ElMessage.error(t('language.switchFailed'))
    }
  } catch (error) {
    console.error('Language switch error:', error)
    ElMessage.error(t('language.switchFailed'))
  }
}
</script>

<style scoped>
/* Add any custom styles if needed */
</style>
