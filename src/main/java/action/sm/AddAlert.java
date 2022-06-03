package action.sm;

import action.Action;
import bot.Sauce;
import bot.SauceObject;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddAlert extends Action {
    int startMin;

    int cheapPrice = 45;
    String bbBot = "801210683483619438";
    String smUpdate = "884718327753211964";
    String cheapPing = "975421913818095656";
    String param2;
    String param3;
    String param4;
    String smChannel = "841034380822577182";

    public AddAlert() {
        startMin = 22;
        cheapPrice = Integer.parseInt(config.get("cheapPrice"));
        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
        param = "ouiSmDrop";
        param2 = "ouiSmDelete";
        param3 = "ouiSmHigh";
        param4 = "ouiSmAlert";
        smChannel = config.get("smChannel");
    }

    /*
    look at messagedata.embeds[0].author_value.iconUrl_value
    https://cdn.discordapp.com/avatars/292839877563908097/17211f5921073e431a0f28f6f4f864be.webp

    292839877563908097 is the id

     */

    @Override
    public Mono<Object> doAction(Message message) {

        String action = getAction(message);
        if (action != null) {
            String userId = message.getAuthor().get().getId().asString();
            Utils.addTrigger(userId, AlertType.drop, 0);
            message.getChannel().block().createMessage("I will alert you when your sauces drop").block();
        }

        action = getAction(message, param2.toLowerCase());
        if (action != null) {
            String userId = message.getAuthor().get().getId().asString();
            Utils.deleteAlert(userId);
            Utils.deleteTrigger(userId);
            message.getChannel().block().createMessage("Alerts deleted").block();
        }

        action = getAction(message, param3.toLowerCase());
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

        action = getAction(message, param4.toLowerCase());
        if (action != null) {
            String userId = message.getAuthor().get().getId().asString();
            StringBuilder sb = new StringBuilder("Your Alerts");
            for (Alert alert :Utils.loadAlerts(userId)) {
                sb.append("\r\n - Alert " + alert.getType().getName());
                sb.append(" for " + alert.getTrigger());
                if (alert.getPrice() > 0)
                    sb.append(" at $" + alert.getPrice());
            }
            message.getChannel().block().createMessage(sb.toString()).block();
        }

        return Mono.empty();
    }

}
