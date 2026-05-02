package dev.ingstudios.turtlebrowse;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.exceptions.OllamaException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.chat.OllamaChatStreamObserver;
import io.github.ollama4j.models.generate.OllamaGenerateTokenHandler;
import io.github.ollama4j.models.response.Model;

public class OllamaChat {
    private Ollama ollama;
    private OllamaChatRequest builder;
    private List<OllamaChatMessage> chatHistory = new ArrayList<>();

    public OllamaChat() throws OllamaException {
        try {
            ollama = new Ollama();

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

            builder = OllamaChatRequest.builder().withModel(chatModel);

            chatHistory.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, "You are Gemma, a helpful AI assistant inside the Turtlebrowse browser. Answer the user's questions in a friendly manner."));
        } catch (OllamaException e) {
            System.out.printf("Error while initializing Ollama:", e.getMessage());
            throw e;
        }
    }

    public void prompt(String prompt, Consumer<String> onThinkChunk, Consumer<String> onResponseChunk, Consumer<String> onDone, Consumer<String> onError) {
        System.out.printf("Received prompt: %s\n", prompt);

        final OllamaChatRequest chatRequest = builder.withMessages(chatHistory).withMessage(OllamaChatMessageRole.USER, prompt).build();

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
                onThinkChunk.accept(message);
            }
        });

        try {
            OllamaChatResult result = ollama.chat(chatRequest, streamObserver);
            onDone.accept(result.getResponseModel().getMessage().getResponse());
            chatHistory = result.getChatHistory();
        } catch (OllamaException e) {
            System.out.printf("Error while chatting: %s\n", e.getMessage());
            onError.accept("An unexpected error occurred while chatting.");
        }
    }

    public void resetHistory() {
        chatHistory = new ArrayList<>();
    }
}
