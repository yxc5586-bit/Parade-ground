<template>
  <div class="shell">
    <header class="topbar">
      <router-link class="brand" to="/">
        <span class="brand-mark">PG</span>
        <span>
          <strong>程序员技术练兵场</strong>
          <small>方案评审作战台</small>
        </span>
      </router-link>

      <nav class="nav">
        <router-link to="/">
          <el-icon><HomeFilled /></el-icon>
          大厅
        </router-link>
        <router-link to="/records">
          <el-icon><Tickets /></el-icon>
          战绩
        </router-link>
      </nav>

      <div class="user-box" v-if="state.user">
        <div class="user-meta">
          <strong>{{ state.user.userName || state.user.userAccount }}</strong>
          <span>{{ rank }} · {{ formatSalary(state.user.currentSalary) }}</span>
        </div>
        <el-button :icon="SwitchButton" plain @click="handleLogout">退出</el-button>
      </div>
    </header>

    <main>
      <slot />
    </main>
  </div>
</template>

<script setup>
import { HomeFilled, SwitchButton, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { userApi } from '../api/user'
import { useUser } from '../composables/useUser'
import { formatSalary } from '../utils/format'

const router = useRouter()
const { state, rank, clearUser } = useUser()

async function handleLogout() {
  await userApi.logout()
  clearUser()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.shell {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto minmax(260px, 1fr);
  align-items: center;
  gap: 18px;
  padding: 12px 28px;
  background: rgba(245, 247, 244, 0.9);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(16px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.brand-mark {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: #fff;
  font-size: 13px;
  font-weight: 900;
  background: var(--primary);
  border-radius: var(--radius);
}

.brand strong,
.brand small {
  display: block;
}

.brand strong {
  font-size: 16px;
  line-height: 1.2;
}

.brand small {
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.nav {
  display: inline-flex;
  gap: 6px;
  padding: 4px;
  background: #edf3ed;
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.nav a {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 34px;
  padding: 0 12px;
  color: var(--muted);
  border-radius: 6px;
  font-size: 14px;
  font-weight: 700;
}

.nav a.router-link-active {
  color: var(--text);
  background: #fff;
  box-shadow: 0 4px 12px rgba(20, 28, 24, 0.08);
}

.user-box {
  display: flex;
  justify-self: end;
  align-items: center;
  gap: 12px;
}

.user-meta {
  text-align: right;
}

.user-meta strong,
.user-meta span {
  display: block;
}

.user-meta strong {
  font-size: 14px;
}

.user-meta span {
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 900px) {
  .topbar {
    position: static;
    grid-template-columns: 1fr;
    padding: 12px;
  }

  .nav {
    width: 100%;
  }

  .nav a {
    flex: 1;
    justify-content: center;
  }

  .user-box {
    justify-self: stretch;
    justify-content: space-between;
  }

  .user-meta {
    text-align: left;
  }
}
</style>
