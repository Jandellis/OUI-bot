package action;

import bot.Clean;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class GiveawayTotal extends Action {

    public GiveawayTotal() {
        param = "accept";
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in
        try {
            if (message.getChannelId().asString().equals("876714404819918918")) {
                if (message.getAuthor().isPresent() && message.getAuthor().get().getId().asString().equals("530082442967646230")) {
                    if (message.getContent().startsWith("^<@&875881574409859163>")) {
                        int total = 0;
                        try {
                            total = Clean.getGift();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int finalTotal = total;
                        String responseMessage = "The total of the last giveaway was $" + String.format("%,d", finalTotal) +
                                "\r\nIf you would like to help increase this ask a recruiter to become a gifter today!";

                        //write message to chat
                        client.getChannelById(Snowflake.of("840395542394568707")).createMessage(responseMessage).block();

                        //write message to giveaways
                        return message.getChannel().flatMap(channel -> channel.createMessage(responseMessage));
                    }
                    if (message.getContent().startsWith("Congratulations to")) {
                        logger.info("got congrats message");
                        String id = message.getContent().split(" ")[2];

                        logger.info("id is " + id);

                        message.getChannel().block().createMessage("Use this to send a gift to the winner").block();
//                        message.getChannel().block().createMessage("`!gift "+id+"`").block();

                        return message.getChannel().flatMap(channel -> channel.createMessage("`!gift "+id+"`"));
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }
}
