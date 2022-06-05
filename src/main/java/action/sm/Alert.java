package action.sm;

public class Alert {

    String name;
    AlertType type;
    String trigger;
    int price;
    Drop drop;

    public Alert(String name, AlertType type, String trigger, int price) {
        this.name = name;
        this.type = type;
        this.trigger = trigger;
        this.price = price;
        this.drop = Drop.getDrop(price);
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public Drop getDrop() {
        return drop;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", trigger='" + trigger + '\'' +
                ", price=" + price +
                ", drop=" + drop +
                '}';
    }
}
