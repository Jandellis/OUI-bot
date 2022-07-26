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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CreateBoostReminder extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    String defaultReact = "\uD83D\uDC4B";
    List<Boost> boosts;

    public CreateBoostReminder() {

        boosts = new ArrayList<>();
        //city
        boosts.add(new Boost("Happy Hour", 4));
        boosts.add(new Boost("Samples", 4));
        boosts.add(new Boost("Mascot", 6));
        boosts.add(new Boost("Online Delivery", 8));
        boosts.add(new Boost("Bus Sign", 24));

        //shack
        boosts.add(new Boost("Rent-A-Chef", 4));
        boosts.add(new Boost("Live Music", 4));
        boosts.add(new Boost("Karaoke Night", 6));
        boosts.add(new Boost("Sign Flipper", 8));
        boosts.add(new Boost("Airplane Sign", 24));


        //beach
        boosts.add(new Boost("Concert", 4));
        boosts.add(new Boost("Hammock", 4));
        boosts.add(new Boost("Parasailing ", 6));
        boosts.add(new Boost("Beach Chairs", 8));
        boosts.add(new Boost("Helicopter Tours", 24));

        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in


        // add list of channels to watch
//        List<String> watchChannels = new ArrayList<>();
//        watchChannels.add("841034380822577182");
//        watchChannels.add("889662502324039690");
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
                            //boosts
                            if (desc.startsWith("\u2705") && desc.contains("You have purchased:")) {


                                List<MessageData> historic = getMessagesOfChannel(message.getRestChannel());
                                AtomicReference<String> userId = new AtomicReference<>("");

                                historic.forEach(messageData -> {

                                    String noSpace = messageData.content().toLowerCase().replace(" ", "");
                                    if (noSpace.contains("!buy")) {
                                        userId.set(messageData.author().id().toString());
                                    }
                                });
                                Profile profile = Utils.loadProfileById(userId.get());
                                if (profile != null) {

                                    for (Boost boost : boosts) {
                                        if (desc.contains(boost.name)) {

                                            createReminder(boost, message, profile);
                                        }
                                    }
                                }

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


    public static List<MessageData> getMessagesOfChannel(RestChannel channel) {
        Snowflake time = Snowflake.of(Instant.now().minus(15, ChronoUnit.SECONDS));
        return channel.getMessagesAfter(time).collectList().block();
    }


    private void createReminder(Boost boost, Message message, Profile profile) {

        Instant reminderTime = message.getTimestamp().plus(boost.duration, ChronoUnit.HOURS);
        react(message, profile);
        ReminderType type = ReminderType.getReminderType(boost.getName());

        Reminder reminder = Utils.addReminder(profile.getName(), type, Timestamp.from(reminderTime), message.getChannelId().asString());


        DoReminder doReminder = new DoReminder(gateway, client);
        doReminder.runReminder(reminder);
    }

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
