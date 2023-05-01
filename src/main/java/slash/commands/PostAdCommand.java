package slash.commands;

import action.reminder.DoReminder;
import action.reminder.ReminderType;
import action.reminder.ReminderUtils;
import action.reminder.model.Profile;
import action.reminder.model.Reminder;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class PostAdCommand extends SlashCommand {
    @Override
    public String getName() {
        return "postad";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        /*
        Since slash command options are optional according to discord, we will wrap it into the following function
        that gets the value of our option as a String without chaining several .get() on all the optional values

        In this case, there is no fear it will return empty/null as this is marked "required: true" in our json.
         */
        Boolean ping = getParameter("ping", false, event);
//        Optional<Boolean> pingPresent = event.getOption("ping")
//            .flatMap(ApplicationCommandInteractionOption::getValue)
//            .map(ApplicationCommandInteractionOptionValue::asBoolean);
//        if (pingPresent.isPresent()) {
//            ping = pingPresent.get();
//        }

        String response = "Posted ";
        if (ping) {
            response = response + " <@465668805448957952>";
        }

        String userid = event.getInteraction().getData().member().get().user().id().asString();
        Instant reminderTime = Instant.now().plus(6, ChronoUnit.HOURS);

        Reminder reminder = ReminderUtils.addReminder(userid, ReminderType.postAd, Timestamp.from(reminderTime), event.getInteraction().getChannelId().asString());
        DoReminder doReminder = new DoReminder(event.getInteraction().getClient(), event.getClient().rest());
        doReminder.runReminder(reminder);
        Profile profile = ReminderUtils.loadProfileById(userid);
//        react(message, profile);

        String react;
        if (profile == null) {
            react = defaultReact;
        } else {
            react = profile.getEmote();
            if (react == null || react.equals("")) {
                react = defaultReact;
            }
        }
        response = react + " " + response;


        //Reply to the slash command, with the name the user supplied
        return  event.reply()
            .withEphemeral(false)
            .withContent(response);
    }
}
