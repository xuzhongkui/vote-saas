<template>
  <div class="bills-page">
    <el-card>
      <template #header>
        <h3>{{ $t('merchantBills.title') }}</h3>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('merchantBills.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('common.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('merchantBills.statusPending')" :value="0" />
            <el-option :label="$t('merchantBills.statusSettled')" :value="1" />
            <el-option :label="$t('common.cancelled')" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBills">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="bills" v-loading="loading">
        <el-table-column :label="$t('merchantBills.billNo')" prop="billNo" width="180" />
        <el-table-column :label="$t('common.type')" width="120">
          <template #default="{ row }">
            {{ row.billType === 'SERVICE_FEE' ? $t('merchantBills.serviceFee') : $t('merchantBills.platformFee') }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.amount')" width="120">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantBills.billPeriod')" width="200">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantBills.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantBills.createdAt')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">{{ $t('common.detail') }}</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handlePay(row)"
            >
              {{ $t('common.pay') }}
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
          @current-change="loadBills"
        />
      </div>
    </el-card>

    <!-- 账单详情对话框 -->
    <el-dialog v-model="detailVisible" :title="$t('merchantBills.detailTitle')" width="600px">
      <el-descriptions :column="1" border v-if="currentBill">
        <el-descriptions-item :label="$t('merchantBills.billNo')">{{ currentBill.billNo }}</el-descriptions-item>
        <el-descriptions-item :label="$t('common.type')">
          {{ currentBill.billType === 'SERVICE_FEE' ? $t('merchantBills.serviceFee') : $t('merchantBills.platformFee') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.amount')">
          <span class="amount">¥{{ currentBill.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantBills.billPeriod')">
          {{ formatDate(currentBill.startDate) }} ~ {{ formatDate(currentBill.endDate) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantBills.status')">
          <el-tag :type="getStatusType(currentBill.status)">
            {{ getStatusText(currentBill.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantBills.createdAt')">
          {{ formatDateTime(currentBill.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantBills.paidAt')" v-if="currentBill.paidAt">
          {{ formatDateTime(currentBill.paidAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantBills.invoice')" v-if="currentBill.invoiceUrl">
          <el-link :href="currentBill.invoiceUrl" target="_blank">{{ $t('merchantBills.viewInvoice') }}</el-link>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getBills, getBill, payBill } from '@/api/merchant'
import { formatDate, formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const bills = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  status: null
})

const detailVisible = ref(false)
const currentBill = ref(null)

const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    0: t('merchantBills.statusPending'),
    1: t('merchantBills.statusSettled'),
    2: t('common.cancelled')
  }
  return map[status] || status
}

const loadBills = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const result = await getBills(params)
    bills.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('Load bills failed:', error)
    ElMessage.error(t('merchantBills.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleViewDetail = async (bill) => {
  try {
    currentBill.value = await getBill(bill.id)
    detailVisible.value = true
  } catch (error) {
    console.error('Load bill detail failed:', error)
  }
}

const handlePay = async (bill) => {
  try {
    await ElMessageBox.confirm(t('merchantBills.payConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await payBill(bill.id)
    ElMessage.success(t('merchantBills.paySuccess'))
    loadBills()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Pay failed:', error)
      ElMessage.error(t('merchantBills.payFailed'))
    }
  }
}

onMounted(() => {
  loadBills()
})
</script>

<style scoped lang="scss">
.bills-page {
  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }

  .amount {
    color: #f56c6c;
    font-size: 18px;
    font-weight: bold;
  }
}
</style>
