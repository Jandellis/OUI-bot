package action;

import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class Kicked extends Action {


    String guildId;
    String param2;

    public Kicked() {
        param = "!f kick";
        param2 = "! f kick";
        guildId = config.get("guildId");
    }

    @Override
    public Mono<Object> doAction(Message message) {

        //watch for kicked users
        String action = getAction(message);
        if (action == null) {
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
                System.out.println("Member kicked " + finalAction);

                MemberData memberData;
                try {
                    memberData = client.getMemberById(Snowflake.of(guildId), Snowflake.of(finalAction)).getData().block();

                    memberData.roles().forEach(id -> {
                        System.out.println("Removed role " + id);
                        client.getGuildById(Snowflake.of(guildId)).removeMemberRole(
                                Snowflake.of(finalAction),
                                Snowflake.of(id),
                                "user kicked").block();
                    });
                } catch (ClientException e) {
                    System.out.println("user left the server " + finalAction);
                }
                return null;
            });
        }

        return Mono.empty();
    }
}
