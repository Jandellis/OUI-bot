package action.giveaway.model;

import java.sql.Timestamp;

public class GiveawayWinner {

    Long id;
    String name;
    int wins;
    Timestamp LastWin;


    public GiveawayWinner(String name, int wins, Timestamp lastWin) {
        this.name = name;
        this.wins = wins;
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

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public Timestamp getLastWin() {
        return LastWin;
    }

    public void setLastWin(Timestamp lastWin) {
        LastWin = lastWin;
    }
}
