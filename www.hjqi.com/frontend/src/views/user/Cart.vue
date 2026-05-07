<template>
  <div class="cart-page" v-loading="loading" :element-loading-text="$t('common.loading')">
    <el-card>
      <template #header>
        <h3>{{ $t('cart.myCart') }}</h3>
      </template>

      <el-table ref="cartTableRef" :data="cartItems" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" />
        <el-table-column :label="$t('product.productName')" min-width="300">
          <template #default="{ row }">
            <div class="product-info">
              <img :src="row.productImage || '/placeholder.png'" class="product-image" />
              <div>
                <div class="product-name">{{ row.productNameZh || row.productNameEn }}</div>
                <div class="sku-name" v-if="row.specNameZh || row.specNameEn">{{ row.specNameZh || row.specNameEn }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="$t('product.productPrice')" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.price) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('cart.quantity')" width="150">
          <template #default="{ row }">
            <el-input-number
              v-model="row.quantity"
              :min="1"
              :max="row.stock || 999"
              @change="handleQuantityChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="$t('cart.subtotal')" width="120">
          <template #default="{ row }">
            <span class="subtotal">
              ¥{{ formatMoney(row.price * row.quantity) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">{{ $t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="cartItems.length === 0" :description="$t('cart.cartEmpty')" />

      <div class="cart-footer" v-if="cartItems.length > 0">
        <div class="total-info">
          <span>{{ $t('cart.selectedItems') }} {{ selectedItems.length }} {{ $t('common.items') }}</span>
          <span class="total-amount">
            {{ $t('cart.totalPrice') }}：<span class="amount">¥{{ formatMoney(totalAmount) }}</span>
          </span>
        </div>
        <el-button
          type="primary"
          size="large"
          :disabled="selectedItems.length === 0"
          @click="handleCheckout"
        >
          {{ $t('cart.checkout') }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useCartStore } from '@/stores/cart'
import { getCart, updateCart, deleteCart } from '@/api/user'
import { formatMoney } from '@/utils'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const { t } = useI18n()
const cartStore = useCartStore()

const cartTableRef = ref(null)
const cartItems = ref([])
const selectedItems = ref([])
const loading = ref(true)
const isInitializing = ref(false) // 防止初始化时触发 selection-change

const totalAmount = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + (item.price || 0) * item.quantity
  }, 0)
})

const loadCart = async () => {
  loading.value = true
  isInitializing.value = true
  try {
    const result = await getCart()
    cartItems.value = result
    cartStore.setCartItems(result)
    
    // 等待 DOM 更新后，根据后端返回的 selected 状态设置默认选中项
    await nextTick()
    if (cartTableRef.value) {
      result.forEach(item => {
        if (item.selected) {
          cartTableRef.value.toggleRowSelection(item, true)
        }
      })
      // 更新 selectedItems
      selectedItems.value = result.filter(item => item.selected)
    }
  } catch (error) {
    console.error('加载购物车失败:', error)
  } finally {
    loading.value = false
    // 延迟一下再关闭初始化标志，确保 selection-change 事件处理完毕
    setTimeout(() => {
      isInitializing.value = false
    }, 100)
  }
}

const handleSelectionChange = async (selection) => {
  selectedItems.value = selection
  
  // 初始化时不触发后端更新
  if (isInitializing.value) {
    return
  }
  
  // 同步更新后端的 selected 状态
  const selectedIds = selection.map(item => item.id)
  for (const item of cartItems.value) {
    const shouldBeSelected = selectedIds.includes(item.id)
    // 只有当状态发生变化时才更新
    if (item.selected !== shouldBeSelected) {
      try {
        await updateCart(item.id, { selected: shouldBeSelected })
        item.selected = shouldBeSelected
      } catch (error) {
        console.error('更新选中状态失败:', error)
      }
    }
  }
}

const handleQuantityChange = async (row) => {
  try {
    await updateCart(row.id, { quantity: row.quantity })
    cartStore.updateCartItem(row.id, { quantity: row.quantity })
  } catch (error) {
    console.error('更新数量失败:', error)
    loadCart()
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(t('common.tip'), t('common.warning'), {
      type: 'warning'
    })
    await deleteCart(row.id)
    cartStore.removeCartItem(row.id)
    ElMessage.success(t('common.success'))
    loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleCheckout = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning(t('common.pleaseSelect'))
    return
  }
  // 将选中的商品信息传递到订单页面
  router.push({
    path: '/user/checkout',
    query: {
      items: JSON.stringify(selectedItems.value.map(item => ({
        cartItemId: item.id,
        productId: item.productId,
        skuId: item.skuId,
        quantity: item.quantity
      })))
    }
  })
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped lang="scss">
.cart-page {
  max-width: 1200px;
  margin: 0 auto;

  .product-info {
    display: flex;
    align-items: center;
    gap: 10px;

    .product-image {
      width: 60px;
      height: 60px;
      object-fit: cover;
      border-radius: 4px;
    }

    .product-name {
      font-weight: bold;
      margin-bottom: 5px;
    }

    .sku-name {
      font-size: 12px;
      color: #909399;
    }
  }

  .subtotal {
    color: #f56c6c;
    font-weight: bold;
  }

  .cart-footer {
    margin-top: 20px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .total-info {
      display: flex;
      align-items: center;
      gap: 40px;

      .total-amount {
        font-size: 16px;

        .amount {
          color: #f56c6c;
          font-size: 24px;
          font-weight: bold;
        }
      }
    }
  }
}
</style>

