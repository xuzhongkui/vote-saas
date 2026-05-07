<template>
  <div class="merchant-payment-container">
    <el-card class="payment-card">
      <template #header>
        <div class="card-header">
          <h2>{{ $t('merchantPayment.title') }}</h2>
          <p class="subtitle">{{ $t('merchantPayment.subtitle') }}</p>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="paymentInfo" class="payment-content">
        <!-- 商家信息 -->
        <el-descriptions :title="$t('merchantPayment.merchantInfo')" :column="2" border>
          <el-descriptions-item :label="$t('merchantPayment.shopName')">
            {{ merchantInfo.shopNameZh }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantPayment.username')">
            {{ merchantInfo.username }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantPayment.contactName')">
            {{ merchantInfo.contactName }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantPayment.contactPhone')">
            {{ merchantInfo.contactPhone }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 缴费信息 -->
        <el-descriptions :title="$t('merchantPayment.paymentInfo')" :column="1" border class="payment-info">
          <el-descriptions-item :label="$t('merchantPayment.orderNo')">
            {{ paymentInfo.paymentNo }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantPayment.paymentItem')">
            {{ $t('merchantPayment.merchantFee') }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantPayment.amountDue')">
            <span class="amount">¥{{ paymentInfo.amount }}</span>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantPayment.orderStatus')">
            <el-tag :type="getStatusType(paymentInfo.status)">
              {{ getStatusText(paymentInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="$t('merchantPayment.createdAt')">
            {{ formatDateTime(paymentInfo.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="paymentInfo.paidAt" :label="$t('merchantPayment.paidAt')">
            {{ formatDateTime(paymentInfo.paidAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 支付方式选择 -->
        <div v-if="paymentInfo.status === 'PENDING'" class="payment-methods">
          <h3>{{ $t('merchantPayment.selectPaymentMethod') }}</h3>
          <el-radio-group v-model="selectedPaymentMethod" class="method-group">
            <el-radio-button label="alipay">
              <el-icon><CreditCard /></el-icon>
              {{ $t('merchantPayment.alipay') }}
            </el-radio-button>
            <el-radio-button label="wechat">
              <el-icon><Wallet /></el-icon>
              {{ $t('merchantPayment.wechat') }}
            </el-radio-button>
            <el-radio-button label="bank">
              <el-icon><Postcard /></el-icon>
              {{ $t('merchantPayment.bank') }}
            </el-radio-button>
          </el-radio-group>

          <el-button
            type="primary"
            size="large"
            @click="handlePay"
            :loading="paying"
            class="pay-button"
          >
            {{ $t('merchantPayment.payNow') }} ¥{{ paymentInfo.amount }}
          </el-button>
        </div>

        <!-- 已支付提示 -->
        <div v-else-if="paymentInfo.status === 'SUCCESS'" class="payment-success">
          <el-result icon="success" :title="$t('merchantPayment.paymentSuccess')" :sub-title="$t('merchantPayment.accountActivated')">
            <template #extra>
              <el-button type="primary" @click="goToMerchantDashboard">
                {{ $t('merchantPayment.goToDashboard') }}
              </el-button>
            </template>
          </el-result>
        </div>

        <!-- 支付说明 -->
        <el-alert
          v-if="paymentInfo.status === 'PENDING'"
          :title="$t('merchantPayment.paymentNotice')"
          type="info"
          :closable="false"
          class="payment-notice"
        >
          <ul>
            <li>{{ $t('merchantPayment.notice1') }}</li>
            <li>{{ $t('merchantPayment.notice2') }}</li>
            <li>{{ $t('merchantPayment.notice3') }}</li>
            <li>{{ $t('merchantPayment.notice4') }}</li>
          </ul>
        </el-alert>
      </div>

      <div v-else class="no-payment">
        <el-empty :description="$t('merchantPayment.noPaymentOrder')">
          <el-button type="primary" @click="createPaymentOrder">
            {{ $t('merchantPayment.createPaymentOrder') }}
          </el-button>
        </el-empty>
      </div>
    </el-card>

    <!-- 支付二维码对话框 -->
    <el-dialog
      v-model="qrCodeDialogVisible"
      :title="$t('merchantPayment.scanToPay')"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="qrcode-container">
        <div class="qrcode-image">
          <img :src="paymentQrCode" alt="支付二维码" />
        </div>
        <p class="qrcode-tip">
          {{ selectedPaymentMethod === 'alipay' ? $t('merchantPayment.scanWithAlipay') : $t('merchantPayment.scanWithWechat') }}
        </p>
        <p class="amount-tip">{{ $t('merchantPayment.paymentAmount') }}：¥{{ paymentInfo?.amount }}</p>
        <div class="payment-actions">
          <el-button type="primary" @click="checkPaymentStatus" :loading="checking">
            {{ $t('merchantPayment.paymentCompleted') }}
          </el-button>
          <el-button type="success" @click="handleMockPayment" :loading="mocking" style="margin-top: 10px;">
            <el-icon><Check /></el-icon>
            {{ $t('merchantPayment.mockPayment') }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { CreditCard, Wallet, Postcard, Check } from '@element-plus/icons-vue'
import {
  createMerchantPaymentOrder,
  getMerchantPaymentOrder,
  getPaymentOrderStatus,
  getMerchantInfo,
  mockPaymentSuccess
} from '@/api/merchant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()

const loading = ref(false)
const paying = ref(false)
const checking = ref(false)
const mocking = ref(false)
const qrCodeDialogVisible = ref(false)
const selectedPaymentMethod = ref('alipay')
const paymentQrCode = ref('')

const merchantInfo = ref({
  username: '',
  shopNameZh: '',
  contactName: '',
  contactPhone: ''
})

const paymentInfo = ref(null)

// 获取商家ID（可选，后端可以从token获取）
const getMerchantId = () => {
  // 优先从路由参数获取
  let merchantId = route.query.merchantId
  if (merchantId) {
    return merchantId
  }
  
  // 从用户store获取商家ID
  if (userStore.userInfo) {
    merchantId = userStore.userInfo.id || userStore.userInfo.merchantId
  }
  
  // 如果还是没有，返回null，让后端从token获取
  return merchantId || null
}

// 加载商家信息
const loadMerchantInfo = async () => {
  try {
    const response = await getMerchantInfo()
    const info = response.data || response
    if (info) {
      merchantInfo.value = {
        username: info.username || '',
        shopNameZh: info.shopNameZh || '',
        contactName: info.contactName || '',
        contactPhone: info.contactPhone || ''
      }
    }
  } catch (error) {
    console.error('加载商家信息失败:', error)
  }
}

// 获取支付订单信息
const fetchPaymentOrder = async () => {
  loading.value = true
  try {
    const merchantId = getMerchantId()
    const response = await getMerchantPaymentOrder(merchantId)
    
    // 兼容不同的响应格式
    const orderData = response.data || response

    if (orderData && orderData.paymentNo) {
      paymentInfo.value = {
        paymentNo: orderData.paymentNo,
        amount: orderData.amount,
        status: orderData.status || 'PENDING',
        createdAt: orderData.createdAt,
        paidAt: orderData.paidAt,
        description: orderData.description,
        merchantName: orderData.merchantName
      }
      // 如果已支付，提示成功
      if (orderData.status === 'SUCCESS') {
        ElMessage.success(t('merchantPayment.paySuccess'))
      }
    } else {
      // 如果没有支付订单
      paymentInfo.value = null
    }
  } catch (error) {
    console.error('获取支付订单失败:', error)
    // 如果是因为没有订单而失败（404），不显示错误，这是正常的
    if (error.response?.status === 404) {
      // 如果本地没有订单或订单未成功，则置空；否则保留现有成功状态
      if (!paymentInfo.value || paymentInfo.value.status !== 'SUCCESS') {
        paymentInfo.value = null
      }
    } else if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error(t('merchantPayment.pleaseLoginMerchant'))
      router.push('/login')
    } else {
      // 其他错误保留现有信息，避免覆盖成功状态
      if (!paymentInfo.value) {
        ElMessage.error(error.response?.data?.message || t('merchantPayment.loadFailed'))
      }
    }
  } finally {
    loading.value = false
  }
}

// 创建支付订单
const createPaymentOrder = async () => {
  loading.value = true
  try {
    const merchantId = getMerchantId()
    const response = await createMerchantPaymentOrder(merchantId)
    
    // 处理响应数据，兼容不同的数据结构
    const orderData = response.data || response
    
    // 设置支付订单信息
    paymentInfo.value = {
      paymentNo: orderData.paymentNo,
      amount: orderData.amount,
      status: orderData.status || 'PENDING',
      createdAt: orderData.createdAt,
      description: orderData.description,
      merchantName: orderData.merchantName
    }
    
    ElMessage.success(t('merchantPayment.createOrderSuccess'))
    
    // 创建成功后，重新加载商家信息（因为可能有更新）
    await loadMerchantInfo()
  } catch (error) {
    if (error.response?.status === 401 || error.response?.status === 403) {
      ElMessage.error(t('merchantPayment.pleaseLoginMerchant'))
      router.push('/login')
    } else {
      ElMessage.error(error.response?.data?.message || t('merchantPayment.createOrderFailed'))
    }
  } finally {
    loading.value = false
  }
}

// 发起支付
const handlePay = async () => {
  if (!paymentInfo.value || !paymentInfo.value.paymentNo) {
    ElMessage.error(t('merchantPayment.orderInfoIncomplete'))
    return
  }
  
  paying.value = true
  try {
    // 模拟生成支付二维码
    // 实际应该调用支付接口获取二维码
    paymentQrCode.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${paymentInfo.value.paymentNo}`
    qrCodeDialogVisible.value = true
    
    // 开始轮询支付状态
    startPollingPaymentStatus()
  } catch (error) {
    console.error('发起支付失败:', error)
    ElMessage.error('发起支付失败')
  } finally {
    paying.value = false
  }
}

// 检查支付状态
const checkPaymentStatus = async () => {
  checking.value = true
  try {
    const response = await getPaymentOrderStatus(paymentInfo.value.paymentNo)
    // 处理响应数据，兼容不同的数据结构
    const orderData = response.data || response
    
    if (orderData && orderData.status === 'SUCCESS') {
      ElMessage.success(t('merchantPayment.paySuccess'))
      qrCodeDialogVisible.value = false
      paymentInfo.value.status = 'SUCCESS'
      paymentInfo.value.paidAt = orderData.paidAt
      // 停止轮询
      stopPollingPaymentStatus()
      // 重新加载订单信息
      await fetchPaymentOrder()
    } else {
      ElMessage.warning(t('merchantPayment.paymentNotComplete'))
    }
  } catch (error) {
    console.error('查询支付状态失败:', error)
    ElMessage.error(t('merchantPayment.checkStatusFailed'))
  } finally {
    checking.value = false
  }
}

// 轮询支付状态
let pollingTimer = null
const startPollingPaymentStatus = () => {
  if (!paymentInfo.value || !paymentInfo.value.paymentNo) {
    console.warn('无法开始轮询：支付订单信息不完整')
    return
  }
  
  pollingTimer = setInterval(async () => {
    try {
      if (!paymentInfo.value || !paymentInfo.value.paymentNo) {
        stopPollingPaymentStatus()
        return
      }
      
      const response = await getPaymentOrderStatus(paymentInfo.value.paymentNo)
      // 处理响应数据，兼容不同的数据结构
      const orderData = response.data || response
      
      if (orderData && orderData.status === 'SUCCESS') {
        ElMessage.success(t('merchantPayment.paySuccess'))
        qrCodeDialogVisible.value = false
        paymentInfo.value.status = 'SUCCESS'
        paymentInfo.value.paidAt = orderData.paidAt
        stopPollingPaymentStatus()
        // 重新加载订单信息
        await fetchPaymentOrder()
      }
    } catch (error) {
      console.error('轮询支付状态失败:', error)
      // 如果是404错误，停止轮询（订单可能不存在）
      if (error.response?.status === 404) {
        stopPollingPaymentStatus()
      }
    }
  }, 3000) // 每3秒查询一次
}

const stopPollingPaymentStatus = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 模拟支付成功（测试用）
const handleMockPayment = async () => {
  if (!paymentInfo.value || !paymentInfo.value.paymentNo) {
    ElMessage.error(t('merchantPayment.orderInfoIncomplete'))
    return
  }
  
  mocking.value = true
  try {
    await mockPaymentSuccess(paymentInfo.value.paymentNo)
    ElMessage.success(t('merchantPayment.mockPaySuccess'))
    
    // 关闭二维码对话框
    qrCodeDialogVisible.value = false
    
    // 停止轮询
    stopPollingPaymentStatus()
    
    // 重新获取订单信息
    await fetchPaymentOrder()
    
    // 重新加载商家信息（因为缴费状态可能已更新）
    await loadMerchantInfo()
  } catch (error) {
    console.error('模拟支付失败:', error)
    ElMessage.error(error.response?.data?.message || t('merchantPayment.mockPayFailed'))
  } finally {
    mocking.value = false
  }
}

// 前往商家后台
const goToMerchantDashboard = () => {
  router.push('/merchant/dashboard')
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    CANCELLED: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    PENDING: t('merchantPayment.statusPending'),
    SUCCESS: t('merchantPayment.statusSuccess'),
    FAILED: t('merchantPayment.statusFailed'),
    CANCELLED: t('merchantPayment.statusCancelled')
  }
  return textMap[status] || t('merchantPayment.statusUnknown')
}

onMounted(async () => {
  // 检查是否已登录
  if (!userStore.isLoggedIn) {
    ElMessage.error(t('merchantPayment.pleaseLogin'))
    router.push('/login')
    return
  }
  
  // 检查是否是商家账号
  if (!userStore.isMerchant) {
    ElMessage.error(t('merchantPayment.merchantOnly'))
    router.push('/login')
    return
  }
  
  // 加载商家信息和支付订单
  await loadMerchantInfo()
  await fetchPaymentOrder()
})

// 组件卸载时停止轮询
import { onUnmounted } from 'vue'
onUnmounted(() => {
  stopPollingPaymentStatus()
})
</script>

<style scoped lang="scss">
.merchant-payment-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.payment-card {
  max-width: 900px;
  margin: 0 auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .card-header {
    text-align: center;

    h2 {
      margin: 0 0 10px 0;
      color: #303133;
      font-size: 24px;
    }

    .subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.payment-content {
  .payment-info {
    margin-top: 20px;

    .amount {
      font-size: 24px;
      font-weight: bold;
      color: #f56c6c;
    }
  }

  .payment-methods {
    margin-top: 30px;
    padding: 20px;
    background: #f9fafc;
    border-radius: 8px;

    h3 {
      margin: 0 0 20px 0;
      font-size: 16px;
      color: #303133;
    }

    .method-group {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;

      :deep(.el-radio-button__inner) {
        display: flex;
        align-items: center;
        gap: 5px;
      }
    }

    .pay-button {
      width: 100%;
      height: 50px;
      font-size: 18px;
    }
  }

  .payment-success {
    margin-top: 30px;
  }

  .payment-notice {
    margin-top: 20px;

    ul {
      margin: 10px 0 0 0;
      padding-left: 20px;

      li {
        margin: 5px 0;
        color: #606266;
        font-size: 14px;
      }
    }
  }
}

.no-payment {
  padding: 40px 0;
}

.qrcode-container {
  text-align: center;

  .qrcode-image {
    margin: 20px 0;

    img {
      width: 200px;
      height: 200px;
      border: 1px solid #dcdfe6;
      border-radius: 8px;
    }
  }

  .qrcode-tip {
    margin: 10px 0;
    font-size: 14px;
    color: #606266;
  }

  .amount-tip {
    margin: 10px 0 20px 0;
    font-size: 18px;
    font-weight: bold;
    color: #f56c6c;
  }

  .payment-actions {
    margin-top: 20px;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
}

.loading-container {
  padding: 20px;
}
</style>

