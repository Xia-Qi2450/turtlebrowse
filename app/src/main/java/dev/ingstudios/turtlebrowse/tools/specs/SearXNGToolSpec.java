package dev.ingstudios.turtlebrowse.tools.specs;

import java.util.Map;

import dev.ingstudios.turtlebrowse.tools.SearXNGSearchTool;
import io.github.ollama4j.tools.Tools;

public class SearXNGToolSpec {
    private SearXNGSearchTool searchTool;

    public SearXNGToolSpec(String userAgent) {
        searchTool = new SearXNGSearchTool(userAgent);
    }

    public Tools.Tool getSpecification() {
        return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
            .name("search_web")
            .description("Searches up-to-date information with on the web")
            .parameters(
                Tools.Parameters.of(
                    Map.of(
                        "search_query",
                        Tools.Property.builder()
                            .type("string")
                            .description("The query you want to search up on the web")
                            .required(true)
                            .build()
                    )
                )
            )
            .build()
        ).toolFunction(
            args -> {
                final String searchQuery = args.get("search_query").toString();
                try {
                    final String searchResult = searchTool.searchWeb(searchQuery).toString();
                    System.out.printf("Search result: %s\n", searchResult);
                    return searchResult;
                } catch (Exception e) {
                    return e.getMessage();
                }
            }
        ).build();
    }
}