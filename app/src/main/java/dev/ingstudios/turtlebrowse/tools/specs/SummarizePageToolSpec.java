package dev.ingstudios.turtlebrowse.tools.specs;

import dev.ingstudios.turtlebrowse.tools.SummarizePageTool;
import dev.ingstudios.turtlebrowse.windows.MainWindow;
import io.github.ollama4j.tools.Tools;

public class SummarizePageToolSpec {
	final SummarizePageTool summarizePageTool;

	public SummarizePageToolSpec(MainWindow parent) {
		summarizePageTool = new SummarizePageTool(parent);
	}

	public Tools.Tool getSpecification() {
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("get_page_summary")
				.description(
						"Extracts the main information from a page in Markdown format. Use this when you need the context of the page. Do not use this for identifying buttons or inputs.")
				.build()).toolFunction(args -> {
					return summarizePageTool.summarizePage();
				}).build();
	}
}
