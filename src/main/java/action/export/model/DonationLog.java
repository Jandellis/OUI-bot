package action.export.model;

import java.sql.Timestamp;

public class DonationLog {
    long donation;
    Timestamp donation_time;

    public DonationLog(long donation, Timestamp donation_time) {
        this.donation_time = donation_time;
        this.donation = donation;
    }

    public long getDonation() {
        return donation;
    }

    public void setDonation(long donation) {
        this.donation = donation;
    }

    public Timestamp getDonation_time() {
        return donation_time;
    }

    public void setDonation_time(Timestamp donation_time) {
        this.donation_time = donation_time;
    }
}
