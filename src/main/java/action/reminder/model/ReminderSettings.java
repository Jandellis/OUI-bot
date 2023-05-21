package action.reminder.model;

public class ReminderSettings {

    String name;

    boolean tip;
    boolean work ;
    boolean overtime ;
    boolean vote ;
    boolean daily ;
    boolean clean ;
    boolean boost ;

    public ReminderSettings(String name, boolean tip, boolean work, boolean overtime, boolean vote, boolean daily, boolean clean, boolean boost) {
        this.name = name;
        this.tip = tip;
        this.work = work;
        this.overtime = overtime;
        this.vote = vote;
        this.daily = daily;
        this.clean = clean;
        this.boost = boost;
    }

    public ReminderSettings(String name) {
        this.name = name;
        this.tip = true;
        this.work = true;
        this.overtime = true;
        this.vote = true;
        this.daily = true;
        this.clean = true;
        this.boost = true;
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
}
