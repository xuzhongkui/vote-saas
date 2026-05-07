# 私域商城 SaaS 系统 - 前端项目

基于 Vue3 + Element Plus 开发的多角色商城管理系统。

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3 组件库
- **Vue Router** - 官方路由管理器
- **Pinia** - Vue 状态管理
- **Axios** - HTTP 客户端

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── admin.js      # 管理员接口
│   │   ├── auth.js       # 认证接口
│   │   ├── merchant.js   # 商家接口
│   │   └── user.js       # 用户接口
│   ├── layouts/          # 布局组件
│   │   ├── AdminLayout.vue    # 管理员布局
│   │   ├── MerchantLayout.vue # 商家布局
│   │   └── UserLayout.vue     # 用户布局
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── stores/           # 状态管理
│   │   ├── cart.js       # 购物车状态
│   │   └── user.js       # 用户状态
│   ├── styles/           # 全局样式
│   │   └── index.scss
│   ├── utils/            # 工具函数
│   │   ├── index.js      # 通用工具
│   │   └── request.js    # HTTP 请求封装
│   ├── views/            # 页面组件
│   │   ├── admin/        # 管理员页面
│   │   │   ├── Dashboard.vue      # 数据概览
│   │   │   ├── Merchants.vue      # 商家管理
│   │   │   ├── SystemConfigs.vue  # 系统配置
│   │   │   ├── Contents.vue       # 内容管理
│   │   │   ├── Bills.vue          # 账单管理
│   │   │   └── OperationLogs.vue  # 操作日志
│   │   ├── auth/         # 认证页面
│   │   │   ├── Login.vue          # 登录
│   │   │   └── Register.vue       # 注册
│   │   ├── merchant/     # 商家页面
│   │   │   ├── Dashboard.vue      # 数据概览
│   │   │   ├── Categories.vue     # 分类管理
│   │   │   ├── Products.vue       # 商品管理
│   │   │   ├── Orders.vue         # 订单管理
│   │   │   ├── Bills.vue          # 账单管理
│   │   │   ├── Promotion.vue      # 推广中心
│   │   │   └── Config.vue         # 店铺设置
│   │   └── user/         # 用户页面
│   │       ├── Products.vue       # 商品列表
│   │       ├── ProductDetail.vue  # 商品详情
│   │       ├── Cart.vue           # 购物车
│   │       ├── Orders.vue         # 我的订单
│   │       ├── OrderDetail.vue    # 订单详情
│   │       ├── Addresses.vue      # 收货地址
│   │       └── Profile.vue        # 个人资料
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML 模板
├── package.json          # 项目依赖
└── vite.config.js        # Vite 配置
```

## 功能模块

### 用户端
- ✅ 用户注册/登录
- ✅ 商品浏览（分类筛选）
- ✅ 商品详情（SKU 选择）
- ✅ 购物车管理
- ✅ 订单管理（创建、支付、查看）
- ✅ 收货地址管理
- ✅ 个人资料管理

### 商家端
- ✅ 商家注册/登录
- ✅ 数据概览（销售统计）
- ✅ 分类管理
- ✅ 商品管理（含 SKU）
- ✅ 订单管理（确认、发货）
- ✅ 账单管理
- ✅ 推广中心（推广链接、统计）
- ✅ 店铺设置

### 管理员端
- ✅ 管理员登录
- ✅ 数据概览（平台统计）
- ✅ 商家管理（审核、启用/禁用）
- ✅ 系统配置管理
- ✅ 内容管理（SEO、活动、新闻、公告）
- ✅ 账单管理（创建账单）
- ✅ 操作日志查看

## 安装依赖

```bash
cd frontend
npm install
```

## 开发运行

```bash
npm run dev
```

访问 http://localhost:3000

## 生产构建

```bash
npm run build
```

构建产物在 `dist` 目录。

## 预览构建

```bash
npm run preview
```

## 配置说明

### API 代理配置

在 `vite.config.js` 中配置了 API 代理：

```javascript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',  // 后端服务地址
      changeOrigin: true
    }
  }
}
```

### 路由守卫

在 `src/router/index.js` 中配置了路由守卫：
- 需要登录的页面会自动跳转到登录页
- 商家页面需要商家权限
- 管理员页面需要管理员权限

### 状态管理

使用 Pinia 管理全局状态：
- `useUserStore` - 用户信息、登录状态
- `useCartStore` - 购物车数据

## 默认账号

### 管理员
- 用户名: admin
- 密码: admin123

### 商家（需要先注册并等待审核）
- 注册后状态为"待审核"
- 管理员审核通过后可登录

### 用户
- 可直接注册使用

## 主要特性

1. **多角色系统** - 支持用户、商家、管理员三种角色
2. **响应式设计** - 适配不同屏幕尺寸
3. **权限控制** - 基于角色的路由守卫
4. **状态管理** - Pinia 统一管理应用状态
5. **API 封装** - 统一的请求拦截和错误处理
6. **组件化开发** - Element Plus 组件库
7. **国际化支持** - 中英文双语内容管理

## 注意事项

1. 确保后端服务已启动（默认端口 8080）
2. 首次使用需要创建管理员账号
3. 商家注册后需要管理员审核
4. 图片上传功能需要配置文件服务器
5. 支付功能为模拟支付，实际使用需对接支付网关

## 浏览器支持

- Chrome (推荐)
- Firefox
- Safari
- Edge

## 开发建议

1. 使用 Vue DevTools 进行调试
2. 遵循 Vue 3 Composition API 规范
3. 组件命名使用 PascalCase
4. 样式使用 scoped 避免污染
5. 合理使用 computed 和 watch

## 常见问题

### 1. 端口被占用
修改 `vite.config.js` 中的 `server.port`

### 2. API 请求失败
检查后端服务是否启动，代理配置是否正确

### 3. 登录��刷新页面退出
检查 localStorage 中的 token 是否存在

### 4. 图片不显示
确认图片 URL 是否正确，或使用占位图

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue 或 Pull Request。

