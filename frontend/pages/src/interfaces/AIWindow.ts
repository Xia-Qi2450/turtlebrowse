export interface AIWindow extends Window {
	addPrompt: (prompt: string) => void;
	addPromptRewrite: (prompt: string) => void;
}
