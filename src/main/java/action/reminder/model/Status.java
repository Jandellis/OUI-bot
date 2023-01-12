package action.reminder.model;

public enum Status {
    gordon(6,2, "gordon"),
    exe(7,3, "executive"),
    head(7,3, "head"),
    sous(7,4, "sous"),
    normal(9,4, "normal");

    private int work;
    private int tips;
    private int ot = 30;
    private int vote = 12*60;
    private int daily = 24*60;
    private int clean = 24*60;

    private String name;

    Status(int work, int tips, String name) {
        this.work = work;
        this.tips = tips;
        this.name = name;
    }

    public static Status getStatus(String name) {
        for (Status value : values()) {
            if (name.toLowerCase().contains(value.name))
                return value;
        }
        return normal;
    }

    public int getWork() {
        return work;
    }

    public int getTips() {
        return tips;
    }

    public int getOt() {
        return ot;
    }

    public int getVote() {
        return vote;
    }

    public int getDaily() {
        return daily;
    }

    public int getClean() {
        return clean;
    }
}
