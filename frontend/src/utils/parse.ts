import { marked } from "marked";

export async function parseMessage(message: string, sender: 'user' | 'assistant'): Promise<string> {
	let result = '';

	const extractRegex = /:::extract([\s\S]*?):::/g;
	const resultExtract = message.replace(extractRegex, (_match, content) => {
		return `<div class="message-extract">${content.trim()}</div>`;
	});
	result = resultExtract;

	if (sender === 'assistant') {
		result = await marked.parse(result);
	}

	console.log('Result:', result);

	return result;
}
