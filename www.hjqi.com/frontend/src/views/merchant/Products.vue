<template>
  <div class="products-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('merchantProducts.title') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('merchantProducts.addProduct') }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('merchantProducts.category')">
          <el-select v-model="searchForm.categoryId" :placeholder="$t('merchantProducts.selectCategory')" clearable style="width: 200px">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="getCategoryDisplayName(cat)"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('merchantProducts.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('merchantProducts.onSale')" :value="1" />
            <el-option :label="$t('merchantProducts.offSale')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadProducts">{{ $t('merchantProducts.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="products" v-loading="loading">
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column :label="$t('merchantProducts.mainImage')" width="100">
          <template #default="{ row }">
            <el-image
              v-if="row.mainImage"
              :src="getImageUrl(row.mainImage)"
              style="width: 60px; height: 60px"
              fit="cover"
              :preview-src-list="[getImageUrl(row.mainImage)]"
              preview-teleported
            >
              <template #error>
                <div class="image-slot" style="width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #909399;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="image-slot" style="width: 60px; height: 60px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; color: #909399;">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.productName')" min-width="200">
          <template #default="{ row }">
            {{ row.nameZh || row.nameEn || $t('merchantProducts.unnamedProduct') }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.category')" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.price')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.price) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.stock')" prop="stock" width="100" />
        <el-table-column :label="$t('merchantProducts.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? $t('merchantProducts.onSale') : $t('merchantProducts.offSale') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.operation')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">{{ $t('merchantProducts.edit') }}</el-button>
            <el-button type="info" link @click="handleManageSku(row)">SKU</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">{{ $t('merchantProducts.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadProducts"
        />
      </div>
    </el-card>

    <!-- 商品编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingProduct ? $t('merchantProducts.editProduct') : $t('merchantProducts.addProduct')"
      width="900px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-tabs v-model="activeFormTab">
          <el-tab-pane :label="$t('merchantProducts.basicInfo')" name="basic">
            <el-form-item :label="$t('merchantProducts.productNameZh')" prop="nameZh">
              <el-input v-model="form.nameZh" :placeholder="$t('merchantProducts.inputProductNameZh')" />
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.subtitleZh')">
              <el-input v-model="form.subtitleZh" :placeholder="$t('merchantProducts.inputSubtitleZh')" />
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.category')" prop="categoryId">
              <el-select v-model="form.categoryId" :placeholder="$t('merchantProducts.selectCategory')" style="width: 100%">
                <el-option
                  v-for="cat in categories"
                  :key="cat.id"
                  :label="getCategoryDisplayName(cat)"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.brand')">
              <el-select v-model="form.brandId" :placeholder="$t('merchantProducts.selectBrand')" clearable style="width: 100%">
                <el-option
                  v-for="brand in brands"
                  :key="brand.id"
                  :label="brand.nameZh || brand.nameEn"
                  :value="brand.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.originalPrice')" v-if="!hasSkus">
              <el-input-number v-model="form.originalPrice" :min="0" :max="999999999" :precision="2" :step="0.01" />
              <span style="margin-left: 10px">{{ $t('merchantProducts.yuan') }}</span>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">{{ $t('merchantProducts.skuPriceTip') }}</div>
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.salePrice')" :prop="hasSkus ? '' : 'price'" v-if="!hasSkus">
              <el-input-number v-model="form.price" :min="0" :max="999999999" :precision="2" :step="0.01" />
              <span style="margin-left: 10px">{{ $t('merchantProducts.yuan') }}</span>
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">{{ $t('merchantProducts.skuPriceTip') }}</div>
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.stock')" :prop="hasSkus ? '' : 'stock'" v-if="!hasSkus">
              <el-input-number v-model="form.stock" :min="0" :max="999999999" />
              <div style="font-size: 12px; color: #909399; margin-top: 4px;">{{ $t('merchantProducts.skuStockTip') }}</div>
            </el-form-item>
            <el-alert v-if="hasSkus" type="info" :closable="false" style="margin-bottom: 20px;">
              <template #title>
                <span>{{ $t('merchantProducts.skuHasPrice') }}</span>
              </template>
            </el-alert>
            <el-form-item :label="$t('merchantProducts.shippingTemplate')">
              <el-select v-model="form.shippingTemplateId" :placeholder="$t('merchantProducts.selectShippingTemplate')" clearable style="width: 100%">
                <el-option
                  v-for="template in shippingTemplates"
                  :key="template.id"
                  :label="template.name"
                  :value="template.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.afterSaleRule')">
              <el-select v-model="form.afterSaleRuleId" :placeholder="$t('merchantProducts.selectAfterSaleRule')" clearable style="width: 100%">
                <el-option
                  v-for="rule in afterSaleRules"
                  :key="rule.id"
                  :label="rule.titleZh || rule.titleEn"
                  :value="rule.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.status')" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">{{ $t('merchantProducts.onSale') }}</el-radio>
                <el-radio :label="0">{{ $t('merchantProducts.offSale') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane :label="$t('merchantProducts.productImages')" name="images">
            <el-form-item :label="$t('merchantProducts.mainImage')">
              <div class="image-uploader">
                <el-upload
                  class="image-upload"
                  :action="uploadUrl"
                  :show-file-list="false"
                  :on-success="handleMainImageSuccess"
                  :on-error="handleImageError"
                  :before-upload="beforeImageUpload"
                  :headers="uploadHeaders"
                  name="file"
                >
                  <template v-if="form.mainImage">
                    <img :src="form.mainImage" class="upload-image" alt="主图预览" />
                  </template>
                  <template v-else>
                    <el-icon class="upload-icon"><Plus /></el-icon>
                  </template>
                </el-upload>
                <div class="upload-tip">{{ $t('merchantProducts.imageTip') }}</div>
                <div v-if="form.mainImage" style="margin-top: 8px; font-size: 12px; color: #909399;">
                  {{ $t('merchantProducts.currentMainImage') }}: {{ form.mainImage }}
                </div>
              </div>
            </el-form-item>
            <el-form-item :label="$t('merchantProducts.detailImages')">
              <el-upload
                :action="uploadUrl"
                list-type="picture-card"
                :on-success="handleDetailImageSuccess"
                :on-remove="handleRemoveDetailImage"
                :before-upload="beforeImageUpload"
                :headers="uploadHeaders"
                :file-list="detailImageList"
                name="file"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">{{ $t('merchantProducts.multiImageTip') }}</div>
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane :label="$t('merchantProducts.skuManagement')" name="skus">
            <div class="sku-management">
              <div class="sku-header-inline">
                <div>
                  <h4 style="margin: 0 0 8px 0;">{{ $t('merchantProducts.skuList') }}</h4>
                  <div style="font-size: 12px; color: #909399;">
                    {{ $t('merchantProducts.skuDesc') }}
                  </div>
                </div>
                <el-button type="primary" size="small" @click="handleAddSkuInline">
                  <el-icon><Plus /></el-icon>
                  {{ $t('merchantProducts.addSku') }}
                </el-button>
              </div>
              
              <el-table :data="formSkus" style="margin-top: 20px" v-if="formSkus.length > 0">
                <el-table-column :label="$t('merchantProducts.skuName')" min-width="150">
                  <template #default="{ row }">
                    {{ row.specNameZh || row.specNameEn || row.skuName || $t('merchantProducts.defaultSku') }}
                  </template>
                </el-table-column>
                <el-table-column :label="$t('merchantProducts.skuCode')" prop="skuCode" width="120" />
                <el-table-column :label="$t('merchantProducts.price')" width="120">
                  <template #default="{ row }">
                    ¥{{ formatMoney(row.price) }}
                  </template>
                </el-table-column>
                <el-table-column :label="$t('merchantProducts.stock')" prop="stock" width="100" />
                <el-table-column :label="$t('merchantProducts.operation')" width="150">
                  <template #default="{ row, $index }">
                    <el-button type="primary" link @click="handleEditSkuInline($index)">{{ $t('merchantProducts.edit') }}</el-button>
                    <el-button type="danger" link @click="handleDeleteSkuInline($index)">{{ $t('merchantProducts.delete') }}</el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <el-empty v-else :description="$t('merchantProducts.noSkuHint')" :image-size="100" />
            </div>
          </el-tab-pane>
        </el-tabs>
        <el-form-item :label="$t('merchantProducts.descriptionZh')">
          <el-input v-model="form.descriptionZh" type="textarea" :rows="5" :placeholder="$t('merchantProducts.inputDescription')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- SKU管理对话框 -->
    <el-dialog v-model="skuDialogVisible" :title="$t('merchantProducts.skuManagement')" width="800px">
      <div class="sku-header">
        <div>
          <h4>{{ currentProduct?.nameZh || currentProduct?.nameEn || $t('product.product') }} - {{ $t('merchantProducts.skuList') }}</h4>
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">
            {{ $t('merchantProducts.skuDesc') }}
          </div>
        </div>
        <el-button type="primary" size="small" @click="handleAddSku">
          <el-icon><Plus /></el-icon>
          {{ $t('merchantProducts.addSku') }}
        </el-button>
      </div>
      
      <el-table :data="skus" style="margin-top: 10px">
        <el-table-column :label="$t('merchantProducts.skuName')" min-width="150">
          <template #default="{ row }">
            {{ row.specNameZh || row.specNameEn || row.skuName || $t('merchantProducts.defaultSku') }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.skuCode')" prop="skuCode" width="120" />
        <el-table-column :label="$t('merchantProducts.price')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.price) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.stock')" prop="stock" width="100" />
        <el-table-column :label="$t('merchantProducts.status')" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? $t('common.enable') : $t('common.disable') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantProducts.operation')" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditSku(row)">{{ $t('merchantProducts.edit') }}</el-button>
            <el-button type="danger" link @click="handleDeleteSku(row.id)">{{ $t('merchantProducts.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- SKU编辑对话框 -->
    <el-dialog
      v-model="skuFormVisible"
      :title="editingSku ? $t('merchantProducts.editSku') : $t('merchantProducts.addSku')"
      width="500px"
    >
      <el-form ref="skuFormRef" :model="skuForm" :rules="skuRules" label-width="100px">
        <el-form-item :label="$t('merchantProducts.skuCode')" prop="skuCode">
          <el-input v-model="skuForm.skuCode" :placeholder="$t('merchantProducts.autoGenerateOrInput')" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">{{ $t('merchantProducts.skuCodeTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.skuNameZh')" prop="specNameZh">
          <el-input v-model="skuForm.specNameZh" :placeholder="$t('merchantProducts.inputSkuNameZh')" />
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.skuNameEn')" prop="specNameEn">
          <el-input v-model="skuForm.specNameEn" :placeholder="$t('merchantProducts.inputSkuNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.price')" prop="price">
          <el-input-number v-model="skuForm.price" :min="0" :max="999999999" :precision="2" :step="0.01" />
          <span style="margin-left: 10px">{{ $t('merchantProducts.yuan') }}</span>
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.stock')" prop="stock">
          <el-input-number v-model="skuForm.stock" :min="0" :max="999999999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuFormVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitSku">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 内联SKU编辑对话框（在商品编辑对话框中） -->
    <el-dialog
      v-model="inlineSkuFormVisible"
      :title="editingSkuIndex >= 0 ? $t('merchantProducts.editSku') : $t('merchantProducts.addSku')"
      width="500px"
      append-to-body
    >
      <el-form ref="inlineSkuFormRef" :model="inlineSkuForm" :rules="skuRules" label-width="100px">
        <el-form-item :label="$t('merchantProducts.skuCode')">
          <el-input v-model="inlineSkuForm.skuCode" :placeholder="$t('merchantProducts.autoGenerateOrInput')" />
          <div style="font-size: 12px; color: #909399; margin-top: 4px;">{{ $t('merchantProducts.skuCodeTip') }}</div>
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.skuNameZh')" prop="specNameZh">
          <el-input v-model="inlineSkuForm.specNameZh" :placeholder="$t('merchantProducts.inputSkuNameZh')" />
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.skuNameEn')" prop="specNameEn">
          <el-input v-model="inlineSkuForm.specNameEn" :placeholder="$t('merchantProducts.inputSkuNameEn')" />
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.price')" prop="price">
          <el-input-number v-model="inlineSkuForm.price" :min="0" :max="999999999" :precision="2" :step="0.01" />
          <span style="margin-left: 10px">{{ $t('merchantProducts.yuan') }}</span>
        </el-form-item>
        <el-form-item :label="$t('merchantProducts.stock')" prop="stock">
          <el-input-number v-model="inlineSkuForm.stock" :min="0" :max="999999999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inlineSkuFormVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitSkuInline">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  getProducts,
  getProduct,
  createProduct,
  updateProduct,
  deleteProduct,
  getCategories,
  getProductSkus,
  createProductSku,
  updateProductSku,
  deleteProductSku,
  getBrands,
  getShippingTemplates,
  getAfterSaleRules
} from '@/api/merchant'
import { formatMoney, toFen } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'

const { t } = useI18n()

const categories = ref([])
const brands = ref([])
const shippingTemplates = ref([])
const afterSaleRules = ref([])
const products = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const activeFormTab = ref('basic')
const detailImageList = ref([])

const searchForm = reactive({
  categoryId: null,
  status: null
})

const dialogVisible = ref(false)
const editingProduct = ref(null)
const formRef = ref(null)

const form = reactive({
  nameZh: '',
  subtitleZh: '',
  categoryId: null,
  brandId: null,
  originalPrice: null,
  price: 0,
  stock: 0,
  mainImage: '',
  images: [],
  descriptionZh: '',
  shippingTemplateId: null,
  afterSaleRuleId: null,
  status: 1
})

// 视频上传相关
const uploadUrl = '/api/common/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const handleMainImageSuccess = (response) => {
  console.log('=== 主图上传成功回调 ===')
  console.log('响应对象:', response)
  console.log('响应类型:', typeof response)
  console.log('response.url:', response?.url)
  console.log('response.data:', response?.data)
  
  // el-upload 使用 action 时，response 是后端直接返回的 JSON 对象
  // 后端返回格式: { url: "/upload/xxx", originalName: "...", size: ... }
  let imageUrl = null
  
  if (response) {
    if (response.url) {
      imageUrl = response.url
    } else if (response.data && response.data.url) {
      imageUrl = response.data.url
    } else if (typeof response === 'string') {
      try {
        const parsed = JSON.parse(response)
        imageUrl = parsed.url || parsed.data?.url
      } catch (e) {
        console.error('解析响应字符串失败:', e)
      }
    }
  }
  
  if (imageUrl) {
    console.log('提取的图片URL:', imageUrl)
    console.log('设置前的 form.mainImage:', form.mainImage)
    form.mainImage = imageUrl
    console.log('设置后的 form.mainImage:', form.mainImage)
    console.log('form 对象:', form)
    ElMessage.success(t('merchantProducts.mainImageUploadSuccess'))
  } else {
    console.error('无法提取图片URL')
    console.error('完整响应:', JSON.stringify(response, null, 2))
    ElMessage.error(t('merchantProducts.mainImageUploadFailed'))
  }
}

const handleDetailImageSuccess = (response) => {
  if (response && response.url) {
    form.images.push(response.url)
    // 同步更新 detailImageList 用于显示
    detailImageList.value.push({
      name: response.url.split('/').pop(),
      url: response.url
    })
    ElMessage.success(t('merchantProducts.imageUploadSuccess'))
  } else {
    ElMessage.error(t('merchantProducts.imageUploadFailed'))
  }
}

const handleRemoveDetailImage = (file) => {
  const url = file.url || file.response?.url
  if (url) {
    const index = form.images.indexOf(url)
    if (index > -1) {
      form.images.splice(index, 1)
    }
    // 同步更新 detailImageList
    const listIndex = detailImageList.value.findIndex(item => item.url === url)
    if (listIndex > -1) {
      detailImageList.value.splice(listIndex, 1)
    }
  }
}

const handleImageError = (error) => {
  console.error('图片上传失败:', error)
  ElMessage.error(t('merchantProducts.imageUploadRetry'))
}

const beforeImageUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error(t('merchantProducts.imageFormatError'))
    return false
  }
  if (!isLt2M) {
    ElMessage.error(t('merchantProducts.imageSizeError'))
    return false
  }
  return true
}

// 使用 computed 动态生成验证规则，根据是否有SKU来决定是否验证价格和库存
const rules = computed(() => {
  const baseRules = {
  nameZh: [{ required: true, message: t('merchantProducts.inputProductNameZh'), trigger: 'blur' }],
    categoryId: [{ required: true, message: t('merchantProducts.selectCategory'), trigger: 'change' }]
  }
  
  // 如果没有SKU，才添加价格和库存的验证规则
  if (!hasSkus.value) {
    baseRules.price = [{ 
    validator: (rule, value, callback) => {
        if (!value || value <= 0) {
        callback(new Error(t('merchantProducts.inputPrice')))
      } else {
        callback()
      }
    }, 
    trigger: 'blur' 
    }]
    baseRules.stock = [{ 
    validator: (rule, value, callback) => {
        if (value === null || value === undefined || value < 0) {
        callback(new Error(t('merchantProducts.inputStock')))
      } else {
        callback()
      }
    }, 
    trigger: 'blur' 
  }]
}
  
  return baseRules
})

// SKU相关
const skuDialogVisible = ref(false)
const skuFormVisible = ref(false)
const currentProduct = ref(null)
const skus = ref([])
const editingSku = ref(null)
const skuFormRef = ref(null)

// 表单中的SKU列表（用于在商品编辑对话框中管理）
const formSkus = ref([])
const inlineSkuFormVisible = ref(false)
const inlineSkuFormRef = ref(null)
const editingSkuIndex = ref(-1)

// 内联SKU表单
const inlineSkuForm = reactive({
  skuCode: '',
  specNameZh: '',
  specNameEn: '',
  price: 0,
  stock: 0
})

// 检查商品是否有SKU（基于formSkus）
const hasSkus = computed(() => {
  return formSkus.value && formSkus.value.length > 0
})

const skuForm = reactive({
  productId: null,
  skuCode: '',
  specNameZh: '',
  specNameEn: '',
  price: 0,
  stock: 0
})

const skuRules = computed(() => ({
  specNameZh: [{ required: true, message: t('merchantProducts.inputSkuNameZh'), trigger: 'blur' }],
  price: [{ required: true, message: t('merchantProducts.inputPrice'), trigger: 'blur' }],
  stock: [{ required: true, message: t('merchantProducts.inputStock'), trigger: 'blur' }]
}))

const getCategoryDisplayName = (cat) => {
  if (cat.nameZh && cat.nameEn) {
    return `${cat.nameZh}（${cat.nameEn}）`
  } else if (cat.nameZh) {
    return cat.nameZh
  } else if (cat.nameEn) {
    return cat.nameEn
  } else {
    return t('merchantProducts.unnamedCategory')
  }
}

const getCategoryName = (categoryId) => {
  if (!categoryId) return '-'
  const category = categories.value.find(cat => cat.id === categoryId)
  if (category) {
    return category.nameZh || category.nameEn || t('merchantProducts.unnamedCategory')
  }
  return '-'
}

const getImageUrl = (url) => {
  if (!url) {
    return ''
  }
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  // vite 配置了 /upload 的代理，所以 /upload/xxx 可以直接访问
  // 直接返回 URL，不需要添加 /api 前缀
  return url
}

const loadCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadBrands = async () => {
  try {
    brands.value = await getBrands()
  } catch (error) {
    console.error('加载品牌失败:', error)
  }
}

const loadShippingTemplates = async () => {
  try {
    shippingTemplates.value = await getShippingTemplates()
  } catch (error) {
    console.error('加载运费模板失败:', error)
  }
}

const loadAfterSaleRules = async () => {
  try {
    const res = await getAfterSaleRules({})
    afterSaleRules.value = res || []
  } catch (error) {
    console.error('加载售后规则失败:', error)
  }
}

const loadProducts = async () => {
  try {
    loading.value = true
    const params = {
      page: page.value,
      size: size.value,
      ...searchForm
    }
    const result = await getProducts(params)
    products.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载商品失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  editingProduct.value = null
  activeFormTab.value = 'basic'
  detailImageList.value = []
  formSkus.value = [] // 重置SKU列表
  Object.assign(form, {
    nameZh: '',
    subtitleZh: '',
    categoryId: null,
    brandId: null,
    originalPrice: null,
    price: 0,
    stock: 0,
    mainImage: '',
    images: [],
    descriptionZh: '',
    shippingTemplateId: null,
    afterSaleRuleId: null,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = async (product) => {
  editingProduct.value = product
  activeFormTab.value = 'basic'
  const detail = await getProduct(product.id)
  
  // 处理图片数组
  let imagesArray = []
  if (detail.images) {
    if (Array.isArray(detail.images)) {
      imagesArray = detail.images
    } else if (typeof detail.images === 'string') {
      try {
        imagesArray = JSON.parse(detail.images)
      } catch (e) {
        imagesArray = []
      }
    }
  }
  
  // 构建 detailImageList
  detailImageList.value = imagesArray.map((url, index) => ({
    name: url.split('/').pop() || `image-${index}`,
    url: url
  }))
  
  // 加载SKU列表到formSkus
  try {
    const productSkus = await getProductSkus(product.id)
    formSkus.value = productSkus.map(sku => ({
      id: sku.id,
      skuCode: sku.skuCode || '',
      specNameZh: sku.specNameZh || '',
      specNameEn: sku.specNameEn || '',
      price: sku.price != null && sku.price !== '' ? Number(sku.price) : 0, // 保持分为单位，确保是数字
      stock: sku.stock != null && sku.stock !== '' ? Number(sku.stock) : 0, // 确保是数字
      status: sku.status !== undefined ? sku.status : 1
    }))
  } catch (error) {
    console.error('加载SKU失败:', error)
    formSkus.value = []
  }
  
  Object.assign(form, {
    nameZh: detail.nameZh || '',
    subtitleZh: detail.subtitleZh || '',
    categoryId: detail.categoryId || null,
    brandId: detail.brandId || null,
    originalPrice: detail.originalPrice ? detail.originalPrice / 100 : null,
    price: detail.price ? detail.price / 100 : 0,
    stock: detail.stock || 0,
    mainImage: detail.mainImage || '',
    images: imagesArray,
    descriptionZh: detail.descriptionZh || '',
    shippingTemplateId: detail.shippingTemplateId || null,
    afterSaleRuleId: detail.afterSaleRuleId || null,
    status: detail.status !== undefined ? detail.status : 1
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
      await formRef.value.validate()
    
    // 构建提交数据，确保所有字段都包含
    const data = {
      nameZh: form.nameZh,
      subtitleZh: form.subtitleZh || null,
      categoryId: form.categoryId,
      brandId: form.brandId || null,
      // 如果有SKU，价格和库存由SKU决定，SPU可以不填
      originalPrice: hasSkus.value ? null : (form.originalPrice ? toFen(form.originalPrice) : null),
      price: hasSkus.value ? 0 : (form.price ? toFen(form.price) : 0),
      stock: hasSkus.value ? 0 : (form.stock || 0),
      mainImage: form.mainImage || null,
      images: form.images && form.images.length > 0 ? form.images : null,
      descriptionZh: form.descriptionZh || null,
      shippingTemplateId: form.shippingTemplateId || null,
      afterSaleRuleId: form.afterSaleRuleId || null,
      status: form.status !== undefined ? form.status : 1
    }
    
    console.log('提交的商品数据:', data)
    
    let productId
    if (editingProduct.value) {
      await updateProduct(editingProduct.value.id, data)
      productId = editingProduct.value.id
      ElMessage.success(t('merchantProducts.editSuccess'))
    } else {
      const result = await createProduct(data)
      productId = result.id || result.data?.id
      ElMessage.success(t('merchantProducts.addSuccess'))
    }
    
    // 保存SKU
    if (productId && formSkus.value.length > 0) {
      try {
        // 先获取现有的SKU列表
        const existingSkus = await getProductSkus(productId)
        const existingSkuIds = existingSkus.map(s => s.id)
        
        // 处理每个SKU
        for (const sku of formSkus.value) {
          const skuData = {
            productId: productId,
            skuCode: sku.skuCode || undefined,
            specNameZh: sku.specNameZh,
            specNameEn: sku.specNameEn || undefined,
            price: sku.price, // 已经是分为单位
            stock: sku.stock,
            status: sku.status !== undefined ? sku.status : 1
          }
          
          if (sku.id && existingSkuIds.includes(sku.id)) {
            // 更新现有SKU
            await updateProductSku(sku.id, skuData)
          } else {
            // 创建新SKU
            await createProductSku(skuData)
          }
        }
        
        // 删除不在formSkus中的SKU
        const formSkuIds = formSkus.value.filter(s => s.id).map(s => s.id)
        for (const existingSku of existingSkus) {
          if (!formSkuIds.includes(existingSku.id)) {
            await deleteProductSku(existingSku.id)
          }
        }
      } catch (error) {
        console.error('保存SKU失败:', error)
        ElMessage.warning(t('merchantProducts.skuSaveWarning'))
      }
    }
    
    dialogVisible.value = false
    loadProducts()
  } catch (error) {
    if (error !== false) {
      console.error('保存商品失败:', error)
      const errorMessage = error.response?.data?.message || error.message || t('merchantProducts.saveFailed')
      ElMessage.error(errorMessage)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('merchantProducts.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteProduct(id)
    ElMessage.success(t('merchantProducts.deleteSuccess'))
    loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// SKU管理
const handleManageSku = async (product) => {
  currentProduct.value = product
  skuDialogVisible.value = true
  try {
    skus.value = await getProductSkus(product.id)
  } catch (error) {
    console.error('加载SKU失败:', error)
  }
}

const handleAddSku = () => {
  editingSku.value = null
  Object.assign(skuForm, {
    productId: currentProduct.value.id,
    skuCode: '',
    specNameZh: '',
    specNameEn: '',
    price: 0,
    stock: 0
  })
  skuFormVisible.value = true
}

const handleEditSku = (sku) => {
  editingSku.value = sku
  Object.assign(skuForm, {
    productId: currentProduct.value.id,
    skuCode: sku.skuCode || '',
    specNameZh: sku.specNameZh || '',
    specNameEn: sku.specNameEn || '',
    price: sku.price / 100,
    stock: sku.stock
  })
  skuFormVisible.value = true
}

const handleSubmitSku = async () => {
  try {
    await skuFormRef.value.validate()
    
    const data = {
      productId: skuForm.productId,
      skuCode: skuForm.skuCode || undefined, // 如果为空，让后端自动生成
      specNameZh: skuForm.specNameZh,
      specNameEn: skuForm.specNameEn || undefined,
      price: toFen(skuForm.price),
      stock: skuForm.stock
    }
    
    if (editingSku.value) {
      await updateProductSku(editingSku.value.id, data)
      ElMessage.success(t('merchantProducts.editSuccess'))
    } else {
      await createProductSku(data)
      ElMessage.success(t('merchantProducts.addSuccess'))
    }
    
    skuFormVisible.value = false
    skus.value = await getProductSkus(currentProduct.value.id)
  } catch (error) {
    if (error !== false) {
      console.error('保存SKU失败:', error)
    }
  }
}

const handleDeleteSku = async (id) => {
  try {
    await ElMessageBox.confirm(t('merchantProducts.deleteSkuConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteProductSku(id)
    ElMessage.success(t('merchantProducts.deleteSuccess'))
    skus.value = await getProductSkus(currentProduct.value.id)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 内联SKU管理方法（在商品编辑对话框中）
const handleAddSkuInline = () => {
  editingSkuIndex.value = -1
  Object.assign(inlineSkuForm, {
    skuCode: '',
    specNameZh: '',
    specNameEn: '',
    price: 0,
    stock: 0
  })
  inlineSkuFormVisible.value = true
}

const handleEditSkuInline = (index) => {
  editingSkuIndex.value = index
  const sku = formSkus.value[index]
  // 确保价格和库存都是数字类型
  const price = sku.price != null && sku.price !== '' ? Number(sku.price) / 100 : 0
  const stock = sku.stock != null && sku.stock !== '' ? Number(sku.stock) : 0
  Object.assign(inlineSkuForm, {
    skuCode: sku.skuCode || '',
    specNameZh: sku.specNameZh || '',
    specNameEn: sku.specNameEn || '',
    price: price, // 转换为元
    stock: stock
  })
  inlineSkuFormVisible.value = true
}

const handleDeleteSkuInline = (index) => {
  formSkus.value.splice(index, 1)
  ElMessage.success(t('merchantProducts.deleteSuccess'))
}

const handleSubmitSkuInline = async () => {
  try {
    await inlineSkuFormRef.value.validate()
    
    const skuData = {
      id: editingSkuIndex.value >= 0 ? formSkus.value[editingSkuIndex.value].id : undefined,
      skuCode: inlineSkuForm.skuCode || '',
      specNameZh: inlineSkuForm.specNameZh,
      specNameEn: inlineSkuForm.specNameEn || '',
      price: toFen(inlineSkuForm.price), // 转换为分
      stock: inlineSkuForm.stock,
      status: 1
    }
    
    if (editingSkuIndex.value >= 0) {
      // 更新现有SKU
      formSkus.value[editingSkuIndex.value] = skuData
    } else {
      // 添加新SKU
      formSkus.value.push(skuData)
    }
    
    inlineSkuFormVisible.value = false
    ElMessage.success(editingSkuIndex.value >= 0 ? t('merchantProducts.editSuccess') : t('merchantProducts.addSuccess'))
    editingSkuIndex.value = -1
  } catch (error) {
    if (error !== false) {
      console.error('保存SKU失败:', error)
    }
  }
}

onMounted(() => {
  loadCategories()
  loadBrands()
  loadShippingTemplates()
  loadAfterSaleRules()
  loadProducts()
})
</script>

<style scoped lang="scss">
.products-page {
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

  .sku-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h4 {
      margin: 0;
    }
  }

  .sku-management {
    .sku-header-inline {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 20px;
    }
  }

  .video-uploader {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .video-preview {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-top: 10px;
    }

    .upload-tip {
      color: #909399;
      font-size: 12px;
      margin-top: 5px;
    }
  }

  .image-uploader {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .image-upload {
      :deep(.el-upload) {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        width: 200px;
        height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fafafa;
        
        &:hover {
          border-color: #409eff;
        }
      }
      
      .upload-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
      }
      
      .upload-icon {
        font-size: 28px;
        color: #8c939d;
      }
    }
    
    .upload-tip {
      color: #909399;
      font-size: 12px;
      margin-top: 5px;
    }
  }
}
</style>

