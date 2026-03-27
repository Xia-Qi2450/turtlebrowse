<script setup lang="ts">
import { getUserName, searchWeb } from '@/utils/java_bridge';
import { onMounted, ref } from 'vue';
import '@m3e/web/form-field';
import '@m3e/web/icon';

const userName = ref<string>('');
const searchQuery = ref<string>('');
const errorMessage = ref<string>('');

async function searchWebWrapper() {
	try {
		await searchWeb(searchQuery.value);
	} catch (error) {
		console.error('Error while searching web:', error);
		errorMessage.value = (error as Error).message;
	}
}

onMounted(async () => {
	userName.value = await getUserName();
});
</script>

<template>
	<div class="newtab-wrapper">
		<h1>Hello, {{ userName }}!</h1>
		<m3e-form-field class="search-bar" variant="outlined" @keydown.enter.prevent="searchWebWrapper()">
			<label slot="label">Search the web</label>
			<input v-model="searchQuery" id="search-fld" />
			<m3e-icon slot="prefix" name="search"></m3e-icon>
			<label slot="hint">{{ errorMessage }}</label>
		</m3e-form-field>
	</div>
</template>

<style scoped>
.newtab-wrapper {
	padding: 20px;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	width: 100dvw;
	height: 100dvh;
}

.search-bar {
	width: 50%;
}
</style>
