package action.reminder;

public enum ReminderType {
    work("work", false),
    grind("grind", false),
    tips("tips", false),
    ot("overtime", false),
    vote("vote", false),
    daily("daily", false),
    clean("clean", false),
    gift("gifts", false),
    importData("importData", false),
    postAd("postAd", false),

    //city
    happy("Happy Hour"),
    samples("Samples"),
    Mascot("Mascot"),
    Delivery("Online Delivery"),
    Bus("Bus Sign"),

    //shack
    Chef("Rent-A-Chef"),
    Music("Live Music"),
    Karaoke("Karaoke Night"),
    Flipper("Sign Flipper"),
    Airplane("Airplane Sign"),


    //beach
    Concert("Concert"),
    Hammock("Hammock"),
    Parasailing("Parasailing"),
    Chairs("Beach Chairs"),
    Helicopter("Helicopter Tours"),

    //Amusement
    Gift("Gift Shop"),
    Painting("Face Painting"),
    Parade("Parade"),
    Show("Live Show"),
    Magic("Magic Show"),

    //mall
    Lunch("Lunch Discount"),
    special("Special"),
    sponsor("Sponsorship"),
    cards("Gift Cards"),
    takeout("Takeout"),

    //Cantina
    margarita("Margarita Bar"),
    taco("Taco Bar"),
    poker("Poker Night"),
    disco("Disco Night"),
    jukebox("Jukebox"),

    //event
    flyers("Flyers", false),
    twirler("Sign Twirler", false),
    refreshments("Refreshments", false),
    music("Music", false),
    festival("Festival", false),
    eventClean("Event Clean", false),
    eventOvertime("Event Overtime", false),

    //franchise
    rewards("Rewards", false),
    menu("Menu", false),
    training("Training", false),
    survey("Survey", false),
    incentives("Incentives", false),
    franchiseTasks("Franchise Tasks", false);


    String name;
    boolean boost;

    ReminderType(String name) {
        this.name = name;
        this.boost = true;
    }

    ReminderType(String name, boolean boost) {
        this.name = name;
        this.boost = boost;
    }

    public String getName() {
        return name;
    }

    public static ReminderType getReminderType(String name) {
        if (name.toLowerCase().equals("event clean"))
            return eventClean;
        for (ReminderType value : values()) {
            if (name.toLowerCase().contains(value.name.toLowerCase()))
                return value;
        }
        return null;
    }
}
