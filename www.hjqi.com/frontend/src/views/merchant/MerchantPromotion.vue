<template>
  <div class="merchant-promotion-container">
    <el-row :gutter="20">
      <!-- 邀请码和二维码展示 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="invite-card">
          <template #header>
            <div class="card-header">
              <el-icon><Share /></el-icon>
              <span>{{ $t('merchantPromotion.myInviteCode') }}</span>
            </div>
          </template>
          
          <div class="invite-content">
            <div class="invite-code-section">
              <h3>{{ $t('merchantPromotion.userInviteCode') }}</h3>
              <div class="code-display">
                <span class="code">{{ merchantInfo.inviteCode }}</span>
                <el-button
                  type="primary"
                  size="small"
                  @click="copyInviteCode"
                  :icon="CopyDocument"
                >
                  {{ $t('merchantPromotion.copy') }}
                </el-button>
              </div>
              <p class="code-tip">{{ $t('merchantPromotion.inviteCodeTip') }}</p>
            </div>

            <el-divider />

            <div class="promotion-code-section">
              <h3>{{ $t('merchantPromotion.merchantPromotionCode') }}</h3>
              <div class="code-display">
                <span class="code">{{ merchantInfo.promotionCode }}</span>
                <el-button
                  type="primary"
                  size="small"
                  @click="copyPromotionCode"
                  :icon="CopyDocument"
                >
                  {{ $t('merchantPromotion.copy') }}
                </el-button>
              </div>
              <p class="code-tip">{{ $t('merchantPromotion.promotionCodeTip') }}</p>
            </div>

            <el-divider />

            <div class="promotion-link-section">
              <h3>{{ $t('merchantPromotion.promotionLink') }}</h3>
              <el-input
                v-model="merchantInfo.promotionLink"
                readonly
                class="link-input"
              >
                <template #append>
                  <el-button @click="copyPromotionLink" :icon="CopyDocument">
                    {{ $t('merchantPromotion.copy') }}
                  </el-button>
                </template>
              </el-input>
              <p class="code-tip">{{ $t('merchantPromotion.promotionLinkTip') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 二维码展示 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="qrcode-card">
          <template #header>
            <div class="card-header">
              <el-icon><Picture /></el-icon>
              <span>{{ $t('merchantPromotion.inviteQrCode') }}</span>
            </div>
          </template>

          <div class="qrcode-content">
            <div v-if="merchantInfo.qrCodeUrl" class="qrcode-display">
              <img :src="merchantInfo.qrCodeUrl" alt="邀请二维码" class="qrcode-image" />
              <p class="qrcode-tip">{{ $t('merchantPromotion.qrCodeTip') }}</p>
              <div class="qrcode-actions">
                <el-button type="primary" @click="downloadQrCode">
                  <el-icon><Download /></el-icon>
                  {{ $t('merchantPromotion.downloadQrCode') }}
                </el-button>
                <el-button @click="regenerateQrCode" :loading="regenerating">
                  <el-icon><RefreshRight /></el-icon>
                  {{ $t('merchantPromotion.regenerateQrCode') }}
                </el-button>
              </div>
            </div>
            <div v-else class="no-qrcode">
              <el-empty :description="$t('merchantPromotion.noQrCode')">
                <el-button type="primary" @click="generateQrCode" :loading="generating">
                  {{ $t('merchantPromotion.generateQrCode') }}
                </el-button>
              </el-empty>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 推广统计 -->
      <el-col :xs="24" :sm="24" :md="24" :lg="8">
        <el-card class="statistics-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>{{ $t('merchantPromotion.promotionStatistics') }}</span>
            </div>
          </template>

          <div class="statistics-content">
            <el-row :gutter="10">
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ statistics?.promotedMerchantCount || 0 }}</div>
                  <div class="stat-label">{{ $t('merchantPromotion.promotedMerchantCount') }}</div>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <div class="stat-value">{{ statistics?.promotedUserCount || 0 }}</div>
                  <div class="stat-label">{{ $t('merchantPromotion.promotedUserCount') }}</div>
                </div>
              </el-col>
            </el-row>

            <el-divider />

            <div class="reward-section">
              <h3>{{ $t('merchantPromotion.promotionReward') }}</h3>
              <div class="reward-item">
                <span class="reward-label">{{ $t('merchantPromotion.totalReward') }}：</span>
                <span class="reward-value total">¥{{ formatAmount(merchantInfo.totalReward) }}</span>
              </div>
              <div class="reward-item">
                <span class="reward-label">{{ $t('merchantPromotion.availableReward') }}：</span>
                <span class="reward-value available">¥{{ formatAmount(merchantInfo.availableReward) }}</span>
              </div>
              <el-button
                type="success"
                class="withdraw-button"
                @click="showWithdrawDialog"
                :disabled="!canWithdraw"
              >
                <el-icon><Wallet /></el-icon>
                {{ $t('merchantPromotion.applyWithdraw') }}
              </el-button>
            </div>

            <el-divider />

            <div class="reward-rules">
              <h3>{{ $t('merchantPromotion.rewardRules') }}</h3>
              <ul>
                <li>{{ $t('merchantPromotion.merchantRewardRule') }}：<strong>¥{{ rewardRules.merchantReward }}</strong></li>
                <li>{{ $t('merchantPromotion.userRewardRule') }}：<strong>{{ rewardRules.userRewardRate }}%</strong> {{ $t('merchantPromotion.consumptionRate') }}</li>
              </ul>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 推广记录 -->
    <el-card class="promotion-records-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>{{ $t('merchantPromotion.promotionRecords') }}</span>
        </div>
      </template>

      <el-table :data="promotionRecords" stripe>
        <el-table-column prop="inviteeType" :label="$t('merchantPromotion.inviteeType')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.inviteeType === 'MERCHANT' ? 'primary' : 'success'" size="small">
              {{ row.inviteeType === 'MERCHANT' ? $t('merchantPromotion.typeMerchant') : $t('merchantPromotion.typeUser') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inviteeName" :label="$t('merchantPromotion.inviteeName')" />
        <el-table-column prop="rewardAmount" :label="$t('merchantPromotion.rewardAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.rewardAmount / 100) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('merchantPromotion.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('merchantPromotion.promotionTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="confirmedAt" :label="$t('merchantPromotion.confirmedTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.confirmedAt) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchPromotionRecords"
        @current-change="fetchPromotionRecords"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 提现记录 -->
    <el-card class="withdraw-records-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon>
          <span>{{ $t('merchantPromotion.withdrawalRecords') }}</span>
        </div>
      </template>

      <el-table :data="withdrawalRecords" stripe>
        <el-table-column prop="withdrawalNo" :label="$t('merchantPromotion.withdrawalNo')" min-width="180" />
        <el-table-column prop="amount" :label="$t('merchantPromotion.applyAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="fee" :label="$t('merchantPromotion.fee')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.fee) }}
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" :label="$t('merchantPromotion.actualAmount')" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.actualAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('merchantPromotion.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getWithdrawStatusType(row.status)" size="small">
              {{ getWithdrawStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('merchantPromotion.applyTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" :label="$t('merchantPromotion.updateTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 提现对话框 -->
    <el-dialog v-model="withdrawDialogVisible" :title="$t('merchantPromotion.applyWithdrawTitle')" width="500px">
      <el-form :model="withdrawForm" :rules="withdrawRules" ref="withdrawFormRef" label-width="100px">
        <el-form-item :label="$t('merchantPromotion.withdrawAmount')" prop="amount">
          <el-input-number
            v-model="withdrawForm.amount"
            :min="1"
            :max="parseFloat(merchantInfo.availableReward)"
            :precision="2"
            :step="10"
            style="width: 100%;"
          />
          <div class="form-tip">{{ $t('merchantPromotion.availableAmountTip') }}：¥{{ formatAmount(merchantInfo.availableReward) }}</div>
        </el-form-item>
        <el-form-item :label="$t('merchantPromotion.accountType')" prop="accountType">
          <el-select v-model="withdrawForm.accountType" :placeholder="$t('merchantPromotion.selectAccountType')" style="width: 100%;">
            <el-option :label="$t('merchantPromotion.accountTypeAlipay')" value="ALIPAY" />
            <el-option :label="$t('merchantPromotion.accountTypeWechat')" value="WECHAT" />
            <el-option :label="$t('merchantPromotion.accountTypeBank')" value="BANK" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('merchantPromotion.accountName')" prop="accountName">
          <el-input v-model="withdrawForm.accountName" :placeholder="$t('merchantPromotion.inputAccountName')" />
        </el-form-item>
        <el-form-item :label="$t('merchantPromotion.accountNumber')" prop="accountNumber">
          <el-input v-model="withdrawForm.accountNumber" :placeholder="$t('merchantPromotion.inputAccountNumber')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleWithdraw" :loading="withdrawing">
          {{ $t('merchantPromotion.confirmWithdraw') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  Share,
  Picture,
  DataAnalysis,
  List,
  CopyDocument,
  Download,
  RefreshRight,
  Wallet
} from '@element-plus/icons-vue'
import {
  getMerchantInfo,
  getPromotionStatistics,
  getPromotionRecords,
  getWithdrawalRecords,
  generateInviteQrCode,
  applyWithdrawal
} from '@/api/merchant'

const { t } = useI18n()

const merchantInfo = ref({
  inviteCode: '',
  promotionCode: '',
  promotionLink: '',
  qrCodeUrl: '',
  totalReward: 0,
  availableReward: 0
})

const statistics = ref({
  promotedMerchantCount: 0,
  promotedUserCount: 0
})

const rewardRules = ref({
  merchantReward: '100.00',
  userRewardRate: '5.00'
})

const promotionRecords = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 提现记录
const withdrawalRecords = ref([])

const generating = ref(false)
const regenerating = ref(false)
const withdrawing = ref(false)
const withdrawDialogVisible = ref(false)
const withdrawFormRef = ref(null)

const withdrawForm = reactive({
  amount: 0,
  accountType: '',
  accountName: '',
  accountNumber: ''
})

const withdrawRules = computed(() => ({
  amount: [
    { required: true, message: t('merchantPromotion.inputWithdrawAmount'), trigger: 'blur' }
  ],
  accountType: [
    { required: true, message: t('merchantPromotion.selectAccountType'), trigger: 'change' }
  ],
  accountName: [
    { required: true, message: t('merchantPromotion.inputAccountName'), trigger: 'blur' }
  ],
  accountNumber: [
    { required: true, message: t('merchantPromotion.inputAccountNumber'), trigger: 'blur' }
  ]
}))

const canWithdraw = computed(() => {
  return merchantInfo.value.availableReward > 0
})

// 复制邀请码
const copyInviteCode = () => {
  navigator.clipboard.writeText(merchantInfo.value.inviteCode)
  ElMessage.success(t('merchantPromotion.inviteCodeCopied'))
}

// 复制推广码
const copyPromotionCode = () => {
  navigator.clipboard.writeText(merchantInfo.value.promotionCode)
  ElMessage.success(t('merchantPromotion.promotionCodeCopied'))
}

// 复制推广链接
const copyPromotionLink = () => {
  navigator.clipboard.writeText(merchantInfo.value.promotionLink)
  ElMessage.success(t('merchantPromotion.promotionLinkCopied'))
}

// 生成二维码
const generateQrCode = async () => {
  generating.value = true
  try {
    const response = await generateInviteQrCode()
    // 响应拦截器已经返回了 response.data，所以直接使用 response.qrCodeUrl
    const qrCodeUrl = response.qrCodeUrl || response.data?.qrCodeUrl
    if (qrCodeUrl) {
      merchantInfo.value.qrCodeUrl = qrCodeUrl
      ElMessage.success(t('merchantPromotion.qrCodeGenerated'))
    } else {
      ElMessage.error(t('merchantPromotion.qrCodeGenerateFailed') + '：' + t('merchantPromotion.qrCodeNoUrl'))
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || t('merchantPromotion.qrCodeGenerateFailed'))
  } finally {
    generating.value = false
  }
}

// 重新生成二维码
const regenerateQrCode = async () => {
  regenerating.value = true
  try {
    const response = await generateInviteQrCode()
    // 响应拦截器已经返回了 response.data，所以直接使用 response.qrCodeUrl
    const qrCodeUrl = response.qrCodeUrl || response.data?.qrCodeUrl
    if (qrCodeUrl) {
      merchantInfo.value.qrCodeUrl = qrCodeUrl
      ElMessage.success(t('merchantPromotion.qrCodeRegenerated'))
    } else {
      ElMessage.error(t('merchantPromotion.qrCodeRegenerateFailed') + '：' + t('merchantPromotion.qrCodeNoUrl'))
    }
  } catch (error) {
    console.error('重新生成二维码失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || t('merchantPromotion.qrCodeRegenerateFailed'))
  } finally {
    regenerating.value = false
  }
}

// 下载二维码
const downloadQrCode = () => {
  const link = document.createElement('a')
  link.href = merchantInfo.value.qrCodeUrl
  link.download = `invite-qrcode-${merchantInfo.value.inviteCode}.png`
  link.click()
  ElMessage.success(t('merchantPromotion.qrCodeDownloaded'))
}

// 显示提现对话框
const showWithdrawDialog = () => {
  withdrawForm.amount = parseFloat(merchantInfo.value.availableReward)
  withdrawDialogVisible.value = true
}

// 申请提现
const handleWithdraw = async () => {
  await withdrawFormRef.value.validate(async (valid) => {
    if (!valid) return

    withdrawing.value = true
    try {
      await applyWithdrawal(withdrawForm)
      ElMessage.success(t('merchantPromotion.withdrawApplied'))
      withdrawDialogVisible.value = false
      fetchMerchantInfo()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || t('merchantPromotion.withdrawFailed'))
    } finally {
      withdrawing.value = false
    }
  })
}

// 获取商家信息
const fetchMerchantInfo = async () => {
  try {
    const response = await getMerchantInfo()
    // 处理响应数据，兼容不同的数据结构
    const data = response.data || response
    if (data) {
      merchantInfo.value = {
        inviteCode: data.inviteCode || '',
        promotionCode: data.promotionCode || '',
        promotionLink: data.promotionLink || '',
        qrCodeUrl: data.qrCodeUrl || '',
        totalReward: data.totalReward || 0,
        availableReward: data.availableReward || 0
      }
    }
  } catch (error) {
    console.error('获取商家信息失败:', error)
  }
}

// 获取推广统计
const fetchPromotionStatistics = async () => {
  try {
    const response = await getPromotionStatistics()
    // 处理响应数据，兼容不同的数据结构
    const data = response.data || response
    if (data) {
      statistics.value = {
        promotedMerchantCount: data.promotedMerchantCount || 0,
        promotedUserCount: data.promotedUserCount || 0
      }
    }
  } catch (error) {
    console.error('获取推广统计失败:', error)
    // 确保 statistics 有默认值
    statistics.value = {
      promotedMerchantCount: 0,
      promotedUserCount: 0
    }
  }
}

// 获取推广记录
const fetchPromotionRecords = async () => {
  try {
    const response = await getPromotionRecords({
      page: currentPage.value,
      size: pageSize.value
    })
    // 处理响应数据，兼容不同的数据结构
    const data = response.data || response
    if (data) {
      // MyBatis Plus 的 Page 对象有 records 和 total 字段
      promotionRecords.value = data.records || data.list || []
      total.value = data.total || 0
    } else {
      promotionRecords.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取推广记录失败:', error)
    promotionRecords.value = []
    total.value = 0
  }
}

// 获取提现记录
const fetchWithdrawalRecords = async () => {
  try {
    const response = await getWithdrawalRecords()
    const data = response.data || response
    withdrawalRecords.value = data || []
  } catch (error) {
    console.error('获取提现记录失败:', error)
    withdrawalRecords.value = []
  }
}

// 格式化金额
const formatAmount = (amount) => {
  return parseFloat(amount || 0).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: t('merchantPromotion.statusPending'),
    1: t('merchantPromotion.statusConfirmed'),
    2: t('merchantPromotion.statusCancelled')
  }
  return textMap[status] || t('merchantPromotion.statusUnknown')
}

// 提现状态类型
const getWithdrawStatusType = (status) => {
  const typeMap = {
    0: 'warning', // 待审核
    1: 'primary', // 审核通过
    2: 'success', // 已打款
    3: 'danger' // 已拒绝
  }
  return typeMap[status] || 'info'
}

// 提现状态文本
const getWithdrawStatusText = (status) => {
  const textMap = {
    0: t('merchantPromotion.withdrawStatusPending'),
    1: t('merchantPromotion.withdrawStatusApproved'),
    2: t('merchantPromotion.withdrawStatusPaid'),
    3: t('merchantPromotion.withdrawStatusRejected')
  }
  return textMap[status] || t('merchantPromotion.statusUnknown')
}

onMounted(() => {
  fetchMerchantInfo()
  fetchPromotionStatistics()
  fetchPromotionRecords()
  fetchWithdrawalRecords()
})
</script>

<style scoped lang="scss">
.merchant-promotion-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
}

.invite-card,
.qrcode-card,
.statistics-card {
  height: 100%;

  .invite-content,
  .qrcode-content,
  .statistics-content {
    h3 {
      margin: 0 0 15px 0;
      font-size: 14px;
      color: #606266;
    }

    .code-display {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;

      .code {
        flex: 1;
        padding: 10px 15px;
        background: #f5f7fa;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        font-size: 18px;
        font-weight: bold;
        color: #409eff;
        text-align: center;
      }
    }

    .code-tip {
      margin: 0;
      font-size: 12px;
      color: #909399;
    }

    .link-input {
      margin-bottom: 10px;
    }
  }
}

.qrcode-display {
  text-align: center;

  .qrcode-image {
    width: 200px;
    height: 200px;
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    margin-bottom: 15px;
  }

  .qrcode-tip {
    margin: 0 0 15px 0;
    font-size: 14px;
    color: #606266;
  }

  .qrcode-actions {
    display: flex;
    gap: 10px;
    justify-content: center;
  }
}

.statistics-content {
  .stat-item {
    text-align: center;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;

    .stat-value {
      font-size: 32px;
      font-weight: bold;
      color: #409eff;
      margin-bottom: 8px;
    }

    .stat-label {
      font-size: 14px;
      color: #909399;
    }
  }

  .reward-section {
    .reward-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 10px 0;

      .reward-label {
        font-size: 14px;
        color: #606266;
      }

      .reward-value {
        font-size: 18px;
        font-weight: bold;

        &.total {
          color: #409eff;
        }

        &.available {
          color: #67c23a;
        }
      }
    }

    .withdraw-button {
      width: 100%;
      margin-top: 15px;
    }
  }

  .reward-rules {
    ul {
      margin: 10px 0 0 0;
      padding-left: 20px;

      li {
        margin: 8px 0;
        font-size: 14px;
        color: #606266;

        strong {
          color: #f56c6c;
        }
      }
    }
  }
}

.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style>

