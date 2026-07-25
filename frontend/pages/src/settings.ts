import { createApp } from 'vue';
import SettingsApp from './SettingsApp.vue';
import '@/assets/main.css';
import 'material-symbols/outlined.css';
import { setTheme } from './utils/theme.ts';

const app = createApp(SettingsApp);

app.mount('#app');

setTheme();
