package action.reminder.model;

public enum ContractType {
    DOUBLE_TIME_SPECIALIST("Double Time Specialist", ContractActionType.overtimeWork),
    VIP_SERVICE("VIP Service", ContractActionType.tips),
    VIP_CATERING("VIP Catering", ContractActionType.work),
    VIP_OVERTIME("VIP Overtime", ContractActionType.overtime),
    VIP("VIP", null),
    SERVICE_SPECIALIST("Service Specialist", ContractActionType.tips),
    SHIFT_SPECIALIST("Shift Specialist", ContractActionType.work),
    OVERTIME_SPECIALIST("Overtime Specialist", ContractActionType.overtime),
    SPECIALIST("Specialist", null),
    CORPORATE_RESTRUCTURING("Corporate Restructuring", ContractActionType.all),
    CATERING_CONTRACTS("Catering Contracts", null),
    ACTIVE_CONTRACT("Active Contract", null),
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
        if (name.contains(CORPORATE_RESTRUCTURING.getName())) {
            return CORPORATE_RESTRUCTURING;
        }
        return UNKNOWN;
    }
}