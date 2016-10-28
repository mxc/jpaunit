package za.co.jumpingbean.jpaunit.test.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="ROBOT")
public class NamedEntityWithNamedFields {

    @Id
    @Column( name = "name" )
    String robotLeader;

    @Column( name = "team_name" )
    String teamName;


    public String getRobotLeader() {
        return robotLeader;
    }

    public void setRobotLeader(String robotLeader) {
        this.robotLeader = robotLeader;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
