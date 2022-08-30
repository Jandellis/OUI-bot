package action.reminder;

import action.Action;
import discord4j.core.object.entity.Message;
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
                    StringBuilder sb = new StringBuilder("I can reminder you when things are off coolodwn and help you on the sauce market. ");

                    sb.append("For sauce market help, type `ouism`\r\n\n");
                    sb.append("To set me up, first type `!shack` or `/shack`\r\n");
                    sb.append("Then type `ouirm on`\r\n");
                    sb.append("Type `ouirm off` to turn me off\r\n");
                    sb.append("\r\n");
                    sb.append("I can do reminders for work, tips, overtime, daily, clean, vote, boosts and gifts\r\n");
                    sb.append("When you do one of these, i will tell you if im missing any cooldowns by reacting with the first letter\r\n");

                    sb.append("Type `ouirm list` to view your current reminders\r\n");

                    sb.append("\r\n");
                    sb.append("You can also add a custom emote for me to react with\r\n");
                    sb.append("Type `ouireact <emote>`, for example to get me to react with :small_blue_diamond:, type ouireact :small_blue_diamond: \r\n");

                    sb.append("\r\n");
                    sb.append("You can also add a custom reminder messages\r\n");
                    sb.append("Type `ouimsg <message>`, for example, type `ouimsg Hey {ping} get back to {task} now!` \r\n");
                    sb.append("Type `ouimsg delete`, to reset to the default \r\n");

                    sb.append("\r\n");
                    sb.append("I can give you suggestions on what order to buy upgrades\r\n");
                    sb.append("Type `ouiup <location>`, location can be the first letter or the full name \r\n");
                    sb.append(":small_orange_diamond: For shack, do `/hire`, `/advertisements`, `/upgrades`, `/decorations`, `/truck`, then  type ouiup s.\n" +
                            ":small_orange_diamond: For mall, do `/hire`, `/advertisements`, `/upgrades`, `/decorations`, `/kiosk` and then type ouiup m.\n" +
                            ":small_orange_diamond: For beach, do `/hire`, `/advertisements`, `/upgrades`, `/decorations`, `/stand` and then type ouiup b. \n" +
                            ":small_orange_diamond: For city, do `/hire`, `/advertisements`, `/upgrades`, `/decorations`, `/cart` and then type ouiup c.\n");
                    sb.append("You can also add the `cheap` flag to list the upgrades in order from cheapest to most expensive\r\n");

                    sb.append("\r\n");
                    sb.append("If you see any bugs just let us know in <#1008163269485281361>\r\n");
                    message.getChannel().block().createMessage(sb.toString()).block();
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
