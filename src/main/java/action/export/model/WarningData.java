package action.export.model;

import java.sql.Timestamp;

public class WarningData {


    String name;
    Timestamp immunityUntil;
    Timestamp giveawayUntil;
    Timestamp lastWarning;

    public WarningData(String name, Timestamp immunityUntil, Timestamp lastWarning, Timestamp giveawayUntil) {
        this.name = name;
        this.immunityUntil = immunityUntil;
        this.lastWarning = lastWarning;
        this.giveawayUntil = giveawayUntil;
    }


    public String getName() {
        return name;
    }

    public Timestamp getImmunityUntil() {
        return immunityUntil;
    }

    public Timestamp getLastWarning() {
        return lastWarning;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImmunityUntil(Timestamp immunityUntil) {
        this.immunityUntil = immunityUntil;
    }

    public void setLastWarning(Timestamp lastWarning) {
        this.lastWarning = lastWarning;
    }

    public Timestamp getGiveawayUntil() {
        return giveawayUntil;
    }

    public void setGiveawayUntil(Timestamp giveawayUntil) {
        this.giveawayUntil = giveawayUntil;
    }
}
