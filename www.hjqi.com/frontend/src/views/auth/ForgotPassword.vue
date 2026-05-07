<template>
  <div class="forgot-password-page">
    <el-card class="forgot-password-card">
      <template #header>
        <div class="card-header">
          <h2>找回密码</h2>
          <el-radio-group v-model="userType" size="small" style="margin-top: 16px">
            <el-radio-button label="USER">用户</el-radio-button>
            <el-radio-button label="MERCHANT">商家</el-radio-button>
            <el-radio-button label="ADMIN">管理员</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-steps :active="currentStep" finish-status="success" align-center>
        <el-step title="输入邮箱" />
        <el-step title="验证邮箱" />
        <el-step title="重置密码" />
      </el-steps>

      <!-- 步骤1：输入邮箱 -->
      <div v-if="currentStep === 0" class="step-content">
        <el-form
          ref="emailFormRef"
          :model="emailForm"
          :rules="emailRules"
          label-width="100px"
          style="max-width: 400px; margin: 40px auto"
        >
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="emailForm.email" placeholder="请输入注册邮箱" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSendCode" :loading="sending">
              发送验证码
            </el-button>
            <el-button @click="$router.push('/login')">返回登录</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2：验证邮箱 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-form
          ref="verifyFormRef"
          :model="verifyForm"
          :rules="verifyRules"
          label-width="100px"
          style="max-width: 400px; margin: 40px auto"
        >
          <el-form-item label="验证码" prop="code">
            <el-input v-model="verifyForm.code" placeholder="请输入邮箱验证码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleVerifyCode" :loading="verifying">
              验证
            </el-button>
            <el-button @click="currentStep = 0">上一步</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：重置密码 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-form
          ref="resetFormRef"
          :model="resetForm"
          :rules="resetRules"
          label-width="100px"
          style="max-width: 400px; margin: 40px auto"
        >
          <el-form-item label="新密码" prop="password">
            <el-input
              v-model="resetForm.password"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="resetForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleResetPassword" :loading="resetting">
              重置密码
            </el-button>
            <el-button @click="currentStep = 1">上一步</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { forgotPassword, verifyCode, resetPassword } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()

const currentStep = ref(0)
const sending = ref(false)
const verifying = ref(false)
const resetting = ref(false)

const emailFormRef = ref(null)
const verifyFormRef = ref(null)
const resetFormRef = ref(null)

// 从路由参数获取用户类型，默认为USER，如果没有则从登录类型映射
const getUserTypeFromRoute = () => {
  if (route.query.userType) {
    return route.query.userType.toUpperCase()
  }
  // 如果没有路由参数，默认返回USER
  return 'USER'
}

const userType = ref(getUserTypeFromRoute())

const emailForm = reactive({
  email: ''
})

const verifyForm = reactive({
  code: ''
})

const resetForm = reactive({
  password: '',
  confirmPassword: ''
})

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const verifyRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  password: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  try {
    await emailFormRef.value.validate()
    sending.value = true
    await forgotPassword({ 
      email: emailForm.email,
      userType: userType.value
    })
    ElMessage.success('验证码已发送到您的邮箱，请查收')
    currentStep.value = 1
  } catch (error) {
    if (error !== false) {
      console.error('发送验证码失败:', error)
      ElMessage.error(error.response?.data?.message || '发送验证码失败')
    }
  } finally {
    sending.value = false
  }
}

const handleVerifyCode = async () => {
  try {
    await verifyFormRef.value.validate()
    verifying.value = true
    await verifyCode({
      email: emailForm.email,
      code: verifyForm.code,
      userType: userType.value
    })
    ElMessage.success('验证码验证成功')
    currentStep.value = 2
  } catch (error) {
    if (error !== false) {
      console.error('验证失败:', error)
      ElMessage.error(error.response?.data?.message || '验证码错误或已过期')
    }
  } finally {
    verifying.value = false
  }
}

const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
    resetting.value = true
    await resetPassword({
      email: emailForm.email,
      code: verifyForm.code,
      newPassword: resetForm.password,
      userType: userType.value
    })
    ElMessage.success('密码重置成功，请使用新密码登录')
    router.push('/login')
  } catch (error) {
    if (error !== false) {
      console.error('重置密码失败:', error)
      ElMessage.error(error.response?.data?.message || '重置密码失败')
    }
  } finally {
    resetting.value = false
  }
}
</script>

<style scoped lang="scss">
.forgot-password-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

  .forgot-password-card {
    width: 600px;

    .card-header {
      text-align: center;

      h2 {
        margin: 0;
        color: #303133;
      }
    }

    .step-content {
      min-height: 200px;
      padding: 20px 0;
    }
  }
}
</style>

