package slash.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * A simple interface defining our slash command class contract.
 *  a getName() method to provide the case-sensitive name of the command.
 *  and a handle() method which will house all the logic for processing each command.
 *  https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-naming
 *  docs for how to write the json
 */
public abstract class SlashCommand {

    public abstract String getName();

    public abstract Mono<Void> handle(ChatInputInteractionEvent event);
    protected static final Logger logger = LogManager.getLogger("ouiBot");

    protected String getParameter(String name, String defaultValue, ChatInputInteractionEvent event)  {
        String value = defaultValue;
        Optional<String> valuePresent = event.getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);
        if (valuePresent.isPresent()) {
            value = valuePresent.get();
        }
        return value;
    }

    protected Boolean getParameter(String name, Boolean defaultValue, ChatInputInteractionEvent event)  {
        Boolean value = defaultValue;
        Optional<Boolean> valuePresent = event.getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean);
        if (valuePresent.isPresent()) {
            value = valuePresent.get();
        }
        return value;
    }
    protected Long getParameter(String name, Long defaultValue, ChatInputInteractionEvent event)  {
        Long value = defaultValue;
        Optional<Long> valuePresent = event.getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong);
        if (valuePresent.isPresent()) {
            value = valuePresent.get();
        }
        return value;
    }


    protected void printException(Throwable e, GatewayDiscordClient gateway) {
        try {
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
        } catch (Throwable e2) {

            logger.error("Exception", e2);
        }
    }
}
