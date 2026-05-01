import type { CefRequest } from "@/types/cef";

export function fetchFromJava(request: string, params?: Record<string, string>): Promise<string | void> {
	console.log('cefQuery:', window.cefQuery);
	console.log('keys:', Object.keys(window));

	return new Promise((resolve, reject) => {
		if (window.cefQuery) {
			window.cefQuery({
				request: JSON.stringify({
					request: request,
					params: params,
				} as CefRequest),
				onSuccess: (response) => resolve(response),
				onFailure: (code, msg) => reject(new Error(`CEF Error ${code}: ${msg}`)),
			});
		} else {
			reject(new Error("CEF environment not detected."));
		}
	});
}

export async function getUserName(): Promise<string> {
	try {
		const name = await fetchFromJava('GET_NAME');
		return name || 'Guest';
	} catch (error) {
		console.error('Error while fetching name:', error);
		return 'Guest';
	}
}

export async function searchWeb(query: string) {
	await fetchFromJava('SEARCH_WEB', { query: query });
}
