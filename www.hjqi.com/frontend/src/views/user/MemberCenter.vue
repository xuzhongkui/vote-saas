<template>
  <div class="member-center-page">
    <el-row :gutter="20">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="6">
        <el-card class="user-card">
          <div class="user-avatar">
            <el-avatar :size="80" :src="userInfo.avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
          </div>
          <div class="user-name">{{ userInfo.nickname || userInfo.username }}</div>
          <div class="user-meta">
            <div class="meta-item">
              <span class="label">{{ $t('profile.username') }}：</span>
              <span>{{ userInfo.username }}</span>
            </div>
            <div class="meta-item" v-if="merchantInfo">
              <span class="label">{{ $t('menu.bindMerchant') }}：</span>
              <span>{{ merchantInfo.shopNameZh || merchantInfo.shopName }}</span>
            </div>
          </div>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="quick-actions" style="margin-top: 20px">
          <template #header>
            <span>{{ $t('common.operation') }}</span>
          </template>
          <el-menu>
            <el-menu-item @click="$router.push('/user/profile')">
              <el-icon><User /></el-icon>
              <span>{{ $t('menu.profile') }}</span>
            </el-menu-item>
            <el-menu-item @click="$router.push('/user/addresses')">
              <el-icon><Location /></el-icon>
              <span>{{ $t('menu.addresses') }}</span>
            </el-menu-item>
            <el-menu-item @click="$router.push('/user/orders')">
              <el-icon><Document /></el-icon>
              <span>{{ $t('menu.orders') }}</span>
            </el-menu-item>
            <el-menu-item @click="showChangeMerchantDialog = true">
              <el-icon><Switch /></el-icon>
              <span>{{ $t('common.changeMerchant') }}</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>

      <!-- 右侧：统计信息 -->
      <el-col :span="18">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ orderStats.total }}</div>
              <div class="stat-label">{{ $t('memberCenter.totalOrders') }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ orderStats.pending }}</div>
              <div class="stat-label">{{ $t('memberCenter.pendingPayment') }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ orderStats.shipped }}</div>
              <div class="stat-label">{{ $t('memberCenter.pendingReceive') }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-value">{{ orderStats.completed }}</div>
              <div class="stat-label">{{ $t('memberCenter.completed') }}</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 消费统计 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <span>{{ $t('memberCenter.consumeStats') }}</span>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="consume-stat">
                <div class="stat-label">{{ $t('memberCenter.totalSpent') }}</div>
                <div class="stat-value primary">¥{{ formatMoney(totalSpent) }}</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="consume-stat">
                <div class="stat-label">{{ $t('memberCenter.avgOrderAmount') }}</div>
                <div class="stat-value success">
                  ¥{{ orderStats.completed > 0 ? formatMoney(Math.floor(totalSpent / orderStats.completed)) : '0.00' }}
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 最近订单 -->
        <el-card style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>{{ $t('memberCenter.recentOrders') }}</span>
              <el-button type="primary" link @click="$router.push('/user/orders')">{{ $t('memberCenter.viewAll') }}</el-button>
            </div>
          </template>
          <el-table :data="recentOrders" v-loading="loading">
            <el-table-column :label="$t('memberCenter.orderNo')" prop="orderNo" width="180" />
            <el-table-column :label="$t('memberCenter.productInfo')" min-width="200">
              <template #default="{ row }">
                <div v-for="item in row.items" :key="item.id" class="order-item">
                  {{ item.productName }}
                  <span v-if="item.skuName">（{{ item.skuName }}）</span>
                  x {{ item.quantity }}
                </div>
              </template>
            </el-table-column>
            <el-table-column :label="$t('memberCenter.totalAmount')" width="120">
              <template #default="{ row }">
                ¥{{ formatMoney(row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('memberCenter.status')" width="120">
              <template #default="{ row }">
                <el-tag :type="ORDER_STATUS_MAP[row.status]?.type">
                  {{ ORDER_STATUS_MAP[row.status]?.label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('memberCenter.orderTime')" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('memberCenter.operation')" width="120">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewOrderDetail(row.id)">{{ $t('memberCenter.viewDetail') }}</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 更换商家对话框 -->
    <el-dialog v-model="showChangeMerchantDialog" :title="$t('memberCenter.changeMerchantTitle')" width="500px">
      <el-form ref="changeMerchantFormRef" :model="changeMerchantForm" :rules="changeMerchantRules" label-width="100px">
        <el-form-item :label="$t('memberCenter.inviteCode')" prop="inviteCode">
          <el-input v-model="changeMerchantForm.inviteCode" :placeholder="$t('memberCenter.inviteCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('memberCenter.orScanQr')">
          <div class="qr-code-section">
            <el-button @click="showQrCodeScanner = true">{{ $t('memberCenter.scanQrCode') }}</el-button>
            <div v-if="qrCodeImage" class="qr-preview">
              <img :src="qrCodeImage" alt="QR Code" />
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangeMerchantDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleChangeMerchant" :loading="changing">{{ $t('memberCenter.confirmChange') }}</el-button>
      </template>
    </el-dialog>

    <!-- 二维码扫描对话框 -->
    <el-dialog v-model="showQrCodeScanner" :title="$t('memberCenter.scanQrCode')" width="400px">
      <div class="scanner-placeholder">
        <p>{{ $t('memberCenter.scanQrHint') }}</p>
        <p class="hint">{{ $t('memberCenter.scanQrSubHint') }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { getProfile, getOrders, changeMerchant, getMerchantInfo } from '@/api/user'
import { formatMoney, formatDateTime, ORDER_STATUS_MAP } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Location, Document, Switch } from '@element-plus/icons-vue'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const userInfo = ref({})
const merchantInfo = ref(null)
const recentOrders = ref([])
const orderStats = reactive({
  total: 0,
  pending: 0,
  shipped: 0,
  completed: 0
})

const totalSpent = computed(() => {
  return recentOrders.value
    .filter(o => o.status === 'COMPLETED')
    .reduce((sum, order) => sum + (order.totalAmount || 0), 0)
})

const showChangeMerchantDialog = ref(false)
const showQrCodeScanner = ref(false)
const changing = ref(false)
const changeMerchantFormRef = ref(null)
const changeMerchantForm = reactive({
  inviteCode: ''
})
const changeMerchantRules = computed(() => ({
  inviteCode: [
    { required: true, message: t('memberCenter.pleaseInputInviteCode'), trigger: 'blur' }
  ]
}))
const qrCodeImage = ref(null)

const loadUserInfo = async () => {
  try {
    const profile = await getProfile()
    userInfo.value = profile
    
    if (profile.merchantId) {
      try {
        merchantInfo.value = await getMerchantInfo(profile.merchantId)
      } catch (error) {
        console.error('Load merchant info failed:', error)
      }
    }
  } catch (error) {
    console.error('Load user info failed:', error)
  }
}

const loadRecentOrders = async () => {
  try {
    loading.value = true
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      console.warn('merchantId not found, cannot load orders')
      return
    }
    
    const orders = await getOrders(merchantId, 1, 100)
    const orderList = orders.records || orders || []
    
    recentOrders.value = orderList.slice(0, 5)
    
    orderStats.total = orderList.length
    orderStats.pending = orderList.filter(o => o.status === 'PENDING' || o.status === 'PENDING_PAYMENT').length
    orderStats.shipped = orderList.filter(o => o.status === 'SHIPPING' || o.status === 'SHIPPED').length
    orderStats.completed = orderList.filter(o => o.status === 'COMPLETED').length
  } catch (error) {
    console.error('Load orders failed:', error)
  } finally {
    loading.value = false
  }
}

const handleChangeMerchant = async () => {
  try {
    await changeMerchantFormRef.value.validate()
    
    await ElMessageBox.confirm(
      t('memberCenter.changeWarning'), 
      t('common.tip'), 
      {
        type: 'warning',
        confirmButtonText: t('memberCenter.confirmChange'),
        cancelButtonText: t('common.cancel')
      }
    )
    
    changing.value = true
    const result = await changeMerchant(changeMerchantForm)
    
    if (result) {
      if (result.token) {
        userStore.setToken(result.token)
      }
      if (result.userInfo) {
        userStore.setUserInfo({ ...userStore.userInfo, ...result.userInfo })
      }
    }
    
    ElMessage.success(t('memberCenter.changeSuccess'))
    showChangeMerchantDialog.value = false
    changeMerchantForm.inviteCode = ''
    
    await loadUserInfo()
    await loadRecentOrders()
    
    ElMessage.info(t('memberCenter.refreshTip'))
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Change merchant failed:', error)
      ElMessage.error(error.response?.data?.message || t('memberCenter.changeFailed'))
    }
  } finally {
    changing.value = false
  }
}

const viewOrderDetail = (orderId) => {
  router.push(`/user/orders/${orderId}`)
}

onMounted(() => {
  loadUserInfo()
  loadRecentOrders()
})
</script>

<style scoped lang="scss">
.member-center-page {
  max-width: 1200px;
  margin: 0 auto;

  .user-card {
    text-align: center;

    .user-avatar {
      margin-bottom: 16px;
    }

    .user-name {
      font-size: 18px;
      font-weight: bold;
      margin-bottom: 16px;
    }

    .user-meta {
      .meta-item {
        margin-bottom: 8px;
        font-size: 14px;
        color: #606266;

        .label {
          color: #909399;
        }
      }
    }
  }

  .quick-actions {
    :deep(.el-menu-item) {
      height: 48px;
      line-height: 48px;
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;

      .stat-value {
        font-size: 32px;
        font-weight: bold;
        color: #409eff;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .consume-stat {
    text-align: center;
    padding: 20px;

    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 12px;
    }

    .stat-value {
      font-size: 28px;
      font-weight: bold;

      &.primary {
        color: #409eff;
      }

      &.success {
        color: #67c23a;
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .order-item {
    margin-bottom: 4px;
  }

  .qr-code-section {
    .qr-preview {
      margin-top: 16px;
      text-align: center;

      img {
        max-width: 200px;
        max-height: 200px;
      }
    }
  }

  .scanner-placeholder {
    text-align: center;
    padding: 40px;

    .hint {
      margin-top: 16px;
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>
