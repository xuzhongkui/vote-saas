<template>
  <div class="product-detail-page">
    <el-card v-if="product">
      <el-row :gutter="40">
        <el-col :span="10">
          <!-- 主图展示 -->
          <div class="image-gallery">
            <div class="main-image-container">
              <el-image 
                :src="currentImage || product.mainImage || '/placeholder.png'" 
                class="main-image"
                fit="cover"
                :preview-src-list="allImages"
                :initial-index="currentImageIndex"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                    <span>{{ $t('product.noImage') }}</span>
                  </div>
                </template>
              </el-image>
            </div>
            <!-- 缩略图列表 -->
            <div class="thumbnail-list" v-if="allImages.length > 1">
              <div 
                v-for="(img, index) in allImages" 
                :key="index"
                :class="['thumbnail-item', { 'active': currentImageIndex === index }]"
                @click="selectImage(index)"
              >
                <el-image :src="img" fit="cover">
                  <template #error>
                    <div class="thumb-error"><el-icon><Picture /></el-icon></div>
                  </template>
                </el-image>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="14">
          <h2>{{ product.name }}</h2>
          <div class="price-section">
            <div class="price-info">
              <span class="price-label">{{ $t('product.unitPrice') }}：</span>
              <span class="price">¥{{ displayPrice }}</span>
            </div>
            <div class="total-price-info" v-if="quantity > 1">
              <span class="price-label">{{ $t('product.totalPrice') }}：</span>
              <span class="total-price">¥{{ totalPrice }}</span>
            </div>
          </div>
          <el-divider />
          
          <div class="product-info">
            <p><strong>{{ $t('product.description') }}：</strong>{{ productDescription }}</p>
            <p><strong>{{ $t('product.stock') }}：</strong>{{ totalStock }} {{ $t('product.pieces') }}</p>
            <p v-if="product.categoryName"><strong>{{ $t('product.category') }}：</strong>{{ product.categoryName }}</p>
          </div>

          <!-- SKU选择 -->
          <div class="sku-section" v-if="skus && skus.length > 0">
            <el-divider />
            <h4 class="section-title">
              <el-icon><Box /></el-icon>
              {{ $t('product.selectSpec') }}
            </h4>
            <div class="sku-list">
              <div
                v-for="sku in skus"
                :key="sku.id"
                :class="['sku-item', { 'selected': selectedSku === sku.id, 'out-of-stock': sku.stock <= 0 }]"
                @click="selectSku(sku)"
              >
                <div class="sku-image" v-if="sku.image">
                  <img :src="sku.image" alt="" />
                </div>
                <div class="sku-info">
                  <div class="sku-name">{{ sku.skuName }}</div>
                  <div class="sku-price">¥{{ formatPrice(sku.price) }}</div>
                  <div class="sku-stock" v-if="sku.stock <= 0">{{ $t('product.outOfStock') }}</div>
                  <div class="sku-stock" v-else>{{ $t('product.stock') }}：{{ sku.stock }}</div>
                </div>
              </div>
            </div>
          </div>

          <el-divider />

          <div class="quantity-section">
            <span>{{ $t('product.quantity') }}：</span>
            <el-input-number v-model="quantity" :min="1" :max="maxStock" @change="handleQuantityChange" />
            <span class="total-price-display" v-if="quantity > 1">
              （{{ $t('product.subtotal') }}：¥{{ totalPrice }}）
            </span>
          </div>

          <div class="actions">
            <el-button type="primary" size="large" @click="handleAddToCart">
              <el-icon><ShoppingCart /></el-icon>
              {{ $t('product.addToCart') }}
            </el-button>
            <el-button type="success" size="large" @click="showChat = true" v-if="product.merchantId">
              <el-icon><ChatLineRound /></el-icon>
              {{ $t('product.contactService') }}
            </el-button>
            <el-button size="large" @click="$router.back()">{{ $t('common.back') }}</el-button>
          </div>
        </el-col>
      </el-row>

      <el-divider />

      <!-- 运费信息 -->
      <div class="shipping-section" v-if="product.shippingTemplate">
        <h3 class="section-title">
          <el-icon><Van /></el-icon>
          {{ $t('product.shippingInfo') }}
        </h3>
        <div class="shipping-info-simple">
          <div class="shipping-fee">
            <span class="label">{{ $t('product.shippingFee') }}：</span>
            <span class="value">¥{{ (product.shippingTemplate.defaultFee / 100).toFixed(2) }}</span>
          </div>
          <div class="shipping-free" v-if="product.shippingTemplate.freeShippingAmount">
            <el-tag type="success" size="large">
              {{ $t('product.freeShippingOver', { amount: (product.shippingTemplate.freeShippingAmount / 100).toFixed(2) }) }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 售后规则信息 -->
      <div class="after-sale-section" v-if="product.afterSaleRule">
        <h3 class="section-title">
          <el-icon><Service /></el-icon>
          {{ $t('product.afterSaleGuarantee') }}
        </h3>
        <el-card shadow="never" class="info-card">
          <div class="after-sale-info">
            <div class="after-sale-title">
              <strong>{{ afterSaleTitle }}</strong>
              <el-tag v-if="product.afterSaleRule.validDays" type="info" size="small">
                {{ $t('product.validPeriod', { days: product.afterSaleRule.validDays }) }}
              </el-tag>
            </div>
            <div class="after-sale-content">
              {{ afterSaleContent }}
            </div>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 客服聊天窗口 -->
    <ChatWindow 
      v-if="showChat && product"
      :merchant-id="product.merchantId"
      @close="showChat = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useLocaleStore } from '@/stores/locale'
import { getProductDetail } from '@/api/user'
import { addToCart as addToCartApi } from '@/api/user'
import { ElMessage } from 'element-plus'
import { ChatLineRound, Box, Van, Service, ShoppingCart, Picture } from '@element-plus/icons-vue'
import ChatWindow from '@/components/ChatWindow.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const localeStore = useLocaleStore()

const product = ref(null)
const skus = ref([])
const selectedSku = ref(null)
const quantity = ref(1)
const showChat = ref(false)
const currentImageIndex = ref(0)

// 所有图片（主图 + 详情图）
const allImages = computed(() => {
  const images = []
  if (product.value?.mainImage) {
    images.push(product.value.mainImage)
  }
  if (product.value?.images && Array.isArray(product.value.images)) {
    images.push(...product.value.images)
  }
  return images
})

// 当前显示的图片
const currentImage = computed(() => {
  return allImages.value[currentImageIndex.value] || product.value?.mainImage || ''
})

// 根据语言选择售后规则标题
const afterSaleTitle = computed(() => {
  if (!product.value?.afterSaleRule) return ''
  const rule = product.value.afterSaleRule
  if (localeStore.isEnglish) {
    return rule.titleEn || rule.titleZh || ''
  }
  return rule.titleZh || rule.titleEn || ''
})

// 根据语言选择售后规则内容
const afterSaleContent = computed(() => {
  if (!product.value?.afterSaleRule) return ''
  const rule = product.value.afterSaleRule
  if (localeStore.isEnglish) {
    return rule.contentEn || rule.contentZh || ''
  }
  return rule.contentZh || rule.contentEn || ''
})

// 根据语言选择商品描述
const productDescription = computed(() => {
  if (!product.value) return t('product.noDescription')
  if (localeStore.isEnglish) {
    return product.value.descriptionEn || product.value.descriptionZh || t('product.noDescription')
  }
  return product.value.descriptionZh || product.value.descriptionEn || t('product.noDescription')
})

// 选择图片
const selectImage = (index) => {
  currentImageIndex.value = index
}

// 计算总库存（如果有SKU，则为所有SKU库存之和；否则为商品库存）
const totalStock = computed(() => {
  if (skus.value && skus.value.length > 0) {
    return skus.value.reduce((sum, sku) => sum + (sku.stock || 0), 0)
  }
  return product.value?.stock || 0
})

// 判断价格是否需要转换
// 后端返回的 BigDecimal 是元为单位，直接使用即可
const normalizePrice = (price) => {
  if (price == null) return 0
  const numPrice = Number(price)
  if (isNaN(numPrice)) return 0
  return numPrice
}

// 格式化价格显示（分转元，保留2位小数）
const formatPrice = (price) => {
  if (price == null) return '0.00'
  const numPrice = Number(price)
  if (isNaN(numPrice)) return '0.00'
  return (numPrice / 100).toFixed(2)
}

// 获取当前单价（元为单位）
const currentUnitPrice = computed(() => {
  // 如果选择了SKU，使用SKU价格
  if (selectedSku.value) {
    const sku = skus.value.find(s => s.id === selectedSku.value)
    if (sku && sku.price != null) {
      return normalizePrice(sku.price)
    }
  }
  // 如果有SKU但没有选择，返回最低SKU价格
  if (skus.value && skus.value.length > 0) {
    const availableSkus = skus.value.filter(sku => sku.stock > 0 && sku.price != null)
    if (availableSkus.length > 0) {
      const prices = availableSkus.map(sku => normalizePrice(sku.price)).filter(p => p > 0)
      if (prices.length > 0) {
        return Math.min(...prices)
      }
    }
    // 如果没有有库存的SKU，返回所有SKU的最低价格
    const allPrices = skus.value.filter(sku => sku.price != null).map(sku => normalizePrice(sku.price)).filter(p => p > 0)
    if (allPrices.length > 0) {
      return Math.min(...allPrices)
    }
  }
  // 没有SKU，使用商品价格
  return normalizePrice(product.value?.price)
})

// 显示价格（根据选择的SKU动态变化）
const displayPrice = computed(() => {
  return formatPrice(currentUnitPrice.value)
})

// 总价（单价 × 数量，分转元显示）
const totalPrice = computed(() => {
  const unitPrice = currentUnitPrice.value
  const qty = quantity.value || 1
  if (unitPrice == null || isNaN(unitPrice)) {
    return '0.00'
  }
  const total = unitPrice * qty
  return (total / 100).toFixed(2)
})

const maxStock = computed(() => {
  let stock = 0
  if (selectedSku.value) {
    // 选择了SKU，使用该SKU的库存
    const sku = skus.value.find(s => s.id === selectedSku.value)
    stock = sku?.stock || 0
  } else if (skus.value && skus.value.length > 0) {
    // 有SKU但未选择，使用所有SKU库存之和
    stock = skus.value.reduce((sum, sku) => sum + (sku.stock || 0), 0)
  } else {
    // 没有SKU，使用商品库存
    stock = product.value?.stock || 0
  }
  // 确保 max 至少为 1，避免 min(1) > max(0) 的错误
  return Math.max(1, stock)
})

const selectSku = (sku) => {
  if (sku.stock <= 0) {
    ElMessage.warning(t('product.specOutOfStock'))
    return
  }
  selectedSku.value = sku.id
  quantity.value = 1
}

// 数量变化处理
const handleQuantityChange = (value) => {
  if (value < 1) {
    quantity.value = 1
  } else if (value > maxStock.value) {
    quantity.value = maxStock.value
    ElMessage.warning(t('product.maxPurchase', { max: maxStock.value }))
  }
}

const loadProduct = async () => {
  try {
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      ElMessage.warning(t('error.merchantNotFound'))
      router.back()
      return
    }
    const result = await getProductDetail(route.params.id, merchantId)
    console.log('=== 商品详情数据 ===')
    console.log('product:', result)
    console.log('product.price:', result.price, typeof result.price)
    console.log('skus:', result.skus)
    if (result.skus && result.skus.length > 0) {
      result.skus.forEach((sku, i) => {
        console.log(`sku[${i}].price:`, sku.price, typeof sku.price)
      })
    }
    product.value = result
    skus.value = result.skus || []
    
    // 重置图片索引
    currentImageIndex.value = 0
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error(t('product.notFound'))
    router.back()
  }
}

const handleAddToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning(t('auth.pleaseLogin'))
    router.push('/login')
    return
  }

  // 如果有SKU但没有选择，提示用户选择
  if (skus.value && skus.value.length > 0 && !selectedSku.value) {
    ElMessage.warning(t('product.pleaseSelectSpec'))
    return
  }

  if (maxStock.value <= 0) {
    ElMessage.warning(t('product.insufficientStock'))
    return
  }

  if (quantity.value < 1) {
    ElMessage.warning(t('product.quantityMin'))
    return
  }

  try {
    const unitPrice = currentUnitPrice.value || 0
    const total = unitPrice * quantity.value
    const totalStr = isNaN(total) ? '0.00' : (total / 100).toFixed(2)
    
    await addToCartApi({
      productId: product.value.id,
      skuId: selectedSku.value || null,
      quantity: quantity.value
    })
    ElMessage.success(t('product.addedToCart', { quantity: quantity.value, total: totalStr }))
    
    // 重新加载购物车数据，确保数量准确
    try {
      const { getCart } = await import('@/api/user')
      const cartItems = await getCart()
      cartStore.setCartItems(cartItems)
      console.log('✅ 购物车数据已更新:', cartItems.length, '件商品')
    } catch (error) {
      console.error('更新购物车数据失败:', error)
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error(t('product.addToCartFailed'))
  }
}

