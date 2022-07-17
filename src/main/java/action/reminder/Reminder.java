package action.reminder;

import java.sql.Timestamp;

public class Reminder {

    String name;
    ReminderType type;
    String channel;
    Timestamp time;

    public Reminder(String name, ReminderType type, Timestamp time, String channel) {
        this.name = name;
        this.type = type;
        this.channel = channel;
        this.time = time;
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
}
