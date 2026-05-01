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
