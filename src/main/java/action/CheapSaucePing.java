package action;

import bot.Sauce;
import bot.SauceObject;
import discord4j.common.util.Snowflake;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheapSaucePing extends Action {
    int startMin;

    int cheapPrice = 45;
    String bbBot = "801210683483619438";
    String smUpdate = "884718327753211964";
    String cheapPing = "975421913818095656";

    public CheapSaucePing() {
        startMin = 22;
        cheapPrice = Integer.parseInt(config.get("cheapPrice"));
        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
    }


    @Override
    public Mono<Object> doAction(Message message) {
        try {
            if (message.getChannelId().asString().equals(smUpdate)) {
                if (message.getAuthor().isPresent() && message.getAuthor().get().getId().asString().equals(bbBot)) {
                    for (Embed embed : message.getEmbeds()) {

                        String title = embed.getData().author().get().name().get();
                        if (title.contains("Hourly Sauce Market Update")) {
                            logger.info("Got SM update");
                            HashMap<SauceObject, Integer> prices = new HashMap<>();

                            String[] sauces = embed.getData().description().get().split("\n\n");
                            for (String sauce : sauces) {
                                String[] info = sauce.split("\n");
                                Sauce sauceName = Sauce.getSauce(info[0]);
                                int price = Integer.parseInt(info[1].replace("$", " ").split("  ")[1]);
                                int oldPrice = Integer.parseInt(info[2].replace("$", " ").split("  ")[1]);
                                SauceObject sauceObject = new SauceObject(sauceName, oldPrice, price);
                                prices.put(sauceObject, price);
                            }
                            print(prices);
                            logger.info("Finished");
                        }
                    }
                }
            }
        } catch (Exception e) {
            printException(e);
        }
        return Mono.empty();
    }

    public void print(HashMap<SauceObject, Integer> prices) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);
        sb.append("<@&" + cheapPing + "> we have some cheap sauce\r\n");


        prices.forEach((sauce, price) -> {
            if (cheapPrice > price && price != -1) {
                int difference = sauce.getOldPrice() - sauce.getPrice();
                String move = " No change";
                if (difference > 0) {
                    move = " :chart_with_downwards_trend: down " + difference;
                }
                if (difference < 0) {
                    difference = difference * -1;
                    move = " :chart_with_upwards_trend: up " + difference;
                }

                sb.append(" - " + sauce.getSauce().getName() + " $" + price + move + "\r\n");
                cheap.set(true);
            }
        });

        if (cheap.get())
            client.getChannelById(Snowflake.of(smUpdate)).createMessage(sb.toString()).block();
        else {
            logger.info("No cheap sauce");
        }
    }
}
