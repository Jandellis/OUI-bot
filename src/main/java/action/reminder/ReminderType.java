package action.reminder;

public enum ReminderType {
    work("work"),
    tips("tips"),
    ot("ot"),
    vote("vote"),
    daily("daily"),
    clean("clean");


    String name;

    ReminderType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ReminderType getReminderType(String name) {
        for (ReminderType value : values()) {
            if (name.toLowerCase().contains(value.name))
                return value;
        }
        return null;
    }
}
