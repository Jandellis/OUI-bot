package action.reminder;

import action.Action;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Help extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public Help() {
        param = "cyhelp";
        watchChannels = Arrays.asList(config.get("watchChannels").split(","));
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {

            AtomicBoolean watched = new AtomicBoolean(true);

//            watchChannels.forEach(channel -> {
//                if (message.getChannelId().asString().equals(channel)) {
//                    watched.set(true);
//                }
//            });
            //if in watch channel
            if (watched.get()) {

                String action = getAction(message);

                if (action != null) {
                    StringBuilder sb = new StringBuilder("I can reminder you when things are off coolodwn and help you on the sauce market. ");

                    sb.append("For sauce market help, type `cysm`\n\n");
                    sb.append("To set me up, first type `/shack`\n");
                    sb.append("Then type `cyrm on`\n");
                    sb.append("Type `cyrm off` to turn me off\n");
                    sb.append("\n");
//                    sb.append("I can do reminders for work, tips, overtime, daily, clean, vote, boosts and gifts\n");
                    sb.append("When you do something that I will create a reminder for that and i will tell you if im missing any cooldowns by reacting with the first letter\n");

                    sb.append("Type `cyrm list` to view your current reminders\n");

                    sb.append("\n");
                    sb.append("You can also add a custom emote for me to react with\n");
                    sb.append("Type `cyreact <emote>`, for example to get me to react with :small_blue_diamond:, type cyreact :small_blue_diamond: \n");
                    sb.append("Type `cyreact delete`, to reset to the default \n");
                    sb.append("\n");
                    sb.append("You can also add a custom reminder messages\n");
                    sb.append("Type `cymsg <message>`, for example, type `cymsg Hey {ping} get back to {task} now! {cmd}` \n");
                    sb.append("Type `cymsg delete`, to reset to the default \n");

                    sb.append("\n");
                    sb.append("I can give you suggestions on what order to buy upgrades\n");
                    sb.append("Type `cyup <location>`, location can be the first letter or the full name \n");
                    sb.append(":small_orange_diamond: For shack, do </hire:1006354977847001159>, </advertisements:1006354977721176137>, </upgrades:1006354978274820107>, </decorations:1006354977788268620>, </truck:1006354978153169014>, then  type cyup s.\n" +
                            ":small_orange_diamond: For mall, do </hire:1006354977847001159>, </advertisements:1006354977721176137>, </upgrades:1006354978274820107>, </decorations:1006354977788268620>, </kiosk:1010956257588428840> and then type cyup m.\n" +
                            ":small_orange_diamond: For beach, do </hire:1006354977847001159>, </advertisements:1006354977721176137>, </upgrades:1006354978274820107>, </decorations:1006354977788268620>, </stand:1006354978153169010> and then type cyup b. \n" +
                            ":small_orange_diamond: For city, do </hire:1006354977847001159>, </advertisements:1006354977721176137>, </upgrades:1006354978274820107>, </decorations:1006354977788268620>, </cart:1006354977721176142> and then type cyup c.\n");
                    sb.append("You can also add the `cheap` flag to list the upgrades in order from cheapest to most expensive\n");
                    sb.append("Type `cyStats <location>` to see stats about your upgrade status\n");
                    message.getChannel().block().createMessage(sb.toString()).block();
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
