package action;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class Action {

    String param;
    public abstract Mono<Void> action(GatewayDiscordClient gateway, DiscordClient client);

    String getAction(Message message) {
        if (message.getContent().toLowerCase().startsWith(param)) {
            System.out.println(message.getContent());

            String temp = message.getContent().toLowerCase().replaceAll(param + " ", "");
            String action = temp.split(" ")[0];
            System.out.println("action " + action);
            return action;
        }
        return null;
    }
}
