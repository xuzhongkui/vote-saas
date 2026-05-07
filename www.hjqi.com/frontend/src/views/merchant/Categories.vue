<template>
  <div class="categories-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('merchantCategories.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('merchantCategories.addCategory') }}
          </el-button>
        </div>
      </template>

      <el-table :data="categories" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('merchantCategories.categoryNameZh')" prop="nameZh" />
        <el-table-column :label="$t('merchantCategories.categoryNameEn')" prop="nameEn" />
        <el-table-column :label="$t('merchantCategories.sort')" prop="sort" width="100" />
        <el-table-column :label="$t('merchantCategories.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? $t('merchantCategories.enabled') : $t('merchantCategories.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.createdAt')" width="180">
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

    <!-- 分类编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingCategory ? $t('merchantCategories.editCategory') : $t('merchantCategories.addCategory')"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="140px">
        <el-form-item :label="$t('merchantCategories.categoryNameZh')" prop="nameZh">
          <el-input v-model="form.nameZh" :placeholder="$t('merchantCategories.inputCategoryNameZh')" />
        </el-form-item>
        <el-form-item :label="$t('merchantCategories.categoryNameEn')" prop="nameEn">
          <el-input v-model="form.nameEn" :placeholder="$t('merchantCategories.inputCategoryNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('merchantCategories.parentCategory')" prop="parentId">
          <el-input-number v-model="form.parentId" :min="0" :placeholder="$t('merchantCategories.parentIdTip')" />
        </el-form-item>
        <el-form-item :label="$t('merchantCategories.sort')" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('merchantCategories.status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ $t('merchantCategories.enabled') }}</el-radio>
            <el-radio :label="0">{{ $t('merchantCategories.disabled') }}</el-radio>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getCategories, createCategory, updateCategory, deleteCategory } from '@/api/merchant'
import { formatDateTime } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingCategory = ref(null)
const formRef = ref(null)

const form = reactive({
  nameZh: '',
  nameEn: '',
  parentId: 0,
  sort: 0,
  status: 1
})

const rules = computed(() => ({
  nameZh: [
    { required: true, message: t('merchantCategories.inputCategoryNameZh'), trigger: 'blur' }
  ]
}))

const loadCategories = async () => {
  try {
    loading.value = true
    categories.value = await getCategories()
  } catch (error) {
    console.error('Load categories failed:', error)
    ElMessage.error(t('merchantCategories.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingCategory.value = null
  Object.assign(form, {
    nameZh: '',
    nameEn: '',
    parentId: 0,
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (category) => {
  editingCategory.value = category
  Object.assign(form, {
    nameZh: category.nameZh || '',
    nameEn: category.nameEn || '',
    parentId: category.parentId || 0,
    sort: category.sort || 0,
    status: category.status !== undefined ? category.status : 1
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (editingCategory.value) {
      await updateCategory(editingCategory.value.id, form)
      ElMessage.success(t('merchantCategories.editSuccess'))
    } else {
      await createCategory(form)
      ElMessage.success(t('merchantCategories.addSuccess'))
    }
    
    dialogVisible.value = false
    loadCategories()
  } catch (error) {
    if (error !== false) {
      console.error('Save category failed:', error)
      ElMessage.error(editingCategory.value ? t('merchantCategories.editFailed') : t('merchantCategories.addFailed'))
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('merchantCategories.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteCategory(id)
    ElMessage.success(t('merchantCategories.deleteSuccess'))
    loadCategories()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete failed:', error)
      ElMessage.error(t('merchantCategories.deleteFailed'))
    }
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped lang="scss">
.categories-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
    }
  }
}
</style>
