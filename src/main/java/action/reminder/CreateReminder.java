package action.reminder;

import action.Action;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CreateReminder extends Action {

    String giveawayChannel;
    String tacoBot = "490707751832649738";

    public CreateReminder() {
        giveawayChannel = config.get("giveawayChannel");
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in


        // add list of channels to watch
        List<String> watchChannels = new ArrayList<>();
        watchChannels.add("841034380822577182");
        watchChannels.add("889662502324039690");
        AtomicBoolean watched = new AtomicBoolean(false);

        watchChannels.forEach(channel -> {
            if (message.getChannelId().asString().equals(channel)) {
                watched.set(true);
            }
        });
        //if in watch channel
        if (watched.get()) {
            if (message.getAuthor().get().getId().asString().equals(tacoBot)) {
                try {
                    for (Embed embed : message.getEmbeds()) {

                        if (embed.getDescription().isPresent()) {
                            String desc = embed.getDescription().get();
                            //tips
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** in tips!")){

                                createReminder(ReminderType.tips, message, desc);
                            }
                            //work
                            if (desc.contains("\uD83D\uDC68\u200D\uD83C\uDF73") && desc.contains("** has cooked a total of")
                                    && !desc.contains("** while working overtime!")){
                                createReminder(ReminderType.work, message, desc);
                            }
                            //ot
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working overtime!")){
                                createReminder(ReminderType.ot, message, desc);

                            }

                            //vote
                            if (desc.startsWith("\u2705") && desc.contains("Voting Daily Streak Progress")){
                                //go look for history and find the last message that has claim and use that for the userid
                                List<MessageData> historic = getMessagesOfChannel(message.getRestChannel());
//                                Collections.reverse(historic);
                                AtomicReference<String> userId = new AtomicReference<>("");

                                historic.forEach(messageData -> {
                                    if (messageData.content().toLowerCase().contains("claim")) {
                                        userId.set(messageData.author().id().toString());
                                    }
                                });
                                Profile profile = Utils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.vote, message, profile);
                                }

                            }

                            //daily
                            if (desc.startsWith("\u2705") && desc.contains("Daily Streak Progress")){
                                List<MessageData> historic = getMessagesOfChannel(message.getRestChannel());
//                                Collections.reverse(historic);
                                AtomicReference<String> userId = new AtomicReference<>("");

                                historic.forEach(messageData -> {
                                    if (messageData.content().toLowerCase().contains("d") || messageData.content().toLowerCase().contains("daily")) {
                                        userId.set(messageData.author().id().toString());
                                    }
                                });
                                Profile profile = Utils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.daily, message, profile);
                                }

                            }
                            //clean
                            if (desc.startsWith("\u2705") && desc.contains("You have cleaned all of your locations!")){

                                List<MessageData> historic = getMessagesOfChannel(message.getRestChannel());
//                                Collections.reverse(historic);
                                AtomicReference<String> userId = new AtomicReference<>("");

                                historic.forEach(messageData -> {
                                    if (messageData.content().toLowerCase().contains("clean")) {
                                        userId.set(messageData.author().id().toString());
                                    }
                                });
                                Profile profile = Utils.loadProfileById(userId.get());
                                if (profile != null) {
                                    createReminder(ReminderType.clean, message, profile);
                                }

                            }
                        }

                        // react with emots for what reminders i dont have








                    }
                } catch (Exception e) {
                    printException(e);
                }

            }
        }
        return Mono.empty();
    }

    public static List<MessageData> getMessagesOfChannel(RestChannel channel){
        Snowflake time = Snowflake.of(Instant.now().minus(15, ChronoUnit.SECONDS));
        return channel.getMessagesAfter(time).collectList().block();
    }



    private void createReminder(ReminderType type, Message message, String desc) {

        String name = desc.split("\\*\\*")[1];
        Profile profile = Utils.loadProfileByName(name);
        if (profile == null) {
            logger.info("No profile found for "+ name);
            return ;
        }
        createReminder(type, message, profile);
    }

    private void createReminder(ReminderType type, Message message, Profile profile) {
        int sleep = 0;
        switch (type){
            case work:
                sleep = profile.getStatus().getWork();
                break;
            case tips:
                sleep = profile.getStatus().getTips();
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

        message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();

        Reminder reminder = Utils.addReminder(profile.getName(), type,Timestamp.from(reminderTime), message.getChannelId().asString());

        List<Reminder> reminders = Utils.loadReminder(profile.getName());
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
        DoReminder doReminder = new DoReminder(gateway, client);
        doReminder.runReminder(reminder);
    }

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
