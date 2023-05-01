package action.export.model;

public class GiveawayData {

    long id;
    int votes;
    int overtime;
    int work;

    public GiveawayData(long id, int votes, int overtime, int work) {
        this.id = id;
        this.votes = votes;
        this.overtime = overtime;
        this.work = work;
    }

    public long getId() {
        return id;
    }

    public int getVotes() {
        return votes;
    }

    public int getOvertime() {
        return overtime;
    }

    public int getWork() {
        return work;
    }
}
