package action.reminder.model;

import action.upgrades.model.LocationEnum;

import java.sql.Time;
import java.sql.Timestamp;

public class ProfileStats {
    String name;
    Long income;
    Long balance;
    LocationEnum location;
    Timestamp importTime;

    public ProfileStats(String name, Long income, Long balance, LocationEnum location, Timestamp importTime) {
        this.name = name;
        this.income = income;
        this.balance = balance;
        this.location = location;
        this.importTime = importTime;
    }

    public ProfileStats(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Long getIncome() {
        return income;
    }

    public Long getBalance() {
        return balance;
    }

    public LocationEnum getLocation() {
        return location;
    }

    public Timestamp getImportTime() {
        return importTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIncome(Long income) {
        this.income = income;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public void setLocation(LocationEnum location) {
        this.location = location;
    }

    public void setImportTime(Timestamp importTime) {
        this.importTime = importTime;
    }
}
