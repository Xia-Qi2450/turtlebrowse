package dev.ingstudios.turtlebrowse.tools;

import java.util.ArrayList;
import java.util.List;

import dev.ingstudios.turtlebrowse.components.MainWindow;
import dev.ingstudios.turtlebrowse.tools.specs.SnapshotToolSpec;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.response.Model;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.tools.Tools;

public class FindElementTool {
	private final Ollama ollama = new Ollama();
	private final OllamaGenerateRequest builder;

	public FindElementTool(MainWindow parent) throws OllamaException {
		ollama.setRequestTimeoutSeconds(120);

		try {
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

			final Tools.Tool snapshotToolSpec = new SnapshotToolSpec(parent).getSpecification();

			ollama.registerTool(snapshotToolSpec);

			builder = OllamaGenerateRequest.builder().withModel(chatModel);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public String findElement(String element) {

		final OllamaGenerateRequest chatRequest = builder.withUseTools(true)
				.withPrompt(
						"Find the backendNodeId of the elment: '%s'. Return the backendNodeId number and nothing else."
								.formatted(element))
				.build();

		try {
			final OllamaResult result = ollama.generate(chatRequest, null);
			final String response = result.getResponse();
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return "An unexpected error occurred while finding element.";
		}
	}
}
