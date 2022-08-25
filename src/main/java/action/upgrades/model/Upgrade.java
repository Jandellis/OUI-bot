package action.upgrades.model;

public class Upgrade {
    String name;
    String fullName;
    int cost;
    int boost;
    int max;
    Integer position;
    boolean optional = false;

    public Upgrade(String name, String fullName, int boost, int cost, int max) {
        this.name = name;
        this.fullName = fullName;
        this.cost = cost;
        this.boost = boost;
        this.max = max;
        this.position = null;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getMax() {
        return max;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getBoost() {
        return boost;
    }
}
