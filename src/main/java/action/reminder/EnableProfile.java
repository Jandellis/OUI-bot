package action.reminder;

import action.Action;
import bot.Sauce;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.EmbedAuthorData;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EnableProfile extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public EnableProfile() {
        param = "ouirm";
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {

            AtomicBoolean watched = new AtomicBoolean(false);

            watchChannels.forEach(channel -> {
                if (message.getChannelId().asString().equals(channel)) {
                    watched.set(true);
                }
            });
            //if in watch channel
            if (watched.get()) {

                String action = getAction(message);

                if (action != null) {
                    if (action.equalsIgnoreCase("on")) {
                        Utils.enableProfile(message.getAuthor().get().getId().asString(), true);
                        message.getChannel().block().createMessage("Reminders on").block();
                    }
                    if (action.equalsIgnoreCase("off")) {
                        Utils.enableProfile(message.getAuthor().get().getId().asString(), false);
                        message.getChannel().block().createMessage("Reminders off").block();
                    }
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
