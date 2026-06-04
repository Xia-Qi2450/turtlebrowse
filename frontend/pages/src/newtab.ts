import { createApp } from 'vue';
import NewTabApp from './NewTabApp.vue';
import '@/assets/main.css';
import 'material-symbols/outlined.css';
import { setTheme } from './utils/theme.ts';

const app = createApp(NewTabApp);

app.mount('#app');

setTheme();
