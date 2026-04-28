import { computed, reactive } from 'vue'
import { userApi } from '../api/user'

const state = reactive({
  user: null,
  loading: false,
  checked: false,
})

function resolveRank(salary) {
  const value = Number(salary || 0)
  if (value < 8000) return '实习试炼生'
  if (value < 12000) return '初级打工人'
  if (value < 18000) return '业务熟练工'
  if (value < 25000) return '中级开发战士'
  if (value < 35000) return '高级开发统领'
  if (value < 50000) return '架构冲锋队长'
  return '系统设计大魔王'
}

async function fetchCurrentUser(options = {}) {
  state.loading = true
  try {
    const user = await userApi.me({ silent: options.silent })
    state.user = user
    return user
  } catch (error) {
    state.user = null
    return null
  } finally {
    state.loading = false
    state.checked = true
  }
}

function setUser(user) {
  state.user = user
  state.checked = true
}

function clearUser() {
  state.user = null
  state.checked = true
}

export function useUser() {
  const rank = computed(() => resolveRank(state.user?.currentSalary))

  return {
    state,
    rank,
    fetchCurrentUser,
    setUser,
    clearUser,
    resolveRank,
  }
}
