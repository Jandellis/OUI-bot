package action;

import action.sm.DoAlerts;
import action.sm.Utils;
import bot.Clean;
import bot.KickList;
import bot.Sauce;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.MemberData;
import discord4j.rest.http.client.ClientException;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Test extends Action {


    String guildId;
    String param2;
    String hitThread;
    Long recruiter;

    public Test() {
        param = "testing";
        guildId = config.get("guildId");
        hitThread = config.get("hitThread");
        recruiter = Long.parseLong(config.get("recruiter"));
    }

    @Override
    public Mono<Object> doAction(Message message) {

        //watch for kicked users
        String action = getAction(message);
        if (action == null || !message.getAuthor().get().getId().asString().equalsIgnoreCase("292839877563908097")) {

            return Mono.empty();
        }


        if (action != null) {
//            message.getChannel().block().createMessage("!found").block();
//            return Mono.empty();

//            client.getChannelById(Snowflake.of("875066825908576286")).




            return message.getChannel().flatMap(channel -> {
                try {
                    message.addReaction(ReactionEmoji.unicode("\uD83D\uDCB0")).block();
                Snowflake messageId = Snowflake.of(action);
                    channel.createMessage("c!speedjar").block();


//                    client.getChannelById(Snowflake.of("asd"));

//                DoAlerts doAlerts = new DoAlerts();
//                doAlerts.action(gateway, client);
//                doAlerts.doAction(channel.getMessageById(messageId).block());
//                Message data = channel.getMessageById(messageId).block();
                } catch (Exception e) {
                    printException(e);
//    e.printStackTrace();
                }

                return channel.createMessage("testing Data");
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
