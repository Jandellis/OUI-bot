package action.reminder.model;

public enum ContractActionType {
    work,
    tips,
    overtime,
    overtimeWork,
    all;

    public String getName() {
        if (this == all) {
            return "work, tips and overtime";
        }
        if (this == overtimeWork) {
            return "work and overtime";
        }
        return this.name();
    }
}