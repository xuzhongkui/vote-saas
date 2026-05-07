<template>
  <div class="order-detail-page" v-loading="loading" :element-loading-text="$t('common.loading')">
    <el-card v-if="order">
      <template #header>
        <div class="card-header">
          <div>
            <h3>{{ $t('order.orderDetail') }}</h3>
            <el-alert
              v-if="order.readOnly"
              type="info"
              :closable="false"
              show-icon
              style="margin-top: 8px;"
            >
              <template #title>
                {{ $t('order.readOnlyMessage') }}
              </template>
            </el-alert>
          </div>
          <el-tag :type="ORDER_STATUS_MAP[order.status]?.type" size="large">
            {{ ORDER_STATUS_MAP[order.status]?.label }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('order.orderNo')">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item :label="$t('order.orderTime')">
          {{ formatDateTime(order.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('order.orderAmount')">
          <span class="amount">¥{{ formatMoney(order.totalAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('order.paymentTime')" v-if="order.paidAt">
          {{ formatDateTime(order.paidAt) }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider :content-position="'left'">{{ $t('order.receiverInfo') }}</el-divider>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="$t('order.receiver')">{{ order.receiverName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('order.receiverPhone')">{{ order.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item :label="$t('order.receiverAddress')">{{ order.receiverAddress }}</el-descriptions-item>
      </el-descriptions>

      <el-divider :content-position="'left'">{{ $t('product.productInfo') }}</el-divider>
      <el-table :data="order.items" border>
        <el-table-column :label="$t('product.productName')" prop="productName" />
        <el-table-column :label="$t('product.specifications')" prop="skuName" />
        <el-table-column :label="$t('product.unitPrice')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.price) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('product.quantity')" prop="quantity" width="100" />
        <el-table-column :label="$t('cart.subtotal')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.subtotal) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="actions" v-if="order.status === 'PENDING' && !order.readOnly">
        <el-button type="primary" size="large" @click="handlePay">{{ $t('order.payNow') }}</el-button>
      </div>

      <div class="actions">
        <el-button @click="$router.back()">{{ $t('common.back') }}</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getOrderDetail, mockPayOrder } from '@/api/user'
import { formatMoney, formatDateTime, ORDER_STATUS_MAP } from '@/utils'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const order = ref(null)
const loading = ref(true)

const loadOrder = async () => {
  loading.value = true
  try {
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      ElMessage.warning(t('error.merchantNotFound'))
      router.back()
      return
    }
    order.value = await getOrderDetail(route.params.id, merchantId)
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error(t('order.notFound'))
    router.back()
  } finally {
    loading.value = false
  }
}

// 跳转到支付说明页面
const handlePay = () => {
  router.push({
    path: '/user/orders/payment',
    query: { orderId: order.value.id }
  })
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped lang="scss">
.order-detail-page {
  max-width: 1200px;
  margin: 0 auto;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
    }
  }

  .amount {
    color: #f56c6c;
    font-size: 18px;
    font-weight: bold;
  }

  .actions {
    margin-top: 20px;
    text-align: right;
  }
}
</style>

