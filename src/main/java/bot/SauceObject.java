package bot;

public class SauceObject {

    Sauce sauce;

    int oldPrice;

    int price;

    public SauceObject(Sauce sauce, int oldPrice, int price) {
        this.sauce = sauce;
        this.oldPrice = oldPrice;
        this.price = price;
    }

    public Sauce getSauce() {
        return sauce;
    }

    public void setSauce(Sauce sauce) {
        this.sauce = sauce;
    }

    public int getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(int oldPrice) {
        this.oldPrice = oldPrice;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
