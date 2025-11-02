package action.upgrades.model;

public enum LocationEnum {

//    shack("shack", 1, ":taco:"),
//    mall("mall", 2, ":department_store:"),
//    beach("beach", 3, ":beach_with_umbrella:"),
//    stadium("stadium", 4, ":stadium:"),
//    amusement ("amusement", 5, ":roller_coaster:"),
//    city("city", 6, ":cityscape:"),
//
//    cantina("cantina", 7, ":cactus:"),
//    hq("hq", 0, ":classical_building:"),
//    event("event", 0, "?"),
//    franchise("franchise", 0, ":office:");

    shack("shack", 1, "\uD83C\uDF2E"),
    mall("mall", 2, "\uD83C\uDFEC"),
    beach("beach", 3, "\uD83C\uDFD6"),
    stadium("stadium", 4, "\uD83C\uDFDF"),
    amusement ("amusement", 5, "\uD83C\uDFA2"),
    city("city", 6, "\uD83C\uDF06"),

    cantina("cantina", 7, "\uD83C\uDF35"),
    hq("hq", 0, "\uD83C\uDFDB"),
    event("event", 0, "\uD83C\uDF9F"),
    franchise("franchise", 0, "\uD83C\uDFE2");

    private String name;
    private int order;
    private String emote;

    private LocationEnum(String name, int order, String emote) {
        this.order = order;
        this.name = name;
        this.emote = emote;
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
        return emote + " " + name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public String getEmote() {
        return emote;
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
        if (name.equalsIgnoreCase("st") ) {
            return stadium;
        }
        for (LocationEnum value : values()) {
            if (value.name.startsWith(name.toLowerCase()))
                return value;
        }
        return null;
    }
}
