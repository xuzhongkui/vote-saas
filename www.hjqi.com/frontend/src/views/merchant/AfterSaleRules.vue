<template>
  <div class="after-sale-rules-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('merchantAfterSale.title') }}</h3>
          <el-button type="primary" @click="handleCreate">{{ $t('merchantAfterSale.addRule') }}</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('merchantAfterSale.ruleType')">
          <el-select v-model="searchForm.ruleType" :placeholder="$t('merchantAfterSale.selectRuleType')" clearable style="width: 150px">
            <el-option :label="$t('merchantAfterSale.typeReturn')" value="RETURN" />
            <el-option :label="$t('merchantAfterSale.typeExchange')" value="EXCHANGE" />
            <el-option :label="$t('merchantAfterSale.typeRefund')" value="REFUND" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRules">{{ $t('merchantAfterSale.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="ruleList" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('merchantAfterSale.ruleType')" prop="ruleType" width="120">
          <template #default="{ row }">
            {{ getRuleTypeText(row.ruleType) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantAfterSale.titleZh')" prop="titleZh" min-width="150" />
        <el-table-column :label="$t('merchantAfterSale.titleEn')" prop="titleEn" min-width="150" />
        <el-table-column :label="$t('merchantAfterSale.validDays')" prop="validDays" width="120" />
        <el-table-column :label="$t('merchantAfterSale.status')" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? $t('merchantAfterSale.enabled') : $t('merchantAfterSale.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantAfterSale.createdAt')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantAfterSale.operation')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('merchantAfterSale.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">{{ $t('merchantAfterSale.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingRule ? $t('merchantAfterSale.editRule') : $t('merchantAfterSale.addRule')"
      width="700px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item :label="$t('merchantAfterSale.ruleType')" prop="ruleType">
          <el-select v-model="form.ruleType" :placeholder="$t('merchantAfterSale.selectType')">
            <el-option :label="$t('merchantAfterSale.typeReturn')" value="RETURN" />
            <el-option :label="$t('merchantAfterSale.typeExchange')" value="EXCHANGE" />
            <el-option :label="$t('merchantAfterSale.typeRefund')" value="REFUND" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('merchantAfterSale.titleZh')" prop="titleZh">
          <el-input v-model="form.titleZh" :placeholder="$t('merchantAfterSale.inputTitleZh')" />
        </el-form-item>
        <el-form-item :label="$t('merchantAfterSale.titleEn')" prop="titleEn">
          <el-input v-model="form.titleEn" :placeholder="$t('merchantAfterSale.inputTitleEn')" />
        </el-form-item>
        <el-form-item :label="$t('merchantAfterSale.contentZh')" prop="contentZh">
          <el-input
            v-model="form.contentZh"
            type="textarea"
            :rows="4"
            :placeholder="$t('merchantAfterSale.inputContentZh')"
          />
        </el-form-item>
        <el-form-item :label="$t('merchantAfterSale.contentEn')" prop="contentEn">
          <el-input
            v-model="form.contentEn"
            type="textarea"
            :rows="4"
            :placeholder="$t('merchantAfterSale.inputContentEn')"
          />
        </el-form-item>
        <el-form-item :label="$t('merchantAfterSale.validDays')" prop="validDays">
          <el-input-number v-model="form.validDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item :label="$t('merchantAfterSale.status')" prop="status" v-if="editingRule">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ $t('merchantAfterSale.enabled') }}</el-radio>
            <el-radio :label="0">{{ $t('merchantAfterSale.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="saving">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  getAfterSaleRules,
  createAfterSaleRule,
  updateAfterSaleRule,
  deleteAfterSaleRule
} from '@/api/merchant'
import { formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const ruleList = ref([])
const loading = ref(false)
const saving = ref(false)

const searchForm = reactive({
  ruleType: null
})

const dialogVisible = ref(false)
const editingRule = ref(null)
const formRef = ref(null)

const form = reactive({
  ruleType: '',
  titleZh: '',
  titleEn: '',
  contentZh: '',
  contentEn: '',
  validDays: 7,
  status: 1
})

const formRules = computed(() => ({
  ruleType: [{ required: true, message: t('merchantAfterSale.selectType'), trigger: 'change' }],
  titleZh: [{ required: true, message: t('merchantAfterSale.inputTitleZh'), trigger: 'blur' }],
  contentZh: [{ required: true, message: t('merchantAfterSale.inputContentZh'), trigger: 'blur' }],
  validDays: [{ required: true, message: t('merchantAfterSale.inputValidDays'), trigger: 'blur' }]
}))

const getRuleTypeText = (type) => {
  const map = {
    RETURN: t('merchantAfterSale.typeReturn'),
    EXCHANGE: t('merchantAfterSale.typeExchange'),
    REFUND: t('merchantAfterSale.typeRefund')
  }
  return map[type] || type
}

const loadRules = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm
    }
    const res = await getAfterSaleRules(params)
    ruleList.value = res || []
  } catch (error) {
    console.error('Load rules failed:', error)
    ElMessage.error(t('merchantAfterSale.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingRule.value = null
  Object.assign(form, {
    ruleType: '',
    titleZh: '',
    titleEn: '',
    contentZh: '',
    contentEn: '',
    validDays: 7,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (rule) => {
  editingRule.value = rule
  Object.assign(form, {
    ruleType: rule.ruleType,
    titleZh: rule.titleZh,
    titleEn: rule.titleEn,
    contentZh: rule.contentZh,
    contentEn: rule.contentEn,
    validDays: rule.validDays,
    status: rule.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    if (editingRule.value) {
      await updateAfterSaleRule(editingRule.value.id, form)
      ElMessage.success(t('merchantAfterSale.editSuccess'))
    } else {
      await createAfterSaleRule(form)
      ElMessage.success(t('merchantAfterSale.addSuccess'))
    }
    
    dialogVisible.value = false
    loadRules()
  } catch (error) {
    if (error !== false) {
      console.error('Save rule failed:', error)
      ElMessage.error(t('merchantAfterSale.saveFailed'))
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('merchantAfterSale.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteAfterSaleRule(id)
    ElMessage.success(t('merchantAfterSale.deleteSuccess'))
    loadRules()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete rule failed:', error)
      ElMessage.error(t('merchantAfterSale.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadRules()
})
</script>

<style scoped lang="scss">
.after-sale-rules-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }
}
</style>
