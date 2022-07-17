package action;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class Left extends Action {


    String guildId;
    String param2;
    String log;
    String hitThread;

    public Left() {
        param = "has left the franchise!";
        param2 = "has been kicked from the franchise by";
        guildId = config.get("guildId");
        log = config.get("log");
        hitThread = config.get("hitThread");
    }

    @Override
    public Mono<Object> doAction(Message message) {


        try {

            if (message.getChannelId().asString().equals(log)) {
                if (message.getContent().contains(param)|| message.getContent().contains(param2)) {
                    logger.info("Left franchise " + message.getContent());

                    String memberId = message.getContent().split("`")[1].split("]")[0].substring(1);

                    logger.info("Member left " + memberId);

                    MemberData memberData;
                    try {
                        deleteOldMessages(memberId);

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

                    message.addReaction(ReactionEmoji.unicode("\uD83D\uDD2B")).block();
                    return Mono.empty();
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

    private void deleteOldMessages(String memberId) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("hitlist.txt")));
        String line;
        logger.info("looking for hitlist message");
        while ((line = br.readLine()) != null) {
            String[] lines = line.split(",");
            if (lines[0].equals(memberId)) {
                try {
                    logger.info("trying to  deleted " + lines[1]);
                    client.getChannelById(Snowflake.of(hitThread)).message(Snowflake.of(lines[1])).delete("old Message").block();
                    logger.info("message deleted " + lines[1]);
                } catch (Exception e) {
                    logger.info("message already deleted");
                }
            }
        }
        br.close();
    }

}
