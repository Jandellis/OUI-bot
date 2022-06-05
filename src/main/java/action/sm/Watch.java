package action.sm;

import bot.Sauce;

public class Watch {

    String name;
    Sauce sauce;

    public Watch(String name, Sauce sauce) {
        this.name = name;
        this.sauce = sauce;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sauce getSauce() {
        return sauce;
    }

    public void setSauce(Sauce sauce) {
        this.sauce = sauce;
    }
}
