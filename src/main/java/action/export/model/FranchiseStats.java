package action.export.model;

import java.sql.Timestamp;

public class FranchiseStats {
    String name;
    Long income;
    Long sold;
    Long balance;
    Timestamp time;

    public FranchiseStats(String name, Long income, Long sold, Long balance, Timestamp time) {
        this.name = name;
        this.income = income;
        this.sold = sold;
        this.balance = balance;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public Long getIncome() {
        return income;
    }

    public Long getSold() {
        return sold;
    }

    public Long getBalance() {
        return balance;
    }

    public Timestamp getTime() {
        return time;
    }
}
