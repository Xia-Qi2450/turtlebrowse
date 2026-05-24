import DownloadView from '@/views/DownloadView.vue';
import HomeView from '@/views/HomeView.vue';
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
    { name: 'home', path: '/', component: HomeView },
    { name: 'download', path: '/download', component: DownloadView },
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
});

export default router;
