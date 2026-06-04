import { createApp } from 'vue';
import ChatApp from './ChatApp.vue';
import '@/assets/main.css';
import 'material-symbols/outlined.css';
import { setTheme } from './utils/theme.ts';
import { registerChatBridge } from './utils/chat.ts';

const app = createApp(ChatApp);

app.mount('#app');

setTheme();
registerChatBridge();
