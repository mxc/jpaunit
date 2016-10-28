package za.co.jumpingbean.jpaunit.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="CARTOON")
public class NamedEntityWithNamedMethods {

    String cartoon;
    String leadCharacter;

    @Id
    @Column( name = "cartoon" )
    public String getCartoon() {
        return cartoon;
    }

    public void setCartoon(String cartoon) {
        this.cartoon = cartoon;
    }

    @Column( name = "lead_character" )
    public String getLeadCharacter() {
        return leadCharacter;
    }

    public void setLeadCharacter(String leadCharacter) {
        this.leadCharacter = leadCharacter;
    }
}
