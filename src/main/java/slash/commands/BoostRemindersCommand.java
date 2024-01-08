package slash.commands;

import action.reminder.ReminderUtils;
import action.reminder.model.ReminderSettings;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class BoostRemindersCommand extends SlashCommand {
    @Override
    public String getName() {
        return "boost_reminders";
    }

    protected String defaultReact = "<a:cylon:1014777339114168340>";

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {


        String name = event.getInteraction().getData().member().get().user().id().asString();
        Boolean all = getParameter("all", (Boolean) null, event);
        Boolean shack = getParameter("shack", (Boolean) null, event);
        Boolean mall = getParameter("mall", (Boolean) null, event);
        Boolean beach = getParameter("beach", (Boolean) null, event);
        Boolean amusement = getParameter("amusement", (Boolean) null, event);
        Boolean city = getParameter("city", (Boolean) null, event);
        //load current reminder settings
        ReminderSettings reminderSettings = ReminderUtils.loadReminderSettings(name);
        //if no reminders set up, create new one and set them all to true
        if (reminderSettings == null){
            reminderSettings = new ReminderSettings(name);
        }

        if (all == null && shack == null && mall == null && beach == null && amusement == null && city == null ) {

//            EnabledReminders enabledReminders = ReminderUtils.loadEnableReminders(name);
            String message = "Your current reminders are ";
            if (reminderSettings.isTip()) {
                message += "\n :small_orange_diamond: shack ";
            }
            if (reminderSettings.isWork()) {
                message += "\n :small_orange_diamond: mall ";
            }
            if (reminderSettings.isOvertime()) {
                message += "\n :small_orange_diamond: beach ";
            }
            if (reminderSettings.isVote()) {
                message += "\n :small_orange_diamond: amusement ";
            }
            if (reminderSettings.isDaily()) {
                message += "\n :small_orange_diamond: city ";
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
            if (shack != null) {
                reminderSettings.setTip(shack);
                if (shack)
                    messageEnable+= "\n :small_orange_diamond: shack ";
                else
                    messageDisable+= "\n :small_orange_diamond: shack ";
            }
            if (mall != null) {
                reminderSettings.setWork(mall);
                if (mall)
                    messageEnable+= "\n :small_orange_diamond: mall ";
                else
                    messageDisable+= "\n :small_orange_diamond: mall ";
            }
            if (beach != null) {
                reminderSettings.setOvertime(beach);
                if (beach)
                    messageEnable+= "\n :small_orange_diamond: beach ";
                else
                    messageDisable+= "\n :small_orange_diamond: beach ";
            }
            if (city != null) {
                reminderSettings.setDaily(city);
                if (city)
                    messageEnable+= "\n :small_orange_diamond: city ";
                else
                    messageDisable+= "\n :small_orange_diamond: city ";
            }
            if (amusement != null) {
                reminderSettings.setVote(amusement);
                if (amusement)
                    messageEnable+= "\n :small_orange_diamond: amusement ";
                else
                    messageDisable+= "\n :small_orange_diamond: amusement ";
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