onMounted(() => {
  loadProduct()
})
</script>

<style scoped lang="scss">
.product-detail-page {
  max-width: 1200px;
  margin: 0 auto;

  .image-gallery {
    .main-image-container {
      width: 100%;
      aspect-ratio: 1;
      border-radius: 8px;
      overflow: hidden;
      background: #f5f7fa;

      .main-image {
        width: 100%;
        height: 100%;
      }

      .image-error {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #909399;
        font-size: 14px;
        gap: 8px;

        .el-icon {
          font-size: 48px;
        }
      }
    }

    .thumbnail-list {
      display: flex;
      gap: 10px;
      margin-top: 12px;
      flex-wrap: wrap;

      .thumbnail-item {
        width: 60px;
        height: 60px;
        border-radius: 4px;
        overflow: hidden;
        cursor: pointer;
        border: 2px solid transparent;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
        }

        &.active {
          border-color: #409eff;
          box-shadow: 0 0 8px rgba(64, 158, 255, 0.4);
        }

        .el-image {
          width: 100%;
          height: 100%;
        }

        .thumb-error {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: #f5f7fa;
          color: #909399;
        }
      }
    }
  }

  .main-image {
    width: 100%;
    border-radius: 8px;
  }

  h2 {
    margin: 0 0 20px 0;
    font-size: 24px;
  }

  .price-section {
    margin: 20px 0;

    .price-info {
      display: flex;
      align-items: baseline;
      gap: 8px;
      margin-bottom: 8px;

      .price-label {
        color: #909399;
        font-size: 16px;
      }

      .price {
        color: #f56c6c;
        font-size: 32px;
        font-weight: bold;
      }
    }

    .total-price-info {
      display: flex;
      align-items: baseline;
      gap: 8px;
      margin-top: 8px;

      .price-label {
        color: #909399;
        font-size: 14px;
      }

      .total-price {
        color: #409eff;
        font-size: 24px;
        font-weight: bold;
      }
    }
  }

  .price {
    color: #f56c6c;
    font-size: 32px;
    font-weight: bold;
  }

  .product-info {
    p {
      margin: 10px 0;
      line-height: 1.8;
    }
  }

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 20px 0 16px 0;
    font-size: 18px;
    color: #303133;
  }

  .sku-section {
    margin: 20px 0;

    .sku-list {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-top: 12px;
    }

    .sku-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 16px;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      min-width: 200px;

      &:hover {
        border-color: #409eff;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
      }

      &.selected {
        border-color: #409eff;
        background: #ecf5ff;
      }

      &.out-of-stock {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .sku-image {
        width: 50px;
        height: 50px;
        border-radius: 4px;
        overflow: hidden;
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .sku-info {
        flex: 1;
        min-width: 0;

        .sku-name {
          font-weight: 500;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .sku-price {
          color: #f56c6c;
          font-size: 16px;
          font-weight: bold;
          margin-bottom: 4px;
        }

        .sku-stock {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .shipping-section {
    margin: 20px 0;

    .shipping-info-simple {
      display: flex;
      align-items: center;
      gap: 20px;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;

      .shipping-fee {
        .label {
          color: #606266;
        }
        .value {
          color: #f56c6c;
          font-size: 18px;
          font-weight: bold;
        }
      }

      .shipping-free {
        .el-tag {
          font-size: 14px;
        }
      }
    }
  }

  .after-sale-section {
    margin: 20px 0;
  }

  .info-card {
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-top: 12px;
  }

  .after-sale-info {
    .after-sale-title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: 16px;
      color: #303133;
    }

    .after-sale-content {
      color: #606266;
      line-height: 1.8;
      white-space: pre-wrap;
    }
  }

  .quantity-section {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;

    .total-price-display {
      margin-left: 12px;
      color: #409eff;
      font-size: 16px;
      font-weight: 500;
    }
  }

  .actions {
    display: flex;
    gap: 10px;
  }
}
</style>

