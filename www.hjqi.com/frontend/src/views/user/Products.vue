<template>
  <div class="products-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('menu.products') }}</h3>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              :placeholder="$t('common.search') + '...'"
              clearable
              style="width: 300px; margin-right: 16px"
              @keyup.enter="loadProducts"
              @clear="loadProducts"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select v-model="selectedCategory" :placeholder="$t('common.pleaseSelect')" clearable @change="loadProducts" style="width: 200px">
              <el-option :label="$t('common.all')" :value="null" />
              <el-option
                v-for="cat in categories"
                :key="cat.id"
                :label="cat.nameZh || cat.nameEn || $t('product.category')"
                :value="cat.id"
              />
            </el-select>
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" shadow="hover" @click="goToDetail(product.id)">
            <img :src="product.mainImage || '/placeholder.png'" class="product-image" />
            <div class="product-info">
              <h4 class="product-name">{{ product.nameZh || product.nameEn || '未命名商品' }}</h4>
              <p class="product-desc">{{ product.subtitleZh || product.subtitleEn || '' }}</p>
              <div class="product-footer">
                <div class="price-section">
                  <span class="price">¥{{ getDisplayPrice(product) }}</span>
                  <span v-if="hasMultiplePrices(product)" class="price-range">起</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="products.length === 0" :description="$t('common.noData')" />

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadProducts"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { getProducts, getCategories } from '@/api/user'
import { formatMoney } from '@/utils'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()

const categories = ref([])
const products = ref([])
const selectedCategory = ref(null)
const searchKeyword = ref('')
const page = ref(1)
const size = ref(12)
const total = ref(0)

const loadCategories = async () => {
  try {
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      console.warn('未找到 merchantId，无法加载分类')
      return
    }
    categories.value = await getCategories(merchantId)
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadProducts = async () => {
  try {
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      console.warn('未找到 merchantId，无法加载商品')
      ElMessage.warning(t('common.tip'))
      return
    }
    
    const params = {
      merchantId: merchantId,
      page: page.value,
      size: size.value,
      categoryId: selectedCategory.value,
      keyword: searchKeyword.value || undefined
    }
    const result = await getProducts(params)
    products.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error(t('common.failed'))
  }
}

const goToDetail = (id) => {
  router.push(`/user/products/${id}`)
}

// 获取商品显示价格（如果有SKU，显示最低价格）
const getDisplayPrice = (product) => {
  if (product.skus && product.skus.length > 0) {
    // 找到有库存的SKU的最低价格
    const availableSkus = product.skus.filter(sku => sku.stock > 0)
    if (availableSkus.length > 0) {
      const minPrice = Math.min(...availableSkus.map(sku => sku.price))
      return formatMoney(minPrice)
    }
    // 如果没有有库存的SKU，显示所有SKU的最低价格
    const minPrice = Math.min(...product.skus.map(sku => sku.price))
    return formatMoney(minPrice)
  }
  return formatMoney(product.price)
}

// 判断是否有多个价格（有SKU且价格不同）
const hasMultiplePrices = (product) => {
  if (product.skus && product.skus.length > 1) {
    const prices = product.skus.map(sku => sku.price)
    const uniquePrices = [...new Set(prices)]
    return uniquePrices.length > 1
  }
  return false
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>

<style scoped lang="scss">
.products-page {
  max-width: 1200px;
  margin: 0 auto;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
    }
  }

  .product-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-5px);
    }

    .product-image {
      width: 100%;
      height: 200px;
      object-fit: cover;
      border-radius: 4px;
    }

    .product-info {
      padding: 10px 0;

      .product-name {
        margin: 10px 0;
        font-size: 16px;
        font-weight: bold;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-desc {
        color: #909399;
        font-size: 14px;
        margin: 5px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .product-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 10px;

        .price-section {
          display: flex;
          align-items: baseline;
          gap: 4px;

          .price {
            color: #f56c6c;
            font-size: 20px;
            font-weight: bold;
          }

          .price-range {
            color: #909399;
            font-size: 14px;
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style>

