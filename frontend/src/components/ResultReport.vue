<template>
  <div class="report">
    <section class="report-top">
      <el-progress
        type="dashboard"
        :width="132"
        :stroke-width="10"
        :percentage="score"
        :color="progressColor"
      >
        <template #default>
          <strong class="score">{{ score }}</strong>
          <span>分</span>
        </template>
      </el-progress>
      <div class="report-copy">
        <h2>{{ report.evaluationTitle || '评审结果已生成' }}</h2>
        <p>{{ report.evaluationText || '本次结果暂无详细评价。' }}</p>
        <div class="report-money">
          <span :class="salaryChangeClass">{{ formatSignedMoney(result.salaryChange) }}</span>
          <strong>{{ formatSalary(result.updatedSalary) }}</strong>
        </div>
      </div>
    </section>

    <section v-if="jobSuggestions.length" class="report-block">
      <h3>
        <el-icon><Briefcase /></el-icon>
        投递建议
      </h3>
      <div class="job-grid">
        <div v-for="item in jobSuggestions" :key="item.companyAlias" class="job-card">
          <strong>{{ item.companyAlias }}</strong>
          <span>{{ item.salaryRange }}</span>
          <p>{{ item.fitReason }}</p>
        </div>
      </div>
    </section>

    <section class="analysis-grid">
      <div class="analysis-card">
        <h3>命中点</h3>
        <p v-if="!correctChoices.length" class="muted">暂无命中分析</p>
        <ul>
          <li v-for="item in correctChoices" :key="item">{{ item }}</li>
        </ul>
      </div>
      <div class="analysis-card">
        <h3>误选点</h3>
        <p v-if="!wrongChoices.length" class="muted">没有明显误选</p>
        <ul>
          <li v-for="item in wrongChoices" :key="item">{{ item }}</li>
        </ul>
      </div>
      <div class="analysis-card">
        <h3>遗漏点</h3>
        <p v-if="!missedChoices.length" class="muted">没有明显遗漏</p>
        <ul>
          <li v-for="item in missedChoices" :key="item">{{ item }}</li>
        </ul>
      </div>
    </section>

    <section class="report-block">
      <h3>
        <el-icon><DocumentChecked /></el-icon>
        标准答案
      </h3>
      <div class="tag-list">
        <el-tag v-for="answer in standardAnswers" :key="answer" type="success" effect="plain">
          {{ answer }}
        </el-tag>
      </div>
    </section>

    <section class="report-block">
      <h3>
        <el-icon><Reading /></el-icon>
        标准解法
      </h3>
      <p class="solution">{{ report.detailedSolution || '暂无标准解法。' }}</p>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Briefcase, DocumentChecked, Reading } from '@element-plus/icons-vue'
import { formatSalary, formatSignedMoney, normalizeArray } from '../utils/format'

const props = defineProps({
  result: {
    type: Object,
    default: () => ({}),
  },
})

const report = computed(() => props.result.resultReport || {})
const score = computed(() => Math.max(0, Math.min(100, Number(props.result.score || 0))))
const standardAnswers = computed(() => normalizeArray(props.result.standardAnswers))
const jobSuggestions = computed(() => normalizeArray(report.value.jobSuggestions))
const reasonAnalysis = computed(() => report.value.reasonAnalysis || {})
const correctChoices = computed(() => normalizeArray(reasonAnalysis.value.correctChoices))
const wrongChoices = computed(() => normalizeArray(reasonAnalysis.value.wrongChoices))
const missedChoices = computed(() => normalizeArray(reasonAnalysis.value.missedChoices))
const salaryChangeClass = computed(() => (Number(props.result.salaryChange || 0) >= 0 ? 'salary-up' : 'salary-down'))
const progressColor = computed(() => {
  if (score.value >= 85) return '#d99a3d'
  if (score.value >= 60) return '#c18a3a'
  return '#d97145'
})
</script>

<style scoped>
.report {
  display: grid;
  gap: 16px;
}

.report-top {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 20px;
  align-items: center;
  padding: 22px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.score {
  display: block;
  font-size: 28px;
  line-height: 1;
}

.report-copy h2 {
  margin: 0;
  font-size: 24px;
  line-height: 1.3;
}

.report-copy p {
  margin: 10px 0 0;
  color: var(--text);
  line-height: 1.75;
}

.report-money {
  display: flex;
  align-items: baseline;
  gap: 14px;
  margin-top: 14px;
}

.report-money span {
  font-size: 24px;
  font-weight: 900;
}

.report-money strong {
  font-size: 18px;
}

.report-block,
.analysis-card {
  padding: 18px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.report-block h3,
.analysis-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 16px;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.job-card {
  padding: 14px;
  background: var(--panel-soft);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.job-card strong,
.job-card span {
  display: block;
}

.job-card span {
  margin-top: 5px;
  color: var(--amber);
  font-size: 13px;
  font-weight: 800;
}

.job-card p {
  margin: 10px 0 0;
  color: var(--text);
  font-size: 13px;
  line-height: 1.6;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

ul {
  margin: 0;
  padding-left: 18px;
  color: var(--text);
  line-height: 1.7;
}

li + li {
  margin-top: 6px;
}

.solution {
  margin: 0;
  color: var(--text);
  line-height: 1.9;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .report-top,
  .job-grid,
  .analysis-grid {
    grid-template-columns: 1fr;
  }

  .report-top {
    justify-items: center;
    text-align: center;
  }

  .report-money {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
