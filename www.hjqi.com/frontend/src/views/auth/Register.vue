<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>注册</h2>
          <el-radio-group v-model="registerType" size="small">
            <el-radio-button label="user">用户注册</el-radio-button>
            <el-radio-button label="merchant">商家注册</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item :label="registerType === 'merchant' ? '公司邮箱' : '邮箱'" prop="email">
          <el-input 
            v-model="form.email" 
            :placeholder="registerType === 'merchant' ? '请输入公司邮箱地址' : '请输入邮箱'" 
          />
        </el-form-item>

        <el-form-item v-if="registerType === 'user'" label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>

        <!-- 用户注册时必须绑定商家 -->
        <template v-if="registerType === 'user'">
          <el-form-item label="商家邀请码" prop="inviteCode">
            <el-input
              v-model="form.inviteCode"
              placeholder="请输入商家邀请码（必填）"
              clearable
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
            <div class="form-tip">
              <el-icon><InfoFilled /></el-icon>
              <span>请联系商家获取邀请码，或扫描商家提供的二维码</span>
            </div>
          </el-form-item>

          <el-form-item label="或扫码绑定">
            <div class="qr-section">
              <el-button @click="showQrScanner = true" :icon="Picture">
                扫描二维码
              </el-button>
              <div v-if="qrCodeImage" class="qr-preview">
                <img :src="qrCodeImage" alt="二维码" />
              </div>
            </div>
          </el-form-item>
        </template>

        <!-- 商家特有字段 -->
        <template v-if="registerType === 'merchant'">
          <el-form-item label="店铺名称（中文）" prop="shopNameZh">
            <el-input v-model="form.shopNameZh" placeholder="请输入店铺中文名称" />
          </el-form-item>

          <el-form-item label="店铺名称（英文）" prop="shopNameEn">
            <el-input v-model="form.shopNameEn" placeholder="请输入店铺英文名称（选填）" />
          </el-form-item>

          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>

          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
          </el-form-item>

          <el-form-item label="推广码" prop="referrerCode">
            <el-input
              v-model="form.referrerCode"
              placeholder="请输入推荐人的推广码（选填，填写后推荐人可获得奖励）"
              clearable
              @blur="handleReferrerCodeBlur"
            >
              <template #append>
                <el-button @click="verifyReferrerCode" :loading="verifying" size="small">
                  验证
                </el-button>
              </template>
            </el-input>
            <div v-if="referrerInfo" class="referrer-info" style="margin-top: 8px;">
              <el-tag type="success" size="small">
                <el-icon><Check /></el-icon>
                店铺名称：{{ referrerInfo.shopName }}
              </el-tag>
            </div>
            <div class="referrer-tips" style="margin-top: 8px; font-size: 12px; color: #909399;">
              <el-icon><InfoFilled /></el-icon>
              推广码获取方式：向推荐您的商家索要其推广码，或通过推广链接注册时自动填入
            </div>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister" style="width: 100%">
            注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="links">
            <el-button type="primary" link @click="$router.push('/login')">
              已有账号？立即登录
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 二维码扫描对话框 -->
    <el-dialog v-model="showQrScanner" title="扫描二维码" width="400px">
      <div class="scanner-placeholder">
        <el-icon :size="60" color="#909399"><Picture /></el-icon>
        <p>请使用手机扫描商家邀请码二维码</p>
        <p class="hint">扫描后会自动填入邀请码</p>
        <el-input
          v-model="scannedCode"
          placeholder="或手动输入扫描到的邀请码"
          style="margin-top: 20px"
          @keyup.enter="handleScannedCode"
        />
        <el-button type="primary" @click="handleScannedCode" style="margin-top: 10px; width: 100%">
          确认
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { userRegister, merchantRegister } from '@/api/auth'
import { verifyPromotionCode } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { Check, InfoFilled, Key, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const registerType = ref('user')
const loading = ref(false)
const verifying = ref(false)
const formRef = ref(null)
const referrerInfo = ref(null)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  inviteCode: '', // 用户注册时的商家邀请码
  shopNameZh: '',
  shopNameEn: '',
  contactName: '',
  contactPhone: '',
  referrerCode: ''
})

