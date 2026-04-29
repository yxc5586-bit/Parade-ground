<template>
  <div class="requirement">
    <article v-if="requirement.background">
      <h3>业务背景</h3>
      <p>{{ requirement.background }}</p>
    </article>
    <article v-if="requirement.target">
      <h3>建设目标</h3>
      <p>{{ requirement.target }}</p>
    </article>
    <article v-if="roles.length">
      <h3>参与角色</h3>
      <div class="tag-list">
        <el-tag v-for="role in roles" :key="role" type="info">{{ role }}</el-tag>
      </div>
    </article>
    <article v-if="rules.length">
      <h3>核心规则</h3>
      <ul>
        <li v-for="rule in rules" :key="rule">{{ rule }}</li>
      </ul>
    </article>
    <article v-if="nonFunctionalRequirements.length">
      <h3>非功能要求</h3>
      <ul>
        <li v-for="item in nonFunctionalRequirements" :key="item">{{ item }}</li>
      </ul>
    </article>
    <article v-for="item in extraEntries" :key="item.key">
      <h3>{{ item.label }}</h3>
      <p v-if="typeof item.value === 'string'">{{ item.value }}</p>
      <ul v-else-if="Array.isArray(item.value)">
        <li v-for="entry in item.value" :key="entry">{{ entry }}</li>
      </ul>
      <pre v-else>{{ JSON.stringify(item.value, null, 2) }}</pre>
    </article>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { normalizeArray } from '../utils/format'

const props = defineProps({
  requirement: {
    type: Object,
    default: () => ({}),
  },
})

const fieldLabels = {
  background: '业务背景',
  target: '建设目标',
  roles: '参与角色',
  rules: '核心规则',
  nonFunctionalRequirements: '非功能要求',
  performanceRequirements: '性能要求',
  stabilityRequirements: '稳定性要求',
  securityRequirements: '安全要求',
  exceptionHandling: '异常处理',
  launchRequirements: '上线要求',
}

const roles = computed(() => normalizeArray(props.requirement.roles))
const rules = computed(() => normalizeArray(props.requirement.rules))
const nonFunctionalRequirements = computed(() => normalizeArray(props.requirement.nonFunctionalRequirements))
const extraEntries = computed(() => {
  const hidden = new Set(['background', 'target', 'roles', 'rules', 'nonFunctionalRequirements'])
  return Object.entries(props.requirement || {})
    .filter(([key, value]) => !hidden.has(key) && value != null && value !== '')
    .map(([key, value]) => ({
      key,
      value,
      label: fieldLabels[key] || key,
    }))
})
</script>

<style scoped>
.requirement {
  display: grid;
  gap: 14px;
}

article {
  padding: 16px;
  background: var(--panel-soft);
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

h3 {
  margin: 0 0 9px;
  font-size: 14px;
}

p,
ul {
  margin: 0;
  color: var(--text);
  font-size: 14px;
  line-height: 1.75;
}

ul {
  padding-left: 18px;
}

li + li {
  margin-top: 5px;
}

pre {
  margin: 0;
  overflow-x: auto;
  color: var(--text);
  white-space: pre-wrap;
}
</style>
