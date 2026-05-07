<template>
  <div class="system-configs-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminSystemConfigs.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('adminSystemConfigs.addConfig') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminSystemConfigs.configGroup')">
          <el-input v-model="searchForm.configGroup" :placeholder="$t('adminSystemConfigs.inputConfigGroup')" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadConfigs">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="configs" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('adminSystemConfigs.configKey')" prop="configKey" width="200" />
        <el-table-column :label="$t('adminSystemConfigs.configValue')" prop="configValue" min-width="200" />
        <el-table-column :label="$t('adminSystemConfigs.configGroup')" prop="configGroup" width="150" />
        <el-table-column :label="$t('adminSystemConfigs.description')" prop="description" min-width="200" />
        <el-table-column :label="$t('common.createTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 配置编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingConfig ? $t('adminSystemConfigs.editConfig') : $t('adminSystemConfigs.addConfig')"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('adminSystemConfigs.configKey')" prop="configKey">
          <el-input v-model="form.configKey" :disabled="!!editingConfig" />
        </el-form-item>
        <el-form-item :label="$t('adminSystemConfigs.configValue')" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('adminSystemConfigs.configGroup')" prop="configGroup">
          <el-input v-model="form.configGroup" />
        </el-form-item>
        <el-form-item :label="$t('adminSystemConfigs.description')" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" />
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
import {
  getSystemConfigs,
  getSystemConfig,
  createSystemConfig,
  updateSystemConfig,
  deleteSystemConfig
} from '@/api/admin'
import { formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const configs = ref([])
const loading = ref(false)

const searchForm = reactive({
  configGroup: ''
})

const dialogVisible = ref(false)
const editingConfig = ref(null)
const formRef = ref(null)

const form = reactive({
  configKey: '',
  configValue: '',
  configGroup: '',
  description: ''
})

const rules = {
  configKey: [{ required: true, message: () => t('adminSystemConfigs.pleaseInputConfigKey'), trigger: 'blur' }],
  configValue: [{ required: true, message: () => t('adminSystemConfigs.pleaseInputConfigValue'), trigger: 'blur' }]
}

const loadConfigs = async () => {
  try {
    loading.value = true
    configs.value = await getSystemConfigs(searchForm.configGroup || null)
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingConfig.value = null
  Object.assign(form, {
    configKey: '',
    configValue: '',
    configGroup: '',
    description: ''
  })
  dialogVisible.value = true
}

const handleEdit = async (config) => {
  editingConfig.value = config
  const detail = await getSystemConfig(config.id)
  Object.assign(form, detail)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (editingConfig.value) {
      await updateSystemConfig(editingConfig.value.id, form)
      ElMessage.success(t('adminSystemConfigs.updateSuccess'))
    } else {
      await createSystemConfig(form)
      ElMessage.success(t('adminSystemConfigs.addSuccess'))
    }
    
    dialogVisible.value = false
    loadConfigs()
  } catch (error) {
    if (error !== false) {
      console.error('保存配置失败:', error)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('adminSystemConfigs.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteSystemConfig(id)
    ElMessage.success(t('adminSystemConfigs.deleteSuccess'))
    loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
.system-configs-page {
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
}
</style>

