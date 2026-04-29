<template>
  <div class="page challenge-page" v-loading="loading">
    <section v-if="level" class="challenge-head">
      <div>
        <p class="eyebrow">当前关卡</p>
        <h1>{{ level.levelName }}</h1>
        <div class="tag-list">
          <el-tag type="success">{{ difficultyLabel }}</el-tag>
          <el-tag type="warning">{{ level.salaryRange }}</el-tag>
          <el-tag v-for="tag in tags" :key="tag" type="info">{{ tag }}</el-tag>
        </div>
      </div>
      <div class="head-tools">
        <SalaryBadge :value="state.user?.currentSalary || 0" />
        <div class="timer">
          <el-icon><Timer /></el-icon>
          {{ formatSpendSeconds(elapsedSeconds) }}
        </div>
      </div>
    </section>

    <section v-if="level" class="challenge-grid">
      <div class="left-stack">
        <section class="section">
          <div class="section-header">
            <div>
              <h2 class="section-title">产品需求文档</h2>
              <p class="section-desc">先读业务闭环，再决定技术选项。</p>
            </div>
          </div>
          <div class="requirement-wrap">
            <RequirementView :requirement="requirement" />
          </div>
        </section>
      </div>

      <div class="right-stack">
        <section
          class="answer-box"
          :class="{ 'is-over': dropTarget === 'answer' }"
          @dragover.prevent="dropTarget = 'answer'"
          @dragleave="dropTarget = ''"
          @drop.prevent="dropToAnswer"
        >
          <div class="answer-header">
            <div>
              <h2>答题区域</h2>
              <p>把你认为正确的方案拖进来，也可以直接点击候选项添加。</p>
            </div>
            <el-tag effect="plain">{{ selectedOptions.length }} 项</el-tag>
          </div>

          <div v-if="!selectedOptions.length" class="answer-empty">
            这里还空着。把关键流程、组件、治理点放进来。
          </div>
          <div v-else class="selected-list">
            <button
              v-for="option in selectedOptions"
              :key="option.id"
              class="option-card selected"
              type="button"
              draggable="true"
              @dragstart="draggingId = option.id"
              @click="removeOption(option.id)"
            >
              <strong>{{ option.id }}</strong>
              <span>{{ option.content }}</span>
              <el-icon><Close /></el-icon>
            </button>
          </div>

          <div class="answer-actions">
            <el-button :icon="RefreshLeft" plain @click="resetAnswer">重置</el-button>
            <el-button type="primary" :icon="Select" :loading="submitting" :disabled="!selectedOptions.length" @click="submitAnswer">
              提交评审
            </el-button>
          </div>
        </section>

        <section
          class="option-pool"
          :class="{ 'is-over': dropTarget === 'pool' }"
          @dragover.prevent="dropTarget = 'pool'"
          @dragleave="dropTarget = ''"
          @drop.prevent="dropToPool"
        >
          <div class="pool-header">
            <div>
              <h2>候选选项</h2>
              <p>正确项、干扰项和迷惑项混在一起，评审味儿很足。</p>
            </div>
            <el-input v-model="keyword" clearable placeholder="搜索选项" />
          </div>

          <div class="option-list">
            <button
              v-for="option in filteredAvailableOptions"
              :key="option.id"
              class="option-card"
              type="button"
              draggable="true"
              @dragstart="draggingId = option.id"
              @click="addOption(option.id)"
            >
              <strong>{{ option.id }}</strong>
              <span>{{ option.content }}</span>
              <el-icon><Plus /></el-icon>
            </button>
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Close, Plus, RefreshLeft, Select, Timer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { levelApi } from '../api/level'
import RequirementView from '../components/RequirementView.vue'
import SalaryBadge from '../components/SalaryBadge.vue'
import { useUser } from '../composables/useUser'
import { formatSpendSeconds, normalizeArray, normalizeOptions } from '../utils/format'
import { createPendingTask } from '../utils/pendingTask'

const difficultyMap = {
  basic: '基础',
  intermediate: '进阶',
  advanced: '高级',
  expert: '专家',
  platform: '平台化',
}

const route = useRoute()
const router = useRouter()
const { state, fetchCurrentUser } = useUser()

const level = ref(null)
const selectedIds = ref([])
const draggingId = ref('')
const dropTarget = ref('')
const loading = ref(false)
const submitting = ref(false)
const keyword = ref('')
const startTime = ref(Date.now())
const elapsedSeconds = ref(0)
let timerId = 0

const options = computed(() => normalizeOptions(level.value?.options))
const tags = computed(() => normalizeArray(level.value?.tags))
const requirement = computed(() => {
  const value = level.value?.requirement
  if (value && typeof value === 'object' && !Array.isArray(value)) return value
  if (typeof value === 'string') return { background: value }
  return {}
})
const selectedOptions = computed(() => selectedIds.value.map((id) => options.value.find((item) => item.id === id)).filter(Boolean))
const availableOptions = computed(() => options.value.filter((item) => !selectedIds.value.includes(item.id)))
const filteredAvailableOptions = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return availableOptions.value
  return availableOptions.value.filter((item) => `${item.id} ${item.content}`.toLowerCase().includes(key))
})
const difficultyLabel = computed(() => difficultyMap[level.value?.difficulty] || level.value?.difficulty || '-')

