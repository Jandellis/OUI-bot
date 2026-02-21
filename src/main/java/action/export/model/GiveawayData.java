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

    public boolean qualifiesForGiveaway() {

        int match = 0;
        if (overtime >= 30) {
            match++;
        }
        if (work >= 50) {
            match++;
        }
        if (votes >= 7) {
            match++;
        }
        return match >= 2;
    }
}
