package action.sm.model;

public class ChangeRange {

    int top;
    int bottom;
    String emote;
    double percent;
    int change;
    int countThisRange;
    int totalThisChange;

    public ChangeRange(int change) {
        this.change = change;
    }


    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    public String getEmote() {
        return emote;
    }

    public double getPercent() {
        return (double) countThisRange / totalThisChange;
    }

    public int getChange() {
        return change;
    }

    public int getCountThisRange() {
        return countThisRange;
    }

    public int getTotalThisChange() {
        return totalThisChange;
    }

    public String getLabel() {
        return String.format("%s %d to %d", emote, top, bottom);
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public void setEmote(String emote) {
        this.emote = emote;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public void setChange(int change) {
        this.change = change;
    }

    public void setCountThisRange(int countThisRange) {
        this.countThisRange = countThisRange;
    }

    public void setTotalThisChange(int totalThisChange) {
        this.totalThisChange = totalThisChange;
    }
}
