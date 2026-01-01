import request from '@/utils/request'

// 基础路径对应 Controller 的 @RequestMapping("/api/category")
// request.js 里 baseURL 已经是 /api 了，所以这里写 /category
const BASE_URL = '/category'

// 1. 获取列表
export function getCategoryList() {
  return request({
    url: `${BASE_URL}/list`,
    method: 'get'
  })
}

// 2. 新增
export function addCategory(data) {
  return request({
    url: `${BASE_URL}/add`,
    method: 'post',
    data
  })
}

// 3. 修改
export function updateCategory(data) {
  return request({
    url: `${BASE_URL}/update`,
    method: 'put',
    data
  })
}

// 4. 删除
export function deleteCategory(id) {
  return request({
    url: `${BASE_URL}/delete/${id}`,
    method: 'delete'
  })
}