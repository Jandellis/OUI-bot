package action.upgrades;

import action.Action;
import action.reminder.EmbedAction;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
import action.upgrades.model.GroupData;
import action.upgrades.model.Location;
import action.upgrades.model.LocationEnum;
import action.upgrades.model.Upgrade;
import action.upgrades.model.UserUpgrades;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedData;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuyUpgrade extends Action implements EmbedAction {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    //    String defaultReact = "\uD83D\uDC4B";
    Location mall = new Location(LocationEnum.mall);
    Location city = new Location(LocationEnum.city);
    Location shack = new Location(LocationEnum.shack);
    Location stadium = new Location(LocationEnum.stadium);
    Location beach = new Location(LocationEnum.beach);
    Location cantina = new Location(LocationEnum.cantina);
    Location amusement = new Location(LocationEnum.amusement);
    Location hq = new Location(LocationEnum.hq);
    Location event = new Location(LocationEnum.event);
    List<Location> locations = new ArrayList<>();

    String paramUp;
    String paramStats;
    String paramUpLimit;

    String reloadEmote = "\uD83D\uDD04";

    public BuyUpgrade() {
        paramUp = "cyUp";
        paramStats = "cyStats";
        paramUpLimit = "cyLimitUp";


        //hire
        mall.addUpgrade("cashier", "Cashier", 10, 250, 35, true);
        mall.addUpgrade("associate", "Inventory Associate", 15, 500, 35, true);
        mall.addUpgrade("janitor", "Janitor", 20, 1000, 35, true);
        mall.addUpgrade("security", "Security Guard", 25, 2000, 30, true);
        mall.addUpgrade("sales", "Sales Associate", 40, 2500, 35, true);
        mall.addUpgrade("leader", "Team Leader", 65, 3500, 35, true);
        mall.addUpgrade("manager", "Store Manager", 150, 5000, 35, true);
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
        mall.addUpgrade("taco", "Taco Shop", 50, 7500, 20, false, true);
        mall.addUpgrade("repair", "Phone Repair", 100, 15000, 15, false, true);
        mall.addUpgrade("froyo", "Froyo Shop", 250, 50000, 15, false, true);
        mall.addUpgrade("photo", "Photo Booths", 400, 150000, 10, false, true);
        mall.addUpgrade("merch", "TacoShack Merch", 2500, 5000000, 1, false, true);

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
        city.addUpgrade("apprentice", "Apprentice Chef", 10, 250, 45, true);
        city.addUpgrade("cook", "Cook", 20, 600, 45, true);
        city.addUpgrade("advertiser", "Advertiser", 20, 700, 45, true);
        city.addUpgrade("greeter", "Greeter", 25, 800, 45, true);
        city.addUpgrade("sous", "Sous Chef", 40, 1200, 45, true);
        city.addUpgrade("head", "Head Chef", 65, 2000, 40, true);
        city.addUpgrade("executive", "Executive Chef", 150, 5000, 45, true);
        //deco
        city.addUpgrade("banner", "Banner", 5, 150, 50);
        city.addUpgrade("sign", "Neon Sign", 10, 500, 50);
        city.addUpgrade("glass", "Stained Glass", 30, 2500, 45);
        city.addUpgrade("artwork", "Artwork", 150, 100000, 8);
        city.addUpgrade("chandelier", "Chandelier", 1750, 5000000, 8);
        //cart
        city.addUpgrade("buns", "Buns", 50, 7500, 30, false, true);
        city.addUpgrade("condiments", "Condiments", 100, 10000, 25, false, true);
        city.addUpgrade("beverages", "Beverages", 275, 50000, 20, false, true);
        city.addUpgrade("coolers", "Coolers", 450, 250000, 15, false, true);
        city.addUpgrade("grill", "Grill", 800, 1000000, 10, false, true);

        locations.add(city);


        //ad
        amusement.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 45);
        amusement.addUpgrade("radio", "Radio Ad", 20, 650, 40);
        amusement.addUpgrade("email", "Email Campaign", 30, 1000, 40);
        amusement.addUpgrade("internet", "Internet Ad", 50, 2000, 45);
        amusement.addUpgrade("tv", "TV Commercial", 160, 5500, 30);
        amusement.addUpgrade("blimp", "Advertising Blimp", 200, 250000, 5);
        //up
        amusement.addUpgrade("paint", "New Paint", 10, 250, 45);
        amusement.addUpgrade("furniture", "New Furniture", 20, 600, 40);
        amusement.addUpgrade("bathrooms", "Nicer Bathrooms", 25, 800, 40);
        amusement.addUpgrade("billboard", "Billboard", 35, 1000, 40);
        amusement.addUpgrade("appliances", "Better Appliances", 90, 1200, 30);
        amusement.addUpgrade("tipjar", "Cooler Tip Jar", 40, 500, 35);
        //hire
        amusement.addUpgrade("apprentice", "Apprentice Chef", 10, 250, 45, true);
        amusement.addUpgrade("cook", "Cook", 20, 600, 45, true);
        amusement.addUpgrade("advertiser", "Advertiser", 20, 700, 45, true);
        amusement.addUpgrade("greeter", "Greeter", 25, 800, 45, true);
        amusement.addUpgrade("sous", "Sous Chef", 40, 1200, 45, true);
        amusement.addUpgrade("head", "Head Chef", 65, 2000, 40, true);
        amusement.addUpgrade("executive", "Executive Chef", 150, 5000, 45, true);
        //deco
        amusement.addUpgrade("benches", "Benches", 5, 100, 50);
        amusement.addUpgrade("speaker", "Speaker", 10, 250, 50);
        amusement.addUpgrade("balloon", "Balloon Archway", 30, 1500, 45);
        amusement.addUpgrade("fireworks", "Fireworks Display", 150, 20000, 8);
        amusement.addUpgrade("plushies", "Taco Plushies", 500, 1500000, 5);
        //Attractions
        amusement.addUpgrade("toss", "Bottle Toss", 50, 5000, 20, false, true);
        amusement.addUpgrade("arcade", "Arcade Games", 100, 8500, 15, false, true);
        amusement.addUpgrade("carnival", "Carnival Games", 250, 25000, 10, false, true);
        amusement.addUpgrade("carousel", "Carousel", 400, 150000, 5, false, true);
        amusement.addUpgrade("coaster", "Roller Coaster", 2000, 750000, 10, false, true);
        amusement.addUpgrade("ferris", "Ferris Wheel", 6000, 50000000, 1, false, true);

        locations.add(amusement);


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
        shack.addUpgrade("apprentice", "Apprentice Chef", 10, 250, 35, true);
        shack.addUpgrade("cook", "Cook", 20, 600, 35, true);
        shack.addUpgrade("advertiser", "Advertiser", 20, 700, 35, true);
        shack.addUpgrade("greeter", "Greeter", 25, 800, 35, true);
        shack.addUpgrade("sous", "Sous Chef", 40, 1200, 35, true);
        shack.addUpgrade("head", "Head Chef", 65, 2000, 30, true);
        shack.addUpgrade("executive", "Executive Chef", 150, 5000, 35, true);
        //deco
        shack.addUpgrade("flowers", "Flowers", 5, 100, 40);
        shack.addUpgrade("ornaments", "Ornaments", 10, 200, 35);
        shack.addUpgrade("lights", "Fancy Lights", 30, 1000, 30);
        shack.addUpgrade("mural", "Mural", 100, 15000, 5);
        shack.addUpgrade("statue", "Taco Statue", 500, 500000, 3);
        //truck
        shack.addUpgrade("register", "Register", 50, 5000, 20, false, true);
        shack.addUpgrade("assistant", "Assistant", 100, 10000, 15, false, true);
        shack.addUpgrade("driver", "Truck Driver", 250, 25000, 10, false, true);
        shack.addUpgrade("kitchen", "Kitchen", 400, 100000, 5, false, true);
        shack.addUpgrade("engine", "Engine", 1000, 1000000, 3, false, true);
        locations.add(shack);


        //ad
        beach.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 40);
        beach.addUpgrade("radio", "Radio Ad", 20, 650, 35);
        beach.addUpgrade("email", "Email Campaign", 30, 1000, 35);
        beach.addUpgrade("internet", "Internet Ad", 50, 2000, 40);
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
        beach.addUpgrade("apprentice", "Apprentice Chef", 10, 250, 40, true);
        beach.addUpgrade("cook", "Cook", 20, 600, 40, true);
        beach.addUpgrade("advertiser", "Advertiser", 20, 700, 40, true);
        beach.addUpgrade("greeter", "Greeter", 25, 800, 40, true);
        beach.addUpgrade("sous", "Sous Chef", 40, 1200, 40, true);
        beach.addUpgrade("head", "Head Chef", 65, 2000, 35, true);
        beach.addUpgrade("executive", "Executive Chef", 150, 5000, 40, true);
        //deco
        beach.addUpgrade("shells", "Sea Shells", 5, 100, 40);
        beach.addUpgrade("umbrella", "Umbrella", 10, 250, 40);
        beach.addUpgrade("leis", "Leis", 30, 1500, 35);
        beach.addUpgrade("tanks", "Fish Tanks", 125, 20000, 5);
        beach.addUpgrade("fountain", "Taco Fountain", 500, 1500000, 3);
        //stand
        beach.addUpgrade("decals", "Decals", 50, 5000, 25, false, true);
        beach.addUpgrade("wheels", "Wheels", 100, 8500, 15, false, true);
        beach.addUpgrade("mixers", "Mixers", 250, 25000, 15, false, true);
        beach.addUpgrade("server", "Server", 400, 150000, 5, false, true);
        beach.addUpgrade("freezer", "Freezer", 750, 750000, 10, false, true);
        locations.add(beach);



        //ad
        stadium.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 40);
        stadium.addUpgrade("radio", "Radio Ad", 20, 650, 35);
        stadium.addUpgrade("email", "Email Campaign", 30, 1000, 35);
        stadium.addUpgrade("internet", "Internet Ad", 50, 2000, 40);
        stadium.addUpgrade("jumbotron", "Jumbotron", 160, 5500, 20);
        stadium.addUpgrade("blimp", "Advertising Blimp", 200, 250000, 4);
        //up
        stadium.addUpgrade("paint", "New Paint", 10, 250, 40);
        stadium.addUpgrade("furniture", "New Furniture", 20, 600, 35);
        stadium.addUpgrade("bathrooms", "Nicer Bathrooms", 25, 800, 35);
        stadium.addUpgrade("billboard", "Billboard", 35, 1000, 30);
        stadium.addUpgrade("appliances", "Better Appliances", 90, 1200, 30);
        stadium.addUpgrade("tipjar", "Cooler Tip Jar", 40, 500, 35);
        //hire
        stadium.addUpgrade("dishwasher", "Dishwasher", 10, 250, 40, true);
        stadium.addUpgrade("cashier", "Cashier", 10, 250, 40, true);
        stadium.addUpgrade("bartender", "Bartender", 35, 1000, 45, true);
        stadium.addUpgrade("vendor", "Seat Vendor", 35, 1000, 45, true);
        stadium.addUpgrade("frycook", "Fry Cook", 40, 1200, 40, true);
        stadium.addUpgrade("linecook", "Line Cook", 65, 2000, 35, true);
        stadium.addUpgrade("supervisor", "Supervisor", 150, 5000, 40, true);
        //deco
        stadium.addUpgrade("garlands", "Turf Garlands", 5, 100, 45);
        stadium.addUpgrade("flags", "Pennant Flags", 15, 500, 45);
        stadium.addUpgrade("ledsign", "LED Sign", 45, 2000, 40);
        stadium.addUpgrade("logo", "Logo", 125, 20000, 10);
        stadium.addUpgrade("livestream", "Livestream TVs", 500, 1000000, 4);
        //stand
        stadium.addUpgrade("giftbags", "Gift Bags", 50, 8000, 25, false, true);
        stadium.addUpgrade("minibar", "Minibar", 120, 12500, 20, false, true);
        stadium.addUpgrade("catering", "Catering Service", 250, 40000, 20, false, true);
        stadium.addUpgrade("lounge", "Lounge Area", 380, 125000, 15, false, true);
        stadium.addUpgrade("fieldaccess", "Field Access", 2000, 2000000, 5, false, true);
        locations.add(stadium);





        //ad
        cantina.addUpgrade("newspaper", "Newspaper Ad", 10, 350, 50);
        cantina.addUpgrade("radio", "Radio Ad", 20, 650, 45);
        cantina.addUpgrade("email", "Email Campaign", 30, 1000, 45);
        cantina.addUpgrade("internet", "Internet Ad", 50, 2000, 50);
        cantina.addUpgrade("tv", "TV Commercial", 160, 5500, 35);
        cantina.addUpgrade("blimp", "Advertising Blimp", 200, 250000, 6);
        //up
        cantina.addUpgrade("paint", "New Paint", 10, 250, 50);
        cantina.addUpgrade("furniture", "New Furniture", 20, 600, 45);
        cantina.addUpgrade("bathrooms", "Nicer Bathrooms", 25, 800, 45);
        cantina.addUpgrade("billboard", "Billboard", 35, 1000, 45);
        cantina.addUpgrade("appliances", "Better Appliances", 90, 1200, 30);
        cantina.addUpgrade("tipjar", "Cooler Tip Jar", 40, 500, 35);
        //hire
        cantina.addUpgrade("dishwasher", "Dishwasher", 10, 250, 50, true);
        cantina.addUpgrade("cashier", "Cashier", 10, 250, 50, true);
        cantina.addUpgrade("server", "Server", 20, 600, 50, true);
        cantina.addUpgrade("bartender", "Bartender", 35, 1000, 55, true);
        cantina.addUpgrade("sous", "Sous Chef", 40, 1200, 50, true);
        cantina.addUpgrade("head", "Head Chef", 65, 2000, 45, true);
        cantina.addUpgrade("manager", "Store Manager", 150, 5000, 50, true);
        //deco
        cantina.addUpgrade("barstools", "Barstools", 5, 175, 70);
        cantina.addUpgrade("skulls", "Sugar Skulls", 15, 650, 55);
        cantina.addUpgrade("coasters", "Coasters", 30, 2750, 50);
        cantina.addUpgrade("tiles", "Mosaic Tiles", 80, 50000, 30);
        cantina.addUpgrade("mirrors", "Vintage Mirrors", 850, 1000000, 18);
        //stage
        cantina.addUpgrade("discolights", "Disco Lights", 50, 8000, 30, false, true);
        cantina.addUpgrade("sound", "Sound System", 110, 12500, 25, false, true);
        cantina.addUpgrade("spotlight", "Spotlight", 260, 40000, 20, false, true);
        cantina.addUpgrade("microphones", "Microphones", 420, 200000, 15, false, true);
        cantina.addUpgrade("lyrics", "Lyrics", 750, 800000, 10, false, true);
        cantina.addUpgrade("pyrotechnics", "Pyrotechnics", 1500, 10000000, 1, false, true);

        locations.add(cantina);



        //Upgrades
        hq.addUpgrade("Customer Service Department", "Customer Service Department", 180, 750000, 20);
        hq.addUpgrade("Food Services Department", "Food Services Department", 180, 750000, 20);
        hq.addUpgrade("Overtime Management", "Overtime Management", 750, 3000000, 5);
        hq.addUpgrade("Lunch Rush Initiative", "Lunch Rush Initiative", 250, 2500000, 6);
        hq.addUpgrade("Task Booster", "Task Booster", 150, 10000000, 4);

        //Employees
        hq.addUpgrade("Secretary", "Secretary", 100, 1000000, 20, true);
        hq.addUpgrade("Treasurer", "Treasurer", 200, 1750000, 20, true);
        hq.addUpgrade("Chief Financial Officer", "Chief Financial Officer", 300, 2500000, 10, true);
        hq.addUpgrade("Chief Executive Officer", "Chief Executive Officer", 500, 5000000, 10, true);
        locations.add(hq);

        event.addUpgrade("Worker Efficiency", "Worker Efficiency", 700, 1000, 25);
        event.addUpgrade("Customer Service Training", "Customer Service Training", 10, 1500, 15);
        event.addUpgrade("Fancier Decor", "Fancier Decor", 20, 2000, 15);
        event.addUpgrade("Newspaper Ads", "Newspaper Ads", 30, 2500, 20);
        event.addUpgrade("Better Sign", "Better Sign", 50, 3000, 20);
        event.addUpgrade("Radio Ads", "Radio Ads", 75, 3500, 25);
        event.addUpgrade("Email Campaign", "Email Campaign", 100, 4000, 25);
        event.addUpgrade("Flashy Lights", "Flashy Lights", 250, 5000, 20);
        locations.add(event);


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


                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0){

                        logger.info("empty embeds, skipping");
//                        handleEmbedAction(message, checkEmbeds(message));
                        return Mono.empty();
                    } else {
                        embedData = message.getData().embeds();
                    }
                    //for some reason the embeds will be empty from slash, but if i load it again it will have data
