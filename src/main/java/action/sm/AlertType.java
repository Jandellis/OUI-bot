package action.sm;

public enum AlertType {
    high("high"),
    drop("drop"),
    low("low");

    private String name;

    private AlertType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static AlertType getAlertType(String name) {
        for (AlertType value : values()) {
            if (name.toLowerCase().contains(value.name))
                return value;
        }
        return null;
    }
}
