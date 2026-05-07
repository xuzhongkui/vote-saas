<template>
  <div class="risk-control-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminRiskControl.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('adminRiskControl.addRule') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminRiskControl.ruleType')">
          <el-select v-model="searchForm.ruleType" :placeholder="$t('adminRiskControl.selectRuleType')" clearable style="width: 180px">
            <el-option :label="$t('adminRiskControl.typeOrderAmount')" value="ORDER_AMOUNT" />
            <el-option :label="$t('adminRiskControl.typeOrderFrequency')" value="ORDER_FREQUENCY" />
            <el-option :label="$t('adminRiskControl.typeIpBlacklist')" value="IP_BLACKLIST" />
            <el-option :label="$t('adminRiskControl.typeDeviceBlacklist')" value="DEVICE_BLACKLIST" />
            <el-option :label="$t('adminRiskControl.typeUserBlacklist')" value="USER_BLACKLIST" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchForm.enabled" :placeholder="$t('common.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('common.enabled')" :value="1" />
            <el-option :label="$t('common.disabled')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRules">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="rulesList" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('adminRiskControl.ruleName')" prop="ruleName" min-width="200" />
        <el-table-column :label="$t('adminRiskControl.ruleType')" prop="ruleType" width="150" />
        <el-table-column :label="$t('adminRiskControl.ruleConfig')" prop="ruleConfig" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('adminRiskControl.action')" prop="action" width="120">
          <template #default="{ row }">
            <el-tag :type="row.action === 'BLOCK' ? 'danger' : row.action === 'ALERT' ? 'warning' : 'info'">
              {{ getActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminRiskControl.priority')" prop="priority" width="100" />
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :active-value="1"
              :inactive-value="0"
              @change="handleToggle(row.id, row.enabled)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRules"
        @current-change="loadRules"
        class="pagination"
      />
    </el-card>

    <!-- 规则编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingRule ? $t('adminRiskControl.editRule') : $t('adminRiskControl.addRule')"
      width="700px"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item :label="$t('adminRiskControl.ruleName')" prop="ruleName">
          <el-input v-model="form.ruleName" :placeholder="$t('adminRiskControl.inputRuleName')" />
        </el-form-item>
        <el-form-item :label="$t('adminRiskControl.ruleType')" prop="ruleType">
          <el-select v-model="form.ruleType" :placeholder="$t('adminRiskControl.selectRuleType')">
            <el-option :label="$t('adminRiskControl.typeOrderAmount')" value="ORDER_AMOUNT" />
            <el-option :label="$t('adminRiskControl.typeOrderFrequency')" value="ORDER_FREQUENCY" />
            <el-option :label="$t('adminRiskControl.typeIpBlacklist')" value="IP_BLACKLIST" />
            <el-option :label="$t('adminRiskControl.typeDeviceBlacklist')" value="DEVICE_BLACKLIST" />
            <el-option :label="$t('adminRiskControl.typeUserBlacklist')" value="USER_BLACKLIST" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminRiskControl.ruleConfig')" prop="ruleConfig">
          <el-input
            v-model="form.ruleConfig"
            type="textarea"
            :rows="4"
            :placeholder="$t('adminRiskControl.ruleConfigPlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="$t('adminRiskControl.action')" prop="action">
          <el-select v-model="form.action" :placeholder="$t('adminRiskControl.selectAction')">
            <el-option :label="$t('adminRiskControl.actionBlock')" value="BLOCK" />
            <el-option :label="$t('adminRiskControl.actionAlert')" value="ALERT" />
            <el-option :label="$t('adminRiskControl.actionReview')" value="REVIEW" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminRiskControl.priority')" prop="priority">
          <el-input-number v-model="form.priority" :min="1" :max="100" />
        </el-form-item>
        <el-form-item :label="$t('adminRiskControl.description')">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item :label="$t('adminRiskControl.enabled')">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
// 图标已在 main.js 中全局注册，这里不需要导入
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const dialogVisible = ref(false)
const editingRule = ref(null)
const formRef = ref(null)

const searchForm = reactive({
  ruleType: '',
  enabled: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const rulesList = ref([])

const form = reactive({
  ruleName: '',
  ruleType: '',
  ruleConfig: '',
  action: '',
  priority: 1,
  description: '',
  enabled: 1
})

const formRules = {
  ruleName: [{ required: true, message: () => t('adminRiskControl.pleaseInputRuleName'), trigger: 'blur' }],
  ruleType: [{ required: true, message: () => t('adminRiskControl.pleaseSelectRuleType'), trigger: 'change' }],
  ruleConfig: [{ required: true, message: () => t('adminRiskControl.pleaseInputRuleConfig'), trigger: 'blur' }],
  action: [{ required: true, message: () => t('adminRiskControl.pleaseSelectAction'), trigger: 'change' }],
  priority: [{ required: true, message: () => t('adminRiskControl.pleaseInputPriority'), trigger: 'blur' }]
}

const getActionText = (action) => {
  const map = { 
    BLOCK: t('adminRiskControl.actionBlock'), 
    ALERT: t('adminRiskControl.actionAlert'), 
    REVIEW: t('adminRiskControl.actionReview') 
  }
  return map[action] || action
}

const loadRules = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request.get('/admin/risk-control/rules', { params })
    rulesList.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(t('adminRiskControl.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingRule.value = null
  Object.assign(form, {
    ruleName: '',
    ruleType: '',
    ruleConfig: '',
    action: '',
    priority: 1,
    description: '',
    enabled: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingRule.value = row
  Object.assign(form, {
    ruleName: row.ruleName,
    ruleType: row.ruleType,
    ruleConfig: row.ruleConfig,
    action: row.action,
    priority: row.priority,
    description: row.description,
    enabled: row.enabled
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
        if (editingRule.value) {
          await request.put(`/admin/risk-control/rules/${editingRule.value.id}`, form)
          ElMessage.success(t('adminRiskControl.updateSuccess'))
        } else {
          await request.post('/admin/risk-control/rules', form)
          ElMessage.success(t('adminRiskControl.createSuccess'))
        }
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(editingRule.value ? t('adminRiskControl.updateFailed') : t('adminRiskControl.createFailed'))
    }
  }
}

const handleToggle = async (id, enabled) => {
  try {
    await request.put(`/admin/risk-control/rules/${id}/toggle`, null, {
      params: { enabled }
    })
    ElMessage.success(enabled === 1 ? t('adminRiskControl.enabledSuccess') : t('adminRiskControl.disabledSuccess'))
  } catch (error) {
    ElMessage.error(t('adminRiskControl.operationFailed'))
    loadRules()
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('adminRiskControl.deleteConfirm'), t('common.tip'), { type: 'warning' })
    await request.delete(`/admin/risk-control/rules/${id}`)
    ElMessage.success(t('adminRiskControl.deleteSuccess'))
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('adminRiskControl.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped lang="scss">
.risk-control-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>

