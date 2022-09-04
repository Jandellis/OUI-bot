package action.reminder;

import action.Action;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class React extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public React() {
        param = "cyreact";
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {

            AtomicBoolean watched = new AtomicBoolean(true);

//            watchChannels.forEach(channel -> {
//                if (message.getChannelId().asString().equals(channel)) {
//                    watched.set(true);
//                }
//            });
            //if in watch channel
            if (watched.get()) {

                String action = getAction(message);

                if (action != null) {
                    if (action.equalsIgnoreCase("delete")) {

                        ReminderUtils.deleteReact(message.getAuthor().get().getId().asString());
                        message.getChannel().block().createMessage("Reaction deleted").block();
                    } else {
                        if (action.startsWith("<")) {

                            String[] emote = action.split(":");
                            Long id = Long.parseLong(emote[2].replace(">", ""));
                            String name = emote[1];
                            boolean animated = true;
                            message.addReaction(ReactionEmoji.of(id, name, animated)).block();
                        } else {
                            message.addReaction(ReactionEmoji.unicode(action)).block();
                        }
                        ReminderUtils.addReact(message.getAuthor().get().getId().asString(), action);
                    }
                }

            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
