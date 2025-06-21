import { createApp } from 'vue'
//import './style.css'
import './scss/style.scss'
import App from './App.vue'

// JS: gives you access to things like Modal, Tooltip, etc.
import * as bootstrap from 'bootstrap'

const app = createApp(App)
app.config.globalProperties.$bootstrap = bootstrap
app.mount('#app')