package action.reminder.model;

import action.upgrades.model.LocationEnum;

public class Boost {

    String name;
    int duration;

    LocationEnum location;

    public Boost(String name, int duration, LocationEnum location) {
        this.name = name;
        this.duration = duration;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public LocationEnum getLocation() {
        return location;
    }
}
