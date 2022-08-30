package action;

import action.sm.model.SystemReminder;
import action.sm.model.SystemReminderType;
import action.sm.Utils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TopLevelGuildChannel;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpeedJar extends Action {


    String guildId;

    String speedJarChannel;
    String speedJarPing;
    String recruiter;
    String everyone;
    String customerBot = "526268502932455435";
//    String customerBot = "292839877563908097";

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SpeedJar() {
        param = "Speed Jar Started";
        guildId = config.get("guildId");
        speedJarChannel = config.get("speedJarChannel");
        speedJarPing = config.get("speedJarPing");
        recruiter = config.get("recruiter");
        //everyone = config.get("everyone");
        everyone = config.get("chefRole");

    }

    // on start up if no unlock look at last 15min
    // find start message and create lock and unlock time but dont print start message

    @Override
    public Mono<Object> doAction(Message message) {
        //✅ Speed Jar Started!
        //from 526268502932455435
        //lock in 15min for chef role
        //unlock 11.5h for chef role and send message to recruiter
        //
        try {
            if (message.getChannelId().asString().equals(speedJarChannel)) {
                if (message.getData().author().id().asString().equals(customerBot)) {
                    if (message.getContent().contains(param)) {

                        return message.getChannel().flatMap(channel -> {

//                            runLock(12);
//                            runUnlock(690);
//
//
//                            LocalDateTime unlockTime = Timestamp.from(message.getTimestamp()).toLocalDateTime().plusMinutes(690);
//                            Utils.addReminder(SystemReminderType.speedJarUnlock, Timestamp.valueOf(unlockTime));
//
//                            LocalDateTime lockTime = Timestamp.from(message.getTimestamp()).toLocalDateTime().plusMinutes(12);
//                            Utils.addReminder(SystemReminderType.speedJarLock, Timestamp.valueOf(lockTime));

                            create(message.getTimestamp());

                            return channel.createMessage("<@&" + speedJarPing + "> starting now!");
                        });
                    }
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

    private void create(Instant messageTime) {
//        runLock(12);
//        runUnlock(690);


        LocalDateTime unlockTime = Timestamp.from(messageTime).toLocalDateTime().plusMinutes(690);
        Utils.addReminder(SystemReminderType.speedJarUnlock, Timestamp.valueOf(unlockTime), "", "");

        LocalDateTime lockTime = Timestamp.from(messageTime).toLocalDateTime().plusMinutes(12);
        Utils.addReminder(SystemReminderType.speedJarLock, Timestamp.valueOf(lockTime), "", "");


        LocalDateTime localNow = LocalDateTime.now();
        runLock(ChronoUnit.MINUTES.between(localNow, lockTime));
        runUnlock(ChronoUnit.MINUTES.between(localNow, unlockTime));

    }

    private void lock() {

        Utils.deleteReminder(SystemReminderType.speedJarLock);
        PermissionSet deined = PermissionSet.of(Permission.SEND_MESSAGES);
        PermissionSet allowed = PermissionSet.none();


        PermissionOverwrite lock = PermissionOverwrite.forRole(Snowflake.of(everyone), allowed, deined);
        gateway.getGuildById(Snowflake.of(guildId)).block().
                getChannelById(Snowflake.of(speedJarChannel)).ofType(TopLevelGuildChannel.class).
                flatMap(guildChannel -> {
                            try {
                                logger.info("locking channel for " + everyone);
                                guildChannel.addRoleOverwrite(Snowflake.of(everyone), lock).block();
                            } catch (Exception e) {
                                printException(e);
                            }

                            return Mono.empty();
                        }
                ).block();

        client.getChannelById(Snowflake.of(speedJarChannel)).createMessage("Thanks for playing speed jar, locked channel for 11.5 hours").block();
    }

    private void unlock() {
        Utils.deleteReminder(SystemReminderType.speedJarUnlock);

        PermissionSet allowed = PermissionSet.of(Permission.SEND_MESSAGES);
        PermissionSet deined = PermissionSet.none();


        PermissionOverwrite lock = PermissionOverwrite.forRole(Snowflake.of(everyone), allowed, deined);
        gateway.getGuildById(Snowflake.of(guildId)).block().
                getChannelById(Snowflake.of(speedJarChannel)).ofType(TopLevelGuildChannel.class).
                flatMap(guildChannel -> {
                            try {
                                guildChannel.addRoleOverwrite(Snowflake.of(everyone), lock).block();
                            } catch (Exception e) {
                                printException(e);
                            }

                            return Mono.empty();
                        }
                ).block();

        client.getChannelById(Snowflake.of(speedJarChannel)).createMessage("<@&" + recruiter + "> Speed Jar unlocked\n\nPlease ping @Recruiter if you want to play").block();
    }


    public void runLock(long delay) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running lock");
                lock();
            }

        };

        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(delay);
        logger.info("lock at " + formatter.format(lockTime));


//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter("lock.txt"));
//            writer.write(formatter.format(lockTime));
//            writer.close();
//        } catch (IOException e) {
//            printException(e);
//        }


        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    public void runUnlock(long delay) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running unlock");
                unlock();
            }

        };

        LocalDateTime unlockTime = LocalDateTime.now().plusMinutes(delay);
        logger.info("unlock at " + formatter.format(unlockTime));
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter("unlock.txt"));
//            writer.write(formatter.format(unlockTime));
//            writer.close();
//        } catch (IOException e) {
//            printException(e);
//        }
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    public void startUp() throws IOException {

        //if file is empty it will not run tasks
        LocalDateTime lockTime = LocalDateTime.now().minusMinutes(1);
        LocalDateTime unlockTime = LocalDateTime.now().minusMinutes(1);

//        BufferedReader unlockReader = new BufferedReader(new FileReader(new File("unlock.txt")));
//        String line;
//        while ((line = unlockReader.readLine()) != null) {
//            unlockTime = LocalDateTime.parse(line, formatter);
//        }
//        unlockReader.close();
//
//        BufferedReader lockReader = new BufferedReader(new FileReader(new File("lock.txt")));
//        while ((line = lockReader.readLine()) != null) {
//            lockTime = LocalDateTime.parse(line, formatter);
//        }
//        lockReader.close();

        List<SystemReminder> lockReminder = Utils.loadReminder(SystemReminderType.speedJarLock);
        List<SystemReminder> unlockReminder = Utils.loadReminder(SystemReminderType.speedJarUnlock);



        LocalDateTime localNow = LocalDateTime.now();

//        if (localNow.isBefore(lockTime)) {
//            long delay = ChronoUnit.MINUTES.between(localNow, lockTime);
//            delay = delay + 1;
//            runLock(delay);
//        }
//
//        if (localNow.isBefore(unlockTime)) {
//            long delay = ChronoUnit.MINUTES.between(localNow, unlockTime);
//            delay = delay + 1;
//            runUnlock(delay);
//        }


        if (!lockReminder.isEmpty() ){
            lockTime = lockReminder.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, lockTime);
            runLock(delay);
        }
        if (!unlockReminder.isEmpty() ){
            unlockTime = unlockReminder.get(0).getTime().toLocalDateTime();
            long delay = ChronoUnit.MINUTES.between(localNow, unlockTime);
            runUnlock(delay);
        } else {

            Runnable taskWrapper = new Runnable() {

                @Override
                public void run() {
                    logger.info("checking for missed messages");


                    List<MessageData> messageDataList = getMessagesOfChannel(client.getChannelById(Snowflake.of(speedJarChannel)));

                    for (MessageData messageData : messageDataList) {
                        if (messageData.author().id().toString().equals(customerBot)) {
                            if (messageData.content().contains(param)) {
                                Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                                create(messageDataTime);
                            }
                        }

                    }
                }

            };

            executorService.schedule(taskWrapper, 15, TimeUnit.SECONDS);
        }


    }


    public static List<MessageData> getMessagesOfChannel(RestChannel channel) {
        Snowflake time = Snowflake.of(Instant.now().minus(15, ChronoUnit.MINUTES));
        return channel.getMessagesAfter(time).collectList().block();
    }
}
