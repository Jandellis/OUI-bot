package action.reminder.model;

public class ReminderTimes {
    int work;
    int tips;
    int overtime;

    public int getWork() {
        return work;
    }

    public void setWork(int work) {
        this.work = work;
    }

    public int getTips() {
        return tips;
    }

    public void setTips(int tips) {
        this.tips = tips;
    }

    public int getOvertime() {
        return overtime;
    }

    public void setOvertime(int overtime) {
        this.overtime = overtime;
    }

    @Override
    public String toString() {
        return "ReminderTimes{" +
                "work=" + work +
                ", tips=" + tips +
                ", overtime=" + overtime +
                '}';
    }
}
