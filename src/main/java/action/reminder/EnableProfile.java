package action.reminder;

import action.Action;
import bot.Sauce;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EnableProfile extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public EnableProfile() {
        param = "ouirm";
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

                String action = getAction(message);

                if (action != null) {
                    boolean updated = false;
                    String response = "";
                    boolean enable = false;
                    boolean onOff = false;
                    if (action.equalsIgnoreCase("on")) {
                        enable = true;
                        onOff = true;
                        response = "Reminders on";
                    }
                    if (action.equalsIgnoreCase("off")) {
                        enable = false;
                        onOff = true;
                        response = "Reminders off";
                    }
                    if (action.equalsIgnoreCase("list")) {

                        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                        embed.color(Color.SUMMER_SKY);
                        embed.title("Your Reminders");

                        List<Reminder> reminders = Utils.loadReminder(message.getAuthor().get().getId().asString());
                        if (reminders.isEmpty()) {

                            embed.addField("No Reminders", "", true);
                        } else {
                            reminders.forEach(reminder -> {
                                LocalDateTime time = reminder.getTime().toLocalDateTime();
                                LocalDateTime now = LocalDateTime.now();
                                long hours = ChronoUnit.HOURS.between(now, time);

                                long minutes = ChronoUnit.MINUTES.between(now, time) % 60;
                                long seconds = ChronoUnit.SECONDS.between(now, time) % 60;

                                String display = "";
                                if (hours > 0) {
                                    display += hours + " hours, ";
                                }
                                if (minutes > 0) {
                                    display += minutes + " minutes, ";
                                }
                                if (seconds > 0) {
                                    display += seconds + " seconds";
                                }

                                embed.addField(reminder.getType().getName(), display, true);
                            });
                        }
                        message.getChannel().block().createMessage(embed.build()).block();
                        return Mono.empty();
                    }

                    if (onOff) {
                        updated = Utils.enableProfile(message.getAuthor().get().getId().asString(), enable);
                        if (!updated) {
                            message.getChannel().block().createMessage("Profile does not exists. Please type `!shack`").block();
                        } else {
                            message.getChannel().block().createMessage(response).block();
                        }
                    } else {
                        message.getChannel().block().createMessage("Sorry, i don't know what you mean. It should be `on` or `off`").block();
                    }
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

}