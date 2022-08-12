package action.reminder;

import java.sql.Timestamp;

public class Reminder {

    String name;
    ReminderType type;
    String channel;
    Timestamp time;
    long id;

    public Reminder(String name, ReminderType type, Timestamp time, String channel) {
        this.name = name;
        this.type = type;
        this.channel = channel;
        this.time = time;
        id = -1;
    }

    public String getName() {
        return name;
    }

    public ReminderType getType() {
        return type;
    }

    public String getChannel() {
        return channel;
    }

    public Timestamp getTime() {
        return time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
