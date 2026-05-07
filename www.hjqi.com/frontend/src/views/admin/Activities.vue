<template>
  <div class="activities-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminActivities.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('adminActivities.addActivity') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminActivities.activityType')">
          <el-select v-model="searchForm.activityType" :placeholder="$t('adminActivities.selectType')" clearable style="width: 150px">
            <el-option :label="$t('adminActivities.typeDiscount')" value="DISCOUNT" />
            <el-option :label="$t('adminActivities.typeCoupon')" value="COUPON" />
            <el-option :label="$t('adminActivities.typeGift')" value="GIFT" />
            <el-option :label="$t('adminActivities.typeFreeShipping')" value="FREE_SHIPPING" />
            <el-option :label="$t('adminActivities.typeNewUser')" value="NEW_USER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('common.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('adminActivities.statusNotStarted')" :value="0" />
            <el-option :label="$t('adminActivities.statusOngoing')" :value="1" />
            <el-option :label="$t('adminActivities.statusEnded')" :value="2" />
            <el-option :label="$t('adminActivities.statusCancelled')" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadActivities">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="activities" v-loading="loading">
        <el-table-column :label="$t('adminActivities.activityNo')" prop="activityNo" width="150" />
        <el-table-column :label="$t('adminActivities.nameZh')" prop="nameZh" min-width="200" />
        <el-table-column :label="$t('adminActivities.nameEn')" prop="nameEn" min-width="200" />
        <el-table-column :label="$t('adminActivities.activityType')" prop="activityType" width="120" />
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminActivities.startTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminActivities.endTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
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
        @size-change="loadActivities"
        @current-change="loadActivities"
        class="pagination"
      />
    </el-card>

    <!-- 活动编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingActivity ? $t('adminActivities.editActivity') : $t('adminActivities.addActivity')"
      width="800px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('adminActivities.nameZh')" prop="nameZh">
          <el-input v-model="form.nameZh" :placeholder="$t('adminActivities.inputNameZh')" />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.nameEn')" prop="nameEn">
          <el-input v-model="form.nameEn" :placeholder="$t('adminActivities.inputNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.descriptionZh')">
          <el-input v-model="form.descriptionZh" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.descriptionEn')">
          <el-input v-model="form.descriptionEn" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.activityType')" prop="activityType">
          <el-select v-model="form.activityType" :placeholder="$t('adminActivities.selectType')">
            <el-option :label="$t('adminActivities.typeDiscount')" value="DISCOUNT" />
            <el-option :label="$t('adminActivities.typeCoupon')" value="COUPON" />
            <el-option :label="$t('adminActivities.typeGift')" value="GIFT" />
            <el-option :label="$t('adminActivities.typeFreeShipping')" value="FREE_SHIPPING" />
            <el-option :label="$t('adminActivities.typeNewUser')" value="NEW_USER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminActivities.coverImage')">
          <el-input v-model="form.coverImage" :placeholder="$t('adminActivities.inputCoverImage')" />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.h5Url')">
          <el-input v-model="form.h5Url" :placeholder="$t('adminActivities.inputH5Url')" />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.pcUrl')">
          <el-input v-model="form.pcUrl" :placeholder="$t('adminActivities.inputPcUrl')" />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.startTime')" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            :placeholder="$t('adminActivities.selectStartTime')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.endTime')" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            :placeholder="$t('adminActivities.selectEndTime')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('adminActivities.enabled')">
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
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const dialogVisible = ref(false)
const editingActivity = ref(null)
const formRef = ref(null)

const searchForm = reactive({
  activityType: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const activities = ref([])

const form = reactive({
  nameZh: '',
  nameEn: '',
  descriptionZh: '',
  descriptionEn: '',
  activityType: '',
  coverImage: '',
  h5Url: '',
  pcUrl: '',
  startTime: null,
  endTime: null,
  enabled: 1
})

const rules = {
  nameZh: [{ required: true, message: () => t('adminActivities.pleaseInputNameZh'), trigger: 'blur' }],
  activityType: [{ required: true, message: () => t('adminActivities.pleaseSelectType'), trigger: 'change' }],
  startTime: [{ required: true, message: () => t('adminActivities.pleaseSelectStartTime'), trigger: 'change' }],
  endTime: [{ required: true, message: () => t('adminActivities.pleaseSelectEndTime'), trigger: 'change' }]
}

const getStatusText = (status) => {
  const map = { 
    0: t('adminActivities.statusNotStarted'), 
    1: t('adminActivities.statusOngoing'), 
    2: t('adminActivities.statusEnded'), 
    3: t('adminActivities.statusCancelled') 
  }
  return map[status] || t('common.unknown')
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const loadActivities = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await request.get('/admin/activity', { params })
    activities.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(t('adminActivities.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingActivity.value = null
  Object.assign(form, {
    nameZh: '',
    nameEn: '',
    descriptionZh: '',
    descriptionEn: '',
    activityType: '',
    coverImage: '',
    h5Url: '',
    pcUrl: '',
    startTime: null,
    endTime: null,
    enabled: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editingActivity.value = row
  Object.assign(form, {
    nameZh: row.nameZh,
    nameEn: row.nameEn,
    descriptionZh: row.descriptionZh,
    descriptionEn: row.descriptionEn,
    activityType: row.activityType,
    coverImage: row.coverImage,
    h5Url: row.h5Url,
    pcUrl: row.pcUrl,
    startTime: row.startTime ? new Date(row.startTime) : null,
    endTime: row.endTime ? new Date(row.endTime) : null,
    enabled: row.enabled
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    const submitData = {
      ...form,
      startTime: form.startTime ? form.startTime.toISOString() : null,
      endTime: form.endTime ? form.endTime.toISOString() : null
    }
        if (editingActivity.value) {
          await request.put(`/admin/activity/${editingActivity.value.id}`, submitData)
          ElMessage.success(t('adminActivities.updateSuccess'))
        } else {
          await request.post('/admin/activity', submitData)
          ElMessage.success(t('adminActivities.createSuccess'))
        }
    dialogVisible.value = false
    loadActivities()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(editingActivity.value ? t('adminActivities.updateFailed') : t('adminActivities.createFailed'))
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('adminActivities.deleteConfirm'), t('common.tip'), { type: 'warning' })
    await request.delete(`/admin/activity/${id}`)
    ElMessage.success(t('adminActivities.deleteSuccess'))
    loadActivities()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('adminActivities.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped lang="scss">
.activities-page {
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

