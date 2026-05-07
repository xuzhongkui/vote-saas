<template>
  <div class="promotion-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>推广链接</h3>
          </template>
          <div class="promotion-link">
            <el-input v-model="promotionLink" readonly>
              <template #append>
                <el-button @click="copyLink">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </template>
            </el-input>
          </div>
          <el-alert
            title="分享您的推广链接，邀请新用户注册，即可获得推广奖励"
            type="info"
            :closable="false"
            style="margin-top: 20px"
          />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <h3>推广统计</h3>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="邀请用户数">
              {{ statistics.invitedUsers || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="邀请商家数">
              {{ statistics.invitedMerchants || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="累计奖励">
              ¥{{ formatMoney(statistics.totalReward || 0) }}
            </el-descriptions-item>
            <el-descriptions-item label="待确认奖励">
              ¥{{ formatMoney(statistics.pendingReward || 0) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <h3>推广记录</h3>
      </template>
      <el-table :data="statistics.records || []">
        <el-table-column label="被邀请人" prop="inviteeName" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            {{ row.inviteeType === 'MERCHANT' ? '商家' : '用户' }}
          </template>
        </el-table-column>
        <el-table-column label="奖励金额" width="120">
          <template #default="{ row }">
            ¥{{ formatMoney(row.rewardAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPromotionLink, getPromotionStatistics } from '@/api/merchant'
import { formatMoney, formatDateTime } from '@/utils'
import { ElMessage } from 'element-plus'

const promotionLink = ref('')
const statistics = ref({})

const loadPromotionLink = async () => {
  try {
    const result = await getPromotionLink()
    promotionLink.value = result.link
  } catch (error) {
    console.error('加载推广链接失败:', error)
  }
}

const loadStatistics = async () => {
  try {
    statistics.value = await getPromotionStatistics()
  } catch (error) {
    console.error('加载推广统计失败:', error)
  }
}

const copyLink = () => {
  navigator.clipboard.writeText(promotionLink.value)
  ElMessage.success('复制成功')
}

onMounted(() => {
  loadPromotionLink()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.promotion-page {
  .promotion-link {
    margin-bottom: 10px;
  }
}
</style>

