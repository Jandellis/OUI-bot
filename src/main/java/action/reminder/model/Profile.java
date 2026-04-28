package action.reminder.model;

import java.sql.Time;
import java.sql.Timestamp;

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
    Boolean ignoredHidden = false;
    Boolean dnd = false;
    String userName;
    Timestamp rushHourEnd;
    Timestamp rushHourIgnore;
    int workIncome;
    int tipsIncome;
    int overtimeIncome;

    public Profile(String name, String shackName, Status status) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
    }

    public Profile(String name,
                   String shackName,
                   Status status,
                   Boolean enabled,
                   String emote,
                   String message,
                   int depth,
                   int upgrade,
                   Time sleepStart,
                   Time sleepEnd,
                   Boolean dmReminder,
                   Boolean ignoredHidden,
                   Boolean dnd,
                   String userName,
                   Timestamp rushHourEnd,
                   Timestamp rushHourIgnore,
                   int workIncome,
                   int tipsIncome,
                   int overtimeIncome) {
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
        this.ignoredHidden = ignoredHidden;
        this.dnd = dnd;
        this.rushHourEnd = rushHourEnd;
        this.rushHourIgnore = rushHourIgnore;
        this.workIncome = workIncome;
        this.tipsIncome = tipsIncome;
        this.overtimeIncome = overtimeIncome;
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

    public Boolean getIgnoredHidden() {
        return ignoredHidden;
    }

    public Boolean getDnd() {
        return dnd;
    }

    public String getUserName() {
        return userName;
    }
    // emabled
    // emote
    // message


    public Timestamp getRushHourEnd() {
        return rushHourEnd;
    }

    public void setRushHourEnd(Timestamp rushHourEnd) {
        this.rushHourEnd = rushHourEnd;
    }

    public Timestamp getRushHourIgnore() {
        return rushHourIgnore;
    }

    public void setRushHourIgnore(Timestamp rushHourIgnore) {
        this.rushHourIgnore = rushHourIgnore;
    }

    public int getOvertimeIncome() {
        return overtimeIncome;
    }

    public void setOvertimeIncome(int overtimeIncome) {
        this.overtimeIncome = overtimeIncome;
    }

    public int getTipsIncome() {
        return tipsIncome;
    }

    public void setTipsIncome(int tipsIncome) {
        this.tipsIncome = tipsIncome;
    }

    public int getWorkIncome() {
        return workIncome;
    }

    public void setWorkIncome(int workIncome) {
        this.workIncome = workIncome;
    }
}
