package dev.ingstudios.turtlebrowse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import io.github.ollama4j.exceptions.OllamaException;

public class BrowserServer {
    private final Gson gson = new Gson();

    public BrowserServer() {
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 6767), 0);
            server.createContext("/prompt-stream", exchange -> {
                System.out.println("Client listening to prompt-stream.");

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }

                exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
                exchange.getResponseHeaders().add("Cache-Control", "no-cache");
                exchange.getResponseHeaders().add("Connection", "keep-alive");

                exchange.sendResponseHeaders(200, 0);

                final Map<String, String> params = getParams(exchange);

                final String prompt = params.get("prompt");

                final OutputStream out = exchange.getResponseBody();

                OllamaChat chat;
                try {
                    chat = new OllamaChat();
                } catch (OllamaException e) {
                    out.write(("data: An error occurred while initializing Ollama:" + e.getMessage()).getBytes());
                    out.flush();
                    return;
                }

                chat.prompt(prompt,
                    chunk -> writeSSE(out, "think", chunk),
                    chunk -> writeSSE(out, "response", chunk),
                    result -> writeSSE(out, "done", result),
                    error -> writeSSE(out, "error", error)
                );
            });
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private Map<String, String> getParams(HttpExchange exchange) {
        final String rawQuery = exchange.getRequestURI().getQuery();
        final Map<String, String> params = new HashMap<>();

        if (rawQuery != null) {
            for (final String param : rawQuery.split("&")) {
                final String[] pair = param.split("=");
                params.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }

        return params;
    }

    private void writeSSE(OutputStream out, String type, String data) {
        try {
            String json = "{\"type\":\"" + type + "\",\"data\":" + gson.toJson(data) + "}";
            out.write(("data: " + json + "\n\n").getBytes());
            out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
