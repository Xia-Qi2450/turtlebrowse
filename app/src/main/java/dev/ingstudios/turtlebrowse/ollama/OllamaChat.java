package dev.ingstudios.turtlebrowse.ollama;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import dev.ingstudios.turtlebrowse.components.MainWindow;
import dev.ingstudios.turtlebrowse.tools.specs.FetchToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.SearXNGToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.SnapshotImageToolSpec;
import dev.ingstudios.turtlebrowse.tools.specs.SnapshotToolSpec;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.chat.OllamaChatStreamObserver;
import io.github.ollama4j.models.chat.OllamaChatToolCalls;
import io.github.ollama4j.models.generate.OllamaGenerateTokenHandler;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.tools.Tools;

public class OllamaChat {
	private Ollama ollama;
	private OllamaChatRequest builder;
	final private List<OllamaChatMessage> history = new ArrayList<>();
	public byte[] pageScreenshot;

	public OllamaChat(String userAgent, MainWindow parent) throws OllamaException {
		try {
			ollama = new Ollama();
			ollama.setRequestTimeoutSeconds(120);

			final List<Model> models = ollama.listModels();

			final String chatModel = "gemma4:e2b";

			final List<String> modelNames = new ArrayList<>();
			models.forEach(model -> {
				final String modelName = model.getName();
				modelNames.add(modelName);
			});

			if (!modelNames.contains(chatModel)) {
				ollama.pullModel(chatModel, (model, resp) -> {
					System.out.printf("Pulling %s: %s\n", model, resp.getStatus());
				});
			}

			final Tools.Tool searchToolSpec = new SearXNGToolSpec(userAgent).getSpecification();
			final Tools.Tool fetchToolSpec = new FetchToolSpec(userAgent).getSpecification();
			final Tools.Tool snapshotToolSpec = new SnapshotToolSpec(parent).getSpecification();
			final Tools.Tool snapshotImageToolSpec = new SnapshotImageToolSpec(parent).getSpecification();

			ollama.registerTool(searchToolSpec);
			ollama.registerTool(fetchToolSpec);
			ollama.registerTool(snapshotToolSpec);
			ollama.registerTool(snapshotImageToolSpec);

			builder = OllamaChatRequest.builder().withModel(chatModel);
		} catch (OllamaException e) {
			System.out.printf("Error while initializing Ollama:", e.getMessage());
			throw e;
		}
	}

	public void prompt(String prompt, Consumer<String> onThinkChunk, Consumer<String> onResponseChunk,
			Consumer<String> onDone, Consumer<String> onError) {
		System.out.printf("Received prompt: %s\n", prompt);
		System.out.printf("History: %s", history.toString());

		if (history.isEmpty()) {
			history.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM,
					"You are Gemma, a helpful AI assistant inside the Turtlebrowse browser. Answer the user's questions in a friendly manner."));
		}

		history.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));

		promptInternal(onThinkChunk, onResponseChunk, onDone, onError);
	}

	private void promptInternal(Consumer<String> onThinkChunk, Consumer<String> onResponseChunk,
			Consumer<String> onDone, Consumer<String> onError) {
		final OllamaChatRequest chatRequest = builder.withMessages(history).build();

		OllamaChatStreamObserver streamObserver = new OllamaChatStreamObserver();
		streamObserver.setThinkingStreamHandler(new OllamaGenerateTokenHandler() {
			@Override
			public void accept(String message) {
				onThinkChunk.accept(message);
			}
		});
		streamObserver.setResponseStreamHandler(new OllamaGenerateTokenHandler() {
			@Override
			public void accept(String message) {
				onResponseChunk.accept(message);
			}
		});

		try {
			final OllamaChatResult result = ollama.chat(chatRequest, streamObserver);
			final OllamaChatMessage assistantMessage = result.getResponseModel().getMessage();
			history.add(assistantMessage);
			System.out.printf("History: %s", history.toString());

			List<OllamaChatToolCalls> toolCalls = result.getResponseModel().getMessage().getToolCalls();

			boolean hasScreenshotCall = false;

			if (toolCalls != null)
				for (final OllamaChatToolCalls call : toolCalls) {
					if (call.getFunction().getName().equals("get_page_screenshot")) {
						System.out.println("Get page screenshot called.");
						if (pageScreenshot == null) {
							onError.accept("No screenshot available.");
						}
						hasScreenshotCall = true;
						final OllamaChatMessage screenshotMessage = new OllamaChatMessage(OllamaChatMessageRole.USER,
								"Here is the screenshot of the page.");
						screenshotMessage.setImages(Collections.singletonList(pageScreenshot));
						history.add(screenshotMessage);
						promptInternal(onThinkChunk, onResponseChunk, onDone, onError);
					}
				}

			if (!hasScreenshotCall)
				onDone.accept(assistantMessage.getResponse());
		} catch (OllamaException e) {
			System.out.printf("Error while chatting: %s\n", e.getMessage());
			onError.accept("An unexpected error occurred while chatting.");
		}
	}
}
