package action.reminder.model;

public enum ContractType {
    DOUBLE_TIME_SPECIALIST("Double Time Specialist", ContractActionType.overtimeWork),
    VIP_SERVICE("VIP Service", ContractActionType.tips),
    VIP_CATERING("VIP Catering", ContractActionType.work),
    VIP_OVERTIME("VIP Overtime", ContractActionType.overtime),
    SERVICE_SPECIALIST("Service Specialist", ContractActionType.tips),
    SHIFT_SPECIALIST("Shift Specialist", ContractActionType.work),
    OVERTIME_SPECIALIST("Overtime Specialist", ContractActionType.overtime),
    CORPORATE_RESTRUCTURING("Corporate Restructuring", ContractActionType.all),
    UNKNOWN("Unknown", null);

    private final String name;
    private final ContractActionType action;

    ContractType(String name, ContractActionType action) {
        this.name = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public ContractActionType getAction() {
        return action;
    }

    public static ContractType fromName(String name) {
        for (ContractType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}