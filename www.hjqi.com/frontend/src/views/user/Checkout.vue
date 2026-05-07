<template>
  <div class="checkout-page" v-loading="pageLoading" :element-loading-text="$t('common.loading')">
    <el-card>
      <template #header>
        <h3>{{ $t('checkout.title') }}</h3>
      </template>

      <el-row :gutter="20" v-if="!pageLoading">
        <!-- 左侧：订单信息 -->
        <el-col :span="16">
          <!-- 收货地址 -->
          <el-card class="section-card" style="margin-bottom: 20px">
            <template #header>
              <div class="section-header">
                <h4>{{ $t('checkout.shippingAddress') }}</h4>
                <el-button type="primary" link @click="showAddressDialog = true">
                  <el-icon><Plus /></el-icon>
                  {{ $t('address.addAddress') }}
                </el-button>
              </div>
            </template>

            <el-radio-group v-model="selectedAddressId" @change="handleAddressChange">
              <el-radio
                v-for="address in addresses"
                :key="address.id"
                :label="address.id"
                class="address-radio"
              >
                <div class="address-info">
                  <div class="address-header">
                    <span class="receiver-name">{{ address.receiverName }}</span>
                    <span class="receiver-phone">{{ address.receiverPhone }}</span>
                    <el-tag v-if="address.isDefault" type="success" size="small">{{ $t('address.default') }}</el-tag>
                  </div>
                  <div class="address-detail">
                    {{ address.fullAddress || `${address.province}${address.city}${address.district}${address.detailAddress}` }}
                  </div>
                </div>
              </el-radio>
            </el-radio-group>

            <el-empty v-if="addresses.length === 0" :description="$t('address.noAddress')">
              <el-button type="primary" @click="showAddressDialog = true">{{ $t('address.addAddress') }}</el-button>
            </el-empty>
          </el-card>

          <!-- 配送方式 -->
          <el-card class="section-card" style="margin-bottom: 20px">
            <template #header>
              <h4>{{ $t('checkout.deliveryMethod') }}</h4>
            </template>

            <el-radio-group v-model="form.deliveryType" @change="handleDeliveryTypeChange">
              <el-radio :label="1" class="delivery-radio">
                <div class="delivery-info">
                  <div class="delivery-name">{{ $t('checkout.selfPickup') }}</div>
                  <div class="delivery-desc">{{ $t('checkout.selfPickupDesc') }}</div>
                </div>
              </el-radio>
              <el-radio :label="2" class="delivery-radio">
                <div class="delivery-info">
                  <div class="delivery-name">{{ $t('checkout.cityDelivery') }}</div>
                  <div class="delivery-desc">{{ $t('checkout.cityDeliveryDesc') }}</div>
                </div>
              </el-radio>
            </el-radio-group>
          </el-card>

          <!-- 提货日期 -->
          <el-card class="section-card" style="margin-bottom: 20px">
            <template #header>
              <h4>{{ $t('checkout.pickupDate') }}</h4>
            </template>

            <el-date-picker
              v-model="form.expectedDate"
              type="date"
              :placeholder="$t('checkout.selectPickupDate')"
              :disabled-date="disabledDate"
              style="width: 100%"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
            />
          </el-card>

          <!-- 备注 -->
          <el-card class="section-card">
            <template #header>
              <h4>{{ $t('checkout.remark') }}</h4>
            </template>

            <el-input
              v-model="form.userRemark"
              type="textarea"
              :rows="4"
              :placeholder="$t('checkout.remarkPlaceholder')"
              maxlength="200"
              show-word-limit
            />
          </el-card>
        </el-col>

        <!-- 右侧：订单汇总 -->
        <el-col :span="8">
          <el-card class="order-summary">
            <template #header>
              <h4>{{ $t('checkout.orderSummary') }}</h4>
            </template>

            <div class="order-items">
              <div
                v-for="item in orderItems"
                :key="item.id"
                class="order-item"
              >
                <img :src="item.productImage || '/placeholder.png'" class="item-image" />
                <div class="item-info">
                  <div class="item-name">{{ item.productNameZh || item.productNameEn || $t('product.product') }}</div>
                  <div class="item-spec" v-if="item.skuName">{{ item.skuName }}</div>
                  <div class="item-quantity">x{{ item.quantity }}</div>
                </div>
                <div class="item-price">¥{{ formatMoney(item.price * item.quantity) }}</div>
              </div>
            </div>

            <el-divider />

            <div class="summary-row">
              <span>{{ $t('checkout.productTotal') }}</span>
              <span>¥{{ formatMoney(productAmount) }}</span>
            </div>
            <div class="summary-row">
              <span>{{ $t('checkout.deliveryFee') }}</span>
              <span v-if="form.deliveryType === 1" class="free-shipping">{{ $t('checkout.freeShippingSelfPickup') }}</span>
              <span v-else-if="isFreeShipping" class="free-shipping">{{ $t('checkout.freeShippingQualified') }}</span>
              <span v-else>¥{{ formatMoney(deliveryFee) }}</span>
            </div>
            <div class="shipping-tip" v-if="form.deliveryType === 2 && amountToFreeShipping > 0 && freeShippingAmount">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ $t('checkout.freeShippingTip', { amount: formatMoney(amountToFreeShipping) }) }}</span>
            </div>
            <div class="summary-row" v-if="discountAmount > 0">
              <span>{{ $t('checkout.discount') }}</span>
              <span class="discount">-¥{{ formatMoney(discountAmount) }}</span>
            </div>
            <el-divider />
            <div class="summary-row total">
              <span>{{ $t('checkout.total') }}</span>
              <span class="total-amount">¥{{ formatMoney(totalAmount) }}</span>
            </div>

            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              :disabled="!canSubmit"
              @click="handleSubmit"
              style="width: 100%; margin-top: 20px"
            >
              {{ $t('checkout.submitOrder') }}
            </el-button>

            <div class="submit-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ $t('checkout.submitTip') }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 地址管理对话框 -->
    <el-dialog
      v-model="showAddressDialog"
      :title="$t('address.manageAddress')"
      width="800px"
      @close="loadAddresses"
    >
      <Addresses @close="showAddressDialog = false" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import { getAddresses, createOrder, getCart, getProductDetail } from '@/api/user'
