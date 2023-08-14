package action.export.model;

public class Franchise {
    String guild;
    String name;
    String warning;
    String warning2;
    String warning3;
    String flex;
    String recruiter;
    String immunity;
    String giveawayRole;
    String court;

    public Franchise(String guild, String name, String warning, String warning2, String warning3, String flex, String recruiter, String immunity, String giveawayRole, String court) {
        this.guild = guild;
        this.name = name;
        this.warning = warning;
        this.warning2 = warning2;
        this.warning3 = warning3;
        this.flex = flex;
        this.recruiter = recruiter;
        this.immunity = immunity;
        this.giveawayRole = giveawayRole;
        this.court = court;
    }

    public String getGuild() {
        return guild;
    }

    public void setGuild(String guild) {
        this.guild = guild;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getWarning2() {
        return warning2;
    }

    public void setWarning2(String warning2) {
        this.warning2 = warning2;
    }

    public String getWarning3() {
        return warning3;
    }

    public void setWarning3(String warning3) {
        this.warning3 = warning3;
    }

    public String getFlex() {
        return flex;
    }

    public void setFlex(String flex) {
        this.flex = flex;
    }

    public String getRecruiter() {
        return recruiter;
    }

    public void setRecruiter(String recruiter) {
        this.recruiter = recruiter;
    }

    public String getImmunity() {
        return immunity;
    }

    public void setImmunity(String immunity) {
        this.immunity = immunity;
    }

    public String getGiveawayRole() {
        return giveawayRole;
    }

    public void setGiveawayRole(String giveawayRole) {
        this.giveawayRole = giveawayRole;
    }

    public String getCourt() {
        return court;
    }

    public void setCourt(String court) {
        this.court = court;
    }
}
