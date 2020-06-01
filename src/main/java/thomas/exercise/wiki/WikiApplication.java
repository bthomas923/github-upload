package thomas.exercise.wiki;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class WikiApplication {
    private final Map<String, StringBuilder> sectionMap = new HashMap<>();

	public WikiApplication() throws IOException {
		Document wikiDoc = Jsoup.connect("https://en.wikipedia.org/wiki/Heidenheim_an_der_Brenz").get();

		// The DIV with class "mw-parser-output" appears to be the deepest element that contains
		// all of the relevant content.
		Elements content = wikiDoc.select(".mw-parser-output");
		populateSectionMap(content.first().child(0));
	}

	private String transformSectionName(final String sectionName) {
		return sectionName == null ? sectionName : sectionName.replace(",","" ).toLowerCase();
	}

	private void populateSectionMap(final Element startElement) {
		String sectionName = "introduction";
		Element currentElement = startElement;

		StringBuilder buffer = sectionMap.computeIfAbsent(sectionName, key -> new StringBuilder());
		while (currentElement != null) {
			String nodeName = currentElement.nodeName();

			// Section name information is in the first child node of any heading tag.
			if ("h2".equalsIgnoreCase(nodeName) || "h3".equalsIgnoreCase(nodeName)) {
				Elements headlines = currentElement.select(".mw-headline");
				if (headlines.size() > 0) {
					sectionName = transformSectionName(headlines.first().id());
					buffer = sectionMap.computeIfAbsent(sectionName, key -> new StringBuilder());
				}
			}

			// Actual text content is mostly in the paragraph tags.
			if ("p".equalsIgnoreCase(currentElement.nodeName())) {
				if (buffer.length() > 0) {
					buffer.append("\n\n");
				}

				buffer.append(currentElement.text());
			}

			currentElement = currentElement.nextElementSibling();
		}
	}

	private String getContents(final String sectionName) {
		return sectionMap.getOrDefault(sectionName, new StringBuilder()).toString();
	}

	@GetMapping("/")
	public String introduction() {
		return getContents("introduction");
	}

	@GetMapping("/government")
	public String government()  {
		return getContents("government");
	}

	@GetMapping("/industry")
	public String industry()  {
		return getContents("industry");
	}

	@GetMapping("/religion")
	public String religion()  {
		return getContents("religion");
	}

	@GetMapping("/geography")
	public String geography()  {
		return getContents("geography");
	}

	@GetMapping("/music")
	public String music()  {
		return getContents("music");
	}

	@GetMapping("/culture")
	public String people_culture_and_architecture()  {
		return getContents("people_culture_and_architecture");
	}

	@GetMapping("/events")
	public String events()  {
		return getContents("events");
	}

	@GetMapping("/all/{section}")
	public String all(@PathVariable("section") String sectionName)  {
		return getContents(transformSectionName(sectionName));
	}

	public static void main(String[] args) {
		SpringApplication.run(WikiApplication.class, args);
	}

}