import { formatMoney } from '@/utils'
import Addresses from './Addresses.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const addresses = ref([])
const selectedAddressId = ref(null)
const selectedAddress = ref(null)
const orderItems = ref([])
const submitting = ref(false)
const showAddressDialog = ref(false)
const pageLoading = ref(true) // 页面加载状态

// 运费相关
const shippingTemplate = ref(null) // 运费模板
const defaultShippingFee = ref(0) // 默认运费（分）
const freeShippingAmount = ref(null) // 包邮金额（分）

const form = ref({
  deliveryType: 1, // 1-自提 2-同城配送
  expectedDate: '',
  userRemark: ''
})

// 计算订单金额（分为单位）
const productAmount = computed(() => {
  return orderItems.value.reduce((total, item) => {
    // item.price 是后端返回的分为单位
    return total + (item.price || 0) * item.quantity
  }, 0)
})

// 计算配送费（分为单位）
const deliveryFee = computed(() => {
  // 自提无配送费
  if (form.value.deliveryType === 1) {
    console.log('配送方式：自提，运费为0')
    return 0
  }
  
  // 同城配送，检查是否满足包邮条件
  // freeShippingAmount 是分为单位
  if (freeShippingAmount.value != null && freeShippingAmount.value > 0) {
    if (productAmount.value >= freeShippingAmount.value) {
      console.log('满足包邮条件，运费为0', {
        productAmount: productAmount.value,
        freeShippingAmount: freeShippingAmount.value
      })
      return 0 // 满足包邮条件
    }
  }
  
  // 返回默认运费（分为单位）
  const fee = defaultShippingFee.value || 0
  console.log('使用默认运费:', {
    defaultShippingFee: defaultShippingFee.value,
    fee: fee,
    productAmount: productAmount.value,
    freeShippingAmount: freeShippingAmount.value
  })
  return fee
})

// 是否满足包邮条件
const isFreeShipping = computed(() => {
  if (form.value.deliveryType === 1) return true // 自提不需要运费
  if (freeShippingAmount.value == null || freeShippingAmount.value <= 0) return false
  return productAmount.value >= freeShippingAmount.value
})

// 还差多少包邮（分为单位）
const amountToFreeShipping = computed(() => {
  if (form.value.deliveryType === 1) return 0
  if (freeShippingAmount.value == null || freeShippingAmount.value <= 0) return 0
  const diff = freeShippingAmount.value - productAmount.value
  return diff > 0 ? diff : 0
})

const discountAmount = computed(() => {
  // 优惠金额计算（暂时为0）
  return 0
})

const totalAmount = computed(() => {
  return productAmount.value + deliveryFee.value - discountAmount.value
})

// 是否可以提交
const canSubmit = computed(() => {
  return selectedAddressId.value != null &&
         form.value.deliveryType != null &&
         form.value.expectedDate != null &&
         form.value.expectedDate !== '' &&
         orderItems.value.length > 0
})

