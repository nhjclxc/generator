import request from '@/utils/request'
import download from '@/utils/download'

// 连接数据库
export function connect(query) {
  return request({
    url: '/gen/connect',
    method: 'get',
    params: query
  })
}

// 关闭数据库连接
export function closeConnect() {
  return request({
    url: '/gen/closeConnect',
    method: 'get'
  })
}

// 查询生成表数据
export function listTable(query) {
  return request({
    url: '/gen/parse',
    method: 'get',
    params: query
  })
}

// 查询生成表数据
export function genCode(query) {
  download.zip(request({
      url: '/gen/genCode',
      method: 'get',
      params: query,
      responseType: 'blob',
    })
  )
}


// 预览生成代码
export function previewTable(query) {
  return request({
    url: '/gen/preview',
    params: query,
    method: 'get'
  })
}
