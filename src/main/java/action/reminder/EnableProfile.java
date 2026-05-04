package action.reminder;

import action.Action;
import action.reminder.model.Boost;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import action.reminder.model.TeamEvent;
import action.upgrades.model.LocationEnum;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EnableProfile extends Action {

    String tacoBot = "490707751832649738";
    List<String> watchChannels;

    public EnableProfile() {
        param = "cyrm";
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
                        int count = 0;

                        List<Reminder> reminders = ReminderUtils.loadReminder(message.getAuthor().get().getId().asString());
                        if (reminders.isEmpty()) {

                            embed.description("No reminders, type /cooldown");
                            count++;
                        } else {
                            for (Reminder reminder : reminders) {
//                                LocalDateTime time = reminder.getTime().toLocalDateTime();
//                                LocalDateTime now = LocalDateTime.now();
//                                long hours = ChronoUnit.HOURS.between(now, time);
//
//                                long minutes = ChronoUnit.MINUTES.between(now, time) % 60;
//                                long seconds = ChronoUnit.SECONDS.between(now, time) % 60;
//
//                                String display = "";
//                                if (hours > 0) {
//                                    display += hours + " hours, ";
//                                }
//                                if (minutes > 0) {
//                                    display += minutes + " minutes, ";
//                                }
//                                if (seconds > 0) {
//                                    display += seconds + " seconds";
//                                }
                                String display = getTimeLeft(reminder);
                                if (!display.equals("")) {
                                    embed.addField(reminder.getType().getName(), display, true);
                                    count++;
                                }
                                if (count == 25) {
                                    message.getChannel().block().createMessage(embed.build()).block();
                                    count = 0;
                                    embed = EmbedCreateSpec.builder();
                                    embed.color(Color.SUMMER_SKY);
                                    embed.title("Your Reminders");
                                }


                            }
                        }
                        if (count > 0) {
                            message.getChannel().block().createMessage(embed.build()).block();
                        }
                        return Mono.empty();
                    }
                    if (action.equalsIgnoreCase("boost")) {

                        EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
                        embed.color(Color.SUMMER_SKY);
                        embed.title("Your Boosts");
                        // get the boosts for your location, remove the event and franchise boosts
                        HashMap<LocationEnum,List<Boost>> allBoosts = CreateBoostReminder.getLocationBoosts();

                        //load all reminders, remove the ones that are not boosts
                        List<Reminder> reminders = ReminderUtils.loadReminder(message.getAuthor().get().getId().asString());
                        Map<String,Reminder> remindersMap = reminders.stream()
                                .collect(Collectors.toMap(
                                reminder -> reminder.getType().getName(),
                                reminder -> reminder,
                                (existing, replacement) -> existing  // or choose `replacement`
                        ));

                        // work out what locations user has. Look at profile data
                        List<LocationEnum> activeLocations = ReminderUtils.loadLocations(message.getAuthor().get().getId().asString());
                        activeLocations.sort(Comparator.comparing(LocationEnum::getOrder));

                        //display all the boosts, marking the ones that are remaining and ready to be bought

                        //loop though active locations
                        for (LocationEnum location : activeLocations) {
                            List<Boost> boosts = allBoosts.get(location);
                            if (boosts != null) {
                                boosts.sort(Comparator.comparing(Boost::getOrder));
                                StringBuilder sb = new StringBuilder();
                                for (Boost boost : boosts) {
                                    sb.append( boost.getName() + " - ");
                                    Reminder reminder = remindersMap.get(boost.getName());
                                    String display = "";
                                    if (reminder == null) {
                                        display = "\uD83E\uDE99";
                                    } else {
                                        display = getTimeLeft(reminder);
                                    }
                                    display += "\n";
                                    sb.append(display);

                                }
                                embed.addField(location.getPrintName(), sb.toString(), true);
                            }
                        }
                        message.getChannel().block().createMessage(embed.build()).block();
                        return Mono.empty();
                    }

                    if (action.equalsIgnoreCase("history")) {

                        String[] inputs =  message.getContent().split(" ");
                        if (inputs.length < 3) {
                            message.getChannel().block().createMessage("missing number").block();
                            return Mono.empty();
                        }
                        int number;
                        try {
                            number = Integer.parseInt(inputs[2]);
                        } catch (NumberFormatException e) {
                            message.getChannel().block().createMessage("missing number").block();
                            return Mono.empty();
                        }

                        ReminderUtils.setDepth(message.getAuthor().get().getId().asString(), number);
                        message.getChannel().block().createMessage("History updated to " + number).block();
                        return Mono.empty();
                    }

                    if (action.equalsIgnoreCase("dm")) {


                        Boolean toggled = ReminderUtils.toggleDmReminders(message.getAuthor().get().getId().asString());
                        if (toggled) {
                            Profile profile = ReminderUtils.loadProfileById(message.getAuthor().get().getId().asString());
                            message.getChannel().block().createMessage("DM reminders toggled, now is " + (profile.getDmReminder() ? "enabled":"disabled")).block();
                        }
                        return Mono.empty();
                    }

                    if (action.equalsIgnoreCase("hide")) {
                        Boolean toggled = ReminderUtils.toggleIgnoredHidden(message.getAuthor().get().getId().asString());
                        if (toggled) {
                            Profile profile = ReminderUtils.loadProfileById(message.getAuthor().get().getId().asString());
                            message.getChannel().block().createMessage("Ignored reminders toggled, now is " + (profile.getIgnoredHidden() ? "hidden":"shown")).block();
                        }
                        return Mono.empty();
                    }

                    if (action.equalsIgnoreCase("dnd")) {
                        Boolean toggled = ReminderUtils.toggleDnd(message.getAuthor().get().getId().asString());
                        if (toggled) {
                            Profile profile = ReminderUtils.loadProfileById(message.getAuthor().get().getId().asString());
                            message.getChannel().block().createMessage("Ignored do not disturb toggled, now is " + (profile.getDnd() ? "enabled":"disabled")).block();
                        }
                        return Mono.empty();
                    }
                    if (action.equalsIgnoreCase("large")) {
                        Boolean toggled = ReminderUtils.toggleLarge(message.getAuthor().get().getId().asString());
                        if (toggled) {
                            Profile profile = ReminderUtils.loadProfileById(message.getAuthor().get().getId().asString());
                            message.getChannel().block().createMessage("Large reminders toggled, now is " + (profile.isLargeReminder() ? "large":"small")).block();
                        }
                        return Mono.empty();
                    }

                    if (onOff) {
                        updated = ReminderUtils.enableProfile(message.getAuthor().get().getId().asString(), enable);
                        if (!updated) {
                            message.getChannel().block().createMessage("Profile does not exists. Please type `/shack`").block();
                        } else {
                            message.getChannel().block().createMessage(response).block();
                        }
                    } else {
                        message.getChannel().block().createMessage("Sorry, i don't know what you mean. It should be `on` or `off`").block();
                    }
                }


                String teamParm = "cyteam";
                String channelId = getAction(message,teamParm);
                if (channelId != null) {
                    // channel id
                    String id1 = getAction(message, teamParm, 1);
                    String id2 = getAction(message, teamParm, 2);
                    String id3 = getAction(message, teamParm, 3);
                    String id4 = getAction(message, teamParm, 4);
                    if (message.getAuthor().get().getId().asString().equals(id1) || message.getAuthor().get().getId().asString().equals("292839877563908097")) {
                        TeamEvent teamEvent = new TeamEvent(channelId, id1, id2, id3, id4);
                        ReminderUtils.createTeamEvent(teamEvent);
                        String reply = "Created your team reminders will be in <#"+channelId+"> will ping " +
                                "\n-<@" +id1 + ">" +
                                "\n-<@" +id2 + ">" +
                                "\n-<@" +id3 + ">" +
                                "\n-<@" +id4 + ">" +
                                "";
                        message.getChannel().block().createMessage(reply).block();
                    }
                }



            }
        } catch (Exception e) {
            printException(e);
        }


        return Mono.empty();
    }

    private String getTimeLeft(Reminder reminder) {
//
        String result = "<t:"+(reminder.getTime().getTime() / 1000)+":R>";
        return result;
//        LocalDateTime time = reminder.getTime().toLocalDateTime();
//        LocalDateTime now = LocalDateTime.now();
//        long hours = ChronoUnit.HOURS.between(now, time);
//
//
//
//        long minutes = ChronoUnit.MINUTES.between(now, time) % 60;
//        long seconds = ChronoUnit.SECONDS.between(now, time) % 60;
//
//        String display = "";
//        if (hours > 0) {
//            display += hours + " hours, ";
//        }
//        if (minutes > 0) {
//            display += minutes + " minutes, ";
//        }
//        if (seconds > 0) {
//            display += seconds + " seconds";
//        }
//        return display;
    }

}
