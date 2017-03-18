package za.co.jumpingbean.jpaunit.test.model;

import javax.persistence.*;

@Entity
public class MethodIntegerEntity {

    private int id;

    private Integer integerValue;

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public void setIntegerValue(Integer integer) {
        this.integerValue = integer;
    }

}
