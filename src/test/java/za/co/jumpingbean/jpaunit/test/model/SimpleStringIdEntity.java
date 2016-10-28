package za.co.jumpingbean.jpaunit.test.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class SimpleStringIdEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", nullable = false)
    public String id;

    public String stringValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

}
