package action.reminder.model;

public class Team {

    String name;
    String team;
    boolean owner;
    boolean joined;

    public Team(String name, String team, boolean owner, boolean joined) {
        this.name = name;
        this.team = team;
        this.owner = owner;
        this.joined = joined;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
}
