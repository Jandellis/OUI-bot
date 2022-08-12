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
    String bbBot = "801210683483619438";
    String smUpdate;
    String smChannel;
    String cheapPing;

    public DoAlerts() {
        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
        smChannel = config.get("smChannel");
    }



    @Override
    public Mono<Object> doAction(Message message) {
        if (message.getChannelId().asString().equals(smUpdate)) {
            if (message.getAuthor().isPresent() && message.getAuthor().get().getId().asString().equals(bbBot)) {
                for (Embed embed : message.getEmbeds()) {
                    try {

                        String title = embed.getData().author().get().name().get();
                        if (title.contains("Hourly Sauce Market Update")) {
                            logger.info("Got SM update - Do Alerts for id " + message.getAuthor().get().getId());
                            HashMap<Sauce, Integer> prices = new HashMap<>();

                            String[] sauces = embed.getData().description().get().split("\n\n");
                            for (String sauce : sauces) {
                                String[] info = sauce.split("\n");
                                Sauce sauceName = Sauce.getSauce(info[0]);
                                int price = Integer.parseInt(info[1].replace("$", " ").split("  ")[1]);

                                prices.put(sauceName, price);
                            }
                            logger.info("Got prices ");
                            prices.forEach((sauce, price) -> logger.info(sauce.getName() + " at $" + price));
                            Utils.updatePrices(prices.get(Sauce.pico),
                                    prices.get(Sauce.guacamole),
                                    prices.get(Sauce.salsa),
                                    prices.get(Sauce.hotsauce),
                                    prices.get(Sauce.chipotle));

                            logger.info("Loading alerts ");

                            for (Alert alert : Utils.loadAlerts()) {
                                logger.info(alert);
                                if (alert.type == AlertType.drop) {
                                    HashMap<Integer, Integer> saucePrices = Utils.loadLast3(Sauce.getSauce(alert.getTrigger()));
                                    printDrop(saucePrices, Sauce.getSauce(alert.getTrigger()), alert.getName());
                                }
                                if (alert.type == AlertType.high) {
                                    int price = alert.getPrice();
                                    printHigh(prices, price, alert.getName(), alert.getTrigger());
                                }
                                if (alert.type == AlertType.low) {
                                    int price = alert.getPrice();
                                    printLow(prices, price, alert.getName(), alert.getTrigger());
                                }

                            }
                            logger.info("Finished");
                        }
                    } catch (Exception e) {
                        printException(e);
                    }
                }
            }
        }
        return Mono.empty();
    }


    public void printLow(HashMap<Sauce, Integer> prices, int priceTrigger, String person, String sauceName) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);
        sb.append("<@" + person + "> price is low\r\n");


        prices.forEach((sauce, price) -> {
            if (priceTrigger > price && price != -1 && sauce.getName().equals(sauceName)) {
                logger.info("price is " + price);

                sb.append(" - " + sauce.getName() + " $" + price + "\r\n");
                cheap.set(true);
            }
        });

        if (cheap.get())
            client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            logger.info("No low sauce");
        }
    }

    public void printHigh(HashMap<Sauce, Integer> prices, int priceTrigger, String person, String sauceName) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean cheap = new AtomicBoolean(false);
        sb.append("<@" + person + "> price is high\r\n");


        prices.forEach((sauce, price) -> {
            if (priceTrigger < price && price != -1 && sauce.getName().equals(sauceName)) {
                logger.info("price is " + price);
                sb.append(" - " + sauce.getName() + " $" + price + "\r\n");
                cheap.set(true);
            }
        });

        if (cheap.get())
            client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            logger.info("No high sauce");
        }
    }

    public void printDrop(HashMap<Integer, Integer> prices, Sauce sauce, String person) {

        StringBuilder sb = new StringBuilder();
        AtomicBoolean dropping = new AtomicBoolean(false);
        sb.append("<@" + person + "> " + sauce + " is dropping");

        Integer now = prices.get(0);
        Integer hour1 = prices.get(1);
        Integer hour2 = prices.get(2);

        logger.info("now: " + now +", hour + 1: " + hour1 +", hour + 2:"+ hour2);

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
            logger.info("No dropping sauce");
        }
    }

}
