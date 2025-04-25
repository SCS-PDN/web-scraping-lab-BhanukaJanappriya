package lk.ac.pdn;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScraper {
    public static void main(String[] args) throws IOException {

        final String url = "https://www.bbc.com";

        Document doc = Jsoup.connect(url).get();

        //Print the page title
        System.out.println("Title: " + doc.title());

        //Print all headings (h1 to h6)
        System.out.println("\nAll Headings:");
        for (int i = 1; i <= 6; i++) {
            Elements headings = doc.select("h" + i);
            for (Element heading : headings) {
                System.out.println("h" + i + ": " + heading.text());
            }
        }

        //Print all <a> tag links
        System.out.println("\nAll Links:");
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            System.out.println(link.attr("abs:href"));
        }
    }
}
