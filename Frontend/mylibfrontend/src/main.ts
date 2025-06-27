import { createApp } from 'vue'
//import './style.css'
import './scss/style.scss'
import App from './App.vue'
import router from './router'
import * as bootstrap from 'bootstrap'
import {loadAppConfig} from "./wrapper/AppConfig.ts";

loadAppConfig().then(() => {
    const app = createApp(App).use(router)
    app.config.globalProperties.$bootstrap = bootstrap
    app.mount('#app')
});