// 关卡API — AI生成关卡、获取关卡详情/当前关卡、精选关卡分页、管理员CRUD
import http from './http'

export const levelApi = {
  generate(data = {}) {
    return http.post('/level/generate', data)
  },
  detail(levelId) {
    return http.get('/level/detail', {
      params: { levelId },
    })
  },
  current(config = {}) {
    return http.get('/level/current', config)
  },
  featuredPage(params = {}) {
    return http.get('/level/featured/page', { params })
  },

  // Admin
  adminList(params = {}) {
    return http.get('/level/admin/list', { params })
  },
  adminUpdate(data) {
    return http.put('/level/admin/update', data)
  },
  adminDelete(id) {
    return http.delete(`/level/admin/${id}`)
  },
  adminSetPriority(id, priority) {
    return http.put('/level/admin/priority', null, {
      params: { id, priority },
    })
  },
}
