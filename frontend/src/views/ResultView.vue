<!-- 结算报告页 — 展示AI评审得分、薪资变化、命中/误选/遗漏分析、标准答案与解法，以及投递建议 -->
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
          <el-button plain :icon="Back" @click="router.push('/')">回营门</el-button>
          <el-button type="primary" :icon="Promotion" :loading="creating" @click="startNextLevel">再开一役</el-button>
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
            <div class="tag-list answer-tags">
              <el-tag v-for="item in normalizeArray(userAnswer.selectedOptionIds)" :key="item" effect="plain">
                {{ item }}
              </el-tag>
            </div>
            <div class="option-replay">
              <div class="option-replay-header">
                <h3>完整选项</h3>
                <span>{{ allOptions.length }} 项</span>
              </div>
              <div v-if="allOptions.length" class="result-option-list">
                <article v-for="option in allOptions" :key="option.id" class="result-option-card" :class="optionClass(option.id)">
                  <strong>{{ option.id }}</strong>
                  <p>{{ option.content }}</p>
                  <div class="option-state">
                    <el-tag v-if="isSelectedOption(option.id)" size="small" effect="plain">本次布阵</el-tag>
                    <el-tag v-if="isStandardOption(option.id)" size="small" type="success" effect="plain">标准答案</el-tag>
                  </div>
                </article>
              </div>
              <p v-else class="option-empty">本条记录暂未返回完整选项。</p>
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
import RequirementView from '../components/RequirementView.vue'
import ResultReport from '../components/ResultReport.vue'
import { useUser } from '../composables/useUser'
import { formatSpendSeconds, normalizeArray, normalizeOptions } from '../utils/format'
import { createPendingTask } from '../utils/pendingTask'

const route = useRoute()
const router = useRouter()
const { fetchCurrentUser } = useUser()
const detail = ref(null)
const loading = ref(false)
const creating = ref(false)

const question = computed(() => detail.value?.question || {})
const userAnswer = computed(() => detail.value?.userAnswer || {})
const result = computed(() => detail.value?.result || {})
const allOptions = computed(() => normalizeOptions(question.value.options))
const selectedAnswerIds = computed(() => new Set(normalizeArray(userAnswer.value.selectedOptionIds).map(normalizeOptionId)))
const standardAnswerIds = computed(() => new Set(normalizeArray(result.value.standardAnswers).map(normalizeOptionId)))
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
    const task = createPendingTask({
      type: 'generate-level',
      payload: { preferredDirection: 'backend' },
      from: router.currentRoute.value.fullPath,
    })
    await router.push({ name: 'loading', query: { id: task.id } })
  } finally {
    creating.value = false
  }
}

function normalizeOptionId(value) {
  return String(value || '').toUpperCase()
}

function isSelectedOption(id) {
  return selectedAnswerIds.value.has(normalizeOptionId(id))
}

function isStandardOption(id) {
  return standardAnswerIds.value.has(normalizeOptionId(id))
}

function optionClass(id) {
  return {
    'is-selected': isSelectedOption(id),
    'is-standard': isStandardOption(id),
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
  grid-template-columns: minmax(360px, 420px) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.answer-review,
.question-review {
  padding: 18px;
}

.answer-review {
  display: grid;
  gap: 18px;
}

.answer-tags {
  min-height: 32px;
}

.option-replay {
  display: grid;
  gap: 12px;
}

.option-replay-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.option-replay-header h3 {
  margin: 0;
  font-size: 15px;
  line-height: 1.35;
}

.option-replay-header span {
  color: var(--muted);
  font-size: 13px;
  font-weight: 800;
}

.result-option-list {
  display: grid;
  gap: 10px;
}

.result-option-card {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: start;
  padding: 12px;
  background: var(--panel-soft);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.result-option-card.is-selected {
  background: rgba(217, 154, 61, 0.12);
  border-color: var(--line-bright);
}

.result-option-card.is-standard {
  box-shadow: inset 3px 0 0 var(--primary);
}

.result-option-card strong {
  display: grid;
  width: 34px;
  height: 30px;
  place-items: center;
  color: #1f160f;
  background: var(--primary);
  border-radius: 6px;
  font-size: 13px;
}

.result-option-card p {
  min-width: 0;
  margin: 0;
  color: var(--text);
  line-height: 1.65;
  overflow-wrap: anywhere;
}

.option-state {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  flex-wrap: wrap;
}

.option-empty {
  margin: 0;
  padding: 18px;
  color: var(--muted);
  text-align: center;
  background: var(--panel-soft);
  border: 1px dashed var(--line-bright);
  border-radius: var(--radius);
}

@media (max-width: 900px) {
  .result-head,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  .result-option-card {
    grid-template-columns: 34px minmax(0, 1fr);
  }

  .option-state {
    grid-column: 2;
    justify-content: flex-start;
  }
}
</style>
