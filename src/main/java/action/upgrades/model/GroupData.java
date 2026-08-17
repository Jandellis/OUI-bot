package action.upgrades.model;

public class GroupData {
    public String category;
    public String boost;
    public long totalAmount = 0;
    public long count = 0;

    public GroupData(String category, String boost) {
        this.category = category;
        this.boost = boost;
    }

    public void addAmount(int amount) {
        totalAmount += amount;
        count++;
    }
}