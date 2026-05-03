export function promptStreaming(prompt: string, onThink: (chunk: string) => void, onResponse: (chunk: string) => void, onFinish: (response: string) => void) {
    const source = new EventSource(`turtlebrowse://api/prompt-stream?prompt=${encodeURIComponent(prompt)}`);
    console.log('EventSource created:', source.readyState);
    source.addEventListener('open', () => {
        console.log('EventSource open.');
    });
    source.addEventListener('message', (event) => {
        const { type, data } = JSON.parse(event.data);

        switch (type) {
            case 'think':
                onThink(data);
                break;
            case 'response':
                onResponse(data);
                break;
            case 'done':
                source.close();
                onFinish(data);
                break;
            case 'error':
                source.close();
                throw new Error(data);
            default:
                console.log('Unknown type:', type);
                throw new Error('Unknown type');
        }
    });
    source.addEventListener('error', (event) => {
        console.error('SSE error:', event);
        source.close();
    });
}
