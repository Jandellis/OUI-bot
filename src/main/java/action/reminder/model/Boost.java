package action.reminder.model;

import action.upgrades.model.LocationEnum;

public class Boost {

    String name;
    double duration;

    LocationEnum location;

    public Boost(String name, double duration, LocationEnum location) {
        this.name = name;
        this.duration = duration;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public double getDuration() {
        return duration;
    }

    public LocationEnum getLocation() {
        return location;
    }
}
