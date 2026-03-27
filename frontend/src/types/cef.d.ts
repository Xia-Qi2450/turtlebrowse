export interface CefRequest {
	request: string;
	params?: Record<string, string>;
}

interface CefQueryOptions {
	request: string;
	onSuccess: (response: string) => void;
	onFailure: (errorCode: number, errorMessage: string) => void;
}

declare global {
	interface Window {
		cefQuery: (options: CefQueryOptions) => void;
	}
}
