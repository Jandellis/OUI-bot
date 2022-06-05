package action;

import bot.Config;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class Welcome extends Action {


    String guildId;
    long chefRole;

    public Welcome() {
        param = "accept";
        guildId = config.get("guildId");
        chefRole = Long.parseLong(config.get("chefRole"));

    }

    @Override
    protected Mono<Object> doAction(Message message) {

        //watch for new users
            String action = getAction(message);
            if (action != null) {
                if (!message.getAuthor().isPresent()) {
                    logger.info("no author!!! " + action);
                } else {
                    User author = message.getAuthor().get();
                    client.getGuildById(Snowflake.of(guildId)).addMemberRole(
                            author.getId(),
                            Snowflake.of(chefRole),
                            "welcome").block();

                    logger.info("Member joined " + author.getId().asString());
                    StringBuilder sb = new StringBuilder();
                    sb.append("Welcome to OUI you now have access to the command `!ot` and an extra daily task in `!g`. You also should have some free menu slots.\r\n");
                    sb.append("Please follow our rules <#840895366287065099> and look at <#887022305581092904> to see what each channel is for.\r\n");
                    sb.append("If you have any questions reach out to a recruiter and they can help you out.");

                    author.getPrivateChannel().flatMap(channel -> {
                        channel.createMessage(sb.toString()).block();
                        logger.info("sent DM");
                        return Mono.empty();
                    }).block();
                }
            }

            return Mono.empty();
    }
}
