<template>
  <div class="emails-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('merchantEmails.title') }}</h3>
          <el-button type="primary" @click="handleCreate">{{ $t('merchantEmails.addEmail') }}</el-button>
        </div>
      </template>

      <el-table :data="emails" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('merchantEmails.emailAddress')" prop="email" min-width="200" />
        <el-table-column :label="$t('merchantEmails.name')" prop="name" width="150" />
        <el-table-column :label="$t('merchantEmails.purpose')" prop="purpose" width="150" />
        <el-table-column :label="$t('merchantEmails.isDefault')" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isDefault === 1" type="success">{{ $t('merchantEmails.yes') }}</el-tag>
            <span v-else>{{ $t('merchantEmails.no') }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantEmails.status')" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? $t('merchantEmails.enabled') : $t('merchantEmails.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantEmails.createdAt')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantEmails.operation')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('merchantEmails.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">{{ $t('merchantEmails.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingEmail ? $t('merchantEmails.editEmail') : $t('merchantEmails.addEmail')"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item :label="$t('merchantEmails.emailAddress')" prop="email">
          <el-input v-model="form.email" :placeholder="$t('merchantEmails.inputEmailAddress')" />
        </el-form-item>
        <el-form-item :label="$t('merchantEmails.name')" prop="name">
          <el-input v-model="form.name" :placeholder="$t('merchantEmails.inputName')" />
        </el-form-item>
        <el-form-item :label="$t('merchantEmails.purpose')" prop="purpose">
          <el-input v-model="form.purpose" :placeholder="$t('merchantEmails.inputPurpose')" />
        </el-form-item>
        <el-form-item :label="$t('merchantEmails.setDefault')">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="$t('merchantEmails.status')" prop="status" v-if="editingEmail">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ $t('merchantEmails.enabled') }}</el-radio>
            <el-radio :label="0">{{ $t('merchantEmails.disabled') }}</el-radio>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  getMerchantEmails,
  createMerchantEmail,
  updateMerchantEmail,
  deleteMerchantEmail
} from '@/api/merchant'
import { formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()

const emails = ref([])
const loading = ref(false)
const saving = ref(false)

const dialogVisible = ref(false)
const editingEmail = ref(null)
const formRef = ref(null)

const form = reactive({
  email: '',
  name: '',
  purpose: '',
  isDefault: 0,
  status: 1
})

const formRules = computed(() => ({
  email: [
    { required: true, message: t('merchantEmails.inputEmailAddress'), trigger: 'blur' },
    { type: 'email', message: t('merchantEmails.emailFormatError'), trigger: 'blur' }
  ],
  name: [{ required: true, message: t('merchantEmails.inputName'), trigger: 'blur' }]
}))

const loadEmails = async () => {
  try {
    loading.value = true
    const res = await getMerchantEmails()
    emails.value = res || []
  } catch (error) {
    console.error('加载邮箱失败:', error)
    ElMessage.error(t('merchantEmails.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingEmail.value = null
  Object.assign(form, {
    email: '',
    name: '',
    purpose: '',
    isDefault: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (email) => {
  editingEmail.value = email
  Object.assign(form, {
    email: email.email,
    name: email.name,
    purpose: email.purpose,
    isDefault: email.isDefault,
    status: email.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    saving.value = true

    if (editingEmail.value) {
      await updateMerchantEmail(editingEmail.value.id, form)
      ElMessage.success(t('merchantEmails.updateSuccess'))
    } else {
      await createMerchantEmail(form)
      ElMessage.success(t('merchantEmails.createSuccess'))
    }

    dialogVisible.value = false
    loadEmails()
  } catch (error) {
    if (error !== false) {
      console.error('保存邮箱失败:', error)
      ElMessage.error(t('merchantEmails.saveFailed'))
    }
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('merchantEmails.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteMerchantEmail(id)
    ElMessage.success(t('merchantEmails.deleteSuccess'))
    loadEmails()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除邮箱失败:', error)
      ElMessage.error(t('merchantEmails.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadEmails()
})
</script>

<style scoped lang="scss">
.emails-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

