<template>
  <div class="page featured-page">
    <section class="featured-panel">
      <div class="panel-head">
        <p class="eyebrow">精选关卡</p>
        <h1>选择战役</h1>
        <p>从精选关卡库中挑选一场战役，直接进入演武，无需等待 AI 生成。</p>
      </div>

      <div v-loading="loading" class="level-grid">
        <button
          v-for="level in levels"
          :key="level.levelId"
          class="level-card"
          type="button"
          @click="selectLevel(level.levelId)"
        >
          <h2>{{ level.levelName }}</h2>
          <div class="card-tags">
            <el-tag type="success" size="small">{{ difficultyLabel(level.difficulty) }}</el-tag>
            <el-tag type="warning" size="small">{{ level.salaryRange }}</el-tag>
            <el-tag v-for="tag in normalizeArray(level.tags)" :key="tag" type="info" size="small">{{ tag }}</el-tag>
          </div>
        </button>

        <div v-if="!loading && !levels.length" class="empty-state">
          暂无穷尽关卡，请先生成或联系管理员添加。
        </div>
      </div>

      <div class="panel-foot">
        <el-pagination
          v-model:current-page="page.current"
          v-model:page-size="page.pageSize"
          :total="page.total"
          :page-sizes="[12, 24, 48]"
          layout="total, prev, pager, next, sizes"
          background
          @current-change="fetchLevels"
          @size-change="fetchLevels"
        />
        <el-button plain @click="router.push('/')">回营门</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { levelApi } from '../api/level'
import { normalizeArray } from '../utils/format'

const difficultyMap = {
  basic: '基础',
  intermediate: '进阶',
  advanced: '高级',
  expert: '专家',
  platform: '平台化',
}

const router = useRouter()
const levels = ref([])
const loading = ref(false)
const page = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
})

onMounted(fetchLevels)

async function fetchLevels() {
  loading.value = true
  try {
    const data = await levelApi.featuredPage({
      current: page.current,
      pageSize: page.pageSize,
    })
    levels.value = data.records || []
    page.total = data.total || 0
  } finally {
    loading.value = false
  }
}

function difficultyLabel(value) {
  return difficultyMap[value] || value || '-'
}

function selectLevel(levelId) {
  router.push(`/challenge/${levelId}`)
}
</script>

<style scoped>
.featured-page {
  display: grid;
  min-height: calc(100vh - 92px);
  place-items: center;
}

.featured-panel {
  display: grid;
  width: min(960px, 100%);
  gap: 22px;
  padding: 34px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.panel-head {
  display: grid;
  gap: 10px;
}

.eyebrow {
  margin: 0;
  color: var(--primary);
  font-size: 13px;
  font-weight: 900;
}

.panel-head h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.25;
}

.panel-head p:last-child {
  max-width: 560px;
  margin: 0;
  color: var(--muted);
  line-height: 1.75;
}

.level-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  min-height: 200px;
}

.level-card {
  display: grid;
  gap: 12px;
  align-content: start;
  width: 100%;
  min-height: 100px;
  padding: 18px;
  color: var(--text);
  text-align: left;
  background: var(--panel-soft);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  cursor: pointer;
  transition: border-color 0.18s, box-shadow 0.18s;
}

.level-card:hover,
.level-card:focus-visible {
  border-color: var(--line-bright);
  box-shadow: var(--glow);
  outline: 0;
}

.level-card h2 {
  margin: 0;
  font-size: 17px;
  line-height: 1.35;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 48px 24px;
  color: var(--muted);
  text-align: center;
  background: var(--panel-soft);
  border: 1px dashed var(--line-bright);
  border-radius: var(--radius);
}

.panel-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .featured-page {
    min-height: calc(100vh - 180px);
    align-items: start;
  }

  .featured-panel {
    padding: 22px;
  }

  .panel-head h1 {
    font-size: 24px;
  }

  .level-grid {
    grid-template-columns: 1fr;
  }
}
</style>
