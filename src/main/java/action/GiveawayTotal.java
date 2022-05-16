package action;

import bot.Clean;
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

        if (message.getChannelId().asString().equals("876714404819918918")) {
            if (message.getAuthor().get().getId().asString().equals("530082442967646230")) {
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

                    return message.getChannel().flatMap(channel -> channel.createMessage(responseMessage));
                }
            }
        }

        return Mono.empty();
    }
}
