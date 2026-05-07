<template>
  <div class="bind-merchant-page">
    <el-card class="bind-card">
      <template #header>
        <div class="card-header">
          <h2>{{ $t('bindMerchant.title') }}</h2>
          <p class="subtitle">{{ $t('bindMerchant.subtitle') }}</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 500px; margin: 0 auto"
      >
        <el-form-item :label="$t('bindMerchant.inviteCode')" prop="inviteCode">
          <el-input
            v-model="form.inviteCode"
            :placeholder="$t('bindMerchant.inviteCodePlaceholder')"
            clearable
            @keyup.enter="handleBind"
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ $t('bindMerchant.inviteCodeTip') }}</span>
          </div>
        </el-form-item>

        <el-form-item :label="$t('bindMerchant.orScanQr')">
          <div class="qr-section">
            <el-button @click="showQrScanner = true" :icon="Picture">
              {{ $t('bindMerchant.scanQrCode') }}
            </el-button>
            <div v-if="qrCodeImage" class="qr-preview">
              <img :src="qrCodeImage" alt="QR Code" />
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleBind" :loading="binding" style="width: 100%">
            {{ $t('bindMerchant.bindButton') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 二维码扫描对话框 -->
    <el-dialog v-model="showQrScanner" :title="$t('bindMerchant.scanQrTitle')" width="400px">
      <div class="scanner-placeholder">
        <el-icon :size="60" color="#909399"><Picture /></el-icon>
        <p>{{ $t('bindMerchant.scanQrHint') }}</p>
        <p class="hint">{{ $t('bindMerchant.scanQrSubHint') }}</p>
        <el-input
          v-model="scannedCode"
          :placeholder="$t('bindMerchant.manualInput')"
          style="margin-top: 20px"
          @keyup.enter="handleScannedCode"
        />
        <el-button type="primary" @click="handleScannedCode" style="margin-top: 10px; width: 100%">
          {{ $t('bindMerchant.confirmButton') }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useI18n } from 'vue-i18n'
import { bindMerchant, getProfile } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Key, InfoFilled, Picture } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const formRef = ref(null)
const binding = ref(false)
const showQrScanner = ref(false)
const scannedCode = ref('')
const qrCodeImage = ref(null)

const form = reactive({
  inviteCode: ''
})

const rules = computed(() => ({
  inviteCode: [
    { required: true, message: t('bindMerchant.pleaseInputInviteCode'), trigger: 'blur' },
    { min: 4, message: t('bindMerchant.inviteCodeMinLength'), trigger: 'blur' }
  ]
}))

// 页面加载时检查用户是否已绑定商家
onMounted(async () => {
  if (userStore.userInfo?.merchantId) {
    ElMessage.info(t('bindMerchant.alreadyBound'))
    router.replace('/user/products')
    return
  }
  
  try {
    const profile = await getProfile()
    if (profile && profile.merchantId) {
      userStore.setUserInfo({ ...userStore.userInfo, ...profile })
      ElMessage.info(t('bindMerchant.alreadyBound'))
      router.replace('/user/products')
    }
  } catch (error) {
    console.error('Get profile failed:', error)
  }
})

const handleBind = async () => {
  try {
    await formRef.value.validate()
    binding.value = true
    
    const result = await bindMerchant({ inviteCode: form.inviteCode })
    
    if (result) {
      if (result.token) {
        userStore.setToken(result.token)
      }
      if (result.userInfo) {
        userStore.setUserInfo({ ...userStore.userInfo, ...result.userInfo })
      }
    }
    
    ElMessage.success(t('bindMerchant.bindSuccess'))
    router.replace('/user/products')
  } catch (error) {
    if (error !== false) {
      console.error('Bind merchant failed:', error)
      if (error.response?.status === 400 && error.response?.data?.message?.includes('已绑定商家')) {
        ElMessage.info(t('bindMerchant.alreadyBound'))
        try {
          const profile = await getProfile()
          if (profile) {
            userStore.setUserInfo({ ...userStore.userInfo, ...profile })
          }
        } catch (e) {
          console.error('Get profile failed:', e)
        }
        router.replace('/user/products')
        return
      }
      ElMessage.error(error.response?.data?.message || t('bindMerchant.bindFailed'))
    }
  } finally {
    binding.value = false
  }
}

const handleScannedCode = () => {
  if (scannedCode.value) {
    form.inviteCode = scannedCode.value
    showQrScanner.value = false
    scannedCode.value = ''
    handleBind()
  } else {
    ElMessage.warning(t('bindMerchant.pleaseInputCode'))
  }
}
</script>

<style scoped lang="scss">
.bind-merchant-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;

  .bind-card {
    width: 100%;
    max-width: 600px;

    .card-header {
      text-align: center;

      h2 {
        margin: 0 0 10px 0;
        color: #303133;
      }

      .subtitle {
        margin: 0;
        color: #909399;
        font-size: 14px;
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