onMounted(async () => {
  const [ready] = await Promise.all([loadLevel(), fetchCurrentUser({ silent: true })])
  if (ready) startTimer()
})

onBeforeUnmount(() => {
  window.clearInterval(timerId)
})

async function loadLevel() {
  loading.value = true
  try {
    const routeLevelId = route.params.levelId
    if (routeLevelId) {
      level.value = await levelApi.detail(routeLevelId)
      return true
    }

    const current = await levelApi.current({ silent: true })
    if (current?.levelId) {
      level.value = current
      router.replace(`/challenge/${current.levelId}`)
      return true
    }

    const task = createPendingTask({
      type: 'generate-level',
      payload: { preferredDirection: 'backend' },
      from: router.currentRoute.value.fullPath,
    })
    await router.replace({ name: 'loading', query: { id: task.id } })
    return false
  } finally {
    loading.value = false
  }
}

function startTimer() {
  startTime.value = Date.now()
  timerId = window.setInterval(() => {
    elapsedSeconds.value = Math.floor((Date.now() - startTime.value) / 1000)
  }, 1000)
}

function addOption(id) {
  if (!id || selectedIds.value.includes(id)) return
  selectedIds.value = [...selectedIds.value, id]
}

function removeOption(id) {
  selectedIds.value = selectedIds.value.filter((item) => item !== id)
}

function dropToAnswer() {
  addOption(draggingId.value)
  draggingId.value = ''
  dropTarget.value = ''
}

function dropToPool() {
  removeOption(draggingId.value)
  draggingId.value = ''
  dropTarget.value = ''
}

function resetAnswer() {
  selectedIds.value = []
  ElMessage.success('已清空答题区')
}

async function submitAnswer() {
  try {
    await ElMessageBox.confirm('提交后会触发 AI 评审并结算薪资，确认提交本关答案？', '提交评审', {
      confirmButtonText: '确认提交',
      cancelButtonText: '再检查一下',
      type: 'warning',
    })
  } catch (error) {
    return
  }

  submitting.value = true
  try {
    const task = createPendingTask({
      type: 'submit-report',
      payload: {
        levelId: level.value.levelId,
        selectedOptionIds: [...selectedIds.value],
        clientSpendSeconds: Math.max(1, elapsedSeconds.value),
      },
      from: router.currentRoute.value.fullPath,
    })
    await router.push({
      name: 'loading',
      query: { id: task.id },
    })
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.challenge-page {
  display: grid;
  gap: 18px;
}

.challenge-head {
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

.challenge-head h1 {
  margin: 0 0 12px;
  font-size: 28px;
  line-height: 1.3;
}

.head-tools {
  display: grid;
  gap: 10px;
  justify-items: end;
}

.timer {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  padding: 0 12px;
  color: var(--primary-deep);
  background: var(--primary-soft);
  border: 1px solid #bfe5dc;
  border-radius: var(--radius);
  font-weight: 900;
}

.challenge-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 430px;
  gap: 18px;
  align-items: start;
}

.left-stack,
.right-stack {
  display: grid;
  gap: 18px;
}

.requirement-wrap {
  padding: 18px;
}

.answer-box,
.option-pool {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.answer-box {
  border-color: #bfe5dc;
}

.answer-box.is-over,
.option-pool.is-over {
  outline: 3px solid rgba(15, 118, 110, 0.18);
}

.answer-header,
.pool-header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 16px;
  border-bottom: 1px solid var(--line);
}

.answer-header h2,
.pool-header h2 {
  margin: 0;
  font-size: 18px;
}

.answer-header p,
.pool-header p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.5;
}

.pool-header .el-input {
  width: 150px;
}

.answer-empty {
  margin: 16px;
  padding: 24px;
  color: var(--muted);
  text-align: center;
  background: var(--panel-soft);
  border: 1px dashed #b9cbbd;
  border-radius: var(--radius);
}

.selected-list,
.option-list {
  display: grid;
  gap: 10px;
  padding: 16px;
}

.option-list {
  max-height: 520px;
  overflow: auto;
}

.option-card {
  display: grid;
  grid-template-columns: 30px 1fr auto;
  gap: 10px;
  align-items: center;
  width: 100%;
  min-height: 54px;
  padding: 10px;
  color: var(--text);
  text-align: left;
  background: var(--panel-soft);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  cursor: grab;
}

.option-card:hover,
.option-card:focus-visible {
  border-color: var(--primary);
  outline: 0;
}

.option-card strong {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  color: var(--primary-deep);
  background: var(--primary-soft);
  border-radius: 6px;
}

.option-card span {
  color: #314038;
  line-height: 1.55;
}

.option-card .el-icon {
  color: var(--primary);
}

.option-card.selected {
  background: #f4fffc;
  border-color: #bfe5dc;
}

.option-card.selected .el-icon {
  color: var(--danger);
}

.answer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 16px 16px;
}

@media (max-width: 980px) {
  .challenge-head,
  .challenge-grid {
    grid-template-columns: 1fr;
  }

  .head-tools {
    justify-items: start;
  }

  .pool-header {
    flex-direction: column;
  }

  .pool-header .el-input {
    width: 100%;
  }
}
</style>
