package action.upgrades.model;

public enum LocationEnum {

    shack("shack", 1),
    mall("mall", 2),
    beach("beach", 3),
    amusement ("amusement", 4),
    city("city", 5),

    cantina("cantina", 6),
    hq("hq", 0),
    event("event", 0),
    franchise("franchise", 0);

    private String name;
    private int order;

    private LocationEnum(String name, int order) {
        this.order = order;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public String getPrintName() {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
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
