<template>
  <div class="app-download-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminAppDownload.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('adminAppDownload.addVersion') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminAppDownload.platform')">
          <el-select v-model="searchForm.platform" :placeholder="$t('adminAppDownload.selectPlatform')" clearable style="width: 150px">
            <el-option label="iOS" value="IOS" />
            <el-option label="Android" value="ANDROID" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchForm.enabled" :placeholder="$t('common.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('common.enabled')" :value="1" />
            <el-option :label="$t('common.disabled')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAppDownloads">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="appDownloads" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('adminAppDownload.platform')" prop="platform" width="100" />
        <el-table-column :label="$t('adminAppDownload.version')" prop="version" width="120" />
        <el-table-column :label="$t('adminAppDownload.versionName')" prop="versionName" width="150" />
        <el-table-column :label="$t('adminAppDownload.downloadUrl')" prop="downloadUrl" min-width="200" show-overflow-tooltip />
        <el-table-column :label="$t('adminAppDownload.fileSize')" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminAppDownload.forceUpdate')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.forceUpdate === 1 ? 'danger' : 'info'">
              {{ row.forceUpdate === 1 ? $t('common.yes') : $t('common.no') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'">
              {{ row.enabled === 1 ? $t('common.enabled') : $t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminAppDownload.publishTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
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
        @size-change="loadAppDownloads"
        @current-change="loadAppDownloads"
        class="pagination"
      />
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingApp ? $t('adminAppDownload.editVersion') : $t('adminAppDownload.addVersion')"
      width="700px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('adminAppDownload.platform')" prop="platform">
          <el-select v-model="form.platform" :placeholder="$t('adminAppDownload.selectPlatform')">
            <el-option label="iOS" value="IOS" />
            <el-option label="Android" value="ANDROID" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.version')" prop="version">
          <el-input v-model="form.version" :placeholder="$t('adminAppDownload.versionExample')" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.versionName')">
          <el-input v-model="form.versionName" :placeholder="$t('adminAppDownload.versionNameExample')" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.downloadUrl')" prop="downloadUrl">
          <el-input v-model="form.downloadUrl" :placeholder="$t('adminAppDownload.inputDownloadUrl')" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.updateNotesZh')">
          <el-input v-model="form.updateNotesZh" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.updateNotesEn')">
          <el-input v-model="form.updateNotesEn" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.fileSizeBytes')">
          <el-input-number v-model="form.fileSize" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.fileMd5')">
          <el-input v-model="form.fileMd5" :placeholder="$t('adminAppDownload.inputFileMd5')" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.forceUpdate')">
          <el-switch v-model="form.forceUpdate" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.enabled')">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="$t('adminAppDownload.publishTime')">
          <el-date-picker
            v-model="form.publishTime"
            type="datetime"
            :placeholder="$t('adminAppDownload.selectPublishTime')"
            style="width: 100%"
          />
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
const editingApp = ref(null)
const formRef = ref(null)

const searchForm = reactive({
  platform: '',
  enabled: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const appDownloads = ref([])

const form = reactive({
  platform: '',
  version: '',
  versionName: '',
  downloadUrl: '',
  updateNotesZh: '',
  updateNotesEn: '',
  forceUpdate: 0,
  fileSize: null,
  fileMd5: '',
  enabled: 1,
  publishTime: null
})

const rules = {
  platform: [{ required: true, message: () => t('adminAppDownload.pleaseSelectPlatform'), trigger: 'change' }],
  version: [{ required: true, message: () => t('adminAppDownload.pleaseInputVersion'), trigger: 'blur' }],
  downloadUrl: [{ required: true, message: () => t('adminAppDownload.pleaseInputDownloadUrl'), trigger: 'blur' }]
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  const kb = bytes / 1024
  const mb = kb / 1024
  if (mb >= 1) {
    return `${mb.toFixed(2)} MB`
  }
  return `${kb.toFixed(2)} KB`
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const loadAppDownloads = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request.get('/admin/app-download', { params })
    appDownloads.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(t('adminAppDownload.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingApp.value = null
  Object.assign(form, {
    platform: '',
    version: '',
    versionName: '',
    downloadUrl: '',
    updateNotesZh: '',
    updateNotesEn: '',
    forceUpdate: 0,
    fileSize: null,
    fileMd5: '',
    enabled: 1,
    publishTime: null
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingApp.value = row
  Object.assign(form, {
    platform: row.platform,
    version: row.version,
    versionName: row.versionName,
    downloadUrl: row.downloadUrl,
    updateNotesZh: row.updateNotesZh,
    updateNotesEn: row.updateNotesEn,
    forceUpdate: row.forceUpdate,
    fileSize: row.fileSize,
    fileMd5: row.fileMd5,
    enabled: row.enabled,
    publishTime: row.publishTime ? new Date(row.publishTime) : null
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const submitData = {
      ...form,
      publishTime: form.publishTime ? form.publishTime.toISOString() : null
    }
        if (editingApp.value) {
          await request.put(`/admin/app-download/${editingApp.value.id}`, submitData)
          ElMessage.success(t('adminAppDownload.updateSuccess'))
        } else {
          await request.post('/admin/app-download', submitData)
          ElMessage.success(t('adminAppDownload.createSuccess'))
        }
    dialogVisible.value = false
    loadAppDownloads()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(editingApp.value ? t('adminAppDownload.updateFailed') : t('adminAppDownload.createFailed'))
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('adminAppDownload.deleteConfirm'), t('common.tip'), { type: 'warning' })
    await request.delete(`/admin/app-download/${id}`)
    ElMessage.success(t('adminAppDownload.deleteSuccess'))
    loadAppDownloads()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('adminAppDownload.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadAppDownloads()
})
</script>

<style scoped lang="scss">
.app-download-page {
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

