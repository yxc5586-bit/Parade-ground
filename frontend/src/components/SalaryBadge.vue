<!-- 薪资徽章组件 — 在答题页顶部展示当前月薪，支持自定义label -->
<template>
  <div class="salary-badge">
    <span>{{ label }}</span>
    <strong>{{ formatSalary(value) }}</strong>
  </div>
</template>

<script setup>
import { formatSalary } from '../utils/format'

defineProps({
  label: {
    type: String,
    default: '当前月薪',
  },
  value: {
    type: Number,
    default: 0,
  },
})
</script>

<style scoped>
.salary-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 36px;
  padding: 0 12px;
  background: var(--amber-soft);
  border: 1px solid var(--line-bright);
  border-radius: var(--radius);
}

.salary-badge span {
  color: var(--muted);
  font-size: 12px;
  font-weight: 800;
}

.salary-badge strong {
  color: var(--amber);
  font-size: 15px;
}
</style>
