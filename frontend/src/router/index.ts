import NewTabView from '@/views/NewTabView.vue';
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
	{ name: 'newtab', path: '/newtab', component: NewTabView, meta: { title: 'New Tab' } },
];

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: routes,
});

router.beforeEach((to) => {
	document.title = to.meta.title as string;
});

export default router;
