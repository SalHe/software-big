import Vue from 'vue'
import App from './App.vue'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import router from './router/index.js'
import VueRouter from 'vue-router';
const echarts = require('echarts');

Vue.prototype.$echarts = echarts

Vue.config.productionTip = false

//引入UI
Vue.use(ElementUI);

//引入路由
Vue.use(VueRouter);


new Vue({
  render: h => h(App),
  router,
}).$mount('#app')
