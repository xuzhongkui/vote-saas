<template>
  <div class="orders-page">
    <el-card>
      <template #header>
        <h3>{{ $t('merchantOrders.title') }}</h3>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('merchantOrders.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('merchantOrders.selectStatus')" clearable style="width: 180px">
            <el-option :label="$t('merchantOrders.statusPending')" value="PENDING" />
            <el-option :label="$t('merchantOrders.statusConfirmed')" value="CONFIRMED" />
            <el-option :label="$t('merchantOrders.statusShipping')" value="SHIPPING" />
            <el-option :label="$t('merchantOrders.statusCompleted')" value="COMPLETED" />
            <el-option :label="$t('merchantOrders.statusCancelled')" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadOrders">{{ $t('merchantOrders.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="orders" v-loading="loading">
        <el-table-column :label="$t('merchantOrders.orderNo')" prop="orderNo" width="180" />
        <el-table-column :label="$t('merchantOrders.user')" prop="userName" width="120" />
        <el-table-column :label="$t('merchantOrders.productInfo')" min-width="200">
          <template #default="{ row }">
            <div v-for="item in row.items" :key="item.id" class="order-item">
              {{ item.productName }}
              <span v-if="item.skuName">（{{ item.skuName }}）</span>
              x {{ item.quantity }}
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantOrders.totalAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantOrders.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantOrders.pickupDate')" width="120">
          <template #default="{ row }">
            {{ row.expectedDate ? formatDate(row.expectedDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantOrders.createdAt')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantOrders.operation')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">{{ $t('merchantOrders.detail') }}</el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              link
              @click="handleConfirm(row)"
            >
              {{ $t('merchantOrders.confirm') }}
            </el-button>
            <el-button
              v-if="row.status === 'CONFIRMED'"
              type="warning"
              link
              @click="handleShip(row)"
            >
              {{ $t('merchantOrders.ship') }}
            </el-button>
            <el-tag v-if="row.status === 'SHIPPING'" type="info" size="small">
              {{ $t('merchantOrders.waitingReceipt') }}
            </el-tag>
            <el-button
              v-if="row.status !== 'COMPLETED' && row.status !== 'CANCELLED'"
              type="danger"
              link
              @click="handleCancel(row)"
            >
              {{ $t('merchantOrders.cancel') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadOrders"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" :title="$t('merchantOrders.orderDetail')" width="800px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item :label="$t('merchantOrders.orderNo')">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.status')">
          <el-tag :type="getStatusType(currentOrder.status)">
            {{ getStatusLabel(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.user')">{{ currentOrder.userName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.productAmount')">
          ¥{{ formatMoney(currentOrder.productAmount || 0) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.deliveryFee')">
          ¥{{ formatMoney(currentOrder.deliveryFee || 0) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.totalAmount')">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px;">
            ¥{{ formatMoney(currentOrder.totalAmount) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.receiverName')">{{ currentOrder.receiverName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.receiverPhone')">{{ currentOrder.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.receiverAddress')" :span="2">
          {{ currentOrder.receiverAddress }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.deliveryType')" v-if="currentOrder.deliveryType">
          {{ currentOrder.deliveryType === 1 ? $t('merchantOrders.deliveryTypeSelf') : $t('merchantOrders.deliveryTypeDelivery') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.expectedDate')" v-if="currentOrder.expectedDate">
          {{ formatDate(currentOrder.expectedDate) }}
          <span v-if="currentOrder.expectedTime"> {{ currentOrder.expectedTime }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.createdAt')" :span="2">
          {{ formatDateTime(currentOrder.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.userRemark')" :span="2" v-if="currentOrder.userRemark">
          <div style="white-space: pre-wrap; color: #606266;">{{ currentOrder.userRemark }}</div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantOrders.merchantRemark')" :span="2">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span v-if="!editingRemark" style="white-space: pre-wrap; color: #606266;">
              {{ currentOrder.merchantRemark || $t('merchantOrders.noRemark') }}
            </span>
            <el-input
              v-else
              v-model="remarkForm.merchantRemark"
              type="textarea"
              :rows="3"
              :placeholder="$t('merchantOrders.merchantRemark')"
              style="flex: 1;"
            />
            <el-button
              v-if="!editingRemark"
              type="primary"
              link
              size="small"
              @click="startEditRemark"
            >
              {{ currentOrder.merchantRemark ? $t('merchantOrders.editRemark') : $t('merchantOrders.addRemark') }}
            </el-button>
            <div v-else style="display: flex; gap: 8px;">
              <el-button type="primary" size="small" @click="saveRemark">{{ $t('merchantOrders.saveRemark') }}</el-button>
              <el-button size="small" @click="cancelEditRemark">{{ $t('merchantOrders.cancelEdit') }}</el-button>
            </div>
          </div>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">{{ $t('merchantOrders.productInfo') }}</el-divider>
      <el-table :data="currentOrder?.items" border>
        <el-table-column :label="$t('product.productName')" prop="productName" />
        <el-table-column :label="$t('product.specifications')" prop="skuName" />
        <el-table-column :label="$t('product.unitPrice')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.price) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('product.quantity')" prop="quantity" width="100" />
        <el-table-column :label="$t('product.subtotal')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.price * row.quantity) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog v-model="shipVisible" :title="$t('merchantOrders.shipTitle')" width="500px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item :label="$t('merchantOrders.expressCompany')" prop="expressCompany">
          <el-input v-model="shipForm.expressCompany" :placeholder="$t('merchantOrders.inputExpressCompany')" />
        </el-form-item>
        <el-form-item :label="$t('merchantOrders.expressNo')" prop="expressNo">
          <el-input v-model="shipForm.expressNo" :placeholder="$t('merchantOrders.inputExpressNo')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitShip">{{ $t('merchantOrders.confirmShip') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getOrders, getOrder, confirmOrder, shipOrder, cancelOrder, updateOrderRemark } from '@/api/merchant'
import { formatMoney, formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

const orders = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  status: null
})

const detailVisible = ref(false)
const currentOrder = ref(null)
const editingRemark = ref(false)
const remarkForm = reactive({
  merchantRemark: ''
})

const shipVisible = ref(false)
const shipFormRef = ref(null)
const shipForm = reactive({
  expressCompany: '',
  expressNo: ''
})

const shipRules = computed(() => ({
  expressCompany: [{ required: true, message: t('merchantOrders.inputExpressCompany'), trigger: 'blur' }],
  expressNo: [{ required: true, message: t('merchantOrders.inputExpressNo'), trigger: 'blur' }]
}))

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    CONFIRMED: 'primary',
    SHIPPING: 'info',
    COMPLETED: 'success',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    PENDING: t('merchantOrders.statusPending'),
    CONFIRMED: t('merchantOrders.statusConfirmed'),
    SHIPPING: t('merchantOrders.statusShipping'),
    COMPLETED: t('merchantOrders.statusCompleted'),
    CANCELLED: t('merchantOrders.statusCancelled')
  }
  return map[status] || status
}

const loadOrders = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const result = await getOrders(params)
    orders.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('Load orders failed:', error)
    ElMessage.error(t('merchantOrders.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleViewDetail = async (order) => {
  try {
    currentOrder.value = await getOrder(order.id)
    editingRemark.value = false
    remarkForm.merchantRemark = currentOrder.value.merchantRemark || ''
    detailVisible.value = true
  } catch (error) {
    console.error('Load order detail failed:', error)
    ElMessage.error(t('merchantOrders.loadDetailFailed'))
  }
}

const startEditRemark = () => {
  editingRemark.value = true
  remarkForm.merchantRemark = currentOrder.value.merchantRemark || ''
}

const cancelEditRemark = () => {
  editingRemark.value = false
  remarkForm.merchantRemark = currentOrder.value.merchantRemark || ''
}

const saveRemark = async () => {
  try {
    await updateOrderRemark(currentOrder.value.id, {
      merchantRemark: remarkForm.merchantRemark
    })
    ElMessage.success(t('merchantOrders.remarkSaveSuccess'))
    editingRemark.value = false
    currentOrder.value = await getOrder(currentOrder.value.id)
  } catch (error) {
    console.error('Save remark failed:', error)
    ElMessage.error(t('merchantOrders.remarkSaveFailed'))
  }
}

const handleConfirm = async (order) => {
  try {
    await ElMessageBox.confirm(t('merchantOrders.confirmOrderTitle'), t('common.tip'), {
      type: 'warning'
    })
    await confirmOrder(order.id, {})
    ElMessage.success(t('merchantOrders.confirmSuccess'))
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Confirm order failed:', error)
      ElMessage.error(t('merchantOrders.confirmFailed'))
    }
  }
}

const handleShip = (order) => {
  currentOrder.value = order
  Object.assign(shipForm, {
    expressCompany: '',
    expressNo: ''
  })
  shipVisible.value = true
}

const handleCancel = async (order) => {
  try {
    const { value: reason } = await ElMessageBox.prompt(t('merchantOrders.cancelReason'), t('merchantOrders.cancelOrderTitle'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      inputPlaceholder: t('merchantOrders.cancelReasonPlaceholder')
    })
    await cancelOrder(order.id, { cancelReason: reason || '' })
    ElMessage.success(t('merchantOrders.cancelSuccess'))
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Cancel order failed:', error)
      ElMessage.error(t('merchantOrders.cancelFailed'))
    }
  }
}

const handleSubmitShip = async () => {
  try {
    await shipFormRef.value.validate()
    await shipOrder(currentOrder.value.id, shipForm)
    ElMessage.success(t('merchantOrders.shipSuccess'))
    shipVisible.value = false
    loadOrders()
  } catch (error) {
    if (error !== false) {
      console.error('Ship failed:', error)
      ElMessage.error(t('merchantOrders.shipFailed'))
    }
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped lang="scss">
.orders-page {
  .search-form {
    margin-bottom: 20px;
  }

  .order-item {
    padding: 5px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>
