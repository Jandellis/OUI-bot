package bot;

public enum Sauce {
    salsa("salsa"),
    pico("pico"),
    hotsauce("hotsauce"),
    guacamole("guacamole"),
    chipotle("chipotle");

    private String name;

    private Sauce(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Sauce getSauce(String name) {
        if (name.equals("guac")) {
            name = "guacamole";
        }
        for (Sauce value : values()) {
            if (name.toLowerCase().contains(value.name))
                return value;
        }
        return null;
    }
}
