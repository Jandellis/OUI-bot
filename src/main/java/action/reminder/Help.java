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
                    StringBuilder sb = new StringBuilder("I can reminder you when things are off cooldown and help you on the sauce market. ");

                    sb.append("For sauce market help, type `cysm`\n\n");
                    sb.append("To set me up, first type `/shack`\n");
                    sb.append("Then type `cyrm on`\n");
                    sb.append("Type `cyrm off` to turn me off\n");
                    sb.append("Type `cyrm dm` to toggle DM reminders\n");
                    sb.append("Type `cyrm dnd` to toggle do not disturb mode\n");
                    sb.append("Type `cyrm hide` to toggle ignored reminders after you do an action\n");
                    sb.append("\n");
//                    sb.append("I can do reminders for work, tips, overtime, daily, clean, vote, boosts and gifts\n");
                    sb.append("When you do something that I will create a reminder for that and i will tell you if im missing any cooldowns by reacting with the first letter\n");

                    sb.append("Type `cyrm list` to view your current reminders\n");
                    sb.append("Type `cyrm boost` to view your boost status\n");

                    sb.append("\n");
                    sb.append("You can also add a custom emote for me to react with\n");
                    sb.append("Type `cyreact <emote>`, for example to get me to react with :small_blue_diamond:, type cyreact :small_blue_diamond: \n");
                    sb.append("Type `cyreact delete`, to reset to the default \n");
                    sb.append("\n");
                    sb.append("You can also add a custom reminder messages\n");
                    sb.append("Type `cymsg <message>`, for example, type `cymsg Hey {ping} get back to {task} now! {cmd}` \n");
                    sb.append("Type `cymsg delete`, to reset to the default \n\n");
                    sb.append("Type `cyrm history <limit>`, the higher the history, the more message I will go back and check who was the owner of a message\n");
                    message.getChannel().block().createMessage(sb.toString()).block();

                    sb = new StringBuilder();
                    sb.append("I can give you suggestions on what order to buy upgrades\n");
                    sb.append("Type `cyup <location>`, location can be the first letter or the full name \n");
                    sb.append("For each location do </hire:1203826200452137022>, </advertisements:1203826194500288593>, </upgrades:1203826209532682311>, </decorations:1203826197352677417> then\n");
                    sb.append(":small_orange_diamond: For shack </truck:1203826208953999482>, then  type cyup s.\n" +
                            ":small_orange_diamond: For mall </kiosk:1203826201504780309> and then type cyup m.\n" +
                            ":small_orange_diamond: For beach </stand:1203826207884582913> and then type cyup b. \n" +
                            ":small_orange_diamond: For amusement park </attractions:1203826194500288594> and then type cyup a. \n" +
                            ":small_orange_diamond: For city </cart:1203826195511250965> and then type cyup c.\n" +
                            ":small_orange_diamond: For Cantina </stage:1276293791728406771> and then type cyup ca.\n" +
                            ":small_orange_diamond: For hq, do </hq upgrades:1203826200959651851>, </hq hire:1203826200959651851> and then type cyup h.\n");
                    message.getChannel().block().createMessage(sb.toString()).block();
                    sb = new StringBuilder();
                    sb.append("There is some flags you can add: \n" +
                            " - `cheap` flag to list the upgrades in order from cheapest to most expensive\n" +
                            " - `grouped` flag to list the upgrades alphabetically\n");
                    sb.append("You can view a page by including 2 numbers at the end. For example `cyup m 25 35` would show upgrade from the mall from upgrade 25 to upgrade 35 \n");
                    sb.append("Type `cyLimitUp <number>` to set how many upgrades listed\n");
                    sb.append("Type `cyStats <location>` to see stats about your upgrade status\n");
                    sb.append("Click on :arrows_counterclockwise: to get Cylon to recheck your upgrade list again\n");
                    sb.append("\nYou can also turn on sleep mode\n");
                    sb.append("To do you you need to set how many minutes to start the sleep mode in and end. For example to start sleep mode in 90 minutes and to make it last for 8 hours\n");
                    sb.append("Type `cySleepStart 1h 30m`\n");
                    sb.append("Type `cySleepEnd 9h 30m`\n");
                    sb.append("To remove sleep mode`\n");
                    sb.append("Type `cySleepClear`\n");
                    sb.append("To view sleep times`\n");
                    sb.append("Type `cySleepList`\n");
                    message.getChannel().block().createMessage(sb.toString()).block();

                    sb = new StringBuilder();
                    sb.append("To view what reminders you have enabled type </reminders:1109632647807901746>\n");
                    sb.append("To change  what reminders you have enabled type </reminders:1109632647807901746> and select the ones that you want\n");
                    message.getChannel().block().createMessage(sb.toString()).block();
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}
