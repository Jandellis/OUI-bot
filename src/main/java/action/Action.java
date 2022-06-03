package action;

import bot.Config;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MemberData;
import reactor.core.publisher.Mono;

public abstract class Action {

    protected String param;
    protected Config config = Config.getInstance();
    protected GatewayDiscordClient gateway;
    protected DiscordClient client;
    protected String guildId;

    public Mono<Void> action(GatewayDiscordClient gateway, DiscordClient client) {
        this.client = client;
        this.gateway = gateway;
        guildId = config.get("guildId");
        return gateway.on(MessageCreateEvent.class, event -> doAction(event.getMessage())).then();
    }

    protected abstract Mono<Object> doAction(Message message);

    protected String getAction(Message message) {
        return getAction(message, param.toLowerCase());
    }

    protected String getAction(Message message, String paramInput) {
        if (message.getContent().toLowerCase().startsWith(paramInput)) {
            System.out.println(message.getContent());

            String temp = message.getContent().toLowerCase().replaceAll(paramInput + " ", "");
            String action = temp.split(" ")[0];
            System.out.println("action " + action);
            return action;
        }
        return null;
    }

    protected boolean hasPermission(Message message, Long role) {
        MemberData memberData = null;

        memberData = client.getMemberById(Snowflake.of(guildId), message.getAuthor().get().getId()).getData().block();

        for (Id id : memberData.roles()) {
            if (id.asLong() == role)
                return true;
        }
        return false;
    }
}
