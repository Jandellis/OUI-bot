package action.reminder.model;

public enum RewardType {
    COUPON("Coupon"),
    GOLDEN_TICKET("Golden Ticket"),
    LUNCH_RUSH("Lunch Rush"),
    CASH("$");

    private String type;

    RewardType(String type) {
        this.type = type;
    }

    public String getType() {
        if (type.equals("$"))
            return "Cash";
        return type;
    }

    public static RewardType fromString(String text) {
        for (RewardType r : values()) {
            if (text.contains(r.type)) {
                return r;
            }
        }
        return null;
    }
}
