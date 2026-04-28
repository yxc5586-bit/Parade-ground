<template>
  <div class="page result-page" v-loading="loading">
    <template v-if="detail">
      <section class="result-head">
        <div>
          <p class="eyebrow">结算报告</p>
          <h1>{{ question.levelName || '闯关结果' }}</h1>
          <div class="tag-list">
            <el-tag v-if="question.difficulty" type="success">{{ question.difficulty }}</el-tag>
            <el-tag v-if="question.salaryRange" type="warning">{{ question.salaryRange }}</el-tag>
            <el-tag v-for="tag in normalizeArray(question.tags)" :key="tag" type="info">{{ tag }}</el-tag>
          </div>
        </div>
        <div class="toolbar">
          <el-button plain :icon="Back" @click="router.push('/')">回大厅</el-button>
          <el-button type="primary" :icon="Promotion" :loading="creating" @click="startNextLevel">下一关</el-button>
        </div>
      </section>

      <ResultReport :result="result" />

      <section class="detail-grid">
        <div class="section">
          <div class="section-header">
            <div>
              <h2 class="section-title">本次作答</h2>
              <p class="section-desc">耗时 {{ formatSpendSeconds(userAnswer.clientSpendSeconds) }}</p>
            </div>
          </div>
          <div class="answer-review">
            <div class="tag-list">
              <el-tag v-for="item in normalizeArray(userAnswer.selectedOptionIds)" :key="item" effect="plain">
                {{ item }}
              </el-tag>
            </div>
          </div>
        </div>

        <div class="section">
          <div class="section-header">
            <div>
              <h2 class="section-title">题面回放</h2>
              <p class="section-desc">历史详情由后端按关卡回查题面。</p>
            </div>
          </div>
          <div class="question-review">
            <RequirementView :requirement="requirement" />
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Back, Promotion } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { gameApi } from '../api/game'
import { levelApi } from '../api/level'
import RequirementView from '../components/RequirementView.vue'
import ResultReport from '../components/ResultReport.vue'
import { useUser } from '../composables/useUser'
import { formatSpendSeconds, normalizeArray } from '../utils/format'

const route = useRoute()
const router = useRouter()
const { fetchCurrentUser } = useUser()
const detail = ref(null)
const loading = ref(false)
const creating = ref(false)

const question = computed(() => detail.value?.question || {})
const userAnswer = computed(() => detail.value?.userAnswer || {})
const result = computed(() => detail.value?.result || {})
const requirement = computed(() => {
  const value = question.value.requirement
  if (value && typeof value === 'object' && !Array.isArray(value)) return value
  if (typeof value === 'string') return { background: value }
  return {}
})

onMounted(async () => {
  await Promise.all([fetchDetail(), fetchCurrentUser({ silent: true })])
})

async function fetchDetail() {
  loading.value = true
  try {
    detail.value = await gameApi.recordDetail(route.params.recordId)
  } finally {
    loading.value = false
  }
}

async function startNextLevel() {
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
.result-page {
  display: grid;
  gap: 18px;
}

.result-head {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 18px;
  align-items: end;
  padding: 22px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--primary);
  font-size: 13px;
  font-weight: 900;
}

.result-head h1 {
  margin: 0 0 12px;
  font-size: 28px;
  line-height: 1.3;
}

.detail-grid {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 18px;
  align-items: start;
}

.answer-review,
.question-review {
  padding: 18px;
}

@media (max-width: 900px) {
  .result-head,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
