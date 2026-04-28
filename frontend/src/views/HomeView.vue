<template>
  <div class="page home-page">
    <section class="command-band">
      <div>
        <p class="eyebrow">研发作战台</p>
        <h1>{{ state.user?.userName || state.user?.userAccount }}，今天继续评审方案</h1>
        <p>每一关都会按当前薪资生成企业场景题，选项拖进答题区后提交，由 AI 评委给出复盘和薪资调整。</p>
      </div>
      <div class="actions">
        <el-select v-model="preferredDirection" class="direction-select" placeholder="方向">
          <el-option label="后端" value="backend" />
          <el-option label="前端" value="frontend" />
          <el-option label="架构" value="architecture" />
          <el-option label="全栈" value="fullstack" />
        </el-select>
        <el-button
          type="primary"
          size="large"
          :icon="currentLevel ? Right : Promotion"
          :loading="generating"
          @click="enterChallenge"
        >
          {{ currentLevel ? '继续当前关卡' : '生成下一关' }}
        </el-button>
      </div>
    </section>

    <section class="metric-grid">
      <div class="metric">
        <span class="metric-label">当前月薪</span>
        <strong class="metric-value amber">{{ formatSalary(state.user?.currentSalary) }}</strong>
      </div>
      <div class="metric">
        <span class="metric-label">当前段位</span>
        <strong class="metric-value">{{ rank }}</strong>
      </div>
      <div class="metric">
        <span class="metric-label">最近闯关</span>
        <strong class="metric-value">{{ page.total || 0 }} 次</strong>
      </div>
    </section>

    <section v-if="currentLevel" class="section">
      <div class="section-header">
        <div>
          <h2 class="section-title">进行中的关卡</h2>
          <p class="section-desc">这题还没提交，后端会通过当前会话保留它。</p>
        </div>
        <el-button type="primary" :icon="Right" @click="goChallenge(currentLevel.levelId)">继续挑战</el-button>
      </div>
      <div class="current-level">
        <h3>{{ currentLevel.levelName }}</h3>
        <div class="tag-list">
          <el-tag type="success">{{ currentLevel.difficulty }}</el-tag>
          <el-tag type="warning">{{ currentLevel.salaryRange }}</el-tag>
          <el-tag v-for="tag in normalizeArray(currentLevel.tags)" :key="tag" type="info">{{ tag }}</el-tag>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="section-header">
        <div>
          <h2 class="section-title">最近战绩</h2>
          <p class="section-desc">看分数，也看薪资曲线。职场没有白答的题。</p>
        </div>
        <el-button plain :icon="Tickets" @click="router.push('/records')">全部战绩</el-button>
      </div>
      <el-table v-loading="loadingRecords" :data="page.records" class="record-table" empty-text="还没有战绩，先开一关">
        <el-table-column prop="levelName" label="关卡" min-width="220" />
        <el-table-column prop="score" label="得分" width="90">
          <template #default="{ row }">
            <strong>{{ row.score }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="salaryChange" label="薪资变化" width="120">
          <template #default="{ row }">
            <span :class="Number(row.salaryChange) >= 0 ? 'salary-up' : 'salary-down'">
              {{ formatSignedMoney(row.salaryChange) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="updatedSalary" label="结算月薪" width="150">
          <template #default="{ row }">{{ formatSalary(row.updatedSalary) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="router.push(`/result/${row.recordId}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Promotion, Right, Tickets } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { gameApi } from '../api/game'
import { levelApi } from '../api/level'
import { useUser } from '../composables/useUser'
import { formatDateTime, formatSalary, formatSignedMoney, normalizeArray } from '../utils/format'

const router = useRouter()
const { state, rank, fetchCurrentUser } = useUser()
const preferredDirection = ref('backend')
const currentLevel = ref(null)
const generating = ref(false)
const loadingRecords = ref(false)
const page = reactive({
  records: [],
  total: 0,
  current: 1,
  pageSize: 5,
  pages: 0,
})

onMounted(async () => {
  await Promise.all([fetchCurrentLevel(), fetchRecords(), fetchCurrentUser({ silent: true })])
})

async function fetchCurrentLevel() {
  currentLevel.value = await levelApi.current({ silent: true })
}

async function fetchRecords() {
  loadingRecords.value = true
  try {
    const data = await gameApi.recordsPage({ current: 1, pageSize: page.pageSize })
    Object.assign(page, data || { records: [] })
  } finally {
    loadingRecords.value = false
  }
}

function goChallenge(levelId) {
  router.push(`/challenge/${levelId}`)
}

async function enterChallenge() {
  if (currentLevel.value?.levelId) {
    goChallenge(currentLevel.value.levelId)
    return
  }
  generating.value = true
  try {
    const data = await levelApi.generate({ preferredDirection: preferredDirection.value })
    router.push(`/challenge/${data.levelId}`)
  } finally {
    generating.value = false
  }
}
</script>

<style scoped>
.home-page {
  display: grid;
  gap: 18px;
}

.command-band {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
  align-items: end;
  padding: 26px;
  color: #fff;
  background: #17362f;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 10px;
  color: #b9efe5;
  font-size: 13px;
  font-weight: 900;
}

.command-band h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.25;
  letter-spacing: 0;
}

.command-band p:last-child {
  max-width: 760px;
  margin: 12px 0 0;
  color: #d8e9e3;
  line-height: 1.75;
}

.actions {
  display: flex;
  gap: 10px;
}

.direction-select {
  width: 132px;
}

.amber {
  color: var(--amber);
}

.current-level {
  padding: 18px 20px;
}

.current-level h3 {
  margin: 0 0 12px;
  font-size: 20px;
}

.record-table {
  width: 100%;
}

@media (max-width: 900px) {
  .command-band {
    grid-template-columns: 1fr;
    padding: 20px;
  }

  .actions {
    flex-direction: column;
  }

  .direction-select {
    width: 100%;
  }
}
</style>
