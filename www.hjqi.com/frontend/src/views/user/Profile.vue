<template>
  <div class="profile-page">
    <el-card>
      <template #header>
        <h3>{{ $t('profile.myProfile') }}</h3>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px"
      >
        <!-- 头像上传 -->
        <el-form-item :label="$t('profile.avatar')">
          <div class="avatar-uploader">
            <el-upload
              class="avatar-upload"
              :action="uploadUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
              name="file"
            >
              <img v-if="form.avatar" :src="form.avatar" class="avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
            <div class="avatar-tip">{{ $t('profile.avatarTip') }}</div>
          </div>
        </el-form-item>

        <el-form-item :label="$t('profile.username')">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item :label="$t('profile.realName')" prop="realName">
          <el-input v-model="form.realName" :placeholder="$t('profile.realNamePlaceholder')" />
        </el-form-item>

        <el-form-item :label="$t('profile.email')" prop="email">
          <el-input v-model="form.email" :placeholder="$t('profile.emailPlaceholder')" />
        </el-form-item>

        <el-form-item :label="$t('profile.phone')" prop="phone">
          <el-input v-model="form.phone" :placeholder="$t('profile.phonePlaceholder')" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="saving">{{ $t('common.save') }}</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getProfile, updateProfile } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const formRef = ref(null)
const saving = ref(false)

const form = reactive({
  username: '',
  email: '',
  phone: '',
  realName: '',
  avatar: ''
})

const rules = computed(() => ({
  email: [
    { type: 'email', message: t('profile.emailFormat'), trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: t('profile.phoneFormat'), trigger: 'blur' }
  ]
}))

// 上传相关
const uploadUrl = '/api/common/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

const handleAvatarSuccess = (response) => {
  if (response && response.url) {
    form.avatar = response.url
    ElMessage.success(t('profile.avatarUploadSuccess'))
  } else {
    ElMessage.error(t('profile.avatarUploadFailed'))
  }
}

const handleAvatarError = (error) => {
  console.error('头像上传失败:', error)
  ElMessage.error(t('profile.avatarUploadFailedRetry'))
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error(t('profile.avatarFormatError'))
    return false
  }
  if (!isLt2M) {
    ElMessage.error(t('profile.avatarSizeError'))
    return false
  }
  return true
}

const loadProfile = async () => {
  try {
    const profile = await getProfile()
    Object.assign(form, profile)
  } catch (error) {
    console.error('加载个人资料失败:', error)
    ElMessage.error(t('profile.loadFailed'))
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    saving.value = true
    await updateProfile({
      realName: form.realName,
      email: form.email,
      phone: form.phone,
      avatar: form.avatar
    })
    ElMessage.success(t('common.saveSuccess'))
    loadProfile()
  } catch (error) {
    if (error !== false) {
      console.error('保存失败:', error)
      ElMessage.error(error.response?.data?.message || t('common.saveFailed'))
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .avatar-upload {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 50%;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      width: 100px;
      height: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        border-color: #409eff;
      }
    }
  }
  
  .avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
  }
  
  .avatar-tip {
    color: #909399;
    font-size: 12px;
  }
}
</style>

