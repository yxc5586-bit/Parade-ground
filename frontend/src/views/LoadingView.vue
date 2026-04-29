<template>
  <div class="page loading-page">
    <section class="loading-panel">
      <div class="signal" :class="{ 'is-error': hasProblem }">
        <span />
        <span />
        <span />
      </div>

      <div class="loading-copy">
        <p class="eyebrow">{{ currentCopy.eyebrow }}</p>
        <h1>{{ currentCopy.title }}</h1>
        <p>{{ currentCopy.description }}</p>
      </div>

      <div v-if="!hasProblem" class="progress-track" aria-hidden="true">
        <span />
      </div>

      <div v-if="hasProblem" class="error-actions">
        <el-alert :title="errorMessage" type="error" show-icon :closable="false" />
        <div class="toolbar">
          <el-button plain @click="goBack">返回上一页</el-button>
          <el-button type="primary" @click="router.replace('/')">回营门</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { gameApi } from '../api/game'
import { levelApi } from '../api/level'
import { useUser } from '../composables/useUser'
import { consumePendingTask } from '../utils/pendingTask'

const copyMap = {
  'generate-level': {
    eyebrow: '战役生成中',
    title: '正在推演下一场训练',
    description: '系统正在按你的当前薪资和方向生成企业场景题，马上进入演武。',
  },
  'submit-report': {
    eyebrow: 'AI 评审中',
    title: '正在评审答案并生成结算报告',
    description: '评委正在核对布阵、生成复盘和薪资调整，请稍等片刻。',
  },
}

const fallbackCopy = {
  eyebrow: '任务处理中',
  title: '正在准备结果',
  description: '请求已经进入处理队列，请稍等片刻。',
}

const route = useRoute()
const router = useRouter()
const { fetchCurrentUser } = useUser()

const task = ref(null)
const running = ref(true)
const errorMessage = ref('')

const currentCopy = computed(() => copyMap[task.value?.type] || fallbackCopy)
const hasProblem = computed(() => !running.value && Boolean(errorMessage.value))

onMounted(runPendingTask)

async function runPendingTask() {
  task.value = consumePendingTask(route.query.id)

  if (!task.value) {
    fail('任务已失效，请从大厅重新开始。')
    return
  }

  try {
    if (task.value.type === 'generate-level') {
      await generateLevel()
      return
    }

    if (task.value.type === 'submit-report') {
      await submitReport()
      return
    }

    fail('未知任务类型，请返回后重试。')
  } catch (error) {
    fail(error.message || '处理失败，请稍后重试。')
  }
}

async function generateLevel() {
  const data = await levelApi.generate(task.value.payload || {})
  await router.replace(`/challenge/${data.levelId}`)
}

async function submitReport() {
  const payload = task.value.payload || {}
  const result = await gameApi.submit({
    levelId: payload.levelId,
    selectedOptionIds: payload.selectedOptionIds || [],
    clientSpendSeconds: payload.clientSpendSeconds,
  })
  await fetchCurrentUser({ silent: true })
  ElMessage.success('评审完成，结算报告已生成')
  await router.replace(`/result/${result.recordId}`)
}

function fail(message) {
  running.value = false
  errorMessage.value = message
}

function goBack() {
  const from = task.value?.from
  if (from && from !== route.fullPath) {
    router.replace(from)
    return
  }
  router.replace('/')
}
</script>

<style scoped>
.loading-page {
  display: grid;
  min-height: calc(100vh - 92px);
  place-items: center;
}

.loading-panel {
  display: grid;
  width: min(680px, 100%);
  gap: 22px;
  padding: 34px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.signal {
  display: inline-grid;
  grid-template-columns: repeat(3, 18px);
  gap: 9px;
  width: max-content;
}

.signal span {
  display: block;
  width: 18px;
  height: 18px;
  background: var(--primary);
  border-radius: 5px;
  animation: pulse 1.05s ease-in-out infinite;
}

.signal span:nth-child(2) {
  animation-delay: 0.12s;
}

.signal span:nth-child(3) {
  animation-delay: 0.24s;
}

.signal.is-error span {
  background: var(--danger);
  animation: none;
}

.loading-copy {
  display: grid;
  gap: 10px;
}

.eyebrow {
  margin: 0;
  color: var(--primary);
  font-size: 13px;
  font-weight: 900;
}

.loading-copy h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.25;
}

.loading-copy p:last-child {
  max-width: 560px;
  margin: 0;
  color: var(--muted);
  line-height: 1.75;
}

.progress-track {
  position: relative;
  height: 10px;
  overflow: hidden;
  background: var(--primary-soft);
  border-radius: 999px;
}

.progress-track span {
  position: absolute;
  inset: 0 auto 0 0;
  width: 38%;
  background: var(--primary);
  border-radius: inherit;
  animation: travel 1.35s ease-in-out infinite;
}

.error-actions {
  display: grid;
  gap: 16px;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.42;
    transform: translateY(0);
  }

  50% {
    opacity: 1;
    transform: translateY(-5px);
  }
}

@keyframes travel {
  0% {
    transform: translateX(-110%);
  }

  100% {
    transform: translateX(270%);
  }
}

@media (max-width: 900px) {
  .loading-page {
    min-height: calc(100vh - 180px);
    align-items: start;
  }

  .loading-panel {
    padding: 22px;
  }

  .loading-copy h1 {
    font-size: 24px;
  }
}
</style>
