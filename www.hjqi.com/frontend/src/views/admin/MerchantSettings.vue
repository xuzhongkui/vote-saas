<template>
  <div class="merchant-settings-page">
    <el-card>
      <template #header>
        <h3>{{ $t('adminMerchantSettings.title') }}</h3>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 审核设置 -->
        <el-tab-pane :label="$t('adminMerchantSettings.auditSettings')" name="audit">
          <el-card>
            <el-form :model="auditForm" label-width="200px">
              <el-form-item :label="$t('adminMerchantSettings.auditSwitch')">
                <el-switch
                  v-model="auditForm.enabled"
                  :active-text="$t('adminMerchantSettings.auditSwitchOn')"
                  :inactive-text="$t('adminMerchantSettings.auditSwitchOff')"
                  @change="handleAuditSwitchChange"
                />
                <div class="form-tip">
                  {{ $t('adminMerchantSettings.auditSwitchTip') }}
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>

        <!-- 套餐管理 -->
        <el-tab-pane :label="$t('adminMerchantSettings.planManagement')" name="plans">
          <el-card>
            <template #header>
              <div class="card-header">
                <h4>{{ $t('adminMerchantSettings.servicePlan') }}</h4>
                <el-button type="primary" @click="handleAddPlan">
                  <el-icon><Plus /></el-icon>
                  {{ $t('adminMerchantSettings.addPlan') }}
                </el-button>
              </div>
            </template>

            <el-table :data="plans" v-loading="plansLoading" stripe border>
              <el-table-column label="ID" prop="id" width="80" />
              <el-table-column :label="$t('adminMerchantSettings.planNameZh')" min-width="150">
                <template #default="{ row }">
                  <div>{{ row.nameZh }}</div>
                  <div v-if="row.nameEn" class="text-secondary">{{ row.nameEn }}</div>
                </template>
              </el-table-column>
              <el-table-column :label="$t('adminMerchantSettings.price')" width="120">
                <template #default="{ row }">
                  ¥{{ formatAmount(row.price) }}
                </template>
              </el-table-column>
              <el-table-column :label="$t('adminMerchantSettings.durationDays')" width="100">
                <template #default="{ row }">
                  {{ row.durationDays }}{{ $t('adminMerchantSettings.days') }}
                </template>
              </el-table-column>
              <el-table-column :label="$t('adminMerchantSettings.maxProducts')" width="120">
                <template #default="{ row }">
                  {{ row.maxProducts || $t('adminMerchantSettings.unlimited') }}
                </template>
              </el-table-column>
              <el-table-column :label="$t('adminMerchantSettings.status')" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                    {{ row.status === 1 ? $t('adminMerchantSettings.enabled') : $t('adminMerchantSettings.disabled') }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column :label="$t('adminMerchantSettings.operation')" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditPlan(row)">{{ $t('adminMerchantSettings.edit') }}</el-button>
                  <el-button type="danger" link @click="handleDeletePlan(row.id)">{{ $t('adminMerchantSettings.delete') }}</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <!-- 入驻费用设置 -->
        <el-tab-pane :label="$t('adminMerchantSettings.registrationFee')" name="fee">
          <el-card>
            <el-form :model="feeForm" label-width="200px">
              <el-form-item :label="$t('adminMerchantSettings.feeEnabled')">
                <el-switch
                  v-model="feeForm.enabled"
                  :active-text="$t('adminMerchantSettings.feeEnabledOn')"
                  :inactive-text="$t('adminMerchantSettings.feeEnabledOff')"
                  @change="handleFeeEnabledChange"
                />
                <div class="form-tip">
                  {{ $t('adminMerchantSettings.feeEnabledTip') }}
                </div>
              </el-form-item>
              <el-form-item :label="$t('adminMerchantSettings.feeAmount')" v-if="feeForm.enabled">
                <el-input-number
                  v-model="feeForm.amount"
                  :min="0"
                  :precision="2"
                  :step="100"
                  @change="handleFeeAmountChange"
                />
                <div class="form-tip">
                  {{ $t('adminMerchantSettings.feeAmountTip') }}
                </div>
              </el-form-item>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 套餐编辑对话框 -->
    <el-dialog
      v-model="planDialogVisible"
      :title="editingPlan ? $t('adminMerchantSettings.editPlan') : $t('adminMerchantSettings.addPlan')"
      width="700px"
    >
      <el-form ref="planFormRef" :model="planForm" :rules="planRules" label-width="120px">
        <el-form-item :label="$t('adminMerchantSettings.planNameZh')" prop="nameZh">
          <el-input v-model="planForm.nameZh" />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.planNameEn')" prop="nameEn">
          <el-input v-model="planForm.nameEn" />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.descriptionZh')">
          <el-input v-model="planForm.descriptionZh" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.descriptionEn')">
          <el-input v-model="planForm.descriptionEn" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.price')" prop="price">
          <el-input-number v-model="planForm.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.durationDays')" prop="durationDays">
          <el-input-number v-model="planForm.durationDays" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.maxProducts')">
          <el-input-number
            v-model="planForm.maxProducts"
            :min="0"
            :placeholder="$t('adminMerchantSettings.unlimited')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.maxOrdersPerMonth')">
          <el-input-number
            v-model="planForm.maxOrdersPerMonth"
            :min="0"
            :placeholder="$t('adminMerchantSettings.unlimited')"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.status')" prop="status">
          <el-radio-group v-model="planForm.status">
            <el-radio :label="1">{{ $t('adminMerchantSettings.enabled') }}</el-radio>
            <el-radio :label="0">{{ $t('adminMerchantSettings.disabled') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('adminMerchantSettings.sort')">
          <el-input-number v-model="planForm.sort" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitPlan">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSystemConfigByKey,
  updateSystemConfig,
  getPlans,
  createPlan,
  updatePlan,
  deletePlan
} from '@/api/admin'

const { t } = useI18n()

const activeTab = ref('audit')

// 审核设置
const auditForm = reactive({
  enabled: true
})

// 入驻费用设置
const feeForm = reactive({
  enabled: true,
  amount: 1000
})

// 套餐管理
const plans = ref([])
const plansLoading = ref(false)
const planDialogVisible = ref(false)
const editingPlan = ref(null)
const planFormRef = ref(null)

const planForm = reactive({
  nameZh: '',
  nameEn: '',
  descriptionZh: '',
  descriptionEn: '',
  price: 0,
  durationDays: 30,
  maxProducts: null,
  maxOrdersPerMonth: null,
  status: 1,
  sort: 0
})

const planRules = {
  nameZh: [{ required: true, message: () => t('adminMerchantSettings.planNameZh'), trigger: 'blur' }],
  price: [{ required: true, message: () => t('adminMerchantSettings.price'), trigger: 'blur' }],
  durationDays: [{ required: true, message: () => t('adminMerchantSettings.durationDays'), trigger: 'blur' }]
}

// 加载审核设置
const loadAuditSettings = async () => {
  try {
    const config = await getSystemConfigByKey('merchant.audit.enabled')
    auditForm.enabled = config.configValue === 'true' || config.configValue === '1'
  } catch (error) {
    console.error('加载审核设置失败:', error)
  }
}

// 加载入驻费用设置
const loadFeeSettings = async () => {
  try {
    const enabledConfig = await getSystemConfigByKey('merchant.registration.fee.enabled')
    feeForm.enabled = enabledConfig.configValue === 'true' || enabledConfig.configValue === '1'

    const amountConfig = await getSystemConfigByKey('merchant.registration.fee')
    feeForm.amount = parseFloat(amountConfig.configValue || 1000)
  } catch (error) {
    console.error('加载费用设置失败:', error)
  }
}

// 加载套餐列表
const loadPlans = async () => {
  try {
    plansLoading.value = true
    const result = await getPlans()
    plans.value = result.data || result || []
  } catch (error) {
    console.error('加载套餐失败:', error)
    ElMessage.error(t('adminMerchantSettings.loadPlansFailed'))
  } finally {
    plansLoading.value = false
  }
}

// 审核开关变化
const handleAuditSwitchChange = async (value) => {
  try {
    const config = await getSystemConfigByKey('merchant.audit.enabled')
    await updateSystemConfig(config.id, { configValue: value ? 'true' : 'false' })
    ElMessage.success(t('adminMerchantSettings.settingsUpdated'))
  } catch (error) {
    console.error('更新审核设置失败:', error)
    ElMessage.error(t('adminMerchantSettings.updateFailed'))
    // 恢复原值
    await loadAuditSettings()
  }
}

// 费用开关变化
const handleFeeEnabledChange = async (value) => {
  try {
    const config = await getSystemConfigByKey('merchant.registration.fee.enabled')
    await updateSystemConfig(config.id, { configValue: value ? 'true' : 'false' })
    ElMessage.success(t('adminMerchantSettings.settingsUpdated'))
  } catch (error) {
    console.error('更新费用设置失败:', error)
    ElMessage.error(t('adminMerchantSettings.updateFailed'))
    await loadFeeSettings()
  }
}

// 费用金额变化
const handleFeeAmountChange = async (value) => {
  try {
    const config = await getSystemConfigByKey('merchant.registration.fee')
    await updateSystemConfig(config.id, { configValue: value.toString() })
    ElMessage.success(t('adminMerchantSettings.settingsUpdated'))
  } catch (error) {
    console.error('更新费用金额失败:', error)
    ElMessage.error(t('adminMerchantSettings.updateFailed'))
    await loadFeeSettings()
  }
}

// 新增套餐
const handleAddPlan = () => {
  editingPlan.value = null
  Object.assign(planForm, {
    nameZh: '',
    nameEn: '',
    descriptionZh: '',
    descriptionEn: '',
    price: 0,
    durationDays: 30,
    maxProducts: null,
    maxOrdersPerMonth: null,
    status: 1,
    sort: 0
  })
  planDialogVisible.value = true
}

// 编辑套餐
const handleEditPlan = (plan) => {
  editingPlan.value = plan
  Object.assign(planForm, {
    nameZh: plan.nameZh,
    nameEn: plan.nameEn || '',
    descriptionZh: plan.descriptionZh || '',
    descriptionEn: plan.descriptionEn || '',
    price: plan.price,
    durationDays: plan.durationDays,
    maxProducts: plan.maxProducts,
    maxOrdersPerMonth: plan.maxOrdersPerMonth,
    status: plan.status,
    sort: plan.sort || 0
  })
  planDialogVisible.value = true
}

// 删除套餐
const handleDeletePlan = async (id) => {
  try {
    await ElMessageBox.confirm(t('adminMerchantSettings.deleteConfirm'), t('common.tip'), { type: 'warning' })
    await deletePlan(id)
    ElMessage.success(t('adminMerchantSettings.deleteSuccess'))
    loadPlans()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除套餐失败:', error)
      ElMessage.error(t('adminMerchantSettings.deleteFailed'))
    }
  }
}

// 提交套餐
const handleSubmitPlan = async () => {
  try {
    await planFormRef.value.validate()
    if (editingPlan.value) {
      await updatePlan(editingPlan.value.id, planForm)
      ElMessage.success(t('adminMerchantSettings.updateSuccess'))
    } else {
      await createPlan(planForm)
      ElMessage.success(t('adminMerchantSettings.createSuccess'))
    }
    planDialogVisible.value = false
    loadPlans()
  } catch (error) {
    if (error !== false) {
      console.error('保存套餐失败:', error)
      ElMessage.error(t('adminMerchantSettings.saveFailed'))
    }
  }
}

// 格式化金额
const formatAmount = (amount) => {
  return parseFloat(amount || 0).toFixed(2)
}

onMounted(() => {
  loadAuditSettings()
  loadFeeSettings()
  loadPlans()
})
</script>

<style scoped lang="scss">
.merchant-settings-page {
  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #909399;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    h4 {
      margin: 0;
    }
  }

  .text-secondary {
    font-size: 12px;
    color: #909399;
  }
}
</style>

