<template>
  <div class="tickets-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>{{ $t('merchantTickets.title') }}</h3>
          <el-button type="primary" @click="handleCreate">{{ $t('merchantTickets.createTicket') }}</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="$t('merchantTickets.status')">
          <el-select v-model="searchForm.status" :placeholder="$t('merchantTickets.selectStatus')" clearable style="width: 150px">
            <el-option :label="$t('merchantTickets.statusOpen')" value="OPEN" />
            <el-option :label="$t('merchantTickets.statusInProgress')" value="IN_PROGRESS" />
            <el-option :label="$t('merchantTickets.statusResolved')" value="RESOLVED" />
            <el-option :label="$t('merchantTickets.statusClosed')" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('merchantTickets.category')">
          <el-select v-model="searchForm.category" :placeholder="$t('merchantTickets.selectCategory')" clearable style="width: 150px">
            <el-option :label="$t('merchantTickets.categoryTechSupport')" value="TECH_SUPPORT" />
            <el-option :label="$t('merchantTickets.categoryAccount')" value="ACCOUNT" />
            <el-option :label="$t('merchantTickets.categoryPayment')" value="PAYMENT" />
            <el-option :label="$t('merchantTickets.categoryOther')" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTickets">{{ $t('merchantTickets.search') }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tickets" v-loading="loading">
        <el-table-column :label="$t('merchantTickets.ticketNo')" prop="ticketNo" width="180" />
        <el-table-column :label="$t('merchantTickets.ticketTitle')" prop="title" min-width="200" />
        <el-table-column :label="$t('merchantTickets.category')" prop="category" width="120">
          <template #default="{ row }">
            {{ getCategoryText(row.category) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantTickets.priority')" prop="priority" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantTickets.status')" prop="status" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantTickets.createdAt')" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('merchantTickets.operation')" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">{{ $t('merchantTickets.view') }}</el-button>
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

    <!-- 创建工单对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="$t('merchantTickets.createTitle')"
      width="600px"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item :label="$t('merchantTickets.category')" prop="category">
          <el-select v-model="createForm.category" :placeholder="$t('merchantTickets.selectCategory')">
            <el-option :label="$t('merchantTickets.categoryTechSupport')" value="TECH_SUPPORT" />
            <el-option :label="$t('merchantTickets.categoryAccount')" value="ACCOUNT" />
            <el-option :label="$t('merchantTickets.categoryPayment')" value="PAYMENT" />
            <el-option :label="$t('merchantTickets.categoryOther')" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('merchantTickets.priority')" prop="priority">
          <el-select v-model="createForm.priority" :placeholder="$t('merchantTickets.selectPriority')">
            <el-option :label="$t('merchantTickets.priorityLow')" value="LOW" />
            <el-option :label="$t('merchantTickets.priorityNormal')" value="NORMAL" />
            <el-option :label="$t('merchantTickets.priorityHigh')" value="HIGH" />
            <el-option :label="$t('merchantTickets.priorityUrgent')" value="URGENT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('merchantTickets.ticketTitle')" prop="title">
          <el-input v-model="createForm.title" :placeholder="$t('merchantTickets.inputTitle')" />
        </el-form-item>
        <el-form-item :label="$t('merchantTickets.content')" prop="content">
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="6"
            :placeholder="$t('merchantTickets.inputContent')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">{{ $t('merchantTickets.cancel') }}</el-button>
        <el-button type="primary" @click="handleSubmitCreate" :loading="creating">{{ $t('merchantTickets.submit') }}</el-button>
      </template>
    </el-dialog>

    <!-- 工单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="$t('merchantTickets.detailTitle')"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentTicket">
        <el-descriptions-item :label="$t('merchantTickets.ticketNo')">{{ currentTicket.ticketNo }}</el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.status')">
          <el-tag :type="getStatusType(currentTicket.status)">
            {{ getStatusText(currentTicket.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.ticketTitle')">{{ currentTicket.title }}</el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.category')">{{ getCategoryText(currentTicket.category) }}</el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.priority')">
          <el-tag :type="getPriorityType(currentTicket.priority)">
            {{ getPriorityText(currentTicket.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.createdAt')">
          {{ formatDateTime(currentTicket.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.content')" :span="2">
          <div style="white-space: pre-wrap; color: #606266;">{{ currentTicket.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.adminReply')" :span="2" v-if="currentTicket.adminReply">
          <div style="white-space: pre-wrap; color: #409eff;">{{ currentTicket.adminReply }}</div>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('merchantTickets.createdAt')" v-if="currentTicket.repliedAt">
          {{ formatDateTime(currentTicket.repliedAt) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">{{ $t('common.close') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { getMerchantTickets, createTicket } from '@/api/ticket'
import { formatDateTime } from '@/utils'
import { ElMessage } from 'element-plus'

const { t } = useI18n()

const tickets = ref([])
const loading = ref(false)
const creating = ref(false)

const searchForm = reactive({
  status: null,
  category: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  category: '',
  priority: 'NORMAL',
  title: '',
  content: ''
})

const createRules = computed(() => ({
  category: [{ required: true, message: t('merchantTickets.selectCategory'), trigger: 'change' }],
  priority: [{ required: true, message: t('merchantTickets.selectPriority'), trigger: 'change' }],
  title: [{ required: true, message: t('merchantTickets.inputTitle'), trigger: 'blur' }],
  content: [{ required: true, message: t('merchantTickets.inputContent'), trigger: 'blur' }]
}))

const detailDialogVisible = ref(false)
const currentTicket = ref(null)

const loadTickets = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...searchForm
    }
    const res = await getMerchantTickets(params)
    tickets.value = res.records || []
    pagination.total = res.total || 0
  } catch (error) {
    console.error('Load tickets failed:', error)
    ElMessage.error(t('merchantTickets.loadFailed'))
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  createForm.category = ''
  createForm.priority = 'NORMAL'
  createForm.title = ''
  createForm.content = ''
  createDialogVisible.value = true
}

const handleSubmitCreate = async () => {
  try {
    await createFormRef.value.validate()
    creating.value = true
    await createTicket({
      category: createForm.category,
      priority: createForm.priority,
      title: createForm.title,
      content: createForm.content
    })
    ElMessage.success(t('merchantTickets.createSuccess'))
    createDialogVisible.value = false
    loadTickets()
  } catch (error) {
    if (error !== false) {
      console.error('Create ticket failed:', error)
      ElMessage.error(t('merchantTickets.createFailed'))
    }
  } finally {
    creating.value = false
  }
}

const handleView = (ticket) => {
  currentTicket.value = ticket
  detailDialogVisible.value = true
}

const getCategoryText = (category) => {
  const map = {
    TECH_SUPPORT: t('merchantTickets.categoryTechSupport'),
    ACCOUNT: t('merchantTickets.categoryAccount'),
    PAYMENT: t('merchantTickets.categoryPayment'),
    OTHER: t('merchantTickets.categoryOther')
  }
  return map[category] || category
}

const getPriorityText = (priority) => {
  const map = {
    LOW: t('merchantTickets.priorityLow'),
    NORMAL: t('merchantTickets.priorityNormal'),
    HIGH: t('merchantTickets.priorityHigh'),
    URGENT: t('merchantTickets.priorityUrgent')
  }
  return map[priority] || priority
}

const getPriorityType = (priority) => {
  const map = {
    LOW: 'info',
    NORMAL: '',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[priority] || ''
}

const getStatusText = (status) => {
  const map = {
    OPEN: t('merchantTickets.statusOpen'),
    IN_PROGRESS: t('merchantTickets.statusInProgress'),
    RESOLVED: t('merchantTickets.statusResolved'),
    CLOSED: t('merchantTickets.statusClosed')
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    OPEN: 'warning',
    IN_PROGRESS: 'primary',
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return map[status] || ''
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
    display: flex;
    justify-content: center;
  }
}
</style>
