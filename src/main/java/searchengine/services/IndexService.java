package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SiteFromProp;
import searchengine.config.SitesList;
import searchengine.dto.index.IndexResponse;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexService {
    private final SitesList sites;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    private ForkJoinPool pool;

    public IndexResponse startIndexing() {
        IndexResponse response = new IndexResponse();
        response.setError("Индексация не выполнена, перезагрузите страницу");

        Map<Site, Map<String, Document> > mapOfResultMap = new HashMap<>();

        for (SiteFromProp siteFromProp : sites.getSites()){

            Optional<Site> siteFromRepo = siteRepository.findAllByUrl(siteFromProp.getUrl()).stream().findFirst();

            if (siteFromRepo.isPresent()) {
                siteFromRepo.get().getPages().clear(); //

                pageRepository.deleteAll(pageRepository.findAllBySite(siteFromRepo.get()));

                siteRepository.delete(siteFromRepo.get());

                log.info("Сервис почистил записи по сайту " + siteFromRepo.get().getName());
            }

            Site site = createSite(siteFromProp);

            pool = new ForkJoinPool();

//            ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
//            scheduler.schedule(() -> {
//                // Вывод сообщения о состоянии объекта "pool"
//                log.info("Состояние объекта pool в процессе: " + pool.toString());
//
//                // Остановка всех потоков в ForkJoinPool
//                pool.shutdownNow();
//            }, 6, TimeUnit.SECONDS);

            log.info("Старт индексации");
            log.info("Состояние объекта pool: " + pool.toString());

            try {
                Map<String, Document> resultMapForkJoin = pool.invoke(new LinkFinder(siteFromProp.getUrl(), site.getUrl()));
                pool.shutdown();

                log.info("resultMapForkJoin.size = " + resultMapForkJoin.size());
                if (resultMapForkJoin.size() < 2) continue;

                log.info("Индексация выполнена");
                log.info("Состояние объекта pool: " + pool.toString()); //

                mapOfResultMap.put(site, resultMapForkJoin);

                site.setStatus(Status.INDEXED);
                site.setStatusTime(LocalDateTime.now());
                siteRepository.save(site);

                response.setResult(true);
                response.setError("");
//                writeToFile(site.getName(), resultMapForkJoin.keySet()); //
            } catch (CancellationException ex) {
                stopPool(site);
                response.setResult(false);
                response.setError("Индексация остановлена пользователем");
                break;
            }
        }

        updateStatusOfSite(mapOfResultMap);

        mapOfResultMap.forEach((site, mapJoinPool) -> mapJoinPool.forEach((key, value) -> createPage(site, key, value)));
        log.info("Сохранение Pages выполнено"); //
        return response;
    }

    public IndexResponse stopIndexing() {
        IndexResponse response = new IndexResponse();
        response.setError("Индексация остановлена пользователем");

        if (pool == null) {
            response.setError("Индексация не запущена");
            return response;
        }
        pool.shutdownNow();

        return response;
    }

    private void stopPool(Site site){
        log.info("Прерывание. Состояние объекта pool: " + pool.toString());

        site.setLastError("Индексация остановлена пользователем");
        site.setStatus(Status.FAILED);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
        pageRepository.deleteAll(pageRepository.findAllBySite(site));
    }

    private Site createSite(SiteFromProp siteFromProp){
        Site site = new Site();
        site.setStatus(Status.INDEXING);
        site.setUrl(siteFromProp.getUrl());
        site.setName(siteFromProp.getName());
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
        log.info("Сервис создал новую запись сайта " + site.getName());
        return site;
    }

    private void createPage(Site site, String key, Document value){
        Page page = new Page();
        page.setSite(site);
        page.setPath(key);
        page.setCode(200);
        page.setContent(value.html());

        site.getPages().add(page);

        pageRepository.save(page);
    }

    private void updateStatusOfSite(Map<Site, Map<String, Document>> mapOfResultMap){

        List<Site> siteJoinPool = mapOfResultMap.keySet().stream().collect(Collectors.toList());

        for (SiteFromProp siteFromProp : sites.getSites()) {

            if (siteJoinPool.isEmpty()) {
                log.info("Все сайты не найдены в выполненных"); //
                for (SiteFromProp siteFromProp2 : sites.getSites()) {
                    Optional<Site> siteFromRepo = siteRepository.findAllByUrl(siteFromProp2.getUrl()).stream().findFirst();

                    if (siteFromRepo.isPresent()) { stopPool(siteFromRepo.get());  }
                }
                break;
            }

            if (!siteJoinPool.stream().map(Site::getUrl).collect(Collectors.toList()).contains(siteFromProp.getUrl())) {
                log.info("Сайт не найден в выполненных: " + siteFromProp.getUrl()); //

                Optional<Site> siteFromRepo = siteRepository.findAllByUrl(siteFromProp.getUrl()).stream().findFirst();

                if (siteFromRepo.isPresent()) { stopPool(siteFromRepo.get());  }

            }
        }
    }

    private static void writeToFile(String name, Set<String> set) {
        String filePath = name+ ".txt";
        System.out.println("Сохраняем данные в файл `" +filePath+ "`. Статистика:");

        File file = new File(filePath);
        try (PrintWriter writer = new PrintWriter(file)) {
            getSortMap(set).forEach((k,v) -> {
                System.out.println("Уровень вложенности= " + k + " Количество ссылок= "+ v.size());
                v.forEach(link -> writer.write("\t".repeat(k) + link + "\n"));
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Map<Integer, Set<String>> getSortMap(Set<String> set) {
        Map<Integer, Set<String>> map = new TreeMap<>();
        set.forEach(item -> {
            String subStr = item.replace("https://", "");
            String[] array = subStr.split("/");
            int level = array.length - 1;

            if (map.containsKey(level)) {
                map.get(level).add(item);
            } else {
                Set<String> newSet = new TreeSet<>();
                newSet.add(item);
                map.put(level, newSet);
            }
        });
        return map;
    }

}
