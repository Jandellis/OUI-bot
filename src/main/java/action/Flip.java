package action;

import java.time.Duration;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class Flip extends Action {


    String guildId;
    String param2;

    public Flip() {
        param = "cyflip";
        param2 = "flouip";
        guildId = config.get("guildId");
    }

    @Override
    public Mono<Object> doAction(Message message) {

        if (message.getContent().toLowerCase().contains(param) || message.getContent().toLowerCase().contains(param2)) {
            return message.getChannel()
                    .flatMap(channel -> {
//                            channel.createMessage("flipping coin...")
//                                    .flatMap(msg -> Mono.delay(Duration.ofSeconds(1))
//                                            .then(msg.edit(spec -> {
//                                                String result = Math.random() < 0.5 ? "Heads!" : "Tails!";
//                                                spec.setContent(message.getAuthor().get().getUsername() + " flipped a coin and got " + result);
//                                            }))
//                                    )
                                String result = Math.random() < 0.5 ? "https://tenor.com/view/coin-flip-coin-flip-tails-coin-gif-5899394254810064979" : "https://tenor.com/view/coin-flip-coin-flip-heads-coin-gif-11367279720157303147";
                                return channel.createMessage(result);
                            }
                    );

        }

        return Mono.empty();
    }

}
