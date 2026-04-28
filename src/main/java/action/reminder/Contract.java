package action.reminder;

public class Contract{
     private String name;
     private String objective;
     private String rewards;
    private double workBuff = 1;
    private double workCoolDown = 1;
    private double tipsBuff = 1;
    private double tipsCoolDown = 1;
    private double overtimeBuff = 1;
    private double overtimeCoolDown = 1;
    private int progress = 0;
    private int total = 1;

    public Contract(String name, String objective, String rewards) {
        this.name = name;
        this.objective = objective;
        this.rewards = rewards;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getRewards() {
        return rewards;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }

    public double getWorkBuff() {
        return workBuff;
    }

    public void setWorkBuff(double workBuff) {
        this.workBuff = workBuff;
    }

    public double getWorkCoolDown() {
        return workCoolDown;
    }

    public void setWorkCoolDown(double workCoolDown) {
        this.workCoolDown = workCoolDown;
    }

    public double getTipsBuff() {
        return tipsBuff;
    }

    public void setTipsBuff(double tipsBuff) {
        this.tipsBuff = tipsBuff;
    }

    public double getTipsCoolDown() {
        return tipsCoolDown;
    }

    public void setTipsCoolDown(double tipsCoolDown) {
        this.tipsCoolDown = tipsCoolDown;
    }

    public double getOvertimeBuff() {
        return overtimeBuff;
    }

    public void setOvertimeBuff(double overtimeBuff) {
        this.overtimeBuff = overtimeBuff;
    }

    public double getOvertimeCoolDown() {
        return overtimeCoolDown;
    }

    public void setOvertimeCoolDown(double overtimeCoolDown) {
        this.overtimeCoolDown = overtimeCoolDown;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "name='" + name + '\'' +
                ", objective='" + objective + '\'' +
                ", rewards='" + rewards + '\'' +
                ", workBuff=" + workBuff +
                ", workCoolDown=" + workCoolDown +
                ", tipsBuff=" + tipsBuff +
                ", tipsCoolDown=" + tipsCoolDown +
                ", overtimeBuff=" + overtimeBuff +
                ", overtimeCoolDown=" + overtimeCoolDown +
                ", progress=" + progress +
                ", total=" + total +
                '}';
    }
}
