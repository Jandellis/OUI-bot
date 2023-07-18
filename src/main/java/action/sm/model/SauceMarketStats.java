package action.sm.model;

public class SauceMarketStats {

    int change;
    double zero;
    double positive;
    double negative;
    int occurred;

    public SauceMarketStats(int change, double zero, double positive, double negative, int occurred) {
        this.change = change;
        this.zero = zero;
        this.positive = positive;
        this.negative = negative;
        this.occurred = occurred;
    }

    public int getChange() {
        return change;
    }

    public void setChange(int change) {
        this.change = change;
    }

    public double getZero() {
        return zero;
    }

    public void setZero(double zero) {
        this.zero = zero;
    }

    public double getPositive() {
        return positive;
    }

    public void setPositive(double positive) {
        this.positive = positive;
    }

    public double getNegative() {
        return negative;
    }

    public void setNegative(double negative) {
        this.negative = negative;
    }

    public int getOccurred() {
        return occurred;
    }

    public void setOccurred(int occurred) {
        this.occurred = occurred;
    }
}
