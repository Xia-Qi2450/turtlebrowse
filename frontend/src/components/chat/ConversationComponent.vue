<script setup lang="ts">
import '@m3e/web/form-field';
import ConversationBubble from './ConversationBubble.vue';
import { usePrompt } from '@/composables/prompt';

const { prompt, prompts, isGenerating, sendPromptKeyboard } = usePrompt();
</script>

<template>
	<div class="conv-wrapper">
		<div class="conv-box">
			<div v-for="conv in prompts" :key="conv.key" class="conv-group">
                <ConversationBubble sender="user" :message="conv.user"></ConversationBubble>
                <ConversationBubble sender="assistant" :thinking="conv.assistant.thinking" :message="conv.assistant.response"></ConversationBubble>
            </div>
		</div>
		<m3e-form-field class="form-field" variant="outlined" :disabled="isGenerating">
			<label slot="label" for="prompt-field">Ask AI</label>
			<input id="prompt-field" v-model="prompt" @keydown="sendPromptKeyboard" />
		</m3e-form-field>
	</div>
</template>

<style scoped>
.conv-wrapper {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 100%;
	height: 100%;
	box-sizing: border-box;
	padding: 20px;
	gap: 20px;
}

.conv-box {
	flex-grow: 1;
	width: 100%;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
    overflow: scroll;
}

.conv-group {
    display: flex;
    flex-direction: column;
    width: 100%;
}

.form-field {
	flex-shrink: 0;
	width: 100%;
}
</style>
