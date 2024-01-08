package action.reminder;

import action.Action;
import action.GiveawayAdd;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.reminder.model.ReminderSettings;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.ReactionData;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DoReminder extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    String giveawayChannel;

    public DoReminder(GatewayDiscordClient gateway, DiscordClient client) {
        this.client = client;
        this.gateway = gateway;
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
        giveawayChannel = config.get("giveawayChannel");
    }

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(20);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * if reminder less than 1min, schedule
     * else
     * do nothing
     * <p>
     * have a scheduler running. every 60 sec check 90sec, 20 in the past 70 in the future. Only load reminders that are not locked
     * reminder table, add locked column
     * when picked up the reminder, set locked
     * <p>
     * on startup load all tasks, even locked one
     *
     * @param reminder
     */
    public void runReminder(Reminder reminder) {
        try {
            Runnable taskWrapper = new Runnable() {

                @Override
                public void run() {
                    logger.info("running reminder");
                    remind(reminder);
                }

            };

            LocalDateTime now = LocalDateTime.now().minusMinutes(10);
            LocalDateTime reminderTime = reminder.getTime().toLocalDateTime();
            if (now.isBefore(reminderTime)) {
                Duration delay = Duration.between(LocalDateTime.now(), reminderTime);

                if (delay.getSeconds() < 65) {
                    logger.info("reminder for " + reminder.getName() + " at " + formatter.format(reminderTime) + " of " + reminder.getType().getName() + " sleep for " + delay.getSeconds());

                    ReminderUtils.lockReminder(reminder);
                    executorService.schedule(taskWrapper, delay.getSeconds(), TimeUnit.SECONDS);
                } else {
                    ReminderUtils.unlockReminder(reminder);
                }
            } else {
                logger.info("deleting old reminder");
                {
                    ReminderUtils.deleteReminder(reminder.getName(), reminder.getType());
                }
            }
        } catch (Exception e) {
            printException(e);

        }

    }

    private void remind(Reminder reminder) {
        logger.info("Doing reminder for " + reminder.getName() + " of " + reminder.getType().getName());

//        List<Reminder> dbReminder = ReminderUtils.loadReminder(reminder);
//        if (dbReminder.size() == 0) {
//            logger.info("Reminder already deleted");
//            return;
//        }
//        if (reminder.getId() != -1) {
//            // this means we have an id, make sure its still in the db otherwise do nothing
//            List<Reminder> idReminder = ReminderUtils.loadReminder(reminder.getId());
//            if (idReminder.size() == 0) {
//                logger.info("Reminder id already deleted");
//                return;
//            }
//        }
//
//        boolean inDB = false;
//        for (Reminder rem : dbReminder) {
//            if (rem.getTime().equals(reminder.getTime())) {
//                inDB = true;
//            }
//        }
//        if (!inDB) {
//            logger.info("Reminder not in database, should be done already");
//            return;
//        }

        Profile profile = ReminderUtils.loadProfileById(reminder.getName());
        if (!profile.getEnabled())
            return;

        String msg = defaultReact + " Boo {ping} go do {cmd}!";

        //if shas sleep, check in sleep time
        //if in sleep time move reminder to end of sleep time

        if (profile.getSleepEnd() != null && profile.getSleepStart() != null) {
            LocalTime start = profile.getSleepStart().toLocalTime();
            LocalTime end = profile.getSleepEnd().toLocalTime();

            LocalTime now = LocalTime.now();

            if (start.isBefore(now)) {
                if (start.isBefore(end)) {
                    if (end.isAfter(now)) {
                        // start at 20, end at 24, time 22
                        //in range
                        LocalDateTime newTime = LocalDateTime.now();
                        long seconds = now.until(end, ChronoUnit.SECONDS);
                        newTime = newTime.plusSeconds(seconds);

                        ReminderUtils.addReminder(profile.getName(), reminder.getType(), Timestamp.valueOf(newTime), reminder.getChannel());
                        return;

                    }
                } else {
                    // start at 20, end at 4, time 22
                    //in range

                    //get time to 24h
                    //get time from 0 to end
                    LocalTime midnight = LocalTime.of(24, 0);
                    LocalTime morning = LocalTime.of(0, 0);
                    long seconds = now.until(midnight, ChronoUnit.SECONDS);
                    //add time until midnight
                    LocalDateTime newTime = LocalDateTime.now();
                    newTime = newTime.plusSeconds(seconds);

                    //add time from midnight until end of sleep
                    seconds = morning.until(end, ChronoUnit.SECONDS);
                    newTime = newTime.plusSeconds(seconds);

                    ReminderUtils.addReminder(profile.getName(), reminder.getType(), Timestamp.valueOf(newTime), reminder.getChannel());
                    return;
                }
            }
            if (end.isAfter(now)) {
                if (start.isAfter(end)) {
                    // start at 20, end at 4, time 2
                    //in range
                    LocalDateTime newTime = LocalDateTime.now();
                    long seconds = now.until(end, ChronoUnit.SECONDS);
                    newTime = newTime.plusSeconds(seconds);

                    ReminderUtils.addReminder(profile.getName(), reminder.getType(), Timestamp.valueOf(newTime), reminder.getChannel());
                    return;
                }
            }
        }

        ReminderSettings reminderSettings = ReminderUtils.loadReminderSettings(profile.getName());
        if (reminderSettings == null) {
            reminderSettings = new ReminderSettings(profile.getName(), true, true, true, true, true, true, true);
        }


        if (profile.getMessage() != null && profile.getMessage().length() > 5) {
            msg = profile.getMessage();
        }
        boolean hasTask = false;
        if (msg.contains("{task}")) {
            hasTask = true;
        }

        if (profile.getDnd()) {
            msg = msg.replace("{ping}", profile.getUserName() );
        } else {
            msg = msg.replace("{ping}", "<@" + reminder.getName() + ">");
        }
        msg = msg.replace("{task}", reminder.getType().getName());

        String command = "";
        boolean doReminder = true;

        switch (reminder.getType()) {
            case work:
                command = "</work:1006354978274820109>";
                doReminder = reminderSettings.isWork();
                break;
            case ot:
                command = "</overtime:1006354977981210646>";
                doReminder = reminderSettings.isOvertime();
                break;
            case tips:
                command = "</tips:1006354978153169013>";
                doReminder = reminderSettings.isTip();
                break;
            case vote:
                command = "</vote link:1006354978274820108>";
                doReminder = reminderSettings.isVote();
                break;
            case clean:
                command = "</clean:1006354977721176143>";
                doReminder = reminderSettings.isClean();
                break;
            case daily:
                command = "</daily:1006354977788268621>";
                doReminder = reminderSettings.isDaily();
                break;
            case gift:
                command = "</gift:1006354977847001160>";
                break;
            case importData:
                command = "</franchise memberdata export:1006354977788268626>";
                break;
            case postAd:
                command = reminder.getType().getName() + " <#663937997313540128> </postad:1077546065559035964>";
                break;
            default:
                //boosts off cooldown
                command = "</shop:1006354978153169007> at " + CreateBoostReminder.getBoost(reminder.getType().getName()).getLocation().getName();
                doReminder = reminderSettings.isBoost();
                if (!hasTask) {
                    command = command + " **" + reminder.getType().getName() + "**";
                }
        }

        msg = msg.replace("{cmd}", command);


        //only do reminder if its been enabled
        if (doReminder) {
            if (profile.getDmReminder()) {
                String finalMsg = "**Reminder from <#" + reminder.getChannel() + ">** \r\n" + msg;
                gateway.getUserById(Snowflake.of(profile.getName())).block().getPrivateChannel().flatMap(channel -> {
                    channel.createMessage(finalMsg).block();
                    logger.info("sent DM");
                    return Mono.empty();
                }).block();
            } else {
                client.getChannelById(Snowflake.of(reminder.getChannel())).createMessage(msg).block();
            }
            if (reminder.getType() == ReminderType.gift) {
                //for gifts only delete that reminder
                ReminderUtils.deleteReminder(reminder);
            } else {
                ReminderUtils.deleteReminder(reminder.getName(), reminder.getType());
            }
        }
    }

    public void startUp() {

        logger.info("Creating reminders on reboot");
        for (Reminder reminder : ReminderUtils.loadReminder()) {
            runReminder(reminder);
        }

        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("checking for missed messages");

                try {

                    watchChannels.forEach(channelId -> {

                        // add error handing around this
                        List<MessageData> messageDataList = getMessagesOfChannel(client.getChannelById(Snowflake.of(channelId)));

                        for (MessageData messageData : messageDataList) {
                            boolean reacted = false;
                            if (messageData.reactions().toOptional().isPresent()) {
                                for (ReactionData reaction : messageData.reactions().get()) {
                                    if (reaction.me()) {
                                        reacted = true;
                                    }
                                }
                            }

                            if (!reacted) {
                                CreateReminder createReminder = new CreateReminder();
                                createReminder.action(gateway, client);

                                createReminder.doAction(gateway.getMessageById(Snowflake.of(channelId), Snowflake.of(messageData.id())).block());
                            }
                        }
                    });


                    List<MessageData> messageDataList = getMessagesOfChannel(client.getChannelById(Snowflake.of(giveawayChannel)));

                    for (MessageData messageData : messageDataList) {
                        boolean reacted = false;
                        if (messageData.reactions().toOptional().isPresent()) {
                            for (ReactionData reaction : messageData.reactions().get()) {
                                if (reaction.me()) {
                                    reacted = true;
                                }
                            }
                        }

                        if (!reacted) {
                            GiveawayAdd giveawayAdd = new GiveawayAdd();
                            giveawayAdd.action(gateway, client);

                            giveawayAdd.doAction(gateway.getMessageById(Snowflake.of(giveawayChannel), Snowflake.of(messageData.id())).block());
                        }
                    }
                } catch (Exception e) {
                    printException(e);

                }

                while (true) {
                    try {
                        logger.info("Sleeping for 60sec until next reminder check");
                        Thread.sleep(60000);
                        logger.info("doing reminder check");
                        scheduleReminders();
                    } catch (Throwable e) {
                        printException(e);
                    }
                }

            }

        };

        executorService.schedule(taskWrapper, 15, TimeUnit.SECONDS);

    }

    public static List<MessageData> getMessagesOfChannel(RestChannel channel) {
        Snowflake time = Snowflake.of(Instant.now().minus(15, ChronoUnit.MINUTES));
        return channel.getMessagesAfter(time).collectList().block();
    }


    @Override
    public Mono<Object> doAction(Message message) {
        return Mono.empty();
    }

    public void scheduleReminders() {

        for (Reminder reminder : ReminderUtils.loadReminderWindow()) {
            runReminder(reminder);
        }
    }
}
