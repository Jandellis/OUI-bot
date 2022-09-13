package action.reminder.model;

public class Profile {

    String name;
    String shackName;
    Status status;
    Boolean enabled;
    String emote;
    String message;
    int depth;

    public Profile(String name, String shackName, Status status) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
    }

    public Profile(String name, String shackName, Status status, Boolean enabled, String emote, String message, int depth) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
        if (enabled == null)
            enabled = false;
        this.enabled = enabled;
        this.emote = emote;
        this.message = message;
        this.depth = depth;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public String getEmote() {
        return emote;
    }

    public String getMessage() {
        return message;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    // emabled
    // emote
    // message
}
