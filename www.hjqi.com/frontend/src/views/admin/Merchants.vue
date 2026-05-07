<template>
  <div class="merchants-page">
    <el-card>
      <template #header>
        <h3>{{ $t('adminMerchants.title') }}</h3>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('common.selectStatus')" clearable style="width: 180px">
            <el-option :label="$t('adminMerchants.statusPending')" :value="0" />
            <el-option :label="$t('adminMerchants.statusApproved')" :value="1" />
            <el-option :label="$t('adminMerchants.statusRejected')" :value="2" />
            <el-option :label="$t('adminMerchants.statusDisabled')" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadMerchants">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="merchants" v-loading="loading" stripe border>
        <el-table-column label="ID" prop="id" width="80" fixed="left" />
        <el-table-column :label="$t('adminMerchants.username')" prop="username" width="120" />
        <el-table-column :label="$t('adminMerchants.shopName')" prop="shopNameZh" min-width="150" show-overflow-tooltip />
        <el-table-column :label="$t('adminMerchants.email')" prop="email" width="180" show-overflow-tooltip />
        <el-table-column :label="$t('adminMerchants.phone')" prop="phone" width="120" />
        <el-table-column :label="$t('adminMerchants.contactPerson')" prop="contactName" width="100" />
        <el-table-column :label="$t('adminMerchants.contactPhone')" prop="contactPhone" width="120" />
        <el-table-column :label="$t('adminMerchants.inviteCode')" prop="inviteCode" width="120" />
        <el-table-column :label="$t('adminMerchants.promotionCode')" prop="promotionCode" width="120" />
        <el-table-column :label="$t('adminMerchants.auditStatus')" width="100" fixed="left">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminMerchants.paymentStatus')" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)">
              {{ getPaymentStatusName(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminMerchants.paymentAmount')" width="120">
          <template #default="{ row }">
            <span v-if="row.paymentAmount">¥{{ formatAmount(row.paymentAmount) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminMerchants.auditTime')" width="180">
          <template #default="{ row }">
            {{ row.auditTime ? formatDateTime(row.auditTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminMerchants.registerTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">{{ $t('common.detail') }}</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handleAudit(row, 1)"
            >
              {{ $t('adminMerchants.approve') }}
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              link
              @click="handleAudit(row, 2)"
            >
              {{ $t('adminMerchants.reject') }}
            </el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              @click="handleUpdateStatus(row, 3)"
            >
              {{ $t('common.disable') }}
            </el-button>
            <el-button
              v-if="row.status === 3"
              type="success"
              link
              @click="handleUpdateStatus(row, 1)"
            >
              {{ $t('common.enable') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadMerchants"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 商家详情对话框 -->
    <el-dialog v-model="detailVisible" :title="$t('adminMerchants.merchantDetail')" width="800px">
      <el-descriptions :column="2" border v-if="currentMerchant">
        <el-descriptions-item label="ID">{{ currentMerchant.id }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.username')">{{ currentMerchant.username }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.shopNameZh')">{{ currentMerchant.shopNameZh }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.shopNameEn')">{{ currentMerchant.shopNameEn || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.email')">{{ currentMerchant.email }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.phone')">{{ currentMerchant.phone }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.contactPerson')">{{ currentMerchant.contactName }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.contactPhone')">{{ currentMerchant.contactPhone }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.inviteCode')">{{ currentMerchant.inviteCode }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.promotionCode')">{{ currentMerchant.promotionCode || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.referrerCode')">{{ currentMerchant.referrerCode || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.auditStatus')">
          <el-tag :type="getStatusType(currentMerchant.status)">
            {{ getStatusName(currentMerchant.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.paymentStatus')">
          <el-tag :type="getPaymentStatusType(currentMerchant.paymentStatus)">
            {{ getPaymentStatusName(currentMerchant.paymentStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.paymentAmount')">
          {{ currentMerchant.paymentAmount ? '¥' + formatAmount(currentMerchant.paymentAmount) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.paymentTime')">
          {{ currentMerchant.paymentTime ? formatDateTime(currentMerchant.paymentTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.registerTime')">
          {{ formatDateTime(currentMerchant.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.auditTime')">
          {{ currentMerchant.auditTime ? formatDateTime(currentMerchant.auditTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminMerchants.auditRemark')" :span="2" v-if="currentMerchant.auditRemark">
          {{ currentMerchant.auditRemark }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog v-model="auditVisible" :title="$t('adminMerchants.auditMerchant')" width="500px">
      <el-form ref="auditFormRef" :model="auditForm" label-width="100px">
        <el-form-item :label="$t('adminMerchants.auditResult')">
          <el-radio-group v-model="auditForm.status">
            <el-radio :label="1">{{ $t('adminMerchants.approve') }}</el-radio>
            <el-radio :label="2">{{ $t('adminMerchants.reject') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('adminMerchants.auditRemark')">
          <el-input v-model="auditForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitAudit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getMerchants, getMerchant, auditMerchant, updateMerchantStatus } from '@/api/admin'
import { formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const merchants = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  status: null
})

const detailVisible = ref(false)
const currentMerchant = ref(null)

const auditVisible = ref(false)
const auditFormRef = ref(null)
const auditForm = reactive({
  status: 1,
  remark: ''
})

const loadMerchants = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const result = await getMerchants(params)
    merchants.value = result.data?.records || result.records || []
    total.value = result.data?.total || result.total || 0
  } catch (error) {
    console.error('加载商家失败:', error)
    ElMessage.error(t('adminMerchants.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleSizeChange = () => {
  page.value = 1
  loadMerchants()
}

const handleViewDetail = async (merchant) => {
  try {
    currentMerchant.value = await getMerchant(merchant.id)
    detailVisible.value = true
  } catch (error) {
    console.error('加载商家详情失败:', error)
  }
}

const handleAudit = (merchant, status) => {
  currentMerchant.value = merchant
  auditForm.status = status
  auditForm.remark = ''
  auditVisible.value = true
}

const handleSubmitAudit = async () => {
  try {
    await auditMerchant(currentMerchant.value.id, auditForm)
    ElMessage.success(t('adminMerchants.auditSuccess'))
    auditVisible.value = false
    loadMerchants()
  } catch (error) {
    console.error('审核失败:', error)
  }
}

const handleUpdateStatus = async (merchant, status) => {
  try {
    const action = status === 3 ? t('common.disable') : t('common.enable')
    await ElMessageBox.confirm(t('adminMerchants.confirmStatusChange', { action }), t('common.tip'), {
      type: 'warning'
    })
    await updateMerchantStatus(merchant.id, status)
    ElMessage.success(t('adminMerchants.statusChangeSuccess', { action }))
    loadMerchants()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新状态失败:', error)
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 待审核
    1: 'success',  // 已通过
    2: 'danger',   // 已拒绝
    3: 'info'      // 已禁用
  }
  return typeMap[status] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const nameMap = {
    0: t('adminMerchants.statusPending'),
    1: t('adminMerchants.statusApproved'),
    2: t('adminMerchants.statusRejected'),
    3: t('adminMerchants.statusDisabled')
  }
  return nameMap[status] || t('common.unknown')
}

// 获取缴费状态类型
const getPaymentStatusType = (status) => {
  if (status === null || status === undefined) return 'info'
  const typeMap = {
    0: 'warning',  // 未缴费
    1: 'success',  // 已缴费
    2: 'danger'    // 已退款
  }
  return typeMap[status] || 'info'
}

// 获取缴费状态名称
const getPaymentStatusName = (status) => {
  if (status === null || status === undefined) return t('adminMerchants.paymentNotSet')
  const nameMap = {
    0: t('adminMerchants.paymentUnpaid'),
    1: t('adminMerchants.paymentPaid'),
    2: t('adminMerchants.paymentRefunded')
  }
  return nameMap[status] || t('common.unknown')
}

// 格式化金额
const formatAmount = (amount) => {
  return parseFloat(amount || 0).toFixed(2)
}

onMounted(() => {
  loadMerchants()
})
</script>

<style scoped lang="scss">
.merchants-page {
  .search-form {
    margin-bottom: 20px;

    .el-select {
      width: 180px;
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>

