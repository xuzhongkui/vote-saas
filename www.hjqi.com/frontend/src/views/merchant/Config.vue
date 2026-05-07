<template>
  <div class="config-page">
    <el-card>
      <template #header>
        <h3>{{ $t('merchantConfig.title') }}</h3>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane :label="$t('merchantConfig.storeInfo')" name="info">
          <el-form
            ref="infoFormRef"
            :model="infoForm"
            :rules="infoRules"
            label-width="140px"
            style="max-width: 600px"
          >
            <el-form-item :label="$t('merchantConfig.shopNameZh')" prop="shopNameZh">
              <el-input v-model="infoForm.shopNameZh" />
            </el-form-item>
            <el-form-item :label="$t('merchantConfig.shopNameEn')">
              <el-input v-model="infoForm.shopNameEn" />
            </el-form-item>
            
            <!-- Logo上传 -->
            <el-form-item :label="$t('merchantConfig.shopLogo')">
              <div class="image-uploader">
                <el-upload
                  class="image-upload"
                  :action="uploadUrl"
                  :show-file-list="false"
                  :on-success="(res) => handleLogoSuccess(res)"
                  :on-error="handleImageError"
                  :before-upload="beforeImageUpload"
                  :headers="uploadHeaders"
                  name="file"
                >
                  <img v-if="infoForm.logo" :src="infoForm.logo" class="upload-image" />
                  <el-icon v-else class="upload-icon"><Plus /></el-icon>
                </el-upload>
                <div class="image-tip">{{ $t('merchantConfig.logoTip') }}</div>
              </div>
            </el-form-item>
            
            <!-- Banner上传 -->
            <el-form-item :label="$t('merchantConfig.shopBanner')">
              <div class="image-uploader">
                <el-upload
                  class="banner-upload"
                  :action="uploadUrl"
                  :show-file-list="false"
                  :on-success="(res) => handleBannerSuccess(res)"
                  :on-error="handleImageError"
                  :before-upload="beforeImageUpload"
                  :headers="uploadHeaders"
                  name="file"
                >
                  <img v-if="infoForm.banner" :src="infoForm.banner" class="upload-banner" />
                  <el-icon v-else class="upload-icon"><Plus /></el-icon>
                </el-upload>
                <div class="image-tip">{{ $t('merchantConfig.bannerTip') }}</div>
              </div>
            </el-form-item>
            
            <el-form-item :label="$t('merchantConfig.contactName')" prop="contactName">
              <el-input v-model="infoForm.contactName" />
            </el-form-item>
            <el-form-item :label="$t('merchantConfig.contactPhone')" prop="contactPhone">
              <el-input v-model="infoForm.contactPhone" />
            </el-form-item>
            <el-form-item :label="$t('merchantConfig.email')">
              <el-input v-model="infoForm.email" disabled />
            </el-form-item>
            <el-form-item :label="$t('merchantConfig.phone')">
              <el-input v-model="infoForm.phone" disabled />
            </el-form-item>
            
            <!-- 邀请码和推广码（只读显示） -->
            <el-divider />
            <el-form-item :label="$t('merchantConfig.inviteCode')">
              <el-input v-model="infoForm.inviteCode" readonly>
                <template #append>
                  <el-button @click="copyInviteCode" :icon="CopyDocument">{{ $t('common.copy') }}</el-button>
                </template>
              </el-input>
              <div class="form-tip">{{ $t('merchantConfig.inviteCodeTip') }}</div>
            </el-form-item>
            <el-form-item :label="$t('merchantConfig.promotionCode')">
              <el-input v-model="infoForm.promotionCode" readonly>
                <template #append>
                  <el-button @click="copyPromotionCode" :icon="CopyDocument">{{ $t('common.copy') }}</el-button>
                </template>
              </el-input>
              <div class="form-tip">{{ $t('merchantConfig.promotionCodeTip') }}</div>
            </el-form-item>
            <el-form-item :label="$t('merchantConfig.promotionLink')">
              <el-input v-model="infoForm.promotionLink" readonly>
                <template #append>
                  <el-button @click="copyPromotionLink" :icon="CopyDocument">{{ $t('common.copy') }}</el-button>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSaveInfo">{{ $t('common.save') }}</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane :label="$t('merchantConfig.storeConfig')" name="config">
          <el-form
            ref="configFormRef"
            :model="configForm"
            label-width="140px"
            style="max-width: 600px"
          >
            <el-form-item :label="$t('merchantConfig.servicePhone')">
              <el-input v-model="configForm.servicePhone" />
            </el-form-item>
            <el-form-item :label="$t('merchantConfig.businessHours')">
              <el-input v-model="configForm.businessHours" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveConfig">{{ $t('common.save') }}</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getMerchantInfo, updateMerchantInfo } from '@/api/merchant'
