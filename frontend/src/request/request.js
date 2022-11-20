// 拦截器文件

import axios from 'axios';

// 创建实例
const instance = axios.create({
    baseURL:'http://localhost/api',
    // 请求超时
    timeout:800000
});

// 拦截器
// 请求拦截
instance.interceptors.request.use(config=>{
    // 部分接口需要token
    var token = sessionStorage.getItem('token');
    if(token){
        config.headers = {
            'Authorization':"Bearer " + token,
        };
    }
    return config;
},err=>{
    return Promise.reject(err);
});



//相应拦截
instance.interceptors.response.use(res=>{
    return res;
},err=>{
    return Promise.reject(err);
});

// 整体导出
export default instance;