package lk.ac.pdn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebScraper {

    public static void main(String[] args) throws IOException {
        final String url = "https://www.bbc.com";

        System.out.println("Title of the Page:");
        System.out.println(getTitle(url));

        System.out.println("\nAll Headings (H1-H6):");
        getAllHeadings(url);

        System.out.println("\nAll Links (<a> tags):");
        getAllLinks(url);

        System.out.println("\nBBC News Headlines with Author & Date:");
        List<NewsArticle> articles = getBBCHeadlines(url);
        articles.forEach(System.out::println);
    }

    public static String getTitle(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return doc.title();
    }

    public static void getAllHeadings(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        for (int i = 1; i <= 6; i++) {
            Elements headings = doc.select("h" + i);
            for (Element heading : headings) {
                System.out.println("H" + i + ": " + heading.text());
            }
        }
    }

    public static void getAllLinks(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            System.out.println(link.attr("abs:href"));
        }
    }

    public static List<NewsArticle> getBBCHeadlines(String url) throws IOException {
        List<NewsArticle> articles = new ArrayList<>();
        Document homepage = Jsoup.connect(url).get();
        Elements newsAnchors = homepage.select("a[href*='/news']");

        for (Element anchor : newsAnchors) {
            String articleUrl = anchor.absUrl("href");

            // Skip non-article pages
            if (articleUrl.contains("/live/") || articleUrl.contains("/av/")) continue;

            try {
                Document articleDoc = Jsoup.connect(articleUrl).get();

                // Extract headline
                Element heading = articleDoc.selectFirst("h1#main-heading");
                if (heading == null) continue;
                String headline = heading.text();

                // Extract author from byline block
                String author = "Unknown Author";
                Element byline = articleDoc.selectFirst("div[data-testid=byline-new-contributors]");
                if (byline != null) {
                    Element authorSpan = byline.selectFirst("span.sc-801dd632-7");
                    if (authorSpan != null) {
                        author = authorSpan.text();
                    }
                }

                // Extract publication date from meta tag
                String date = "Unknown Date";
                Element dateMeta = articleDoc.selectFirst("meta[property=article:published_time]");
                if (dateMeta != null) {
                    date = dateMeta.attr("content");
                }

                // Add to list
                NewsArticle article = new NewsArticle();
                article.setHeadline(headline);
                article.setAuthor(author);
                article.setDate(date);
                article.setLink(articleUrl);
                articles.add(article);

                // Optional: limit number of articles
                if (articles.size() >= 10) break;

            } catch (IOException e) {
                System.out.println("Failed to fetch: " + articleUrl);
            }
        }
        return articles;
    }

    static class NewsArticle {
        private String headline;
        private String author;
        private String date;
        private String link;

        public void setHeadline(String headline) { this.headline = headline; }
        public void setAuthor(String author) { this.author = author; }
        public void setDate(String date) { this.date = date; }
        public void setLink(String link) { this.link = link; }

        @Override
        public String toString() {
            return String.format("Headline: %s\nAuthor: %s\nDate: %s\nLink: %s\n", headline, author, date, link);
        }
    }
}
