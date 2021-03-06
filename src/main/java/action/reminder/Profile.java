package action.reminder;

public class Profile {

    String name;
    String shackName;
    Status status;
    Boolean enabled;
    String emote;
    String message;

    public Profile(String name, String shackName, Status status) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
    }

    public Profile(String name, String shackName, Status status, Boolean enabled, String emote, String message) {
        this.name = name;
        this.shackName = shackName;
        this.status = status;
        if (enabled == null)
            enabled = false;
        this.enabled = enabled;
        this.emote = emote;
        this.message = message;
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

    // emabled
    // emote
    // message
}
