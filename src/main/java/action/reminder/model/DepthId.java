package action.reminder.model;

public class DepthId {

    String id;
    int depth;

    public DepthId(String id, int depth) {
        this.id = id;
        this.depth = depth;
    }

    public String getId() {
        return id;
    }

    public int getDepth() {
        return depth;
    }
}
