package action.upgrades;

import action.Action;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
import action.upgrades.model.Location;
import action.upgrades.model.LocationEnum;
import action.upgrades.model.Upgrade;
import action.upgrades.model.UserUpgrades;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BuyUpgrade extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    //    String defaultReact = "\uD83D\uDC4B";
    Location mall = new Location(LocationEnum.mall);
    Location city = new Location(LocationEnum.city);
    Location shack = new Location(LocationEnum.shack);
    Location beach = new Location(LocationEnum.beach);
    Location hq = new Location(LocationEnum.hq);
    List<Location> locations = new ArrayList<>();

    String paramUp;
    String paramStats;
    String paramUpLimit;

    public BuyUpgrade() {
        paramUp = "cyUp";
        paramStats = "cyStats";
        paramUpLimit = "cyLimitUp";


        //hire
        mall.addUpgrade("cashier", "Cashier", 10, 250, 35);
        mall.addUpgrade("associate", "Inventory Associate", 15, 500, 35);
        mall.addUpgrade("janitor", "Janitor", 20, 1000, 35);
        mall.addUpgrade("security", "Security Guard", 25, 2000, 30);
        mall.addUpgrade("sales", "Sales Associate", 40, 2500, 35);
        mall.addUpgrade("leader", "Team Leader", 65, 3500, 35);
        mall.addUpgrade("manager", "Store Manager", 150, 5000, 35);
        //deco
        mall.addUpgrade("chair", "Chair", 5, 150, 40);
        mall.addUpgrade("booth", "Booth", 10, 500, 35);
        mall.addUpgrade("display", "Display", 30, 2500, 30);
        mall.addUpgrade("buffet", "Buffet", 100, 50000, 6);
        mall.addUpgrade("screen", "Flat Screen", 400, 2000000, 4);
        //ad
        mall.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 35);
        mall.addUpgrade("radio", "Radio Ad", 20, 650, 30);
        mall.addUpgrade("email", "Email Campaign", 30, 1000, 30);
        mall.addUpgrade("internet", "Internet Ad", 50, 2000, 35);
        mall.addUpgrade("tv", "TV Commercial", 160, 5500, 15);
        mall.addUpgrade("blimp", "Advertising Blimp", 200, 250000, 3);
        //up
        mall.addUpgrade("paint", "New Paint", 10, 250, 35);
        mall.addUpgrade("furniture", "New Furniture", 20, 600, 30);
        mall.addUpgrade("bathrooms", "Nicer Bathrooms", 25, 800, 30);
        mall.addUpgrade("billboard", "Billboard", 35, 1000, 25);
        mall.addUpgrade("appliances", "Better Appliances", 90, 1200, 30);
        mall.addUpgrade("tipjar", "Cooler Tip Jar", 40, 500, 35);
        //kiosk
        mall.addUpgrade("taco", "Taco Shop", 50, 7500, 20, true);
        mall.addUpgrade("repair", "Phone Repair", 100, 15000, 15, true);
        mall.addUpgrade("froyo", "Froyo Shop", 250, 50000, 15, true);
        mall.addUpgrade("photo", "Photo Booths", 400, 150000, 10, true);
        mall.addUpgrade("merch", "TacoShack Merch", 2500, 5000000, 1, true);

        locations.add(mall);

        //ad
        city.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 45);
        city.addUpgrade("radio", "Radio Ad", 20, 650, 40);
        city.addUpgrade("email", "Email Campaign", 30, 1000, 40);
        city.addUpgrade("internet", "Internet Ad", 50, 2000, 45);
        city.addUpgrade("tv", "TV Commercial", 160, 5500, 30);
        city.addUpgrade("blimp", "Advertising Blimp", 200, 250000, 5);
        //up
        city.addUpgrade("paint", "New Paint", 10, 250, 45);
        city.addUpgrade("furniture", "New Furniture", 20, 600, 40);
        city.addUpgrade("bathrooms", "Nicer Bathrooms", 25, 800, 40);
        city.addUpgrade("billboard", "Billboard", 35, 1000, 40);
        city.addUpgrade("appliances", "Better Appliances", 90, 1200, 30);
        city.addUpgrade("tipjar", "Cooler Tip Jar", 40, 500, 35);
        //hire
        city.addUpgrade("apprentice", "Apprentice Chef", 10, 250, 45);
        city.addUpgrade("cook", "Cook", 20, 600, 45);
        city.addUpgrade("advertiser", "Advertiser", 20, 700, 45);
        city.addUpgrade("greeter", "Greeter", 25, 800, 45);
        city.addUpgrade("sous", "Sous Chef", 40, 1200, 45);
        city.addUpgrade("head", "Head Chef", 65, 2000, 40);
        city.addUpgrade("executive", "Executive Chef", 150, 5000, 45);
        //deco
        city.addUpgrade("banner", "Banner", 5, 150, 50);
        city.addUpgrade("sign", "Neon Sign", 10, 500, 50);
        city.addUpgrade("glass", "Stained Glass", 30, 2500, 45);
        city.addUpgrade("artwork", "Artwork", 150, 100000, 8);
        city.addUpgrade("chandelier", "Chandelier", 500, 5000000, 8);
        //cart
        city.addUpgrade("buns", "Buns", 50, 7500, 30, true);
        city.addUpgrade("condiments", "Condiments", 100, 10000, 25, true);
        city.addUpgrade("beverages", "Beverages", 275, 50000, 20, true);
        city.addUpgrade("coolers", "Coolers", 450, 250000, 15, true);
        city.addUpgrade("grill", "Grill", 800, 1000000, 10, true);

        locations.add(city);


        //ad
        shack.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 35);
        shack.addUpgrade("radio", "Radio Ad", 20, 650, 30);
        shack.addUpgrade("email", "Email Campaign", 30, 1000, 30);
        shack.addUpgrade("internet", "Internet Ad", 50, 2000, 35);
        shack.addUpgrade("tv", "TV Commercial", 160, 5500, 15);
        shack.addUpgrade("blimp", "Advertising Blimp", 200, 250000, 3);
        //up
        shack.addUpgrade("paint", "New Paint", 10, 250, 35);
        shack.addUpgrade("furniture", "New Furniture", 20, 600, 30);
        shack.addUpgrade("bathrooms", "Nicer Bathrooms", 25, 800, 30);
        shack.addUpgrade("billboard", "Billboard", 35, 1000, 25);
        shack.addUpgrade("appliances", "Better Appliances", 90, 1200, 30);
        shack.addUpgrade("tipjar", "Cooler Tip Jar", 40, 500, 35);
        //hire
        shack.addUpgrade("apprentice", "Apprentice Chef", 10, 250, 35);
        shack.addUpgrade("cook", "Cook", 20, 600, 35);
        shack.addUpgrade("advertiser", "Advertiser", 20, 700, 35);
        shack.addUpgrade("greeter", "Greeter", 25, 800, 35);
        shack.addUpgrade("sous", "Sous Chef", 40, 1200, 35);
        shack.addUpgrade("head", "Head Chef", 65, 2000, 30);
        shack.addUpgrade("executive", "Executive Chef", 150, 5000, 35);
        //deco
        shack.addUpgrade("flowers", "Flowers", 5, 150, 40);
        shack.addUpgrade("ornaments", "Ornaments", 10, 500, 35);
        shack.addUpgrade("lights", "Fancy Lights", 30, 2500, 30);
        shack.addUpgrade("mural", "Mural", 100, 15000, 5);
        shack.addUpgrade("statue", "Taco Statue", 500, 5000000, 3);
        //truck
        shack.addUpgrade("register", "Register", 50, 5000, 20, true);
        shack.addUpgrade("assistant", "Assistant", 100, 10000, 15, true);
        shack.addUpgrade("driver", "Truck Driver", 250, 25000, 10, true);
        shack.addUpgrade("kitchen", "Kitchen", 400, 100000, 5, true);
        shack.addUpgrade("engine", "Engine", 1000, 1000000, 3, true);
        locations.add(shack);


        //ad
        beach.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 40);
        beach.addUpgrade("radio", "Radio Ad", 20, 650, 35);
        beach.addUpgrade("email", "Email Campaign", 30, 1000, 35);
        beach.addUpgrade("internet", "Internet Ad", 50, 2000, 10);
        beach.addUpgrade("tv", "TV Commercial", 160, 5500, 20);
        beach.addUpgrade("blimp", "Advertising Blimp", 200, 250000, 4);
        //up
        beach.addUpgrade("paint", "New Paint", 10, 250, 40);
        beach.addUpgrade("furniture", "New Furniture", 20, 600, 35);
        beach.addUpgrade("bathrooms", "Nicer Bathrooms", 25, 800, 35);
        beach.addUpgrade("billboard", "Billboard", 35, 1000, 30);
        beach.addUpgrade("appliances", "Better Appliances", 90, 1200, 30);
        beach.addUpgrade("tipjar", "Cooler Tip Jar", 40, 500, 35);
        //hire
        beach.addUpgrade("apprentice", "Apprentice Chef", 10, 250, 40);
        beach.addUpgrade("cook", "Cook", 20, 600, 40);
        beach.addUpgrade("advertiser", "Advertiser", 20, 700, 40);
        beach.addUpgrade("greeter", "Greeter", 25, 800, 40);
        beach.addUpgrade("sous", "Sous Chef", 40, 1200, 40);
        beach.addUpgrade("head", "Head Chef", 65, 2000, 35);
        beach.addUpgrade("executive", "Executive Chef", 150, 5000, 40);
        //deco
        beach.addUpgrade("shells", "Sea Shells", 5, 100, 40);
        beach.addUpgrade("umbrella", "Umbrella", 10, 250, 40);
        beach.addUpgrade("leis", "Leis", 30, 1500, 35);
        beach.addUpgrade("tanks", "Fish Tanks", 125, 20000, 5);
        beach.addUpgrade("fountain", "Taco Fountain", 500, 1500000, 3);
        //stand
        beach.addUpgrade("decals", "Decals", 50, 5000, 25, true);
        beach.addUpgrade("wheels", "Wheels", 100, 8500, 15, true);
        beach.addUpgrade("mixers", "Mixers", 250, 25000, 15, true);
        beach.addUpgrade("server", "Server", 400, 150000, 5, true);
        beach.addUpgrade("freezer", "Freezer", 750, 750000, 10, true);
        locations.add(beach);


        //Upgrades
        hq.addUpgrade("Customer Service Department", "Customer Service Department", 180, 750000, 20);
        hq.addUpgrade("Food Services Department", "Food Services Department", 180, 750000, 20);
        hq.addUpgrade("Overtime Management", "Overtime Management", 750, 3000000, 5);
        hq.addUpgrade("Lunch Rush Initiative", "Lunch Rush Initiative", 250, 2500000, 6);
        hq.addUpgrade("Task Booster", "Task Booster", 150, 10000000, 4);

        //Employees
        hq.addUpgrade("Secretary", "Secretary", 100, 1000000, 20);
        hq.addUpgrade("Treasurer", "Treasurer", 200, 1750000, 20);
        hq.addUpgrade("Chief Financial Officer", "Chief Financial Officer", 300, 2500000, 10);
        hq.addUpgrade("Chief Executive Officer", "Chief Executive Officer", 500, 5000000, 10);
        locations.add(hq);


        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in


        // add list of channels to watch
