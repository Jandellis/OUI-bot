package action;

import bot.Clean;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class GiveawayAdd extends Action {

    String giveawayChannel;
    String tacoBot = "490707751832649738";

    public GiveawayAdd() {
        giveawayChannel = config.get("giveawayChannel");
    }

    @Override
    public Mono<Object> doAction(Message message) {
        //work out how much people got in


        if (message.getChannelId().asString().equals(giveawayChannel)) {
            if (message.getAuthor().get().getId().asString().equals(tacoBot)) {

                for (Embed embed : message.getEmbeds()) {

                    String line = embed.getDescription().get();

                    if (line.contains(" You have sent a gift of `$")) {

                        String amount = line.replace(" You have sent a gift of `", "");
                        int index = amount.indexOf("$");
                        amount = amount.substring(index + 1);
                        amount = amount.replace(",", "");
                        String[] split = amount.split("` to ");
                        amount = split[0];
                        try {
                            Clean.addGift(Integer.parseInt(amount));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        logger.info("Added to gift");
                        logger.info(embed.getData());
                    }
                }

            }
        }
        return Mono.empty();
    }
}
