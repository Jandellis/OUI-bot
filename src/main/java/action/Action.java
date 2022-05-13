package action;

import bot.Config;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class Action {

    String param;
    Config config = Config.getInstance();

    public abstract Mono<Void> action(GatewayDiscordClient gateway, DiscordClient client);

    String getAction(Message message) {
        return getAction(message, param);
    }

    String getAction(Message message, String paramInput) {
        if (message.getContent().toLowerCase().startsWith(paramInput)) {
            System.out.println(message.getContent());

            String temp = message.getContent().toLowerCase().replaceAll(paramInput + " ", "");
            String action = temp.split(" ")[0];
            System.out.println("action " + action);
            return action;
        }
        return null;
    }
}
