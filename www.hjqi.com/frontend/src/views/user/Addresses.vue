<template>
  <div class="addresses-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('address.shippingAddress') }}</h3>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            {{ $t('address.addAddress') }}
          </el-button>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="8" v-for="address in addresses" :key="address.id">
          <el-card class="address-card" :class="{ default: address.isDefault }">
            <div class="address-info">
              <div class="name-phone">
                <strong>{{ address.receiverName }}</strong>
                <span>{{ address.receiverPhone }}</span>
              </div>
              <div class="address">{{ address.fullAddress }}</div>
              <el-tag v-if="address.isDefault" type="success" size="small">{{ $t('address.defaultAddress') }}</el-tag>
            </div>
            <div class="address-actions">
              <el-button type="primary" link @click="handleEdit(address)">{{ $t('common.edit') }}</el-button>
              <el-button type="danger" link @click="handleDelete(address.id)">{{ $t('common.delete') }}</el-button>
              <el-button
                v-if="!address.isDefault"
                type="success"
                link
                @click="handleSetDefault(address.id)"
              >
                {{ $t('address.setDefault') }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="addresses.length === 0" :description="$t('address.noAddress')" />
    </el-card>

    <!-- 地址编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingAddress ? $t('address.editAddress') : $t('address.addAddress')"
      width="500px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="$t('address.receiverName')" prop="receiverName">
          <el-input v-model="form.receiverName" :placeholder="$t('address.pleaseInputReceiverName')" />
        </el-form-item>
        <el-form-item :label="$t('address.receiverPhone')" prop="receiverPhone">
          <el-input v-model="form.receiverPhone" :placeholder="$t('address.pleaseInputReceiverPhone')" />
        </el-form-item>
        <el-form-item :label="$t('address.detailAddress')" prop="fullAddress">
          <el-input
            v-model="form.fullAddress"
            type="textarea"
            :rows="3"
            :placeholder="$t('address.pleaseInputDetailAddress')"
          />
        </el-form-item>
        <el-form-item :label="$t('address.setDefault')">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getAddresses, createAddress, updateAddress, deleteAddress } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

const addresses = ref([])
const dialogVisible = ref(false)
const editingAddress = ref(null)
const formRef = ref(null)

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  fullAddress: '',
  isDefault: false
})

const rules = computed(() => ({
  receiverName: [
    { required: true, message: t('address.pleaseInputReceiverName'), trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: t('address.pleaseInputReceiverPhone'), trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: t('address.phoneFormatError'), trigger: 'blur' }
  ],
  fullAddress: [
    { required: true, message: t('address.pleaseInputDetailAddress'), trigger: 'blur' }
  ]
}))

const loadAddresses = async () => {
  try {
    addresses.value = await getAddresses()
  } catch (error) {
    console.error('加载地址失败:', error)
  }
}

const handleAdd = () => {
  editingAddress.value = null
  Object.assign(form, {
    receiverName: '',
    receiverPhone: '',
    fullAddress: '',
    isDefault: false
  })
  dialogVisible.value = true
}

const handleEdit = (address) => {
  editingAddress.value = address
  Object.assign(form, {
    receiverName: address.receiverName,
    receiverPhone: address.receiverPhone,
    fullAddress: address.fullAddress,
    isDefault: address.isDefault
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (editingAddress.value) {
      await updateAddress(editingAddress.value.id, form)
      ElMessage.success(t('address.updateSuccess'))
    } else {
      await createAddress(form)
      ElMessage.success(t('address.addSuccess'))
    }
    
    dialogVisible.value = false
    loadAddresses()
  } catch (error) {
    if (error !== false) {
      console.error('保存地址失败:', error)
    }
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm(t('address.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteAddress(id)
    ElMessage.success(t('address.deleteSuccess'))
    loadAddresses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSetDefault = async (id) => {
  try {
    await updateAddress(id, { isDefault: true })
    ElMessage.success(t('address.setDefaultSuccess'))
    loadAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
  }
}

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped lang="scss">
.addresses-page {
  max-width: 1200px;
  margin: 0 auto;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h3 {
      margin: 0;
    }
  }

  .address-card {
    margin-bottom: 20px;

    &.default {
      border-color: #67c23a;
    }

    .address-info {
      margin-bottom: 10px;

      .name-phone {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
      }

      .address {
        color: #606266;
        margin-bottom: 10px;
        line-height: 1.6;
      }
    }

    .address-actions {
      display: flex;
      gap: 10px;
      border-top: 1px solid #f0f0f0;
      padding-top: 10px;
    }
  }
}
</style>

