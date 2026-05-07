<template>
  <div class="merchant-register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>商家入驻注册</h2>
          <p class="subtitle">加入我们的平台，开启您的商业之旅</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="140px"
        class="register-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（用于登录）"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="店铺名称（中文）" prop="shopNameZh">
          <el-input
            v-model="registerForm.shopNameZh"
            placeholder="请输入店铺中文名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="店铺名称（英文）" prop="shopNameEn">
          <el-input
            v-model="registerForm.shopNameEn"
            placeholder="请输入店铺英文名称（选填）"
            clearable
          />
        </el-form-item>

        <el-form-item label="公司邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入公司邮箱地址"
            clearable
          />
        </el-form-item>

        <el-form-item label="联系人姓名" prop="contactName">
          <el-input
            v-model="registerForm.contactName"
            placeholder="请输入联系人姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="registerForm.contactPhone"
            placeholder="请输入联系电话"
            clearable
          />
        </el-form-item>

        <el-form-item label="推广码" prop="referrerCode">
          <el-input
            v-model="registerForm.referrerCode"
            placeholder="请输入推荐人的推广码（选填，填写后推荐人可获得奖励）"
            clearable
            @blur="handleReferrerCodeBlur"
          >
            <template #append>
              <el-button @click="verifyReferrerCode" :loading="verifying">
                验证
              </el-button>
            </template>
          </el-input>
          <div class="referrer-tips">
            <p class="tip-text">
              <el-icon><InfoFilled /></el-icon>
              推广码获取方式：向推荐您的商家索要其推广码，或通过推广链接注册时自动填入
            </p>
            <div v-if="referrerInfo" class="referrer-info">
              <el-tag type="success" size="small">
                <el-icon><Check /></el-icon>
                店铺名称：{{ referrerInfo.shopName }}
              </el-tag>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="agreeTerms">
            我已阅读并同意
            <el-link type="primary" @click="showTerms">《商家入驻协议》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleRegister"
            :loading="loading"
            class="register-button"
          >
            立即注册
          </el-button>
          <el-button @click="goToLogin">已有账号？去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 注册成功对话框 -->
    <el-dialog
      v-model="successDialogVisible"
      title="注册成功"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="success-content">
        <el-result icon="success" :title="successMessage">
          <template #sub-title>
            <div class="success-info">
              <p v-if="registerResult.needAudit">
                <el-icon><Clock /></el-icon>
                您的申请正在审核中，请耐心等待
              </p>
              <p v-if="registerResult.needPayment">
                <el-icon><Money /></el-icon>
                审核通过后需缴纳入驻费用：¥{{ registerResult.paymentAmount }}
              </p>
              <p v-if="!registerResult.needAudit && !registerResult.needPayment">
                <el-icon><Check /></el-icon>
                您的账号已激活，可以立即登录使用
              </p>
              
              <!-- 显示邀请码和二维码（如果注册成功） -->
              <div v-if="registerResult.inviteCode" class="invite-info">
                <el-divider />
                <h3>您的店铺信息</h3>
                <div class="invite-code-display">
                  <p><strong>店铺邀请码：</strong>{{ registerResult.inviteCode }}</p>
                  <p class="code-tip">用户注册时填写此邀请码，即可绑定到您的店铺</p>
                </div>
                <div v-if="registerResult.promotionCode" class="promotion-code-display">
                  <p><strong>推广码：</strong>{{ registerResult.promotionCode }}</p>
                  <p class="code-tip">其他商家使用此推广码注册，您将获得推广奖励</p>
                </div>
                <div v-if="registerResult.qrCodeUrl" class="qrcode-display">
                  <p><strong>邀请二维码：</strong></p>
                  <img :src="registerResult.qrCodeUrl" alt="邀请二维码" class="qrcode-image" />
                </div>
              </div>
            </div>
          </template>
          <template #extra>
            <el-button
              v-if="registerResult.needPayment && !registerResult.needAudit"
              type="primary"
              @click="goToPayment"
            >
              立即缴费
            </el-button>
            <el-button @click="goToLogin">前往登录</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Money, Check, InfoFilled } from '@element-plus/icons-vue'
import { merchantRegister } from '@/api/auth'
import { verifyPromotionCode } from '@/api/merchant'

const router = useRouter()
const route = useRoute()

