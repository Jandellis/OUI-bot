package action;

import bot.Sauce;
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
        if (message.getChannelId().asString().equals(smUpdate)) {
            if (message.getAuthor().get().getId().asString().equals(bbBot)) {
                for (Embed embed : message.getEmbeds()) {

                    String title = embed.getData().author().get().name().get();
                    if (title.contains("Hourly Sauce Market Update")) {
                        System.out.println("Got SM update");
                        HashMap<Sauce, Integer> prices = new HashMap<>();

                        String[] sauces = embed.getData().description().get().split("\n\n");
                        for (String sauce : sauces) {
                            String[] info = sauce.split("\n");
                            Sauce sauceName = Sauce.getSauce(info[0]);
                            int price = Integer.parseInt(info[1].replace("$", " ").split("  ")[1]);
                            prices.put(sauceName, price);
                        }
                        print(prices);
                        System.out.println("Finished");
                    }
                }
            }
        }
        return Mono.empty();
    }

    public void print(HashMap<Sauce, Integer> prices) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);
        sb.append("<@&" + cheapPing + "> we have some cheap sauce\r\n");


        prices.forEach((sauce, price) -> {
            if (cheapPrice > price && price != -1) {
                sb.append(" - " + sauce.getName() + " $" + price + "\r\n");
                cheap.set(true);
            }
        });

        if (cheap.get())
            client.getChannelById(Snowflake.of(smUpdate)).createMessage(sb.toString()).block();
        else {
            System.out.println("No cheap sauce");
        }
    }
}
