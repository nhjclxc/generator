import Vue from 'vue'
import App from './App.vue'

import clipboard from './utils/clipboard'
Vue.directive('clipboard', clipboard)

import { Pagination, Dialog, Button, Switch, Form, FormItem, Col, Input, Table, 
  TableColumn, Row, Loading, Message, Link, Tabs, TabPane } from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css'; 

Vue.config.productionTip = false

Vue.component(Dialog.name, Dialog);
Vue.component(Button.name, Button);
Vue.component(Switch.name, Switch);
Vue.component(Form.name, Form);
Vue.component(FormItem.name, FormItem);
Vue.component(Col.name, Col);
Vue.component(Input.name, Input);
Vue.component(Table.name, Table);
Vue.component(TableColumn.name, TableColumn);
Vue.component(Row.name, Row);
Vue.component(Message.name, Message);
Vue.component(Link.name, Link);
Vue.component(Tabs.name, Tabs);
Vue.component(TabPane.name, TabPane);

Vue.use(Loading.directive);
Vue.use(Pagination);

new Vue({
  render: h => h(App),
}).$mount('#app')
