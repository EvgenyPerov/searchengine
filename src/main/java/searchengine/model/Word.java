package searchengine.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity(name = "words")
@NoArgsConstructor
@Getter
@Setter
public class Word {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    Long id;

    @Column(nullable = false, unique = true)
    String word;

    String morphologyInfo;

    int count;
}
