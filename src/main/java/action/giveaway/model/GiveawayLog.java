package action.giveaway.model;

import java.sql.Timestamp;

public class GiveawayLog {

    Long id;
    String name;
    Timestamp LastWin;


    public GiveawayLog(String name, Timestamp lastWin) {
        this.name = name;
        LastWin = lastWin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Timestamp getLastWin() {
        return LastWin;
    }

    public void setLastWin(Timestamp lastWin) {
        LastWin = lastWin;
    }
}
