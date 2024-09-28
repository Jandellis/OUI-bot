package action.upgrades.model;

import bot.Sauce;

public enum LocationEnum {

    shack("shack"),
    mall("mall"),
    beach("beach"),
    amusement ("amusement"),
    city("city"),

    cantina("cantina"),
    hq("hq"),
    event("event");

    private String name;

    private LocationEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public static LocationEnum getLocation(String name) {
        if (name.equalsIgnoreCase("taco")) {
            return shack;
        }
        if (name.equalsIgnoreCase("p") || name.equalsIgnoreCase("park") ) {
            return amusement;
        }
        if (name.equalsIgnoreCase("ca") ) {
            return cantina;
        }
        for (LocationEnum value : values()) {
            if (value.name.startsWith(name.toLowerCase()))
                return value;
        }
        return null;
    }
}
