<script setup lang="ts">
import '@m3e/web/form-field';
import ConversationBubble from './ConversationBubble.vue';
import { ref } from 'vue';
import { promptStreaming } from '@/utils/chat';
import { M3eSnackbar } from '@m3e/web/snackbar';

interface Conversation {
    key: string;
    user: string;
    assistant: {
        thinking: string;
        response: string;
    };
}

const prompt = ref<string>('');
const prompts = ref<Conversation[]>([]);
const isGenerating = ref<boolean>(false);

function sendPrompt() {
    isGenerating.value = true;

    const conversation: Conversation = {
        key: window.crypto.randomUUID(),
        user: prompt.value,
        assistant: {
            thinking: '',
            response: '',
        },
    };

    prompts.value.push(conversation);

    try {
        promptStreaming(prompt.value, (chunk) => {
            conversation.assistant.thinking += chunk;
        }, (chunk) => {
            conversation.assistant.response += chunk;
        }, (response) => {
            conversation.assistant.response = response;
            isGenerating.value = false;
        });

        prompt.value = '';
    } catch (error) {
        console.error('An error occurred while generating:', error);
        M3eSnackbar.open((error as Error).message, {
            duration: 4000,
        });
        isGenerating.value = false;
    }
}
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
			<input id="prompt-field" v-model="prompt" @keydown.enter="sendPrompt()" />
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
    width: 100%;
}

.form-field {
	flex-shrink: 0;
	width: 100%;
}
</style>
