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
                    boolean updated = false;
                    String response = "";
                    boolean enable = false;
                    boolean onOff = false;
                    if (action.equalsIgnoreCase("on")) {
                        enable = true;
                        onOff = true;
                        response = "Reminders on";
                    }
                    if (action.equalsIgnoreCase("off")) {
                        enable = false;
                        onOff = true;
                        response = "Reminders off";
                    }

                    if (onOff) {
                        updated = Utils.enableProfile(message.getAuthor().get().getId().asString(), enable);
                        if (!updated) {
                            message.getChannel().block().createMessage("Profile does not exists. Please type `!shack`").block();
                        } else {
                            message.getChannel().block().createMessage(response).block();
                        }
                    } else {
                        message.getChannel().block().createMessage("Sorry, i don't know what you mean. It should be `on` or `off`").block();
                    }
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