// 禁用日期（不能选择今天之前的日期）
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 86400000ms = 1天
}

// 加载地址列表
const loadAddresses = async () => {
  try {
    const result = await getAddresses()
    addresses.value = result || []
    
    // 自动选择默认地址
    const defaultAddress = addresses.value.find(addr => addr.isDefault)
    if (defaultAddress) {
      selectedAddressId.value = defaultAddress.id
      selectedAddress.value = defaultAddress
    } else if (addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
      selectedAddress.value = addresses.value[0]
    }
  } catch (error) {
    console.error('加载地址失败:', error)
    ElMessage.error(t('address.loadFailed'))
  }
}

// 地址选择变化
const handleAddressChange = (addressId) => {
  selectedAddress.value = addresses.value.find(addr => addr.id === addressId)
}

// 配送方式变化
const handleDeliveryTypeChange = (type) => {
  // 配送方式变化时的处理逻辑
  if (type === 1) {
    // 自提时，地址可以为空或使用默认地址
  } else {
    // 同城配送时，必须选择地址
    if (!selectedAddressId.value && addresses.value.length > 0) {
      selectedAddressId.value = addresses.value[0].id
      selectedAddress.value = addresses.value[0]
    }
  }
}

// 提交订单
const handleSubmit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning(t('checkout.completeInfo'))
    return
  }

  if (!selectedAddress.value) {
    ElMessage.warning(t('checkout.selectAddress'))
    return
  }

  if (!form.value.expectedDate) {
    ElMessage.warning(t('checkout.selectPickupDate'))
    return
  }

  try {
    submitting.value = true

    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (!merchantId) {
      ElMessage.warning(t('error.merchantNotFound'))
      return
    }

    // 构建订单请求
    const orderData = {
      merchantId: merchantId,
      deliveryType: form.value.deliveryType,
      receiverName: selectedAddress.value.receiverName,
      receiverPhone: selectedAddress.value.receiverPhone,
      receiverAddress: selectedAddress.value.fullAddress || 
                      `${selectedAddress.value.province}${selectedAddress.value.city}${selectedAddress.value.district}${selectedAddress.value.detailAddress}`,
      expectedDate: form.value.expectedDate,
      userRemark: form.value.userRemark || ''
    }

    console.log('提交订单数据:', {
      ...orderData,
      计算的运费_分: deliveryFee.value,
      商品总价_分: productAmount.value,
      订单总额_分: totalAmount.value
    })

    const order = await createOrder(orderData)
    
    console.log('创建订单返回:', order)
    
    ElMessage.success(t('checkout.orderCreated'))
    
    // 清空购物车 store（因为后端已经清空了购物车）
    cartStore.clearCart()
    console.log('✅ 购物车已清空')
    
    // 跳转到支付说明页，带上前端计算的金额
    router.push({
      path: '/user/orders/payment',
      query: {
        orderId: order.id,
        orderNo: order.orderNo,
        productAmount: productAmount.value,  // 商品总价（分）
        deliveryFee: deliveryFee.value,      // 配送费（分）
        totalAmount: totalAmount.value       // 订单总额（分）
      }
    })
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.response?.data?.message || t('checkout.orderFailed'))
  } finally {
    submitting.value = false
  }
}

// 加载购物车选中项
const loadCartItems = async () => {
  try {
    const cartItems = await getCart()
    // 只加载选中的商品
    const selectedCartItems = cartItems.filter(item => item.selected)
    
    if (selectedCartItems.length === 0) {
      ElMessage.warning(t('cart.selectProducts'))
      router.push('/user/cart')
      return
    }

    // 转换为订单项格式
    orderItems.value = selectedCartItems.map(item => ({
      id: item.id,
      productId: item.productId,
      skuId: item.skuId,
      quantity: item.quantity,
      price: item.price || 0,
      productNameZh: item.productNameZh || '',
      productNameEn: item.productNameEn || '',
      productImage: item.productImage || '',
      skuName: item.specNameZh || item.specNameEn || ''
    }))

    // 加载第一个商品的运费模板信息
    if (selectedCartItems.length > 0) {
      try {
        const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
        const firstItem = selectedCartItems[0]
        const productDetail = await getProductDetail(firstItem.productId, merchantId)
        console.log('商品详情:', productDetail)
        if (productDetail.shippingTemplate) {
          shippingTemplate.value = productDetail.shippingTemplate
          // 后端返回的是分为单位，直接使用
          const template = productDetail.shippingTemplate
          defaultShippingFee.value = template.defaultFee || 0
          freeShippingAmount.value = template.freeShippingAmount || null
          console.log('运费模板加载成功:', {
            templateId: template.id,
            templateName: template.name,
            defaultFee_分: template.defaultFee,
            defaultShippingFee_分: defaultShippingFee.value,
            freeShippingAmount_分: template.freeShippingAmount,
            freeShippingAmount_变量: freeShippingAmount.value
          })
        } else {
          console.warn('商品没有运费模板')
        }
      } catch (e) {
        console.error('加载运费模板失败:', e)
      }
    }
  } catch (error) {
    console.error('加载购物车失败', error)
    ElMessage.error(t('cart.loadFailed'))
    router.push('/user/cart')
  }
}

