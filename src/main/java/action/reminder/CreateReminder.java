package action.reminder;

import action.Action;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
//                                String name = desc.split("\\*\\*")[1];
//                                Profile profile = Utils.loadProfileByName(name);
//                                if (profile == null) {
//                                    return Mono.empty();
//                                }
//                                Instant reminderTime = message.getTimestamp().plus(profile.getStatus().getTips(), ChronoUnit.MINUTES);
//
//                                message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();
//
//                                Reminder reminder = Utils.addReminder(profile.getName(), ReminderType.tips,Timestamp.from(reminderTime), message.getChannelId().asString());
//                                DoReminder doReminder = new DoReminder(gateway, client);
//                                doReminder.runReminder(reminder);
                            }
                            //work -- to do
                            if (desc.contains("\uD83D\uDC68\u200D\uD83C\uDF73") && desc.contains("** has cooked a total of")
                                    && !desc.contains("** while working overtime!")){
                                createReminder(ReminderType.work, message, desc);
//                                String name = desc.split("\\*\\*")[1];
//                                Profile profile = Utils.loadProfileByName(name);
//                                Instant reminderTime = message.getTimestamp().plus(profile.getStatus().getWork(), ChronoUnit.MINUTES);
//
//                                message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();
//
//                                Reminder reminder = Utils.addReminder(profile.getName(), ReminderType.work,Timestamp.from(reminderTime), message.getChannelId().asString());
//                                DoReminder doReminder = new DoReminder(gateway, client);
//                                doReminder.runReminder(reminder);
                            }
                            //ot
                            if (desc.startsWith("\uD83D\uDCB5") && desc.contains("** while working overtime!")){
                                createReminder(ReminderType.ot, message, desc);

//                                String name = desc.split("\\*\\*")[1];
//                                Profile profile = Utils.loadProfileByName(name);
//                                Instant reminderTime = message.getTimestamp().plus(profile.getStatus().getWork(), ChronoUnit.MINUTES);
//
//                                message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();
//
//                                Reminder reminder = Utils.addReminder(profile.getName(), ReminderType.ot,Timestamp.from(reminderTime), message.getChannelId().asString());
//                                DoReminder doReminder = new DoReminder(gateway, client);
//                                doReminder.runReminder(reminder);
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


    private void createReminder(ReminderType type, Message message, String desc) {
        String name = desc.split("\\*\\*")[1];
        Profile profile = Utils.loadProfileByName(name);
        if (profile == null) {
            logger.info("No profile found for "+ name);
            return ;
        }
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
                break;
            case daily:
                break;
            case clean:
                break;
        }




        Instant reminderTime = message.getTimestamp().plus(sleep, ChronoUnit.MINUTES);

        message.addReaction(ReactionEmoji.unicode("\uD83D\uDC4B")).block();

        Reminder reminder = Utils.addReminder(profile.getName(), type,Timestamp.from(reminderTime), message.getChannelId().asString());
        DoReminder doReminder = new DoReminder(gateway, client);
        doReminder.runReminder(reminder);
    }
}
