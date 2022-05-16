package action;

import bot.Config;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class Action {

    String param;
    Config config = Config.getInstance();
    GatewayDiscordClient gateway;
    DiscordClient client;

    public Mono<Void> action(GatewayDiscordClient gateway, DiscordClient client) {
        this.client = client;
        this.gateway = gateway;
        return gateway.on(MessageCreateEvent.class, event -> doAction(event.getMessage())).then();
    }

    protected abstract Mono<Object> doAction(Message message);

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
