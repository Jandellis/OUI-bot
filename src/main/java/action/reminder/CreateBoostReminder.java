package action.reminder;

import action.Action;
import action.reminder.model.Boost;
import action.reminder.model.Profile;
import action.reminder.model.ProfileStats;
import action.reminder.model.Reminder;
import action.upgrades.model.LocationEnum;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CreateBoostReminder extends Action implements EmbedAction {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    String defaultReact = "\uD83D\uDC4B";
    public static HashMap<String,Boost> boosts;

    public static Boost getBoost(String name) {
        return boosts.get(name);
    }

    public static HashMap<LocationEnum,List<Boost>> getLocationBoosts() {
        HashMap<LocationEnum,List<Boost>> locationBoosts = new HashMap<>();
        for (Boost boost : boosts.values()) {
            if (boost.getLocation() != LocationEnum.event && boost.getLocation() != LocationEnum.franchise) {
                List<Boost> boostList = locationBoosts.computeIfAbsent(boost.getLocation(), k -> new ArrayList<>());
                boostList.add(boost);
                locationBoosts.put(boost.getLocation(), boostList);
            }
        }
        return locationBoosts;
    }

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);

    public CreateBoostReminder() {

        boosts = new HashMap<>();
        //city
        boosts.put("Happy Hour", new Boost("Happy Hour", 4, LocationEnum.city, 1));
        boosts.put("Samples",new Boost("Samples", 4, LocationEnum.city, 2));
        boosts.put("Mascot",new Boost("Mascot", 6, LocationEnum.city, 3));
        boosts.put("Online Delivery",new Boost("Online Delivery", 8, LocationEnum.city, 4));
        boosts.put("Bus Sign", new Boost("Bus Sign", 24, LocationEnum.city, 5));

        //shack
        boosts.put("Rent-A-Chef", new Boost("Rent-A-Chef", 4, LocationEnum.shack, 1));
        boosts.put("Live Music", new Boost("Live Music", 4, LocationEnum.shack, 2));
        boosts.put("Karaoke Night", new Boost("Karaoke Night", 6, LocationEnum.shack, 3));
        boosts.put("Sign Flipper", new Boost("Sign Flipper", 8, LocationEnum.shack, 4));
        boosts.put("Airplane Sign", new Boost("Airplane Sign", 24, LocationEnum.shack, 5));


        //beach
        boosts.put("Concert", new Boost("Concert", 4, LocationEnum.beach, 1));
        boosts.put("Hammock", new Boost("Hammock", 4, LocationEnum.beach, 2));
        boosts.put("Parasailing", new Boost("Parasailing", 6, LocationEnum.beach,3 ));
        boosts.put("Beach Chairs", new Boost("Beach Chairs", 8, LocationEnum.beach, 4));
        boosts.put("Helicopter Tours", new Boost("Helicopter Tours", 24, LocationEnum.beach, 5));

        //stadium
        boosts.put("Merch Cannon", new Boost("Merch Cannon", 4, LocationEnum.stadium, 1));
        boosts.put("Victory Parade", new Boost("Victory Parade", 4, LocationEnum.stadium, 2));
        boosts.put("Autograph Signing", new Boost("Autograph Signing", 6, LocationEnum.stadium,3 ));
        boosts.put("Light Show", new Boost("Light Show", 8, LocationEnum.stadium, 4));
        boosts.put("Gameday Promotion", new Boost("Gameday Promotion", 24, LocationEnum.stadium, 5));

        //mall
        boosts.put("Lunch Discount", new Boost("Lunch Discount", 4, LocationEnum.mall, 1));
        boosts.put("Sponsorship", new Boost("Sponsorship", 4, LocationEnum.mall, 2));
        boosts.put("Gift Cards", new Boost("Gift Cards", 6, LocationEnum.mall, 3));
        boosts.put("Takeout",new Boost("Takeout", 8, LocationEnum.mall, 4));
        boosts.put("Special",new Boost("Special", 24, LocationEnum.mall, 5));

        //Amusement
        boosts.put("Magic Show", new Boost("Magic Show", 4, LocationEnum.amusement, 1));
        boosts.put("Parade",new Boost("Parade", 4, LocationEnum.amusement, 2));
        boosts.put("Face Painting", new Boost("Face Painting", 6, LocationEnum.amusement, 3));
        boosts.put("Gift Shop",new Boost("Gift Shop", 8, LocationEnum.amusement, 4));
        boosts.put("Live Show",new Boost("Live Show", 24, LocationEnum.amusement, 5));

        //Cantina
        boosts.put("Margarita Bar", new Boost("Margarita Bar", 8, LocationEnum.cantina, 1));
        boosts.put("Taco Bar",new Boost("Taco Bar", 6, LocationEnum.cantina, 2));
        boosts.put("Poker Night", new Boost("Poker Night", 4, LocationEnum.cantina, 3));
        boosts.put("Disco Night",new Boost("Disco Night", 24, LocationEnum.cantina, 4));
        boosts.put("Jukebox",new Boost("Jukebox", 4, LocationEnum.cantina, 5));


        //Event
        boosts.put("Flyers", new Boost("Flyers", 1, LocationEnum.event, 1));
        boosts.put("Sign Twirler",new Boost("Sign Twirler", 2, LocationEnum.event, 2));
        boosts.put("Refreshments", new Boost("Refreshments", 3, LocationEnum.event, 3));
        boosts.put("Music",new Boost("Music", 4, LocationEnum.event, 4));
        boosts.put("Festival",new Boost("Festival", 6, LocationEnum.event, 5));

        boosts.put ("Loyalty Rewards", new Boost("Rewards", 12, LocationEnum.franchise, 1));
        boosts.put ("Seasonal Menu", new Boost("Menu", 18, LocationEnum.franchise, 2));
        boosts.put ("Training Refresher", new Boost("Training", 12, LocationEnum.franchise, 3));
        boosts.put ("Customer Survey", new Boost("Survey", 20, LocationEnum.franchise, 4));
        boosts.put ("Employee Incentives", new Boost("Incentives", 24, LocationEnum.franchise, 5));

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
            if (message.getData().author().id().asString().equals(tacoBot)) {
                try {


                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {

                        embedData = checkEmbeds(message);
                    } else {
                        embedData = message.getData().embeds();
                    }

                    handleEmbedAction(message, embedData);
                } catch (Exception e) {
                    printException(e);
                }

            }
        }
        return Mono.empty();
    }


    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        try {
            for (EmbedData embed : embedData) {


                if (embed.description().toOptional().isPresent()) {
                    String desc = embed.description().get();
                    //boosts
                    if (desc.startsWith("\u2705") && (desc.contains("You have purchased:") || desc.contains("You have bought"))) {

                        logger.info("buying boosts " + desc);
                        AtomicReference<String> userId = new AtomicReference<>("");
                        userId.set(getId(message, embed));

//                                    if (userId.get().equals("")) {
//                                        List<MessageData> historic = getMessagesOfChannel(message.getRestChannel());
//                                        historic.forEach(messageData -> {
//
//                                            String noSpace = messageData.content().toLowerCase().replace(" ", "");
//                                            if (noSpace.contains("!buy")) {
//                                                userId.set(messageData.author().id().toString());
//                                            }
//                                        });
//                                    }
                        Profile profile = ReminderUtils.loadProfileById(userId.get());
                        if (profile != null) {

//                            if (desc.contains("Live Music")){
//                                //stop live music clashing with music
//                                createReminder(boosts.get("Live Music"), message, profile);
//                            } else {

                            LocationEnum location = getLocation(embed);
                            boolean franchise = false;

                            // if location is amusement part and boost = Parade
//                            if (desc.contains("Parade") && location == LocationEnum.amusement) {
//                                createReminder(boosts.get("Parade"), message, profile);
//                            } else {

                                for (Boost boost : boosts.values()) {
                                    switch (boost.getLocation()) {
                                        case event:
                                        case franchise:
                                            franchise = true;

                                    }


                                    if (boost.getLocation() == location) {
                                        if (desc.contains(boost.getName())) {
                                            logger.info(location.getName() + " -- creating reminder for " + boost.getName());
                                            createReminder(boost, message, profile);
                                        }
                                    }
//                                    if (boost.getLocation() == LocationEnum.franchise) {
//                                        if (desc.contains(boost.getName())) {
//                                            logger.info("creating reminder for " + boost.getName());
//                                            createReminder(boost, message, profile);
//                                            franchise = true;
//                                        }
//                                    }
//                                    if (boost.getLocation() == LocationEnum.event) {
//                                        if (desc.contains(boost.getName())) {
//                                            logger.info("creating reminder for " + boost.getName());
//                                            createReminder(boost, message, profile);
//                                            franchise = true;
//                                        }
//                                    }
                                }
//                            }
                            if (!franchise) {
                                // look at the footer to see the balance and save that
                                Long balance = getBalance(embed);
                                // only save balance if its not a franchise
                                if (balance != null && !franchise) {
                                    ProfileStats profileStats = new ProfileStats(userId.get());
                                    profileStats.setImportTime(Timestamp.from(Instant.now()));
                                    profileStats.setLocation(location);
                                    profileStats.setIncome(-1L);
                                    profileStats.setBalance(balance);
                                    ReminderUtils.addProfileStats(profileStats);
                                }
                            }

                        }

                    }

                }

                if (embed.title().toOptional().isPresent()) {
                    if (embed.title().get().startsWith("\uD83D\uDCC8 Active Boosts")) {
                        logger.info("Got Boost");
                        AtomicReference<String> userId = new AtomicReference<>("");
                        userId.set(getId(message, embed));

                        Profile profile = ReminderUtils.loadProfileById(userId.get());
                        if (profile != null) {
                            String desc = embed.description().get();
                            String[] lines = desc.split("\n");

                            List<Reminder> reminders = ReminderUtils.loadReminder(profile.getName());

                            for (String line : lines) {
                                for (Boost boost : boosts.values()) {
                                    if (line.contains(boost.getName())) {
                                        boolean found = false;

                                        for (Reminder reminder : reminders) {
                                            if (reminder.getType().getName().equals(boost.getName())) {
                                                found = true;
                                                logger.info("Got reminder already for " + boost.getName());
                                                break;
                                            }
                                        }
                                        if (!found) {

                                            logger.info("Creating new reminder already for " + boost.getName());
                                            createReminder(boost, message, profile, getSeconds(line));
                                        }
                                    }
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


    public static List<MessageData> getMessagesOfChannel(RestChannel channel) {
        Snowflake time = Snowflake.of(Instant.now().minus(15, ChronoUnit.SECONDS));
        return channel.getMessagesAfter(time).collectList().block();
    }


    public int getSeconds(String value) {
        int seconds = 0;

        //**Airplane Sign:** `2 hours` Remaining
        String time = value.split("`")[1];

        seconds = Integer.parseInt(time.split(" ")[0]);
        if (value.contains("minute")) {
            seconds = seconds * 60;
        }
        if (value.contains("hour")) {
            seconds = seconds * 60 * 60;
        }

        return seconds;
    }


    private void createReminder(Boost boost, Message message, Profile profile) {
        double reminderLength = boost.getDuration() * 60 * 60;
        if (boost.getLocation() == LocationEnum.franchise) {
            try {
                if (message.getMessageReference().isPresent()) {
                    //delete the shop message
                    Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getMessageReference().get().getMessageId().get().asLong())).block();
                    msg.delete().block();
//                message.getReferencedMessage().get().delete().block();
                }
            } catch (Exception e) {

            }
            if (hasPermission(message) && isOUI(message)) {
                profile = ReminderUtils.loadProfileById("292839877563908097");
            }
            //add on 30 seconds for the franchise boost as they are too slow
            reminderLength = reminderLength + 60;
        }
        createReminder(boost, message, profile, (int) reminderLength);
    }

    private void createReminder(Boost boost, Message message, Profile profile, int duration) {

        Instant reminderTime = message.getTimestamp().plus(duration, ChronoUnit.SECONDS);
        ReminderType type = ReminderType.getReminderType(boost.getName());
        logger.info("creating reminder " + type.getName() + " for " + boost.getName());

        Reminder reminder = ReminderUtils.addReminder(profile.getName(), type, Timestamp.from(reminderTime), message.getChannelId().asString());

        DoReminder doReminder = new DoReminder(gateway, client);
        doReminder.runReminder(reminder);
        react(message, profile);
    }

//    private void react(Message message, Profile profile) {
//        if (!profile.getEnabled())
//            return;
//        String react = profile.getEmote();
//        if (react == null || react.equals("")) {
//            react = defaultReact;
//        }
//        react(message, react);
//    }

//
//    @Override
//    public void checkMessageAgain(Message message) {
//
//        Runnable taskWrapper = new Runnable() {
//
//            @Override
//            public void run() {
//                logger.info("checking message again");
//                Message msg = gateway.getMessageById(Snowflake.of(message.getChannelId().asString()), Snowflake.of(message.getId().asString())).block();
//                doAction(msg, false);
//            }
//
//        };
//        logger.info("checking message again in 2 sec");
//        executorService.schedule(taskWrapper, 2, TimeUnit.SECONDS);
//    }

    //
//        "\uD83C\uDDFC" - W
//                "\uD83C\uDDF9" - t
//                        "\uD83C\uDDF4" - o
//                                "\uD83C\uDDE9" - d
//                                        "\uD83C\uDDFB" - v
//                                                "\uD83C\uDDE8" - c


    // once slash look at
    // message.data.interaction_value.user.id_value
}
