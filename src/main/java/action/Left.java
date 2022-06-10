package action;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;


public class Left extends Action {


    String guildId;
    String param2;
    String log;

    public Left() {
        param = "has left the franchise!";
        guildId = config.get("guildId");
        log = config.get("log");
    }

    @Override
    public Mono<Object> doAction(Message message) {


        try {

            if (message.getChannelId().asString().equals(log)) {
                if (message.getContent().contains(param)) {
                    logger.info("Left franchise " + message.getContent());

                    String memberId = message.getContent().split("`")[1].split("]")[0].substring(1);

                    logger.info("Member left " + memberId);

                    MemberData memberData;
                    try {

                        memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(memberId)).getData().block();

                        memberData.roles().forEach(id -> {
                            logger.info("Removed role " + id);
                            client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                    Snowflake.of(memberId),
                                    Snowflake.of(id),
                                    "user kicked").block();
                        });
                    } catch (ClientException e) {
                        logger.info("user left the server " + memberId);
                    }
                    return Mono.empty();
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

}
