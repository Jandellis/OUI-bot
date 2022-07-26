package action.reminder;

import action.Action;
import action.sm.Drop;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Help extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public Help() {
        param = "ouihelp";
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {

            AtomicBoolean watched = new AtomicBoolean(false);

            watchChannels.forEach(channel -> {
                if (message.getChannelId().asString().equals(channel)) {
                    watched.set(true);
                }
            });
            //if in watch channel
            if (watched.get()) {


                if (message.getContent().equalsIgnoreCase(param)) {
                    StringBuilder sb = new StringBuilder("I can reminder you when things are off coolodwn and help you on the sauce market.");

                    sb.append("For sauce market help, type `ouism`\r\n\n");
                    sb.append("To set me up, first type `!shack`\r\n");
                    sb.append("Then type `ouirm on`\r\n");
                    sb.append("Type `ouirm off` to turn me off\r\n");
                    sb.append("\r\n");
                    sb.append("Currently I only work for work, tips, overtime, daily, clean and vote\r\n");
                    sb.append("When you do one of these, i will tell you if im missing any cooldowns by reacting with the first letter\r\n");

                    sb.append("\r\n");
                    sb.append("You can also add a custom emote for me to react with\r\n");
                    sb.append("Type `ouireact <emote>`, for example to get me to react with :small_blue_diamond:, type ouireact :small_blue_diamond: \r\n");

                    sb.append("\r\n");
                    sb.append("You can also add a custom reminder messages\r\n");
                    sb.append("Type `ouimsg <message>`, for example, type `ouimsg Hey {ping} get back to {task} now!` \r\n");
                    sb.append("Type `ouimsg delete`, to reset to the default` \r\n");

                    sb.append("\r\n");
                    sb.append("If you see any bugs just let us know in <#840395542394568707>\r\n");
                    message.getChannel().block().createMessage(sb.toString()).block();
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
