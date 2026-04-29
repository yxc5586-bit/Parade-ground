import { computed, reactive } from 'vue'
import { userApi } from '../api/user'

const state = reactive({
  user: null,
  loading: false,
  checked: false,
})

function resolveRank(salary) {
  const value = Number(salary || 0)
  if (value < 8000) return '试炼新兵'
  if (value < 12000) return '初阶校尉'
  if (value < 18000) return '破阵先锋'
  if (value < 25000) return '中阶统领'
  if (value < 35000) return '高级主将'
  if (value < 50000) return '架构军师'
  return '系统帅才'
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