//        List<String> watchChannels = new ArrayList<>();
//        watchChannels.add("841034380822577182");
//        watchChannels.add("889662502324039690");
        AtomicBoolean watched = new AtomicBoolean(true);

//        watchChannels.forEach(channel -> {
//            if (message.getChannelId().asString().equals(channel)) {
//                watched.set(true);
//            }
//        });
        //if in watch channel
        if (watched.get()) {
            try {
                if (message.getData().author().id().asString().equals(tacoBot)) {
                    //for some reason the embeds will be empty from slash, but if i load it again it will have data
                    if (checkAge(message)) {
                        checkMessageAgain(message);
                    } else {
                        for (Embed embed : message.getEmbeds()) {
                            if (embed.getTitle().isPresent() && embed.getDescription().isPresent()) {
                                String title = embed.getTitle().get();

                                if (title.contains("Decorations") ||
                                        title.contains("Upgrades ") ||
                                        title.contains("Advertisements ") ||
                                        title.contains("Employees ") ||
                                        title.contains("Mall Kiosk") ||
                                        title.contains("Taco Truck") ||
                                        title.contains("Hotdog Cart") ||
                                        title.contains("Ice Cream Stand")) {
                                    String id = getId(message);
                                    Location location = getLocation(title, embed.getDescription().get());

                                    if (location == null) {
                                        return Mono.empty();
                                    }

                                    if (embed.getDescription().isPresent()) {
                                        String desc = embed.getDescription().get();
                                        String[] lines = desc.split("\n");
                                        List<Upgrade> upgrades = new ArrayList<>();
                                        for (String line : lines) {

                                            Upgrade upgrade = location.getUpgrade(line);
                                            if (upgrade != null) {
                                                upgrades.add(upgrade);
                                                UpgradeUtils.addUserUpgrades(new UserUpgrades(id, location.getName().getName(), upgrade.getName(), upgrade.getPosition()));
                                            }
                                        }

                                        Profile profile = ReminderUtils.loadProfileById(id);
                                        if (profile != null) {
                                            react(message, profile);
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    //message is from a user
                    //check if they did ouiup m or ouiup mall

                    String action = getAction(message, paramUp.toLowerCase());
                    if (action != null) {
                        String userId = message.getAuthor().get().getId().asString();

                        LocationEnum locationEnum = LocationEnum.getLocation(action);
                        if (locationEnum == null) {
                            return Mono.empty();
                        }

                        Location location = getLocation(locationEnum.getName(), "");

                        if (location == null) {
                            return Mono.empty();
                        }

                        List<UserUpgrades> list = UpgradeUtils.loadUserUpgrades(userId, location.getName().getName());
                        Profile profile = ReminderUtils.loadProfileById(userId);

                        if (list.isEmpty()) {

                            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                            embed.color(Color.SUMMER_SKY);
                            embed.title("Your upgrades");
                            String commands = "</hire:1006354977847001159>, </advertisements:1006354977721176137>, </upgrades:1006354978274820107>, </decorations:1006354977788268620>, ";
                            switch (locationEnum) {
                                case shack:
                                    commands = commands + "</truck:1006354978153169014>";
                                    break;
                                case mall:
                                    commands = commands + "</kiosk:1010956257588428840>";
                                    break;
                                case beach:
                                    commands = commands + "</stand:1006354978153169010>";
                                    break;
                                case city:
                                    commands = commands + "</cart:1006354977721176142>";
                                    break;
                                case hq:
                                    commands = "</hq upgrades:1018564197602295859>, </hq hire:1018564197602295859> ";
                                    break;

                            }

                            embed.addField("Error", "I have no data, please run the following command " + commands, false);


                            message.getChannel().block().createMessage(embed.build()).block();
                            return Mono.empty();
                        }

                        List<UserUpgrades> total = new ArrayList<>();
                        list.forEach(userUpgrades -> {
                            Upgrade up = location.getUpgrades().get(userUpgrades.getUpgrade());
                            int max = up.getMax();
                            int current = userUpgrades.getProgress();
                            for (int i = current; i < max; i++) {
                                UserUpgrades newUp = new UserUpgrades(userUpgrades.getName(), userUpgrades.getLocation(), userUpgrades.getUpgrade(), i + 1);
                                newUp.setCurrentCost(location.getCost(userUpgrades.getUpgrade(), i + 1));
                                newUp.setValue(newUp.getCurrentCost() / up.getBoost());
                                newUp.setBoost(up.getBoost());
                                total.add(newUp);
                            }

                        });

                        boolean cheapSort = message.getContent().toLowerCase().contains("cheap");
                        String title = "";

                        if (cheapSort) {
                            title = " - Cheapest";
                            total.sort((o1, o2) -> {
                                if (o1.getCurrentCost() == o2.getCurrentCost())
                                    return 0;
                                if (o1.getCurrentCost() < o2.getCurrentCost())
                                    return -1;
                                else
                                    return 1;
                            });
                        } else {
                            total.sort((o1, o2) -> {
                                if (o1.getValue() == o2.getValue())
                                    return 0;
                                if (o1.getValue() < o2.getValue())
                                    return -1;
                                else
                                    return 1;
                            });
                        }
                        int upgradeLimit;
                        if (profile.getUpgrade() == 0) {
                            upgradeLimit = 30;
                        } else {
                            upgradeLimit = profile.getUpgrade();
                        }

                        StringBuilder sb = new StringBuilder();
                        int count = 1;
                        long totalCost = 0;
                        int totalBoost = 0;
                        for (UserUpgrades upgrade : total) {
                            if (upgrade.getCurrentCost() > 0) {
                                String value = "";
                                value = String.format("%,d", upgrade.getCurrentCost());
                                String boost = "";
                                boost = String.format("%,d", upgrade.getBoost());
                                String line = count + " - `" + upgrade.getUpgrade() + "` - **$" + value + "**";
                                int space = 35;
                                if (location.getName() == LocationEnum.hq) {
                                    space = 60;
                                }
                                int length = space - line.length();
                                sb.append(line);
                                sb.append(" ");
                                for (int i = 0; i < length; i++) {
                                    sb.append("-");
                                }
                                Boolean fakeBoost = false;
                                if (upgrade.getUpgrade().equals("tipjar")) {
                                    boost = " more tips";
                                    fakeBoost = true;
                                }
                                if (upgrade.getUpgrade().equals("appliances")) {
                                    boost = " more work";
                                    fakeBoost = true;
                                }
                                if (upgrade.getUpgrade().equals("Customer Service Department")) {
                                    boost = " 4% tip";
                                    fakeBoost = true;
                                }
                                if (upgrade.getUpgrade().equals("Food Services Department")) {
                                    boost = " 4% work";
                                    fakeBoost = true;
                                }
                                if (upgrade.getUpgrade().equals("Overtime Management")) {
                                    boost = " 100% overtime";
                                    fakeBoost = true;
                                }
                                if (upgrade.getUpgrade().equals("Lunch Rush Initiative")) {
                                    boost = " 1 Hour lunch rush";
                                    fakeBoost = true;
                                }
                                if (upgrade.getUpgrade().equals("Task Booster")) {
                                    boost = " 100% Daily task";
                                    fakeBoost = true;
                                }

                                sb.append("*(+$" + boost + ")*\r\n");

                                count++;
                                totalCost = totalCost + upgrade.getCurrentCost();
                                if (!fakeBoost) {
                                    totalBoost = totalBoost + upgrade.getBoost();
                                }
                            }
                            if (count == upgradeLimit + 1) {
                                break;
                            }
                        }
                        if (totalCost == 0) {
                            sb.append("Maxed out!");
                        }

                        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                        embed.color(Color.SUMMER_SKY);
                        embed.title("Your upgrades" + title);
//                        embed.addField(title, sb.toString(), false);
                        embed.description(sb.toString());
                        embed.addField("Total Cost", "$" + String.format("%,d", totalCost), true);
                        embed.addField("Total Boost", "$" + String.format("%,d", totalBoost), true);


                        message.getChannel().block().createMessage(embed.build()).block();

                    }
                    action = getAction(message, paramStats.toLowerCase());
                    if (action != null) {
                        String userId = message.getAuthor().get().getId().asString();

                        LocationEnum locationEnum = LocationEnum.getLocation(action);
                        if (locationEnum == null) {
                            return Mono.empty();
                        }

                        Location location = getLocation(locationEnum.getName(), "");

                        if (location == null) {
                            return Mono.empty();
                        }

                        List<UserUpgrades> list = UpgradeUtils.loadUserUpgrades(userId, location.getName().getName());

                        if (list.isEmpty()) {

                            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                            embed.color(Color.SUMMER_SKY);
                            embed.title("Your upgrades");
                            String commands = "</hire:1006354977847001159>, </advertisements:1006354977721176137>, </upgrades:1006354978274820107>, </decorations:1006354977788268620>, ";
                            switch (locationEnum) {
                                case shack:
                                    commands = commands + "</truck:1006354978153169014>";
                                    break;
                                case mall:
                                    commands = commands + "</kiosk:1010956257588428840>";
                                    break;
                                case beach:
                                    commands = commands + "</stand:1006354978153169010>";
                                    break;
                                case city:
                                    commands = commands + "</cart:1006354977721176142>";
                                    break;
                                case hq:
                                    commands = "</hq upgrades:1018564197602295859>, </hq hire:1018564197602295859> ";
                                    break;

                            }

                            embed.addField("Error", "I have no data, please run the following command " + commands, false);


                            message.getChannel().block().createMessage(embed.build()).block();
                            return Mono.empty();
                        }
                        List<UserUpgrades> total = new ArrayList<>();
                        list.forEach(userUpgrades -> {
                            Upgrade up = location.getUpgrades().get(userUpgrades.getUpgrade());
                            int max = up.getMax();
                            int current = userUpgrades.getProgress();
                            for (int i = current; i < max; i++) {
                                UserUpgrades newUp = new UserUpgrades(userUpgrades.getName(), userUpgrades.getLocation(), userUpgrades.getUpgrade(), i + 1);
                                newUp.setCurrentCost(location.getCost(userUpgrades.getUpgrade(), i + 1));
                                newUp.setValue(newUp.getCurrentCost() / up.getBoost());
                                newUp.setBoost(up.getBoost());
                                total.add(newUp);
                            }
                        });

                        AtomicInteger totalUpgrades = new AtomicInteger();
                        AtomicLong totalCost = new AtomicLong();
                        AtomicLong totalBoost = new AtomicLong();
                        location.getUpgrades().forEach((name, upgrade) -> {
                            int max = upgrade.getMax();
                            for (int i = 0; i < max; i++) {
                                totalUpgrades.getAndIncrement();
                                totalCost.addAndGet(location.getCost(upgrade.getName(), i + 1));
                                int boost = upgrade.getBoost();
                                if (upgrade.getName().equals("appliances") ||
                                        upgrade.getName().equals("tipjar") ||
                                        upgrade.getName().equals("Customer Service Department") ||
                                        upgrade.getName().equals("Food Services Department") ||
                                        upgrade.getName().equals("Overtime Management") ||
                                        upgrade.getName().equals("Lunch Rush Initiative") ||
                                        upgrade.getName().equals("Task Booster")) {
                                    boost = 0;
                                }
                                totalBoost.addAndGet(boost);
                            }
                        });


                        int countLeft = 0;
                        Long costLeft = 0L;
                        Long boostLeft = 0L;
                        for (UserUpgrades upgrade : total) {
                            if (upgrade.getCurrentCost() > 0) {
                                countLeft++;
                                costLeft = costLeft + upgrade.getCurrentCost();
                                if (!upgrade.getName().equals("appliances") &&
                                        !upgrade.getUpgrade().equals("tipjar") &&
                                        !upgrade.getUpgrade().equals("Customer Service Department") &&
                                        !upgrade.getUpgrade().equals("Food Services Department") &&
                                        !upgrade.getUpgrade().equals("Overtime Management") &&
                                        !upgrade.getUpgrade().equals("Lunch Rush Initiative") &&
                                        !upgrade.getUpgrade().equals("Task Booster")) {
                                    boostLeft = boostLeft + upgrade.getBoost();
//                                    logger.info("Added boost  " + upgrade.getUpgrade() + " $"+upgrade.getBoost());
//                                } else {
//                                    logger.info("skipping " + upgrade.getUpgrade());
                                }
                            }
                        }


                        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                        embed.color(Color.SUMMER_SKY);
                        embed.title("Upgrade stats for " + location.getName().getName());
                        StringBuilder sb = new StringBuilder("```");
                        sb.append("Total Spent            $" + String.format("%,d", (totalCost.get() - costLeft)) + " \n");
                        sb.append("Total Remaining        $" + String.format("%,d", costLeft) + " \n");
                        sb.append("Percentage Remaining   " + String.format("%.02f", ((costLeft * 1.0 / totalCost.get()) * 100)) + "% \n");
                        sb.append("Upgrades Purchased     " + (totalUpgrades.get() - countLeft) + " \n");
                        sb.append("Upgrades Remaining     " + countLeft + " \n");
                        sb.append("Income Purchased       $" + String.format("%,d", (totalBoost.get() - boostLeft)) + " \n");
                        sb.append("Income Remaining       $" + String.format("%,d", boostLeft) + " \n");
                        sb.append("```");


                        embed.description(sb.toString());
                        message.getChannel().block().createMessage(embed.build()).block();

                    }


                    action = getAction(message, paramUpLimit.toLowerCase());
                    if (action != null) {
                        String userId = message.getAuthor().get().getId().asString();


                            int number;
                            try {
                                number = Integer.parseInt(action);
                            } catch (NumberFormatException e) {
                                message.getChannel().block().createMessage("missing number").block();
                                return Mono.empty();
                            }
                            if (number > 30) {

                                message.getChannel().block().createMessage("Max value is 30r").block();
                                return Mono.empty();
                            }

                            ReminderUtils.setUpgrade(userId, number);
                            message.getChannel().block().createMessage("Upgrade limit updated to " + number).block();
                            return Mono.empty();
                    }

                }
            } catch (Exception e) {
                printException(e);
            }
        }
        return Mono.empty();
    }


    private void react(Message message, Profile profile) {
        String react = profile.getEmote();
        if (react == null || react.equals("")) {
            react = defaultReact;
        }

        if (react.startsWith("<")) {
            String[] emote = react.split(":");
            Long id = Long.parseLong(emote[2].replace(">", ""));
            String name = emote[1];
            boolean animated = true;
            message.addReaction(ReactionEmoji.of(id, name, true)).block();
        } else {
            message.addReaction(ReactionEmoji.unicode(react)).block();
        }
    }

    private Location getLocation(String name, String desc) {

        if (name.contains("Taco Truck")) {
            name = "shack";
        }
        if (name.contains("Ice Cream Stand")) {
            name = "beach";
        }
        if (name.contains("Hotdog Cart")) {
            name = "city";
        }
        Location defaultLocation = null;

        for (Location location : locations) {

            if (LocationEnum.shack.getName().equals(location.getName().getName())) {
                defaultLocation = location;
            } else {
                if (name.toLowerCase().contains(location.getName().getName())) {
                    return location;
                }
                if (desc.contains("**HQ Balance:**") && LocationEnum.hq.getName().equals(location.getName().getName())) {
                    return location;
                }
            }

        }
        return defaultLocation;
    }

}
