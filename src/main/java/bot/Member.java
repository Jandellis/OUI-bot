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
    int overtime;
    int votes;

    String franchise;

    public Member(Long id, String name, int income, int shifts, int weeklyShifts, int tips, long donations, double happy, int overtime, int votes, String franchise) {
        this.id = id;
        this.name = name;
        this.income = income;
        this.shifts = shifts;
        this.weeklyShifts = weeklyShifts;
        this.tips = tips;
        this.donations = donations;
        this.happy = happy;
        this.overtime = overtime;
        this.votes = votes;
        this.franchise = franchise;
    }

    public Member(String id, String name, String income, String shifts, String weeklyShifts, String tips, String donations, String happy, String overtime, String votes, String franchise) {
        this.id = Long.parseLong(id);
        this.name = name;
        this.income = Integer.parseInt(income);
        this.shifts = Integer.parseInt(shifts);
        this.weeklyShifts = Integer.parseInt(weeklyShifts);
        this.tips = Integer.parseInt(tips);
        this.donations = Long.parseLong(donations);
        this.happy = Double.parseDouble(happy);
        this.overtime = Integer.parseInt(overtime);
        this.votes = Integer.parseInt(votes);
        this.franchise = franchise;
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

    public int getOvertime() {
        return overtime;
    }

    public void setOvertime(int overtime) {
        this.overtime = overtime;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setDonations(long donations) {
        this.donations = donations;
    }

    public String getFranchise() {
        return franchise;
    }

    public void setFranchise(String franchise) {
        this.franchise = franchise;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", income=" + income +
                ", shifts=" + shifts +
                ", weeklyShifts=" + weeklyShifts +
                ", tips=" + tips +
                ", donations=" + donations +
                ", happy=" + happy +
                ", overtime=" + overtime +
                ", votes=" + votes +
                '}';
    }
}

