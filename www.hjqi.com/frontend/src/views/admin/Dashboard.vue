<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('adminDashboard.totalMerchants')" :value="statistics.totalMerchantCount || 0">
            <template #prefix>
              <el-icon><Shop /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('adminDashboard.totalUsers')" :value="statistics.totalUserCount || 0">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('adminDashboard.totalOrders')" :value="statistics.totalOrderCount || 0">
            <template #prefix>
              <el-icon><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <el-statistic :title="$t('adminDashboard.todayOrders')" :value="statistics.todayOrderCount || 0">
            <template #prefix>
              <el-icon><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>{{ $t('adminDashboard.pendingMerchants') }}</h4>
          </template>
          <el-table :data="pendingMerchants" max-height="400" stripe border>
            <el-table-column :label="$t('adminDashboard.shopName')" prop="shopNameZh" min-width="150" show-overflow-tooltip />
            <el-table-column :label="$t('adminDashboard.contactPerson')" prop="contactName" width="100" />
            <el-table-column :label="$t('adminDashboard.contactPhone')" prop="contactPhone" width="120" />
            <el-table-column :label="$t('adminDashboard.email')" prop="email" width="180" show-overflow-tooltip />
            <el-table-column :label="$t('adminDashboard.phone')" prop="phone" width="120" />
            <el-table-column :label="$t('adminDashboard.applyTime')" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column :label="$t('common.operation')" width="150" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="goToMerchantDetail(row.id)">
                  {{ $t('adminDashboard.viewDetail') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <h4>{{ $t('adminDashboard.platformStats') }}</h4>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item :label="$t('adminDashboard.pendingMerchants')">
              <el-tag type="warning">{{ statistics.pendingMerchantCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('adminDashboard.activeMerchants')">
              <el-tag type="success">{{ statistics.activeMerchantCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('adminDashboard.disabledMerchants')">
              <el-tag type="danger">{{ statistics.disabledMerchantCount || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('adminDashboard.todayOrderCount')">
              {{ statistics.todayOrderCount || 0 }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('adminDashboard.weekOrderCount')">
              {{ statistics.weekOrderCount || 0 }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('adminDashboard.monthOrderCount')">
              {{ statistics.monthOrderCount || 0 }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getStatistics, getMerchants } from '@/api/admin'
import { formatMoney, formatDateTime } from '@/utils'

const { t } = useI18n()
const router = useRouter()
const statistics = ref({})
const pendingMerchants = ref([])

const loadStatistics = async () => {
  try {
    // 获取平台整体的真实统计数据
    const stats = await getStatistics()
    statistics.value = stats.data || stats || {}
    console.log('平台统计数据:', statistics.value)
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 设置默认值
    statistics.value = {
      totalMerchantCount: 0,
      pendingMerchantCount: 0,
      activeMerchantCount: 0,
      disabledMerchantCount: 0,
      totalUserCount: 0,
      totalOrderCount: 0,
      todayOrderCount: 0,
      weekOrderCount: 0,
      monthOrderCount: 0
    }
  }
}

const loadPendingMerchants = async () => {
  try {
    // 获取待审核商家列表
    const result = await getMerchants({ page: 1, size: 10, status: 0 })
    // 处理不同的响应格式
    pendingMerchants.value = result.data?.records || result.records || []
    console.log('待审核商家:', pendingMerchants.value)
  } catch (error) {
    console.error('加载待审核商家失败:', error)
    pendingMerchants.value = []
  }
}

// 跳转到商家详情
const goToMerchantDetail = (merchantId) => {
  router.push({
    path: '/admin/merchants',
    query: { id: merchantId }
  })
}

onMounted(() => {
  loadStatistics()
  loadPendingMerchants()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  .stat-card {
    :deep(.el-statistic__head) {
      font-size: 14px;
      color: #909399;
    }

    :deep(.el-statistic__content) {
      font-size: 24px;
      font-weight: bold;
    }
  }
}
</style>
