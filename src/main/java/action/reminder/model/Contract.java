package action.reminder.model;

import java.util.ArrayList;
import java.util.List;

public class Contract{
     private String name;
     private String objective;
     private String rewards;
     private int rep;
    private double workBuff = 1;
    private double workCoolDown = 1;
    private double tipsBuff = 1;
    private double tipsCoolDown = 1;
    private double overtimeBuff = 1;
    private double overtimeCoolDown = 1;
    private int progress = 0;
    private int total = 1;

    private List<String[]> participants = new ArrayList<>(); // [userId, count, percent]
    private List<String> rewardList = new ArrayList<>();

    public Contract(String name, String objective, String rewards) {
        this.name = name;
        this.objective = objective;
        this.rewards = rewards;
    }
    public Contract(String name, String objective, String rewards, int rep, int total) {
        this.name = name;
        this.objective = objective;
        this.rewards = rewards;
        this.rep = rep;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public boolean isContractType(ContractType type){
        return name.contains(type.getName());

    }

    public ContractActionType getActionType() {
        ContractType contractType = ContractType.fromName(name);
        return contractType.getAction();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getRewards() {
        return rewards;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
    }

    public double getWorkBuff() {
        return workBuff;
    }

    public void setWorkBuff(double workBuff) {
        this.workBuff = workBuff;
    }

    public double getWorkCoolDown() {
        return workCoolDown;
    }

    public void setWorkCoolDown(double workCoolDown) {
        this.workCoolDown = workCoolDown;
    }

    public double getTipsBuff() {
        return tipsBuff;
    }

    public void setTipsBuff(double tipsBuff) {
        this.tipsBuff = tipsBuff;
    }

    public double getTipsCoolDown() {
        return tipsCoolDown;
    }

    public void setTipsCoolDown(double tipsCoolDown) {
        this.tipsCoolDown = tipsCoolDown;
    }

    public double getOvertimeBuff() {
        return overtimeBuff;
    }

    public void setOvertimeBuff(double overtimeBuff) {
        this.overtimeBuff = overtimeBuff;
    }

    public double getOvertimeCoolDown() {
        return overtimeCoolDown;
    }

    public void setOvertimeCoolDown(double overtimeCoolDown) {
        this.overtimeCoolDown = overtimeCoolDown;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getRep() {
        return rep;
    }

    public void setRep(int rep) {
        this.rep = rep;
    }

    public List<String[]> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String[]> participants) {
        this.participants = participants;
    }

    public List<String> getRewardList() {
        return rewardList;
    }

    public void setRewardList(List<String> rewardList) {
        this.rewardList = rewardList;
    }

    public int participantCount(){
        if (participants.isEmpty()){
            return 1;
        }
        return participants.size();
    }

    @Override
    public String toString() {
        return "Contract{" +
                "name='" + name + '\'' +
                ", objective='" + objective + '\'' +
                ", rewards='" + rewards + '\'' +
                ", rep=" + rep +
                ", workBuff=" + workBuff +
                ", workCoolDown=" + workCoolDown +
                ", tipsBuff=" + tipsBuff +
                ", tipsCoolDown=" + tipsCoolDown +
                ", overtimeBuff=" + overtimeBuff +
                ", overtimeCoolDown=" + overtimeCoolDown +
                ", progress=" + progress +
                ", total=" + total +
                ", participants=" + participants +
                ", rewardList=" + rewardList +
                '}';
    }
}
