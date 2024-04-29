<template>
  <div class="app-container">
      <!-- 搜索表单相关 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" :inline-message="true" label-width="68px">
      <!-- 数据库相关配置 -->
      <div style="width: 100%;">
        <el-form-item label="jdbcUrl" prop="jdbcUrl"  type="flex" justify="start" >
        <el-input
          v-model="queryParams.jdbcUrl"
          placeholder="数据库地址, 如：jdbc:mysql://127.0.0.1:3306/test，test表示数据库的一个Schema"
          clearable
          @keyup.enter.native="handleQuery"
        style="margin: 0 360px 10px 5px;"
        />
      </el-form-item>
      <el-form-item label="username" prop="username" >
        <el-input
          v-model="queryParams.username"
          placeholder="数据库用户名"
          clearable
          @keyup.enter.native="handleQuery"
          style="width: 120px;"
        />
      </el-form-item>
      <el-form-item label="password" prop="password" >
        <el-input
          v-model="queryParams.password"
          placeholder="数据库密码"
          clearable
          @keyup.enter.native="handleQuery"
          show-password
          style="width: 120px;"
        />
      </el-form-item>
      </div>
      
      <!-- 生成代码相关配置 -->
      <div style="width: 100%;">
        <el-form-item label="package" prop="package">
          <el-input 
            v-model="queryParams.packageName"
            placeholder="包名"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="author" prop="author">
          <el-input
            v-model="queryParams.author"
            placeholder="作者"
            clearable
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item prop="enableSwagger">
          <el-switch
            style="display: block"
            v-model="queryParams.enableSwagger"
            active-color="#13ce66"
            inactive-color="#ff4949"
            inactive-text="enableSwagger"
            @change="changeEnableSwagger">
          </el-switch>
        </el-form-item>
        <el-form-item prop="enableLombok"">
          <el-switch
            style="display: block"
            v-model="queryParams.enableLombok"
            active-color="#13ce66"
            inactive-color="#ff4949"
            inactive-text="enableLombok"
            @change="changeEnableLombok">
          </el-switch>
        </el-form-item>
        <el-form-item prop="enableVue3"">
          <el-switch
            style="display: block"
            v-model="queryParams.enableVue3"
            active-color="#13ce66"
            inactive-color="#ff4949"
            inactive-text="enableVue3"
            @change="changeEnableVue3">
          </el-switch>
        </el-form-item>
      </div>

      <!-- 搜索相关 -->
      <el-form-item label="表名称" prop="tableName">
        <el-input
          v-model="queryParams.tableName"
          placeholder="请输入表名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="表描述" prop="tableComment">
        <el-input
          v-model="queryParams.tableComment"
          placeholder="请输入表描述"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
        <!-- 批量生成代码按钮 -->
        <el-button
          type="primary"
          plain
          icon="el-icon-download"
          size="mini"
          :disabled="multiple"
          @click="handleGenTable"
        >批量生成代码</el-button>

        <el-button type="success" @click="handleConnect">连接数据库</el-button>

      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8" type="flex" justify="end" style="margin-right: 5%;">
      <el-col :span="1.5">
      </el-col>
    </el-row>

    <!-- 数据列表，表格 -->
    <el-table v-loading="loading" :data="tableList" @selection-change="handleSelectionChange"
      style="width: 90%; margin: auto">

      <el-table-column type="selection" align="center" style="width: 5%;"></el-table-column>
      <el-table-column label="序号" type="index" style="width: 5%;" align="center">
        <template slot-scope="scope">
          <span>{{(queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1}}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="表名称"
        align="center"
        prop="tableName"
        :show-overflow-tooltip="true"
        style="width: 25%;"
      />
      <el-table-column
        label="表描述"
        align="center"
        prop="tableComment"
        :show-overflow-tooltip="true"
        style="width: 25%;"
      />
      <el-table-column
        label="实体"
        align="center"
        prop="className"
        :show-overflow-tooltip="true"
        style="width: 25%;"
      />
      <el-table-column label="操作" align="right" class-name="small-padding fixed-width"  style="width: 20%;">
        <template slot-scope="scope">
          <el-button
            type="text"
            size="small"
            icon="el-icon-view"
            @click="handlePreview(scope.row)"
          >预览</el-button>
          <el-button
            type="text"
            size="small"
            icon="el-icon-download"
            @click="handleGenTable(scope.row)"
          >生成代码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页插件 -->
    <el-pagination
        align="right"
        style="margin-right: 5%;"
        :current-page.sync="queryParams.pageNum"
        :page-sizes="[10,20,30,50]"
        :page-size.sync="queryParams.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>

    <!-- 预览界面 -->
    <el-dialog :title="preview.title" :visible.sync="preview.open" width="90%" top="5vh" append-to-body class="scrollbar">
      <el-tabs v-model="preview.activeName">
        <el-tab-pane
          v-for="(value, key) in preview.data"
          :label="key.substring(key.lastIndexOf('/')+1,key.indexOf('.vm'))"
          :name="key.substring(key.lastIndexOf('/')+1,key.indexOf('.vm'))"
          :key="key">
          <el-link :underline="false" icon="el-icon-document-copy" v-clipboard:copy="value" v-clipboard:success="clipboardSuccess" style="float:right">复制</el-link>
          <pre><code class="hljs" v-html="highlightedCode(value, key)"></code></pre>

        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script>
import { connect, closeConnect, listTable, previewTable, genCode  } from "@/utils/generatorAPI";

import { Message } from 'element-ui'

// https://highlightjs.org/
// Using require
const hljs = require('highlight.js/lib/core');
// Load any languages you need
hljs.registerLanguage('javascript', require('highlight.js/lib/languages/javascript'));
hljs.registerLanguage('java', require('highlight.js/lib/languages/java'));
hljs.registerLanguage('xml', require('highlight.js/lib/languages/xml'));
hljs.registerLanguage('html', require('highlight.js/lib/languages/xml'));
hljs.registerLanguage('vue', require('highlight.js/lib/languages/xml'));
hljs.registerLanguage('sql', require('highlight.js/lib/languages/sql'));

export default {
  name: "Generator",
  components: {  },
  data() {
    return {
      // 遮罩层
      loading: false,
      // 选中表数组
      tableNames: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 总条数
      total: 0,
      // 表数据
      tableList: [],
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        tableName: '',
        tables: '',
        tableComment: '',
        author: '',
        packageName: 'com.example',
        jdbcUrl: 'jdbc:mysql://rm-bp1j0xlvvp7f274318o.mysql.rds.aliyuncs.com:3306/smart_construction_dev',  // 后端项目里面配置文件的数据库地址
        username: 'root',
        password: '',
        enableLombok: true,
        enableSwagger: true,
        changeEnableVue3: false,
      },
      // 预览参数
      preview: {
        open: false,
        title: "代码预览",
        data: {},
        activeName: "domain.java"
      }
    };
  },
  created() {
    // this.getList();
  },
  activated() {
    this.getList();
  },
  methods: {
    handleSizeChange(val){
      this.queryParams.pageSize = val
      this.getList();
    },
    handleCurrentChange(val){
      this.queryParams.pageNum = val
      this.getList();
    },
    changeEnableLombok(value){
      this.queryParams.enableLombok = value
    },
    changeEnableSwagger(value){
      this.queryParams.enableSwagger = value
    },
    changeEnableVue3(value){
      this.queryParams.enableVue3 = value
    },
    /** 查询表集合 */
    getList() {
      this.loading = true;
      
      listTable({
        'pageNum': this.queryParams.pageNum,
        'pageSize': this.queryParams.pageSize,
        'tableName': this.queryParams.tableName,
        'tableComment': this.queryParams.tableComment
      }).then(response => {
          this.tableList = response.list;
          this.total = response.total;
          this.loading = false;
        }
      ).catch(err => {
        this.loading = false;
        console.log('查询表集合异常', err.message);
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 生成代码操作 */
    handleGenTable(row) {
      // const tableNames = row.tableName || this.tableNames;
      var tableNames = row.tableName;
      if (typeof tableNames === 'undefined') {
        tableNames = this.tableNames.join(',')
      }

      if (tableNames == "") {
        Message({ message: '请选择要生成的数据', type: 'error' })
        return;
      }

      // 请求后端接口
      genCode({
        'enableLombok': this.queryParams.enableLombok,
        'enableSwagger': this.queryParams.enableSwagger,
        'enableVue3': this.queryParams.enableVue3,
        'author': this.queryParams.author,
        'packageName': this.queryParams.packageName,
        'autoRemovePre': this.queryParams.autoRemovePre,
        'tablePrefix': this.queryParams.tablePrefix,
        'tables': tableNames
      });
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      if (this.$refs["queryForm"]) {
        this.$refs["queryForm"].resetFields();
      }
      this.handleQuery();
    },
    /** 连接数据库 */
    handleConnect() {
      // 请求后端接口
      connect({
        'jdbcUrl': this.queryParams.jdbcUrl,
        'username': this.queryParams.username,
        'password': this.queryParams.password
      }).then(response => {
          this.tableList = response.list;
          this.total = response.total;
          this.loading = false;
        }
      ).catch(err => {
        this.loading = false;
        console.log('handleConnect 查询表集合异常', err.message);
      });
    },
    /** 预览按钮 */
    handlePreview(row) {

      previewTable({
        'enableLombok': this.queryParams.enableLombok,
        'enableSwagger': this.queryParams.enableSwagger,
        'enableVue3': this.queryParams.enableVue3,
        'author': this.queryParams.author,
        'packageName': this.queryParams.packageName,
        'autoRemovePre': this.queryParams.autoRemovePre,
        'tablePrefix': this.queryParams.tablePrefix,
        'tables': row.tableName
      }).then(response => {
        this.preview.data = response;
        this.preview.open = true;
        this.preview.activeName = "domain.java";
      });
    },
    /** 高亮显示 */
    highlightedCode(code, key) {
      const vmName = key.substring(key.lastIndexOf("/") + 1, key.indexOf(".vm"));
      var language = vmName.substring(vmName.indexOf(".") + 1, vmName.length);
      // highlight(code, {language, ignoreIllegals})
      const result = hljs.highlight(code || "", {language });
      return result.value || '&nbsp;';
    },
    /** 复制代码成功 */
    clipboardSuccess() {
      Message({ message: '复制成功', type: 'success' })
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.tableNames = selection.map(item => item.tableName);
      this.single = selection.length != 1;
      this.multiple = !selection.length;
    },
  }
};
</script>

<style scoped>

</style>
