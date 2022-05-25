package action;

import bot.KickMember;
import discord4j.common.util.Snowflake;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TopLevelGuildChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    @Override
    public Mono<Object> doAction(Message message) {
        //✅ Speed Jar Started!
        //from 526268502932455435
        //lock in 15min for chef role
        //unlock 11.5h for chef role and send message to recruiter
        //

        if (message.getChannelId().asString().equals(speedJarChannel)) {
            if (message.getAuthor().get().getId().asString().equals(customerBot)) {
                if (message.getContent().contains(param)) {

                    return message.getChannel().flatMap(channel -> {

                        runLock(12);
                        runUnlock(690);
                        return channel.createMessage("<@&" + speedJarPing + "> starting now!");
                    });
                }
            }
        }

        return Mono.empty();
    }

    private void lock() {
        PermissionSet deined = PermissionSet.of(Permission.SEND_MESSAGES);
        PermissionSet allowed = PermissionSet.none();


        PermissionOverwrite lock = PermissionOverwrite.forRole(Snowflake.of(everyone), allowed, deined);
        gateway.getGuildById(Snowflake.of(guildId)).block().
                getChannelById(Snowflake.of(speedJarChannel)).ofType(TopLevelGuildChannel.class).
                flatMap(guildChannel ->
                        guildChannel.addRoleOverwrite(Snowflake.of(everyone), lock)
                ).block();

        client.getChannelById(Snowflake.of(speedJarChannel)).createMessage("Thanks for playing speed jar, locked channel for 11.5 hours").block();
    }

    private void unlock() {
        PermissionSet allowed = PermissionSet.of(Permission.SEND_MESSAGES);
        PermissionSet deined = PermissionSet.none();


        PermissionOverwrite lock = PermissionOverwrite.forRole(Snowflake.of(everyone), allowed, deined);
        gateway.getGuildById(Snowflake.of(guildId)).block().
                getChannelById(Snowflake.of(speedJarChannel)).ofType(TopLevelGuildChannel.class).
                flatMap(guildChannel ->
                        guildChannel.addRoleOverwrite(Snowflake.of(everyone), lock)
                ).block();

        client.getChannelById(Snowflake.of(speedJarChannel)).createMessage("<@&" + recruiter + "> Speed Jar unlocked").block();
    }


    public void runLock(long delay) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                System.out.println("running lock");
                lock();
            }

        };

        LocalDateTime lockTime = LocalDateTime.now().plusMinutes(delay);
        System.out.println("lock at " + formatter.format(lockTime));


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("lock.txt"));
            writer.write(formatter.format(lockTime));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    public void runUnlock(long delay) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                System.out.println("running unlock");
                unlock();
            }

        };

        LocalDateTime unlockTime = LocalDateTime.now().plusMinutes(delay);
        System.out.println("unlock at " + formatter.format(unlockTime));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("unlock.txt"));
            writer.write(formatter.format(unlockTime));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    public void startUp() throws IOException {

        //if file is empty it will not run tasks
        LocalDateTime lockTime = LocalDateTime.now().minusMinutes(1);
        LocalDateTime unlockTime = LocalDateTime.now().minusMinutes(1);

        BufferedReader unlockReader = new BufferedReader(new FileReader(new File("unlock.txt")));
        String line;
        while ((line = unlockReader.readLine()) != null) {
            unlockTime = LocalDateTime.parse(line, formatter);
        }
        unlockReader.close();

        BufferedReader lockReader = new BufferedReader(new FileReader(new File("lock.txt")));
        while ((line = lockReader.readLine()) != null) {
            lockTime = LocalDateTime.parse(line, formatter);
        }
        lockReader.close();

        LocalDateTime localNow = LocalDateTime.now();

        if (localNow.isBefore(lockTime)){
            long delay = ChronoUnit.MINUTES.between(localNow, lockTime);
            runLock(delay);
        }

        if (localNow.isBefore(unlockTime)){
            long delay = ChronoUnit.MINUTES.between(localNow, unlockTime);
            runUnlock(delay);
        }


    }
}
