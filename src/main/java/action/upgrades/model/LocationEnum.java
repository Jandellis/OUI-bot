package action.upgrades.model;

import bot.Sauce;

public enum LocationEnum {

    mall("mall"),
    city("city"),
    shack("shack"),
    beach("beach"),
    hq("hq");

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
        for (LocationEnum value : values()) {
            if (value.name.startsWith(name.toLowerCase()))
                return value;
        }
        return null;
    }
}
