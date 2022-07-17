package action.reminder;

public class Profile {

    String name;
    String shackName;
    Status status;

    public Profile(String name, String shackName, Status status) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getShackName() {
        return shackName;
    }

    public Status getStatus() {
        return status;
    }

    // emabled
    // emote
    // message
}