onMounted(async () => {
  pageLoading.value = true
  try {
    await Promise.all([
      loadAddresses(),
      loadCartItems()
    ])
  } finally {
    pageLoading.value = false
  }
  
  // 设置默认提货日期为明天
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  form.value.expectedDate = tomorrow.toISOString().split('T')[0]
})
</script>

<style scoped lang="scss">
.checkout-page {
  max-width: 1200px;
  margin: 0 auto;

  .section-card {
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h4 {
        margin: 0;
        font-size: 16px;
      }
    }

    :deep(.el-radio-group) {
      display: flex;
      flex-direction: column;
      width: 100%;
    }

    :deep(.el-radio) {
      height: auto !important;
      line-height: normal !important;
      white-space: normal !important;
      align-items: flex-start;
      margin-right: 0;
    }

    :deep(.el-radio__label) {
      white-space: normal;
      line-height: 1.5;
      padding-left: 8px;
    }

    :deep(.el-radio__input) {
      margin-top: 4px;
    }

    .address-radio {
      display: flex;
      width: 100%;
      margin-bottom: 12px;
      padding: 16px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      transition: all 0.3s;
      box-sizing: border-box;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        border-color: #409eff;
        background: #f5f7fa;
      }

      &.is-checked {
        border-color: #409eff;
        background: #ecf5ff;
      }

      .address-info {
        flex: 1;

        .address-header {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;
          flex-wrap: wrap;

          .receiver-name {
            font-weight: bold;
            font-size: 16px;
            color: #303133;
          }

          .receiver-phone {
            color: #606266;
            font-size: 14px;
          }
        }

        .address-detail {
          color: #606266;
          font-size: 14px;
          line-height: 1.6;
          word-break: break-all;
        }
      }
    }

    .delivery-radio {
      display: flex;
      width: 100%;
      margin-bottom: 12px;
      padding: 20px;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      transition: all 0.3s;
      box-sizing: border-box;

      &:last-child {
        margin-bottom: 0;
      }

      &:hover {
        border-color: #409eff;
        background: #f5f7fa;
      }

      &.is-checked {
        border-color: #409eff;
        background: #ecf5ff;
      }

      .delivery-info {
        flex: 1;

        .delivery-name {
          font-weight: bold;
          font-size: 16px;
          margin-bottom: 6px;
          color: #303133;
        }

        .delivery-desc {
          color: #909399;
          font-size: 14px;
          line-height: 1.5;
        }
      }
    }
  }

  .order-summary {
    position: sticky;
    top: 20px;

    .order-items {
      max-height: 400px;
      overflow-y: auto;

      .order-item {
        display: flex;
        gap: 12px;
        margin-bottom: 16px;
        padding-bottom: 16px;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
          margin-bottom: 0;
          padding-bottom: 0;
        }

        .item-image {
          width: 60px;
          height: 60px;
          object-fit: cover;
          border-radius: 4px;
        }

        .item-info {
          flex: 1;
          min-width: 0;

          .item-name {
            font-weight: 500;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .item-spec {
            font-size: 12px;
            color: #909399;
            margin-bottom: 4px;
          }

          .item-quantity {
            font-size: 12px;
            color: #909399;
          }
        }

        .item-price {
          font-weight: bold;
          color: #f56c6c;
        }
      }
    }

    .summary-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 12px;
      font-size: 14px;

      &.total {
        font-size: 18px;
        font-weight: bold;

        .total-amount {
          color: #f56c6c;
          font-size: 24px;
        }
      }

      .discount {
        color: #67c23a;
      }

      .free-shipping {
        color: #67c23a;
        font-weight: 500;
      }
    }

    .shipping-tip {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-bottom: 12px;
      padding: 8px 12px;
      background: #fdf6ec;
      border-radius: 4px;
      font-size: 12px;
      color: #e6a23c;
    }

    .submit-tips {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 12px;
      font-size: 12px;
      color: #909399;
    }
  }
}
</style>
