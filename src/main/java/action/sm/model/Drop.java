package action.sm.model;

public enum Drop {
    both(-1, "both"),
    owned(0, "owned"),
    watchlist(-3, "watchlist");

    private String name;
    private int price;

    private Drop(int price, String name) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public static Drop getDrop(String name) {
        for (Drop value : values()) {
            if (name.toLowerCase().contains(value.name))
                return value;
        }
        return null;
    }
    public static Drop getDrop(int price) {
        for (Drop value : values()) {
            if (price == value.price)
                return value;
        }
        return null;
    }
}
