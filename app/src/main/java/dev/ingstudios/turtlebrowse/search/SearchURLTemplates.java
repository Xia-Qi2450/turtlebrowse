package dev.ingstudios.turtlebrowse.search;

import java.util.HashMap;
import java.util.Map;

public class SearchURLTemplates {
	public static Map<String, String> searchTemplates = new HashMap<>();

	static {
		searchTemplates.put("google", "https://google.com/search?q=%s");
		searchTemplates.put("ddg", "https://duckduckgo.com/?q=%s");
		searchTemplates.put("ddg-noai", "https://noai.duckduckgo.com/?q=%s");
		searchTemplates.put("brave", "https://search.brave.com/search?q=%s");
		searchTemplates.put("startpage", "https://startpage.com/sp/search?query=%s");
		searchTemplates.put("yahoo", "https://search.yahoo.com/search?p=%s");
		searchTemplates.put("vyntr", "https://vyntr.com/search?q=%s");
		searchTemplates.put("bing", "https://bing.com/search?q=%s");
	}
}
