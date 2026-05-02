<script setup lang="ts">
interface ComponentProps {
	message: string;
    thinking?: string;
	sender: 'user' | 'assistant';
}

const props = defineProps<ComponentProps>();
</script>

<template>
	<div class="conv-bubble">
        <p v-if="props.thinking" class="conv-thinking">{{ props.thinking }}</p>
		<p class="conv-message">{{ props.message }}</p>
	</div>
</template>

<style scoped>
.conv-bubble {
	width: fit-content;
	max-width: 50%;
	padding: 10px;
	box-sizing: border-box;
	border-radius: 25px;
	background-color: v-bind("props.sender === 'user' ? 'var(--md-sys-color-primary-container)' : 'transparent'");
	align-self: v-bind("props.sender === 'user' ? 'flex-end' : 'flex-start'");
    color: v-bind("props.sender === 'user' ? 'var(--md-sys-color-on-primary-container)' : 'var(--md-sys-color-on-surface)'");
}

.conv-message, .conv-thinking {
	width: 100%;
	text-align: v-bind("props.sender === 'user' ? 'right' : 'left'");
	margin: 0;
}

.conv-thinking {
    color: var(--md-sys-color-on-surface-variant);
}
</style>
