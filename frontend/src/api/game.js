// 闯关API — 提交答案、分页查询战绩、查看战绩详情
import http from './http'

export const gameApi = {
  submit(data) {
    return http.post('/game/submit', data)
  },
  recordsPage(params) {
    return http.get('/game/records/page', { params })
  },
  recordDetail(recordId) {
    return http.get('/game/records/detail', {
      params: { recordId },
    })
  },
}
