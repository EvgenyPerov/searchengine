package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.index.IndexResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexService indexService;

    public ApiController(StatisticsService statisticsService, IndexService indexService) {
        this.statisticsService = statisticsService;
        this.indexService = indexService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("Получение страницы statistics");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() {
        log.info("Получение страницы startIndexing");
        return ResponseEntity.ok(indexService.startIndexing());
//        return ResponseEntity.ok(new IndexResponse());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() {
        log.info("Получение страницы stopIndexing");
        return ResponseEntity.ok(indexService.stopIndexing());
    }
}
