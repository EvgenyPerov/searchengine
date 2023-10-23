package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import searchengine.model.Site;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Integer>{

    List<Site> findAllByUrl(String url);

    void deleteAllByUrl(String url);


}