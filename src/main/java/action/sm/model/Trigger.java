package action.sm.model;

import action.sm.model.AlertType;
import action.sm.model.Drop;

public class Trigger {

    String name;
    AlertType type;
    int price;
    Drop drop;

    public Trigger(String name, AlertType type, int price) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.drop = Drop.getDrop(price);
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Drop getDrop() {
        return drop;
    }
}
