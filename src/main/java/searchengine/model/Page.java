package searchengine.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "pages")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

//    @Transient
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false, unique = true, length = 500)
//    @Column(columnDefinition = "TEXT NOT NULL", le)
    private String path;

    @Column(columnDefinition = "INT NOT NULL")
    private Integer code;

    @Column(columnDefinition = "LONGTEXT NOT NULL")
    private String content;

    @JsonIgnore
    @OneToMany(mappedBy = "page")
    private List<Index> indexes = new ArrayList<>();

}