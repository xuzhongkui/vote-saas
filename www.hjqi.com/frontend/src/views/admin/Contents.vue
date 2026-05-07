<template>
  <div class="contents-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminContents.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('adminContents.addContent') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminContents.contentType')">
          <el-select v-model="searchForm.contentType" :placeholder="$t('adminContents.selectType')" clearable style="width: 180px">
            <el-option :label="$t('adminContents.typeSeo')" value="SEO" />
            <el-option :label="$t('adminContents.typeActivity')" value="ACTIVITY" />
            <el-option :label="$t('adminContents.typeNews')" value="NEWS" />
            <el-option :label="$t('adminContents.typeAnnouncement')" value="ANNOUNCEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('common.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('common.enabled')" :value="1" />
            <el-option :label="$t('common.disabled')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadContents">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="contents" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('common.type')" width="120">
          <template #default="{ row }">
            {{ getContentTypeLabel(row.contentType) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminContents.titleZh')" prop="titleZh" min-width="200" />
        <el-table-column :label="$t('adminContents.titleEn')" prop="titleEn" min-width="200" />
        <el-table-column :label="$t('adminContents.sort')" prop="sort" width="80" />
        <el-table-column :label="$t('common.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? $t('common.enabled') : $t('common.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('adminContents.publishTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadContents"
        />
      </div>
    </el-card>

    <!-- 内容编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingContent ? $t('adminContents.editContent') : $t('adminContents.addContent')"
      width="700px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('adminContents.contentType')" prop="contentType">
          <el-select v-model="form.contentType" :placeholder="$t('adminContents.selectType')">
            <el-option :label="$t('adminContents.typeSeo')" value="SEO" />
            <el-option :label="$t('adminContents.typeActivity')" value="ACTIVITY" />
            <el-option :label="$t('adminContents.typeNews')" value="NEWS" />
            <el-option :label="$t('adminContents.typeAnnouncement')" value="ANNOUNCEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminContents.titleZh')" prop="titleZh">
          <el-input v-model="form.titleZh" />
        </el-form-item>
        <el-form-item :label="$t('adminContents.titleEn')" prop="titleEn">
          <el-input v-model="form.titleEn" />
        </el-form-item>
        <el-form-item :label="$t('adminContents.contentZh')" prop="contentZh">
          <el-input v-model="form.contentZh" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item :label="$t('adminContents.contentEn')" prop="contentEn">
          <el-input v-model="form.contentEn" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item :label="$t('adminContents.coverImage')" prop="coverImage">
          <el-input v-model="form.coverImage" :placeholder="$t('adminContents.inputImageUrl')" />
        </el-form-item>
        <el-form-item :label="$t('adminContents.linkUrl')" prop="linkUrl">
          <el-input v-model="form.linkUrl" :placeholder="$t('adminContents.inputLinkUrl')" />
        </el-form-item>
        <el-form-item :label="$t('adminContents.sort')" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('adminContents.publishTime')" prop="publishAt">
          <el-date-picker
            v-model="form.publishAt"
            type="datetime"
            :placeholder="$t('adminContents.selectPublishTime')"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item :label="$t('common.status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ $t('common.enable') }}</el-radio>
            <el-radio :label="0">{{ $t('common.disable') }}</el-radio>
          </el-radio-group>
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
import { getContents, getContent, createContent, updateContent, deleteContent } from '@/api/admin'
import { formatDateTime, CONTENT_TYPE_MAP } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const contents = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const searchForm = reactive({
  contentType: null,
  status: null
})

const dialogVisible = ref(false)
const editingContent = ref(null)
const formRef = ref(null)

const form = reactive({
  contentType: '',
  titleZh: '',
  titleEn: '',
  contentZh: '',
  contentEn: '',
  coverImage: '',
  linkUrl: '',
  sort: 0,
  publishAt: '',
  status: 1
})

const rules = {
  contentType: [{ required: true, message: () => t('adminContents.pleaseSelectType'), trigger: 'change' }],
  titleZh: [{ required: true, message: () => t('adminContents.pleaseInputTitleZh'), trigger: 'blur' }]
}

const getContentTypeLabel = (type) => {
  const typeMap = {
    SEO: t('adminContents.typeSeo'),
    ACTIVITY: t('adminContents.typeActivity'),
    NEWS: t('adminContents.typeNews'),
    ANNOUNCEMENT: t('adminContents.typeAnnouncement')
  }
  return typeMap[type] || type
}

const loadContents = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const result = await getContents(params)
    contents.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载内容失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingContent.value = null
  Object.assign(form, {
    contentType: '',
    titleZh: '',
    titleEn: '',
    contentZh: '',
    contentEn: '',
    coverImage: '',
    linkUrl: '',
    sort: 0,
    publishAt: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = async (content) => {
  editingContent.value = content
  const detail = await getContent(content.id)
  Object.assign(form, detail)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (editingContent.value) {
      await updateContent(editingContent.value.id, form)
      ElMessage.success(t('adminContents.updateSuccess'))
    } else {
      await createContent(form)
      ElMessage.success(t('adminContents.addSuccess'))
    }
    
    dialogVisible.value = false
    loadContents()
  } catch (error) {
    if (error !== false) {
      console.error('保存内容失败:', error)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('adminContents.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteContent(id)
    ElMessage.success(t('adminContents.deleteSuccess'))
    loadContents()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadContents()
})
</script>

<style scoped lang="scss">
.contents-page {
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

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>

