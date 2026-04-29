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
      <div class="solution-markdown" v-html="renderedSolution"></div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Briefcase, DocumentChecked, Reading } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import { formatSalary, formatSignedMoney, normalizeArray } from '../utils/format'

const markdown = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
})

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
const renderedSolution = computed(() => markdown.render(report.value.detailedSolution || '暂无标准解法。'))
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

.solution-markdown {
  margin: 0;
  color: var(--text);
  line-height: 1.9;
  overflow-wrap: anywhere;
}

.solution-markdown :deep(*) {
  max-width: 100%;
}

.solution-markdown :deep(p),
.solution-markdown :deep(ul),
.solution-markdown :deep(ol),
.solution-markdown :deep(blockquote),
.solution-markdown :deep(pre) {
  margin: 0 0 12px;
}

.solution-markdown :deep(:last-child) {
  margin-bottom: 0;
}

.solution-markdown :deep(h1),
.solution-markdown :deep(h2),
.solution-markdown :deep(h3),
.solution-markdown :deep(h4) {
  margin: 16px 0 10px;
  color: #f3c870;
  line-height: 1.35;
}

.solution-markdown :deep(h1) {
  font-size: 22px;
}

.solution-markdown :deep(h2) {
  font-size: 20px;
}

.solution-markdown :deep(h3) {
  font-size: 18px;
}

.solution-markdown :deep(h4) {
  font-size: 16px;
}

.solution-markdown :deep(ul),
.solution-markdown :deep(ol) {
  padding-left: 22px;
}

.solution-markdown :deep(li + li) {
  margin-top: 6px;
}

.solution-markdown :deep(a) {
  color: var(--amber);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.solution-markdown :deep(code) {
  padding: 2px 6px;
  color: #ffe2a0;
  background: rgba(31, 22, 15, 0.5);
  border: 1px solid var(--line);
  border-radius: 5px;
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
  font-size: 0.92em;
}

.solution-markdown :deep(pre) {
  padding: 14px;
  background: rgba(31, 22, 15, 0.62);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow-x: auto;
}

.solution-markdown :deep(pre code) {
  padding: 0;
  background: transparent;
  border: 0;
}

.solution-markdown :deep(blockquote) {
  padding: 2px 0 2px 12px;
  color: var(--muted);
  border-left: 3px solid var(--primary);
}

.solution-markdown :deep(table) {
  width: 100%;
  border-collapse: collapse;
  overflow: hidden;
}

.solution-markdown :deep(th),
.solution-markdown :deep(td) {
  padding: 8px 10px;
  border: 1px solid var(--line);
  text-align: left;
}

.solution-markdown :deep(th) {
  color: #e9c275;
  background: rgba(31, 22, 15, 0.42);
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
