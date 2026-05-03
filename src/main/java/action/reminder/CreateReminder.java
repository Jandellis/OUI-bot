package action.reminder;

import action.Action;
import action.export.ExportUtils;
import action.reminder.model.Contract;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.reminder.model.ReminderSettings;
import action.sm.Utils;
import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.retriever.EntityRetrievalStrategy;
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
    String reloadEmote = "\uD83D\uDD04";

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


//
//                    if (contractTypeCheck(message, "Active Contract")) {
//                        Profile profile = null;
//                        String userId = getContainerUser(message);
//                        profile = ReminderUtils.loadProfileByUserName(userId);
//                        Contract contract = getCooldownMultipliers(message);
//                        if (contract == null){
//                            return Mono.empty();
//                        }
//                        logger.info(contract.toString());
//
////                        double tips = cooldowns.getOrDefault("Tips Cooldown", 1.0);
////                        double work = cooldowns.getOrDefault("Work Cooldown", 1.0);
////                        double overtime = cooldowns.getOrDefault("Overtime Cooldown", 1.0);
////                        double tipsBuff = cooldowns.getOrDefault("Tips Payout ", 1.0);
////                        double workBuff = cooldowns.getOrDefault("Work Payout ", 1.0);
////                        double overtimeBuff = cooldowns.getOrDefault("Overtime Payout ", 1.0);
////                        double progress = cooldowns.getOrDefault("progress", 0.0);
////                        double total = cooldowns.getOrDefault("progress", 0.0);
//
////
////                        StringBuilder sb = new StringBuilder("Updated cooldowns modifiers:");
//////                            if (tips != 1.0){
////                            sb.append("\n- Tips Cooldown: ").append(contract.getTipsCoolDown());
//////                            }
//////                            if (work != 1.0){
////                            sb.append("\n- Work Cooldown: ").append(contract.getWorkCoolDown());
//////                            }
//////                            if (overtime != 1.0){
////                            sb.append("\n- Overtime Cooldown: ").append(contract.getOvertimeCoolDown());
//////                            }
//
////                        ReminderSettings reminderSettings = ReminderUtils.loadReminderSettings(profile.getName());
////                        if (reminderSettings == null) {
////                            reminderSettings = new ReminderSettings(profile.getName(), true, true, false, true, true, true, true, true, 1, 1, 1);
////                        }
////                        boolean shouldShowMessage = false;
////                        if (reminderSettings.getOvertimeModifier() != contract.getOvertimeCoolDown()
////                                || reminderSettings.getTipsModifier() != contract.getTipsCoolDown()
////                                || reminderSettings.getWorkModifier() != contract.getWorkCoolDown()) {
////                            shouldShowMessage = true;
////                        }
////
////                        reminderSettings.setTipsModifier(contract.getTipsCoolDown());
////                        reminderSettings.setWorkModifier(contract.getWorkCoolDown());
////                        reminderSettings.setOvertimeModifier(contract.getOvertimeCoolDown());
////                        logger.info("setting reminder settings {}", reminderSettings);
//
//
//
//
//                        int sleepWork = 0;
//                        sleepWork = profile.getStatus().getWork();
//                        int sleepTips = 0;
//                        sleepTips = profile.getStatus().getTips();
//                        AtomicBoolean isPatreonServer = new AtomicBoolean(false);
//                        if (message.getGuildId().isPresent()) {
//                            patreonServers.forEach(server -> {
//                                if (message.getGuildId().get().asString().equals(server)) {
//                                    isPatreonServer.set(true);
//                                }
//                            });
//                        }
//
//                        if (!isPatreonServer.get()) {
//                            sleepWork = sleepWork + 1;
//                            sleepTips = sleepTips + 1;
//                        }
//
//                        int sleepOverTime = 0;
//                        sleepOverTime = profile.getStatus().getOt();
//                        boolean work = false;
//                        boolean overtime = false;
//                        boolean tips = false;
//                        if (contract.getName().contains("Overtime")) {
//                            overtime = true;
//                        }
//                        if (contract.getName().contains("Work") || contract.getName().contains("Shift")) {
//                            work = true;
//                        }
//                        if (contract.getName().contains("Tips") || contract.getName().contains("Service")) {
//                            tips = true;
//                        }
//                        if (contract.getName().contains("Double Time")) {
//                            work = true;
//                            overtime = true;
//                        }
//                        if (contract.getName().contains("Corporate Restructuring")) {
//                            work = true;
//                            overtime = true;
//                            tips = true;
//                        }
//                        if (contract.getObjective().contains("from work")) {
//                            work = true;
//                        }
//                        if (contract.getObjective().contains("from tips")) {
//                            tips = true;
//                        }
//                        if (contract.getObjective().contains("from overtime")) {
//                            overtime = true;
//                        }
//                        if (contract.getObjective().contains("from work and overtime")) {
//                            overtime = true;
//                            work = true;
//                        }
//
//                        if (contract.getName().contains("Corporate Restructuring") || contract.getName().contains("VIP") || contract.getName().contains("Double Time")) {
//
//                            if (profile.getOvertimeIncome() > 1 && profile.getWorkIncome() > 1 && profile.getTipsIncome() > 1) {
//                                int sleep = minutesToCompleteEarningsGoal(profile,
//                                        (contract.getTotal() - contract.getProgress()),
//                                        (int) (sleepWork * contract.getTipsCoolDown()),
//                                        (int) (sleepOverTime * contract.getOvertimeCoolDown()),
//                                        (int) (sleepTips * contract.getTipsCoolDown()),
//                                        work,
//                                        overtime,
//                                        tips,
//                                        contract.getWorkBuff(),
//                                        contract.getOvertimeBuff(),
//                                        contract.getTipsBuff());
//                                logger.info("time left is {} min", sleep);
//                                String timeStr;
//                                if (sleep >= 60) {
//                                    int hours = sleep / 60;
//                                    int mins = sleep % 60;
//                                    timeStr = hours + "h " + mins + "m";
//                                } else {
//                                    timeStr = sleep + "m";
//                                }
//
////                            String printMessage = contract.getName() + " - It will take you " + timeStr + " to earn " + (contract.getTotal()- contract.getProgress());
//                                String printMessage = contract.getName() + " - It will take you " + timeStr + " to earn $" + String.format("%,d", (contract.getTotal() - contract.getProgress()));
//
//                                message.getChannel().flatMap(channel -> {
//                                    return channel.createMessage(printMessage);
//                                }).block();
//                            }
//                        } else {
//
//                            if (work) {
//                                int sleep = 0;
//                                sleep = profile.getStatus().getWork();
//                                printSimpleContract(contract, profile, message, sleep, "work", true).block();
//                            }
//                            if (tips) {
//                                int sleep = 0;
//                                sleep = profile.getStatus().getTips();
//                                printSimpleContract(contract, profile, message, sleep, "tips", true).block();
//                            }
//                            if (overtime) {
//                                int sleep = 0;
//                                sleep = profile.getStatus().getOt();
//                                printSimpleContract(contract, profile, message, sleep, "overtime", true).block();
//                            }
//
//                        }
//
//
//
////                        ReminderUtils.updateReminderSettings(reminderSettings);
//                        react(message, profile);
////                        if (shouldShowMessage) {
////                            return message.getChannel().flatMap(channel -> {
////                                return channel.createMessage(sb.toString());
////                            });
////                        }
//
//                    }
//                    if (contractTypeCheck(message, "Catering Contracts")){
////                        logger.info("2message is {}", message);
//
//                        Profile profile = null;
//                        String userId = getContainerUser(message);
//                        profile = ReminderUtils.loadProfileByUserName(userId);
//                        List<Contract> contracts = getContracts(message);
//                        for (Contract contract : contracts) {
//                            if (contract.getName().endsWith("Service Specialist")){
//                                tipsContracts(contract, profile, message).block();
//                            }
//                            if (contract.getName().endsWith("Overtime Specialist")){
//                                overtimeContracts(contract, profile, message).block();
//                            }
//                            if (contract.getName().endsWith("Shift Specialist")){
//                                workContracts(contract, profile, message).block();
//                            }
//                            if (contract.getName().endsWith("Double Time Specialist")){
//                                doubleTimeContracts(contract, profile, message).block();
//                            }
//                            if (contract.getName().endsWith("Corporate Restructuring")){
//                                restructuringContract(contract, profile, message).block();
//                            }
//                            if (contract.getName().contains("VIP")){
//                                if (contract.getObjective().endsWith("work")) {
//                                    workVIPContracts(contract, profile, message).block();
//                                }
//                                if (contract.getObjective().endsWith("overtime")) {
//                                    overtimeVIPContracts(contract, profile, message).block();
//                                }
//                                if (contract.getObjective().endsWith("tips")) {
//                                    tipsVIPContracts(contract, profile, message).block();
//                                }
//                            }
//                        }
//                        react(message, profile);
////                        react(message, reloadEmote);
//
//                    }
////                    if (contractTypeCheck(message, "Double Time Specialist")) {
////
////                        Profile profile = null;
////                        String userId = getContainerUser(message);
////                        profile = ReminderUtils.loadProfileByUserName(userId);
////                        react(message, profile);
////                        react(message, reloadEmote);
////                        logger.info("message is {}", message);
////                    }
//
//
//
////                        handleEmbedAction(message, checkEmbeds(message));
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
    protected Mono<Object> doReactionEvent(ReactionAddEvent reactionAddEvent) {

        try {

            if (reactionAddEvent.getEmoji().asUnicodeEmoji().isPresent())
                if (reactionAddEvent.getEmoji().asUnicodeEmoji().get().getRaw().equals(reloadEmote)) {
                    //got reaction
//                    Message message = reactionAddEvent.getMessage().block();

                    Message message = reactionAddEvent.getClient()
                            .withRetrievalStrategy(EntityRetrievalStrategy.REST)
                            .getMessageById(reactionAddEvent.getChannelId(), reactionAddEvent.getMessageId())
                            .block();
                    EmbedData embedData = null;
                    if (message.getEmbeds().size()> 0) {
                        embedData = message.getEmbeds().get(0).getData();
                    }

                    // need to update this to getContainerUser
//                    String messageAuthorId = getId(message, embedData);
                    String messageAuthorId = getContainerUser(message);
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
                                    String value = desc.split("collected a total of \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                    income = (int) (income / getEmbedPayout(desc));

                                createReminder(ReminderType.tips, message, desc, embed, income);
                            }
                            //work
                            if (desc.contains("\uD83D\uDC68\u200D\uD83C\uDF73") && desc.contains("** has cooked a total of")
                                    && !desc.contains("** while working overtime!")) {
                                int income = 0;
                                    String value = desc.split("tacos and earned \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                    income = (int) (income / getEmbedPayout(desc));
                                createReminder(ReminderType.work, message, desc, embed, income);
                            }
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working!")) {
                                int income = 0;
                                    String value = desc.split("tacos and earned \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                    income = (int) (income / getEmbedPayout(desc));


                                createReminder(ReminderType.work, message, desc, embed, income);
                            }
                            //ot
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working overtime!")) {
                                int income = 0;
                                    String value = desc.split("tacos and earned \\*\\*\\$")[1].split("\\*\\*")[0];
                                    value = value.replace(",", "");
                                    income = Integer.parseInt(value);
                                    income = (int) (income / getEmbedPayout(desc));

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
                                    createReminder(ReminderType.vote, message, profile, desc);
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
                                    createReminder(ReminderType.daily, message, profile, desc);
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
                                    createReminder(ReminderType.clean, message, profile, desc);
                                }

                            }
                            // event
                            // event clean
                            if (desc.startsWith("\u2705") && desc.contains("You have cleaned your team's shack")) {
                                AtomicReference<String> userId = new AtomicReference<>("");
                                userId.set(getId(message, embed));

                                Profile profile = ReminderUtils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.eventClean, message, profile, desc);
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

    public double getEmbedPayout(String description ) {
        Pattern pattern = Pattern.compile("\\*\\*([+-]\\d+)%\\*\\*\\s+\\w+\\s+Payout");
            for (String line : description.split("\n")) {
                Matcher m = pattern.matcher(line.trim());
                if (m.find()) {
                    logger.info("found payout: {}", m.group(1));
                    return 1 + (Integer.parseInt(m.group(1)) / 100.0);
                }
        }
        return 1;
    }

    public double getEmbedCooldown(String desc) {
        Pattern pattern = Pattern.compile("\\*\\*([+-]\\d+)%\\*\\*\\s+\\w+\\s+Cooldown");
        double cooldown = 0;

            for (String line : desc.split("\n")) {
//                logger.info("checking cooldown line: '{}'", line.trim());
                Matcher m = pattern.matcher(line.trim());
                if (m.find()) {
                    logger.info("found cooldown: {}", m.group(1));
                    cooldown += (Integer.parseInt(m.group(1)) / 100.0);
                }
            }
        return 1 + cooldown;
    }

    //create new command
    // pass in work,tips,ot modifiers
    //take your flex numbers and work out how many work/tips/ot you do in a week
    // work out the baseline value
    // work out the modified value



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
        createReminder(type, message, profile, desc);
    }

    private void createReminder(ReminderType type, Message message, Profile profile, String desc) {
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
//                sleep = (int) (sleep * reminderSettings.getWorkModifier());
                sleep = (int) (sleep * getEmbedCooldown(desc));
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
//                sleep = (int) (sleep * reminderSettings.getTipsModifier());
                sleep = (int) (sleep * getEmbedCooldown(desc));
                break;
            case ot:
                sleep = profile.getStatus().getOt();
                if (isRushHour(profile)) {
                    sleep = 15;
                }
                sleep = sleep * 60;

//                sleep = (int) (sleep * reminderSettings.getOvertimeModifier());
                sleep = (int) (sleep * getEmbedCooldown(desc));
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

//    public Map<String, Double> getCooldownMultipliers(Message message) {
//        Map<String, Double> result = new HashMap<>();
//        Possible<List<ComponentData>> possible = message.getData().components();
//        if (possible.isAbsent()) return result;
//
//        Pattern effectPattern = Pattern.compile("\\*\\*([+-]\\d+)%\\*\\*\\s+(.*)");
//        Pattern progressPattern = Pattern.compile("\\*\\*Progress:\\*\\*\\s*(?:Earn\\s+)?`\\$?([\\d,]+)/\\$?([\\d,]+)`");
//
//        for (ComponentData top : possible.get()) {
//            if (top.components().isAbsent()) continue;
//            for (ComponentData item : top.components().get()) {
//                if (item.type() != 10 || item.content().isAbsent()) continue;
//                String text = item.content().get();
//
//                // parse progress/total
//                Matcher progressMatcher = progressPattern.matcher(text);
//                if (progressMatcher.find()) {
//                    long progress = Long.parseLong(progressMatcher.group(1).replace(",", ""));
//                    long total = Long.parseLong(progressMatcher.group(2).replace(",", ""));
//                    result.put("progress", (double) progress);
//                    result.put("total", (double) total);
//                }
//
//                // parse active effects
//                if (!text.contains("Active Effects")) continue;
//                for (String line : text.split("\n")) {
//                    Matcher m = effectPattern.matcher(line.trim());
//                    boolean found = m.find();
//                    if (!found) continue;
//
//                    int percent = Integer.parseInt(m.group(1));
//                    String type = m.group(2).trim();
//                    double multiplier = 1 + (percent / 100.0);
//                    logger.info("adding '{}' multiplier: {}", type, multiplier);
//                    result.put(type, multiplier);
//                }
//            }
//        }
//        return result;
//    }

    public Contract getCooldownMultipliers(Message message) {
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return null;

        Pattern effectPattern = Pattern.compile("\\*\\*([+-]\\d+)%\\*\\*\\s+(.*)");
//        Pattern progressPattern = Pattern.compile("\\*\\*Progress:\\*\\*\\s*(?:\\w+\\s+)?`\\$?([\\d,]+)/\\$?([\\d,]+)`");
        Pattern progressPattern = Pattern.compile("\\*\\*Progress:\\*\\*\\s*[^`]*`\\$?([\\d,]+)/\\$?([\\d,]+)`");
        Pattern namePattern = Pattern.compile("^#{1,3}\\s+\\S+\\s+(.+)$");

        Contract contract = null;

        for (ComponentData top : possible.get()) {
            if (top.components().isAbsent()) continue;
            for (ComponentData item : top.components().get()) {
                if (item.type() != 10 || item.content().isAbsent()) continue;
                String text = item.content().get();

                // parse contract name
                Matcher nameMatcher = namePattern.matcher(text.trim());
                if (nameMatcher.find() && !text.contains("Active Contract")) {
                    if (contract == null) {
                        contract = new Contract(nameMatcher.group(1).trim(), null, null);
                    }
                }

                // parse progress/total
                Matcher progressMatcher = progressPattern.matcher(text);
                if (progressMatcher.find() && contract != null) {
                    contract.setProgress((int) Long.parseLong(progressMatcher.group(1).replace(",", "")));
                    contract.setTotal((int) Long.parseLong(progressMatcher.group(2).replace(",", "")));

                    // grab rewards line
                    for (String line : text.split("\n")) {
                        if (line.contains("Rewards:")) {
                            contract.setRewards(line.replaceAll(".*\\*\\*Rewards:\\*\\*\\s*", "").trim());
                        }
                        if (line.contains("Objective:") || line.contains("Progress:")) {
                            contract.setObjective(line.replaceAll(".*\\*\\*(Objective|Progress):\\*\\*\\s*", "").trim());
                        }
                    }
                }

                // parse active effects
                if (!text.contains("Active Effects")) continue;
                for (String line : text.split("\n")) {
                    Matcher m = effectPattern.matcher(line.trim());
                    boolean found = m.find();
                    if (!found) continue;

                    int percent = Integer.parseInt(m.group(1));
                    double multiplier = 1 + (percent / 100.0);
                    String type = m.group(2).trim();

                    if (contract == null) continue;

                    switch (type) {
                        case "Work Payout":     contract.setWorkBuff(multiplier); break;
                        case "Work Cooldown":   contract.setWorkCoolDown(multiplier); break;
                        case "Tips Payout":     contract.setTipsBuff(multiplier); break;
                        case "Tips Cooldown":   contract.setTipsCoolDown(multiplier); break;
                        case "Overtime Payout": contract.setOvertimeBuff(multiplier); break;
                        case "Overtime Cooldown": contract.setOvertimeCoolDown(multiplier); break;
                        default: logger.info("unknown effect type: {}", type); break;
                    }
                }
            }
        }
        return contract;
    }

    public List<Contract> getContracts(Message message) {
        List<Contract> contracts = new ArrayList<>();
        Possible<List<ComponentData>> possible = message.getData().components();
        if (possible.isAbsent()) return contracts;

//        Pattern namePattern = Pattern.compile("\\*\\*(.+?)\\*\\*\\s*$");
        Pattern namePattern = Pattern.compile("\\*\\*(.+?)\\*\\*");
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

                    logger.info("parsing section text: {}", text.substring(0, Math.min(50, text.length())));

                    for (String line : text.split("\n")) {
                        line = line.trim();

                        if (name == null && !line.startsWith("📌")) {
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

    public Mono<Object> tipsVIPContracts(Contract contract, Profile profile, Message message) {
        return VIPContracts(contract, profile, message, "tips");
    }

    public Mono<Object> workVIPContracts(Contract contract, Profile profile, Message message) {
        return VIPContracts(contract, profile, message, "work");
    }

    public Mono<Object> overtimeVIPContracts(Contract contract, Profile profile, Message message) {

        return VIPContracts(contract, profile, message, "overtime");
    }
    public Mono<Object> restructuringContract(Contract contract, Profile profile, Message message) {

        return VIPContracts(contract, profile, message, "work, tips and overtime");
    }

    public Mono<Object> VIPContracts(Contract contract, Profile profile, Message message, String type) {

        String obj = contract.getObjective().split("`")[1];
        int value = Integer.parseInt(obj.replace(",", "").replace("$", ""));

        int sleepWork = 0;
        sleepWork = profile.getStatus().getWork();
        int sleepTips = 0;
        sleepTips = profile.getStatus().getTips();
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
            sleepTips = sleepTips + 1;
        }

        int sleepOverTime = 0;
        sleepOverTime = profile.getStatus().getOt();
        boolean work = false;
        boolean overtime = false;
        boolean tips = false;
        if (type.equals("overtime")) {
            overtime = true;
        }
        if (type.equals("work")) {
            work = true;
        }
        if (type.equals("tips")) {
            tips = true;
        }
        if (type.equals("work, tips and overtime")) {
            tips = true;
            work = true;
            overtime = true;
        }
        if (profile.getOvertimeIncome() < 1 && profile.getWorkIncome() < 1 && profile.getTipsIncome() < 1) {
            return Mono.empty();
        }

        int sleep = minutesToCompleteEarningsGoal(profile, value, sleepWork, sleepOverTime, sleepTips, work, overtime, tips, 1, 1,1);
        String timeStr;
        if (sleep >= 60) {
            int hours = sleep / 60;
            int mins = sleep % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = sleep + "m";
        }
        String printMessage = contract.getName() + " - **With out the buffs**, it will take you " + timeStr + " to earn " + obj + " with "+ type;
        return message.getChannel().flatMap(channel -> {
            return channel.createMessage(printMessage);
        });
    }

    public Mono<Object> tipsContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getTips();
        return printSimpleContract(contract, profile, message, sleep, "tips", false);
    }

    public Mono<Object> workContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getWork();
        return printSimpleContract(contract, profile, message, sleep, "work", false);
    }

    public Mono<Object> overtimeContracts(Contract contract, Profile profile, Message message) {
        int sleep = 0;
        sleep = profile.getStatus().getOt();
        return printSimpleContract(contract, profile, message, sleep, "overtime", false);
    }

    public Mono<Object> doubleTimeContracts(Contract contract, Profile profile, Message message) {


        String obj = contract.getObjective().split("`")[1];
        int value = Integer.parseInt(obj.replace(",", "").replace("$", ""));

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

        if (profile.getOvertimeIncome() < 1 && profile.getWorkIncome() < 1 && profile.getTipsIncome() < 1) {
            return Mono.empty();
        }

        int sleep = minutesToCompleteEarningsGoal(profile, value, sleepWork, sleepOverTime, profile.getStatus().getTips(), true, true, false, 1, 1, 1);
        String timeStr;
        if (sleep >= 60) {
            int hours = sleep / 60;
            int mins = sleep % 60;
            timeStr = hours + "h " + mins + "m";
        } else {
            timeStr = sleep + "m";
        }
        String printMessage = contract.getName() + " - **With out the buffs**, it will take you " + timeStr + " to earn " + obj + " with overtime and work";
        return message.getChannel().flatMap(channel -> {
            return channel.createMessage(printMessage);
        });
    }

    public int minutesToCompleteEarningsGoal(Profile profile, long goal, int sleepWork, int sleepOvertime, int sleepTips, boolean includeWork, boolean includeOvertime, boolean includeTips, double workBuff, double overtimeBuff, double tipBuff) {
        logger.info("minutesToCompleteEarningsGoal - goal: {}, sleepWork: {}, sleepOvertime: {}, sleepTips: {}, includeWork: {}, includeOvertime: {}, includeTips: {}, workBuff: {}, overtimeBuff: {}, tipBuff: {}",
                goal, sleepWork, sleepOvertime, sleepTips, includeWork, includeOvertime, includeTips, workBuff, overtimeBuff, tipBuff);


        long totalEarned = 0;
        int totalMinutes = 0;

        int workCooldown = 0;
        int overtimeCooldown = 0;
        int tipsCooldown = 0;
        int max = 60*24*7*4;
        if (!includeWork && !includeOvertime && !includeTips) {
            return max;
        }


        while (totalEarned < goal && totalMinutes < max) {
            // advance time by 1 minute
            totalMinutes++;
            if (workCooldown > 0) workCooldown--;
            if (overtimeCooldown > 0) overtimeCooldown--;
            if (tipsCooldown > 0) tipsCooldown--;

            // do work if available
            if (workCooldown == 0 && includeWork) {
                totalEarned += (long) (profile.getWorkIncome()* workBuff);
                workCooldown = sleepWork;
            }

            // do overtime if available
            if (overtimeCooldown == 0 && includeOvertime) {
                totalEarned += (long) (profile.getOvertimeIncome()*  overtimeBuff);
                overtimeCooldown = sleepOvertime;
            }
            if (tipsCooldown == 0 && includeTips) {
                totalEarned += (long) (profile.getTipsIncome()*  tipBuff);
                tipsCooldown = sleepTips;
            }
        }

        return totalMinutes;
    }

    public Mono<Object>  printSimpleContract(Contract contract, Profile profile, Message message, int sleep, String type, boolean remianing){

        String obj = "";
        if (remianing) {
            obj = (contract.getTotal() - contract.getProgress()) + "";
        } else {
            obj = contract.getObjective().split("`")[1];
        }
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
        String printMessage = contract.getName() + " - It will take you " + timeStr + " to complete " + value + " " + type;
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

