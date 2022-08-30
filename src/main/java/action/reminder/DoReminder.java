package action.reminder;

import action.Action;
import action.GiveawayAdd;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.ReactionData;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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
                logger.info("reminder for " + reminder.getName() + " at " + formatter.format(reminderTime) + " of " + reminder.getType().getName() + " sleep for "+ delay.getSeconds());

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
        String msg = "Boo {ping} go do `{task}`!";

        if (profile.getMessage() != null && profile.getMessage().length() > 5) {
            msg = profile.getMessage();
        }
        msg = msg.replace("{ping}", "<@" + reminder.getName() + ">");
        msg = msg.replace("{task}", reminder.getType().getName());


        client.getChannelById(Snowflake.of(reminder.getChannel())).createMessage(msg).block();
        if (reminder.getType() == ReminderType.gift) {
            //for gifts only delete that reminder
            ReminderUtils.deleteReminder(reminder);
        } else {
            ReminderUtils.deleteReminder(reminder.getName(), reminder.getType());
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
