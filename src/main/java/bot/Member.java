package bot;

public class Member {
    Long id;
    String name;
    int income;
    int shifts;
    int weeklyShifts;
    int tips;
    long donations;
    double happy;

    public Member(Long id, String name, int income, int shifts, int weeklyShifts, int tips, long donations, double happy) {
        this.id = id;
        this.name = name;
        this.income = income;
        this.shifts = shifts;
        this.weeklyShifts = weeklyShifts;
        this.tips = tips;
        this.donations = donations;
        this.happy = happy;
    }

    public Member(String id, String name, String income, String shifts, String weeklyShifts, String tips, String donations, String happy) {
        this.id = Long.parseLong(id);
        this.name = name;
        this.income = Integer.parseInt(income);
        this.shifts = Integer.parseInt(shifts);
        this.weeklyShifts = Integer.parseInt(weeklyShifts);
        this.tips = Integer.parseInt(tips);
        this.donations = Long.parseLong(donations);
        this.happy = Double.parseDouble(happy);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public int getShifts() {
        return shifts;
    }

    public void setShifts(int shifts) {
        this.shifts = shifts;
    }

    public int getWeeklyShifts() {
        return weeklyShifts;
    }

    public void setWeeklyShifts(int weeklyShifts) {
        this.weeklyShifts = weeklyShifts;
    }

    public int getTips() {
        return tips;
    }

    public void setTips(int tips) {
        this.tips = tips;
    }

    public long getDonations() {
        return donations;
    }

    public void setDonations(int donations) {
        this.donations = donations;
    }

    public double getHappy() {
        return happy;
    }

    public void setHappy(double happy) {
        this.happy = happy;
    }
}

