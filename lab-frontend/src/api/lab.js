import request from '@/utils/request'

// 1. 获取实验室列表
// 对应后端: @GetMapping("/list")
export function getLabList(params) {
  return request({
    url: '/lab/list',
    method: 'get',     // 查询必须是 get
    params
  })
}

// 2. 新增实验室
// 对应后端: @PostMapping("/add")
export function addLab(data) {
  return request({
    url: '/lab/add',
    method: 'post',    // 新增通常是 post，这个你必须要保留对
    data
  })
}

// 3. 修改实验室
// 对应后端: @PutMapping("/update")
// 🔴 修复点：原本是 'post'，必须改为 'put'
export function updateLab(data) {
  return request({
    url: '/lab/update',
    method: 'put',     // <--- 这里的 post 改成 put
    data
  })
}

// 4. 删除实验室
// 对应后端: @DeleteMapping("/delete/{id}")
// 🔴 修复点：原本是 'post'，必须改为 'delete'
export function deleteLab(id) {
  return request({
    url: `/lab/delete/${id}`, // 注意：使用反引号 ` 拼接 ID
    method: 'delete'   // <--- 这里的 post 改成 delete
  })
}