<template>
  <div class="page records-page">
    <section class="section">
      <div class="section-header">
        <div>
          <h1 class="section-title">历史战绩</h1>
          <p class="section-desc">每一次方案评审都会沉淀在这里，方便复盘薪资变化和错漏点。</p>
        </div>
        <el-button type="primary" :icon="Promotion" :loading="creating" @click="startNewLevel">生成下一关</el-button>
      </div>

      <div class="table-wrap">
        <el-table v-loading="loading" :data="page.records" empty-text="暂无战绩">
          <el-table-column prop="levelName" label="关卡名称" min-width="260" />
          <el-table-column prop="score" label="得分" width="100">
            <template #default="{ row }">
              <el-tag :type="scoreTagType(row.score)" effect="plain">{{ row.score }} 分</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="salaryChange" label="薪资变化" width="130">
            <template #default="{ row }">
              <strong :class="Number(row.salaryChange) >= 0 ? 'salary-up' : 'salary-down'">
                {{ formatSignedMoney(row.salaryChange) }}
              </strong>
            </template>
          </el-table-column>
          <el-table-column prop="updatedSalary" label="结算月薪" width="160">
            <template #default="{ row }">{{ formatSalary(row.updatedSalary) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="作答时间" width="180">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="110" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="router.push(`/result/${row.recordId}`)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.pageSize"
          background
          layout="prev, pager, next, sizes, total"
          :page-sizes="[5, 10, 20, 50]"
          :total="page.total"
          @current-change="fetchRecords"
          @size-change="handleSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Promotion } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { gameApi } from '../api/game'
import { levelApi } from '../api/level'
import { formatDateTime, formatSalary, formatSignedMoney } from '../utils/format'

const router = useRouter()
const loading = ref(false)
const creating = ref(false)
const page = reactive({
  records: [],
  total: 0,
  current: 1,
  pageSize: 10,
  pages: 0,
})

onMounted(fetchRecords)

function scoreTagType(score) {
  const value = Number(score || 0)
  if (value >= 80) return 'success'
  if (value >= 60) return 'warning'
  return 'danger'
}

async function fetchRecords() {
  loading.value = true
  try {
    const data = await gameApi.recordsPage({
      current: page.current,
      pageSize: page.pageSize,
    })
    Object.assign(page, data || { records: [] })
  } finally {
    loading.value = false
  }
}

function handleSizeChange() {
  page.current = 1
  fetchRecords()
}

async function startNewLevel() {
  creating.value = true
  try {
    const data = await levelApi.generate({ preferredDirection: 'backend' })
    router.push(`/challenge/${data.levelId}`)
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.records-page {
  display: grid;
  gap: 18px;
}

.table-wrap {
  padding: 18px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  padding: 0 18px 18px;
}

@media (max-width: 900px) {
  .pagination {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
</style>
