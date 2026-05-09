// 用户API — 注册、登录、注销、获取当前登录用户信息
import http from './http'

export const userApi = {
  register(data) {
    return http.post('/user/register', data)
  },
  login(data) {
    return http.post('/user/login', data)
  },
  logout() {
    return http.post('/user/logout')
  },
  me(config = {}) {
    return http.get('/user/me', config)
  },
}
