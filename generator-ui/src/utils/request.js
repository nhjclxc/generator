import axios from 'axios'
import { Message, Loading } from 'element-ui'

import {tansParams, blobValidate} from './common.js'
import errorCode from './errorCode'

let downloadLoadingInstance;

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

const Authorization = 'Authorization'


// 创建axios实例
const service = axios.create({
  // axios中请求配置有baseURL选项，表示请求URL公共部分
  baseURL: window.GENERATOR_CONFIG.baseURL,
  // 超时时间
  timeout: 10000
})


// request拦截器
service.interceptors.request.use(config => {
  
  // get请求映射params参数
  if (config.method === 'get' && config.params) {
    let url = config.url + '?' + tansParams(config.params);
    url = url.slice(0, -1);
    config.params = {};
    config.url = url;
  }
  // 添加会话sessionUuid
  const flag = isConnect(config.url)

  // sessionStorage.setItem('msg','hello!!!')  连接接口响应
  // const result = sessionStorage.getItem('person')   除连接接口外的其他接口
  // sessionStorage.removeItem('msg2')   组件销毁

  // 所有请求添加Authorization头，connect添加是为了，删除上一个连接
  const sessionUuid = sessionStorage.getItem(Authorization)
  config.headers['Authorization'] = sessionUuid 


  return config
}, error => {
    console.log(error)
    Promise.reject(error)
})


// 响应拦截器
service.interceptors.response.use(res => {
    // 未设置状态码则默认成功状态
    const code = res.data.code || 200;
    // 获取错误信息
    const msg = errorCode[code] || res.data.msg || errorCode['default']


    if (code < 200 || code > 300){
      // console.log('接口异常');
      Message({ message: msg, type: 'error' })
      return Promise.reject(new Error(msg))
    }
    const flag = isConnect(res.config.url)
    // flag=true向sessionStorage里面添加添加Authorization
    if (flag){
      let sessionUuid = res.headers['authorization']
      sessionStorage.setItem(Authorization, sessionUuid)
      // console.log('sessionStorage.get', sessionStorage.getItem(Authorization));
      Message({ message: msg, type: 'success' })
    }

    // 二进制数据则直接返回
    if (res.headers.getContentType().includes("stream") || res.request.responseType ===  'blob' || res.request.responseType ===  'arraybuffer') {
      return res
    }
    return res.data.data
  },
  error => {
    console.log('err' + error)
    let { message } = error;
    if (message == "Network Error") {
      message = "后端接口连接异常";
    } else if (message.includes("timeout")) {
      message = "系统接口请求超时";
    } else if (message.includes("Request failed with status code")) {
      message = "系统接口" + message.substr(message.length - 3) + "异常";
    }
    Message({ message: message, type: 'error', duration: 5 * 1000 })
    return Promise.reject(error)
  }
)

/**
 * 判断是不是连接数据库的请求
 * @param {} url 请求地址
 * @returns true是，否则不是
 */
function isConnect(url){
  const regex = /^\/gen\/connect/;
  const match = url.match(regex);

  if (match && match.length > 0) {
      // const extracted = match[0];
      // console.log(extracted); 
      return true;
  }
  return false
}

// 通用下载方法
export function download(url, params, filename, config) {
  downloadLoadingInstance = Loading.service({ text: "正在下载数据，请稍候", spinner: "el-icon-loading", background: "rgba(0, 0, 0, 0.7)", })
  return service.post(url, params, {
    transformRequest: [(params) => { return tansParams(params) }],
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    responseType: 'blob',
    ...config
  }).then(async (data) => {
    const isBlob = blobValidate(data);
    if (isBlob) {
      const blob = new Blob([data])
      saveAs(blob, filename)
    } else {
      const resText = await data.text();
      const rspObj = JSON.parse(resText);
      const errMsg = errorCode[rspObj.code] || rspObj.msg || errorCode['default']
      Message.error(errMsg);
    }
    downloadLoadingInstance.close();
  }).catch((r) => {
    console.error(r)
    Message.error('下载文件出现错误，请联系管理员！')
    downloadLoadingInstance.close();
  })
}

export default service
