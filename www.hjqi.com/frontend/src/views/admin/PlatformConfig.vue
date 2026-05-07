<template>
  <div class="platform-config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminPlatformConfig.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('adminPlatformConfig.addConfig') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminPlatformConfig.configType')">
          <el-select v-model="searchForm.configType" :placeholder="$t('adminPlatformConfig.selectConfigType')" clearable style="width: 180px">
            <el-option :label="$t('adminPlatformConfig.typeSeo')" value="SEO" />
            <el-option :label="$t('adminPlatformConfig.typePayment')" value="PAYMENT" />
            <el-option :label="$t('adminPlatformConfig.typeRiskControl')" value="RISK_CONTROL" />
            <el-option :label="$t('adminPlatformConfig.typeAppDownload')" value="APP_DOWNLOAD" />
            <el-option :label="$t('adminPlatformConfig.typeBasicInfo')" value="BASIC_INFO" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.configGroup')">
          <el-input v-model="searchForm.configGroup" :placeholder="$t('adminPlatformConfig.inputConfigGroup')" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadConfigs">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="configs" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('adminPlatformConfig.configKey')" prop="configKey" width="200" />
        <el-table-column :label="$t('adminPlatformConfig.nameZh')" prop="nameZh" width="150" />
        <el-table-column :label="$t('adminPlatformConfig.nameEn')" prop="nameEn" width="150" />
        <el-table-column :label="$t('adminPlatformConfig.configValue')" prop="configValue" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('adminPlatformConfig.configType')" prop="configType" width="120" />
        <el-table-column :label="$t('adminPlatformConfig.configGroup')" prop="configGroup" width="150" />
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
        @size-change="loadConfigs"
        @current-change="loadConfigs"
        class="pagination"
      />
    </el-card>

    <!-- 配置编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingConfig ? $t('adminPlatformConfig.editConfig') : $t('adminPlatformConfig.addConfig')"
      width="700px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('adminPlatformConfig.configKey')" prop="configKey">
          <el-input v-model="form.configKey" :disabled="!!editingConfig" :placeholder="$t('adminPlatformConfig.configKeyExample')" />
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.nameZh')" prop="nameZh">
          <el-input v-model="form.nameZh" :placeholder="$t('adminPlatformConfig.inputNameZh')" />
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.nameEn')" prop="nameEn">
          <el-input v-model="form.nameEn" :placeholder="$t('adminPlatformConfig.inputNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.configValue')" prop="configValue">
          <el-input
            v-model="form.configValue"
            type="textarea"
            :rows="4"
            :placeholder="$t('adminPlatformConfig.configValuePlaceholder')"
          />
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.configType')" prop="configType">
          <el-select v-model="form.configType" :placeholder="$t('adminPlatformConfig.selectConfigType')">
            <el-option :label="$t('adminPlatformConfig.typeSeo')" value="SEO" />
            <el-option :label="$t('adminPlatformConfig.typePayment')" value="PAYMENT" />
            <el-option :label="$t('adminPlatformConfig.typeRiskControl')" value="RISK_CONTROL" />
            <el-option :label="$t('adminPlatformConfig.typeAppDownload')" value="APP_DOWNLOAD" />
            <el-option :label="$t('adminPlatformConfig.typeBasicInfo')" value="BASIC_INFO" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.configGroup')" prop="configGroup">
          <el-input v-model="form.configGroup" :placeholder="$t('adminPlatformConfig.configGroupExample')" />
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.description')">
          <el-input v-model="form.description" type="textarea" :rows="2" :placeholder="$t('adminPlatformConfig.inputDescription')" />
        </el-form-item>
        <el-form-item :label="$t('adminPlatformConfig.sort')">
          <el-input-number v-model="form.sort" :min="0" />
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const dialogVisible = ref(false)
const editingConfig = ref(null)
const formRef = ref(null)

const searchForm = reactive({
  configType: '',
  configGroup: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const configs = ref([])

const form = reactive({
  configKey: '',
  configValue: '',
  nameZh: '',
  nameEn: '',
  description: '',
  configType: '',
  configGroup: '',
  sort: 0
})

const rules = {
  configKey: [{ required: true, message: () => t('adminPlatformConfig.pleaseInputConfigKey'), trigger: 'blur' }],
  configValue: [{ required: true, message: () => t('adminPlatformConfig.pleaseInputConfigValue'), trigger: 'blur' }],
  configType: [{ required: true, message: () => t('adminPlatformConfig.pleaseSelectConfigType'), trigger: 'change' }]
}

const loadConfigs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request.get('/admin/platform-config', { params })
    configs.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(t('adminPlatformConfig.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingConfig.value = null
  Object.assign(form, {
    configKey: '',
    configValue: '',
    nameZh: '',
    nameEn: '',
    description: '',
    configType: '',
    configGroup: '',
    sort: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingConfig.value = row
  Object.assign(form, {
    configKey: row.configKey,
    configValue: row.configValue,
    nameZh: row.nameZh,
    nameEn: row.nameEn,
    description: row.description,
    configType: row.configType,
    configGroup: row.configGroup,
    sort: row.sort
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
        if (editingConfig.value) {
          await request.put(`/admin/platform-config/${editingConfig.value.id}`, form)
          ElMessage.success(t('adminPlatformConfig.updateSuccess'))
        } else {
          await request.post('/admin/platform-config', form)
          ElMessage.success(t('adminPlatformConfig.createSuccess'))
        }
    dialogVisible.value = false
    loadConfigs()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(editingConfig.value ? t('adminPlatformConfig.updateFailed') : t('adminPlatformConfig.createFailed'))
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('adminPlatformConfig.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await request.delete(`/admin/platform-config/${id}`)
    ElMessage.success(t('adminPlatformConfig.deleteSuccess'))
    loadConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('adminPlatformConfig.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
.platform-config-page {
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

