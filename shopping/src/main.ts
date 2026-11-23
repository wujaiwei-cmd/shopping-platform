import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { installDemoBackend } from './api/demoBackend'

// 线上（GitHub Pages）没有 Java 后端，用浏览器内的演示后端接管所有 /api 请求。
// 详见 src/api/demoBackend.ts；本机要连真实后端时，访问 ?demo=off 即可关闭。
installDemoBackend()

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
