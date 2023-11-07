package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "indexes")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

       @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

       @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;

    @Column(columnDefinition = "FLOAT NOT NULL")
    private Float rang;

}
