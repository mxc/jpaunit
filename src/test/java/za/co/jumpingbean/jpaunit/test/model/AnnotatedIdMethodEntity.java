package za.co.jumpingbean.jpaunit.test.model;

import javax.persistence.Id;

public class AnnotatedIdMethodEntity {

    private int id;

    @Id
    int getId() {
        return id;
    }

    void setId(int id) {
         this.id = id;
    }

}
