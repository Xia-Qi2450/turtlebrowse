import type { SearchEngine } from "@/types/SearchEngine";

export async function fetchFromJava(request: string, params?: Record<string, string>): Promise<string | void> {
	const response = await fetch(`turtlebrowse://api/${request}`, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json',
		},
		body: JSON.stringify(params),
	});
	return response.text();
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

export async function getTheme(): Promise<string | undefined> {
    try {
        return await fetchFromJava('GET_THEME') as string | undefined;
    } catch (error) {
        console.error('Error while getting theme:', error);
        return undefined;
    }
}

export async function getDefaultSearchEngine(): Promise<SearchEngine> {
	try {
		const searchEngine = await fetchFromJava('GET_SEARCH_ENGINE') as SearchEngine | undefined;

		if (!searchEngine) {
			return 'google';
		}

		return searchEngine;
	} catch (error) {
		console.error('Error while getting default search engine:', error);
		return 'google';
	}
}

export async function setDefaultSearchEngine(engine: SearchEngine) {
	try {
		await fetchFromJava('SET_SEARCH_ENGINE', { engine: engine });
	} catch (error) {
		console.error('Failed to set search engine:', error);
	}
}
