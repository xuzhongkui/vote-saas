<template>
  <div class="merchant-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <el-icon><Shop /></el-icon>
          <span>{{ $t('menu.dashboard') }}</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/merchant/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>{{ $t('menu.dashboard') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/categories">
            <el-icon><Menu /></el-icon>
            <span>{{ $t('menu.categories') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/products">
            <el-icon><Goods /></el-icon>
            <span>{{ $t('menu.products') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/orders">
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.orders') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/payment">
            <el-icon><Money /></el-icon>
            <span>{{ $t('merchant.subscription') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/bills">
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.bills') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/invoices">
            <el-icon><DocumentCopy /></el-icon>
            <span>{{ $t('menu.invoices') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/promotion">
            <el-icon><Share /></el-icon>
            <span>{{ $t('menu.promotions') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/config">
            <el-icon><Setting /></el-icon>
            <span>{{ $t('merchant.storeSettings') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/brands">
            <el-icon><Trophy /></el-icon>
            <span>{{ $t('menu.brands') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/tickets">
            <el-icon><Message /></el-icon>
            <span>{{ $t('menu.tickets') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/after-sale-rules">
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.afterSaleRules') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/emails">
            <el-icon><Message /></el-icon>
            <span>{{ $t('menu.emailManagement') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/shipping-templates">
            <el-icon><Van /></el-icon>
            <span>{{ $t('menu.shippingTemplates') }}</span>
          </el-menu-item>
          <el-menu-item index="/merchant/customer-service">
            <el-icon><ChatLineRound /></el-icon>
            <span>{{ $t('menu.customerService') }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-content">
            <div class="breadcrumb">
              <el-breadcrumb separator="/">
                <el-breadcrumb-item :to="{ path: '/merchant/dashboard' }">
                  {{ $t('menu.dashboard') }}
                </el-breadcrumb-item>
                <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            
            <div class="user-actions">
              <!-- 语言切换器组件 -->
              <LanguageSwitcher user-type="merchant" size="small" />
              
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <el-icon><User /></el-icon>
                  {{ userStore.userInfo?.shopName || $t('merchant.merchantName') }}
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="logout">
                      {{ $t('auth.logout') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <!-- 主体内容 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { useLocaleStore } from '@/stores/locale'
import { ElMessage } from 'element-plus'
import { 
  Shop, 
  DataAnalysis, 
  Menu, 
  Goods, 
  Document, 
  DocumentCopy,
  Tickets, 
  Share, 
  Setting, 
  Trophy, 
  User,
  Money,
  ChatLineRound,
  Bell,
  Message,
  Van
} from '@element-plus/icons-vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const userStore = useUserStore()
const localeStore = useLocaleStore()

const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta.title || '')

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success(t('auth.logoutSuccess'))
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.merchant-layout {
  height: 100%;
  
  .el-container {
    height: 100%;
  }

  .sidebar {
    background: #001529;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    }

    .sidebar-menu {
      border: none;
      background: #001529;
      
      :deep(.el-menu-item) {
        color: rgba(255, 255, 255, 0.65);
        
        &:hover {
          color: #fff;
          background: rgba(255, 255, 255, 0.1);
        }
        
        &.is-active {
          color: #fff;
          background: #1890ff;
        }
      }
    }
  }

  .header {
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    padding: 0 20px;
    height: 60px;
    
    .header-content {
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
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
    background: #f5f7fa;
    overflow-y: auto;
  }
}
</style>

