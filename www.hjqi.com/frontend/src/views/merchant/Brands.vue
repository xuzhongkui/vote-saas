<template>
  <div class="brands-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ $t('merchantBrands.title') }}</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            {{ $t('merchantBrands.addBrand') }}
          </el-button>
        </div>
      </template>

      <el-table :data="brands" v-loading="loading" stripe>
        <el-table-column prop="nameZh" :label="$t('merchantBrands.brandNameZh')" />
        <el-table-column prop="nameEn" :label="$t('merchantBrands.brandNameEn')" />
        <el-table-column :label="$t('merchantBrands.logo')">
          <template #default="{ row }">
            <el-image 
              v-if="row.logo" 
              :src="row.logo" 
              style="width: 60px; height: 60px"
              fit="cover"
            />
            <span v-else>{{ $t('common.none') }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" :label="$t('merchantBrands.sort')" width="100" />
        <el-table-column :label="$t('merchantBrands.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? $t('merchantBrands.enabled') : $t('merchantBrands.disabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('common.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadBrands"
        @current-change="loadBrands"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingBrand ? $t('merchantBrands.editBrand') : $t('merchantBrands.addBrand')"
      width="600px"
    >
      <el-form :model="brandForm" label-width="140px">
        <el-form-item :label="$t('merchantBrands.brandNameZh')" required>
          <el-input v-model="brandForm.nameZh" :placeholder="$t('merchantBrands.inputBrandNameZh')" />
        </el-form-item>
        <el-form-item :label="$t('merchantBrands.brandNameEn')">
          <el-input v-model="brandForm.nameEn" :placeholder="$t('merchantBrands.inputBrandNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('merchantBrands.logo')">
          <div class="logo-uploader">
            <el-upload
              class="logo-upload"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :on-error="handleLogoError"
              :before-upload="beforeLogoUpload"
              :headers="uploadHeaders"
              name="file"
            >
              <img v-if="brandForm.logo" :src="brandForm.logo" class="logo-image" />
              <el-icon v-else class="logo-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="logo-tip">{{ $t('merchantBrands.logoTip') }}</div>
          </div>
        </el-form-item>
        <el-form-item :label="$t('merchantBrands.descriptionZh')">
          <el-input 
            v-model="brandForm.descriptionZh" 
            type="textarea" 
            :rows="3"
            :placeholder="$t('merchantBrands.inputDescriptionZh')"
          />
        </el-form-item>
        <el-form-item :label="$t('merchantBrands.descriptionEn')">
          <el-input 
            v-model="brandForm.descriptionEn" 
            type="textarea" 
            :rows="3"
            :placeholder="$t('merchantBrands.inputDescriptionEn')"
          />
        </el-form-item>
        <el-form-item :label="$t('merchantBrands.sort')">
          <el-input-number v-model="brandForm.sort" :min="0" />
        </el-form-item>
        <el-form-item :label="$t('merchantBrands.status')">
          <el-radio-group v-model="brandForm.status">
            <el-radio :label="1">{{ $t('merchantBrands.enabled') }}</el-radio>
            <el-radio :label="0">{{ $t('merchantBrands.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getBrands, createBrand, updateBrand, deleteBrand } from '@/api/brand'

const { t } = useI18n()

const loading = ref(false)
const brands = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const showCreateDialog = ref(false)
const editingBrand = ref(null)
const brandForm = ref({
  nameZh: '',
  nameEn: '',
  logo: '',
  descriptionZh: '',
  descriptionEn: '',
  sort: 0,
  status: 1
})

const loadBrands = async () => {
  loading.value = true
  try {
    const response = await getBrands(page.value, size.value)
    brands.value = response.records || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error(t('merchantBrands.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleEdit = (brand) => {
  editingBrand.value = brand
  brandForm.value = {
    nameZh: brand.nameZh,
    nameEn: brand.nameEn || '',
    logo: brand.logo || '',
    descriptionZh: brand.descriptionZh || '',
    descriptionEn: brand.descriptionEn || '',
    sort: brand.sort || 0,
    status: brand.status
  }
  showCreateDialog.value = true
}

const handleDelete = async (brand) => {
  try {
    await ElMessageBox.confirm(t('merchantBrands.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteBrand(brand.id)
    ElMessage.success(t('merchantBrands.deleteSuccess'))
    loadBrands()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('merchantBrands.deleteFailed'))
    }
  }
}

const handleSubmit = async () => {
  if (!brandForm.value.nameZh) {
    ElMessage.warning(t('merchantBrands.inputBrandNameZh'))
    return
  }

  try {
    if (editingBrand.value) {
      await updateBrand(editingBrand.value.id, brandForm.value)
      ElMessage.success(t('merchantBrands.editSuccess'))
    } else {
      await createBrand(brandForm.value)
      ElMessage.success(t('merchantBrands.addSuccess'))
    }
    showCreateDialog.value = false
    resetForm()
    loadBrands()
  } catch (error) {
    ElMessage.error(editingBrand.value ? t('merchantBrands.editFailed') : t('merchantBrands.addFailed'))
  }
}

const resetForm = () => {
  editingBrand.value = null
  brandForm.value = {
    nameZh: '',
    nameEn: '',
    logo: '',
    descriptionZh: '',
    descriptionEn: '',
    sort: 0,
    status: 1
  }
}

// 文件上传相关
const uploadUrl = '/api/common/upload'

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const beforeLogoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error(t('common.imageOnly'))
    return false
  }
  if (!isLt2M) {
    ElMessage.error(t('common.imageSizeLimit'))
    return false
  }
  return true
}

const handleLogoSuccess = (response) => {
  if (response && response.url) {
    brandForm.value.logo = response.url
    ElMessage.success(t('common.uploadSuccess'))
  } else {
    ElMessage.error(t('common.uploadFailed'))
  }
}

const handleLogoError = () => {
  ElMessage.error(t('common.uploadFailed'))
}

onMounted(() => {
  loadBrands()
})
</script>

<style scoped lang="scss">
.brands-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.logo-uploader {
  .logo-upload {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: all 0.3s;
      width: 120px;
      height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        border-color: #409eff;
      }
    }

    .logo-image {
      width: 120px;
      height: 120px;
      object-fit: cover;
      display: block;
    }

    .logo-uploader-icon {
      font-size: 28px;
      color: #8c939d;
    }
  }

  .logo-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}
</style>
