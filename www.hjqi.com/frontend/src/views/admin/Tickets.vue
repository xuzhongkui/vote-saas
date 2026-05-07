<template>
  <div class="tickets-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('adminTickets.title') }}</h3>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('adminTickets.merchantId')">
          <el-input v-model="searchForm.merchantId" :placeholder="$t('adminTickets.inputMerchantId')" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item :label="$t('common.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('common.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('adminTickets.statusOpen')" value="OPEN" />
            <el-option :label="$t('adminTickets.statusInProgress')" value="IN_PROGRESS" />
            <el-option :label="$t('adminTickets.statusResolved')" value="RESOLVED" />
            <el-option :label="$t('adminTickets.statusClosed')" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('adminTickets.category')">
          <el-select v-model="searchForm.category" :placeholder="$t('adminTickets.selectCategory')" clearable style="width: 150px">
            <el-option :label="$t('adminTickets.categoryTechSupport')" value="TECH_SUPPORT" />
            <el-option :label="$t('adminTickets.categoryAccount')" value="ACCOUNT" />
            <el-option :label="$t('adminTickets.categoryPayment')" value="PAYMENT" />
            <el-option :label="$t('adminTickets.categoryOther')" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTickets">{{ $t('common.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tickets" v-loading="loading">
        <el-table-column :label="$t('adminTickets.ticketNo')" prop="ticketNo" width="180" />
        <el-table-column :label="$t('adminTickets.merchantId')" prop="merchantId" width="100" />
        <el-table-column :label="$t('adminTickets.ticketTitle')" prop="title" min-width="200" />
        <el-table-column :label="$t('adminTickets.category')" prop="category" width="120" />
        <el-table-column :label="$t('adminTickets.priority')" prop="priority" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.status')" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.createTime')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.operation')" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">{{ $t('common.view') }}</el-button>
            <el-button type="success" link @click="handleReply(row)" v-if="row.status !== 'CLOSED'">{{ $t('adminTickets.reply') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadTickets"
        @current-change="loadTickets"
        class="pagination"
      />
    </el-card>

    <!-- 工单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="$t('adminTickets.ticketDetail')"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentTicket">
        <el-descriptions-item :label="$t('adminTickets.ticketNo')">{{ currentTicket.ticketNo }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminTickets.merchantId')">{{ currentTicket.merchantId }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminTickets.ticketTitle')">{{ currentTicket.title }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminTickets.category')">{{ currentTicket.category }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminTickets.priority')">
          <el-tag :type="getPriorityType(currentTicket.priority)">
            {{ getPriorityText(currentTicket.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.status')">
          <el-tag :type="getStatusType(currentTicket.status)">
            {{ getStatusText(currentTicket.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminTickets.content')" :span="2">
          <div style="white-space: pre-wrap;">{{ currentTicket.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('adminTickets.adminReply')" :span="2" v-if="currentTicket.adminReply">
          <div style="white-space: pre-wrap;">{{ currentTicket.adminReply }}</div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('common.createTime')">{{ formatDateTime(currentTicket.createdAt) }}</el-descriptions-item>
        <el-descriptions-item :label="$t('adminTickets.resolvedAt')" v-if="currentTicket.resolvedAt">
          {{ formatDateTime(currentTicket.resolvedAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      :title="$t('adminTickets.replyTicket')"
      width="600px"
    >
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="100px">
        <el-form-item :label="$t('adminTickets.replyContent')" prop="adminReply">
          <el-input
            v-model="replyForm.adminReply"
            type="textarea"
            :rows="6"
            :placeholder="$t('adminTickets.inputReplyContent')"
          />
        </el-form-item>
        <el-form-item :label="$t('common.status')" prop="status">
          <el-select v-model="replyForm.status" :placeholder="$t('common.selectStatus')">
            <el-option :label="$t('adminTickets.statusInProgress')" value="IN_PROGRESS" />
            <el-option :label="$t('adminTickets.statusResolved')" value="RESOLVED" />
            <el-option :label="$t('adminTickets.statusClosed')" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitReply">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const { t } = useI18n()
const loading = ref(false)
const detailDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const currentTicket = ref(null)
const replyFormRef = ref(null)

const searchForm = reactive({
  merchantId: '',
  status: '',
  category: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const tickets = ref([])

const replyForm = reactive({
  adminReply: '',
  status: 'IN_PROGRESS'
})

const replyRules = {
  adminReply: [{ required: true, message: () => t('adminTickets.pleaseInputReply'), trigger: 'blur' }],
  status: [{ required: true, message: () => t('adminTickets.pleaseSelectStatus'), trigger: 'change' }]
}

const getPriorityText = (priority) => {
  const map = { 
    LOW: t('adminTickets.priorityLow'), 
    NORMAL: t('adminTickets.priorityNormal'), 
    HIGH: t('adminTickets.priorityHigh'), 
    URGENT: t('adminTickets.priorityUrgent') 
  }
  return map[priority] || priority
}

const getPriorityType = (priority) => {
  const map = { LOW: 'info', NORMAL: '', HIGH: 'warning', URGENT: 'danger' }
  return map[priority] || ''
}

const getStatusText = (status) => {
  const map = { 
    OPEN: t('adminTickets.statusOpen'), 
    IN_PROGRESS: t('adminTickets.statusInProgress'), 
    RESOLVED: t('adminTickets.statusResolved'), 
    CLOSED: t('adminTickets.statusClosed') 
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = { OPEN: 'warning', IN_PROGRESS: 'primary', RESOLVED: 'success', CLOSED: 'info' }
  return map[status] || ''
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const loadTickets = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    if (searchForm.merchantId) {
      params.merchantId = parseInt(searchForm.merchantId)
    }
    if (searchForm.status) {
      params.status = searchForm.status
    }
    if (searchForm.category) {
      params.category = searchForm.category
    }
    const res = await request.get('/ticket/admin/list', { params })
    tickets.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    ElMessage.error(t('adminTickets.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleView = (row) => {
  currentTicket.value = row
  detailDialogVisible.value = true
}

const handleReply = (row) => {
  currentTicket.value = row
  Object.assign(replyForm, {
    adminReply: '',
    status: row.status === 'OPEN' ? 'IN_PROGRESS' : row.status
  })
  replyDialogVisible.value = true
}

const handleSubmitReply = async () => {
  if (!replyFormRef.value) return
  try {
    await replyFormRef.value.validate()
        await request.post('/ticket/handle', null, {
      params: {
        ticketId: currentTicket.value.id,
        adminReply: replyForm.adminReply,
        status: replyForm.status
      }
    })
    ElMessage.success(t('adminTickets.replySuccess'))
    replyDialogVisible.value = false
    loadTickets()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(t('adminTickets.replyFailed'))
    }
  }
}

onMounted(() => {
  loadTickets()
})
</script>

<style scoped lang="scss">
.tickets-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    justify-content: flex-end;
  }
}
</style>

