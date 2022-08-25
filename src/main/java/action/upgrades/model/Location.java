package action.upgrades.model;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Location {

    LocationEnum name;
    HashMap<String, Upgrade> upgrades;

    public Location(LocationEnum name) {
        this.name = name;
        this.upgrades = new HashMap<>();
    }

    public void addUpgrade(String name, String fullName, int boost, int cost, int max) {
        upgrades.put(name, new Upgrade(name, fullName, boost, cost, max));
    }
    public void addUpgrade(String name, String fullName, int boost, int cost, int max, boolean optional) {
        Upgrade upgrade = new Upgrade(name, fullName, boost, cost, max);
        upgrade.setOptional(optional);
        upgrades.put(name, upgrade);
    }

    public int getCost(String name, int position) {
        if (!upgrades.containsKey(name)) {
            return -2;
        }
        Upgrade upgrade = upgrades.get(name);
        if ( upgrade.max < position) {
            return -1;
        }

        return upgrade.cost * position * position;
    }

    public LocationEnum getName() {
        return name;
    }

    public HashMap<String, Upgrade> getUpgrades() {
        return upgrades;
    }

    public Upgrade getUpgrade(String line){
        AtomicReference<Upgrade> upgradeItem = new AtomicReference<>();

        upgrades.forEach((name, upgrade) -> {
            if (line.contains("**"+upgrade.fullName+"**")) {
                upgradeItem.set(upgrade);
                //**Booth** `(13/35)`
                String position = line.split("`")[1].split("/")[0].substring(1);
                upgradeItem.get().setPosition(Integer.parseInt(position));

            }
        });
        return upgradeItem.get();
    }


}
