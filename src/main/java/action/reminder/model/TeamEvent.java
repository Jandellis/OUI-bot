package action.reminder.model;

public class TeamEvent {

    private String reminder;
    private String id1;
    private String id2;
    private String id3;
    private String id4;


    public TeamEvent(String reminder, String id1, String id2, String id3, String id4) {
        this.reminder = reminder;
        this.id1 = id1;
        this.id2 = id2;
        this.id3 = id3;
        this.id4 = id4;
    }

    public String getReminder() {
        return reminder;
    }

    public String getId1() {
        return id1;
    }

    public String getId2() {
        return id2;
    }

    public String getId3() {
        return id3;
    }

    public String getId4() {
        return id4;
    }
}
