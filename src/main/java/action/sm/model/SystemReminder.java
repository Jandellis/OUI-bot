package action.sm.model;


import action.sm.model.SystemReminderType;

import java.sql.Timestamp;

public class SystemReminder {

    SystemReminderType type;
    Timestamp time;
    String messageId;
    String name;

    public SystemReminder(SystemReminderType type, Timestamp time, String messageId, String name) {
        this.type = type;
        this.time = time;
        this.messageId = messageId;
        this.name = name;
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

    public String getName() {
        return name;
    }
}
