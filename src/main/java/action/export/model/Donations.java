package action.export.model;

public class Donations {


    private long maxDonation;
    private long minDonation;
    private String role;

    public Donations(long maxDonation, long minDonation, String role) {
        this.maxDonation = maxDonation;
        this.minDonation = minDonation;
        this.role = role;
    }

    public long getMaxDonation() {
        return maxDonation;
    }

    public long getMinDonation() {
        return minDonation;
    }

    public String getRole() {
        return role;
    }
}
