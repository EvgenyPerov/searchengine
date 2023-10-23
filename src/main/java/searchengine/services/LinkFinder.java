package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;


public class LinkFinder extends RecursiveTask<Map<String, Document>> {

    private String url;
    private final String rootUrl;

    private static volatile Set<String> checkUrl = new HashSet<>();

    public LinkFinder(String rootUrl, String url) {
        this.url = url;
        this.rootUrl = rootUrl.replace("https://www.", "https://");
    }

    @Override
    protected Map<String, Document> compute() {
            Map<String, Document> linksMap = new TreeMap<>();
            Set<LinkFinder> tasks = new HashSet<>();

            Document document;
            Elements elements;
            try {
                Thread.sleep(200);
                document = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get();
                linksMap.put(url, document);
                elements = document.select("a");
                elements.forEach(el ->  {
                   String item = el.attr("abs:href");
                    if (!item.isEmpty()
                            && !checkUrl.contains(item)
                            && !item.contains("#")
                            && !item.contains("?")
                            && item.replace("https://www.", "https://").startsWith(rootUrl)){

                        linksMap.put(item, document);
                        LinkFinder linkFinderTask = new LinkFinder(rootUrl, item);
                        linkFinderTask.fork();
                        tasks.add(linkFinderTask);
                        checkUrl.add(item);

                        System.out.println(checkUrl.size());//
                    }
                });
            } catch (InterruptedException | IOException ignored) {     }

            tasks.forEach(task -> linksMap.putAll(task.join()));

        tasks.forEach(task -> task.quietlyComplete());
            tasks.clear();

        return  linksMap;
    }

}
