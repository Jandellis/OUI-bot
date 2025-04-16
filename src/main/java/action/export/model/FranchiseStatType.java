package action.export.model;

import action.sm.model.SystemReminderType;

public enum FranchiseStatType {


    balance("balance"),
    income("income"),
    sold("sold"),
    donations("donations"),
    ot_estimate("ot_estimate"),
    vote_estimate("vote_estimate");


    String name;

    FranchiseStatType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static FranchiseStatType getReminderType(String name) {
        for (FranchiseStatType value : values()) {
            if (name.toLowerCase().contains(value.name.toLowerCase()))
                return value;
        }
        return null;
    }
}
