package action.reminder.model;

public class Reward {
    int amount;
    RewardType type;

    public Reward(int amount, RewardType type) {
        this.amount = amount;
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public RewardType getType() {
        return type;
    }
}