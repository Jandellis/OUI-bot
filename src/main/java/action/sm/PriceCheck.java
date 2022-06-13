package action.sm;

import action.Action;
import bot.Config;
import bot.Sauce;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class PriceCheck extends Action {


    Config config = Config.getInstance();
    int startMin;
    String smUpdate;
    String smChannel;
    String cheapPing;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public PriceCheck() {

        smUpdate = config.get("smUpdate");
        cheapPing = config.get("cheapPing");
        smChannel = config.get("smChannel");
        startMin = Integer.parseInt(config.get("priceCheck"));
    }

    public void loadPrices() {
        try {


            WebClient webClient = new WebClient();
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.addRequestHeader("token", "123");
            String data = webClient.getPage("https://tacoshack.online/api/saucemarket").getWebResponse().getContentAsString();

            logger.info(data);


            JSONParser jsonParser = new JSONParser();

            Object obj = jsonParser.parse(data);
            HashMap<Sauce, Integer> prices = new HashMap<>();

            for (Sauce sauce : Sauce.values()) {
                int price = Integer.parseInt(((JSONObject)((JSONObject) obj).get(sauce.getName())).get("price").toString());

                logger.info(sauce + " at $" + price);
                prices.put(sauce, price);
            }


            Utils.updatePrices(prices.get(Sauce.pico),
                    prices.get(Sauce.guacamole),
                    prices.get(Sauce.salsa),
                    prices.get(Sauce.hotsauce),
                    prices.get(Sauce.chipotle));

            logger.info("Loading alerts");

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
        } catch (Exception e) {
            printException(e);
        }

    }

    public void startUp() {
        LocalDateTime nextRunTime = LocalDateTime.now().minusMinutes(1);

        BufferedReader unlockReader = null;
        try {
            unlockReader = new BufferedReader(new FileReader(new File("priceCheck.txt")));

            String line;
            while ((line = unlockReader.readLine()) != null) {
                nextRunTime = LocalDateTime.parse(line, formatter);
            }
            unlockReader.close();
            if (LocalDateTime.now().isAfter(nextRunTime)) {
                loadPrices();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }


    /**
     * Run task once a hour
     */
    public void start() {

        Runnable taskWrapper = new Runnable() {

            @Override
            public void run() {
                logger.info("running price check");
                loadPrices();
                start();
            }

        };
        long delay = computeNextDelay();


        LocalDateTime priceCheckTime = LocalDateTime.now().plusMinutes(delay);
        logger.info("price check at " + formatter.format(priceCheckTime));
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("priceCheck.txt"));
            writer.write(formatter.format(priceCheckTime));
            writer.close();
        } catch (IOException e) {
            printException(e);
        }

        executorService.schedule(taskWrapper, delay, TimeUnit.MINUTES);
    }

    private long computeNextDelay() {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        LocalDateTime localNow = LocalDateTime.now();


        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);


        int min = zonedNow.getMinute();
        long delay = 60;
        if (min > startMin)
            delay = (60 + startMin) - min;
        if (min < startMin)
            delay = startMin - min;
        logger.info("running price check with delay of " + delay);

        return delay;
    }

    public void stop() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
        }
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

        logger.info("now: " + now + ", hour + 1: " + hour1 + ", hour + 2:" + hour2);

        if (hour1 == null) {
            hour1 = now;
        }
        if (hour2 == null) {
            hour2 = hour1;
        }

        Integer dif = now - hour1;
        Integer dif2 = hour1 - hour2;

        if (dif < -9) {
            int drop = dif * -1;
            sb.append("\r\ndown $" + drop + " last hour ");
            dropping.set(true);
        }
        if (dif < 0 && dif2 < 0) {
            int drop2 = (now - hour2) * -1;
            sb.append("\r\ndown $" + drop2 + " last 2 hours");
            dropping.set(true);
        }


        if (dropping.get())
            client.getChannelById(Snowflake.of(smChannel)).createMessage(sb.toString()).block();
        else {
            logger.info("No dropping sauce");
        }
    }

    @Override
    protected Mono<Object> doAction(Message message) {
        return Mono.empty();
    }
}
