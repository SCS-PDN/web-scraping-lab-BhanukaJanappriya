import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/scrape")
public class ScrapeService extends HttpServlet {

    static class NewsArticle {
        private String headline;
        private String author;
        private String date;

        public NewsArticle(String headline, String author, String date) {
            this.headline = headline;
            this.author = author;
            this.date = date;
        }

        public String getHeadline() {
            return headline;
        }

        public String getAuthor() {
            return author;
        }

        public String getDate() {
            return date;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getParameter("url");
        List<NewsArticle> scrapedData = scrapeData(url);

        Gson gson = new Gson();
        String json = gson.toJson(scrapedData);

        response.setContentType("application/json");
        response.getWriter().write(json);
    }

    private List<NewsArticle> scrapeData(String url) throws IOException {
        List<NewsArticle> articles = new ArrayList<>();
        Document doc = Jsoup.connect(url).get();
        Elements articleLinks = doc.select("a[href*='/news/']");

        for (Element link : articleLinks) {
            String articleUrl = link.absUrl("href");
            if (articleUrl.contains("/live/") || articleUrl.contains("/av/")) continue; // skip non-articles

            try {
                Document articleDoc = Jsoup.connect(articleUrl).get();
                String headline = articleDoc.selectFirst("h1#main-heading") != null ? articleDoc.selectFirst("h1#main-heading").text() : "N/A";
                String author = articleDoc.selectFirst("div[data-testid=byline-new-contributors] span") != null ? articleDoc.selectFirst("div[data-testid=byline-new-contributors] span").text() : "N/A";
                Element dateMeta = articleDoc.selectFirst("meta[property=article:published_time]");
                String date = dateMeta != null ? dateMeta.attr("content") : "N/A";

                if (!headline.equals("N/A")) {
                    articles.add(new NewsArticle(headline, author, date));
                }
            } catch (Exception e) {
                // Skip bad links
                e.printStackTrace();
            }
        }
        return articles;
    }
}
