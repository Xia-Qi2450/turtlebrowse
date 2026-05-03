<script setup lang="ts">
import { marked } from 'marked';
import '@m3e/web/expansion-panel';

interface ComponentProps {
	message: string;
    thinking?: string;
	sender: 'user' | 'assistant';
}

const props = defineProps<ComponentProps>();
</script>

<template>
	<div class="conv-bubble" :class="props.sender === 'user' ? 'user-message' : ''">
        <m3e-expansion-panel v-if="props.thinking && props.message" class="think-expand" toggle-position="before" toggle-direction="horizontal">
            <span slot="header">Show thinking</span>
            <span class="conv-thinking" v-html="marked.parse(props.thinking)"></span>
        </m3e-expansion-panel>
        <span v-else-if="props.thinking" class="conv-thinking" v-html="marked.parse(props.thinking)"></span>

		<span class="conv-message" v-html="props.sender === 'assistant' ? marked.parse(props.message) : props.message"></span>
	</div>
</template>

<style scoped>
.conv-bubble {
    display: flex;
    flex-direction: column;
    align-items: v-bind("props.sender === 'user' ? 'flex-end' : 'flex-start'");
	width: fit-content;
	max-width: 50%;
	padding: 10px;
    gap: 10px;
	box-sizing: border-box;
	border-radius: 25px;
	background-color: v-bind("props.sender === 'user' ? 'var(--md-sys-color-primary-container)' : 'transparent'");
	align-self: v-bind("props.sender === 'user' ? 'flex-end' : 'flex-start'");
    color: v-bind("props.sender === 'user' ? 'var(--md-sys-color-on-primary-container)' : 'var(--md-sys-color-on-surface)'");
}

.conv-message *, .conv-thinking * {
	width: 100%;
	text-align: v-bind("props.sender === 'user' ? 'right' : 'left'");
}

.conv-message {
    font-size: 1rem;
}

.conv-thinking {
    color: var(--md-sys-color-on-surface-variant);
    font-size: 1rem;
}

.think-expand {
    --m3e-expansion-panel-shape: 10px;
    --m3e-expansion-panel-open-shape: 10px;
}

.user-message {
    margin: 0;
}
</style>
