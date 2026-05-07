<template>
  <div class="user-layout">
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-content">
          <div class="logo">
            <el-icon><ShoppingBag /></el-icon>
            <span>{{ $t('menu.cart') }}</span>
          </div>
          
          <el-menu
            mode="horizontal"
            :default-active="activeMenu"
            router
            class="nav-menu"
          >
            <el-menu-item index="/user/products">
              {{ $t('menu.products') }}
            </el-menu-item>
            <el-menu-item index="/user/cart" v-if="userStore.isLoggedIn">
              <el-badge :value="cartStore.cartCount" :hidden="cartStore.cartCount === 0">
                {{ $t('menu.cart') }}
              </el-badge>
            </el-menu-item>
            <el-menu-item index="/user/orders" v-if="userStore.isLoggedIn">
              {{ $t('order.myOrders') }}
            </el-menu-item>
            <el-menu-item index="/user/chat" v-if="userStore.isLoggedIn">
              <el-badge :value="chatUnreadCount" :hidden="chatUnreadCount === 0">
                <el-icon style="margin-right: 4px;"><ChatDotRound /></el-icon>
                {{ $t('menu.customerService') }}
              </el-badge>
            </el-menu-item>
          </el-menu>

          <div class="user-actions">
            <!-- 语言切换器组件 -->
            <LanguageSwitcher user-type="user" size="small" />
            
            <template v-if="userStore.isLoggedIn">
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <el-icon><User /></el-icon>
                  {{ userStore.userInfo?.username || $t('auth.username') }}
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="member-center">
                      {{ $t('menu.memberCenter') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="profile">
                      {{ $t('menu.profile') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="addresses">
                      {{ $t('menu.addresses') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="chat">
                      <el-icon><ChatDotRound /></el-icon>
                      {{ $t('menu.customerService') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="merchant" v-if="userStore.isMerchant">
                      {{ $t('menu.dashboard') }}
                    </el-dropdown-item>
                    <el-dropdown-item command="admin" v-if="userStore.isAdmin">
                      {{ $t('menu.dashboard') }}
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      {{ $t('auth.logout') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            <template v-else>
              <el-button type="primary" link @click="$router.push('/login')">
                {{ $t('auth.login') }}
              </el-button>
              <el-button type="primary" @click="$router.push('/register')">
                {{ $t('auth.register') }}
              </el-button>
            </template>
          </div>
        </div>
      </el-header>

      <!-- 主体内容 -->
      <el-main class="main-content">
        <router-view />
      </el-main>

      <!-- 底部 -->
      <el-footer class="footer">
        <div class="footer-content">
          <p>&copy; 2025 {{ $t('menu.cart') }} SaaS</p>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { useChatStore } from '@/stores/chat'
import { useLocaleStore } from '@/stores/locale'
import { ElMessage } from 'element-plus'
import { ChatDotRound, User, ShoppingBag } from '@element-plus/icons-vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import { initStompClient, disconnectStomp, subscribeToTopic } from '@/utils/websocket-stomp'
import { getCart } from '@/api/user'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const userStore = useUserStore()
const cartStore = useCartStore()
const chatStore = useChatStore()
const localeStore = useLocaleStore()

// 聊天未读消息数（从 store 获取）
const chatUnreadCount = computed(() => chatStore.merchantUnreadCount)

// 加载购物车数据
const loadCartData = async () => {
  if (userStore.isLoggedIn) {
    try {
      const cartItems = await getCart()
      cartStore.setCartItems(cartItems)
      console.log('✅ 购物车数据已加载:', cartItems.length, '件商品')
    } catch (error) {
      console.error('加载购物车失败:', error)
    }
  }
}

// 初始化WebSocket连接
onMounted(() => {
  if (userStore.isLoggedIn && userStore.userInfo?.id) {
    // 加载购物车数据
    loadCartData()
    
    initStompClient('user', userStore.userInfo.id)
    
    // 订阅聊天消息，更新未读数
    const userId = userStore.userInfo.id
    subscribeToTopic(`/topic/user/${userId}/chat`, (data) => {
      if (data.senderType === 'MERCHANT') {
        // 如果不在聊天页面，增加未读数
        if (!route.path.includes('/user/chat')) {
          chatStore.incrementMerchantUnread()
        }
      }
    })
    
    // 加载初始未读数（需要 merchantId）
    const merchantId = userStore.userInfo?.merchantId || userStore.userInfo?.currentMerchantId
    if (merchantId) {
      chatStore.loadMerchantUnreadCount(merchantId)
    }
  }
})

// 监听路由变化，进入聊天页面时清零未读数
watch(() => route.path, (newPath) => {
  if (newPath.includes('/user/chat')) {
    // 进入聊天页面，清零未读数
    chatStore.clearMerchantUnread()
  }
})

onUnmounted(() => {
  disconnectStomp()
})

const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  switch (command) {
    case 'member-center':
      router.push('/user/member-center')
      break
    case 'profile':
      router.push('/user/profile')
      break
    case 'addresses':
      router.push('/user/addresses')
      break
    case 'chat':
      router.push('/user/chat')
      break
    case 'merchant':
      router.push('/merchant/dashboard')
      break
    case 'admin':
      router.push('/admin/dashboard')
      break
    case 'logout':
      userStore.logout()
      cartStore.clearCart()
      ElMessage.success(t('auth.logoutSuccess'))
      router.push('/login')
      break
  }
}
</script>

<style scoped lang="scss">
.user-layout {
  height: 100%;
  
  .el-container {
    height: 100%;
    flex-direction: column;
  }

  .header {
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 0;
    height: 60px;
    
    .header-content {
      max-width: 1200px;
      margin: 0 auto;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 20px;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: bold;
      color: #409eff;
      cursor: pointer;
    }

    .nav-menu {
      flex: 1;
      border: none;
      margin: 0 40px;
      
      // 调整购物车红点位置
      :deep(.el-badge__content) {
        top: 8px;
        right: 4px;
      }
    }

    .user-actions {
      display: flex;
      align-items: center;
      gap: 16px;

      .user-info {
        display: flex;
        align-items: center;
        gap: 4px;
        cursor: pointer;
        color: #606266;
        
        &:hover {
          color: #409eff;
        }
      }
    }
  }

  .main-content {
    flex: 1;
    overflow-y: auto;
    background: #f5f7fa;
    padding: 20px;
  }

  .footer {
    background: #fff;
    border-top: 1px solid #e4e7ed;
    height: 60px;
    
    .footer-content {
      max-width: 1200px;
      margin: 0 auto;
      text-align: center;
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>

