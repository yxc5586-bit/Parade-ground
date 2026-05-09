<!-- 管理员关卡管理页 — 筛选/分页查看所有关卡，支持编辑关卡信息、设置精选优先级、删除关卡 -->
<template>
  <div class="admin-page">
    <div class="page-header">
      <h1>关卡管理</h1>
      <span class="role-badge">管理员</span>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
        v-model="filters.levelName"
        placeholder="关卡名称"
        clearable
        style="width: 200px"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="filters.difficulty" placeholder="难度" clearable style="width: 140px">
        <el-option label="基础" value="basic" />
        <el-option label="中级" value="intermediate" />
        <el-option label="高级" value="advanced" />
        <el-option label="专家" value="expert" />
        <el-option label="平台" value="platform" />
      </el-select>
      <el-select v-model="filters.salaryRange" placeholder="薪资范围" clearable style="width: 160px">
        <el-option label="10000-15000" value="10000-15000" />
        <el-option label="15000-25000" value="15000-25000" />
        <el-option label="25000-35000" value="25000-35000" />
        <el-option label="35000-50000" value="35000-50000" />
        <el-option label="50000+" value="50000+" />
      </el-select>
      <el-select v-model="filters.priority" placeholder="精选状态" clearable style="width: 140px">
        <el-option label="精选" :value="999" />
        <el-option label="非精选" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 关卡表格 -->
    <el-table :data="tableData" v-loading="loading" class="level-table">
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="levelId" label="业务编号" min-width="180" show-overflow-tooltip />
      <el-table-column prop="levelName" label="关卡名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="difficulty" label="难度" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="difficultyTag(row.difficulty)" size="small">{{ row.difficulty }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="salaryRange" label="薪资范围" width="120" align="center" />
      <el-table-column label="优先级" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="priorityTag(row.priority)" size="small" effect="dark">
            {{ priorityLabel(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="精选操作" width="170" align="center" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.priority !== 999"
            type="warning"
            size="small"
            :icon="Star"
            @click="handleSetPriority(row, 999)"
          >
            设为精选
          </el-button>
          <el-button
            v-else
            type="info"
            size="small"
            @click="handleSetPriority(row, 0)"
          >
            取消精选
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="170" align="center" fixed="right">
        <template #default="{ row }">
          <span class="action-btns">
            <el-button type="primary" size="small" :icon="Edit" @click="openEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定要删除该关卡吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" size="small" :icon="Delete">删除</el-button>
              </template>
            </el-popconfirm>
          </span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑关卡" width="800px" destroy-on-close>
      <el-form v-if="editForm" :model="editForm" label-width="110px">
        <el-form-item label="关卡名称">
          <el-input v-model="editForm.levelName" />
        </el-form-item>
        <el-form-item label="业务编号">
          <el-input v-model="editForm.levelId" />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="editForm.difficulty" style="width: 100%">
            <el-option label="basic" value="basic" />
            <el-option label="intermediate" value="intermediate" />
            <el-option label="advanced" value="advanced" />
            <el-option label="expert" value="expert" />
            <el-option label="platform" value="platform" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资范围">
          <el-select v-model="editForm.salaryRange" style="width: 100%">
            <el-option label="10000-15000" value="10000-15000" />
            <el-option label="15000-25000" value="15000-25000" />
            <el-option label="25000-35000" value="25000-35000" />
            <el-option label="35000-50000" value="35000-50000" />
            <el-option label="50000+" value="50000+" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="editForm.priority" style="width: 100%">
            <el-option label="普通 (0)" :value="0" />
            <el-option label="提升 (99)" :value="99" />
            <el-option label="精选 (999)" :value="999" />
            <el-option label="置顶 (9999)" :value="9999" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="题目需求">
          <el-input v-model="editForm.requirement" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="选项列表">
          <el-input v-model="editForm.options" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="正确答案">
          <el-input v-model="editForm.correctOptionIds" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="解析方向">
          <el-input v-model="editForm.analysisDirection" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Delete, Edit, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { levelApi } from '../api/level'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const editDialogVisible = ref(false)
const editForm = ref(null)

const filters = reactive({
  levelName: '',
  difficulty: '',
  salaryRange: '',
  priority: '',
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
})

function fetchList() {
  loading.value = true
  const params = {
    current: pagination.current,
    pageSize: pagination.pageSize,
  }
  if (filters.levelName) params.levelName = filters.levelName
  if (filters.difficulty) params.difficulty = filters.difficulty
  if (filters.salaryRange) params.salaryRange = filters.salaryRange
  if (filters.priority !== '' && filters.priority !== null) params.priority = filters.priority

  levelApi
    .adminList(params)
    .then((res) => {
      tableData.value = res.records || []
      pagination.total = res.total || 0
      pagination.current = res.current || 1
    })
    .catch(() => {
      tableData.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

function handleSearch() {
  pagination.current = 1
  fetchList()
}

function handleReset() {
  filters.levelName = ''
  filters.difficulty = ''
  filters.salaryRange = ''
  filters.priority = ''
  pagination.current = 1
  fetchList()
}

async function handleSetPriority(row, priority) {
  try {
    await levelApi.adminSetPriority(row.id, priority)
    ElMessage.success(priority === 999 ? '已设为精选' : '已取消精选')
    fetchList()
  } catch {
    // error handled by interceptor
  }
}

async function handleDelete(id) {
  try {
    await levelApi.adminDelete(id)
    ElMessage.success('关卡已删除')
    fetchList()
  } catch {
    // error handled by interceptor
  }
}

function openEditDialog(row) {
  editForm.value = { ...row }
  editDialogVisible.value = true
}

async function handleSaveEdit() {
  saving.value = true
  try {
    await levelApi.adminUpdate(editForm.value)
    ElMessage.success('关卡已更新')
    editDialogVisible.value = false
    fetchList()
  } catch {
    // error handled by interceptor
  } finally {
    saving.value = false
  }
}

function difficultyTag(difficulty) {
  const map = {
    basic: 'info',
    intermediate: '',
    advanced: 'warning',
    expert: 'danger',
    platform: 'danger',
  }
  return map[difficulty] || 'info'
}

function priorityTag(priority) {
  const p = Number(priority || 0)
  if (p >= 9999) return 'danger'
  if (p >= 999) return 'warning'
  if (p >= 99) return 'success'
  return 'info'
}

function priorityLabel(priority) {
  const p = Number(priority || 0)
  if (p >= 9999) return '置顶'
  if (p >= 999) return '精选'
  if (p >= 99) return '提升'
  return '普通'
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-page {
  max-width: 1300px;
  margin: 0 auto;
  padding: 28px 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 22px;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  color: var(--primary);
}

.role-badge {
  padding: 2px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary), var(--amber));
  color: #1f160f;
  font-size: 12px;
  font-weight: 700;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.level-table {
  margin-bottom: 18px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.3), 0 0 0 1px var(--line);
}

.level-table :deep(.el-table__body tr:nth-child(even) td) {
  background-color: rgba(255, 255, 255, 0.03);
}

.level-table :deep(.el-table__body tr:nth-child(odd) td) {
  background-color: transparent;
}

.level-table :deep(.el-table__header th) {
  background-color: rgba(255, 255, 255, 0.04);
  border-bottom: 1px solid var(--line);
}

.level-table :deep(.el-table__body td) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.action-btns {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
}
</style>
