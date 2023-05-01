package action.reminder.model;

import action.upgrades.model.LocationEnum;

import java.sql.Timestamp;

public class FlexStats {
    String name;
    Long work;
    Long tips;
    Long votes;
    Long overtime;
    Long donations;
    Timestamp importTime;

    String shackName;


    public FlexStats(Timestamp importTime, String name, String shackName, Long work, Long tips, Long donations, Long votes, Long overtime) {
        this.name = name;
        this.work = work;
        this.tips = tips;
        this.importTime = importTime;
        this.shackName = shackName;
        this.donations = donations;
        this.votes = votes;
        this.overtime = overtime;
    }

    public FlexStats(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getWork() {
        return work;
    }

    public void setWork(Long work) {
        this.work = work;
    }

    public Long getTips() {
        return tips;
    }

    public void setTips(Long tips) {
        this.tips = tips;
    }

    public Timestamp getImportTime() {
        return importTime;
    }

    public void setImportTime(Timestamp importTime) {
        this.importTime = importTime;
    }

    public String getShackName() {
        return shackName;
    }

    public void setShackName(String shackName) {
        this.shackName = shackName;
    }

    public Long getDonations() {
        return donations;
    }

    public void setDonations(Long donations) {
        this.donations = donations;
    }

    public Long getVotes() {
        return votes;
    }

    public void setVotes(Long votes) {
        this.votes = votes;
    }

    public Long getOvertime() {
        return overtime;
    }

    public void setOvertime(Long overtime) {
        this.overtime = overtime;
    }
}
