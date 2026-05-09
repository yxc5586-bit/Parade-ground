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
