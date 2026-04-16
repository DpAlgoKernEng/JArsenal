import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './styles/global.scss'
import App from './App.vue'
import router from './router'
import { setupPermissionDirectives } from './directives/permission'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 注册权限指令
setupPermissionDirectives(app)

app.mount('#app')