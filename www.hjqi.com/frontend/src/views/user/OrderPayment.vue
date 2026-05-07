<template>
  <div class="order-payment-page" v-loading="pageLoading" :element-loading-text="$t('orderPayment.loadingOrder')">
    <el-card>
      <template #header>
        <h3>{{ $t('orderPayment.title') }}</h3>
      </template>

      <div v-if="orderInfo" class="payment-content">
        <!-- 订单信息 -->
        <el-card class="order-info-card" style="margin-bottom: 20px">
          <el-descriptions :column="2" border>
            <el-descriptions-item :label="$t('orderPayment.orderNo')">{{ orderInfo.orderNo }}</el-descriptions-item>
            <el-descriptions-item :label="$t('orderPayment.orderStatus')">
              <el-tag :type="getStatusType(orderInfo.status)">
                {{ getStatusText(orderInfo.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('orderPayment.productAmount')">
              ¥{{ formatMoney(orderInfo.productAmount) }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('orderPayment.deliveryFee')">
              ¥{{ formatMoney(orderInfo.deliveryFee || 0) }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('orderPayment.totalAmount')" :span="2">
              <span class="total-amount">¥{{ formatMoney(orderInfo.totalAmount) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 支付说明 -->
        <el-card class="payment-instruction-card">
          <template #header>
            <h4>{{ $t('orderPayment.paymentInstruction') }}</h4>
          </template>

          <div class="instruction-content">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              style="margin-bottom: 20px"
            >
              <template #title>
                <div class="alert-title">
                  <h4>{{ $t('orderPayment.platformNotice') }}</h4>
                  <p>{{ $t('orderPayment.platformNoticeDesc') }}</p>
                </div>
              </template>
            </el-alert>

            <div class="payment-steps">
              <el-steps :active="currentStep" finish-status="success" align-center>
                <el-step :title="$t('orderPayment.stepOrderSubmitted')" :description="$t('orderPayment.stepOrderSubmittedDesc')" />
                <el-step :title="$t('orderPayment.stepContactMerchant')" :description="$t('orderPayment.stepContactMerchantDesc')" />
                <el-step :title="$t('orderPayment.stepCompletePayment')" :description="$t('orderPayment.stepCompletePaymentDesc')" />
                <el-step :title="$t('orderPayment.stepWaitShipping')" :description="$t('orderPayment.stepWaitShippingDesc')" />
              </el-steps>
            </div>

            <div class="contact-info" style="margin-top: 30px">
              <h4>{{ $t('orderPayment.merchantContact') }}</h4>
              <el-descriptions :column="1" border>
                <el-descriptions-item :label="$t('orderPayment.merchantName')">
                  {{ merchantInfo?.shopNameZh || merchantInfo?.shopNameEn || $t('orderPayment.unknownMerchant') }}
                </el-descriptions-item>
                <el-descriptions-item :label="$t('orderPayment.contactPhone')">
                  <span v-if="merchantInfo?.contactPhone">
                    <a :href="'tel:' + merchantInfo.contactPhone">{{ merchantInfo.contactPhone }}</a>
                  </span>
                  <span v-else>{{ $t('orderPayment.notAvailable') }}</span>
                </el-descriptions-item>
                <el-descriptions-item :label="$t('orderPayment.contactEmail')">
                  <span v-if="merchantInfo?.email">
                    <a :href="'mailto:' + merchantInfo.email">{{ merchantInfo.email }}</a>
                  </span>
                  <span v-else>{{ $t('orderPayment.notAvailable') }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="payment-methods" style="margin-top: 30px">
              <h4>{{ $t('orderPayment.paymentMethods') }}</h4>
              <p class="payment-desc">
                {{ $t('orderPayment.paymentMethodsDesc') }}
              </p>
              <ul class="method-list">
                <li>{{ $t('orderPayment.methodChat') }}</li>
                <li>{{ $t('orderPayment.methodPhone') }}</li>
                <li>{{ $t('orderPayment.methodEmail') }}</li>
              </ul>
            </div>

            <div class="action-buttons" style="margin-top: 30px">
              <el-button type="primary" size="large" @click="openChat">
                <el-icon><ChatLineRound /></el-icon>
                {{ $t('orderPayment.contactService') }}
              </el-button>
              <el-button size="large" @click="goToOrders">
                {{ $t('orderPayment.viewOrderDetail') }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-else :description="$t('orderPayment.loadingOrder')" />
    </el-card>

    <!-- 客服聊天窗口 -->
    <ChatWindow 
      v-if="showChat && orderInfo?.merchantId"
      :merchant-id="orderInfo.merchantId"
      @close="showChat = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { ChatLineRound } from '@element-plus/icons-vue'
import { getOrderDetail, getMerchantInfo } from '@/api/user'
import { formatMoney } from '@/utils'
import ChatWindow from '@/components/ChatWindow.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { t } = useI18n()

const orderInfo = ref(null)
const merchantInfo = ref(null)
const currentStep = ref(1)
const pageLoading = ref(true)

// 获取订单状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'PENDING_PAYMENT': 'warning',
    'CONFIRMED': 'success',
    'SHIPPED': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取订单状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': t('orderPayment.statusPending'),
    'PENDING_PAYMENT': t('orderPayment.statusPendingPayment'),
    'CONFIRMED': t('orderPayment.statusConfirmed'),
    'SHIPPED': t('orderPayment.statusShipped'),
    'COMPLETED': t('orderPayment.statusCompleted'),
    'CANCELLED': t('orderPayment.statusCancelled')
  }
  return statusMap[status] || status
}

// 加载订单信息
const loadOrderInfo = async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.error(t('orderPayment.orderIdRequired'))
    router.push('/user/orders')
    return
  }

  pageLoading.value = true
  try {
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      ElMessage.error(t('orderPayment.merchantNotFound'))
      router.push('/user/orders')
      return
    }

    const order = await getOrderDetail(orderId, merchantId)
    
    if (route.query.productAmount && route.query.deliveryFee && route.query.totalAmount) {
      orderInfo.value = {
        ...order,
        productAmount: Number(route.query.productAmount),
        deliveryFee: Number(route.query.deliveryFee),
        totalAmount: Number(route.query.totalAmount)
      }
    } else {
      orderInfo.value = order
    }

    if (order.merchantId) {
      try {
        const merchant = await getMerchantInfo(order.merchantId)
        merchantInfo.value = merchant
      } catch (error) {
        console.error('Load merchant info failed:', error)
      }
    }

    if (order.status === 'PENDING' || order.status === 'PENDING_PAYMENT') {
      currentStep.value = 1
    } else if (order.status === 'CONFIRMED') {
      currentStep.value = 3
    } else if (order.status === 'SHIPPED') {
      currentStep.value = 4
    }
  } catch (error) {
    console.error('Load order info failed:', error)
    ElMessage.error(t('orderPayment.loadOrderFailed'))
    router.push('/user/orders')
  } finally {
    pageLoading.value = false
  }
}

const showChat = ref(false)

const openChat = () => {
  if (orderInfo.value?.merchantId) {
    showChat.value = true
  } else {
    ElMessage.warning(t('orderPayment.cannotGetMerchant'))
  }
}

const goToOrders = () => {
  if (orderInfo.value?.id) {
    router.push(`/user/orders/${orderInfo.value.id}`)
  } else {
    router.push('/user/orders')
  }
}

onMounted(() => {
  loadOrderInfo()
})
</script>

<style scoped lang="scss">
.order-payment-page {
  max-width: 1000px;
  margin: 0 auto;

  .payment-content {
    .order-info-card {
      .total-amount {
        color: #f56c6c;
        font-size: 20px;
        font-weight: bold;
      }
    }

    .payment-instruction-card {
      .instruction-content {
        .alert-title {
          h4 {
            margin: 0 0 8px 0;
            font-size: 16px;
          }

          p {
            margin: 0;
            font-size: 14px;
            line-height: 1.6;
          }
        }

        .payment-steps {
          margin: 30px 0;
        }

        .contact-info,
        .payment-methods {
          h4 {
            margin: 0 0 16px 0;
            font-size: 16px;
            color: #303133;
          }

          .payment-desc {
            margin: 12px 0;
            color: #606266;
            line-height: 1.6;
          }

          .method-list {
            margin: 12px 0;
            padding-left: 20px;
            color: #606266;
            line-height: 2;

            li {
              margin-bottom: 8px;
            }
          }
        }

        .action-buttons {
          display: flex;
          gap: 16px;
          justify-content: center;
        }
      }
    }
  }
}
</style>
