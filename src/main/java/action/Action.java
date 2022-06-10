package action;

import bot.Config;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.MemberData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;


public abstract class Action {

    protected String param;
    protected Config config = Config.getInstance();
    protected GatewayDiscordClient gateway;
    protected DiscordClient client;
    protected String guildId;

    protected static final Logger logger = LogManager.getLogger("ouiBot");


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
        try {
            if (message.getContent().toLowerCase().startsWith(paramInput)) {
                logger.info(message.getContent());

                String temp = message.getContent().toLowerCase().replaceAll(paramInput + " ", "");
                String action = temp.split(" ")[0];
                logger.info("action " + action);
                return action;
            }
        } catch (Exception e) {
            printException(e);
        }
        return null;
    }

    protected boolean hasPermission(Message message, Long role) {
        try {
            MemberData memberData = null;

            memberData = client.getMemberById(Snowflake.of(guildId), message.getAuthor().get().getId()).getData().block();

            for (Id id : memberData.roles()) {
                if (id.asLong() == role)
                    return true;
            }

        } catch (Exception e) {
            printException(e);
        }
        return false;
    }

    protected void printException(Exception e) {
        logger.error("Exception", e);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();

        if (sStackTrace.length() > 1000) {
            sStackTrace = sStackTrace.substring(0, 999);
        }

        String finalSStackTrace = sStackTrace;
        gateway.getUserById(Snowflake.of("292839877563908097")).block().getPrivateChannel().flatMap(channel -> {
            channel.createMessage("**something broke!!**\r\n\r\n " + finalSStackTrace).block();
            logger.info("sent DM");
            return Mono.empty();
        }).block();
    }
}
