package action.reminder.model;

public class Stats {
    String id;
    int work;
    int tips;
    int ot;
    int total = 0;

    public Stats(String id, int work, int tips, int ot) {
        this.id = id;
        this.work = work;
        this.tips = tips;
        this.ot = ot;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public int getOt() {
        return ot;
    }

    public void setOt(int ot) {
        this.ot = ot;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
