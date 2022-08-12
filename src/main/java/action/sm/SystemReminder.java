package action.sm;


import java.sql.Timestamp;

public class SystemReminder {

    SystemReminderType type;
    Timestamp time;
    String messageId;

    public SystemReminder(SystemReminderType type, Timestamp time, String messageId) {
        this.type = type;
        this.time = time;
        this.messageId = messageId;
    }


    public SystemReminderType getType() {
        return type;
    }


    public Timestamp getTime() {
        return time;
    }

    public String getMessageId() {
        return messageId;
    }
}
