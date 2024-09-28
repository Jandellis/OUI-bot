package action.reminder;

public enum ReminderType {
    work("work"),
    grind("grind"),
    tips("tips"),
    ot("overtime"),
    vote("vote"),
    daily("daily"),
    clean("clean"),
    gift("gifts"),
    importData("importData"),
    postAd("postAd"),

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
    flyers("Flyers"),
    twirler("Sign Twirler"),
    refreshments("Refreshments"),
    music("Music"),
    festival("Festival"),
    eventClean("Event Clean"),
    eventOvertime("Event Overtime");


    String name;

    ReminderType(String name) {
        this.name = name;
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
