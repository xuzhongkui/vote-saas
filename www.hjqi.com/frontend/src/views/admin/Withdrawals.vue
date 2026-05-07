<template>
  <div class="withdrawals-page">
    <el-card class="filter-card">
      <template #header>
        <span>{{ $t('adminWithdrawals.title') }}</span>
      </template>
      <el-form :inline="true" :model="query">
        <el-form-item :label="$t('common.status')">
          <el-select v-model="query.status" :placeholder="$t('common.all')" clearable style="width: 160px">
            <el-option :label="$t('adminWithdrawals.statusPending')" :value="0" />
            <el-option :label="$t('adminWithdrawals.statusApproved')" :value="1" />
            <el-option :label="$t('adminWithdrawals.statusTransferred')" :value="2" />
            <el-option :label="$t('adminWithdrawals.statusRejected')" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">{{ $t('common.search') }}</el-button>
          <el-button @click="resetFilters">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" style="margin-top: 16px;">
      <el-table :data="list" border stripe>
        <el-table-column prop="withdrawalNo" :label="$t('adminWithdrawals.withdrawalNo')" min-width="180" />
        <el-table-column prop="merchantName" :label="$t('adminWithdrawals.merchantName')" min-width="160" />
        <el-table-column prop="amount" :label="$t('adminWithdrawals.applyAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="fee" :label="$t('adminWithdrawals.fee')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.fee) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" :label="$t('adminWithdrawals.actualAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.actualAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="accountType" :label="$t('adminWithdrawals.accountType')" width="100">
          <template #default="{ row }">
            <el-tag size="small">
              {{ formatAccountType(row.accountType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="accountName" :label="$t('adminWithdrawals.accountName')" min-width="140" />
        <el-table-column prop="accountNumber" :label="$t('adminWithdrawals.accountNumber')" min-width="160" />
        <el-table-column prop="status" :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('adminWithdrawals.applyTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="auditTime" :label="$t('adminWithdrawals.auditTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.auditTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="transferTime" :label="$t('adminWithdrawals.transferTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.transferTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="260" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="openAuditDialog(row, true)"
            >
              {{ $t('adminWithdrawals.approve') }}
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="openAuditDialog(row, false)"
            >
              {{ $t('adminWithdrawals.reject') }}
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              @click="openTransferDialog(row)"
            >
              {{ $t('adminWithdrawals.confirmTransfer') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditDialogVisible" :title="$t('adminWithdrawals.auditTitle')" width="500px">
      <div v-if="currentRow">
        <p>{{ $t('adminWithdrawals.merchantName') }}：{{ currentRow.merchantName }}</p>
        <p>{{ $t('adminWithdrawals.withdrawalNo') }}：{{ currentRow.withdrawalNo }}</p>
        <p>{{ $t('adminWithdrawals.applyAmount') }}：¥{{ formatAmount(currentRow.amount) }}</p>
        <p>{{ $t('adminWithdrawals.operationType') }}：<strong>{{ auditApproved ? $t('adminWithdrawals.approve') : $t('adminWithdrawals.reject') }}</strong></p>
      </div>
      <el-input
        v-model="auditRemark"
        type="textarea"
        :rows="3"
        :placeholder="$t('adminWithdrawals.auditRemarkPlaceholder')"
        style="margin-top: 12px;"
      />
      <template #footer>
        <el-button @click="auditDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="auditLoading" @click="submitAudit">
          {{ $t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 打款对话框 -->
    <el-dialog v-model="transferDialogVisible" :title="$t('adminWithdrawals.confirmTransfer')" width="500px">
      <div v-if="currentRow">
        <p>{{ $t('adminWithdrawals.merchantName') }}：{{ currentRow.merchantName }}</p>
        <p>{{ $t('adminWithdrawals.withdrawalNo') }}：{{ currentRow.withdrawalNo }}</p>
        <p>{{ $t('adminWithdrawals.actualAmount') }}：¥{{ formatAmount(currentRow.actualAmount) }}</p>
      </div>
      <el-input
        v-model="transferVoucher"
        type="textarea"
        :rows="3"
        :placeholder="$t('adminWithdrawals.transferVoucherPlaceholder')"
        style="margin-top: 12px;"
      />
      <template #footer>
        <el-button @click="transferDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="transferLoading" @click="submitTransfer">
          {{ $t('adminWithdrawals.confirmTransfer') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const query = ref({
  status: null
})

const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const list = ref([])

// 操作相关状态
const currentRow = ref(null)
const auditDialogVisible = ref(false)
const auditApproved = ref(true)
const auditRemark = ref('')
const auditLoading = ref(false)

const transferDialogVisible = ref(false)
const transferVoucher = ref('')
const transferLoading = ref(false)

const fetchData = async () => {
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    if (query.value.status !== null && query.value.status !== undefined) {
      params.status = query.value.status
    }
    const res = await request.get('/admin/promotion/withdrawals', { params })
    const data = res.data || res
    list.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    console.error('获取提现申请失败', error)
    ElMessage.error(error.response?.data?.message || t('adminWithdrawals.loadFailed'))
  }
}

const resetFilters = () => {
  query.value.status = null
  page.value = 1
  fetchData()
}

const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return parseFloat(amount).toFixed(2)
}

const formatDateTime = (val) => {
  if (!val) return '-'
  return new Date(val).toLocaleString('zh-CN')
}

const statusText = (status) => {
  const map = {
    0: t('adminWithdrawals.statusPending'),
    1: t('adminWithdrawals.statusApproved'),
    2: t('adminWithdrawals.statusTransferred'),
    3: t('adminWithdrawals.statusRejected')
  }
  return map[status] || t('common.unknown')
}

const statusType = (status) => {
  const map = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'danger'
  }
  return map[status] || 'info'
}

const formatAccountType = (type) => {
  const map = {
    ALIPAY: t('adminWithdrawals.accountAlipay'),
    WECHAT: t('adminWithdrawals.accountWechat'),
    BANK: t('adminWithdrawals.accountBank')
  }
  return map[type] || type || '-'
}

const openAuditDialog = (row, approved) => {
  currentRow.value = row
  auditApproved.value = approved
  auditRemark.value = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (!currentRow.value) return
  auditLoading.value = true
  try {
    await request.post(`/admin/promotion/withdrawals/${currentRow.value.id}/audit`, {
      approved: auditApproved.value,
      remark: auditRemark.value
    })
    ElMessage.success(auditApproved.value ? t('adminWithdrawals.approveSuccess') : t('adminWithdrawals.rejectSuccess'))
    auditDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('审核提现失败', error)
    ElMessage.error(error.response?.data?.message || t('adminWithdrawals.auditFailed'))
  } finally {
    auditLoading.value = false
  }
}

const openTransferDialog = (row) => {
  currentRow.value = row
  transferVoucher.value = ''
  transferDialogVisible.value = true
}

const submitTransfer = async () => {
  if (!currentRow.value) return
  transferLoading.value = true
  try {
    await request.post(`/admin/promotion/withdrawals/${currentRow.value.id}/transfer`, {
      transferVoucher: transferVoucher.value
    })
    ElMessage.success(t('adminWithdrawals.transferSuccess'))
    transferDialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('打款确认失败', error)
    ElMessage.error(error.response?.data?.message || t('adminWithdrawals.transferFailed'))
  } finally {
    transferLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.withdrawals-page {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}
</style>


