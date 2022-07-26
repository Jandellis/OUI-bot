package action.reminder;

public class Boost {

    String name;
    int duration;

    public Boost(String name, int duration) {
        this.name = name;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }
}
