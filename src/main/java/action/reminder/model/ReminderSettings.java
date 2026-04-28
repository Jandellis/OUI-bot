package action.reminder.model;

public class ReminderSettings {

    String name;

    boolean tip;
    boolean work;
    boolean grind;
    boolean overtime;
    boolean vote;
    boolean daily;
    boolean clean;
    boolean boost;
    double workModifier;
    double overtimeModifier;
    double tipsModifier;

    public ReminderSettings(String name, boolean tip, boolean work, boolean grind, boolean overtime, boolean vote, boolean daily, boolean clean, boolean boost, double workModifier, double overtimeModifier, double tipsModifier) {
        this.name = name;
        this.tip = tip;
        this.work = work;
        this.grind = grind;
        this.overtime = overtime;
        this.vote = vote;
        this.daily = daily;
        this.clean = clean;
        this.boost = boost;
        this.workModifier = workModifier;
        this.overtimeModifier = overtimeModifier;
        this.tipsModifier = tipsModifier;
    }

    public ReminderSettings(String name) {
        this.name = name;
        this.tip = true;
        this.work = true;
        this.grind = true;
        this.overtime = true;
        this.vote = true;
        this.daily = true;
        this.clean = true;
        this.boost = true;
        this.workModifier = 1;
        this.overtimeModifier = 1;
        this.tipsModifier = 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTip() {
        return tip;
    }

    public void setTip(boolean tip) {
        this.tip = tip;
    }

    public boolean isWork() {
        return work;
    }

    public void setWork(boolean work) {
        this.work = work;
    }

    public boolean isGrind() {
        return grind;
    }

    public void setGrind(boolean grind) {
        this.grind = grind;
    }

    public boolean isOvertime() {
        return overtime;
    }

    public void setOvertime(boolean overtime) {
        this.overtime = overtime;
    }

    public boolean isVote() {
        return vote;
    }

    public void setVote(boolean vote) {
        this.vote = vote;
    }

    public boolean isDaily() {
        return daily;
    }

    public void setDaily(boolean daily) {
        this.daily = daily;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public boolean isBoost() {
        return boost;
    }

    public void setBoost(boolean boost) {
        this.boost = boost;
    }

    public double getWorkModifier() {
        return workModifier;
    }

    public void setWorkModifier(double workModifier) {
        this.workModifier = workModifier;
    }

    public double getOvertimeModifier() {
        return overtimeModifier;
    }

    public void setOvertimeModifier(double overtimeModifier) {
        this.overtimeModifier = overtimeModifier;
    }

    public double getTipsModifier() {
        return tipsModifier;
    }

    public void setTipsModifier(double tipsModifier) {
        this.tipsModifier = tipsModifier;
    }

    @Override
    public String toString() {
        return "ReminderSettings{" +
                "name='" + name + '\'' +
                ", tip=" + tip +
                ", work=" + work +
                ", grind=" + grind +
                ", overtime=" + overtime +
                ", vote=" + vote +
                ", daily=" + daily +
                ", clean=" + clean +
                ", boost=" + boost +
                ", workModifier=" + workModifier +
                ", overtimeModifier=" + overtimeModifier +
                ", tipsModifier=" + tipsModifier +
                '}';
    }
}