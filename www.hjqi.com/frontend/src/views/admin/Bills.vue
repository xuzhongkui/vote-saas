<template>
  <div class="bills-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminBills.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('adminBills.createBill') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('common.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('adminBills.statusPending')" :value="0" />
            <el-option :label="$t('adminBills.statusPaid')" :value="1" />
            <el-option :label="$t('adminBills.statusCancelled')" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadBills">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="bills" v-loading="loading">
        <el-table-column :label="$t('adminBills.billNo')" prop="billNo" width="180" />
        <el-table-column :label="$t('adminBills.merchant')" prop="merchantName" width="150" />
        <el-table-column :label="$t('adminBills.billType')" width="120">
          <template #default="{ row }">
            {{ row.billType === 'SERVICE_FEE' ? $t('adminBills.typeServiceFee') : $t('adminBills.typePlatformFee') }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.amount')" width="120">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminBills.billPeriod')" width="200">
          <template #default="{ row }">
            {{ formatDate(row.startDate) }} ~ {{ formatDate(row.endDate) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="BILL_STATUS_MAP[row.status]?.type">
              {{ getBillStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.createTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">{{ $t('common.detail') }}</el-button>
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

    <!-- 创建账单对话框 -->
    <el-dialog v-model="dialogVisible" :title="$t('adminBills.createBill')" width="600px" @open="loadMerchantList">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('adminBills.selectMerchant')" prop="merchantId">
          <el-select 
            v-model="form.merchantId" 
            :placeholder="$t('adminBills.pleaseSelectMerchant')" 
            filterable
            style="width: 100%"
            :loading="merchantsLoading"
          >
            <el-option
              v-for="merchant in merchantList"
              :key="merchant.id"
              :label="`${merchant.shopNameZh || merchant.username} (ID: ${merchant.id})`"
              :value="merchant.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ merchant.shopNameZh || merchant.username }}</span>
                <el-tag size="small" type="info">ID: {{ merchant.id }}</el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminBills.billType')" prop="billType">
          <el-select v-model="form.billType" :placeholder="$t('adminBills.selectType')" style="width: 100%">
            <el-option :label="$t('adminBills.typeServiceFee')" value="SERVICE_FEE" />
            <el-option :label="$t('adminBills.typePlatformFee')" value="PLATFORM_FEE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.amount')" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :step="0.01" style="width: 200px" />
          <span style="margin-left: 10px">{{ $t('adminBills.yuan') }}</span>
        </el-form-item>
        <el-form-item :label="$t('adminBills.startDate')" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            :placeholder="$t('adminBills.selectStartDate')"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('adminBills.endDate')" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            :placeholder="$t('adminBills.selectEndDate')"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 账单详情对话框 -->
    <el-dialog v-model="detailVisible" :title="$t('adminBills.billDetail')" width="600px">
      <el-descriptions :column="1" border v-if="currentBill">
        <el-descriptions-item :label="$t('adminBills.billNo')">{{ currentBill.billNo }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminBills.merchant')">{{ currentBill.merchantName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminBills.billType')">
          {{ currentBill.billType === 'SERVICE_FEE' ? $t('adminBills.typeServiceFee') : $t('adminBills.typePlatformFee') }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.amount')">
          <span class="amount">¥{{ currentBill.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminBills.billPeriod')">
          {{ formatDate(currentBill.startDate) }} ~ {{ formatDate(currentBill.endDate) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.status')">
          <el-tag :type="BILL_STATUS_MAP[currentBill.status]?.type">
            {{ getBillStatusLabel(currentBill.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.createTime')">
          {{ formatDateTime(currentBill.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminBills.paidAt')" v-if="currentBill.paidAt">
          {{ formatDateTime(currentBill.paidAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminBills.invoice')" v-if="currentBill.invoiceUrl">
          <el-link :href="currentBill.invoiceUrl" target="_blank">{{ $t('adminBills.viewInvoice') }}</el-link>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAdminBills, getAdminBill, createBill, getMerchants } from '@/api/admin'
import { formatDate, formatDateTime, BILL_STATUS_MAP } from '@/utils'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const bills = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  status: null
})

const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 商家列表
const merchantList = ref([])
const merchantsLoading = ref(false)

const form = reactive({
  merchantId: null,
  billType: '',
  amount: 0,
  startDate: '',
  endDate: ''
})

const rules = {
  merchantId: [{ required: true, message: () => t('adminBills.pleaseSelectMerchant'), trigger: 'change' }],
  billType: [{ required: true, message: () => t('adminBills.pleaseSelectType'), trigger: 'change' }],
  amount: [{ required: true, message: () => t('adminBills.pleaseInputAmount'), trigger: 'blur' }],
  startDate: [{ required: true, message: () => t('adminBills.pleaseSelectStartDate'), trigger: 'change' }],
  endDate: [{ required: true, message: () => t('adminBills.pleaseSelectEndDate'), trigger: 'change' }]
}

const getBillStatusLabel = (status) => {
  const statusMap = {
    0: t('adminBills.statusPending'),
    1: t('adminBills.statusPaid'),
    2: t('adminBills.statusCancelled')
  }
  return statusMap[status] || t('common.unknown')
}

const detailVisible = ref(false)
const currentBill = ref(null)

// 加载商家列表
const loadMerchantList = async () => {
  try {
    merchantsLoading.value = true
    const result = await getMerchants({ page: 1, size: 1000, status: 1 }) // 只加载已审核通过的商家
    merchantList.value = result.records || []
  } catch (error) {
    console.error('加载商家列表失败:', error)
    ElMessage.error(t('adminBills.loadMerchantsFailed'))
  } finally {
    merchantsLoading.value = false
  }
}

const loadBills = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const result = await getAdminBills(params)
    bills.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载账单失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, {
    merchantId: null,
    billType: '',
    amount: 0,
    startDate: '',
    endDate: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    await createBill(form)
    ElMessage.success(t('adminBills.createSuccess'))
    dialogVisible.value = false
    loadBills()
  } catch (error) {
    if (error !== false) {
      console.error('创建账单失败:', error)
      ElMessage.error(error.response?.data?.message || t('adminBills.createFailed'))
    }
  } finally {
    submitting.value = false
  }
}

const handleViewDetail = async (bill) => {
  try {
    currentBill.value = await getAdminBill(bill.id)
    detailVisible.value = true
  } catch (error) {
    console.error('加载账单详情失败:', error)
  }
}

onMounted(() => {
  loadBills()
})
</script>

<style scoped lang="scss">
.bills-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
    }
  }

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

