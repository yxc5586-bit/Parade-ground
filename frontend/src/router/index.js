import { createRouter, createWebHistory } from 'vue-router'
import { useUser } from '../composables/useUser'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
  },
  {
    path: '/challenge/:levelId?',
    name: 'challenge',
    component: () => import('../views/ChallengeView.vue'),
  },
  {
    path: '/result/:recordId',
    name: 'result',
    component: () => import('../views/ResultView.vue'),
  },
  {
    path: '/records',
    name: 'records',
    component: () => import('../views/RecordsView.vue'),
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/AdminView.vue'),
    meta: { admin: true },
  },
  {
    path: '/loading',
    name: 'loading',
    component: () => import('../views/LoadingView.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const { state, fetchCurrentUser } = useUser()

  if (!state.checked) {
    await fetchCurrentUser({ silent: true })
  }

  if (to.meta.public) {
    if (state.user && to.name === 'login') {
      return '/'
    }
    return true
  }

  if (!state.user) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.admin && state.user.userRole !== 'admin') {
    return '/'
  }

  return true
})

export default router
