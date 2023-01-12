package action.reminder.model;

import java.sql.Time;

public class Profile {

    String name;
    String shackName;
    Status status;
    Boolean enabled;
    String emote;
    String message;
    int depth;
    int upgrade;
    Time sleepStart;
    Time sleepEnd;
    Boolean dmReminder = false;
    String userName;

    public Profile(String name, String shackName, Status status) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
    }

    public Profile(String name, String shackName, Status status, Boolean enabled, String emote, String message, int depth, int upgrade, Time sleepStart, Time sleepEnd, Boolean dmReminder, String userName) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
        if (enabled == null)
            enabled = false;
        this.enabled = enabled;
        this.emote = emote;
        this.message = message;
        this.depth = depth;
        this.upgrade = upgrade;
        this.sleepEnd = sleepEnd;
        this.sleepStart = sleepStart;
        this.dmReminder = dmReminder;
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public String getShackName() {
        return shackName;
    }

    public Status getStatus() {
        return status;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getEmote() {
        return emote;
    }

    public String getMessage() {
        return message;
    }

    public int getDepth() {
        return depth;
    }

    public int getUpgrade() {
        return upgrade;
    }

    public Time getSleepStart() {
        return sleepStart;
    }

    public Time getSleepEnd() {
        return sleepEnd;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Boolean getDmReminder() {
        return dmReminder;
    }

    public String getUserName() {
        return userName;
    }
    // emabled
    // emote
    // message
}
