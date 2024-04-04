import request from '@/utils/request'
import download from '@/utils/download'

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
export function previewTable(tableId) {
  return request({
    url: '/gen/preview/' + tableId,
    method: 'get'
  })
}
