# 私域商城 SaaS 系统 (Vote SaaS)

一个功能完整的私域电商解决方案，支持多商户、商品管理、订单系统、支付集成、客服聊天等。

## 功能特性

### 核心电商功能
- **多商户支持** - 每个商家独立运营空间
- **商品管理** - SKU、商品分类、品牌管理
- **购物车** - 实时同步
- **订单系统** - 完整的订单流程（创建→支付→发货→收货→售后）
- **售后规则** - 可配置的售后政策
- **支付集成** - 支付配置系统
- **运费模板** - 灵活的运费计算

### 营销功能
- **促销活动** - 促销记录与提现
- **会员通知** - 邮件通知系统
- **风险控制** - 规则引擎

### 其他功能
- **聊天系统** - 商户与客户实时沟通
- **运营日志** - 完整的操作审计
- **发票管理** - 电子发票
- **应用下载** - APP下载配置

## 技术栈

| 组件 | 技术 |
|------|------|
| **前端** | Vue.js 3 + Vite |
| **后端** | Spring Boot 3.4.13 + Java 17 |
| **数据库** | MySQL 5.x |
| **Web服务器** | Nginx + WAF |
| **缓存** | Redis |
| **邮件** | QQ邮箱 SMTP |
| **容器** | Docker |

## 环境要求

- JDK 17+
- MySQL 5.7+
- Node.js 18+
- Maven 3.8+
- Nginx 1.20+
- 内存 4GB+
- 硬盘 30GB+

## 安装步骤

### 1. 服务器环境

```bash
# CentOS 8 / Ubuntu 20.04

# 安装 JDK 17
yum install -y java-17-openjdk java-17-openjdk-devel
java -version

# 安装 Node.js 18
curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
yum install -y nodejs
node -v

# 安装 Maven
yum install -y maven
mvn -version

# 安装 MySQL 5.7
yum install -y mysql mysql-server
systemctl start mysqld
mysql --version
```

### 2. 数据库配置

```bash
# 登录 MySQL
mysql -uroot -p

# 创建数据库
CREATE DATABASE vote_saas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE hjqi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据
USE vote_saas;
SOURCE /path/to/vote_saas.sql;

USE hjqi;
SOURCE /path/to/hjqi.sql;

# 创建用户（可选）
CREATE USER 'vote'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON vote_saas.* TO 'vote'@'%';
FLUSH PRIVILEGES;
```

### 3. 后端配置

```bash
# 创建项目目录
mkdir -p /www/wwwroot/vote
cd /www/wwwroot/vote

# 复制 JAR 文件
cp vote-0.0.1-SNAPSHOT.jar app.jar

# 创建配置目录
mkdir -p upload
```

编辑 `application-prod.yml`:

```yaml
spring:
  application:
    name: vote
  datasource:
    url: jdbc:mysql://localhost:3306/vote_saas?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: YOUR_PASSWORD
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 50MB
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com
    password: YOUR_EMAIL_AUTH_CODE
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

server:
  port: 8080
```

### 4. 启动后端

```bash
# 直接启动
java -jar -Xmx1024M -Xms256M app.jar --spring.profiles.active=prod &

# 或使用 systemd 服务
cat > /etc/systemd/system/vote.service << EOF
[Unit]
Description=Vote SaaS
After=network.target mysqld.service

[Service]
Type=simple
User=www
WorkingDirectory=/www/wwwroot/vote
ExecStart=/usr/bin/java -jar -Xmx1024M -Xms256M app.jar --spring.profiles.active=prod
Restart=always

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable vote
systemctl start vote
```

### 5. 前端部署

```bash
# 解压前端
cd /www/wwwroot/vote
tar -xzf vote_frontend.tar.gz

# Nginx 配置
cat > /etc/nginx/conf.d/vote.conf << 'EOF'
server {
    listen 80;
    server_name your-domain.com;
    root /www/wwwroot/vote/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /ws/ {
        proxy_pass http://127.0.0.1:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
EOF

nginx -t && systemctl reload nginx
```

### 6. SSL 配置（可选）

```bash
# 使用 Let's Encrypt
yum install -y certbot python3-certbot-nginx
certbot --nginx -d your-domain.com

# 或在宝塔面板操作更简单
```

## 数据库结构

### 核心表

| 表名 | 说明 |
|------|------|
| `t_activity` | 活动管理 |
| `t_admin` | 管理员 |
| `t_brand` | 品牌 |
| `t_cart_item` | 购物车 |
| `t_category` | 商品分类 |
| `t_order` | 订单主表 |
| `t_order_item` | 订单明细 |
| `t_product` | 商品 |
| `t_product_sku` | SKU |
| `t_user` | 会员 |
| `t_user_address` | 收货地址 |
| `t_merchant` | 商户 |
| `t_merchant_bill` | 商户账单 |
| `t_payment_order` | 支付订单 |
| `t_chat_message` | 聊天记录 |
| `t_after_sale_rule` | 售后规则 |
| `t_shipping_template` | 运费模板 |
| `t_promotion_record` | 促销记录 |

## API 文档

基础 URL: `http://your-domain.com/api/`

### 认证
- Header: `Authorization: Bearer {token}`

### 主要接口

```
POST   /api/auth/login          # 登录
GET    /api/product/list        # 商品列表
GET    /api/product/{id}       # 商品详情
POST   /api/order/create        # 创建订单
GET    /api/order/{id}          # 订单详情
POST   /api/pay/create          # 发起支付
WS     /ws/chat                 # 客服聊天
```

## 目录结构

```
/www/wwwroot/vote/
├── vote-0.0.1-SNAPSHOT.jar    # 后端 JAR
├── dist/                       # 前端构建产物
│   ├── index.html
│   └── assets/                 # 静态资源
├── upload/                     # 上传文件
└── application-prod.yml        # 生产配置

/www/wwwroot/www.hjqi.com/      # 另一个站点
├── src/                        # 源码
├── frontend/                   # 前端源码
├── target/                     # 构建产物
└── pom.xml                     # Maven 配置
```

## 常见问题

### 1. 启动失败
```bash
# 检查端口占用
netstat -tlnp | grep 8080

# 检查日志
tail -f /www/wwwroot/vote/app.log
```

### 2. 数据库连接失败
```bash
# 检查 MySQL
mysql -uroot -p -e "SELECT 1"

# 检查远程连接
mysql -uroot -p -e "GRANT ALL ON vote_saas.* TO 'root'@'%'; FLUSH PRIVILEGES;"
```

### 3. 邮件发送失败
- 检查 QQ 邮箱授权码是否正确
- 确保 SMTP 服务已开启

## 联系方式

- 官网: www.hjqi.com
- 支持: vote.hjqi.com

## License

Private - All Rights Reserved