package dev.ingstudios.turtlebrowse.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dev.kreuzberg.htmltomarkdown.HtmlToMarkdown;

public class SummarizePageTool {
	public SummarizePageTool() {
	}

	public String summarizePage(String html) {
		if (html == null) {
			return "";
		}

		final Document doc = Jsoup.parse(html);

		final String[] selectors = {
				"article",
				"main",
				"#content",
				".content",
				"#main",
				"#app",
				"#root"
		};

		Element contentElement = doc.body();

		for (final String selector : selectors) {
			if (selector == null)
				continue;
			final Element found = doc.selectFirst(selector);
			if (found != null && !found.text().isEmpty()) {
				contentElement = found;
				break;
			}
		}

		final Elements possibleAds = contentElement
				.select("[class*=ad], [id*=ad], [class*=advert], [id*=advert]");
		for (final Element ad : possibleAds) {
			ad.remove();
		}

		contentElement.select("script, style, svg, canvas, iframe, noscript, img, video, audio").remove();
		contentElement.select("*").forEach((el) -> {
			el.clearAttributes();
		});
		final String cleanHtml = contentElement.html();
		System.out.printf("Clean HTML: %s\n", cleanHtml);
		System.out.println("Attempting to convert HTML to Markdown...");
		final String markdown = HtmlToMarkdown.convert(cleanHtml);
		System.out.printf("Converted Markdown: %s\n", markdown);

		return markdown;
	}
}
