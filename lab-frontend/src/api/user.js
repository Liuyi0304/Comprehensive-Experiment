import request from '@/utils/request'

const BASE_URL = '/user'

// 1. 获取列表
export function getUserList(keyword) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params: { keyword }
  })
}

// 2. 新增
export function addUser(data) {
  return request({
    url: `${BASE_URL}/add`,
    method: 'post',
    data
  })
}

// 3. 修改
export function updateUser(data) {
  return request({
    url: `${BASE_URL}/update`,
    method: 'put',
    data
  })
}

// 4. 删除
export function deleteUser(id) {
  return request({
    url: `${BASE_URL}/delete/${id}`,
    method: 'delete'
  })
}
export function getUserOptions(keyword) {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get',
    params: { keyword }
  })
}
// 5. 重置密码
export function resetPassword(id) {
  return request({
    url: `${BASE_URL}/reset-pwd/${id}`,
    method: 'post'
  })
}