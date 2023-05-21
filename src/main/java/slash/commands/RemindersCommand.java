package slash.commands;

import action.reminder.ReminderUtils;
import action.reminder.model.ReminderSettings;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class RemindersCommand extends SlashCommand {
    @Override
    public String getName() {
        return "reminders";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {


        String name = event.getInteraction().getData().member().get().user().id().asString();
        Boolean all = getParameter("all", (Boolean) null, event);
        Boolean tip = getParameter("tip", (Boolean) null, event);
        Boolean work = getParameter("work", (Boolean) null, event);
        Boolean overtime = getParameter("overtime", (Boolean) null, event);
        Boolean vote = getParameter("vote", (Boolean) null, event);
        Boolean daily = getParameter("daily", (Boolean) null, event);
        Boolean clean = getParameter("clean", (Boolean) null, event);
        Boolean boost = getParameter("boost", (Boolean) null, event);
        //load current reminder settings
        ReminderSettings reminderSettings = ReminderUtils.loadReminderSettings(name);
        //if no reminders set up, create new one and set them all to true
        if (reminderSettings == null){
            reminderSettings = new ReminderSettings(name);
        }

        if (all == null && tip == null && work == null && overtime == null && vote == null && daily == null && clean == null && boost == null ) {

//            EnabledReminders enabledReminders = ReminderUtils.loadEnableReminders(name);
            String message = "Your current reminders are ";
            if (reminderSettings.isTip()) {
                message += "\n :small_orange_diamond: tip ";
            }
            if (reminderSettings.isWork()) {
                message += "\n :small_orange_diamond: work ";
            }
            if (reminderSettings.isOvertime()) {
                message += "\n :small_orange_diamond: overtime ";
            }
            if (reminderSettings.isVote()) {
                message += "\n :small_orange_diamond: vote ";
            }
            if (reminderSettings.isDaily()) {
                message += "\n :small_orange_diamond: daily ";
            }
            if (reminderSettings.isClean()) {
                message += "\n :small_orange_diamond: clean ";
            }
            if (reminderSettings.isBoost()) {
                message += "\n :small_orange_diamond: boost ";
            }
            if (message.equals("Your current reminders are ")){
                message += "\n :small_orange_diamond: none";
            }
            return event.reply()
                    .withEphemeral(false)
                    .withContent(message);
        }

        String messageEnable = "";
        String messageDisable = "";
        if (all != null) {
            reminderSettings = new ReminderSettings(name, all, all, all, all, all, all, all);
            ReminderUtils.updateReminderSettings(reminderSettings);
            if (all) {
                messageEnable = "\n :small_orange_diamond: all";
            } else {
                messageDisable = "\n :small_orange_diamond: all";
            }
        } else {
//            EnabledReminders enabledReminders = new EnabledReminders(name);
            if (tip != null) {
                reminderSettings.setTip(tip);
                if (tip)
                    messageEnable+= "\n :small_orange_diamond: tip ";
                else
                    messageDisable+= "\n :small_orange_diamond: tip ";
            }
            if (work != null) {
                reminderSettings.setWork(work);
                if (work)
                    messageEnable+= "\n :small_orange_diamond: work ";
                else
                    messageDisable+= "\n :small_orange_diamond: work ";
            }
            if (overtime != null) {
                reminderSettings.setOvertime(overtime);
                if (overtime)
                    messageEnable+= "\n :small_orange_diamond: overtime ";
                else
                    messageDisable+= "\n :small_orange_diamond: overtime ";
            }
            if (daily != null) {
                reminderSettings.setDaily(daily);
                if (daily)
                    messageEnable+= "\n :small_orange_diamond: daily ";
                else
                    messageDisable+= "\n :small_orange_diamond: daily ";
            }
            if (clean != null) {
                reminderSettings.setClean(clean);
                if (clean)
                    messageEnable+= "\n :small_orange_diamond: clean ";
                else
                    messageDisable+= "\n :small_orange_diamond: clean ";
            }
            if (vote != null) {
                reminderSettings.setVote(vote);
                if (vote)
                    messageEnable+= "\n :small_orange_diamond: vote ";
                else
                    messageDisable+= "\n :small_orange_diamond: vote ";
            }
            if (boost != null) {
                reminderSettings.setBoost(boost);
                if (boost)
                    messageEnable+= "\n :small_orange_diamond: boost ";
                else
                    messageDisable+= "\n :small_orange_diamond: boost ";
            }
            ReminderUtils.updateReminderSettings(reminderSettings);
        }
        String message = "";
        if (!messageEnable.equals("")) {
            message += "Reminders enabled are: " + messageEnable + "\n";
        }
        if (!messageDisable.equals("")) {
            message += "Reminders disabled are:" + messageDisable;
        }
        message += " ";

        //Reply to the slash command, with the name the user supplied
        return event.reply()
                .withEphemeral(false)
                .withContent(message);
    }


}
