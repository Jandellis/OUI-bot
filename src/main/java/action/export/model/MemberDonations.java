package action.export.model;

public class MemberDonations {
    private long donation;
    private String name;

    public MemberDonations(long donation, String name) {
        this.donation = donation;
        this.name = name;
    }

    public void setDonation(long donation) {
        this.donation = donation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDonation() {
        return donation;
    }

    public String getName() {
        return name;
    }

}
