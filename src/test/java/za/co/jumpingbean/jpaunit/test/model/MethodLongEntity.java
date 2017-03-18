package za.co.jumpingbean.jpaunit.test.model;

import javax.persistence.*;

@Entity
public class MethodLongEntity {

    private Long id;

    private Long longValue;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }
}
