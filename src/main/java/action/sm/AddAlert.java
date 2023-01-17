package action.sm;

import action.Action;
import action.sm.model.Alert;
import action.sm.model.AlertType;
import action.sm.model.Drop;
import action.sm.model.Trigger;
import action.sm.model.Watch;
import bot.Sauce;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class AddAlert extends Action {
    String smUpdate;
    String cheapPing;
    String paramDrop;
    String paramRise;
    String paramDelete;
    String paramHigh;
    String paramLow;
    String parmaAlert;
    String paramTrigger;
    String paramHelp;

    String paramWatch;
    String paramWatchDelete;
    String paramWatchView;
    String smChannel;

    public AddAlert() {
        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
        paramDrop = "cySmDrop";
        paramRise = "cySmRise";
        paramDelete = "cySmDelete";
        paramHigh = "cySmHigh";
        parmaAlert = "cySmAlert";
        paramTrigger = "cySmTrigger";
        paramHelp = "cySm";

        paramLow = "cySmLow";
        paramWatch = "cySmWatch";
        paramWatchDelete = "cySmWatchDelete";
        paramWatchView = "cySmWatchView";
        smChannel = config.get("smChannel");
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {
            String action = getAction(message, paramDrop.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();

                Drop drop = Drop.getDrop(action);
                if (drop == null) {
                    drop = Drop.owned;
                }
                Utils.addTrigger(userId, AlertType.drop, drop.getPrice());
                String alert = "";
                switch (drop) {
                    case both:
                        alert = "your sauces on your watchlist and that you own";
                        break;
                    case owned:
                        alert = "your sauces that you own";
                        break;
                    case watchlist:
                        alert = "your sauces on your watchlist";
                        break;
                }
                message.getChannel().block().createMessage("I will alert you when " + alert + " drop").block();
            }


            action = getAction(message, paramRise.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();

                Utils.addTrigger(userId, AlertType.rise, Drop.watchlist.getPrice());
                message.getChannel().block().createMessage("I will alert you when your sauces on your watchlist rise").block();
            }

            action = getAction(message, paramDelete.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                Utils.deleteAlert(userId);
                Utils.deleteTrigger(userId);
                Utils.deleteWatch(userId);
                message.getChannel().block().createMessage("All sm data deleted").block();
            }

            action = getAction(message, paramHigh.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                int price;
                try {
                    price = Integer.parseInt(action);
                } catch (Exception e) {
                    message.getChannel().block().createMessage("invalid price").block();
                    return Mono.empty();
                }
                Utils.addTrigger(userId, AlertType.high, price);
                message.getChannel().block().createMessage("I will alert you when the sauces you own are above $" + price).block();
            }

            action = getAction(message, parmaAlert.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                StringBuilder sb = new StringBuilder("Your Alerts");
                for (Alert alert : Utils.loadAlerts(userId)) {
                    sb.append("\r\n - Alert " + alert.getType().getName());
                    sb.append(" for " + alert.getTrigger());
                    if (alert.getPrice() > 0)
                        sb.append(" at $" + alert.getPrice());
                }
                message.getChannel().block().createMessage(sb.toString()).block();
            }

            action = getAction(message, paramTrigger.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                StringBuilder sb = new StringBuilder("Your Triggers");
                for (Trigger trigger : Utils.loadTriggers(userId)) {
                    sb.append("\r\n - Trigger " + trigger.getType().getName());
                    if (trigger.getPrice() > 0)
                        sb.append(" at $" + trigger.getPrice());
                    else {
                        sb.append(" for " + trigger.getDrop().getName());
                    }
                }
                message.getChannel().block().createMessage(sb.toString()).block();
            }


            action = getAction(message, paramLow.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                int price;
                try {
                    price = Integer.parseInt(action);
                } catch (Exception e) {
                    message.getChannel().block().createMessage("invalid price").block();
                    return Mono.empty();
                }
                Utils.addTrigger(userId, AlertType.low, price);
                message.getChannel().block().createMessage("I will alert you when the sauces on your watchlist are below $" + price).block();
            }

            action = getAction(message, paramWatchView.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                StringBuilder sb = new StringBuilder("Your Watches");
                for (Watch watch : Utils.loadWatch(userId)) {
                    sb.append("\r\n - " + watch.getSauce().getName());
                }
                message.getChannel().block().createMessage(sb.toString()).block();
            }

            action = getAction(message, paramWatchDelete.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                Utils.deleteWatch(userId);
                message.getChannel().block().createMessage("Watches deleted").block();
            }

            action = getAction(message, paramWatch.toLowerCase());
            if (action != null) {
                String userId = message.getAuthor().get().getId().asString();
                if (action.equalsIgnoreCase(paramWatch)) {
                    StringBuilder sb = new StringBuilder("Your Watches");
                    for (Watch watch : Utils.loadWatch(userId)) {
                        sb.append("\r\n - " + watch.getSauce().getName());
                    }
                    message.getChannel().block().createMessage(sb.toString()).block();
                } else {
                    Sauce sauce = Sauce.getSauce(action);
                    if (sauce == null) {
                        message.getChannel().block().createMessage("invalid sauce").block();
                        return Mono.empty();
                    }
                    Utils.addWatch(userId, sauce);
                    message.getChannel().block().createMessage("Added " + sauce + " to watchlist").block();
                }
            }

            if (message.getContent().equalsIgnoreCase(paramHelp)) {
                StringBuilder sb = new StringBuilder("I can help you monitor the price of your sauces. ");
                sb.append("To set me up, first create some triggers.\r\n");
                sb.append(" :small_blue_diamond: **" + paramDrop + " <" + Drop.both.getName() + "|" + Drop.owned.getName() + "|" + Drop.watchlist.getName() + "> **- add trigger for price dropping more than $10 in one hour or 2 hours in a row. Example `" + paramDrop + " " + Drop.owned.getName() + "`\r\n");
//                sb.append(" :small_blue_diamond: **" + paramRise + " **- add trigger for price rising more than $10 in one hour. Example `" + paramRise + "`\r\n");
                sb.append(" :small_blue_diamond: **" + paramHigh + " <price>** - add trigger when price hits that. Example `" + paramHigh + " 150`\r\n");
                sb.append(" :small_blue_diamond: **" + paramLow + " <price>** - add trigger when price is lower. Example `" + paramLow + " 55`\r\n");

                sb.append("\r\nYou can set up some sauces to watch. (only works for " + paramDrop + " and " + paramLow + ")\r\n");
                sb.append(" :small_blue_diamond: **" + paramWatch + " <sauce>** - add sauce to watch. Example `" + paramWatch + " pico`\r\n");
                sb.append("\r\nNow that you have set up your triggers and watchlist everytime you type `/saucemarket list` in the sauce market channel I will automatically create or delete alerts for the sauces you own and on your watchlist\r\n");
                sb.append("\r\nExamples:\r\n");
                sb.append(" :small_orange_diamond: you have 10K of pico and you have a high alert of $150. If the price is $155 I will ping you.\r\n");
                sb.append(" :small_orange_diamond: you have 10K of pico and you have a dropping alert for owned. If the price drops from $155 to $145 I will ping you again saying the price is dropping.\r\n");
                sb.append(" :small_orange_diamond: you have 10K of pico and you have a dropping alert for owned. If the price drops from $155 to $149 then $149 to $146 the next hour I will ping you again saying the price is dropping.\r\n");
                sb.append(" :small_orange_diamond: you have salsa on your watch list you have a dropping alert for watchlist. If the price drops from $90 to $78 I will ping you again saying the price is dropping.\r\n");
                sb.append(" :small_orange_diamond: you have salsa on your watch list you have a low alert of $61. If the price of salsa is $58 I will ping you.\r\n");
                sb.append("\r\nOther commands:\r\n");
                sb.append(" :small_blue_diamond: **" + paramDelete + "** - delete all triggers, alerts and watchlist\r\n");
                sb.append(" :small_blue_diamond: **" + parmaAlert + "** - view your alerts\r\n");
                sb.append(" :small_blue_diamond: **" + paramTrigger + "** - view your triggers\r\n");
                sb.append(" :small_blue_diamond: **" + paramWatch + "** - view your watchlist\r\n");
                message.getChannel().block().createMessage(sb.toString()).block();
            }
        } catch (Exception e) {
            printException(e);
        }

        return Mono.empty();
    }

}
