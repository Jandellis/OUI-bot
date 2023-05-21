package action.reminder;

import action.Action;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CreateReminder extends Action implements EmbedAction {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
    Long recruiter;
    List<String> patreonServers;

    public CreateReminder() {
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
        patreonServers = Arrays.asList(config.get("patreonServers").split(","));
        recruiter = Long.parseLong(config.get("recruiter"));
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

                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {
                        logger.info("empty embeds, skipping");
//                        handleEmbedAction(message, checkEmbeds(message));
                        return Mono.empty();
                    } else {

                        logger.info("non empty embeds");
                        handleEmbedAction(message, message.getData().embeds());
                    }

                } catch (Exception e) {
                    printException(e);
                }

            }
        }

        String actionData = getAction(message, "cyposted");
        if (actionData != null) {
//            if (hasPermission(message, recruiter)) {

            Instant reminderTime = message.getTimestamp().plus(6, ChronoUnit.HOURS);

            Reminder reminder = ReminderUtils.addReminder(message.getAuthor().get().getId().asString(), ReminderType.postAd, Timestamp.from(reminderTime), message.getChannelId().asString());
            DoReminder doReminder = new DoReminder(gateway, client);
            doReminder.runReminder(reminder);
            Profile profile = ReminderUtils.loadProfileById(message.getAuthor().get().getId().asString());
            react(message, profile);
//            } else {
//                logger.info(message.getAuthor().get().getId().asString() + " is not a recruiter");
//            }
        }

        return Mono.empty();
    }


    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {
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
                    //for some reason the embeds will be empty from slash, but if i load it again it will have data
//                    if (checkAge(message)) {
//                    } else {
                    for (EmbedData embed : embedData) {
                        logger.info("EmbedData is -- " + embed);


                        if (embed.description().toOptional().isPresent()) {
                            String desc = embed.description().get();
                            //tips
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** in tips!")) {

                                createReminder(ReminderType.tips, message, desc, embed);
                            }
                            //work
                            if (desc.contains("\uD83D\uDC68\u200D\uD83C\uDF73") && desc.contains("** has cooked a total of")
                                    && !desc.contains("** while working overtime!")) {
                                createReminder(ReminderType.work, message, desc, embed);
                            }
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working!")) {

                                createReminder(ReminderType.work, message, desc, embed);
                            }
                            //ot
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working overtime!")) {
                                createReminder(ReminderType.ot, message, desc, embed);

                            }

                            //vote
                            if ((desc.startsWith("\u2705") || desc.startsWith("\uD83C\uDF89")) && desc.contains("Voting Daily Streak Progress")) {
                                AtomicReference<String> userId = new AtomicReference<>("");
                                userId.set(getId(message, embed));

                                if (userId.get().equals("")) {
                                    //go look for history and find the last message that has claim and use that for the userid
                                    List<MessageData> historic = getMessagesOfChannel(message);
                                    historic.forEach(messageData -> {
                                        Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                                        if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp()))) {
                                            if (messageData.content().toLowerCase().contains("claim")) {
                                                userId.set(messageData.author().id().toString());
                                            }
                                        }
                                    });
                                }
                                if (userId.get().equals(tacoBot)) {
                                    // if user clicks on claim button
                                    //go find /vote link command and use that user
                                    List<MessageData> historic2 = getMessagesOfChannel(message, 120);

                                    historic2.forEach(messageData -> {
                                        Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                                        if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp())) && messageData.interaction().toOptional().isPresent()) {

                                            if (messageData.interaction().get().name().toLowerCase().contains("vote link")) {
                                                userId.set(messageData.interaction().get().user().id().toString());
                                            }
                                        }
                                    });
                                }
                                Profile profile = ReminderUtils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.vote, message, profile);
                                }

                            }

                            //daily
                            if ((desc.startsWith("\u2705") || desc.startsWith("\uD83C\uDF89"))
                                    && desc.contains("__**Daily Streak Progress**__")
//                                        && embed.getFooter().isPresent()
//                                        && embed.getFooter().get().getText().contains("daily")
                            ) {

                                AtomicReference<String> userId = new AtomicReference<>("");
                                userId.set(getId(message, embed));
                                if (userId.get().equals("")) {
                                    List<MessageData> historic = getMessagesOfChannel(message);
                                    historic.forEach(messageData -> {
                                        Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                                        if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp()))) {
                                            String noSpace = messageData.content().toLowerCase().replace(" ", "");
                                            if (noSpace.contains("!d") || noSpace.contains("!daily")) {
                                                userId.set(messageData.author().id().toString());
                                            }
                                        }
                                    });
                                }
                                Profile profile = ReminderUtils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.daily, message, profile);
                                }

                            }
                            //clean
                            if (desc.startsWith("\u2705") && desc.contains("You have cleaned")) {
                                AtomicReference<String> userId = new AtomicReference<>("");
                                userId.set(getId(message, embed));

                                if (userId.get().equals("")) {
                                    List<MessageData> historic = getMessagesOfChannel(message);
                                    historic.forEach(messageData -> {
                                        Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                                        if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp()))) {
                                            if (messageData.content().toLowerCase().contains("clean")) {
                                                userId.set(messageData.author().id().toString());
                                            }
                                        }
                                    });
                                }
                                Profile profile = ReminderUtils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.clean, message, profile);
                                }

                            }

                        } else if (embed.title().toOptional().isPresent()) {

                            //cooldown

                            String title = embed.title().get();

                            if (title.contains("Cooldowns |")) {
                                AtomicReference<String> userId = new AtomicReference<>("");
                                userId.set(getId(message, embed));

                                if (userId.get().equals("")) {
                                    String footer = embed.footer().get().text();

                                    List<MessageData> historic = getMessagesOfChannel(message);
                                    historic.forEach(messageData -> {
                                        Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                                        if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp()))) {
                                            if (footer.contains(messageData.author().discriminator())) {
                                                userId.set(messageData.author().id().toString());
                                            }
                                        }
                                    });
                                }
                                Profile profile = ReminderUtils.loadProfileById(userId.get());


                                if (profile != null) {
                                    List<Reminder> reminders = ReminderUtils.loadReminder(profile.getName());
                                    //if already got reminder dont add
                                    boolean tips = false;
                                    boolean work = false;
                                    boolean ot = false;
                                    boolean vote = false;
                                    boolean daily = false;
                                    boolean clean = false;


                                    for (Reminder dbReminder : reminders) {
                                        if (dbReminder.getType() == ReminderType.tips) {
                                            tips = true;
                                        }
                                        if (dbReminder.getType() == ReminderType.work) {
                                            work = true;
                                        }
                                        if (dbReminder.getType() == ReminderType.ot) {
                                            ot = true;
                                        }
                                        if (dbReminder.getType() == ReminderType.vote) {
                                            vote = true;
                                        }
                                        if (dbReminder.getType() == ReminderType.clean) {
                                            clean = true;
                                        }
                                        if (dbReminder.getType() == ReminderType.daily) {
                                            daily = true;
                                        }

                                    }
                                    //need to add in cooldown time
                                    if (!work) {
                                        int seconds = getSeconds(embed.fields().get().get(0).value());
                                        createReminder(ReminderType.work, message, profile, seconds);
                                    }
                                    if (!tips) {
                                        int seconds = getSeconds(embed.fields().get().get(1).value());
                                        createReminder(ReminderType.tips, message, profile, seconds);
                                    }
                                    if (!ot) {
                                        int seconds = getSeconds(embed.fields().get().get(2).value());
                                        createReminder(ReminderType.ot, message, profile, seconds);
                                    }
                                    if (!vote) {
                                        int seconds = getSeconds(embed.fields().get().get(5).value());
                                        createReminder(ReminderType.vote, message, profile, seconds);
                                    }
                                    if (!daily) {
                                        int seconds = getSeconds(embed.fields().get().get(4).value());
                                        createReminder(ReminderType.daily, message, profile, seconds);
                                    }
                                    if (!clean) {
                                        int seconds = getSeconds(embed.fields().get().get(3).value());
                                        createReminder(ReminderType.clean, message, profile, seconds);
                                    }

                                }

                            }
                        }

                        // react with emots for what reminders i dont have


                    }
