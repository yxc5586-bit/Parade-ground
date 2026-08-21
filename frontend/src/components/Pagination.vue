<template>
  <div class="pagination-wrap">
    <el-pagination
      :current-page="current"
      :page-size="pageSize"
      :total="total"
      :page-sizes="pageSizes"
      :layout="layout"
      :background="background"
      @update:current-page="$emit('update:current', $event)"
      @update:page-size="$emit('update:pageSize', $event)"
      @current-change="$emit('current-change', $event)"
      @size-change="$emit('size-change', $event)"
    />
  </div>
</template>

<script setup>
defineProps({
  current: {
    type: Number,
    required: true,
  },
  pageSize: {
    type: Number,
    default: 10,
  },
  total: {
    type: Number,
    required: true,
  },
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50],
  },
  layout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper',
  },
  background: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['update:current', 'update:pageSize', 'current-change', 'size-change'])
</script>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
}
</style>
