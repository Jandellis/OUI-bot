package action;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class Karen extends Action {


    String customerChannel;
    String customerPing;
    String customerBot = "526268502932455435";
//    String customerBot = "292839877563908097";

    String param2;

    public Karen() {
        param = "Someone left a tip for";
        param2 = "feeling a little extra hungry today";
        customerChannel = "840942880775471114";
        customerPing = "931599227824517151";

    }

    @Override
    public Mono<Object> doAction(Message message) {
        try {
            if (message.getChannelId().asString().equals(customerChannel)) {
                if (message.getAuthor().get().getId().asString().equals(customerBot)) {
                    if (message.getContent().contains(param2)) {

                        return message.getChannel().flatMap(channel -> {
                            return channel.createMessage("<@&" + customerPing + "> Karen is here, with sell :speaking_head:");
                        });
                    }
                    if (message.getContent().contains(param)) {

                        return message.getChannel().flatMap(channel -> {
                            return channel.createMessage("<@&" + customerPing + "> Karen is here, with unscramble :speaking_head:");
                        });
                    }
                }
            }

        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

}
