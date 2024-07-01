package action;

import java.util.HashMap;

public class SpeedJarDetails {

    private static HashMap<String, SpeedJarDetails> map = new HashMap();

    private boolean active;
    private int messages;

    public static SpeedJarDetails getSpeedJarDetails(String channel) {
        if (!map.containsKey(channel)) {
            SpeedJarDetails details = new SpeedJarDetails();
            map.put(channel, details);
        }
        return map.get(channel);
    }

    private SpeedJarDetails() {
        active = false;
        messages = 0;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getMessages() {
        return messages;
    }

    public void addMessage() {
        messages++;
    }
    public void resetMessages() {
        messages = 0;
    }
}
