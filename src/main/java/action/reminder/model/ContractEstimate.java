package action.reminder.model;

public class ContractEstimate {

    int min = 0;
    int work = 0;
    int overtime = 0;
    int tips = 0;

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getWork() {
        return work;
    }

    public void setWork(int work) {
        this.work = work;
    }

    public int getOvertime() {
        return overtime;
    }

    public void setOvertime(int overtime) {
        this.overtime = overtime;
    }

    public int getTips() {
        return tips;
    }

    public void setTips(int tips) {
        this.tips = tips;
    }

    public void addWork(){
        work++;
    }
    public void addOvertime(){
        overtime++;
    }
    public void addTips(){
        tips++;
    }

    @Override
    public String toString() {
        return "ContractEstimate{" +
                "min=" + min +
                ", work=" + work +
                ", overtime=" + overtime +
                ", tips=" + tips +
                '}';
    }
}
