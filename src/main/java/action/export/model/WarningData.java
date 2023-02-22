package action.export.model;

import java.sql.Timestamp;

public class WarningData {


    String name;
    Timestamp immunityUntil;
    Timestamp lastWarning;

    public WarningData(String name, Timestamp immunityUntil, Timestamp lastWarning) {
        this.name = name;
        this.immunityUntil = immunityUntil;
        this.lastWarning = lastWarning;
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
}
