package action.reminder;

public class Contract{
     private String name;
     private String objective;
     private String rewards;

    public Contract(String name, String objective, String rewards) {
        this.name = name;
        this.objective = objective;
        this.rewards = rewards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getRewards() {
        return rewards;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }
}
