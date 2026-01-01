import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login', // AuthController
    method: 'post',
    data // { username, password }
  })
}