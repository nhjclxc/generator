const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  // 用于放置生成的静态资源 (js、css、img、fonts) 的；（项目打包之后，静态资源会放在这个文件夹下）
  // publicPath: process.env.NODE_ENV === 'dev' ? '/' : '././'
  publicPath: process.env.NODE_ENV === 'development' ? '/' : '././',
  // 关闭eslint校验
  lintOnSave: false,
  devServer: {
    port: 8098, // 前端页面访问端口
  },
})
