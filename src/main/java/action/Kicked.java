package action;

import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Kicked extends Action {


    String guildId;
    String param2;
    String hitThread;
    Long recruiter;

    public Kicked() {
        param = "!f kick";
        param2 = "! f kick";
        guildId = config.get("guildId");
        hitThread = config.get("hitThread");
        recruiter = Long.parseLong(config.get("recruiter"));
    }

    @Override
    public Mono<Object> doAction(Message message) {

        //watch for kicked users
        String action = getAction(message);
        if (action == null || !hasPermission(message, recruiter)) {
            action = getAction(message, param2);
        }


        if (action != null) {
            String finalAction = action;
            return message.getChannel().flatMap(channel -> {

                try {
                    Clean.kickMember(finalAction);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.info("Member kicked " + finalAction);

                MemberData memberData;
                try {
                    deleteOldMessages(finalAction);

                    memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(finalAction)).getData().block();

                    memberData.roles().forEach(id -> {
                        logger.info("Removed role " + id);
                        client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                Snowflake.of(finalAction),
                                Snowflake.of(id),
                                "user kicked").block();
                    });
                } catch (ClientException | IOException e) {
                    logger.info("user left the server " + finalAction);
                }
                return Mono.empty();
            });
        }

        return Mono.empty();
    }

    private void deleteOldMessages(String memberId) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("hitlist.txt")));
        String line;
        while ((line = br.readLine()) != null) {
            String[] lines = line.split(",");
            if (lines[0].equals(memberId)) {
                try {
                    client.getChannelById(Snowflake.of(hitThread)).message(Snowflake.of(lines[1])).delete("old Message").block();
                } catch ( Exception e) {
                    logger.info("message already deleted");
                }
            }
        }
        br.close();
    }
}
