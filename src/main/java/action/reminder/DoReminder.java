package action.reminder;

import action.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.Embed;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TopLevelGuildChannel;
import discord4j.discordjson.json.EmojiData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.ReactionData;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DoReminder extends Action {

    String tacoBot = "490707751832649738";

    public DoReminder(GatewayDiscordClient gateway, DiscordClient client) {
            this.client = client;
            this.gateway = gateway;
    }

    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public void runReminder(Reminder reminder) {
        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running reminder");
                remind(reminder);
            }

        };

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = reminder.getTime().toLocalDateTime();
//        if (now.isBefore(reminderTime)) {
            Duration delay = Duration.between(now, reminderTime);
            logger.info("reminder for "+reminder.getName()+" at " + formatter.format(reminderTime) +" of " + reminder.getType().getName());

            executorService.schedule(taskWrapper, delay.getSeconds(), TimeUnit.SECONDS);
//        }

    }

    private void remind(Reminder reminder) {
        logger.info("Doing reminder for "+reminder.getName()+" of " + reminder.getType().getName());

        client.getChannelById(Snowflake.of(reminder.getChannel())).createMessage("Boo <@"+reminder.getName()+"> go do `"+reminder.getType().getName()+"`!").block();
        Utils.deleteReminder(reminder.getName(), reminder.getType());
    }

    public void startUp(){
        logger.info("Creating reminders on reboot");
        for (Reminder reminder: Utils.loadReminder()) {
            runReminder(reminder);
        }

        List<String> watchChannels = new ArrayList<>();
        watchChannels.add("841034380822577182");
        AtomicBoolean watched = new AtomicBoolean(false);

        watchChannels.forEach(channelId -> {

            List<MessageData> messageDataList = getMessagesOfChannel(client.getChannelById(Snowflake.of(channelId)));

            for (MessageData messageData : messageDataList) {
                boolean reacted = false;
                if (messageData.reactions().toOptional().isPresent()) {
                    for (ReactionData reaction:  messageData.reactions().get()) {
                        if (reaction.me()) {
                            reacted = true;
                        }
                    }
//                //messageData.reactions().get().
//                "\uD83D\uDC3E"
//                messageData.reactions().get().get(0).emoji().name().get()
                }

                if (!reacted) {
                    CreateReminder createReminder = new CreateReminder();
                    createReminder.action(gateway, client);

                    createReminder.doAction(gateway.getMessageById(Snowflake.of(channelId), Snowflake.of(messageData.id())).block());
                }

            }
        });





    }

    public static List<MessageData> getMessagesOfChannel(RestChannel channel){
        Snowflake time = Snowflake.of(Instant.now().minus(5, ChronoUnit.MINUTES));
        return channel.getMessagesAfter(time).collectList().block();
    }



    @Override
    public Mono<Object> doAction(Message message) {
        return Mono.empty();
    }
}
