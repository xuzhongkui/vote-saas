<template>
  <div class="language-settings-page">
    <el-card class="settings-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon class="header-icon"><Translate /></el-icon>
            <h3>{{ $t('language.title') }}</h3>
          </div>
        </div>
      </template>

      <div class="settings-content">
        <!-- 当前语言显示 -->
        <div class="current-language">
          <div class="label">{{ $t('language.currentLanguage') }}</div>
          <div class="value">
            <span class="flag">{{ currentLocaleInfo.flag }}</span>
            <span class="name">{{ currentLocaleInfo.label }}</span>
          </div>
        </div>

        <!-- 语言切换 -->
        <div class="language-switch">
          <div class="label">{{ $t('language.switchLanguage') }}</div>
          <div class="language-options">
            <div
              v-for="option in localeOptions"
              :key="option.value"
              class="language-option"
              :class="{ active: currentLocale === option.value }"
              @click="handleSwitchLanguage(option.value)"
            >
              <div class="option-content">
                <span class="flag">{{ option.flag }}</span>
                <span class="name">{{ option.label }}</span>
              </div>
              <el-icon v-if="currentLocale === option.value" class="check-icon"><Check /></el-icon>
            </div>
          </div>
        </div>

        <!-- 提示信息 -->
        <el-alert
          :title="$t('language.tip')"
          type="info"
          show-icon
          :closable="false"
          class="tip-alert"
        />
      </div>
    </el-card>

    <!-- 预览效果 -->
    <el-card class="preview-card">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><View /></el-icon>
          <h3>{{ currentLocale === 'zh-CN' ? '预览效果' : 'Preview' }}</h3>
        </div>
      </template>

      <div class="preview-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="$t('common.confirm')">
            <el-button type="primary" size="small">{{ $t('common.confirm') }}</el-button>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('common.cancel')">
            <el-button size="small">{{ $t('common.cancel') }}</el-button>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('common.save')">
            <el-button type="success" size="small">{{ $t('common.save') }}</el-button>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('common.delete')">
            <el-button type="danger" size="small">{{ $t('common.delete') }}</el-button>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('menu.dashboard')">
            {{ $t('menu.dashboard') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('menu.merchants')">
            {{ $t('menu.merchants') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('auth.login')">
            {{ $t('auth.login') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('auth.register')">
            {{ $t('auth.register') }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Translate, Check, View } from '@element-plus/icons-vue'
import { useLocaleStore } from '@/stores/locale'

const { t } = useI18n()
const localeStore = useLocaleStore()

const currentLocale = computed(() => localeStore.currentLocale)
const currentLocaleInfo = computed(() => localeStore.currentLocaleInfo)
const localeOptions = localeStore.localeOptions

const handleSwitchLanguage = async (locale) => {
  if (locale === currentLocale.value) {
    return
  }

  const localeName = locale === 'zh-CN' ? '简体中文' : 'English'
  
  try {
    await ElMessageBox.confirm(
      currentLocale.value === 'zh-CN' 
        ? `确定要将网站语言切换为 ${localeName} 吗？切换后所有用户都将看到对应语言的界面。`
        : `Are you sure you want to switch the website language to ${localeName}? After switching, all users will see the interface in the corresponding language.`,
      currentLocale.value === 'zh-CN' ? '切换语言' : 'Switch Language',
      {
        confirmButtonText: currentLocale.value === 'zh-CN' ? '确定' : 'Confirm',
        cancelButtonText: currentLocale.value === 'zh-CN' ? '取消' : 'Cancel',
        type: 'warning'
      }
    )

    const success = await localeStore.switchLocale(locale, true)
    if (success) {
      ElMessage.success(t('language.switchSuccess'))
    } else {
      ElMessage.error(t('language.switchFailed'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换语言失败:', error)
      ElMessage.error(t('language.switchFailed'))
    }
  }
}
</script>

<style scoped lang="scss">
.language-settings-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;

  .settings-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .header-left {
        display: flex;
        align-items: center;
        gap: 10px;

        .header-icon {
          font-size: 24px;
          color: var(--el-color-primary);
        }

        h3 {
          margin: 0;
          font-size: 18px;
        }
      }
    }
  }

  .settings-content {
    .current-language {
      margin-bottom: 30px;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      color: white;

      .label {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 8px;
      }

      .value {
        display: flex;
        align-items: center;
        gap: 12px;

        .flag {
          font-size: 32px;
        }

        .name {
          font-size: 24px;
          font-weight: 600;
        }
      }
    }

    .language-switch {
      margin-bottom: 30px;

      .label {
        font-size: 16px;
        font-weight: 500;
        margin-bottom: 16px;
        color: var(--el-text-color-primary);
      }

      .language-options {
        display: flex;
        gap: 16px;

        .language-option {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 20px;
          border: 2px solid var(--el-border-color);
          border-radius: 12px;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            border-color: var(--el-color-primary-light-3);
            background-color: var(--el-color-primary-light-9);
          }

          &.active {
            border-color: var(--el-color-primary);
            background-color: var(--el-color-primary-light-9);

            .option-content .name {
              color: var(--el-color-primary);
              font-weight: 600;
            }
          }

          .option-content {
            display: flex;
            align-items: center;
            gap: 12px;

            .flag {
              font-size: 28px;
            }

            .name {
              font-size: 18px;
              color: var(--el-text-color-primary);
            }
          }

          .check-icon {
            font-size: 24px;
            color: var(--el-color-primary);
          }
        }
      }
    }

    .tip-alert {
      margin-top: 20px;
    }
  }

  .preview-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;

      .header-icon {
        font-size: 20px;
        color: var(--el-color-primary);
      }

      h3 {
        margin: 0;
        font-size: 16px;
      }
    }

    .preview-content {
      padding: 10px 0;
    }
  }
}

@media (max-width: 600px) {
  .language-settings-page {
    .settings-content {
      .language-switch {
        .language-options {
          flex-direction: column;
        }
      }
    }
  }
}
</style>

