<template>
  <div class="shipping-templates-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('merchantShipping.title') }}</h3>
          <el-button type="primary" @click="handleCreate">{{ $t('merchantShipping.addTemplate') }}</el-button>
        </div>
      </template>

      <el-table :data="templates" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('merchantShipping.templateName')" prop="name" min-width="150" />
        <el-table-column :label="$t('merchantShipping.defaultFee')" width="120">
          <template #default="{ row }">
            <span class="fee-text">¥{{ (row.defaultFee / 100).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantShipping.freeShippingAmount')" width="150">
          <template #default="{ row }">
            <span v-if="row.freeShippingAmount" class="free-text">
              {{ $t('merchantShipping.freeShippingText', { amount: (row.freeShippingAmount / 100).toFixed(2) }) }}
            </span>
            <span v-else class="no-free">{{ $t('merchantShipping.noFreeShipping') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantShipping.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? $t('merchantShipping.enabled') : $t('merchantShipping.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantShipping.isDefault')" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="success">{{ $t('merchantShipping.yes') }}</el-tag>
            <span v-else>{{ $t('merchantShipping.no') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantShipping.operation')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('merchantShipping.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">{{ $t('merchantShipping.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingTemplate ? $t('merchantShipping.editTemplate') : $t('merchantShipping.addTemplate')"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item :label="$t('merchantShipping.templateName')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('merchantShipping.inputTemplateName')" />
        </el-form-item>
        <el-form-item :label="$t('merchantShipping.defaultFee')" prop="defaultFee">
          <el-input-number v-model="form.defaultFee" :min="0" :precision="2" />
          <span style="margin-left: 10px">{{ $t('merchantShipping.yuan') }}</span>
        </el-form-item>
        <el-form-item :label="$t('merchantShipping.freeShippingAmount')">
          <el-input-number v-model="form.freeShippingAmount" :min="0" :precision="2" />
          <span style="margin-left: 10px">{{ $t('merchantShipping.yuan') }}</span>
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            {{ $t('merchantShipping.freeShippingTip') }}
          </div>
        </el-form-item>
        <el-form-item :label="$t('merchantShipping.setDefault')">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            {{ $t('merchantShipping.defaultTip') }}
          </div>
        </el-form-item>
        <el-form-item :label="$t('merchantShipping.status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ $t('merchantShipping.enabled') }}</el-radio>
            <el-radio :label="0">{{ $t('merchantShipping.disabled') }}</el-radio>
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
  getShippingTemplates,
  createShippingTemplate,
  updateShippingTemplate,
  deleteShippingTemplate
} from '@/api/merchant'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const templates = ref([])
const loading = ref(false)
const saving = ref(false)

const dialogVisible = ref(false)
const editingTemplate = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  defaultFee: 0,
  freeShippingAmount: null,
  isDefault: 0,
  status: 1
})

const formRules = computed(() => ({
  name: [{ required: true, message: t('merchantShipping.inputTemplateName'), trigger: 'blur' }],
  defaultFee: [{ required: true, message: t('merchantShipping.inputDefaultFee'), trigger: 'blur' }]
}))

const loadTemplates = async () => {
  try {
    loading.value = true
    const res = await getShippingTemplates()
    templates.value = res || []
  } catch (error) {
    console.error('Load templates failed:', error)
    ElMessage.error(t('merchantShipping.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingTemplate.value = null
  Object.assign(form, {
    name: '',
    defaultFee: 0,
    freeShippingAmount: null,
    isDefault: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (template) => {
  editingTemplate.value = template
  Object.assign(form, {
    name: template.name,
    defaultFee: template.defaultFee ? template.defaultFee / 100 : 0,
    freeShippingAmount: template.freeShippingAmount ? template.freeShippingAmount / 100 : null,
    isDefault: template.isDefault,
    status: template.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    
    const data = {
      name: form.name,
      type: 'FIXED',
      defaultFee: Math.round(form.defaultFee * 100),
      freeShippingAmount: form.freeShippingAmount ? Math.round(form.freeShippingAmount * 100) : null,
      isDefault: form.isDefault,
      status: form.status
    }
    
    if (editingTemplate.value) {
      await updateShippingTemplate(editingTemplate.value.id, data)
      ElMessage.success(t('merchantShipping.editSuccess'))
    } else {
      await createShippingTemplate(data)
      ElMessage.success(t('merchantShipping.addSuccess'))
    }
    
    dialogVisible.value = false
    loadTemplates()
  } catch (error) {
    if (error !== false) {
      console.error('Save template failed:', error)
      ElMessage.error(t('merchantShipping.saveFailed'))
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('merchantShipping.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteShippingTemplate(id)
    ElMessage.success(t('merchantShipping.deleteSuccess'))
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete template failed:', error)
      ElMessage.error(t('merchantShipping.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped lang="scss">
.shipping-templates-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .fee-text {
    color: #f56c6c;
    font-weight: bold;
  }

  .free-text {
    color: #67c23a;
  }

  .no-free {
    color: #909399;
  }
}
</style>
