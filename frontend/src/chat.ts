import { createApp } from 'vue';
import ChatApp from './ChatApp.vue';
import '@/assets/main.css';
import 'material-symbols/outlined.css';

const app = createApp(ChatApp);

app.mount('#app');
