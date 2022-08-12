package action.sm;

import action.Action;
import bot.Sauce;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.EmbedAuthorData;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class CleanUp extends Action {

    String bbBot = "801210683483619438";
    String smChannel;
    static long chefRole;

    public CleanUp() {
        param = "based on your Sauce Market portfolio";
        smChannel = config.get("smChannel");
        chefRole = Long.parseLong(config.get("chefRole"));
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {

            if (message.getChannelId().asString().equals(smChannel)) {
                if (message.getAuthor().isPresent() && message.getAuthor().get().getId().asString().equals(bbBot)) {
                    if (message.getContent().contains(param)) {
                        try {
                            logger.info(message.getContent());
                            String id = message.getContent().split(">")[0].split("@")[1];
                            if (!hasPermission(id, chefRole)) {
                                message.delete().block();
                                logger.info("Message deleted");
                            }
                        } catch (Exception e) {
                            printException(e);
                        }
                    }

                }
            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