//                    }
                } catch (Exception e) {
                    printException(e);
                }

            }
        }

        String actionData = getAction(message, "cyposted");
        if (actionData != null) {
//            if (hasPermission(message, recruiter)) {

            Instant reminderTime = message.getTimestamp().plus(6, ChronoUnit.HOURS);

            Reminder reminder = ReminderUtils.addReminder(message.getAuthor().get().getId().asString(), ReminderType.postAd, Timestamp.from(reminderTime), message.getChannelId().asString());
            DoReminder doReminder = new DoReminder(gateway, client);
            doReminder.runReminder(reminder);
            Profile profile = ReminderUtils.loadProfileById(message.getAuthor().get().getId().asString());
            react(message, profile);
//            } else {
//                logger.info(message.getAuthor().get().getId().asString() + " is not a recruiter");
//            }
        }

        return Mono.empty();
    }

    public int getSeconds(String value) {
        int seconds = 0;
        if (value.startsWith("\u274C")) {
            seconds = Integer.parseInt(value.split(" ")[1]);
            if (value.contains("minute")) {
                seconds = seconds * 60;
            }
            if (value.contains("hour")) {
                seconds = seconds * 60 * 60;
            }

        }
        return seconds;
    }

    public static List<MessageData> getMessagesOfChannel(RestChannel channel) {
        Snowflake time = Snowflake.of(Instant.now().minus(15, ChronoUnit.SECONDS));
        return channel.getMessagesAfter(time).collectList().block();
    }


    public static List<MessageData> getMessagesOfChannel(Message message) {

//        Snowflake time = Snowflake.of(message.getTimestamp().minus(15, ChronoUnit.SECONDS));
        return getMessagesOfChannel(message, 15);
    }

    public static List<MessageData> getMessagesOfChannel(Message message, int time) {
        Snowflake snowflakeTime = Snowflake.of(message.getTimestamp().minus(time, ChronoUnit.SECONDS));
        return message.getRestChannel().getMessagesAfter(snowflakeTime).collectList().block();
    }


    private void createReminder(ReminderType type, Message message, String desc, EmbedData embed) {

        String name = desc.split("\\*\\*")[1];
        Profile profile = ReminderUtils.loadProfileByName(name);
        if (profile == null) {
            String userId = getId(message, embed);
            profile = ReminderUtils.loadProfileById(userId);
        }
        if (profile == null) {
            logger.info("No profile found for " + name);
            return;
        }

        if (type == ReminderType.work) {
            if (!ReminderUtils.updateStatsWork(profile.getName())) {
                ReminderUtils.createStats(profile.getName(), 1, 0, 0);
            }
        }
        if (type == ReminderType.tips) {
            if (!ReminderUtils.updateStatsTips(profile.getName())) {
                ReminderUtils.createStats(profile.getName(), 0, 1, 0);
            }
        }
        if (type == ReminderType.ot) {
            if (!ReminderUtils.updateStatsOvertime(profile.getName())) {
                ReminderUtils.createStats(profile.getName(), 0, 0, 1);
            }
        }
        createReminder(type, message, profile);
    }

    private void createReminder(ReminderType type, Message message, Profile profile) {
        int sleep = 0;
        AtomicBoolean isPatreonServer = new AtomicBoolean(false);
        if (message.getGuildId().isPresent()) {
            patreonServers.forEach(server -> {
                if (message.getGuildId().get().asString().equals(server)) {
                    isPatreonServer.set(true);
                }
            });
        }
        switch (type) {
            case work:
                sleep = profile.getStatus().getWork();
                if (!isPatreonServer.get()) {
                    sleep = sleep + 1;
                }
                break;
            case tips:
                sleep = profile.getStatus().getTips();
                if (!isPatreonServer.get()) {
                    sleep = sleep + 1;
                }
                break;
            case ot:
                sleep = profile.getStatus().getOt();
                break;
            case vote:
                sleep = profile.getStatus().getVote();
                break;
            case daily:
                sleep = profile.getStatus().getDaily();
                break;
            case clean:
                sleep = profile.getStatus().getClean();
                break;
        }


        Instant reminderTime = message.getTimestamp().plus(sleep, ChronoUnit.MINUTES);
        AtomicBoolean alreadyProcessed = new AtomicBoolean(false);
        message.getReactions().forEach(reaction -> {
            if (reaction.selfReacted()) {
                alreadyProcessed.set(true);
            }
        });
        if (alreadyProcessed.get()) {
            return;
        }


        Reminder reminder = ReminderUtils.addReminder(profile.getName(), type, Timestamp.from(reminderTime), message.getChannelId().asString());

        List<Reminder> reminders = ReminderUtils.loadReminder(profile.getName());
        boolean tips = false;
        boolean work = false;
        boolean ot = false;
        boolean vote = false;
        boolean daily = false;
        boolean clean = false;


        //only put letter emotes or /commands if enabled
        if (profile.getEnabled()) {
            for (Reminder dbReminder : reminders) {
                if (dbReminder.getType() == ReminderType.tips) {
                    tips = true;
                }
                if (dbReminder.getType() == ReminderType.work) {
                    work = true;
                }
                if (dbReminder.getType() == ReminderType.ot) {
                    ot = true;
                }
                if (dbReminder.getType() == ReminderType.vote) {
                    vote = true;
                }
                if (dbReminder.getType() == ReminderType.clean) {
                    clean = true;
                }
                if (dbReminder.getType() == ReminderType.daily) {
                    daily = true;
                }
            }

            StringBuilder missingReminders = new StringBuilder();
            if (!work) {
                missingReminders.append("</work:1006354978274820109>\n");
            }
            if (!tips) {
                missingReminders.append("</tips:1006354978153169013>\n");
            }
            if (!ot) {
                missingReminders.append("</overtime:1006354977981210646>\n");
            }
            if (!vote) {
                missingReminders.append("</vote link:1006354978274820108>\n");
            }
            if (!daily) {
                missingReminders.append("</daily:1006354977788268621>\n");
            }
            if (!clean) {
                missingReminders.append("</clean:1006354977721176143>\n");
            }
            if (missingReminders.toString().length() > 1) {
                message.getChannel().block().createMessage(missingReminders.toString()).block();
            }
            react(message, profile);

            if (!work) {
                message.addReaction(ReactionEmoji.unicode("\uD83C\uDDFC")).block();
            }
            if (!tips) {
                message.addReaction(ReactionEmoji.unicode("\uD83C\uDDF9")).block();
            }
            if (!ot) {
                message.addReaction(ReactionEmoji.unicode("\uD83C\uDDF4")).block();
            }
            if (!vote) {
                message.addReaction(ReactionEmoji.unicode("\uD83C\uDDFB")).block();
            }
            if (!daily) {
                message.addReaction(ReactionEmoji.unicode("\uD83C\uDDE9")).block();
            }
            if (!clean) {
                message.addReaction(ReactionEmoji.unicode("\uD83C\uDDE8")).block();
            }
        }


        DoReminder doReminder = new DoReminder(gateway, client);
//        if (reminder.getId() == -2) {
//            logger.info("got double");
//
//
//
//            gateway.getUserById(Snowflake.of("292839877563908097")).block().getPrivateChannel().flatMap(channel -> {
//                channel.createMessage("got double from channel "+ message.getRestChannel().getId().toString()).block();
//                logger.info("sent DM");
//                return Mono.empty();
//            }).block();
//        }
        doReminder.runReminder(reminder);
    }


    // from cooldown
    private void createReminder(ReminderType type, Message message, Profile profile, int time) {
        if (time > 0) {

            Instant reminderTime = message.getTimestamp().plus(time, ChronoUnit.SECONDS);

            react(message, profile);

            Reminder reminder = ReminderUtils.addReminder(profile.getName(), type, Timestamp.from(reminderTime), message.getChannelId().asString());

            DoReminder doReminder = new DoReminder(gateway, client);
            doReminder.runReminder(reminder);

        }
    }

    private void react(Message message, Profile profile) {
        if (!profile.getEnabled())
            return;
        String react;
        if (profile == null) {
            react = defaultReact;
        } else {
            react = profile.getEmote();
            if (react == null || react.equals("")) {
                react = defaultReact;
            }
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

//
//    private void checkMessageAgain(Message message) {
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
//        logger.info("checking message again in 1 sec");
//        executorService.schedule(taskWrapper, 1, TimeUnit.SECONDS);
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
    // if this is not a person, for vote look at the vote streak count
}
