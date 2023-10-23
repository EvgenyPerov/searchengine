package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Word;

import java.util.List;

@Repository
public interface WordRepository  extends JpaRepository<Word, Long> {

//    @Query(value = "SELECT * from words where word LIKE %:wordPart% LIMIT :limit", nativeQuery = true)
//    List<Word> findAllContains(String wordPart, int limit);
}
