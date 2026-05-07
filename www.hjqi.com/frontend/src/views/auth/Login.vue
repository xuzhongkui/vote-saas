<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>{{ $t('auth.login') }}</h2>
          <el-radio-group v-model="loginType" size="small">
            <el-radio-button label="user">{{ $t('auth.userLogin') }}</el-radio-button>
            <el-radio-button label="merchant">{{ $t('auth.merchantLogin') }}</el-radio-button>
            <el-radio-button label="admin">{{ $t('auth.adminLogin') }}</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :label-width="120"
      >
        <el-form-item :label="$t('auth.username')" prop="username">
          <el-input v-model="form.username" :placeholder="$t('auth.pleaseInputUsername')" />
        </el-form-item>

        <el-form-item :label="$t('auth.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="$t('auth.pleaseInputPassword')"
            show-password
          />
        </el-form-item>

        <!-- 用户首次登录时必须绑定商家 -->
        <el-form-item 
          v-if="loginType === 'user'" 
          :label="$t('merchant.merchantInfo')" 
          prop="inviteCode"
          :rules="loginType === 'user' ? [{ required: false, message: $t('validation.required'), trigger: 'blur' }] : []"
        >
          <el-input
            v-model="form.inviteCode"
            :placeholder="$t('common.pleaseInput')"
            clearable
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ $t('common.tip') }}</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
            {{ $t('auth.login') }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="links">
            <el-button type="primary" link @click="$router.push('/register')">
              {{ $t('auth.noAccount') }} {{ $t('auth.goRegister') }}
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="handleForgotPassword" 
              style="margin-left: 16px"
            >
              {{ $t('auth.forgotPassword') }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
import { userLogin, merchantLogin, adminLogin } from '@/api/auth'
import { ElMessage } from 'element-plus'
import { Key, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()
const userStore = useUserStore()

const loginType = ref('user')
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  inviteCode: '' // 用户首次登录时的商家邀请码
})

const rules = computed(() => ({
  username: [
    { required: true, message: t('auth.pleaseInputUsername'), trigger: 'blur' }
  ],
  password: [
    { required: true, message: t('auth.pleaseInputPassword'), trigger: 'blur' },
    { min: 6, message: t('validation.minLength', { min: 6 }), trigger: 'blur' }
  ]
}))

const handleForgotPassword = () => {
  // 根据当前登录类型映射到用户类型
  const userTypeMap = {
    'user': 'USER',
    'merchant': 'MERCHANT',
    'admin': 'ADMIN'
  }
  const userType = userTypeMap[loginType.value] || 'USER'
  router.push({
    path: '/forgot-password',
    query: { userType }
  })
}

const handleLogin = async () => {
  try {
    // 表单验证失败时，validate() 会抛出异常，但不应该显示网络错误
    await formRef.value.validate()
  } catch (error) {
    // 表单验证失败，不显示错误提示（Element Plus 会自动显示验证错误）
    return
  }
  
  try {
    loading.value = true

    let response
    let userType
    
    if (loginType.value === 'user') {
      // 用户登录时传递邀请码（如果有）
      const loginData = {
        username: form.username,
        password: form.password
      }
      if (form.inviteCode && form.inviteCode.trim()) {
        loginData.inviteCode = form.inviteCode.trim()
      }
      response = await userLogin(loginData)
      userType = 'USER'
    } else if (loginType.value === 'merchant') {
      response = await merchantLogin(form)
      userType = 'MERCHANT'
    } else {
      response = await adminLogin(form)
      userType = 'ADMIN'
    }

    console.log('🔐 登录响应数据:', response)
    console.log('🔐 Token:', response.token)
    console.log('🔐 UserInfo:', response.user || response.merchant || response.admin)
    console.log('🔐 UserType:', userType)

    const userInfo = response.user || response.merchant || response.admin
    console.log('🔐 准备保存的 userInfo:', userInfo)

    userStore.login({
      token: response.token,
      userInfo: userInfo,
      userType
    })

    console.log('🔐 保存后的 localStorage userInfo:', localStorage.getItem('userInfo'))

    ElMessage.success(t('auth.loginSuccess'))

    // 根据用户类型跳转
    if (userType === 'ADMIN') {
      router.push('/admin/dashboard')
    } else if (userType === 'MERCHANT') {
      router.push('/merchant/dashboard')
    } else {
      // 用户端：检查是否已绑定商家
      const userInfo = response.user || response.merchant || response.admin
      if (!userInfo?.merchantId) {
        router.push('/user/bind-merchant')
      } else {
        router.push('/user/products')
      }
    }
  } catch (error) {
    // 只有真正的网络请求错误才显示提示
    if (error.response) {
      const { status, data } = error.response
      
      // 获取错误消息 - 后端统一返回 { "message": "..." } 格式
      let message = null
      if (data) {
        if (typeof data === 'string') {
          message = data
        } else if (data.message) {
          message = data.message
        }
      }
      
      // 如果有后端返回的具体错误消息，直接使用
      if (message) {
        ElMessage.error(message)
      } else {
        // 如果没有具体消息，根据状态码显示默认提示
        if (status === 401) {
          ElMessage.error(t('auth.loginFailed'))
        } else if (status === 403) {
          ElMessage.error(t('error.forbidden'))
        } else if (status === 400) {
          ElMessage.error(t('validation.required'))
        } else if (status === 500) {
          ElMessage.error(t('error.serverError'))
        } else {
          ElMessage.error(t('auth.loginFailed'))
        }
      }
    } else if (error.message && error.message !== 'Network Error') {
      // 只有非网络错误才显示
      ElMessage.error(error.message)
    }
    // 网络错误不显示提示，避免干扰用户
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  .login-card {
    width: 100%;
    max-width: 600px;

    .card-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 16px;

      h2 {
        margin: 0;
        color: #303133;
      }
    }

    .links {
      width: 100%;
      text-align: center;
    }

    .form-tip {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 8px;
      font-size: 12px;
      color: #909399;

      .el-icon {
        font-size: 14px;
      }
    }
  }
}
</style>

