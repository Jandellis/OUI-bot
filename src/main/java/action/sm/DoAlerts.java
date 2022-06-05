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

public class DoAlerts extends Action {
    int startMin;

    int cheapPrice = 45;
    String bbBot = "801210683483619438";
    String smUpdate = "884718327753211964";
//    String smChannel = "889662502324039690"; //test server
    String smChannel = "841034380822577182";
    String cheapPing = "975421913818095656";

    public DoAlerts() {
        startMin = 22;
        cheapPrice = Integer.parseInt(config.get("cheapPrice"));
        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
//        smChannel = config.get("smChannel");
    }



    @Override
    public Mono<Object> doAction(Message message) {
        if (message.getChannelId().asString().equals(smUpdate)) {
            if (message.getAuthor().get().getId().asString().equals(bbBot)) {
                for (Embed embed : message.getEmbeds()) {

                    String title = embed.getData().author().get().name().get();
                    if (title.contains("Hourly Sauce Market Update")) {
                        System.out.println("Got SM update - Do Alerts");
                        HashMap<Sauce, Integer> prices = new HashMap<>();

                        String[] sauces = embed.getData().description().get().split("\n\n");
                        for (String sauce : sauces) {
                            String[] info = sauce.split("\n");
                            Sauce sauceName = Sauce.getSauce(info[0]);
                            int price = Integer.parseInt(info[1].replace("$", " ").split("  ")[1]);

                            prices.put(sauceName, price);
                        }
                        Utils.updatePrices(prices.get(Sauce.pico),
                                prices.get(Sauce.guacamole),
                                prices.get(Sauce.salsa),
                                prices.get(Sauce.hotsauce),
                                prices.get(Sauce.chipotle));

                        for (Alert alert :Utils.loadAlerts()) {
                            if (alert.type == AlertType.drop) {

                                HashMap<Integer, Integer> saucePrices = Utils.loadLast3(Sauce.getSauce(alert.getTrigger()));
                                printDrop(saucePrices, Sauce.getSauce(alert.getTrigger()), alert.getName());
                            }
                            if (alert.type == AlertType.high) {
                                int price = alert.getPrice();
                                printHigh(prices, price, alert.getName(), alert.getTrigger());
                            }

                        }
                        System.out.println("Finished");
                    }
                }
            }
        }
        return Mono.empty();
    }



    public void printHigh(HashMap<Sauce, Integer> prices, int priceTrigger, String person, String sauceName) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);
        sb.append("<@" + person + "> price is high\r\n");


        prices.forEach((sauce, price) -> {
            if (priceTrigger < price && price != -1 && sauce.getName().equals(sauceName)) {

                sb.append(" - " + sauce.getName() + " $" + price + "\r\n");
                cheap.set(true);
            }
        });

        if (cheap.get())
            client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            System.out.println("No cheap sauce");
        }
    }

    public void printDrop(HashMap<Integer, Integer> prices, Sauce sauce, String person) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean dropping = new AtomicBoolean(false);
        sb.append("<@" + person + "> " + sauce + " is dropping");

        Integer now = prices.get(0);
        Integer hour1 = prices.get(1);
        Integer hour2 = prices.get(2);

        if (hour1 == null) {
            hour1 = now;
        }
        if (hour2 == null) {
            hour2 = hour1;
        }

        Integer dif = now - hour1;
        Integer dif2 = hour1 - hour2;

        if (dif < -9 ) {
            int drop = dif * -1;
            sb.append("\r\ndown $" + drop+ " last hour ");
            dropping.set(true);
        }
        if (dif < 0 && dif2 < 0) {
            int drop2 = (now - hour2) * -1;
            sb.append("\r\ndown $"+ drop2 +" last 2 hours");
            dropping.set(true);
        }


        if (dropping.get())
            client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            System.out.println("No cheap sauce");
        }
    }

}
