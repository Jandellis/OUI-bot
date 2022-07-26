package action.sm;


import java.sql.Timestamp;

public class SystemReminder {

    SystemReminderType type;
    Timestamp time;

    public SystemReminder(SystemReminderType type, Timestamp time) {
        this.type = type;
        this.time = time;
    }


    public SystemReminderType getType() {
        return type;
    }


    public Timestamp getTime() {
        return time;
    }
}
