package searchengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "lemmas")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

     @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String lemma;

    @Column(columnDefinition = "INT NOT NULL")
    private Integer frequency;

    @JsonIgnore
    @OneToMany(mappedBy = "lemma")
    private List<Index> indexes = new ArrayList<>();

    public void increaseFrequency(){++frequency;}

    public void decreaseFrequency(){--frequency;}

}
