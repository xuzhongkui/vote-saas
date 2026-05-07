<template>
  <div class="invoices-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ $t('merchantInvoices.title') }}</h2>
          <p class="subtitle">{{ $t('merchantInvoices.subtitle') }}</p>
        </div>
      </template>

      <el-table :data="invoices" stripe v-loading="loading">
        <el-table-column prop="invoiceNo" :label="$t('merchantInvoices.invoiceNo')" width="180" />
        <el-table-column prop="invoiceType" :label="$t('merchantInvoices.invoiceType')" width="120">
          <template #default="{ row }">
            <el-tag :type="getInvoiceTypeTag(row.invoiceType)">
              {{ getInvoiceTypeName(row.invoiceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" :label="$t('merchantInvoices.amount')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="taxAmount" :label="$t('merchantInvoices.taxAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.taxAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" :label="$t('merchantInvoices.totalAmount')" width="120">
          <template #default="{ row }">
            <span class="total-amount">¥{{ formatAmount(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceDate" :label="$t('merchantInvoices.invoiceDate')" width="120">
          <template #default="{ row }">
            {{ formatDate(row.invoiceDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('merchantInvoices.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantInvoices.operation')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewInvoice(row)"
              :icon="View"
            >
              {{ $t('merchantInvoices.view') }}
            </el-button>
            <el-button
              v-if="row.pdfUrl"
              type="success"
              size="small"
              @click="downloadInvoice(row)"
              :icon="Download"
            >
              {{ $t('merchantInvoices.download') }}
            </el-button>
            <el-button
              type="info"
              size="small"
              @click="resendEmail(row)"
              :icon="Message"
              :loading="resending === row.id"
            >
              {{ $t('merchantInvoices.resend') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && invoices.length === 0" :description="$t('merchantInvoices.noInvoices')" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { View, Download, Message } from '@element-plus/icons-vue'
import { getMerchantInvoices, resendInvoiceEmail } from '@/api/merchant'
import { useLocaleStore } from '@/stores/locale'

const router = useRouter()
const { t } = useI18n()
const localeStore = useLocaleStore()

const loading = ref(false)
const resending = ref(null)
const invoices = ref([])

// 获取发票列表
const fetchInvoices = async () => {
  loading.value = true
  try {
    const response = await getMerchantInvoices()
    
    if (Array.isArray(response)) {
      invoices.value = response
    } else if (response.data && Array.isArray(response.data)) {
      invoices.value = response.data
    } else {
      invoices.value = []
    }
  } catch (error) {
    console.error('Load invoices failed:', error)
    ElMessage.error(t('merchantInvoices.loadFailed'))
  } finally {
    loading.value = false
  }
}

// 查看发票详情
const viewInvoice = (invoice) => {
  router.push(`/merchant/invoices/${invoice.id}`)
}

// 下载发票
const downloadInvoice = (invoice) => {
  if (!invoice.pdfUrl) {
    ElMessage.warning(t('merchantInvoices.pdfNotReady'))
    return
  }
  
  const downloadUrl = `/api/merchant/invoices/${invoice.id}/pdf`
  const token = localStorage.getItem('token')
  
  if (token) {
    fetch(downloadUrl, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Download failed')
      }
      return response.blob()
    })
    .then(blob => {
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${t('merchantInvoices.invoiceFile')}_${invoice.invoiceNo}.pdf`
      link.style.display = 'none'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success(t('merchantInvoices.downloadSuccess'))
    })
    .catch(error => {
      console.error('Download failed:', error)
      ElMessage.error(t('merchantInvoices.downloadFailed'))
    })
  } else {
    ElMessage.error(t('error.unauthorized'))
  }
}

// 重新发送邮件
const resendEmail = async (invoice) => {
  resending.value = invoice.id
  try {
    await resendInvoiceEmail(invoice.id)
    ElMessage.success(t('merchantInvoices.resendSuccess'))
  } catch (error) {
    ElMessage.error(t('merchantInvoices.resendFailed'))
  } finally {
    resending.value = null
  }
}

// 格式化金额
const formatAmount = (amount) => {
  return parseFloat(amount || 0).toFixed(2)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString(localeStore.isEnglish ? 'en-US' : 'zh-CN')
}

// 获取发票类型名称
const getInvoiceTypeName = (type) => {
  const typeMap = {
    'REGISTRATION': t('merchantInvoices.typeRegistration'),
    'SUBSCRIPTION': t('merchantInvoices.typeSubscription'),
    'SERVICE_FEE': t('merchantInvoices.typeServiceFee'),
    'PLATFORM_FEE': t('merchantInvoices.typePlatformFee'),
    'OTHER': t('merchantInvoices.typeOther')
  }
  return typeMap[type] || type
}

// 获取发票类型标签
const getInvoiceTypeTag = (type) => {
  const tagMap = {
    'REGISTRATION': 'primary',
    'SUBSCRIPTION': 'success',
    'SERVICE_FEE': 'warning',
    'PLATFORM_FEE': 'danger',
    'OTHER': 'info'
  }
  return tagMap[type] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    0: t('merchantInvoices.statusPending'),
    1: t('merchantInvoices.statusGenerated'),
    2: t('merchantInvoices.statusSent'),
    3: t('merchantInvoices.statusCancelled')
  }
  return statusMap[status] || t('common.unknown')
}

// 获取状态标签
const getStatusTag = (status) => {
  const tagMap = {
    0: 'info',
    1: 'success',
    2: 'success',
    3: 'danger'
  }
  return tagMap[status] || 'info'
}

onMounted(() => {
  fetchInvoices()
})
</script>

<style scoped lang="scss">
.invoices-container {
  padding: 20px;
}

.card-header {
  h2 {
    margin: 0 0 5px 0;
    font-size: 20px;
    color: #303133;
  }

  .subtitle {
    margin: 0;
    font-size: 14px;
    color: #909399;
  }
}

.total-amount {
  font-weight: bold;
  color: #f56c6c;
  font-size: 16px;
}
</style>
