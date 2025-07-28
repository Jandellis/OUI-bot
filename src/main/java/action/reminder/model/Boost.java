package action.reminder.model;

import action.upgrades.model.LocationEnum;

public class Boost {

    String name;
    double duration;

    LocationEnum location;
    int order;

    public Boost(String name, double duration, LocationEnum location, int order) {
        this.name = name;
        this.duration = duration;
        this.location = location;
        this.order = order;
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

    public int getOrder() {
        return order;
    }
}
