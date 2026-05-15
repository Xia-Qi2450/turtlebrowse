package dev.ingstudios.turtlebrowse.tools.specs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.ingstudios.turtlebrowse.components.MainWindow;
import dev.ingstudios.turtlebrowse.tools.InteractionTool;
import io.github.ollama4j.tools.Tools;

public class InteractionToolSpec {
	private final InteractionTool interactionTool;
	private final List<String> actions = new ArrayList<>();

	public InteractionToolSpec(MainWindow parent) {
		interactionTool = new InteractionTool(parent);
	}

	public final Tools.Tool getSpecification() {
		actions.add("click");
		return Tools.Tool.builder().toolSpec(Tools.ToolSpec.builder()
				.name("interact_with_page")
				.description(
						"Creates an interaction with the current page or element (e.g. click an element). Call get_dom_snapshot to get context on the backend node ID and the current page. This tool returns the DOM snapshot after the action has been performed.")
				.parameters(
						Tools.Parameters.of(
								Map.of(
										"action",
										Tools.Property.builder()
												.enumValues(actions)
												.description("The action you want to perform on the element.")
												.required(true)
												.build(),
										"backendNodeId",
										Tools.Property.builder()
												.type("string")
												.description(
														"The backend node ID element you want to perform an action on.")
												.required(true)
												.build())))
				.build()).toolFunction(args -> {
					final String action = args.get("action").toString();
					final String nodeId = args.get("backendNodeId").toString();
					switch (action) {
						case "click": {
							final String snapshot = interactionTool.clickAndReturnTree(nodeId);
							return snapshot;
						}

						default: {
							System.err.printf("A valid action was not provided: %s\n", action);
							return "A valid action was not provided.";
						}
					}
				}).build();
	}
}
