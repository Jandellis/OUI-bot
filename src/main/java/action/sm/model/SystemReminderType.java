package action.sm.model;

public enum SystemReminderType {
    sauce("sauce"),
    speedJarUnlock("speedJarUnlock"),
    speedJarLock("speedJarLock"),
    giveaway("giveaway");


    String name;

    SystemReminderType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SystemReminderType getReminderType(String name) {
        for (SystemReminderType value : values()) {
            if (name.toLowerCase().contains(value.name.toLowerCase()))
                return value;
        }
        return null;
    }
}
