import { createApp } from 'vue';
import NewTabApp from './NewTabApp.vue';
import '@/assets/main.css';
import 'material-symbols/outlined.css';

const app = createApp(NewTabApp);

app.mount('#app');