const registerFormRef = ref(null)
const loading = ref(false)
const verifying = ref(false)
const agreeTerms = ref(false)
const successDialogVisible = ref(false)
const referrerInfo = ref(null)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  shopNameZh: '',
  shopNameEn: '',
  email: '',
  contactName: '',
  contactPhone: '',
  referrerCode: ''
})

const registerResult = ref({
  merchantId: null,
  needAudit: false,
  needPayment: false,
  paymentAmount: 0,
  message: '',
  inviteCode: '',
  promotionCode: '',
  qrCodeUrl: ''
})

const successMessage = ref('')

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  shopNameZh: [
    { required: true, message: '请输入店铺中文名称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入公司邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ]
}

// 验证推广码
const verifyReferrerCode = async () => {
  if (!registerForm.referrerCode || registerForm.referrerCode.trim() === '') {
    ElMessage.warning('请输入推广码')
    return
  }

  verifying.value = true
  try {
    const response = await verifyPromotionCode(registerForm.referrerCode.trim())
    // 响应拦截器已经返回了 response.data，所以直接使用 response
    const data = response.valid !== undefined ? response : (response.data || {})
    
    if (data.valid) {
      referrerInfo.value = {
        shopName: data.shopName || '店铺名称未设置'
      }
      ElMessage.success('推广码验证成功')
    } else {
      ElMessage.error(data.message || '推广码无效')
      referrerInfo.value = null
    }
  } catch (error) {
    console.error('验证推广码失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '验证失败')
    referrerInfo.value = null
  } finally {
    verifying.value = false
  }
}

// 推广码输入框失焦时自动验证（如果已输入）
const handleReferrerCodeBlur = () => {
  if (registerForm.referrerCode && registerForm.referrerCode.trim() !== '' && !referrerInfo.value) {
    verifyReferrerCode()
  }
}

// 注册
const handleRegister = async () => {
  if (!agreeTerms.value) {
    ElMessage.warning('请先阅读并同意商家入驻协议')
    return
  }

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const response = await merchantRegister(registerForm)
      registerResult.value = response.data
      successMessage.value = response.data.message || '注册成功'
      successDialogVisible.value = true
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}

// 前往缴费
const goToPayment = () => {
  successDialogVisible.value = false
  router.push({
    path: '/merchant/payment',
    query: { merchantId: registerResult.value.merchantId }
  })
}

// 前往登录
const goToLogin = () => {
  router.push('/merchant/login')
}

// 显示协议
const showTerms = () => {
  ElMessage.info('商家入驻协议')
  // TODO: 显示协议内容
}

// 页面加载时检查URL中的推广码
onMounted(() => {
  const referrer = route.query.referrer
  if (referrer) {
    registerForm.referrerCode = referrer
    verifyReferrerCode()
  }
})
</script>

<style scoped lang="scss">
.merchant-register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 700px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border-radius: 12px;

  .card-header {
    text-align: center;

    h2 {
      margin: 0 0 10px 0;
      color: #303133;
      font-size: 28px;
    }

    .subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }
}

.register-form {
  margin-top: 20px;

  .register-button {
    width: 100%;
  }

  .referrer-tips {
    margin-top: 8px;
    
    .tip-text {
      display: flex;
      align-items: center;
      gap: 6px;
      margin: 0 0 8px 0;
      font-size: 12px;
      color: #909399;
      
      .el-icon {
        color: #409eff;
      }
    }
    
    .referrer-info {
      margin-top: 8px;
      
      .el-tag {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}

.success-content {
  .success-info {
    text-align: left;
    margin: 20px 0;

    p {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 10px 0;
      font-size: 14px;
      color: #606266;
    }
  }

  .invite-info {
    margin-top: 20px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;

    h3 {
      margin: 0 0 15px 0;
      font-size: 16px;
      color: #303133;
    }

    .invite-code-display,
    .promotion-code-display {
      margin: 15px 0;

      p {
        margin: 8px 0;
        font-size: 14px;
        color: #606266;

        strong {
          color: #409eff;
        }
      }

      .code-tip {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
      }
    }

    .qrcode-display {
      text-align: center;
      margin-top: 20px;

      p {
        margin-bottom: 10px;
        font-size: 14px;
        color: #606266;
      }

      .qrcode-image {
        width: 200px;
        height: 200px;
        border: 1px solid #dcdfe6;
        border-radius: 8px;
      }
    }
  }
}
</style>

