package dev.ingstudios.turtlebrowse.tools.specs;

import java.util.Map;

import dev.ingstudios.turtlebrowse.tools.FetchTool;
import io.github.ollama4j.tools.Tools;

public class FetchToolSpec {
	private final FetchTool fetchTool;

	public FetchToolSpec(String userAgent) {
		fetchTool = new FetchTool(userAgent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("fetch_url")
				.description(
						"Fetches a URL on the internet. This is useful when you want to extract more information from a page on fetch the results from an API.")
				.parameters(
						Tools.Parameters.of(
								Map.of(
										"url",
										Tools.Property.builder()
												.type("string")
												.description(
														"The full URL you want to fetch from including the URL scheme.")
												.required(true)
												.build())))
				.build()).toolFunction(
						args -> {
							final String url = args.get("url").toString();
							try {
								final String response = fetchTool.fetch(url);
								return response;
							} catch (Exception e) {
								return e.getMessage();
							}
						})
				.build();
	}
}
