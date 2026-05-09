<template>
  <div class="shell">
    <header class="topbar">
      <router-link class="brand" to="/">
        <span class="brand-mark">练</span>
        <span>
          <strong>程序员技术练兵场</strong>
          <small>代码演武作战台</small>
        </span>
      </router-link>

      <nav class="nav">
        <router-link to="/">
          <el-icon><HomeFilled /></el-icon>
          营门
        </router-link>
        <router-link to="/records">
          <el-icon><Tickets /></el-icon>
          战绩
        </router-link>
      </nav>

      <div class="user-box" v-if="state.user">
        <div class="user-avatar" :class="avatarClass" :style="avatarStyle">{{ avatarClass.includes('default') ? userInitial : '' }}</div>
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
import { computed } from 'vue'
import { HomeFilled, SwitchButton, Tickets } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { userApi } from '../api/user'
import { useUser } from '../composables/useUser'
import { formatSalary } from '../utils/format'

const router = useRouter()
const { state, rank, clearUser } = useUser()

const userInitial = computed(() => {
  const name = state.user?.userName || state.user?.userAccount || ''
  return name.charAt(0).toUpperCase()
})

const avatarClass = computed(() => {
  const avatar = state.user?.userAvatar
  if (!avatar || /^[0-3]$/.test(avatar)) {
    return `avatar-default avatar-default--${avatar || '0'}`
  }
  return 'avatar-custom'
})

const avatarStyle = computed(() => {
  const avatar = state.user?.userAvatar
  if (avatar && !/^[0-3]$/.test(avatar)) {
    return { backgroundImage: `url(${avatar})` }
  }
  return {}
})

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
  background: rgba(31, 22, 15, 0.84);
  border-bottom: 1px solid var(--line);
  box-shadow: 0 10px 28px rgba(12, 8, 4, 0.26);
  backdrop-filter: blur(16px) saturate(120%);
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
  color: #1f160f;
  font-family: "Songti SC", "STSong", serif;
  font-size: 17px;
  font-weight: 900;
  background: linear-gradient(135deg, var(--amber), var(--primary));
  border: 1px solid rgba(230, 208, 163, 0.38);
  border-radius: 4px;
  box-shadow: 0 0 22px rgba(217, 154, 61, 0.18);
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
  background: rgba(12, 8, 4, 0.28);
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
  color: #1f160f;
  background: linear-gradient(135deg, var(--primary), var(--amber));
  box-shadow: 0 8px 18px rgba(217, 154, 61, 0.18);
}

.user-box {
  display: flex;
  justify-self: end;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  flex-shrink: 0;
  user-select: none;
}

.avatar-default--0 {
  background: linear-gradient(135deg, #d99a3d, #e6d0a3);
}

.avatar-default--1 {
  background: linear-gradient(135deg, #3b82f6, #67e8f9);
}

.avatar-default--2 {
  background: linear-gradient(135deg, #22c55e, #86efac);
}

.avatar-default--3 {
  background: linear-gradient(135deg, #a855f7, #d8b4fe);
}

.avatar-custom {
  background-size: cover;
  background-position: center;
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
