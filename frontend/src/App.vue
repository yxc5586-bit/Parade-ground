<!-- 根组件 — 公开页面（如登录）无壳渲染，其余页面包裹在AppShell导航布局中 -->
<template>
  <router-view v-if="route.meta.public" />
  <AppShell v-else>
    <router-view />
  </AppShell>
</template>

<script setup>
import { useRoute } from 'vue-router'
import AppShell from './components/AppShell.vue'

const route = useRoute()
</script>
