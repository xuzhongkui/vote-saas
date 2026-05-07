<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <el-icon><Setting /></el-icon>
          <span>{{ $t('menu.dashboard') }}</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>{{ $t('menu.dashboard') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchants">
            <el-icon><Shop /></el-icon>
            <span>{{ $t('menu.merchants') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/system-configs">
            <el-icon><Tools /></el-icon>
            <span>{{ $t('menu.systemConfig') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/contents">
            <el-icon><Document /></el-icon>
            <span>{{ $t('menu.contents') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/bills">
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.bills') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/withdrawals">
            <el-icon><Tickets /></el-icon>
            <span>{{ $t('menu.withdrawals') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/logs">
            <el-icon><List /></el-icon>
            <span>{{ $t('menu.operationLogs') }}</span>
          </el-menu-item>
          <el-sub-menu index="platform">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>{{ $t('menu.platformConfig') }}</span>
            </template>
            <el-menu-item index="/admin/platform-config">
              <el-icon><Tools /></el-icon>
              <span>{{ $t('menu.platformConfig') }}</span>
            </el-menu-item>
            <el-menu-item index="/admin/activities">
              <el-icon><Star /></el-icon>
              <span>{{ $t('menu.activities') }}</span>
            </el-menu-item>
            <el-menu-item index="/admin/risk-control">
              <el-icon><Lock /></el-icon>
              <span>{{ $t('menu.riskControl') }}</span>
            </el-menu-item>
            <el-menu-item index="/admin/app-download">
              <el-icon><Download /></el-icon>
              <span>{{ $t('menu.appDownload') }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/tickets">
            <el-icon><Message /></el-icon>
            <span>{{ $t('menu.tickets') }}</span>
          </el-menu-item>
          <el-menu-item index="/admin/merchant-customer-service">
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
                <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">
                  {{ $t('menu.dashboard') }}
                </el-breadcrumb-item>
                <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            
            <div class="user-actions">
              <!-- 语言切换器组件 -->
              <LanguageSwitcher user-type="admin" size="small" />
              
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <el-icon><User /></el-icon>
                  {{ userStore.userInfo?.username || $t('auth.username') }}
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="logout">{{ $t('auth.logout') }}</el-dropdown-item>
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
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
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
.admin-layout {
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
      
      :deep(.el-sub-menu) {
        .el-sub-menu__title {
          color: rgba(255, 255, 255, 0.65);
          
          &:hover {
            color: #fff;
            background: rgba(255, 255, 255, 0.1);
          }
        }
        
        .el-menu {
          background: #000c17;
        }
        
        .el-menu-item {
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
