package action;

import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class Karen extends Action {


    String customerChannel;
    String customerPing;
    String customerBot = "526268502932455435";
//    String customerBot = "292839877563908097";

    String param2;
    String param3;
    String param4;
    String param5;
    String param6;
    String param7;

    public Karen() {
        param = "Someone left a tip for";
        param2 = "feeling a little extra hungry today";
        param3 = "I want to buy";
        param4 = "Sell Game";
        param5 = "Math Game";
        param6 = "Unscramble Game";
        param7 = "Trivia Game";
        customerChannel = "840942880775471114";
        customerPing = "931599227824517151";

    }

    @Override
    public Mono<Object> doAction(Message message) {
        try {
            if (message.getChannelId().asString().equals(customerChannel)) {
                if (message.getData().author().id().asString().equals(customerBot)) {
                    if (message.getContent().contains(param2) || message.getContent().contains(param3)) {
                        logger.info("got sell");

                        return message.getChannel().flatMap(channel -> {
                            return channel.createMessage("<@&" + customerPing + "> Karen is here, with sell :speaking_head:");
                        });
                    }
                    if (message.getContent().contains(param)) {
                        logger.info("got unscramble");
                        return message.getChannel().flatMap(channel -> {
                            return channel.createMessage("<@&" + customerPing + "> Karen is here, with unscramble :speaking_head:");
                        });
                    }
                    for (Embed embed: message.getEmbeds()){
                        if (embed.getTitle().isPresent() && embed.getDescription().isPresent()) {

                            String [] question = embed.getDescription().get().split("`");
                            if (embed.getTitle().get().contains(param4)){
                                logger.info("got sell");
                                return message.getChannel().flatMap(channel -> {
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with sell :speaking_head:");
                                });
                            }
                            if (embed.getTitle().get().contains(param5)){
                                logger.info("got math");
                                return message.getChannel().flatMap(channel -> {
                                    channel.createMessage("Bill was " + question[1]).block();
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with math :speaking_head:");
                                });
                            }
                            if (embed.getTitle().get().contains(param6)){
                                logger.info("got unscramble");
                                return message.getChannel().flatMap(channel -> {
                                    channel.createMessage("Unscramble was " + question[1]).block();
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with unscramble :speaking_head:");
                                });
                            }
                            if (embed.getTitle().get().contains(param7)){
                                logger.info("got trivia");
                                return message.getChannel().flatMap(channel -> {
                                    channel.createMessage("Question was " + question[1]).block();
                                    return channel.createMessage("<@&" + customerPing + "> Karen is here, with trivia :speaking_head:");
                                });
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

}
