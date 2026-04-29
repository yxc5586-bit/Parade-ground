<template>
  <div class="page home-page">
    <section class="command-band">
      <div>
        <p class="eyebrow">长城落日 · 代码演武</p>
        <h1>程序员技术练兵场</h1>
        <p class="commander">
          {{ state.user?.userName || state.user?.userAccount }}，入营列阵。每一关都会按当前薪资生成企业场景题，在代码沙场中磨炼基本功，突破技术关卡。
        </p>
        <div class="hero-ledger">
          <span>今日目标：破阵一关</span>
          <span>训练方式：方案评审</span>
          <span>战力结算：薪资曲线</span>
        </div>
      </div>
      <div class="actions">
        <el-select v-model="preferredDirection" class="direction-select" placeholder="方向">
          <el-option label="后端破阵营" value="backend" />
          <el-option label="前端攻城营" value="frontend" />
          <el-option label="系统设计沙盘" value="architecture" />
          <el-option label="全栈远征营" value="fullstack" />
        </el-select>
        <el-button
          type="primary"
          size="large"
          :icon="currentLevel ? Right : Promotion"
          :loading="generating"
          @click="enterChallenge"
        >
          {{ currentLevel ? '继续当前战役' : '开始练兵' }}
        </el-button>
      </div>
    </section>

    <section class="metric-grid">
      <div class="metric">
        <span class="metric-label">当前月薪</span>
        <strong class="metric-value amber">{{ formatSalary(state.user?.currentSalary) }}</strong>
      </div>
      <div class="metric">
        <span class="metric-label">当前军阶</span>
        <strong class="metric-value">{{ rank }}</strong>
      </div>
      <div class="metric">
        <span class="metric-label">累计战役</span>
        <strong class="metric-value">{{ page.total || 0 }} 次</strong>
      </div>
    </section>

    <section v-if="currentLevel" class="section">
      <div class="section-header">
        <div>
          <h2 class="section-title">进行中的关卡</h2>
          <p class="section-desc">战局尚未收束，继续完成这场技术演武。</p>
        </div>
        <el-button type="primary" :icon="Right" @click="goChallenge(currentLevel.levelId)">继续破阵</el-button>
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
          <p class="section-desc">看分数，也看薪资曲线。沙场没有白练的基本功。</p>
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
import { createPendingTask } from '../utils/pendingTask'

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
    const task = createPendingTask({
      type: 'generate-level',
      payload: { preferredDirection: preferredDirection.value },
      from: router.currentRoute.value.fullPath,
    })
    await router.push({ name: 'loading', query: { id: task.id } })
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
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
  align-items: end;
  min-height: 420px;
  padding: 42px;
  color: var(--text);
  background:
    linear-gradient(90deg, rgba(31, 22, 15, 0.92) 0%, rgba(31, 22, 15, 0.66) 42%, rgba(31, 22, 15, 0.16) 100%),
    linear-gradient(0deg, rgba(31, 22, 15, 0.52), rgba(31, 22, 15, 0.08)),
    url('../assets/banner.png') center / cover no-repeat;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.command-band::after {
  position: absolute;
  inset: auto 0 0;
  height: 36%;
  pointer-events: none;
  content: "";
  background: linear-gradient(180deg, transparent, rgba(31, 22, 15, 0.76));
}

.command-band > * {
  position: relative;
  z-index: 1;
}

.eyebrow {
  margin: 0 0 10px;
  color: #e9c275;
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.12em;
}

.command-band h1 {
  margin: 0;
  max-width: 620px;
  font-size: clamp(34px, 5vw, 58px);
  line-height: 1.25;
  letter-spacing: 0;
  text-shadow: 0 3px 22px rgba(12, 8, 4, 0.5);
}

.commander {
  max-width: 680px;
  margin: 12px 0 0;
  color: #ecd8aa;
  line-height: 1.75;
}

.hero-ledger {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 22px;
}

.hero-ledger span {
  padding: 8px 10px;
  color: #f0d8a1;
  background: rgba(31, 22, 15, 0.42);
  border: 1px solid rgba(217, 154, 61, 0.28);
  border-radius: 4px;
  font-size: 13px;
  font-weight: 800;
}

.actions {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 12px;
  background: rgba(31, 22, 15, 0.52);
  border: 1px solid rgba(217, 154, 61, 0.26);
  border-radius: var(--radius);
  backdrop-filter: blur(10px);
}

.direction-select {
  width: 168px;
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
    min-height: 520px;
    padding: 28px 20px;
    background-position: 62% center;
  }

  .actions {
    flex-direction: column;
    align-items: stretch;
  }

  .direction-select {
    width: 100%;
  }
}
</style>
