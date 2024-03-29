package action;

import action.reminder.DoReminder;
import action.reminder.EmbedAction;
import action.reminder.ReminderType;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GiveawayAdd extends Action implements EmbedAction {

    String giveawayChannel;
    String tacoBot = "490707751832649738";

    public GiveawayAdd() {
        giveawayChannel = config.get("giveawayChannel");
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in


        if (message.getChannelId().asString().equals(giveawayChannel)) {
            if (message.getData().author().id().asString().equals(tacoBot)) {
                try {
                    //for some reason the embeds will be empty from slash, but if i load it again it will have data
//                    if (checkAge(message)) {
//                        checkMessageAgain(message);
//                    }

                    List<EmbedData> embedData;
                    if (message.getEmbeds().isEmpty() || message.getEmbeds().size() == 0) {

                        embedData = checkEmbeds(message);
                    } else {
                        embedData = message.getData().embeds();
                    }
                    handleEmbedAction(message, embedData);

                } catch (Exception e) {
                    printException(e);
                }

            }
        }
        return Mono.empty();
    }


    @Override
    public Mono<Object> handleEmbedAction(Message message, List<EmbedData> embedData) {

        try {
            for (EmbedData embed : embedData) {
                if (embed.description().toOptional().isPresent()) {


                    String line = embed.description().get();

                    if (line.contains(" You have received a gift of `$")) {

                        String amount = line.replace(" You have received a gift of `", "");
                        int index = amount.indexOf("$");
                        amount = amount.substring(index + 1);
                        amount = amount.replace(",", "");
                        String[] split = amount.split("` from ");
                        amount = split[0];
                        try {
                            Clean.addGift(Integer.parseInt(amount));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        logger.info("Added to gift");
                        logger.info(embed);
                        message.addReaction(ReactionEmoji.unicode("\uD83D\uDCB0")).block();
//                        doReminderCheck(message, embed);

                    }

                }
            }
        } catch (Exception e) {
            printException(e);
        }
        return Mono.empty();
    }

    private void doReminderCheck(Message message, EmbedData embedData) {
        AtomicReference<String> userId = new AtomicReference<>("");
        userId.set(getId(message, embedData));


        if (userId.get().equals("")) {
            List<MessageData> historic = getMessagesOfChannel(message);
            historic.forEach(messageData -> {
                Instant messageDataTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(messageData.timestamp(), Instant::from);
                if (Timestamp.from(messageDataTime).before(Timestamp.from(message.getTimestamp()))) {
                    String noSpace = messageData.content().toLowerCase().replace(" ", "");
                    if (noSpace.contains("!gift")) {
                        userId.set(messageData.author().id().toString());
                    }
                }
            });
        }
        Profile profile = ReminderUtils.loadProfileById(userId.get());
        if (profile != null) {
            Instant reminderTime = message.getTimestamp().plus(60 * 24, ChronoUnit.MINUTES);
            Reminder reminder = ReminderUtils.addMultipleReminder(profile.getName(), ReminderType.gift, Timestamp.from(reminderTime), message.getChannelId().asString());

            DoReminder doReminder = new DoReminder(gateway, client);
            doReminder.runReminder(reminder);
            react(message, profile);
        }

    }

    String defaultReact = "\uD83D\uDC4B";

    private void react(Message message, Profile profile) {
        if (!profile.getEnabled())
            return;
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


    public static List<MessageData> getMessagesOfChannel(Message message) {

        Snowflake time = Snowflake.of(message.getTimestamp().minus(15, ChronoUnit.SECONDS));
        return message.getRestChannel().getMessagesAfter(time).collectList().block();
    }
}
