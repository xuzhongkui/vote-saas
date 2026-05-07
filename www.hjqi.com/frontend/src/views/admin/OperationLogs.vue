<template>
  <div class="logs-page">
    <el-card>
      <template #header>
        <h3>{{ $t('adminOperationLogs.title') }}</h3>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminOperationLogs.userType')">
          <el-select v-model="searchForm.userType" :placeholder="$t('adminOperationLogs.selectUserType')" clearable style="width: 150px">
            <el-option :label="$t('adminOperationLogs.userTypeAdmin')" value="ADMIN" />
            <el-option :label="$t('adminOperationLogs.userTypeMerchant')" value="MERCHANT" />
            <el-option :label="$t('adminOperationLogs.userTypeUser')" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminOperationLogs.module')">
          <el-select v-model="searchForm.module" :placeholder="$t('adminOperationLogs.selectModule')" clearable style="width: 150px">
            <el-option :label="$t('adminOperationLogs.moduleSystem')" value="SYSTEM" />
            <el-option :label="$t('adminOperationLogs.moduleMerchant')" value="MERCHANT" />
            <el-option :label="$t('adminOperationLogs.moduleOrder')" value="ORDER" />
            <el-option :label="$t('adminOperationLogs.moduleProduct')" value="PRODUCT" />
            <el-option :label="$t('adminOperationLogs.moduleConfig')" value="CONFIG" />
            <el-option :label="$t('adminOperationLogs.moduleUser')" value="USER" />
            <el-option :label="$t('adminOperationLogs.moduleWithdrawal')" value="WITHDRAWAL" />
            <el-option :label="$t('adminOperationLogs.moduleBill')" value="BILL" />
            <el-option :label="$t('adminOperationLogs.moduleCategory')" value="CATEGORY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadLogs">{{ $t('common.search') }}</el-button>
          <el-button @click="resetSearch">{{ $t('common.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('adminOperationLogs.userType')" width="100">
          <template #default="{ row }">
            <el-tag :type="USER_TYPE_TAG[row.userType]" size="small">
              {{ getUserTypeLabel(row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminOperationLogs.username')" prop="username" width="120" />
        <el-table-column :label="$t('adminOperationLogs.operation')" width="100">
          <template #default="{ row }">
            <el-tag :type="OPERATION_TAG[row.operation]" size="small">
              {{ getOperationLabel(row.operation) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminOperationLogs.module')" width="100">
          <template #default="{ row }">
            {{ getModuleLabel(row.module) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminOperationLogs.description')" prop="description" min-width="200" />
        <el-table-column :label="$t('adminOperationLogs.requestUrl')" prop="requestUrl" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('adminOperationLogs.requestMethod')" prop="requestMethod" width="100">
          <template #default="{ row }">
            <el-tag :type="METHOD_TAG[row.requestMethod]" size="small">
              {{ row.requestMethod }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminOperationLogs.ipAddress')" prop="ipAddress" width="130" />
        <el-table-column :label="$t('adminOperationLogs.operationTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadLogs"
          @size-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getLogs } from '@/api/admin'
import { formatDateTime } from '@/utils'

const { t } = useI18n()
const logs = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  userType: null,
  module: null
})

// 用户类型映射
const getUserTypeLabel = (type) => {
  const map = {
    ADMIN: t('adminOperationLogs.userTypeAdmin'),
    MERCHANT: t('adminOperationLogs.userTypeMerchant'),
    USER: t('adminOperationLogs.userTypeUser'),
    UNKNOWN: t('common.unknown')
  }
  return map[type] || type
}

const USER_TYPE_TAG = {
  ADMIN: 'danger',
  MERCHANT: 'warning',
  USER: 'success',
  UNKNOWN: 'info'
}

// 操作类型映射
const getOperationLabel = (operation) => {
  const map = {
    CREATE: t('adminOperationLogs.operationCreate'),
    UPDATE: t('adminOperationLogs.operationUpdate'),
    DELETE: t('adminOperationLogs.operationDelete'),
    AUDIT: t('adminOperationLogs.operationAudit'),
    LOGIN: t('adminOperationLogs.operationLogin'),
    QUERY: t('adminOperationLogs.operationQuery'),
    EXPORT: t('adminOperationLogs.operationExport')
  }
  return map[operation] || operation
}

const OPERATION_TAG = {
  CREATE: 'success',
  UPDATE: 'warning',
  DELETE: 'danger',
  AUDIT: 'primary',
  LOGIN: 'info',
  QUERY: '',
  EXPORT: 'info'
}

// 模块映射
const getModuleLabel = (module) => {
  const map = {
    SYSTEM: t('adminOperationLogs.moduleSystem'),
    MERCHANT: t('adminOperationLogs.moduleMerchant'),
    ORDER: t('adminOperationLogs.moduleOrder'),
    PRODUCT: t('adminOperationLogs.moduleProduct'),
    CONFIG: t('adminOperationLogs.moduleConfig'),
    USER: t('adminOperationLogs.moduleUser'),
    WITHDRAWAL: t('adminOperationLogs.moduleWithdrawal'),
    BILL: t('adminOperationLogs.moduleBill'),
    CATEGORY: t('adminOperationLogs.moduleCategory'),
    SKU: 'SKU'
  }
  return map[module] || module
}

// 请求方法标签
const METHOD_TAG = {
  GET: 'info',
  POST: 'success',
  PUT: 'warning',
  DELETE: 'danger'
}

const loadLogs = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const result = await getLogs(params)
    logs.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载日志失败:', error)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.userType = null
  searchForm.module = null
  page.value = 1
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped lang="scss">
.logs-page {
  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>

