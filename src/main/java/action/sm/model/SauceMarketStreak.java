package action.sm.model;

public class SauceMarketStreak {

    String name;
    int length;
    String direction;

    public SauceMarketStreak(String name, int length, String direction) {
        this.name = name;
        this.length = length;
        this.direction = direction;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public String getDirection() {
        return direction;
    }
}
