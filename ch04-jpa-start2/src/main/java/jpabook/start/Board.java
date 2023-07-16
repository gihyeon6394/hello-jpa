package jpabook.start;

import javax.persistence.*;

@Entity
@Table(name = "BOARD")
@TableGenerator(
        name = "BOARD_SEQ_GENERATOR",
        table = "MY_SEQUENCE",
        pkColumnValue = "BOARD_SEQ", allocationSize = 1)
public class Board {

    @Id
    @Column(name = "BOARD_ID")
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "BOARD_SEQ_GENERATOR")
    private Long id;

    private String title;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