import { ElMessage } from 'element-plus'
import { CopyDocument, Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

const activeTab = ref('info')
const infoFormRef = ref(null)
const configFormRef = ref(null)

const infoForm = reactive({
  shopNameZh: '',
  shopNameEn: '',
  logo: '',
  banner: '',
  contactName: '',
  contactPhone: '',
  email: '',
  phone: '',
  inviteCode: '',
  promotionCode: '',
  promotionLink: ''
})

const infoRules = computed(() => ({
  shopNameZh: [{ required: true, message: t('merchantConfig.inputShopName'), trigger: 'blur' }],
  contactName: [{ required: true, message: t('merchantConfig.inputContactName'), trigger: 'blur' }],
  contactPhone: [{ required: true, message: t('merchantConfig.inputContactPhone'), trigger: 'blur' }]
}))

const configForm = reactive({
  servicePhone: '',
  businessHours: ''
})

// 文件上传相关
const uploadUrl = '/api/common/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const handleLogoSuccess = (response) => {
  if (response && response.url) {
    infoForm.logo = response.url
    ElMessage.success(t('merchantConfig.logoUploadSuccess'))
  } else {
    ElMessage.error(t('merchantConfig.logoUploadFailed'))
  }
}

const handleBannerSuccess = (response) => {
  if (response && response.url) {
    infoForm.banner = response.url
    ElMessage.success(t('merchantConfig.bannerUploadSuccess'))
  } else {
    ElMessage.error(t('merchantConfig.bannerUploadFailed'))
  }
}

const handleImageError = (error) => {
  console.error('Image upload failed:', error)
  ElMessage.error(t('common.uploadFailed'))
}

const beforeImageUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error(t('common.imageOnly'))
    return false
  }
  if (!isLt2M) {
    ElMessage.error(t('common.imageSizeLimit'))
    return false
  }
  return true
}

const loadMerchantInfo = async () => {
  try {
    const response = await getMerchantInfo()
    const info = response.data || response
    Object.assign(infoForm, {
      shopNameZh: info.shopNameZh || '',
      shopNameEn: info.shopNameEn || '',
      logo: info.logo || '',
      banner: info.banner || '',
      contactName: info.contactName || '',
      contactPhone: info.contactPhone || '',
      email: info.email || '',
      phone: info.phone || '',
      inviteCode: info.inviteCode || '',
      promotionCode: info.promotionCode || '',
      promotionLink: info.promotionLink || ''
    })
    configForm.servicePhone = info.servicePhone || ''
    configForm.businessHours = info.businessHours || ''
  } catch (error) {
    console.error('Load merchant info failed:', error)
    ElMessage.error(t('merchantConfig.loadFailed'))
  }
}

const copyInviteCode = () => {
  if (infoForm.inviteCode) {
    navigator.clipboard.writeText(infoForm.inviteCode)
    ElMessage.success(t('merchantConfig.inviteCodeCopied'))
  }
}

const copyPromotionCode = () => {
  if (infoForm.promotionCode) {
    navigator.clipboard.writeText(infoForm.promotionCode)
    ElMessage.success(t('merchantConfig.promotionCodeCopied'))
  }
}

const copyPromotionLink = () => {
  if (infoForm.promotionLink) {
    navigator.clipboard.writeText(infoForm.promotionLink)
    ElMessage.success(t('merchantConfig.promotionLinkCopied'))
  }
}

const handleSaveInfo = async () => {
  try {
    await infoFormRef.value.validate()
    const updateData = {
      shopNameZh: infoForm.shopNameZh,
      shopNameEn: infoForm.shopNameEn,
      logo: infoForm.logo,
      banner: infoForm.banner,
      contactName: infoForm.contactName,
      contactPhone: infoForm.contactPhone
    }
    await updateMerchantInfo(updateData)
    ElMessage.success(t('common.saveSuccess'))
    loadMerchantInfo()
  } catch (error) {
    if (error !== false) {
      console.error('Save merchant info failed:', error)
      ElMessage.error(t('common.saveFailed'))
    }
  }
}

const handleSaveConfig = async () => {
  try {
    const updateData = {
      servicePhone: configForm.servicePhone || null,
      businessHours: configForm.businessHours || null
    }
    await updateMerchantInfo(updateData)
    ElMessage.success(t('common.saveSuccess'))
    setTimeout(() => {
      loadMerchantInfo()
    }, 500)
  } catch (error) {
    console.error('Save config failed:', error)
    ElMessage.error(t('common.saveFailed'))
  }
}

onMounted(() => {
  loadMerchantInfo()
})
</script>

<style scoped lang="scss">
.config-page {
  max-width: 1200px;
  
  .form-tip {
    margin-top: 5px;
    font-size: 12px;
    color: #909399;
  }
  
  .image-uploader {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .image-upload {
      :deep(.el-upload) {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        width: 200px;
        height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fafafa;
        
        &:hover {
          border-color: #409eff;
        }
      }
    }
    
    .banner-upload {
      :deep(.el-upload) {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        width: 600px;
        height: 150px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fafafa;
        
        &:hover {
          border-color: #409eff;
        }
      }
    }
    
    .upload-image {
      width: 200px;
      height: 200px;
      object-fit: cover;
      border-radius: 6px;
    }
    
    .upload-banner {
      width: 600px;
      height: 150px;
      object-fit: cover;
      border-radius: 6px;
    }
    
    .upload-icon {
      font-size: 28px;
      color: #8c939d;
    }
    
    .image-tip {
      color: #909399;
      font-size: 12px;
      margin-top: 4px;
    }
  }
}
</style>
