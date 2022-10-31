package action.reminder;

import action.Action;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Sleep extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;
    String paramStart;
    String paramEnd;
    String paramClear;
    String paramList;

    public Sleep() {
        paramStart = "cysleepstart";
        paramEnd = "cysleepend";
        paramClear = "cysleepclear";
        paramList = "cysleeplist";
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

                String action = getAction(message, paramStart);

                if (action != null) {
                    int hours = 0;
                    int minutes = 0;

                    String first = getAction(message, paramStart, 0);
                    String second = getAction(message, paramStart, 1);

                    if (first.contains("h")) {
                        hours = Integer.parseInt(first.replace("h", ""));
                    } else {
                        minutes = Integer.parseInt(first.replace("m", ""));
                    }

                    if (!second.isEmpty()) {
                        if (second.contains("h")) {
                            hours = Integer.parseInt(second.replace("h", ""));
                        } else {
                            minutes = Integer.parseInt(second.replace("m", ""));
                        }
                    }

                    LocalTime time = LocalDateTime.now().toLocalTime();

                    time = time.plusHours(hours);
                    time = time.plusMinutes(minutes);

                    Time sleep = Time.valueOf(time);


                    ReminderUtils.setSleepStart(message.getAuthor().get().getId().asString(), sleep);
                    message.getChannel().block().createMessage("Sleep start will be in " + hours + " hours and " + minutes + " minutes").block();
                    return Mono.empty();

                }


                action = getAction(message, paramEnd);

                if (action != null) {
                    int hours = 0;
                    int minutes = 0;

                    String first = getAction(message, paramEnd, 0);
                    String second = getAction(message, paramEnd, 1);

                    if (first.contains("h")) {
                        hours = Integer.parseInt(first.replace("h", ""));
                    } else {
                        minutes = Integer.parseInt(first.replace("m", ""));
                    }

                    if (!second.isEmpty()) {
                        if (second.contains("h")) {
                            hours = Integer.parseInt(second.replace("h", ""));
                        } else {
                            minutes = Integer.parseInt(second.replace("m", ""));
                        }
                    }

                    LocalTime time = LocalDateTime.now().toLocalTime();

                    time = time.plusHours(hours);
                    time = time.plusMinutes(minutes);

                    Time sleep = Time.valueOf(time);


                    ReminderUtils.setSleepEnd(message.getAuthor().get().getId().asString(), sleep);
                    message.getChannel().block().createMessage("Sleep end will be in " + hours + " hours and " + minutes + " minutes").block();
                    return Mono.empty();

                }


                action = getAction(message, paramClear);

                if (action != null) {


                    ReminderUtils.clearSleep(message.getAuthor().get().getId().asString());
                    message.getChannel().block().createMessage("Sleep cleared").block();
                    return Mono.empty();

                }
                action = getAction(message, paramList);

                if (action != null) {

                    Profile profile = ReminderUtils.loadProfileById(message.getAuthor().get().getId().asString());
                    String sleep = "Sleep start " + profile.getSleepStart() + "\n" +
                            "Sleep end " + profile.getSleepEnd() +"\n" +
                            "Current Time " + LocalTime.now();


                    EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                    embed.color(Color.SUMMER_SKY);
                    embed.title("Sleep");
                    if (profile.getSleepEnd() == null || profile.getSleepStart() == null ){

                        embed.description("Not set");
                    } else {

                        timeToSleep(embed, profile.getSleepStart().toLocalTime(), "Sleep Start");
                        timeToSleep(embed, profile.getSleepEnd().toLocalTime(), "Sleep End");
                    }




                    message.getChannel().block().createMessage(embed.build()).block();
                    return Mono.empty();

                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

    private void timeToSleep(EmbedCreateSpec.Builder embed, LocalTime sleep, String name){


        LocalDateTime now = LocalDateTime.now();
        LocalDateTime time = sleep.atDate(LocalDate.now());
        if (time.isBefore(now)) {
            //if before now add one day to move to tomorrow
            time = sleep.atDate(LocalDate.now().plusDays(1));
        }
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
        if (!display.equals("")) {
            embed.addField(name, display, true);
        }
    }

}
