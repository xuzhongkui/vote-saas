<template>
  <div class="orders-page">
    <el-card>
      <template #header>
        <h3>{{ $t('order.myOrders') }}</h3>
      </template>

      <el-table :data="orders" v-loading="loading" :element-loading-text="$t('common.loading')">
        <el-table-column :label="$t('order.orderNo')" prop="orderNo" width="180" />
        <el-table-column :label="$t('product.productName')" min-width="200">
          <template #default="{ row }">
            <div v-for="item in row.items" :key="item.id" class="order-item">
              {{ item.productName }}
              <span v-if="item.skuName">（{{ item.skuName }}）</span>
              x {{ item.quantity }}
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('order.orderAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.status')" width="120">
          <template #default="{ row }">
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-tag :type="ORDER_STATUS_MAP[row.status]?.type">
                {{ ORDER_STATUS_MAP[row.status]?.label }}
              </el-tag>
              <el-tag v-if="row.readOnly" type="info" size="small">{{ $t('order.readOnly') }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('order.orderTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row.id)">{{ $t('common.detail') }}</el-button>
            <el-button
              v-if="row.status === 'PENDING' && !row.readOnly"
              type="success"
              link
              @click="handlePay(row.id)"
            >
              {{ $t('order.payNow') }}
            </el-button>
            <el-button
              v-if="row.status === 'SHIPPING' && !row.readOnly"
              type="warning"
              link
              @click="handleConfirmReceive(row)"
            >
              {{ $t('order.confirmReceive') }}
            </el-button>
            <el-button
              v-if="(row.status === 'PENDING' || row.status === 'CONFIRMED') && !row.readOnly"
              type="danger"
              link
              @click="handleCancelOrder(row)"
            >
              {{ $t('order.cancelOrder') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="orders.length === 0 && !loading" :description="$t('order.noOrders')" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOrders, mockPayOrder, confirmReceive, cancelOrder } from '@/api/user'
import { formatMoney, formatDateTime, ORDER_STATUS_MAP } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const orders = ref([])
const loading = ref(false)

const loadOrders = async () => {
  try {
    loading.value = true
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      ElMessage.warning(t('error.merchantNotFound'))
      return
    }
    orders.value = await getOrders(merchantId)
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/user/orders/${id}`)
}

// 跳转到支付说明页面
const handlePay = (id) => {
  router.push({
    path: '/user/orders/payment',
    query: { orderId: id }
  })
}

// 用户确认收货
const handleConfirmReceive = async (order) => {
  try {
    await ElMessageBox.confirm(t('order.confirmReceiveMessage'), t('order.confirmReceive'), {
      type: 'warning',
      confirmButtonText: t('order.confirmReceive'),
      cancelButtonText: t('common.cancel')
    })
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    await confirmReceive(order.id, merchantId)
    ElMessage.success(t('order.confirmReceiveSuccess'))
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error(t('order.confirmReceiveFailed'))
    }
  }
}

// 用户取消订单
const handleCancelOrder = async (order) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(t('order.cancelReasonPrompt'), t('order.cancelOrder'), {
      confirmButtonText: t('order.confirmCancel'),
      cancelButtonText: t('common.back'),
      inputPlaceholder: t('order.cancelReasonPlaceholder'),
      type: 'warning'
    })
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    await cancelOrder(order.id, merchantId, { cancelReason: reason || '' })
    ElMessage.success(t('order.cancelSuccess'))
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error(t('order.cancelFailed'))
    }
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped lang="scss">
.orders-page {
  max-width: 1200px;
  margin: 0 auto;

  .order-item {
    padding: 5px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }
}
</style>

