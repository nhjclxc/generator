
// 后端服务主机
const serviceHost = 'localhost'

// 后端服务端口
const servicePort = 8099

window.GENERATOR_CONFIG = {
  baseURL: `http://${serviceHost}:${servicePort}`,
  // baseURL: `http://localhost:8099`,
}

console.log('配置文件执行完毕！！！');