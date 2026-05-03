<script setup lang="ts">
import { marked } from 'marked';

interface ComponentProps {
	message: string;
    thinking?: string;
	sender: 'user' | 'assistant';
}

const props = defineProps<ComponentProps>();
</script>

<template>
	<div class="conv-bubble">
        <span v-if="props.thinking" class="conv-thinking" v-html="marked.parse(props.thinking)"></span>
        <hr v-if="props.thinking" />
		<span class="conv-message" v-html="marked.parse(props.message)"></span>
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

.conv-message {
    font-size: 1rem;
}

.conv-thinking {
    color: var(--md-sys-color-on-surface-variant);
    font-size: 0.7rem;
}
</style>
