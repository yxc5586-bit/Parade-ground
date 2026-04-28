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
}
