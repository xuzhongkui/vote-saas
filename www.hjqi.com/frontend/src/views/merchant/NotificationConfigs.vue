<template>
  <div class="notification-configs-page">
    <el-card>
      <template #header>
        <div class="header-content">
          <h3>{{ $t('merchantNotification.title') }}</h3>
          <el-tag type="info">{{ $t('merchantNotification.pushOnlyTag') }}</el-tag>
        </div>
      </template>

      <el-alert
        :title="$t('merchantNotification.noticeTitle')"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <p>{{ $t('merchantNotification.noticeDesc') }}</p>
        <ul style="margin: 8px 0; padding-left: 20px;">
          <li>{{ $t('merchantNotification.noticeChatMessage') }}</li>
          <li>{{ $t('merchantNotification.noticeOrderFlow') }}</li>
        </ul>
      </el-alert>

      <el-table :data="notificationTypes" v-loading="loading">
        <el-table-column :label="$t('merchantNotification.notificationType')" prop="label" width="200" />
        <el-table-column :label="$t('merchantNotification.description')" prop="description" min-width="300" />
        <el-table-column :label="$t('merchantNotification.pushEnabled')" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggle(row)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  getMerchantNotificationConfigsGrouped,
  saveMerchantNotificationConfig
} from '@/api/merchant'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const loading = ref(false)

// 通知类型配置（只保留推送）
const notificationTypes = computed(() => [
  {
    type: 'CHAT_MESSAGE',
    label: t('merchantNotification.typeChatMessage'),
    description: t('merchantNotification.typeChatMessageDesc'),
    enabled: getEnabledStatus('CHAT_MESSAGE')
  },
  {
    type: 'ORDER_NEW',
    label: t('merchantNotification.typeNewOrder'),
    description: t('merchantNotification.typeNewOrderDesc'),
    enabled: getEnabledStatus('ORDER_NEW')
  },
  {
    type: 'ORDER_CONFIRMED',
    label: t('merchantNotification.typeOrderConfirmed'),
    description: t('merchantNotification.typeOrderConfirmedDesc'),
    enabled: getEnabledStatus('ORDER_CONFIRMED')
  },
  {
    type: 'ORDER_SHIPPED',
    label: t('merchantNotification.typeOrderShipped'),
    description: t('merchantNotification.typeOrderShippedDesc'),
    enabled: getEnabledStatus('ORDER_SHIPPED')
  },
  {
    type: 'ORDER_COMPLETED',
    label: t('merchantNotification.typeOrderCompleted'),
    description: t('merchantNotification.typeOrderCompletedDesc'),
    enabled: getEnabledStatus('ORDER_COMPLETED')
  },
  {
    type: 'ORDER_CANCELLED',
    label: t('merchantNotification.typeOrderCancelled'),
    description: t('merchantNotification.typeOrderCancelledDesc'),
    enabled: getEnabledStatus('ORDER_CANCELLED')
  }
])

const enabledStatuses = ref({})

const getEnabledStatus = (type) => {
  return enabledStatuses.value[type] ?? 1
}

const loadConfigs = async () => {
  try {
    loading.value = true
    const res = await getMerchantNotificationConfigsGrouped()
    
    if (res) {
      Object.keys(res).forEach(type => {
        const configs = res[type]
        const pushConfig = configs.find(c => c.notificationChannel === 'PUSH')
        if (pushConfig) {
          enabledStatuses.value[type] = pushConfig.enabled
        }
      })
    }
  } catch (error) {
    console.error('Load configs failed:', error)
    ElMessage.error(t('merchantNotification.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleToggle = async (row) => {
  try {
    await saveMerchantNotificationConfig({
      notificationType: row.type,
      notificationChannel: 'PUSH',
      enabled: row.enabled,
      configJson: null
    })
    enabledStatuses.value[row.type] = row.enabled
    ElMessage.success(t('merchantNotification.saveSuccess'))
  } catch (error) {
    console.error('Update config failed:', error)
    ElMessage.error(t('merchantNotification.saveFailed'))
    enabledStatuses.value[row.type] = row.enabled === 1 ? 0 : 1
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
.notification-configs-page {
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      margin: 0;
    }
  }
  
  :deep(.el-alert) {
    p {
      margin: 0;
      line-height: 1.6;
    }
    
    ul {
      li {
        line-height: 1.8;
      }
    }
  }
}
</style>
