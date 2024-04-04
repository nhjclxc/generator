<template>
  <div id="app">
    <div id="header" >
      <h1>依据MySQL表结构实现：前端Vue和后端SpingBoot项目的代码生成</h1>
    </div>
    <Generator />
  </div>
</template>

<script>
// 执行配置文件
import _ from '../config'


import Generator from './viwes/generator.vue'

console.log('window.GENERATOR_CONFIG', window.GENERATOR_CONFIG);
console.log('window.GENERATOR_CONFIG.baseURL', window.GENERATOR_CONFIG.baseURL);

export default {
  name: 'App',
  components: {
    Generator,
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#header {
  background-color: rgb(101, 232, 103);
  height: 60px;
  width: 100%;
  top: 0px;
    margin-bottom: 10px;
    display: flex;
    justify-content: left; /* 水平居中对齐 */
    align-items: center; /* 垂直居中对齐 */
}
h1 {
    color: white;
  }
</style>