const showQrScanner = ref(false)
const scannedCode = ref('')
const qrCodeImage = ref(null)

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { 
      required: true, 
      message: '请输入手机号', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (registerType.value === 'merchant') {
          // 商家注册时手机号不是必填
          callback()
        } else {
          // 用户注册时手机号是必填
          if (!value) {
            callback(new Error('请输入手机号'))
          } else if (!/^1[3-9]\d{9}$/.test(value)) {
            callback(new Error('请输入正确的手机号'))
          } else {
            callback()
          }
        }
      }
    }
  ],
  shopNameZh: [
    { required: true, message: '请输入店铺中文名称', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ],
  inviteCode: [
    { 
      required: true, 
      message: '请输入商家邀请码', 
      trigger: 'blur',
      validator: (rule, value, callback) => {
        if (registerType.value === 'user') {
          if (!value || value.trim() === '') {
            callback(new Error('请输入商家邀请码'))
          } else if (value.trim().length < 4) {
            callback(new Error('邀请码长度不能少于4位'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      }
    }
  ]
}

// 验证推广码
const verifyReferrerCode = async () => {
  if (!form.referrerCode || form.referrerCode.trim() === '') {
    ElMessage.warning('请输入推广码')
    return
  }

  verifying.value = true
  try {
    const response = await verifyPromotionCode(form.referrerCode.trim())
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
  if (form.referrerCode && form.referrerCode.trim() !== '' && !referrerInfo.value) {
    verifyReferrerCode()
  }
}

// 切换注册类型时清空商家特有字段
watch(registerType, (newType) => {
  if (newType === 'user') {
    form.shopNameZh = ''
    form.shopNameEn = ''
    form.contactName = ''
    form.contactPhone = ''
    form.referrerCode = ''
    referrerInfo.value = null
  }
})

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    const data = {
      username: form.username,
      password: form.password,
      email: form.email,
      phone: form.phone
    }

    if (registerType.value === 'merchant') {
      // 商家注册时不传递手机号，使用联系电话代替
      delete data.phone
      data.shopNameZh = form.shopNameZh
      data.shopNameEn = form.shopNameEn
      data.contactName = form.contactName
      data.contactPhone = form.contactPhone
      if (form.referrerCode && form.referrerCode.trim() !== '') {
        data.referrerCode = form.referrerCode.trim()
      }
      await merchantRegister(data)
      ElMessage.success('商家注册成功，请等待平台审核')
    } else {
      // 用户注册时必须包含邀请码
      if (!form.inviteCode || form.inviteCode.trim() === '') {
        ElMessage.error('请输入商家邀请码')
        return
      }
      data.inviteCode = form.inviteCode.trim()
      await userRegister(data)
      ElMessage.success('注册成功，请登录')
    }

    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error(error.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const handleScannedCode = () => {
  if (scannedCode.value) {
    form.inviteCode = scannedCode.value
    showQrScanner.value = false
    scannedCode.value = ''
  } else {
    ElMessage.warning('请输入邀请码')
  }
}

// 页面加载时检查URL中的推广码
onMounted(() => {
  const referrer = route.query.referrer
  if (referrer && registerType.value === 'merchant') {
    form.referrerCode = referrer
    verifyReferrerCode()
  }
})
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  .register-card {
    width: 100%;
    max-width: 700px;

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

  .qr-section {
    .qr-preview {
      margin-top: 16px;
      text-align: center;

      img {
        max-width: 200px;
        max-height: 200px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
      }
    }
  }

  .scanner-placeholder {
    text-align: center;
    padding: 20px;

    p {
      margin: 16px 0 8px 0;
      color: #303133;
    }

    .hint {
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>

