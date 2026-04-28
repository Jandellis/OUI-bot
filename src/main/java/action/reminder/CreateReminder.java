package action.reminder;

import action.Action;
import action.export.ExportUtils;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.reminder.model.ReminderSettings;
import action.sm.Utils;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.emoji.Emoji;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (message.getData().author().id().asString().equals(tacoBot)) {
            try {

                if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {
                    logger.info("empty embeds, skipping");



                    if (contractTypeCheck(message, "Active Contract")) {
                        Profile profile = null;
                        String userId = getContainerUser(message);
                        profile = ReminderUtils.loadProfileByUserName(userId);
                        Map<String, Double> cooldowns = getCooldownMultipliers(message);
                        double tips = cooldowns.getOrDefault("Tips Cooldown", 1.0);
                        double work = cooldowns.getOrDefault("Work Cooldown", 1.0);
                        double overtime = cooldowns.getOrDefault("Overtime Cooldown", 1.0);


                        StringBuilder sb = new StringBuilder("Updated cooldowns modifiers:");
//                            if (tips != 1.0){
                            sb.append("\n- Tips Cooldown: ").append(tips);
//                            }
//                            if (work != 1.0){
                            sb.append("\n- Work Cooldown: ").append(work);
//                            }
//                            if (overtime != 1.0){
                            sb.append("\n- Overtime Cooldown: ").append(overtime);
//                            }

                        ReminderSettings reminderSettings = ReminderUtils.loadReminderSettings(profile.getName());
                        if (reminderSettings == null) {
                            reminderSettings = new ReminderSettings(profile.getName(), true, true, false, true, true, true, true, true, 1, 1, 1);
                        }
                        boolean shouldShowMessage = false;
                        if (reminderSettings.getOvertimeModifier() != overtime
                                || reminderSettings.getTipsModifier() != tips
                                || reminderSettings.getWorkModifier() != work) {
                            shouldShowMessage = true;
                        }

                        reminderSettings.setTipsModifier(tips);
                        reminderSettings.setWorkModifier(work);
                        reminderSettings.setOvertimeModifier(overtime);
                        logger.info("setting reminder settings {}", reminderSettings);

                        ReminderUtils.updateReminderSettings(reminderSettings);
                        react(message, profile);
                        if (shouldShowMessage) {
                            return message.getChannel().flatMap(channel -> {
                                return channel.createMessage(sb.toString());
                            });
                        }

                    }
                    if (contractTypeCheck(message, "Catering Contracts")){
                        Profile profile = null;
                        String userId = getContainerUser(message);
                        profile = ReminderUtils.loadProfileByUserName(userId);
                        List<Contract> contracts = getContracts(message);
                        for (Contract contract : contracts) {
                            if (contract.getName().endsWith("Service Specialist")){
                                tipsContracts(contract, profile, message).block();
                            }
                            if (contract.getName().endsWith("Overtime Specialist")){
                                overtimeContracts(contract, profile, message).block();
                            }
                            if (contract.getName().endsWith("Shift  Specialist")){
                                workContracts(contract, profile, message).block();
                            }
                            if (contract.getName().endsWith("Double Time  Specialist")){
                                //need to update this one once i see the % increase
//                                doubleTimeContracts(contract, profile, message).block();
                            }
                        }

                    }



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
                                int income = 0;
                                if (!desc.contains("%**")) {
                                    String value = desc.split("collected a total of \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                }

                                createReminder(ReminderType.tips, message, desc, embed, income);
                            }
                            //work
                            if (desc.contains("\uD83D\uDC68\u200D\uD83C\uDF73") && desc.contains("** has cooked a total of")
                                    && !desc.contains("** while working overtime!")) {
                                int income = 0;
                                if (!desc.contains("%**")) {
                                    String value = desc.split("tacos and earned \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                }
                                createReminder(ReminderType.work, message, desc, embed, income);
                            }
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working!")) {
                                int income = 0;
                                if (!desc.contains("%**")) {
                                    String value = desc.split("tacos and earned \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                }

                                createReminder(ReminderType.work, message, desc, embed, income);
                            }
                            //ot
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working overtime!")) {
                                int income = 0;
                                if (!desc.contains("%**")) {
                                    String value = desc.split("tacos and earned \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                }
                                createReminder(ReminderType.ot, message, desc, embed,  income);

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
                            if (desc.startsWith("\u2705") && (desc.contains("You have cleaned your Shack") || desc.contains("You have cleaned all of your locations"))) {
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
                            // event
                            // event clean
                            if (desc.startsWith("\u2705") && desc.contains("You have cleaned your team's shack")) {
                                AtomicReference<String> userId = new AtomicReference<>("");
                                userId.set(getId(message, embed));

                                Profile profile = ReminderUtils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.eventClean, message, profile);
                                }
                            }

                        }
                        if (embed.title().toOptional().isPresent()) {

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
                                checkRushHour(message, profile);


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


    private void checkRushHour(Message message, Profile profile) {
        logger.info("Checking rush hour");
        if (message.getGuildId().isPresent() && profile != null && profile.getEnabled()) {
                if (message.getGuildId().get().asString().equals("840395541791768599")) {
                    logger.info("In OUI");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    if ( profile.getRushHourEnd() != null) {
                        logger.info("User rush hour end at " + formatter.format(profile.getRushHourEnd().toLocalDateTime()));
                    }
                    LocalDateTime rushHour = ExportUtils.loadFranchiseRushHour("oui");
//                    logger.info("OUI rush hour warn at " + formatter.format(rushHour));
                    if (rushHour != null
                            && profile.getRushHourEnd() != null
                            && profile.getRushHourEnd().toLocalDateTime().isBefore(LocalDateTime.now())) {


                        if (profile.getRushHourIgnore() == null
                                ||
                                (profile.getRushHourIgnore() != null &&
                                        profile.getRushHourIgnore().toLocalDateTime().isBefore(LocalDateTime.now()))) {
                            if (LocalDateTime.now().isBefore(rushHour)) {
                                String msg = "A Rush Hour is active now, come join <#1289435874240630825>";
                                message.getChannel().block().createMessage(msg).block();
                            }
                        }

                    }

                    //if me
                    if ((profile.getName().equals("292839877563908097")
                            || profile.getName().equals("695518297168281640")
                            || profile.getName().equals("762526280435367986"))) {
                        List<SystemReminder> warn = Utils.loadReminder(SystemReminderType.rushHourStart);
                        if (warn.isEmpty()) {
                            String msg = "Go start the rush hour <#1289435874240630825>";
                            message.getChannel().block().createMessage(msg).block();
                        }
                    }
            };
        }



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


    private void createReminder(ReminderType type, Message message, String desc, EmbedData embed, int income) {

        String name = desc.split("\\*\\*")[1];
        Profile profile = null;
        if (profile == null) {
            String userId = getId(message, embed);
            profile = ReminderUtils.loadProfileById(userId);
        }

        if (profile == null) {
            profile = ReminderUtils.loadProfileByName(name);
        }
        if (profile == null) {
            logger.info("No profile found for " + name);
            return;
        }

        if (type == ReminderType.work) {
            if (!ReminderUtils.updateStatsWork(profile.getName())) {
                ReminderUtils.createStats(profile.getName(), 1, 0, 0);
            }
            if (income > 0) {
                ReminderUtils.updatesWorkIncome(profile.getName(), income);
            }
        }
        if (type == ReminderType.tips) {
            if (!ReminderUtils.updateStatsTips(profile.getName())) {
                ReminderUtils.createStats(profile.getName(), 0, 1, 0);
            }
            if (income > 0) {
                ReminderUtils.updatesTipsIncome(profile.getName(), income);
            }
        }
        if (type == ReminderType.ot) {
            if (!ReminderUtils.updateStatsOvertime(profile.getName())) {
                ReminderUtils.createStats(profile.getName(), 0, 0, 1);
            }
            if (income > 0) {
            ReminderUtils.updatesOvertimeIncome(profile.getName(), income);
            }
        }
        createReminder(type, message, profile);
    }

    private void createReminder(ReminderType type, Message message, Profile profile) {
        checkRushHour(message, profile);
        int sleep = 0;
        AtomicBoolean isPatreonServer = new AtomicBoolean(false);
        if (message.getGuildId().isPresent()) {
            patreonServers.forEach(server -> {
                if (message.getGuildId().get().asString().equals(server)) {
                    isPatreonServer.set(true);
                }
            });
        }
        ReminderSettings reminderSettings = ReminderUtils.loadReminderSettings(profile.getName());
        if (reminderSettings == null) {
            reminderSettings = new ReminderSettings(profile.getName(), true, true, false, true, true, true, true, true, 1, 1, 1);
        }

        switch (type) {
            case work:
                sleep = profile.getStatus().getWork();
                if (!isPatreonServer.get()) {
                    sleep = sleep + 1;
                }
                if (isRushHour(profile)) {
                    sleep = 4;
                }
                //convert to seconds
                sleep = sleep * 60;
                sleep = (int) (sleep * reminderSettings.getWorkModifier());
                //grind is double work 1min min warning
                int grindSleep = 0;
                grindSleep = sleep * 2 -1;

                Instant reminderTime = message.getTimestamp().plus(grindSleep, ChronoUnit.SECONDS);
                ReminderUtils.addReminder(profile.getName(), ReminderType.grind, Timestamp.from(reminderTime), message.getChannelId().asString());

                break;
            case tips:
                sleep = profile.getStatus().getTips();
                if (!isPatreonServer.get()) {
                    sleep = sleep + 1;
                }
                if (isRushHour(profile)) {
                    sleep = 2;
                }
                sleep = sleep * 60;
                sleep = (int) (sleep * reminderSettings.getTipsModifier());
                break;
            case ot:
                sleep = profile.getStatus().getOt();
                if (isRushHour(profile)) {
                    sleep = 15;
                }
                sleep = sleep * 60;
                sleep = (int) (sleep * reminderSettings.getOvertimeModifier());
                break;
            case vote:
                sleep = profile.getStatus().getVote()*60;
                break;
            case daily:
                sleep = profile.getStatus().getDaily()*60;
                break;
            case clean:
                sleep = profile.getStatus().getClean()*60;
                break;

            case eventClean:
                sleep = 2*60*60;
                break;
        }


        Instant reminderTime = message.getTimestamp().plus(sleep, ChronoUnit.SECONDS);
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

        profile.getIgnoredHidden();


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
                if (dbReminder.getType() == ReminderType.tips || (profile.getIgnoredHidden() && !reminderSettings.isTip())) {
                    tips = true;
                }
                if (dbReminder.getType() == ReminderType.work || (profile.getIgnoredHidden() && !reminderSettings.isWork())) {
                    work = true;
                }
                if (dbReminder.getType() == ReminderType.ot || (profile.getIgnoredHidden() && !reminderSettings.isOvertime())) {
                    ot = true;
                }
                if (dbReminder.getType() == ReminderType.vote || (profile.getIgnoredHidden() && !reminderSettings.isVote())) {
                    vote = true;
                }
                if (dbReminder.getType() == ReminderType.clean || (profile.getIgnoredHidden() && !reminderSettings.isClean())) {
                    clean = true;
                }
                if (dbReminder.getType() == ReminderType.daily || (profile.getIgnoredHidden() && !reminderSettings.isDaily())) {
                    daily = true;
                }
            }

            StringBuilder missingReminders = new StringBuilder();
            if (!work) {
                missingReminders.append("</work:1203826210250166292>\n");
            }
            if (!tips) {
                missingReminders.append("</tips:1203826208383696957>\n");
            }
            if (!ot) {
                missingReminders.append("</overtime:1203826204356911104>\n");
            }
            if (!vote) {
                missingReminders.append("</vote link:1203826209532682312>\n");
            }
            if (!daily) {
                missingReminders.append("</daily:1203826197352677416>\n");
            }
            if (!clean) {
                missingReminders.append("</clean:1203826195511250967>\n");
            }
            if (missingReminders.toString().length() > 1) {
                message.getChannel().block().createMessage(missingReminders.toString()).block();
            }
            react(message, profile);

            if (!work) {
                message.addReaction(Emoji.unicode("\uD83C\uDDFC")).block();
            }
            if (!tips) {
                message.addReaction(Emoji.unicode("\uD83C\uDDF9")).block();
            }
            if (!ot) {
                message.addReaction(Emoji.unicode("\uD83C\uDDF4")).block();
            }
            if (!vote) {
                message.addReaction(Emoji.unicode("\uD83C\uDDFB")).block();
            }
            if (!daily) {
                message.addReaction(Emoji.unicode("\uD83C\uDDE9")).block();
            }
            if (!clean) {
                message.addReaction(Emoji.unicode("\uD83C\uDDE8")).block();
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

    private boolean isRushHour (Profile profile){
        if (profile.getRushHourEnd() == null ) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = profile.getRushHourEnd().toLocalDateTime();
        return now.isBefore(reminderTime);
    }


    // from cooldown
    private void createReminder(ReminderType type, Message message, Profile profile, int time) {
        if (time > 0) {

            Instant reminderTime = message.getTimestamp().plus(time, ChronoUnit.SECONDS);

            Reminder reminder = ReminderUtils.addReminder(profile.getName(), type, Timestamp.from(reminderTime), message.getChannelId().asString());

            DoReminder doReminder = new DoReminder(gateway, client);
            doReminder.runReminder(reminder);

            react(message, profile);

        }
    }
//
//    private void react(Message message, Profile profile) {
//        if (!profile.getEnabled())
//            return;
//        String react;
//        if (profile == null) {
//            react = defaultReact;
//        } else {
//            react = profile.getEmote();
//            if (react == null || react.equals("")) {
//                react = defaultReact;
//            }
//        }
//
//        if (react.startsWith("<")) {
//            String[] emote = react.split(":");
//            Long id = Long.parseLong(emote[2].replace(">", ""));
//            String name = emote[1];
//            boolean animated = true;
//            message.addReaction(Emoji.of(id, name, true)).block();
//        } else {
//            message.addReaction(Emoji.unicode(react)).block();
//        }
//    }

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


    public Map<String, Double> getCooldownMultipliers(Message message) {
        Map<String, Double> cooldowns = new HashMap<>();
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return cooldowns;

        Pattern pattern = Pattern.compile("\\*\\*([+-]\\d+)%\\*\\*\\s+(.*)");

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData item : top.components().get()) {
                if (item.type() != 10 || item.content().isAbsent()) continue;
                String text = item.content().get();
                if (!text.contains("Active Effects")) continue;

                for (String line : text.split("\n")) {
                    logger.info("effects are {}", line);
                    Matcher m = pattern.matcher(line.trim());
                    boolean found = m.find();
                    logger.info("trying to match '{}' result: {}", line.trim(), found);
                    if (!found) continue;

                    int percent = Integer.parseInt(m.group(1));
                    String type = m.group(2).trim();
                    double multiplier = 1 + (percent / 100.0);
                    logger.info("adding '{}' multiplier: {}", type, multiplier);
                    cooldowns.put(type, multiplier);
                }
            }
        }
        return cooldowns;
    }

    public List<Contract> getContracts(Message message) {
        List<Contract> contracts = new ArrayList<>();
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return contracts;

        Pattern namePattern = Pattern.compile("\\*\\*(.+?)\\*\\*\\s*$");
        Pattern objectivePattern = Pattern.compile("\\*\\*Objective:\\*\\*\\s*(.+)");
        Pattern rewardsPattern = Pattern.compile("\\*\\*Rewards:\\*\\*\\s*(.+)");

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData section : top.components().get()) {
                // type 9 = Section
                if (section.type() != 9) continue;
                if (section.components().isAbsent()) continue;

                for (ComponentData item : section.components().get()) {
                    if (item.type() != 10 || item.content().isAbsent()) continue;

                    String text = item.content().get();
                    String name = null;
                    String objective = null;
                    String rewards = null;

                    for (String line : text.split("\n")) {
                        line = line.trim();

                        if (name == null) {
                            Matcher m = namePattern.matcher(line);
                            if (m.find()) {
                                name = m.group(1).trim();
                            }
                        }

                        if (objective == null) {
                            Matcher m = objectivePattern.matcher(line);
                            if (m.find()) {
                                objective = m.group(1).trim();
                            }
                        }
                        if (rewards == null) {
                            Matcher m = rewardsPattern.matcher(line);
                            if (m.find()) {
                                rewards = m.group(1).trim();
                            }
                        }
                    }

                    if (name != null && objective != null) {
                        logger.info("found contract: '{}' -> '{}', {}", name, objective, rewards);
                        contracts.add(new Contract(name, objective, rewards));
                    }
                }
            }
        }
        return contracts;
    }

    public Mono<Object> tipsContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getTips();
        return printSimpleContract(contract, profile, message, sleep, "tips");
    }

    public Mono<Object> workContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getWork();
        return printSimpleContract(contract, profile, message, sleep, "work");
    }

    public Mono<Object> overtimeContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getOt();
        return printSimpleContract(contract, profile, message, sleep, "overtime");
    }

    public Mono<Object> doubleTimeContracts(Contract contract, Profile profile, Message message) {


        String obj = contract.getObjective().split("`")[1];
        int value = Integer.parseInt(obj.replace(",", "").replace("\\$", ""));

        int sleepWork = 0;
        sleepWork = profile.getStatus().getWork();
        AtomicBoolean isPatreonServer = new AtomicBoolean(false);
        if (message.getGuildId().isPresent()) {
            patreonServers.forEach(server -> {
                if (message.getGuildId().get().asString().equals(server)) {
                    isPatreonServer.set(true);
                }
            });
        }

        if (!isPatreonServer.get()) {
            sleepWork = sleepWork + 1;
        }

        int sleepOverTime = 0;
        sleepOverTime = profile.getStatus().getOt();

        int sleep = minutesToCompleteEarningsGoal(profile, value, sleepWork, sleepOverTime);
        String timeStr;
        if (sleep >= 60) {
            int hours = sleep / 60;
            int mins = sleep % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = sleep + "m";
        }
        String printMessage = "It will take you " + timeStr + " to earn " + value + " with overtime and work";
        return message.getChannel().flatMap(channel -> {
            return channel.createMessage(printMessage);
        });
    }

    public int minutesToCompleteEarningsGoal(Profile profile, long goal, int sleepWork, int sleepOvertime) {
        long totalEarned = 0;
        int totalMinutes = 0;

        int workCooldown = 0;
        int overtimeCooldown = 0;

        while (totalEarned < goal) {
            // advance time by 1 minute
            totalMinutes++;
            if (workCooldown > 0) workCooldown--;
            if (overtimeCooldown > 0) overtimeCooldown--;

            // do work if available
            if (workCooldown == 0) {
                totalEarned += profile.getWorkIncome();
                workCooldown = sleepWork;
            }

            // do overtime if available
            if (overtimeCooldown == 0) {
                totalEarned += profile.getOvertimeIncome();
                overtimeCooldown = sleepOvertime;
            }
        }

        return totalMinutes;
    }

    public Mono<Object>  printSimpleContract(Contract contract, Profile profile, Message message, int sleep, String type){

        String obj = contract.getObjective().split("`")[1];
        int value = Integer.parseInt(obj.replace(",", ""));


        AtomicBoolean isPatreonServer = new AtomicBoolean(false);
        if (message.getGuildId().isPresent()) {
            patreonServers.forEach(server -> {
                if (message.getGuildId().get().asString().equals(server)) {
                    isPatreonServer.set(true);
                }
            });
        }

        if (!isPatreonServer.get() && !type.equals("overtime")) {
            sleep = sleep + 1;
        }
        sleep = sleep * value;
        String timeStr;
        if (sleep >= 60) {
            int hours = sleep / 60;
            int mins = sleep % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = sleep + "m";
        }
        String printMessage = "It will take you " + timeStr + " to complete " + value + " " + type;
        return message.getChannel().flatMap(channel -> {
            return channel.createMessage(printMessage);
        });
    }

    public boolean contractTypeCheck(Message message, String type) {
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return false;

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData item : top.components().get()) {
                if (item.type() != 10 || item.content().isAbsent()) continue;
                if (item.content().get().contains(type)) return true;
            }
        }
        return false;
    }



}

