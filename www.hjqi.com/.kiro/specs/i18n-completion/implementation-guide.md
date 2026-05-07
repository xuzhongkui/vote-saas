# i18n Implementation Guide

## 已完成的工作

### 1. 核心基础设施 ✅
- LanguageSwitcher 组件已创建并集成到所有布局
- 所有布局（User、Merchant、Admin）已更新
- 语言切换功能正常工作

### 2. 示例页面 ✅
以下页面已完全国际化，可作为参考：
- `frontend/src/views/auth/Login.vue`
- `frontend/src/views/user/Products.vue`
- `frontend/src/views/user/Cart.vue`
- `frontend/src/views/merchant/Dashboard.vue`（部分）
- `frontend/src/layouts/UserLayout.vue`
- `frontend/src/layouts/MerchantLayout.vue`
- `frontend/src/layouts/AdminLayout.vue`

## 国际化模式

### 模板中的文本替换

**硬编码文本 → i18n 调用**

```vue
<!-- 之前 -->
<h3>商品列表</h3>
<el-button>删除</el-button>
<el-table-column label="商品名称" />

<!-- 之后 -->
<h3>{{ $t('menu.products') }}</h3>
<el-button>{{ $t('common.delete') }}</el-button>
<el-table-column :label="$t('product.productName')" />
```

### Script 中的文本替换

```javascript
// 之前
import { ElMessage } from 'element-plus'
ElMessage.success('操作成功')
ElMessage.error('操作失败')

// 之后
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
ElMessage.success(t('common.success'))
ElMessage.error(t('common.failed'))
```

### 表单验证规则

```javascript
// 之前
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

// 之后
const rules = computed(() => ({
  username: [
    { required: true, message: t('auth.pleaseInputUsername'), trigger: 'blur' }
  ]
}))
```

### 确认对话框

```javascript
// 之前
await ElMessageBox.confirm('确定要删除吗？', '提示', {
  type: 'warning'
})

// 之后
await ElMessageBox.confirm(t('common.tip'), t('common.warning'), {
  type: 'warning'
})
```

## 剩余工作清单

### 用户端页面（需要更新）
- [ ] ProductDetail.vue
- [ ] Checkout.vue
- [ ] Addresses.vue
- [ ] Profile.vue
- [ ] MemberCenter.vue
- [ ] OrderDetail.vue
- [ ] OrderPayment.vue
- [ ] BindMerchant.vue
- [ ] CustomerChat.vue
- [ ] Orders.vue

### 商家端页面（需要更新）
- [ ] Products.vue
- [ ] Orders.vue
- [ ] Brands.vue
- [ ] Categories.vue
- [ ] Bills.vue
- [ ] Invoices.vue
- [ ] Tickets.vue
- [ ] Config.vue
- [ ] CustomerService.vue
- [ ] ShippingTemplates.vue
- [ ] AfterSaleRules.vue
- [ ] NotificationConfigs.vue
- [ ] Emails.vue
- [ ] MerchantPayment.vue
- [ ] MerchantPromotion.vue

### 管理员端页面（需要更新）
- [ ] Dashboard.vue
- [ ] Merchants.vue
- [ ] Bills.vue
- [ ] Tickets.vue
- [ ] Contents.vue
- [ ] Activities.vue
- [ ] SystemConfigs.vue
- [ ] PlatformConfig.vue
- [ ] OperationLogs.vue
- [ ] RiskControl.vue
- [ ] AppDownload.vue
- [ ] LanguageSettings.vue
- [ ] MerchantCustomerService.vue
- [ ] MerchantSettings.vue
- [ ] Withdrawals.vue

### 认证页面（需要更新）
- [ ] Register.vue
- [ ] ForgotPassword.vue
- [ ] MerchantRegister.vue

## 实施步骤

对于每个页面：

1. **导入 useI18n**
   ```javascript
   import { useI18n } from 'vue-i18n'
   const { t } = useI18n()
   ```

2. **替换模板中的硬编码文本**
   - 标题、标签 → `{{ $t('key') }}`
   - 属性绑定 → `:label="$t('key')"`
   - placeholder → `:placeholder="$t('key')"`

3. **替换 Script 中的文本**
   - ElMessage → `t('key')`
   - ElMessageBox → `t('key')`
   - 验证规则 → 使用 `computed()` 包装

4. **检查翻译键是否存在**
   - 查看 `frontend/src/i18n/zh-CN.js`
   - 查看 `frontend/src/i18n/en-US.js`
   - 如果缺失，添加新的翻译键

5. **测试**
   - 切换语言
   - 验证所有文本正确显示
   - 检查控制台是否有缺失翻译键的警告

## 常用翻译键参考

```javascript
// 通用
common.confirm, common.cancel, common.save, common.delete
common.edit, common.add, common.search, common.reset
common.success, common.failed, common.loading
common.noData, common.pleaseSelect, common.pleaseInput

// 菜单
menu.dashboard, menu.products, menu.orders, menu.cart

// 商品
product.productName, product.productPrice, product.stock

// 订单
order.orderNo, order.orderStatus, order.orderAmount

// 购物车
cart.myCart, cart.checkout, cart.quantity, cart.subtotal

// 认证
auth.login, auth.logout, auth.register, auth.username, auth.password

// 商家
merchant.merchantName, merchant.totalSales, merchant.todayOrders

// 验证
validation.required, validation.email, validation.phone

// 错误
error.networkError, error.serverError, error.unauthorized
```

## 注意事项

1. **保持一致性**：使用已有的翻译键，避免创建重复的键
2. **语义化命名**：翻译键应该清晰表达含义
3. **分组管理**：按功能模块组织翻译键
4. **测试覆盖**：每个页面更新后都要测试语言切换
5. **Element Plus 组件**：已通过 App.vue 中的 ElConfigProvider 自动切换语言

## 快速参考

### 查找页面文件
```bash
frontend/src/views/user/       # 用户端页面
frontend/src/views/merchant/   # 商家端页面
frontend/src/views/admin/      # 管理员端页面
frontend/src/views/auth/       # 认证页面
```

### 翻译文件位置
```bash
frontend/src/i18n/zh-CN.js     # 中文翻译
frontend/src/i18n/en-US.js     # 英文翻译
```

### 测试语言切换
1. 启动开发服务器
2. 登录系统
3. 点击右上角的语言切换器
4. 验证页面文本是否正确切换
