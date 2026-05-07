import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  // ==================== 登录注册 ====================
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: { title: '找回密码' }
  },
  {
    path: '/merchant/register',
    name: 'MerchantRegister',
    component: () => import('@/views/auth/MerchantRegister.vue'),
    meta: { title: '商家入驻注册' }
  },
  {
    path: '/merchant/payment',
    name: 'MerchantPayment',
    component: () => import('@/views/merchant/MerchantPayment.vue'),
    meta: { title: '商家入驻缴费' }
  },
  {
    path: '/user/bind-merchant',
    name: 'BindMerchant',
    component: () => import('@/views/user/BindMerchant.vue'),
    meta: { title: '绑定商家', requiresAuth: true }
  },
  
  // ==================== 用户端 ====================
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      {
        path: 'products',
        name: 'UserProducts',
        component: () => import('@/views/user/Products.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/user/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/user/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/user/Checkout.vue'),
        meta: { title: '结算页', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'UserOrders',
        component: () => import('@/views/user/Orders.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'orders/payment',
        name: 'OrderPayment',
        component: () => import('@/views/user/OrderPayment.vue'),
        meta: { title: '订单支付说明', requiresAuth: true }
      },
      {
        path: 'orders/:id',
        name: 'OrderDetail',
        component: () => import('@/views/user/OrderDetail.vue'),
        meta: { title: '订单详情', requiresAuth: true }
      },
      {
        path: 'addresses',
        name: 'Addresses',
        component: () => import('@/views/user/Addresses.vue'),
        meta: { title: '收货地址', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人资料', requiresAuth: true }
      },
      {
        path: 'member-center',
        name: 'MemberCenter',
        component: () => import('@/views/user/MemberCenter.vue'),
        meta: { title: '会员中心', requiresAuth: true }
      },
      {
        path: 'chat',
        name: 'CustomerChat',
        component: () => import('@/views/user/CustomerChat.vue'),
        meta: { title: '在线客服', requiresAuth: true }
      }
    ]
  },

  // ==================== 商家端 ====================
  {
    path: '/merchant',
    component: () => import('@/layouts/MerchantLayout.vue'),
    meta: { requiresAuth: true, requiresMerchant: true },
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'payment',
        name: 'MerchantPayment',
        component: () => import('@/views/merchant/MerchantPayment.vue'),
        meta: { title: '入驻缴费' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: () => import('@/views/merchant/Categories.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/Products.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/Orders.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'bills',
        name: 'MerchantBills',
        component: () => import('@/views/merchant/Bills.vue'),
        meta: { title: '账单管理' }
      },
      {
        path: 'promotion',
        name: 'Promotion',
        component: () => import('@/views/merchant/MerchantPromotion.vue'),
        meta: { title: '推广中心' }
      },
      {
        path: 'config',
        name: 'MerchantConfig',
        component: () => import('@/views/merchant/Config.vue'),
        meta: { title: '店铺设置' }
      },
      {
        path: 'brands',
        name: 'Brands',
        component: () => import('@/views/merchant/Brands.vue'),
        meta: { title: '品牌管理' }
      },
      {
        path: 'invoices',
        name: 'Invoices',
        component: () => import('@/views/merchant/Invoices.vue'),
        meta: { title: '发票管理' }
      },
      {
        path: 'invoices/:id',
        name: 'InvoiceDetail',
        component: () => import('@/views/merchant/InvoiceDetail.vue'),
        meta: { title: '发票详情' }
      },
      {
        path: 'tickets',
        name: 'MerchantTickets',
        component: () => import('@/views/merchant/Tickets.vue'),
        meta: { title: '工单管理' }
      },
      {
        path: 'after-sale-rules',
        name: 'AfterSaleRules',
        component: () => import('@/views/merchant/AfterSaleRules.vue'),
        meta: { title: '售后规则' }
      },
      {
        path: 'emails',
        name: 'MerchantEmails',
        component: () => import('@/views/merchant/Emails.vue'),
        meta: { title: '邮箱配置' }
      },
      {
        path: 'notification-configs',
        name: 'NotificationConfigs',
        component: () => import('@/views/merchant/NotificationConfigs.vue'),
        meta: { title: '通知配置' }
      },
      {
        path: 'shipping-templates',
        name: 'ShippingTemplates',
        component: () => import('@/views/merchant/ShippingTemplates.vue'),
        meta: { title: '运费模板' }
      },
      {
        path: 'customer-service',
        name: 'CustomerService',
        component: () => import('@/views/merchant/CustomerService.vue'),
        meta: { title: '客服管理' }
      }
    ]
  },

  // ==================== 管理员端 ====================
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'merchants',
        name: 'Merchants',
        component: () => import('@/views/admin/Merchants.vue'),
        meta: { title: '商家管理' }
      },
      {
        path: 'merchant-settings',
        name: 'MerchantSettings',
        component: () => import('@/views/admin/MerchantSettings.vue'),
        meta: { title: '商家设置' }
      },
      {
        path: 'system-configs',
        name: 'SystemConfigs',
        component: () => import('@/views/admin/SystemConfigs.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: 'contents',
        name: 'Contents',
        component: () => import('@/views/admin/Contents.vue'),
        meta: { title: '内容管理' }
      },
      {
        path: 'bills',
        name: 'AdminBills',
        component: () => import('@/views/admin/Bills.vue'),
        meta: { title: '账单管理' }
      },
      {
        path: 'withdrawals',
        name: 'AdminWithdrawals',
        component: () => import('@/views/admin/Withdrawals.vue'),
        meta: { title: '提现管理' }
      },
      {
        path: 'logs',
        name: 'OperationLogs',
        component: () => import('@/views/admin/OperationLogs.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: 'platform-config',
        name: 'PlatformConfig',
        component: () => import('@/views/admin/PlatformConfig.vue'),
        meta: { title: '平台配置' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/admin/Activities.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'risk-control',
        name: 'RiskControl',
        component: () => import('@/views/admin/RiskControl.vue'),
        meta: { title: '风控管理' }
      },
      {
        path: 'app-download',
        name: 'AppDownload',
        component: () => import('@/views/admin/AppDownload.vue'),
        meta: { title: 'App下载' }
      },
      {
        path: 'tickets',
        name: 'AdminTickets',
        component: () => import('@/views/admin/Tickets.vue'),
        meta: { title: '工单管理' }
      },
      {
        path: 'merchant-customer-service',
        name: 'MerchantCustomerService',
        component: () => import('@/views/admin/MerchantCustomerService.vue'),
        meta: { title: '商家客服' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 私域商城`
  }

  // 需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next('/login')
    return
  }

  // 用户端需要绑定商家（绑定页面除外）
  if (userStore.isUser && userStore.isLoggedIn && to.path.startsWith('/user') && to.path !== '/user/bind-merchant') {
    if (!userStore.userInfo?.merchantId) {
      next('/user/bind-merchant')
      return
    }
  }

  // 需要商家权限
  if (to.meta.requiresMerchant && !userStore.isMerchant) {
    next('/login')
    return
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    next('/login')
    return
  }

  next()
})

export default router

