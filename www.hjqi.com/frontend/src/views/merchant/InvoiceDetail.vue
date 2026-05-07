<template>
  <div class="invoice-detail-page">
    <el-page-header @back="goBack" :title="$t('merchantInvoices.backButton')">
      <template #content>
        <span class="page-title">{{ $t('merchantInvoices.detailTitle') }}</span>
      </template>
    </el-page-header>

    <el-card v-loading="loading" class="detail-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h2>{{ $t('merchantInvoices.invoiceNo') }}：{{ invoice?.invoiceNo }}</h2>
            <el-tag :type="getStatusTag(invoice?.status)" size="large">
              {{ getStatusName(invoice?.status) }}
            </el-tag>
          </div>
          <div class="header-right">
            <el-button
              v-if="invoice?.pdfUrl"
              type="success"
              :icon="Download"
              @click="downloadInvoice"
            >
              {{ $t('merchantInvoices.downloadPdf') }}
            </el-button>
            <el-button
              type="info"
              :icon="Message"
              @click="resendEmail"
              :loading="resending"
            >
              {{ $t('merchantInvoices.resendEmail') }}
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="invoice" class="invoice-content">
        <!-- 发票信息 -->
        <el-descriptions :title="$t('merchantInvoices.invoiceInfo')" :column="2" border class="info-section">
          <el-descriptions-item :label="$t('merchantInvoices.invoiceNo')" label-class-name="label-bold">
            <span class="invoice-no">{{ invoice.invoiceNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantInvoices.invoiceDate')">
            {{ formatDate(invoice.invoiceDate) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantInvoices.invoiceType')">
            <el-tag :type="getInvoiceTypeTag(invoice.invoiceType)">
              {{ getInvoiceTypeName(invoice.invoiceType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantInvoices.status')">
            <el-tag :type="getStatusTag(invoice.status)">
              {{ getStatusName(invoice.status) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 金额信息 -->
        <el-descriptions :title="$t('merchantInvoices.amountInfo')" :column="1" border class="info-section amount-section">
          <el-descriptions-item :label="$t('merchantInvoices.amount')">
            <span class="amount">¥{{ formatAmount(invoice.amount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantInvoices.taxAmount')">
            <span class="tax-amount">¥{{ formatAmount(invoice.taxAmount) }}</span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantInvoices.priceWithTax')">
            <span class="total-amount">¥{{ formatAmount(invoice.totalAmount) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 购买方信息 -->
        <el-descriptions :title="$t('merchantInvoices.buyerInfo')" :column="2" border class="info-section">
          <el-descriptions-item :label="$t('merchantInvoices.invoiceTitle')" v-if="invoice.invoiceTitle" :span="2">
            {{ invoice.invoiceTitle }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantInvoices.taxNumber')" v-if="invoice.taxNumber" :span="2">
            {{ invoice.taxNumber }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 备注信息 -->
        <el-descriptions
          v-if="invoice.remark"
          :title="$t('merchantInvoices.remarkInfo')"
          :column="1"
          border
          class="info-section"
        >
          <el-descriptions-item :label="$t('merchantInvoices.remark')">
            {{ invoice.remark }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 时间信息 -->
        <el-descriptions :title="$t('merchantInvoices.timeInfo')" :column="2" border class="info-section">
          <el-descriptions-item :label="$t('merchantInvoices.createdAt')">
            {{ formatDateTime(invoice.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantInvoices.updatedAt')">
            {{ formatDateTime(invoice.updatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  Download,
  Message
} from '@element-plus/icons-vue'
import { getInvoiceDetail, resendInvoiceEmail } from '@/api/merchant'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const resending = ref(false)
const invoice = ref(null)

// 获取发票详情
const fetchInvoiceDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const response = await getInvoiceDetail(id)
    invoice.value = response.data || response
    console.log('发票详情:', invoice.value)
  } catch (error) {
    console.error('获取发票详情失败:', error)
    ElMessage.error(error.response?.data?.message || t('merchantInvoices.loadDetailFailed'))
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 下载发票
const downloadInvoice = () => {
  if (!invoice.value?.pdfUrl) {
    ElMessage.warning(t('merchantInvoices.pdfNotReady'))
    return
  }

  const downloadUrl = `/api/merchant/invoices/${invoice.value.id}/pdf`
  const token = localStorage.getItem('token')

  if (token) {
    fetch(downloadUrl, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error('下载失败')
        }
        return response.blob()
      })
      .then((blob) => {
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `Invoice_${invoice.value.invoiceNo}.pdf`
        link.style.display = 'none'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success(t('merchantInvoices.downloadSuccess'))
      })
      .catch((error) => {
        console.error('下载失败:', error)
        ElMessage.error(t('merchantInvoices.downloadFailed'))
      })
  } else {
    ElMessage.error(t('merchantInvoices.pleaseLogin'))
  }
}

// 重新发送邮件
const resendEmail = async () => {
  resending.value = true
  try {
    await resendInvoiceEmail(invoice.value.id)
    ElMessage.success(t('merchantInvoices.resendSuccess'))
  } catch (error) {
    console.error('重新发送邮件失败:', error)
    ElMessage.error(error.response?.data?.message || t('merchantInvoices.resendFailed'))
  } finally {
    resending.value = false
  }
}

// 格式化金额
const formatAmount = (amount) => {
  return parseFloat(amount || 0).toFixed(2)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取发票类型名称
const getInvoiceTypeName = (type) => {
  const typeMap = {
    REGISTRATION: t('merchantInvoices.typeRegistration'),
    SUBSCRIPTION: t('merchantInvoices.typeSubscription'),
    SERVICE_FEE: t('merchantInvoices.typeServiceFee'),
    PLATFORM_FEE: t('merchantInvoices.typePlatformFee'),
    OTHER: t('merchantInvoices.typeOther')
  }
  return typeMap[type] || type
}

// 获取发票类型标签
const getInvoiceTypeTag = (type) => {
  const tagMap = {
    REGISTRATION: 'primary',
    SUBSCRIPTION: 'success',
    SERVICE_FEE: 'warning',
    PLATFORM_FEE: 'danger',
    OTHER: 'info'
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
  return statusMap[status] || t('merchantInvoices.statusUnknown')
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
  fetchInvoiceDetail()
})
</script>

<style scoped lang="scss">
.invoice-detail-page {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  .page-title {
    font-size: 20px;
    font-weight: bold;
    color: #303133;
  }

  .detail-card {
    margin-top: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 15px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 15px;

        h2 {
          margin: 0;
          font-size: 20px;
          color: #303133;
        }
      }

      .header-right {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
      }
    }
  }

  .invoice-content {
    .info-section {
      margin-bottom: 25px;

      &:last-child {
        margin-bottom: 0;
      }

      :deep(.el-descriptions__title) {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
        margin-bottom: 15px;
      }

      :deep(.el-descriptions__label) {
        width: 150px;
        background: #f5f7fa;
        font-weight: 500;
      }

      :deep(.label-bold) {
        font-weight: bold;
      }
    }

    .amount-section {
      background: #f0f9ff;
      border: 2px solid #3b82f6;
      border-radius: 8px;
      padding: 20px;

      :deep(.el-descriptions__title) {
        color: #1e40af;
        font-weight: bold;
      }

      :deep(.el-descriptions__label) {
        background: #dbeafe;
        color: #1e40af;
        font-weight: bold;
      }

      :deep(.el-descriptions__content) {
        background: #fff;
        color: #1e40af;
      }

      .amount,
      .tax-amount,
      .total-amount {
        font-size: 24px;
        font-weight: bold;
        color: #dc2626;
      }

      .total-amount {
        font-size: 28px;
        color: #b91c1c;
      }
    }

    .invoice-no {
      font-size: 16px;
      font-weight: bold;
      color: #e74c3c;
    }
  }
}

@media (max-width: 768px) {
  .invoice-detail-page {
    padding: 10px;

    .detail-card .card-header {
      flex-direction: column;
      align-items: flex-start;

      .header-left {
        width: 100%;
        flex-direction: column;
        align-items: flex-start;
      }

      .header-right {
        width: 100%;

        .el-button {
          flex: 1;
        }
      }
    }

    .invoice-content {
      .info-section {
        :deep(.el-descriptions) {
          font-size: 14px;
        }
      }

      .amount-section {
        .amount,
        .tax-amount {
          font-size: 18px;
        }

        .total-amount {
          font-size: 22px;
        }
      }
    }
  }
}
</style>
