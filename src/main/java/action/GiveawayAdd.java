package action;

import action.reminder.DoReminder;
import action.reminder.Profile;
import action.reminder.Reminder;
import action.reminder.ReminderType;
import action.reminder.Utils;
import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.entity.RestChannel;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GiveawayAdd extends Action {

    String giveawayChannel;
    String tacoBot = "490707751832649738";

    public GiveawayAdd() {
        giveawayChannel = config.get("giveawayChannel");
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in


        if (message.getChannelId().asString().equals(giveawayChannel)) {
            if (message.getAuthor().get().getId().asString().equals(tacoBot)) {
                try {
                    for (Embed embed : message.getEmbeds()) {
                        if (embed.getDescription().isPresent()) {


                            String line = embed.getDescription().get();

                            if (line.contains(" You have sent a gift of `$")) {

                                String amount = line.replace(" You have sent a gift of `", "");
                                int index = amount.indexOf("$");
                                amount = amount.substring(index + 1);
                                amount = amount.replace(",", "");
                                String[] split = amount.split("` to ");
                                amount = split[0];
                                try {
                                    Clean.addGift(Integer.parseInt(amount));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                logger.info("Added to gift");
                                logger.info(embed.getData());
                                message.addReaction(ReactionEmoji.unicode("\uD83D\uDCB0")).block();
                                doReminderCheck(message);

                            }

                        }
                    }
                } catch (Exception e) {
                    printException(e);
                }

            }
        }
        return Mono.empty();
    }

    private void doReminderCheck(Message message){
        List<MessageData> historic = getMessagesOfChannel(message.getRestChannel());
        AtomicReference<String> userId = new AtomicReference<>("");

        historic.forEach(messageData -> {
            String noSpace = messageData.content().toLowerCase().replace(" ", "");
            if (noSpace.contains("!gift")) {
                userId.set(messageData.author().id().toString());
            }
        });
        Profile profile = Utils.loadProfileById(userId.get());
        if (profile != null) {
            Instant reminderTime = message.getTimestamp().plus(60*24, ChronoUnit.MINUTES);
            Reminder reminder = Utils.addMultipleReminder(profile.getName(), ReminderType.gift, Timestamp.from(reminderTime), message.getChannelId().asString());

            DoReminder doReminder = new DoReminder(gateway, client);
            doReminder.runReminder(reminder);
            react(message, profile);
        }

    }

    String defaultReact = "\uD83D\uDC4B";
    private void react(Message message, Profile profile) {
        String react = profile.getEmote();
        if (react == null || react.equals("")) {
            react = defaultReact;
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


    public static List<MessageData> getMessagesOfChannel(RestChannel channel) {
        Snowflake time = Snowflake.of(Instant.now().minus(15, ChronoUnit.SECONDS));
        return channel.getMessagesAfter(time).collectList().block();
    }
}
