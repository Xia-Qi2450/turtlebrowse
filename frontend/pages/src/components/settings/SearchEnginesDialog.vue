<script setup lang="ts">
import { useDialog } from '@/composables/dialog';
import '@m3e/web/dialog';
import { M3eDialogElement } from '@m3e/web/dialog';
import { onMounted, useTemplateRef } from 'vue';

const dialog = useTemplateRef<M3eDialogElement>('dialog');

const { searchEnginesDialog } = useDialog();

onMounted(() => {
	searchEnginesDialog.value = dialog.value;
});
</script>

<template>
	<m3e-dialog ref="dialog">
		<span slot="header">Search engines</span>
	</m3e-dialog>
</template>
