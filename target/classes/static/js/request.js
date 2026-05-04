// 创建 Axios 实例
//openclaw发力
const request = axios.create({
// ✨ 重点：这里的 baseURL 必须包含你的项目前缀 /music
    baseURL: '/music/api',
    timeout: 5000
});

// 请求拦截器
request.interceptors.request.use(config => {
// 自动带上 Token，这样你每个页面就不用手动写了
    const token = localStorage.getItem('token');
    if (token) {
        config.headers['token'] = token;
    }
    return config;
}, error => Promise.reject(error));

// 响应拦截器
request.interceptors.response.use(response => {
    const res = response.data;
// 统一处理后端返回的 Result 对象
    if (res.code !== 200) {
        alert(res.msg || '操作失败');
        return Promise.reject(new Error(res.msg || 'Error'));
    }
    return res.data; // 这样你在页面里拿到的直接就是业务数据
}, error => {
    return Promise.reject(error);
});