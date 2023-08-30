
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 引入element plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 引入cookies
import VueCookies from 'vue-cookies'

const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
app.use(ElementPlus)
app.use(router)

app.mount('#app')
