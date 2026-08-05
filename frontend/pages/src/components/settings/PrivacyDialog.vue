<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, useTemplateRef } from 'vue';
import '@m3e/web/switch';

const dialog = useTemplateRef<M3eDialogElement>('dialog');

const { privacyDialog } = useDialog();

onMounted(() => {
	privacyDialog.value = dialog.value;
});
</script>

<template>
	<m3e-dialog ref="dialog">
		<span slot="header">Privacy & security</span>
		<div class="privacy-dialog">
			<div class="toggle-setting">
				<p>Enable Discord Presence</p>
				<m3e-switch icons="both"></m3e-switch>
			</div>
		</div>
	</m3e-dialog>
</template>

<style scoped>
.privacy-dialog {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 8px;
}

.toggle-setting {
	display: flex;
	flex-direction: row;
	gap: 15px;
	align-items: center;
	justify-content: space-between;
}
</style>
