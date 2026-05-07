<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('merchant.todayOrders')" :value="statistics.todayOrderCount || 0">
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('merchant.todaySales')" :value="(parseFloat(statistics.todaySalesAmount || 0) / 100).toFixed(2)" prefix="¥">
            <template #prefix>
              <el-icon><Money /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('merchant.productCount')" :value="statistics.productCount || 0">
            <template #prefix>
              <el-icon><Goods /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('order.pending')" :value="statistics.pendingOrderCount || 0">
            <template #prefix>
              <el-icon><Warning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="8">
        <el-card class="stat-card">
          <el-statistic :title="$t('statistics.weekOrders')" :value="statistics.weekOrderCount || 0">
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <el-statistic :title="$t('statistics.weekSales')" :value="(parseFloat(statistics.weekSalesAmount || 0) / 100).toFixed(2)" prefix="¥">
            <template #prefix>
              <el-icon><Money /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <el-statistic :title="$t('statistics.totalUsers')" :value="statistics.userCount || 0">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>{{ $t('statistics.orderStatusStats') }}</h4>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="$t('merchantOrders.statusPending')">
              <el-tag type="warning">{{ statistics.pendingOrderCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('statistics.confirmedOrders')">
              <el-tag type="primary">{{ statistics.confirmedOrderCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('statistics.shippingOrders')">
              <el-tag type="info">{{ statistics.shippingOrderCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('statistics.completedOrders')">
              <el-tag type="success">{{ statistics.completedOrderCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('statistics.cancelledOrders')">
              <el-tag type="danger">{{ statistics.cancelledOrderCount || 0 }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>{{ $t('statistics.salesStats') }}</h4>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="$t('statistics.monthOrders')">
              {{ statistics.monthOrderCount || 0 }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('statistics.monthSales')">
              ¥{{ formatMoney(statistics.monthSalesAmount || 0) }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('statistics.totalOrders')">
              {{ statistics.totalOrderCount || 0 }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('statistics.totalSales')">
              ¥{{ formatMoney(statistics.totalSalesAmount || 0) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>{{ $t('statistics.recentOrders') }}</h4>
          </template>
          <el-table :data="recentOrders" max-height="400">
            <el-table-column :label="$t('order.orderNo')" prop="orderNo" width="150" />
            <el-table-column :label="$t('statistics.amount')" width="100">
              <template #default="{ row }">
                ¥{{ formatMoney(row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.status')" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)" size="small">
                  {{ getOrderStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('statistics.time')" width="150">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>{{ $t('statistics.storeInfo') }}</h4>
          </template>
          <div class="merchant-info">
            <el-descriptions :column="1" border>
              <el-descriptions-item :label="$t('statistics.inviteCode')">
                <div class="code-display">
                  <span class="code-text">{{ merchantInfo.inviteCode || '-' }}</span>
                  <el-button
                    v-if="merchantInfo.inviteCode"
                    type="primary"
                    size="small"
                    @click="copyInviteCode"
                    :icon="CopyDocument"
                  >
                    {{ $t('statistics.copy') }}
                  </el-button>
                </div>
                <div class="code-tip">{{ $t('statistics.inviteCodeTip') }}</div>
              </el-descriptions-item>
              <el-descriptions-item :label="$t('statistics.promotionCode')">
                <div class="code-display">
                  <span class="code-text">{{ merchantInfo.promotionCode || '-' }}</span>
                  <el-button
                    v-if="merchantInfo.promotionCode"
                    type="primary"
                    size="small"
                    @click="copyPromotionCode"
                    :icon="CopyDocument"
                  >
                    {{ $t('statistics.copy') }}
                  </el-button>
                </div>
                <div class="code-tip">{{ $t('statistics.promotionCodeTip') }}</div>
              </el-descriptions-item>
              <el-descriptions-item :label="$t('statistics.paymentStatus')">
                <el-tag :type="getPaymentStatusType(merchantInfo.paymentStatus)">
                  {{ getPaymentStatusName(merchantInfo.paymentStatus) }}
                </el-tag>
                <el-button
                  v-if="merchantInfo.paymentStatus === 0"
                  type="primary"
                  size="small"
                  link
                  @click="goToPayment"
                  style="margin-left: 10px;"
                >
                  {{ $t('statistics.payNow') }}
                </el-button>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getStatistics, getOrders, getMerchantInfo } from '@/api/merchant'
import { formatMoney, formatDateTime } from '@/utils'
import { CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { t } = useI18n()
const statistics = ref({})
const recentOrders = ref([])
const merchantInfo = ref({
  inviteCode: '',
  promotionCode: '',
  paymentStatus: null
})

const getOrderStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    SHIPPING: 'info',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

const getOrderStatusLabel = (status) => {
  const map = {
    PENDING: t('merchantOrders.statusPending'),
    CONFIRMED: t('merchantOrders.statusConfirmed'),
    SHIPPING: t('merchantOrders.statusShipping'),
    COMPLETED: t('merchantOrders.statusCompleted'),
    CANCELLED: t('merchantOrders.statusCancelled')
  }
  return map[status] || status
}

const loadStatistics = async () => {
  try {
    const stats = await getStatistics()
    statistics.value = stats.data || stats || {}
  } catch (error) {
    console.error('Load statistics failed:', error)
    statistics.value = {
      todayOrderCount: 0,
      todaySalesAmount: 0,
      weekOrderCount: 0,
      weekSalesAmount: 0,
      monthOrderCount: 0,
      monthSalesAmount: 0,
      totalOrderCount: 0,
      totalSalesAmount: 0,
      userCount: 0,
      productCount: 0,
      pendingOrderCount: 0,
      confirmedOrderCount: 0,
      shippingOrderCount: 0,
      completedOrderCount: 0,
      cancelledOrderCount: 0
    }
  }
}

const loadRecentOrders = async () => {
  try {
    const result = await getOrders({ page: 1, size: 10 })
    recentOrders.value = result.data?.records || result.records || []
  } catch (error) {
    console.error('Load recent orders failed:', error)
    recentOrders.value = []
  }
}

const loadMerchantInfo = async () => {
  try {
    const response = await getMerchantInfo()
    const info = response.data || response
    merchantInfo.value = {
      inviteCode: info.inviteCode || '',
      promotionCode: info.promotionCode || '',
      paymentStatus: info.paymentStatus
    }
  } catch (error) {
    console.error('Load merchant info failed:', error)
  }
}

const copyInviteCode = () => {
  if (merchantInfo.value.inviteCode) {
    navigator.clipboard.writeText(merchantInfo.value.inviteCode)
    ElMessage.success(t('common.copySuccess'))
  }
}

const copyPromotionCode = () => {
  if (merchantInfo.value.promotionCode) {
    navigator.clipboard.writeText(merchantInfo.value.promotionCode)
    ElMessage.success(t('common.copySuccess'))
  }
}

const goToPayment = () => {
  router.push('/merchant/payment')
}

const getPaymentStatusType = (status) => {
  if (status === null || status === undefined) return 'info'
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

const getPaymentStatusName = (status) => {
  if (status === null || status === undefined) return t('statistics.notSet')
  const nameMap = {
    0: t('statistics.unpaid'),
    1: t('statistics.paid'),
    2: t('statistics.refunded')
  }
  return nameMap[status] || t('common.unknown')
}

onMounted(() => {
  loadStatistics()
  loadRecentOrders()
  loadMerchantInfo()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  .stat-card {
    :deep(.el-statistic__head) {
      font-size: 14px;
      color: #909399;
    }

    :deep(.el-statistic__content) {
      font-size: 24px;
      font-weight: bold;
    }
  }

  .sales-stats {
    min-height: 300px;
  }

  .merchant-info {
    .code-display {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 5px;

      .code-text {
        flex: 1;
        font-weight: bold;
        color: #409eff;
        font-size: 16px;
      }
    }

    .code-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 5px;
    }
  }
}
</style>