//                    if (checkAge(message)) {
//                        checkMessageAgain(message);
//                    } else {

//                    }

                    handleEmbedAction(message, embedData);

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

                        Location location = getLocation(locationEnum.getName(), "", "");

                        if (location == null) {
                            return Mono.empty();
                        }

                        List<UserUpgrades> list = UpgradeUtils.loadUserUpgrades(userId, location.getName().getName());
                        Profile profile = ReminderUtils.loadProfileById(userId);

                        if (list.isEmpty()) {

                            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                            embed.color(Color.SUMMER_SKY);
                            embed.title("Your upgrades - " + location.getName().getPrintName());
                            String commands = "</hire:1203826200452137022>, </advertisements:1203826194500288593>, </upgrades:1203826209532682311>, </decorations:1203826197352677417>, ";
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
                                case stadium:
                                    commands = commands + "</suite:1429616125984899132>";
                                    break;
                                case city:
                                    commands = commands + "</cart:1006354977721176142>";
                                    break;
                                case cantina:
                                    commands = commands + "</stage:1276293791728406771>";
                                    break;
                                case hq:
                                    commands = "</hq upgrades:1018564197602295859>, </hq hire:1018564197602295859> ";
                                    break;
                                case amusement:
                                    commands = commands + "</attractions:1150514355167821946>";
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
                                newUp.setEmote(up.isHire());
                                total.add(newUp);
                            }

                        });

                        boolean cheapSort = message.getContent().toLowerCase().contains("cheap");
                        boolean groupSort = message.getContent().toLowerCase().contains("grouped");
                        String[] split = message.getContent().split(" ");
                        int startPage = -1;
                        int endPage = -1;
                        for (String s : split) {
                            try {
                                int number = Integer.parseInt(s);
                                if (startPage == -1)
                                    startPage = number;
                                else
                                    endPage = number;
                            } catch (NumberFormatException e) {

                            }
                        }
                        //flip so end page is after start page
                        if (endPage < startPage) {
                            int temp = endPage;
                            endPage = startPage;
                            startPage = temp;
                        }
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
                        if (endPage == -1) {
                            startPage = 1;
                            endPage = upgradeLimit;
                        }

                        StringBuilder sb = new StringBuilder();
                        int count = 1;
                        long totalCost = 0;
                        int totalBoost = 0;
                        for (UserUpgrades upgrade : total) {
                            if (upgrade.getCurrentCost() > 0) {
                                //make the page
                                if (startPage == -1 || endPage == -1 || (startPage <= count && endPage >= count)) {
                                    String value = "";
                                    value = String.format("%,d", upgrade.getCurrentCost());
                                    String boost = "";
                                    boost = String.format("%,d", upgrade.getBoost());
                                    String line = count +" - "+upgrade.getEmote()+" `" + upgrade.getUpgrade() + "` - **$" + value + "**";
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
                                    if (upgrade.getUpgrade().equals("Worker Efficiency")) {
                                        boost = " mover overtime";
                                        fakeBoost = true;
                                    }
                                    sb.append("*(+$" + boost + ")*\n");
                                    totalCost = totalCost + upgrade.getCurrentCost();
                                    if (!fakeBoost) {
                                        totalBoost = totalBoost + upgrade.getBoost();
                                    }
                                }

                                count++;
                            }
                            if (startPage == -1 || endPage == -1) {
                                if (count == upgradeLimit + 1) {
                                    break;
                                }
                            }
                            if (endPage < count) {
                                break;
                            }
                        }
                        if (totalCost == 0) {
                            sb.append("Maxed out!");
                        } else {

                            //sorted by price first, then alphabetically
                            if (groupSort) {
                                title = " - Grouped";
                                String[] lines = sb.toString().split("\n");
                                List<String> sorted = new ArrayList();
                                //1 - :wrench: newspaper - **$350** (+$10)
                                for (String line : lines) {
                                    sorted.add(line.split(" - :")[1]);
                                }
                                Collections.sort(sorted);
                                sb = new StringBuilder();
//                                count = 1;
//                                for (String line : sorted) {
//
//                                    sb.append(count + " - :" + line + "\n");
//                                    count++;
//
//                                }
                                Map<String, GroupData> grouped = new LinkedHashMap<>();
//                                Pattern linePattern = Pattern.compile("^(.*?)\\s*-\\s*\\$(\\d+)\\s*\\(\\+\\$(\\d+)\\)$");
//                                Pattern linePattern = Pattern.compile("^(.*?)\\s*-\\s*\\$(\\d+)\\s*\\((.*?)\\)$");
//                                Pattern linePattern = Pattern.compile("^\\d+\\s*-\\s*(:\\w+:\\s*\\w+)\\s*-\\s*\\$(\\d+)\\s*\\((.*?)\\)$");
//                                Pattern linePattern = Pattern.compile(
//                                        "^\\d+\\s*-\\s*(:\\w+:\\s*`[^`]+`)\\s*-\\s*\\*\\*\\$(\\d+)\\*\\*\\s*\\*\\((.*?)\\)\\*$"
//                                );
                                Pattern linePattern = Pattern.compile(
                                        "^\\s*([\\w:.-]+\\s*`([^`]+)`)\\s*-\\s*\\*\\*\\$([0-9]{1,3}(?:,[0-9]{3})*)\\*\\*\\s*\\*\\((.*?)\\)\\*\\s*$"
                                );
                                title += " (x"+sorted.size()+")";

                                for (String line : sorted) {
                                    Matcher matcher = linePattern.matcher(line.trim());
                                    if (matcher.find()) {
                                        //1 - :wrench: newspaper - $350 (+$10)
                                        String category = matcher.group(1).trim(); // e.g. "wrench: newspaper"
                                        int amount = Integer.parseInt(matcher.group(3).replace(",", "")); //350
                                        String boost = matcher.group(4).trim(); // 10

                                        // Key groups by both category and (+$X)
                                        String key = category + " (+$" + boost + ")";
                                        grouped.computeIfAbsent(key, k -> new GroupData(category, boost))
                                                .addAmount(amount);
                                    }
                                }

                                int index = 1;
                                for (GroupData g : grouped.values()) {
                                    sb.append(String.format("%d - :%s (x%d) - **$%,d** *(%s)*\n",
                                            index++, g.category, g.count, g.totalAmount, g.boost));
                                }


                            }
                        }



                        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                        embed.color(Color.SUMMER_SKY);
                        embed.title("Your upgrades - " + location.getName().getPrintName() + " " + title);
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

                        Location location = getLocation(locationEnum.getName(), "", "");

                        if (location == null) {
                            return Mono.empty();
                        }

                        List<UserUpgrades> list = UpgradeUtils.loadUserUpgrades(userId, location.getName().getName());

                        if (list.isEmpty()) {

                            EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                            embed.color(Color.SUMMER_SKY);
                            embed.title("Your upgrades - " + location.getName().getPrintName());
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
                                case stadium:
                                    commands = commands + "</suite:1429616125984899132>";
                                    break;
                                case city:
                                    commands = commands + "</cart:1006354977721176142>";
                                    break;
                                case cantina:
                                    commands = commands + "</stage:1276293791728406771>";
                                    break;
                                case hq:
                                    commands = "</hq upgrades:1018564197602295859>, </hq hire:1018564197602295859> ";
                                    break;
                                case amusement:
                                    commands = commands + "</attractions:1150514355167821946>";
                                    break;

                            }

                            embed.addField("Error", "I have no data, please run the following command " + commands, false);


                            message.getChannel().block().createMessage(embed.build()).block();
                            return Mono.empty();
                        }

                        // if list is missing something, try and add it at 0;
                        if (location.getUpgrades().size() != list.size()) {
                            location.getUpgrades().forEach((name, upgrade) -> {
                                AtomicBoolean found = new AtomicBoolean(false);
                                list.forEach(userUpgrades -> {
                                    if (userUpgrades.getUpgrade().equals(upgrade.getName())) {
                                        found.set(true);
                                    }
                                });

                                if (found.get() == false) {
                                    list.add(new UserUpgrades(name, location.getName().getName(), upgrade.getName(), 0));
                                }
                            });
                            logger.info("Adding missing items into the list");
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
                                newUp.setEmote(up.isHire());
                                total.add(newUp);
                            }
                        });

                        AtomicInteger totalUpgrades = new AtomicInteger();
                        AtomicLong totalCost = new AtomicLong();
                        AtomicLong totalBoost = new AtomicLong();
                        location.getUpgrades().forEach((name, upgrade) -> {
                            int max = upgrade.getMax();
                            StringBuilder sb = new StringBuilder(upgrade.getName() + "-"+ upgrade.getBoost()+ "-"+upgrade.getMax()+ "\r\n");
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
                                    upgrade.getName().equals("Task Booster")||
                                    upgrade.getName().equals("Worker Efficiency")) {
                                    boost = 0;
                                }
                                totalBoost.addAndGet(boost);
                                int pos = i+ 1;
                                sb.append(pos + "-" + location.getCost(upgrade.getName(), pos) +"\r\n");
                            }
                            if (userId.equals("292839877563908097")) {
//                                dmMe(sb.toString());
                            }
                        });


                        int countLeft = 0;
                        Long costLeft = 0L;
                        Long boostLeft = 0L;
                        for (UserUpgrades upgrade : total) {
                            if (upgrade.getCurrentCost() > 0) {
                                countLeft++;
                                costLeft = costLeft + upgrade.getCurrentCost();
                                if (!upgrade.getUpgrade().equals("appliances") &&
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
                        embed.title("Upgrade stats - " + location.getName().getPrintName());
                        StringBuilder sb = new StringBuilder("```");
                        sb.append("Total Spent            $" + String.format("%,d", (totalCost.get() - costLeft)) + " \n");
                        sb.append("Total Remaining        $" + String.format("%,d", costLeft) + " \n");
                        sb.append("Percentage Remaining   " + String.format("%.02f", ((costLeft * 1.0 / totalCost.get()) * 100)) + "% \n");
                        sb.append("Upgrades Purchased     " + (totalUpgrades.get() - countLeft) + " \n");
                        sb.append("Upgrades Remaining     " + countLeft + " \n");
                        sb.append("Income Purchased       $" + String.format("%,d", (totalBoost.get() - boostLeft)) + " \n");
                        sb.append("Income Remaining       $" + String.format("%,d", boostLeft) + " ("+String.format("%.02f", ((boostLeft * 1.0 / totalBoost.get()) * 100)) + "%) \n");
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
                        if (number > 51) {

                            message.getChannel().block().createMessage("Max value is 50").block();
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


    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        try {
            for (EmbedData embed : embedData) {
                if (embed.title().toOptional().isPresent() && embed.description().toOptional().isPresent()) {
                    String title = embed.title().get();

                    if (title.contains("Decorations") ||
                            title.contains("Upgrades ") ||
                            title.contains("Advertisements ") ||
                            title.contains("Employees ") ||
                            title.contains("Mall Kiosk") ||
                            title.contains("Taco Truck") ||
                            title.contains("Hotdog Cart") ||
                            title.contains("Amusement Park Attractions") ||
                            title.contains("Ice Cream Stand") ||
                            title.contains("Karaoke Stage") ||
                            title.contains("Team Shack Upgrades")) {
                        String id = getId(message, embed);
                        Location location = getLocation(title, embed.description().get(), "");

                        if (location == null) {
                            return Mono.empty();
                        }

                        if (embed.description().toOptional().isPresent()) {
                            String desc = embed.description().get();
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
                                react(message, profile, false);
                                react(message, profile, true);//:arrows_counterclockwise:
                            }
                        }
                    }
                }

                if (embed.description().toOptional().isPresent()) {
                    String desc = embed.description().get();
                    //boosts
                    if (desc.startsWith("\u2705") && (desc.contains("You have purchased:") || desc.contains("You have hired:"))) {

                        String id = getId(message, embed);
                        Location location = getLocation("", "", embedData.get(0).footer().get().text());

                        Upgrade upgrade = location.getUpgradeDesc(desc);
                        if (upgrade != null) {
                            UserUpgrades userUpgrades = UpgradeUtils.loadUserUpgrade(id, location.getName().getName(), upgrade.getName());
                            if (userUpgrades != null) {
                                upgrade.setPosition(userUpgrades.getProgress() + 1);
                                UpgradeUtils.addUserUpgrades(new UserUpgrades(id, location.getName().getName(), upgrade.getName(), upgrade.getPosition()));


                                Profile profile = ReminderUtils.loadProfileById(id);
                                if (profile != null) {
                                    react(message, profile, false);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
}


    private void react(Message message, Profile profile, boolean refresh) {
        if (!profile.getEnabled())
            return;
        String react = profile.getEmote();
        if (react == null || react.equals("")) {
            react = defaultReact;
        }
        if (refresh) {
            react = reloadEmote;
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

    private Location getLocation(String name, String desc, String footer) {

        if (name.contains("Taco Truck")) {
            name = "shack";
        }
        if (name.contains("Ice Cream Stand")) {
            name = "beach";
        }
        if (name.contains("VIP Suite")) {
            name = "stadium";
        }
        if (name.contains("Hotdog Cart")) {
            name = "city";
        }
        if (name.contains("Karaoke Stage")) {
            name = "cantina";
        }
        if (name.contains("Amusement")) {
            name = "amusement";
        }
        if (name.contains("Team")) {
            name = "event";
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
                if (footer.contains("|") && location.getName().getName().contains(footer.split("\\|")[1].split(" ")[2].toLowerCase())) {
                    return location;
                }
            }

        }
        return defaultLocation;
    }

    @Override
    protected Mono<Object> doReactionEvent(ReactionAddEvent reactionAddEvent) {

        try {

            if (reactionAddEvent.getEmoji().asUnicodeEmoji().isPresent())
                if (reactionAddEvent.getEmoji().asUnicodeEmoji().get().getRaw().equals(reloadEmote)) {
                    //got reaction
                    Message message = reactionAddEvent.getMessage().block();
                    EmbedData embedData = null;
                    if (message.getEmbeds().size()> 0) {
                     embedData = message.getEmbeds().get(0).getData();
                    }

                    String messageAuthorId = getId(message, embedData);
                    if (messageAuthorId.equals(reactionAddEvent.getUserId().asString())) {
                        //user is the same as who wrote the did the message
                        //remove all reactions
                        message.removeAllReactions().block();
                        doAction(message);

                    }
                }
        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }
}
