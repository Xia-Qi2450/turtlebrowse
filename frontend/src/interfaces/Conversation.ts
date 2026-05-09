export interface Conversation {
	key: string;
	user: string;
	assistant: {
		thinking: string;
		response: string;
	};
}
